package com.zjude.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.zjude.system.domain.ZjudgMaster;

/**
 * 家政师傅档案数据访问。
 */
public interface ZjudgMasterMapper
{
    /**
     * 后台条件查询，联表带出登录账号名与城市名。
     *
     * @param query 查询条件
     * @return 师傅列表
     */
    List<ZjudgMaster> selectList(ZjudgMaster query);

    /**
     * 按主键查询，联表带出账号名与城市名。
     *
     * @param id 师傅主键
     * @return 师傅档案
     */
    ZjudgMaster selectById(String id);

    /**
     * 按登录账号查询，用于身份识别与入驻幂等判断。
     *
     * @param userId 登录账号主键
     * @return 师傅档案，不存在时返回 null
     */
    ZjudgMaster selectByUserId(@Param("userId") String userId);

    /**
     * 新增师傅档案。
     *
     * @param item 师傅档案
     * @return 影响行数
     */
    int insert(ZjudgMaster item);

    /**
     * 更新师傅档案的入驻资料。仅更新资料字段，不触碰审核结论与统计字段。
     *
     * @param item 师傅档案
     * @return 影响行数
     */
    int updateProfile(ZjudgMaster item);

    /**
     * 后台编辑，更新运营维护字段。
     *
     * @param item 师傅档案
     * @return 影响行数
     */
    int update(ZjudgMaster item);

    /**
     * 写入审核结论。仅当当前处于待审核状态时生效，避免并发重复审批。
     *
     * @param id 师傅主键
     * @param auditStatus 审核状态
     * @param auditRemark 审核意见
     * @param auditBy 审核人
     * @param fromStatus 期望的当前审核状态
     * @return 影响行数
     */
    int updateAudit(@Param("id") String id, @Param("auditStatus") String auditStatus,
            @Param("auditRemark") String auditRemark, @Param("auditBy") String auditBy,
            @Param("fromStatus") String fromStatus);

    /**
     * 切换接单开关。
     *
     * @param id 师傅主键
     * @param accepting 接单开关
     * @param updateBy 操作人
     * @return 影响行数
     */
    int updateAccepting(@Param("id") String id, @Param("accepting") String accepting,
            @Param("updateBy") String updateBy);

    /**
     * 完工后回写评分与完单数。
     *
     * @param id 师傅主键
     * @param rating 新的平均评分
     * @param orderCount 新的累计完单数
     * @return 影响行数
     */
    int updateStatistics(@Param("id") String id, @Param("rating") java.math.BigDecimal rating,
            @Param("orderCount") Integer orderCount);

    /**
     * 查询可派单的候选师傅。
     *
     * <p>只返回审核通过、账号正常、接单开关打开的师傅。</p>
     *
     * @param cityId 订单所属城市，可为空表示不限制
     * @return 候选师傅列表
     */
    java.util.List<ZjudgMaster> selectDispatchCandidates(@Param("cityId") String cityId);

    /**
     * 统计师傅当前在途订单数（已接单或服务中），用于派单负载均衡。
     *
     * @param masterId 师傅主键
     * @return 在途订单数
     */
    int countActiveOrders(@Param("masterId") String masterId);

    /**
     * 按主键批量删除。
     *
     * @param ids 师傅主键数组
     * @return 影响行数
     */
    int deleteByIds(String[] ids);
}
