package com.zjude.system.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zjude.common.enums.ZjudgOrderPaymentSource;
import com.zjude.common.exception.ServiceException;
import com.zjude.common.utils.StringUtils;
import com.zjude.common.utils.uuid.base.BaseKeyGenerate;
import com.zjude.system.domain.ZjudgDispatchRecord;
import com.zjude.system.domain.ZjudgMaster;
import com.zjude.system.domain.ZjudgOrder;
import com.zjude.system.domain.ZjudgService;
import com.zjude.system.mapper.ZjudgDispatchRecordMapper;
import com.zjude.system.mapper.ZjudgMasterMapper;
import com.zjude.system.mapper.ZjudgOrderMapper;
import com.zjude.system.mapper.ZjudgServiceMapper;
import com.zjude.system.service.ISysConfigService;
import com.zjude.system.service.IZjudgDispatchService;

/**
 * 自动派单服务实现。
 *
 * <p>评分模型（权重均可配置）：</p>
 * <pre>
 *   score = 技能匹配 &times; w_skill + 城市匹配 &times; w_city
 *         + (评分/5) &times; w_rating + 负载系数 &times; w_load
 * </pre>
 * <p>负载系数 = 1 / (1 + 在途订单数)，即在途越少得分越高。</p>
 *
 * <p>抢占使用带条件的 UPDATE，配合 {@code master_id IS NULL} 判断，
 * 保证并发派单时同一订单只会被一位师傅拿到。</p>
 */
@Service("zjudgDispatchService")
public class ZjudgDispatchServiceImpl implements IZjudgDispatchService
{
    private static final Logger log = LoggerFactory.getLogger(ZjudgDispatchServiceImpl.class);

    private static final String CONFIG_ENABLED = "zjudg.dispatch.enabled";

    private static final String CONFIG_MAX_CANDIDATES = "zjudg.dispatch.maxCandidates";

    private static final String CONFIG_W_SKILL = "zjudg.dispatch.weight.skill";

    private static final String CONFIG_W_CITY = "zjudg.dispatch.weight.city";

    private static final String CONFIG_W_RATING = "zjudg.dispatch.weight.rating";

    private static final String CONFIG_W_LOAD = "zjudg.dispatch.weight.load";

    /** 拒单惩罚：单次扣分与有效期，超过有效期的拒单不再计入 */
    private static final String CONFIG_W_REJECT = "zjudg.dispatch.weight.reject";

    private static final String CONFIG_REJECT_DAYS = "zjudg.dispatch.reject.windowDays";

    private static final String RECORD_KEY_PREFIX = "ZD_";

    /** 订单状态：待接单 */
    private static final String STATUS_PENDING = "0";

    /** 派单状态：未派单 */
    private static final String DISPATCH_NONE = "0";

    @Resource
    private ZjudgOrderMapper orderMapper;

    @Resource
    private ZjudgMasterMapper masterMapper;

    @Resource
    private ZjudgServiceMapper serviceMapper;

    @Resource
    private ZjudgDispatchRecordMapper recordMapper;

    @Resource
    private ISysConfigService configService;

    @Resource
    private BaseKeyGenerate keyGenerate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String autoDispatch(String orderId)
    {
        if (!"1".equals(conf(CONFIG_ENABLED, "0")))
        {
            log.debug("自动派单未启用，跳过 orderId={}", orderId);
            return null;
        }
        ZjudgOrder order = orderMapper.selectById(orderId);
        if (order == null)
        {
            throw new ServiceException("订单不存在");
        }
        if (!canDispatch(order))
        {
            return null;
        }

        List<ZjudgDispatchRecord> candidates = evaluateCandidates(orderId);
        if (candidates.isEmpty())
        {
            markDispatchFailed(orderId, "无可用候选师傅");
            return null;
        }

        // 落候选记录，便于追溯
        recordMapper.insertBatch(candidates);

        for (ZjudgDispatchRecord candidate : candidates)
        {
            ZjudgMaster master = masterMapper.selectById(candidate.getMasterId());
            if (master == null || !master.canAcceptOrders())
            {
                continue;
            }
            // 抢占：条件为「仍未指派且仍待接单」，并发下只有一个分支能成功
            int rows = orderMapper.assignMaster(orderId, candidate.getMasterId(), DISPATCH_NONE);
            if (rows == 0)
            {
                log.info("订单已被其他流程指派，停止派单 orderId={}", orderId);
                return null;
            }
            // 候选记录已在派单前批量落库，这里回写命中结果
            recordMapper.updateResult(candidate.getRecordId(), ZjudgDispatchRecord.RESULT_SUCCESS,
                    "自动派单成功，评分 " + candidate.getScore());
            log.info("自动派单成功 orderId={} masterId={} score={}", orderId, candidate.getMasterId(),
                    candidate.getScore());
            return candidate.getMasterId();
        }

        markDispatchFailed(orderId, "候选师傅均不可用");
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean manualDispatch(String orderId, String masterId, String operator)
    {
        ZjudgOrder order = orderMapper.selectById(orderId);
        if (order == null)
        {
            throw new ServiceException("订单不存在");
        }
        if (StringUtils.isNotEmpty(order.getMasterId()))
        {
            throw new ServiceException("订单已有承接师傅，请先取消后再指派");
        }
        if (!STATUS_PENDING.equals(order.getOrderStatus()))
        {
            throw new ServiceException("仅待接单的订单可以指派");
        }
        ZjudgMaster master = masterMapper.selectById(masterId);
        if (master == null)
        {
            throw new ServiceException("师傅不存在");
        }
        if (!master.canAcceptOrders())
        {
            throw new ServiceException("该师傅当前不可接单（可能未通过审核或已停接）");
        }
        // 与自接单限制保持一致：不能指派给下单人自己
        if (StringUtils.isNotEmpty(master.getUserId()) && master.getUserId().equals(order.getUserId()))
        {
            throw new ServiceException("不能将订单指派给下单人本人");
        }

        int rows = orderMapper.assignMaster(orderId, masterId, null);
        if (rows == 0)
        {
            throw new ServiceException("订单状态已变更，请刷新后重试");
        }

        ZjudgDispatchRecord record = new ZjudgDispatchRecord();
        record.setRecordId(keyGenerate.snowflakeStringKey(RECORD_KEY_PREFIX));
        record.setOrderId(orderId);
        record.setOrderNo(order.getOrderNo());
        record.setMasterId(masterId);
        record.setScore(BigDecimal.ZERO);
        record.setResult(ZjudgDispatchRecord.RESULT_SUCCESS);
        record.setReason("后台手动派单：" + (operator == null ? "system" : operator));
        recordMapper.insertBatch(List.of(record));
        return true;
    }

    @Override
    public List<ZjudgDispatchRecord> evaluateCandidates(String orderId)
    {
        ZjudgOrder order = orderMapper.selectById(orderId);
        if (order == null)
        {
            throw new ServiceException("订单不存在");
        }
        // 候选不做城市硬过滤：同城作为加分项，避免本地无师傅时订单派不出去
        List<ZjudgMaster> masters = masterMapper.selectDispatchCandidates(null);
        if (masters == null || masters.isEmpty())
        {
            return List.of();
        }

        String requiredCategory = resolveServiceCategory(order.getServiceId());
        int wSkill = confInt(CONFIG_W_SKILL, 50);
        int wCity = confInt(CONFIG_W_CITY, 30);
        int wRating = confInt(CONFIG_W_RATING, 20);
        int wLoad = confInt(CONFIG_W_LOAD, 20);
        int wReject = confInt(CONFIG_W_REJECT, 15);
        int rejectWindowDays = Math.max(1, confInt(CONFIG_REJECT_DAYS, 30));
        String rejectSince = java.time.LocalDate.now().minusDays(rejectWindowDays)
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00";
        int maxCandidates = Math.max(1, confInt(CONFIG_MAX_CANDIDATES, 5));

        List<ZjudgDispatchRecord> candidates = new ArrayList<>();
        for (ZjudgMaster master : masters)
        {
            // 排除下单人本人，避免自接自完
            if (StringUtils.isNotEmpty(master.getUserId()) && master.getUserId().equals(order.getUserId()))
            {
                continue;
            }
            BigDecimal score = BigDecimal.ZERO;
            List<String> reasons = new ArrayList<>();

            if (StringUtils.isNotEmpty(requiredCategory) && skillMatches(master.getSkillCategoryIds(), requiredCategory))
            {
                score = score.add(BigDecimal.valueOf(wSkill));
                reasons.add("技能匹配");
            }
            // 同城优先：师傅未填城市视为可服务任意城市，但不加分
            if (StringUtils.isNotEmpty(order.getCityId()) && order.getCityId().equals(master.getCityId()))
            {
                score = score.add(BigDecimal.valueOf(wCity));
                reasons.add("同城");
            }
            BigDecimal rating = master.getRating() == null ? BigDecimal.ZERO : master.getRating();
            score = score.add(rating.divide(new BigDecimal("5"), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(wRating)));
            int active = masterMapper.countActiveOrders(master.getMasterId());
            BigDecimal load = BigDecimal.ONE.divide(BigDecimal.valueOf(1L + active), 4, RoundingMode.HALF_UP);
            score = score.add(load.multiply(BigDecimal.valueOf(wLoad)));
            reasons.add("在途 " + active + " 单");

            // 拒单惩罚：近期每拒一次扣固定分，最多扣到有权重上限的两倍，避免负分无限累加
            int rejected = recordMapper.countRejected(master.getMasterId(), rejectSince);
            if (rejected > 0 && wReject > 0)
            {
                int refusedDeduct = Math.min(rejected, 4) * wReject;
                score = score.subtract(BigDecimal.valueOf(refusedDeduct));
                reasons.add("近期拒单 " + rejected + " 次，扣 " + refusedDeduct);
            }

            ZjudgDispatchRecord record = new ZjudgDispatchRecord();
            record.setRecordId(keyGenerate.snowflakeStringKey(RECORD_KEY_PREFIX));
            record.setOrderId(order.getOrderId());
            record.setOrderNo(order.getOrderNo());
            record.setMasterId(master.getMasterId());
            record.setScore(score.setScale(2, RoundingMode.HALF_UP));
            record.setResult(ZjudgDispatchRecord.RESULT_CANDIDATE);
            record.setReason(String.join("，", reasons));
            candidates.add(record);
        }

        candidates.sort(Comparator.comparing(ZjudgDispatchRecord::getScore).reversed());
        return candidates.size() > maxCandidates ? candidates.subList(0, maxCandidates) : candidates;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int dispatchPendingOrders()
    {
        if (!"1".equals(conf(CONFIG_ENABLED, "0")))
        {
            return 0;
        }
        List<ZjudgOrder> pending = orderMapper.selectDispatchPending();
        int success = 0;
        for (ZjudgOrder order : pending)
        {
            try
            {
                if (autoDispatch(order.getOrderId()) != null)
                {
                    success++;
                }
            }
            catch (Exception exception)
            {
                // 单笔失败不影响其他订单
                log.warn("自动派单失败 orderId={}", order.getOrderId(), exception);
            }
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectDispatch(String orderId, String masterId, String reason)
    {
        if (StringUtils.isEmpty(orderId) || StringUtils.isEmpty(masterId))
        {
            throw new ServiceException("缺少订单或师傅信息");
        }
        ZjudgOrder order = orderMapper.selectById(orderId);
        if (order == null)
        {
            throw new ServiceException("订单不存在");
        }
        if (!masterId.equals(order.getMasterId()))
        {
            throw new ServiceException("该订单未指派给你");
        }
        if (!STATUS_PENDING.equals(order.getOrderStatus()))
        {
            throw new ServiceException("订单已开工或已完成，无法拒单");
        }

        // 释放失败说明状态已被并发改变（例如用户同时取消），此时不应记拒单
        int rows = orderMapper.releaseMaster(orderId, masterId);
        if (rows == 0)
        {
            throw new ServiceException("订单状态已变更，请刷新后重试");
        }

        String note = truncateReason(reason);
        String recordId = recordMapper.selectRecordIdByOrderAndMaster(orderId, masterId);
        if (StringUtils.isNotEmpty(recordId))
        {
            recordMapper.updateResult(recordId, ZjudgDispatchRecord.RESULT_REJECTED, note);
        }
        else
        {
            // 非自动派单（抢单）场景没有候选记录，补一条，保证拒单次数可统计
            ZjudgDispatchRecord record = new ZjudgDispatchRecord();
            record.setRecordId(keyGenerate.snowflakeStringKey(RECORD_KEY_PREFIX));
            record.setOrderId(orderId);
            record.setOrderNo(order.getOrderNo());
            record.setMasterId(masterId);
            record.setScore(BigDecimal.ZERO);
            record.setResult(ZjudgDispatchRecord.RESULT_REJECTED);
            record.setReason(note);
            recordMapper.insertBatch(List.of(record));
        }
        log.info("师傅拒单 orderId={} masterId={} reason={}", orderId, masterId, note);
    }

    /**
     * 拒单原因裁剪，限制在库字段长度内。
     *
     * @param reason 原始原因
     * @return 可落库的原因
     */
    private String truncateReason(String reason)
    {
        String note = StringUtils.isEmpty(reason) ? "师傅拒单" : reason.trim();
        return note.length() > 180 ? note.substring(0, 180) : note;
    }

    @Override
    public List<ZjudgDispatchRecord> listRecords(String orderId)
    {
        return recordMapper.selectByOrderId(orderId);
    }

    @Override
    public List<ZjudgDispatchRecord> selectRecords(ZjudgDispatchRecord query)
    {
        return recordMapper.selectList(query);
    }

    @Override
    public boolean canDispatch(ZjudgOrder order)
    {
        if (order == null)
        {
            return false;
        }
        // 已指派、非待接单、已取消都不再派单
        if (StringUtils.isNotEmpty(order.getMasterId()))
        {
            return false;
        }
        if (!STATUS_PENDING.equals(order.getOrderStatus()))
        {
            return false;
        }
        if (!"1".equals(order.getPaymentStatus())
                || ZjudgOrderPaymentSource.OFFLINE.getCode().equals(order.getPaymentSource()))
        {
            return false;
        }
        if ("1".equals(order.getDispatchStatus()))
        {
            return false;
        }
        return true;
    }

    /**
     * 标记派单失败，供运营在后台看到并人工介入。
     *
     * @param orderId 订单主键
     * @param reason 原因
     */
    private void markDispatchFailed(String orderId, String reason)
    {
        orderMapper.markDispatchFailed(orderId);
        log.info("订单派单未成功 orderId={} reason={}", orderId, reason);
    }

    /**
     * 取服务所属一级分类，用于与师傅技能比对。
     *
     * @param serviceId 服务主键
     * @return 分类主键，取不到返回空串
     */
    private String resolveServiceCategory(String serviceId)
    {
        if (StringUtils.isEmpty(serviceId))
        {
            return "";
        }
        ZjudgService service = serviceMapper.selectById(serviceId);
        return service == null || service.getCategoryId() == null ? "" : service.getCategoryId();
    }

    /**
     * 判断师傅技能是否覆盖服务分类。
     *
     * <p>技能存的是逗号分隔的分类 ID，同时兼容一级与二级分类：
     * 只要任一项相同即视为匹配。</p>
     *
     * @param skillIds 师傅技能分类
     * @param categoryId 服务分类
     * @return 匹配返回 true
     */
    private boolean skillMatches(String skillIds, String categoryId)
    {
        if (StringUtils.isEmpty(skillIds) || StringUtils.isEmpty(categoryId))
        {
            return false;
        }
        for (String item : skillIds.split(","))
        {
            String trimmed = item.trim();
            if (trimmed.isEmpty())
            {
                continue;
            }
            if (trimmed.equals(categoryId))
            {
                return true;
            }
            // 二级分类前缀匹配一级分类：ZC_CLEAN_HOME 以 ZC_CLEAN 开头
            if (categoryId.startsWith(trimmed + "_") || trimmed.startsWith(categoryId + "_"))
            {
                return true;
            }
        }
        return false;
    }

    private String conf(String key, String fallback)
    {
        String value = configService.selectConfigByKey(key);
        return StringUtils.isEmpty(value) ? fallback : value.trim();
    }

    private int confInt(String key, int fallback)
    {
        try
        {
            return Integer.parseInt(conf(key, String.valueOf(fallback)));
        }
        catch (NumberFormatException exception)
        {
            return fallback;
        }
    }
}
