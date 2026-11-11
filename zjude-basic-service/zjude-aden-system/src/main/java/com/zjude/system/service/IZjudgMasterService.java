package com.zjude.system.service;

import java.util.List;
import com.zjude.system.domain.ZjudgMaster;

/**
 * 家政师傅档案服务。
 *
 * <p>写操作均需在应用服务边界保证事务与业务不变量：</p>
 * <ul>
 *   <li>同一登录账号只能有一条师傅档案（唯一约束兜底，服务侧先行校验）；</li>
 *   <li>入驻资料只能修改自己档案，且已通过审核的账号可重新提交以触发再审；</li>
 *   <li>审核结论只能由待审核流转到通过或驳回，重复审批必须被拒绝；</li>
 *   <li>师傅端所有接口必须先经 {@link #requireActiveMaster} 校验身份。</li>
 * </ul>
 */
public interface IZjudgMasterService
{
    /**
     * 后台条件查询。
     *
     * @param query 查询条件
     * @return 师傅列表
     */
    List<ZjudgMaster> selectList(ZjudgMaster query);

    /**
     * 按主键查询。
     *
     * @param id 师傅主键
     * @return 师傅档案，不存在时返回 null
     */
    ZjudgMaster selectById(String id);

    /**
     * 按登录账号查询师傅档案。
     *
     * @param userId 登录账号主键
     * @return 师傅档案，未入驻时返回 null
     */
    ZjudgMaster selectByUserId(String userId);

    /**
     * 校验登录账号是可用师傅，返回师傅主键。
     *
     * <p>师傅端接口的统一入口：同时要求账号未停用与审核已通过，任一不满足即抛出业务异常。</p>
     *
     * @param userId 登录账号主键
     * @return 师傅主键
     */
    String requireActiveMaster(String userId);

    /**
     * 提交入驻申请。已存在档案时按更新处理并重置为待审核。
     *
     * @param applicant 申请资料，userId 由调用方从登录态填充
     * @return 保存后的师傅档案
     */
    ZjudgMaster apply(ZjudgMaster applicant);

    /**
     * 后台新增师傅。
     *
     * @param item 师傅档案
     * @return 影响行数
     */
    int insert(ZjudgMaster item);

    /**
     * 后台编辑师傅。
     *
     * @param item 师傅档案
     * @return 影响行数
     */
    int update(ZjudgMaster item);

    /**
     * 审核师傅入驻申请。
     *
     * @param masterId 师傅主键
     * @param auditStatus 审核结论，仅接受通过或驳回
     * @param auditRemark 审核意见，驳回时必填
     * @param auditBy 审核人
     * @return 影响行数
     */
    int audit(String masterId, String auditStatus, String auditRemark, String auditBy);

    /**
     * 师傅自主切换接单开关。
     *
     * @param userId 登录账号主键
     * @param accepting 目标开关值
     * @return 影响行数
     */
    int switchAccepting(String userId, String accepting);

    /**
     * 后台批量删除。
     *
     * @param ids 师傅主键数组
     * @return 影响行数
     */
    int deleteByIds(String[] ids);
}
