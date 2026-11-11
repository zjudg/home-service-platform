package com.zjude.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.zjude.system.domain.ZjudgDispatchRecord;

/**
 * 自动派单记录数据访问。
 */
public interface ZjudgDispatchRecordMapper
{
    /**
     * 批量写入派单记录。
     *
     * @param records 记录列表
     * @return 影响行数
     */
    int insertBatch(@Param("records") List<ZjudgDispatchRecord> records);

    /**
     * 按订单查询派单记录。
     *
     * @param orderId 订单主键
     * @return 记录列表
     */
    List<ZjudgDispatchRecord> selectByOrderId(@Param("orderId") String orderId);

    /**
     * 后台查询派单记录。
     *
     * @param query 查询条件
     * @return 记录列表
     */
    List<ZjudgDispatchRecord> selectList(ZjudgDispatchRecord query);

    /**
     * 更新派单结果。
     *
     * @param recordId 记录主键
     * @param result 结果
     * @param reason 说明
     * @return 影响行数
     */
    int updateResult(@Param("recordId") String recordId, @Param("result") String result,
            @Param("reason") String reason);

    /**
     * 统计师傅在指定时间窗口内的拒单次数。
     *
     * <p>时间窗口用于「近期表现」：很久以前的拒单不应永久拉低派单权重。</p>
     *
     * @param masterId 师傅主键
     * @param since 起始时间 yyyy-MM-dd HH:mm:ss
     * @return 拒单次数
     */
    int countRejected(@Param("masterId") String masterId, @Param("since") String since);

    /**
     * 查询某订单下某位师傅的候选记录主键，用于拒单时回写结果。
     *
     * @param orderId 订单主键
     * @param masterId 师傅主键
     * @return 记录主键，没有则返回 null
     */
    String selectRecordIdByOrderAndMaster(@Param("orderId") String orderId, @Param("masterId") String masterId);
}
