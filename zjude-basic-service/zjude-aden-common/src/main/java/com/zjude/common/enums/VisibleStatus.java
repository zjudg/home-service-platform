package com.zjude.common.enums;

/**
 * 显示状态
 *
 * @author bai
 */
public enum VisibleStatus
{
    SHOW("0", "显示"), HIDE("1", "隐藏");

    private final String code;
    private final String info;

    VisibleStatus(String code, String info)
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
     * 判断显示状态编码是否合法
     *
     * @param code 显示状态编码
     * @return 结果
     */
    public static boolean isValid(String code)
    {
        for (VisibleStatus status : values())
        {
            if (status.code.equals(code))
            {
                return true;
            }
        }
        return false;
    }
}
