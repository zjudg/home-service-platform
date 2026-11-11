package com.zjude.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.zjude.system.domain.ZjudgUserAddress;

/**
 * 用户服务地址数据访问。
 *
 * <p>所有查询都带 userId 条件，避免通过 id 直接读到他人地址。</p>
 */
public interface ZjudgUserAddressMapper
{
    List<ZjudgUserAddress> selectListByUser(String userId);

    ZjudgUserAddress selectByIdAndUser(@Param("addressId") String addressId, @Param("userId") String userId);

    int countByUser(String userId);

    int insert(ZjudgUserAddress item);

    int update(ZjudgUserAddress item);

    int deleteByIdAndUser(@Param("addressId") String addressId, @Param("userId") String userId);

    /** 把该用户其余地址的默认标记清空，保证默认地址唯一。 */
    int clearDefault(String userId);

    int markDefault(@Param("addressId") String addressId, @Param("userId") String userId);
}
