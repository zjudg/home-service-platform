package com.zjude.system.service.impl;

import java.util.List;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zjude.common.exception.ServiceException;
import com.zjude.common.utils.StringUtils;
import com.zjude.common.utils.uuid.base.BaseKeyGenerate;
import com.zjude.system.domain.ZjudgUserAddress;
import com.zjude.system.mapper.ZjudgUserAddressMapper;
import com.zjude.system.service.IZjudgUserAddressService;

/**
 * 用户服务地址应用服务实现。
 *
 * <p>默认地址的唯一性在这里保证：任何一次「设为默认」都先清空同用户其他地址，
 * 再标记目标地址，两步在同一事务内完成。</p>
 */
@Service
public class ZjudgUserAddressServiceImpl implements IZjudgUserAddressService
{
    /** 单个用户的地址条数上限，防止恶意刷单式堆叠。 */
    private static final int MAX_ADDRESS_PER_USER = 20;

    private static final String YES = "1";
    private static final String NO = "0";

    @Resource
    private ZjudgUserAddressMapper mapper;

    @Resource
    private BaseKeyGenerate keyGenerate;

    @Override
    public List<ZjudgUserAddress> selectByUser(String userId)
    {
        requireUser(userId);
        return mapper.selectListByUser(userId);
    }

    @Override
    public ZjudgUserAddress selectById(String userId, String addressId)
    {
        requireUser(userId);
        if (StringUtils.isEmpty(addressId))
        {
            return null;
        }
        return mapper.selectByIdAndUser(addressId, userId);
    }

    @Override
    public ZjudgUserAddress selectDefault(String userId)
    {
        if (StringUtils.isEmpty(userId))
        {
            return null;
        }
        for (ZjudgUserAddress address : mapper.selectListByUser(userId))
        {
            if (YES.equals(address.getIsDefault()))
            {
                return address;
            }
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ZjudgUserAddress add(String userId, ZjudgUserAddress address)
    {
        requireUser(userId);
        validate(address);

        int count = mapper.countByUser(userId);
        if (count >= MAX_ADDRESS_PER_USER)
        {
            throw new ServiceException("地址数量已达上限，请先删除不用的地址");
        }

        boolean makeDefault = YES.equals(address.getIsDefault()) || count == 0;
        if (makeDefault)
        {
            mapper.clearDefault(userId);
        }

        ZjudgUserAddress item = new ZjudgUserAddress();
        copyEditableFields(address, item);
        item.setAddressId(keyGenerate.snowflakeStringKey("ZA_"));
        item.setUserId(userId);
        item.setIsDefault(makeDefault ? YES : NO);
        item.setStatus(NO);
        item.setCreateBy(userId);
        mapper.insert(item);
        return mapper.selectByIdAndUser(item.getAddressId(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(String userId, ZjudgUserAddress address)
    {
        requireUser(userId);
        if (address == null || StringUtils.isEmpty(address.getAddressId()))
        {
            throw new ServiceException("地址主键不能为空");
        }
        validate(address);

        ZjudgUserAddress existing = mapper.selectByIdAndUser(address.getAddressId(), userId);
        if (existing == null)
        {
            // 不区分「不存在」与「不属于本人」，避免探测他人地址
            throw new ServiceException("地址不存在");
        }

        boolean makeDefault = YES.equals(address.getIsDefault());
        if (makeDefault && !YES.equals(existing.getIsDefault()))
        {
            mapper.clearDefault(userId);
        }

        ZjudgUserAddress item = new ZjudgUserAddress();
        copyEditableFields(address, item);
        item.setAddressId(existing.getAddressId());
        item.setUserId(userId);
        item.setIsDefault(makeDefault || YES.equals(existing.getIsDefault()) ? YES : NO);
        // 状态只能由后台维护，接口不接受客户端提交，停用走独立流程
        item.setStatus(existing.getStatus());
        item.setUpdateBy(userId);
        return mapper.update(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int setDefault(String userId, String addressId)
    {
        requireUser(userId);
        if (StringUtils.isEmpty(addressId))
        {
            throw new ServiceException("请选择地址");
        }
        ZjudgUserAddress existing = mapper.selectByIdAndUser(addressId, userId);
        if (existing == null)
        {
            throw new ServiceException("地址不存在");
        }
        if (YES.equals(existing.getIsDefault()))
        {
            return 1;
        }
        mapper.clearDefault(userId);
        return mapper.markDefault(addressId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(String userId, String addressId)
    {
        requireUser(userId);
        if (StringUtils.isEmpty(addressId))
        {
            throw new ServiceException("请选择要删除的地址");
        }
        ZjudgUserAddress existing = mapper.selectByIdAndUser(addressId, userId);
        if (existing == null)
        {
            throw new ServiceException("地址不存在");
        }

        int rows = mapper.deleteByIdAndUser(addressId, userId);
        // 删掉默认地址后补一条，避免下单时没有可用默认地址
        if (rows > 0 && YES.equals(existing.getIsDefault()))
        {
            List<ZjudgUserAddress> remaining = mapper.selectListByUser(userId);
            if (!remaining.isEmpty())
            {
                mapper.markDefault(remaining.get(0).getAddressId(), userId);
            }
        }
        return rows;
    }

    private void requireUser(String userId)
    {
        if (StringUtils.isEmpty(userId))
        {
            throw new ServiceException("请先登录");
        }
    }

    private void validate(ZjudgUserAddress address)
    {
        if (address == null)
        {
            throw new ServiceException("地址信息不能为空");
        }
        if (StringUtils.isEmpty(address.getContactName()))
        {
            throw new ServiceException("请填写联系人");
        }
        if (StringUtils.isEmpty(address.getContactPhone()) || !address.getContactPhone().matches("^1\\d{10}$"))
        {
            throw new ServiceException("联系电话格式不正确");
        }
        if (StringUtils.isEmpty(address.getDetail()))
        {
            throw new ServiceException("请填写详细地址");
        }
    }

    /** 只拷贝用户可编辑的字段，防止客户端顺带修改 userId / status。 */
    private void copyEditableFields(ZjudgUserAddress source, ZjudgUserAddress target)
    {
        target.setContactName(source.getContactName().trim());
        target.setContactPhone(source.getContactPhone().trim());
        target.setCityId(source.getCityId());
        target.setRegion(source.getRegion());
        target.setDetail(source.getDetail().trim());
        target.setLabel(source.getLabel());
        target.setRemark(source.getRemark());
    }
}
