package com.zjude.system.service.impl;

import java.math.BigDecimal;
import java.util.List;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zjude.common.exception.ServiceException;
import com.zjude.common.utils.StringUtils;
import com.zjude.common.utils.uuid.base.BaseKeyGenerate;
import com.zjude.system.domain.ZjudgMaster;
import com.zjude.system.mapper.ZjudgMasterMapper;
import com.zjude.system.service.IZjudgMasterService;

/**
 * 家政师傅档案服务实现。
 *
 * <p>身份判定与审核流转的规则集中在 {@link ZjudgMaster} 的领域判断与
 * {@link #audit} 中，避免在 Controller 层散落魔法值。</p>
 */
@Service("zjudgMasterService")
public class ZjudgMasterServiceImpl implements IZjudgMasterService
{
    /** 师傅主键前缀 */
    private static final String MASTER_KEY_PREFIX = "ZM_";

    /** 审核意见最大长度，与表结构一致 */
    private static final int AUDIT_REMARK_MAX_LENGTH = 500;

    @Resource
    private ZjudgMasterMapper mapper;

    @Resource
    private BaseKeyGenerate keyGenerate;

    @Override
    public List<ZjudgMaster> selectList(ZjudgMaster query)
    {
        return mapper.selectList(query);
    }

    @Override
    public ZjudgMaster selectById(String id)
    {
        if (StringUtils.isEmpty(id))
        {
            return null;
        }
        return mapper.selectById(id);
    }

    @Override
    public ZjudgMaster selectByUserId(String userId)
    {
        if (StringUtils.isEmpty(userId))
        {
            return null;
        }
        return mapper.selectByUserId(userId);
    }

    @Override
    public String requireActiveMaster(String userId)
    {
        if (StringUtils.isEmpty(userId))
        {
            throw new ServiceException("请先登录");
        }
        ZjudgMaster master = mapper.selectByUserId(userId);
        if (master == null)
        {
            throw new ServiceException("当前账号还不是师傅，请先提交入驻申请");
        }
        if (!ZjudgMaster.AUDIT_APPROVED.equals(master.getAuditStatus()))
        {
            // 不区分「待审核」与「已驳回」，避免通过接口探测审核进度细节
            throw new ServiceException("师傅入驻申请尚未通过审核");
        }
        if (!ZjudgMaster.STATUS_NORMAL.equals(master.getStatus()))
        {
            throw new ServiceException("师傅账号已被停用，请联系平台");
        }
        return master.getMasterId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ZjudgMaster apply(ZjudgMaster applicant)
    {
        if (applicant == null || StringUtils.isEmpty(applicant.getUserId()))
        {
            throw new ServiceException("请先登录后再申请");
        }
        if (StringUtils.isEmpty(applicant.getMasterName()))
        {
            throw new ServiceException("请填写师傅姓名");
        }
        if (StringUtils.isEmpty(applicant.getPhone()))
        {
            throw new ServiceException("请填写联系电话");
        }

        ZjudgMaster existing = mapper.selectByUserId(applicant.getUserId());
        if (existing == null)
        {
            applicant.setMasterId(keyGenerate.snowflakeStringKey(MASTER_KEY_PREFIX));
            applicant.setStatus(ZjudgMaster.STATUS_NORMAL);
            applicant.setAuditStatus(ZjudgMaster.AUDIT_PENDING);
            applicant.setAuditRemark(null);
            applicant.setAuditBy(null);
            applicant.setHomeRecommend("0");
            applicant.setRating(BigDecimal.ZERO);
            applicant.setOrderCount(0);
            // 待审核期间不能处于接单中，审核通过后再由师傅打开接单开关。
            applicant.setAccepting(ZjudgMaster.ACCEPTING_OFF);
            applicant.setCreateBy(applicant.getUserId());
            mapper.insert(applicant);
            return mapper.selectById(applicant.getMasterId());
        }

        // 已入驻账号重新提交资料：覆盖资料并回到待审核，由平台重新确认资质
        ZjudgMaster update = new ZjudgMaster();
        update.setMasterId(existing.getMasterId());
        update.setMasterName(applicant.getMasterName());
        update.setRealName(applicant.getRealName());
        update.setIdCardNo(applicant.getIdCardNo());
        update.setHealthCertUrl(applicant.getHealthCertUrl());
        update.setSkillCertUrl(applicant.getSkillCertUrl());
        update.setSkillCategoryIds(applicant.getSkillCategoryIds());
        update.setPhone(applicant.getPhone());
        update.setCityId(applicant.getCityId());
        update.setRegion(applicant.getRegion());
        update.setAvatarUrl(applicant.getAvatarUrl());
        // 重新提交资料会回到待审核，必须先关闭接单开关。
        update.setAccepting(ZjudgMaster.ACCEPTING_OFF);
        update.setUpdateBy(applicant.getUserId());
        mapper.updateProfile(update);

        if (!ZjudgMaster.AUDIT_PENDING.equals(existing.getAuditStatus()))
        {
            mapper.updateAudit(existing.getMasterId(), ZjudgMaster.AUDIT_PENDING, null, applicant.getUserId(),
                    existing.getAuditStatus());
        }
        return mapper.selectById(existing.getMasterId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(ZjudgMaster item)
    {
        if (item == null)
        {
            throw new ServiceException("师傅信息不能为空");
        }
        if (StringUtils.isEmpty(item.getUserId()))
        {
            throw new ServiceException("请先选择关联的登录账号");
        }
        if (mapper.selectByUserId(item.getUserId()) != null)
        {
            throw new ServiceException("该账号已存在师傅档案");
        }
        item.setMasterId(keyGenerate.snowflakeStringKey(MASTER_KEY_PREFIX));
        if (StringUtils.isEmpty(item.getStatus()))
        {
            item.setStatus(ZjudgMaster.STATUS_NORMAL);
        }
        if (StringUtils.isEmpty(item.getAuditStatus()))
        {
            item.setAuditStatus(ZjudgMaster.AUDIT_PENDING);
        }
        if (StringUtils.isEmpty(item.getAccepting()))
        {
            item.setAccepting(ZjudgMaster.ACCEPTING_OFF);
        }
        if (StringUtils.isEmpty(item.getHomeRecommend()))
        {
            item.setHomeRecommend("0");
        }
        if (item.getRating() == null)
        {
            item.setRating(BigDecimal.ZERO);
        }
        if (item.getOrderCount() == null)
        {
            item.setOrderCount(0);
        }
        return mapper.insert(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(ZjudgMaster item)
    {
        if (item == null || StringUtils.isEmpty(item.getMasterId()))
        {
            throw new ServiceException("师傅主键不能为空");
        }
        ZjudgMaster existing = mapper.selectById(item.getMasterId());
        if (existing == null)
        {
            throw new ServiceException("师傅档案不存在");
        }
        if (!ZjudgMaster.AUDIT_APPROVED.equals(existing.getAuditStatus()))
        {
            if (ZjudgMaster.ACCEPTING_ON.equals(item.getAccepting()))
            {
                throw new ServiceException("审核通过后才能接单");
            }
            item.setAccepting(ZjudgMaster.ACCEPTING_OFF);
        }
        else if (StringUtils.isEmpty(item.getAccepting()))
        {
            item.setAccepting(existing.getAccepting());
        }
        return mapper.update(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int audit(String masterId, String auditStatus, String auditRemark, String auditBy)
    {
        if (StringUtils.isEmpty(masterId))
        {
            throw new ServiceException("师傅主键不能为空");
        }
        if (!ZjudgMaster.AUDIT_APPROVED.equals(auditStatus) && !ZjudgMaster.AUDIT_REJECTED.equals(auditStatus))
        {
            throw new ServiceException("审核结论只能是审核通过或驳回");
        }
        String remark = auditRemark == null ? null : auditRemark.trim();
        if (ZjudgMaster.AUDIT_REJECTED.equals(auditStatus) && StringUtils.isEmpty(remark))
        {
            throw new ServiceException("驳回时必须填写审核意见");
        }
        if (remark != null && remark.length() > AUDIT_REMARK_MAX_LENGTH)
        {
            throw new ServiceException("审核意见不能超过" + AUDIT_REMARK_MAX_LENGTH + "个字符");
        }

        ZjudgMaster master = mapper.selectById(masterId);
        if (master == null)
        {
            throw new ServiceException("师傅档案不存在");
        }
        if (ZjudgMaster.AUDIT_APPROVED.equals(master.getAuditStatus())
                || ZjudgMaster.AUDIT_REJECTED.equals(master.getAuditStatus()))
        {
            throw new ServiceException("该申请已审核，请勿重复操作");
        }

        int rows = mapper.updateAudit(masterId, auditStatus, remark, auditBy, master.getAuditStatus());
        if (rows == 0)
        {
            throw new ServiceException("审核状态已变更，请刷新后重试");
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int switchAccepting(String userId, String accepting)
    {
        if (!ZjudgMaster.ACCEPTING_ON.equals(accepting) && !ZjudgMaster.ACCEPTING_OFF.equals(accepting))
        {
            throw new ServiceException("接单开关取值不合法");
        }
        String masterId = requireActiveMaster(userId);
        return mapper.updateAccepting(masterId, accepting, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(String[] ids)
    {
        if (ids == null || ids.length == 0)
        {
            return 0;
        }
        return mapper.deleteByIds(ids);
    }
}
