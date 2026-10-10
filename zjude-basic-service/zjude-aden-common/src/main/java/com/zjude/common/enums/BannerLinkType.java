package com.zjude.common.enums;

/**
 * 轮播图跳转类型
 *
 * @author bai
 */
public enum BannerLinkType
{
    NONE("0", "无跳转"), INTERNAL("1", "内部页面"), EXTERNAL("2", "外部链接");

    private final String code;
    private final String info;

    BannerLinkType(String code, String info)
    {
        this.code = code;
        this.info = info;
    }

    public String getCode()
    {
        return code;
    }

    public String getInfo()
    {
        return info;
    }

    /**
     * 判断跳转类型编码是否合法
     *
     * @param code 跳转类型编码
     * @return 结果
     */
    public static boolean isValid(String code)
    {
        for (BannerLinkType type : values())
        {
            if (type.code.equals(code))
            {
                return true;
            }
        }
        return false;
    }
}
