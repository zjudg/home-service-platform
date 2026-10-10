package com.zjude.common.enums;

/**
 * 轮播图广告位置
 *
 * @author bai
 */
public enum BannerPosition
{
    INDEX("index", "首页"), CATEGORY("category", "分类页"), ACTIVITY("activity", "活动页");

    private final String code;
    private final String info;

    BannerPosition(String code, String info)
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
     * 判断广告位置编码是否合法
     *
     * @param code 广告位置编码
     * @return 结果
     */
    public static boolean isValid(String code)
    {
        for (BannerPosition position : values())
        {
            if (position.code.equals(code))
            {
                return true;
            }
        }
        return false;
    }
}
