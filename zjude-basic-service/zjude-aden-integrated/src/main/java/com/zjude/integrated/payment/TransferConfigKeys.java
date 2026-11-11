package com.zjude.integrated.payment;

/**
 * 微信商家转账（提现打款）配置键。
 *
 * <p>与支付配置共用同一个配置存储，但独立开关：
 * 未开通「商家转账到零钱」权限的商户可以保持关闭，走后台手工打款。</p>
 */
public final class TransferConfigKeys
{
    /** 自动转账开关（1 开启 / 0 关闭） */
    public static final String AUTO_TRANSFER_ENABLED = "zjudg.settle.autoTransfer";

    /** 转账场景 ID（微信商户平台申请后获得） */
    public static final String SCENE_ID = "payment.wechat.transferSceneId";

    /** 转账场景名称，用于说明文案 */
    public static final String SCENE_NAME = "payment.wechat.transferSceneName";

    /** 转账备注 */
    public static final String REMARK = "payment.wechat.transferRemark";

    private TransferConfigKeys()
    {
    }
}
