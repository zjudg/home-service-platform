package com.zjude.system.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import com.zjude.common.exception.ServiceException;
import com.zjude.common.utils.StringUtils;
import com.zjude.common.utils.uuid.base.BaseKeyGenerate;
import com.zjude.system.domain.ZjudgCertificateProduct;
import com.zjude.system.mapper.ZjudgCertificateProductMapper;
import com.zjude.system.service.IZjudgCertificateProductService;
import com.zjude.system.domain.enums.ZjudgCertificateProductStatus;

@Service("zjudgCertificateProductService")
public class ZjudgCertificateProductServiceImpl implements IZjudgCertificateProductService
{
    @Resource private ZjudgCertificateProductMapper mapper;
    @Resource private BaseKeyGenerate keyGenerate;

    public List<ZjudgCertificateProduct> selectList(ZjudgCertificateProduct query) { return mapper.selectList(query); }
    public List<ZjudgCertificateProduct> selectPublishedList() { return mapper.selectPublishedList(); }
    public ZjudgCertificateProduct selectById(String id) { return StringUtils.isEmpty(id) ? null : mapper.selectById(id); }
    public int insert(ZjudgCertificateProduct item)
    {
        validate(item);
        item.setProductId(keyGenerate.snowflakeStringKey("ZCP_"));
        return mapper.insert(item);
    }
    public int update(ZjudgCertificateProduct item) { validate(item); return mapper.update(item); }
    public int deleteByIds(String[] ids) { return mapper.deleteByIds(ids); }

    private void validate(ZjudgCertificateProduct item)
    {
        if (item == null || StringUtils.isEmpty(item.getProductName())) throw new ServiceException("证书名称不能为空");
        if (item.getPrice() == null || item.getPrice().signum() <= 0) throw new ServiceException("证书价格必须大于零");
        try
        {
            item.getPrice().setScale(2, RoundingMode.UNNECESSARY);
        }
        catch (ArithmeticException exception)
        {
            throw new ServiceException("证书价格最多只能有两位小数");
        }
        if (item.getPrice().precision() - item.getPrice().scale() > 10) throw new ServiceException("证书价格超出允许范围");
        if (!ZjudgCertificateProductStatus.ON_SALE.value().equals(item.getStatus())
                && !ZjudgCertificateProductStatus.OFF_SALE.value().equals(item.getStatus())) throw new ServiceException("证书状态不合法");
    }
}
