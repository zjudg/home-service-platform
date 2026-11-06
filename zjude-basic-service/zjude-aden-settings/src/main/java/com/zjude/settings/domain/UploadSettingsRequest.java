package com.zjude.settings.domain;

/**
 * 上传设置请求。密钥字段为空时表示沿用已保存的密钥。
 */
public class UploadSettingsRequest
{
    private String provider;
    private QiniuConfig qiniu = new QiniuConfig();
    private AliyunConfig aliyun = new AliyunConfig();
    private TencentConfig tencent = new TencentConfig();

    public String getProvider()
    {
        return provider;
    }

    public void setProvider(String provider)
    {
        this.provider = provider;
    }

    public QiniuConfig getQiniu()
    {
        return qiniu;
    }

    public void setQiniu(QiniuConfig qiniu)
    {
        this.qiniu = qiniu;
    }

    public AliyunConfig getAliyun()
    {
        return aliyun;
    }

    public void setAliyun(AliyunConfig aliyun)
    {
        this.aliyun = aliyun;
    }

    public TencentConfig getTencent()
    {
        return tencent;
    }

    public void setTencent(TencentConfig tencent)
    {
        this.tencent = tencent;
    }

    public static class QiniuConfig
    {
        private String bucket;
        private String accessKey;
        private String secretKey;
        private String domain;

        public String getBucket() { return bucket; }
        public void setBucket(String bucket) { this.bucket = bucket; }
        public String getAccessKey() { return accessKey; }
        public void setAccessKey(String accessKey) { this.accessKey = accessKey; }
        public String getSecretKey() { return secretKey; }
        public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
        public String getDomain() { return domain; }
        public void setDomain(String domain) { this.domain = domain; }
    }

    public static class AliyunConfig
    {
        private String bucket;
        private String endpoint;
        private String accessKeyId;
        private String accessKeySecret;
        private String dir;
        private String domain;

        public String getBucket() { return bucket; }
        public void setBucket(String bucket) { this.bucket = bucket; }
        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
        public String getAccessKeyId() { return accessKeyId; }
        public void setAccessKeyId(String accessKeyId) { this.accessKeyId = accessKeyId; }
        public String getAccessKeySecret() { return accessKeySecret; }
        public void setAccessKeySecret(String accessKeySecret) { this.accessKeySecret = accessKeySecret; }
        public String getDir() { return dir; }
        public void setDir(String dir) { this.dir = dir; }
        public String getDomain() { return domain; }
        public void setDomain(String domain) { this.domain = domain; }
    }

    public static class TencentConfig
    {
        private String bucket;
        private String region;
        private String secretId;
        private String secretKey;
        private String domain;

        public String getBucket() { return bucket; }
        public void setBucket(String bucket) { this.bucket = bucket; }
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public String getSecretId() { return secretId; }
        public void setSecretId(String secretId) { this.secretId = secretId; }
        public String getSecretKey() { return secretKey; }
        public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
        public String getDomain() { return domain; }
        public void setDomain(String domain) { this.domain = domain; }
    }
}
