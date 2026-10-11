package com.zjude.common.enums;

/**
 * 是否标记
 *
 * @author bai
 */
public enum YesNo
{
    YES("Y", "是"), NO("N", "否");

    private final String code;
    private final String info;

    YesNo(String code, String info)
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
     * 判断是否标记编码是否合法
     *
     * @param code 是否标记编码
     * @return 结果
     */
    public static boolean isValid(String code)
    {
        for (YesNo item : values())
        {
            if (item.code.equals(code))
            {
                return true;
            }
        }
        return false;
    }
}
