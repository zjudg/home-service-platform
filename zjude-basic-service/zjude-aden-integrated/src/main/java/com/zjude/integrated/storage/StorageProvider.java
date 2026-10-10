package com.zjude.integrated.storage;

/**
 * 文件存储提供商。
 */
public enum StorageProvider
{
    LOCAL("local"),
    QINIU("qiniu"),
    ALIYUN("aliyun"),
    TENCENT("tencent");

    private final String code;

    StorageProvider(String code)
    {
        this.code = code;
    }

    public String getCode()
    {
        return code;
    }

    public static StorageProvider fromCode(String code)
    {
        for (StorageProvider provider : values())
        {
            if (provider.code.equalsIgnoreCase(code))
            {
                return provider;
            }
        }
        return LOCAL;
    }
}
