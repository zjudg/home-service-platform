package com.zjude.system.service.impl;

import java.util.List;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import com.zjude.common.exception.ServiceException;
import com.zjude.common.utils.uuid.base.BaseKeyGenerate;
import com.zjude.system.domain.ZjudgService;
import com.zjude.system.mapper.ZjudgServiceMapper;
import com.zjude.system.service.IZjudgServiceService;
import com.zjude.system.mapper.ZjudgServiceUnitMapper;
import com.zjude.system.domain.ZjudgServiceUnit;
import java.math.BigDecimal;

@Service("zjudgServiceService")
public class ZjudgServiceServiceImpl implements IZjudgServiceService {
    private static final String EXTRA_TIME_SERVICE_NAME = "补时专用";
    private static final String COMMISSION_FIXED = "FIXED";
    private static final String COMMISSION_RATE = "RATE";
    @Resource private ZjudgServiceMapper mapper;
    @Resource private ZjudgServiceUnitMapper unitMapper;
    @Resource private BaseKeyGenerate keyGenerate;
    public List<ZjudgService> selectList(ZjudgService q) { return mapper.selectList(q); }
    public ZjudgService selectById(String id) { return mapper.selectById(id); }
    public int insert(ZjudgService item) { check(item); item.setServiceId(keyGenerate.snowflakeStringKey("ZS_")); return mapper.insert(item); }
    public int update(ZjudgService item) { check(item); return mapper.update(item); }
    public int deleteByIds(String[] ids) { return mapper.deleteByIds(ids); }
    private void check(ZjudgService item) {
        // The supplement product is a standalone service record. Derive its
        // catalog behavior from the name so clients cannot create a hidden toggle.
        boolean extraTimeService = EXTRA_TIME_SERVICE_NAME.equals(item.getServiceName() == null ? null : item.getServiceName().trim());
        item.setExtraTimeOnly(extraTimeService ? 1 : 0);
        if (!Integer.valueOf(0).equals(item.getExtraTimeOnly()) && !Integer.valueOf(1).equals(item.getExtraTimeOnly())) throw new ServiceException("补时商品标记不合法");
        if (Integer.valueOf(1).equals(item.getExtraTimeOnly())) {
            ZjudgServiceUnit unit = unitMapper.selectById(item.getUnitId());
            if (unit == null || !"HOUR".equals(unit.getMeasureType())) throw new ServiceException("补时专用商品必须使用按小时计量的服务单位");
            if (item.getPrice() == null || item.getPrice().signum() <= 0) throw new ServiceException("补时商品每小时售价必须大于0");
            if (mapper.countExtraTimeProduct(item.getCityId(), item.getServiceId()) > 0) throw new ServiceException("该城市已配置补时专用商品");
        }
        if (item.getDetailImage() == null || item.getDetailImage().isBlank()) throw new ServiceException("服务详情主图不能为空");
        if (item.getProductImage() == null || item.getProductImage().isBlank()) throw new ServiceException("商品列表图不能为空");
        if (item.getPrice() == null || item.getPrice().signum() < 0) throw new ServiceException("服务卖价不能为空且不能小于0");
        if (item.getMasterPrice() == null || item.getMasterPrice().signum() < 0) throw new ServiceException("师傅接单价不能为空且不能小于0");
        if (item.getMasterPrice().compareTo(item.getPrice()) > 0) throw new ServiceException("师傅接单价不能高于服务卖价");
        String commissionType = item.getCommissionType();
        if (!COMMISSION_FIXED.equals(commissionType) && !COMMISSION_RATE.equals(commissionType)) throw new ServiceException("扣款方式只能选择固定金额或百分比");
        if (COMMISSION_RATE.equals(commissionType)) {
            BigDecimal rate = item.getCommissionRate();
            if (rate == null || rate.signum() < 0 || rate.compareTo(BigDecimal.ONE) > 0) throw new ServiceException("百分比扣款必须在0%到100%之间");
            BigDecimal expected = item.getPrice().multiply(BigDecimal.ONE.subtract(rate)).setScale(2, java.math.RoundingMode.HALF_UP);
            if (item.getMasterPrice().compareTo(expected) != 0) throw new ServiceException("百分比扣款计算的师傅到手价不正确");
        } else {
            item.setCommissionRate(BigDecimal.ZERO);
        }
        if (item.getPrice() != null && item.getOriginalPrice() != null && item.getOriginalPrice().compareTo(item.getPrice()) < 0) throw new ServiceException("划线价不能低于服务价格");
        if (!"0".equals(item.getStatus()) && !"1".equals(item.getStatus())) throw new ServiceException("服务状态不合法");
    }
}
