package com.zjude.system.service;

import java.util.List;
import com.zjude.system.domain.ZjudgUserAddress;

/**
 * 用户服务地址应用服务。
 *
 * <p>所有方法都以 userId 作为数据边界，调用方只能读写自己的地址。</p>
 */
public interface IZjudgUserAddressService
{
    /** 当前用户的地址列表，默认地址排在最前。 */
    List<ZjudgUserAddress> selectByUser(String userId);

    /** 当前用户地址详情，不存在或不属于本人时返回 null。 */
    ZjudgUserAddress selectById(String userId, String addressId);

    /** 当前用户的默认地址，没有则返回 null。 */
    ZjudgUserAddress selectDefault(String userId);

    /** 新增地址；用户的第一条地址自动设为默认。 */
    ZjudgUserAddress add(String userId, ZjudgUserAddress address);

    /** 修改地址，只能改自己的。 */
    int update(String userId, ZjudgUserAddress address);

    /** 设为默认地址。 */
    int setDefault(String userId, String addressId);

    /** 删除地址；删除的是默认地址时，把最近更新的一条补为默认。 */
    int delete(String userId, String addressId);
}
