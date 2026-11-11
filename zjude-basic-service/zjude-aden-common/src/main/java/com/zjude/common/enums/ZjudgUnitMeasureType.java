package com.zjude.common.enums;

/** 服务单位的计量方式，由后台服务单位配置决定。 */
public enum ZjudgUnitMeasureType
{
    NONE("NONE", "无需数量"),
    HOUR("HOUR", "按小时"),
    AREA("AREA", "按面积"),
    COUNT("COUNT", "按数量");

    private final String code;
    private final String label;

    ZjudgUnitMeasureType(String code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public String getCode() { return code; }
    public String getLabel() { return label; }

    public static boolean isValid(String code)
    {
        return fromCode(code) != null;
    }

    public static ZjudgUnitMeasureType fromCode(String code)
    {
        for (ZjudgUnitMeasureType type : values())
        {
            if (type.code.equals(code)) return type;
        }
        return null;
    }
}
