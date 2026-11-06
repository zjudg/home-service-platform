package com.zjude.integrated.storage;

/**
 * 上传设置在 sys_config 中使用的键名。
 */
public final class UploadConfigKeys
{
    public static final String PROVIDER = "upload.storage.provider";

    public static final String QINIU_BUCKET = "upload.qiniu.bucket";
    public static final String QINIU_ACCESS_KEY = "upload.qiniu.accessKey";
    public static final String QINIU_SECRET_KEY = "upload.qiniu.secretKey";
    public static final String QINIU_DOMAIN = "upload.qiniu.domain";

    public static final String ALIYUN_BUCKET = "upload.aliyun.bucket";
    public static final String ALIYUN_ENDPOINT = "upload.aliyun.endpoint";
    public static final String ALIYUN_ACCESS_KEY_ID = "upload.aliyun.accessKeyId";
    public static final String ALIYUN_ACCESS_KEY_SECRET = "upload.aliyun.accessKeySecret";
    public static final String ALIYUN_DIR = "upload.aliyun.dir";
    public static final String ALIYUN_DOMAIN = "upload.aliyun.domain";

    public static final String TENCENT_BUCKET = "upload.tencent.bucket";
    public static final String TENCENT_REGION = "upload.tencent.region";
    public static final String TENCENT_SECRET_ID = "upload.tencent.secretId";
    public static final String TENCENT_SECRET_KEY = "upload.tencent.secretKey";
    public static final String TENCENT_DOMAIN = "upload.tencent.domain";

    private UploadConfigKeys()
    {
    }
}
