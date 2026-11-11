package com.zjude.common.enums;

/**
 * 家政订单状态。
 *
 * <p>状态流转：待接单 -> 已接单 -> 服务中 -> 已完成。
 * 待接单、已接单、服务中 均可取消；已完成与已取消为终态，不允许再流转。</p>
 *
 * <p>「已接单」为师傅承接订单后的中间态，保证师傅开工前订单不会被直接置为服务中或已完成。</p>
 *
 * @author bai
 */
public enum ZjudgOrderStatus
{
    /** 待接单 */
    PENDING("0", "待接单"),
    /** 已接单 */
    ACCEPTED("4", "已接单"),
    /** 服务中 */
    SERVING("1", "服务中"),
    /** 已完成 */
    FINISHED("2", "已完成"),
    /** 已取消 */
    CANCELLED("3", "已取消");

    private final String code;

    private final String label;

    ZjudgOrderStatus(String code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public String getCode()
    {
        return code;
    }

    public String getLabel()
    {
        return label;
    }

    /**
     * 根据编码解析状态，未知编码返回 null。
     *
     * @param code 状态编码
     * @return 对应状态
     */
    public static ZjudgOrderStatus of(String code)
    {
        for (ZjudgOrderStatus status : values())
        {
            if (status.code.equals(code))
            {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断当前状态是否允许迁移到目标状态。
     *
     * @param target 目标状态
     * @return 是否允许
     */
    public boolean canTransitionTo(ZjudgOrderStatus target)
    {
        if (target == null)
        {
            return false;
        }
        switch (this)
        {
            case PENDING:
                return target == ACCEPTED || target == CANCELLED;
            case ACCEPTED:
                return target == SERVING || target == CANCELLED;
            case SERVING:
                return target == FINISHED || target == CANCELLED;
            case FINISHED:
            case CANCELLED:
            default:
                return false;
        }
    }

    /**
     * 是否终态。
     *
     * @return 终态返回 true
     */
    public boolean isFinal()
    {
        return this == FINISHED || this == CANCELLED;
    }
}
