package com.zjude.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.zjude.system.domain.ZjudgMasterWithdraw;

/**
 * 师傅提现数据访问。
 */
public interface ZjudgMasterWithdrawMapper
{
    /**
     * 新增提现申请。
     *
     * @param item 提现单
     * @return 影响行数
     */
    int insert(ZjudgMasterWithdraw item);

    /**
     * 按主键查询。
     *
     * @param withdrawId 提现主键
     * @return 提现单
     */
    ZjudgMasterWithdraw selectById(@Param("withdrawId") String withdrawId);

    /**
     * 按提现单号查询，用于转账回调定位。
     *
     * @param withdrawNo 提现单号
     * @return 提现单
     */
    ZjudgMasterWithdraw selectByWithdrawNo(@Param("withdrawNo") String withdrawNo);

    /**
     * 查询师傅最近一条进行中的提现。
     *
     * @param masterId 师傅主键
     * @return 提现单，无则返回 null
     */
    ZjudgMasterWithdraw selectInProgress(@Param("masterId") String masterId);

    /**
     * 后台条件查询。
     *
     * @param query 查询条件
     * @return 提现列表
     */
    List<ZjudgMasterWithdraw> selectList(ZjudgMasterWithdraw query);

    /**
     * 师傅本人查询自己的提现记录。
     *
     * @param masterId 师傅主键
     * @return 提现列表
     */
    List<ZjudgMasterWithdraw> selectByMaster(@Param("masterId") String masterId);

    /**
     * 更新状态。仅当当前状态与期望一致时生效，保证并发与重复操作安全。
     *
     * @param withdrawId 提现主键
     * @param fromStatus 期望的当前状态
     * @param targetStatus 目标状态
     * @param auditBy 审核人
     * @param auditRemark 审核意见
     * @param transferNo 打款单号
     * @param failReason 失败原因
     * @return 影响行数
     */
    int updateStatus(@Param("withdrawId") String withdrawId, @Param("fromStatus") String fromStatus,
            @Param("targetStatus") String targetStatus, @Param("auditBy") String auditBy,
            @Param("auditRemark") String auditRemark, @Param("transferNo") String transferNo,
            @Param("failReason") String failReason);
}
