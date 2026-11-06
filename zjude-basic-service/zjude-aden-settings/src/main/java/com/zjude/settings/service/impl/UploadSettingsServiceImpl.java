package com.zjude.settings.service.impl;

import java.util.HashMap;
import java.util.Map;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zjude.common.utils.StringUtils;
import com.zjude.integrated.storage.StorageProvider;
import com.zjude.integrated.storage.UploadConfigKeys;
import com.zjude.settings.domain.UploadSettingsRequest;
import com.zjude.settings.service.IUploadSettingsService;
import com.zjude.system.domain.SysConfig;
import com.zjude.system.service.ISysConfigService;

/**
 * 上传设置应用服务。
 */
@Service
public class UploadSettingsServiceImpl implements IUploadSettingsService
{
    private static final String CONFIG_NAME_PREFIX = "上传设置-";

    @Resource
    private ISysConfigService configService;

    @Override
    public UploadSettingsRequest getSettings()
    {
        Map<String, String> values = loadValues();
        UploadSettingsRequest settings = new UploadSettingsRequest();
        settings.setProvider(value(values, UploadConfigKeys.PROVIDER, StorageProvider.LOCAL.getCode()));

        UploadSettingsRequest.QiniuConfig qiniu = new UploadSettingsRequest.QiniuConfig();
        qiniu.setBucket(value(values, UploadConfigKeys.QINIU_BUCKET));
        qiniu.setAccessKey(value(values, UploadConfigKeys.QINIU_ACCESS_KEY));
        qiniu.setDomain(value(values, UploadConfigKeys.QINIU_DOMAIN));
        settings.setQiniu(qiniu);

        UploadSettingsRequest.AliyunConfig aliyun = new UploadSettingsRequest.AliyunConfig();
        aliyun.setBucket(value(values, UploadConfigKeys.ALIYUN_BUCKET));
        aliyun.setEndpoint(value(values, UploadConfigKeys.ALIYUN_ENDPOINT));
        aliyun.setAccessKeyId(value(values, UploadConfigKeys.ALIYUN_ACCESS_KEY_ID));
        aliyun.setDir(value(values, UploadConfigKeys.ALIYUN_DIR));
        aliyun.setDomain(value(values, UploadConfigKeys.ALIYUN_DOMAIN));
        settings.setAliyun(aliyun);

        UploadSettingsRequest.TencentConfig tencent = new UploadSettingsRequest.TencentConfig();
        tencent.setBucket(value(values, UploadConfigKeys.TENCENT_BUCKET));
        tencent.setRegion(value(values, UploadConfigKeys.TENCENT_REGION));
        tencent.setSecretId(value(values, UploadConfigKeys.TENCENT_SECRET_ID));
        tencent.setDomain(value(values, UploadConfigKeys.TENCENT_DOMAIN));
        settings.setTencent(tencent);
        return settings;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSettings(UploadSettingsRequest request, String username)
    {
        Map<String, SysConfig> records = loadRecords();
        validate(request, records);
        String provider = request.getProvider().trim().toLowerCase();
        save(records, UploadConfigKeys.PROVIDER, "当前存储方式", provider, username);

        UploadSettingsRequest.QiniuConfig qiniu = safe(request.getQiniu(), UploadSettingsRequest.QiniuConfig::new);
        save(records, UploadConfigKeys.QINIU_BUCKET, "七牛云-存储空间", qiniu.getBucket(), username);
        save(records, UploadConfigKeys.QINIU_ACCESS_KEY, "七牛云-AccessKey", qiniu.getAccessKey(), username);
        save(records, UploadConfigKeys.QINIU_SECRET_KEY, "七牛云-SecretKey", secret(qiniu.getSecretKey(),
                records, UploadConfigKeys.QINIU_SECRET_KEY), username);
        save(records, UploadConfigKeys.QINIU_DOMAIN, "七牛云-空间域名", qiniu.getDomain(), username);

        UploadSettingsRequest.AliyunConfig aliyun = safe(request.getAliyun(), UploadSettingsRequest.AliyunConfig::new);
        save(records, UploadConfigKeys.ALIYUN_BUCKET, "阿里云-存储空间", aliyun.getBucket(), username);
        save(records, UploadConfigKeys.ALIYUN_ENDPOINT, "阿里云-访问节点", aliyun.getEndpoint(), username);
        save(records, UploadConfigKeys.ALIYUN_ACCESS_KEY_ID, "阿里云-AccessKeyId", aliyun.getAccessKeyId(), username);
        save(records, UploadConfigKeys.ALIYUN_ACCESS_KEY_SECRET, "阿里云-AccessKeySecret",
                secret(aliyun.getAccessKeySecret(), records, UploadConfigKeys.ALIYUN_ACCESS_KEY_SECRET), username);
        save(records, UploadConfigKeys.ALIYUN_DIR, "阿里云-目录", aliyun.getDir(), username);
        save(records, UploadConfigKeys.ALIYUN_DOMAIN, "阿里云-空间域名", aliyun.getDomain(), username);

        UploadSettingsRequest.TencentConfig tencent = safe(request.getTencent(),
                UploadSettingsRequest.TencentConfig::new);
        save(records, UploadConfigKeys.TENCENT_BUCKET, "腾讯云-存储空间", tencent.getBucket(), username);
        save(records, UploadConfigKeys.TENCENT_REGION, "腾讯云-所属地区", tencent.getRegion(), username);
        save(records, UploadConfigKeys.TENCENT_SECRET_ID, "腾讯云-SecretId", tencent.getSecretId(), username);
        save(records, UploadConfigKeys.TENCENT_SECRET_KEY, "腾讯云-SecretKey",
                secret(tencent.getSecretKey(), records, UploadConfigKeys.TENCENT_SECRET_KEY), username);
        save(records, UploadConfigKeys.TENCENT_DOMAIN, "腾讯云-空间域名", tencent.getDomain(), username);
        configService.resetConfigCache();
    }

    private void validate(UploadSettingsRequest request, Map<String, SysConfig> records)
    {
        if (request == null || StringUtils.isEmpty(request.getProvider()))
        {
            throw new IllegalArgumentException("存储方式不能为空");
        }
        StorageProvider provider = StorageProvider.fromCode(request.getProvider());
        if (!provider.getCode().equalsIgnoreCase(request.getProvider().trim()))
        {
            throw new IllegalArgumentException("不支持的存储方式：" + request.getProvider());
        }
        if (provider == StorageProvider.QINIU)
        {
            UploadSettingsRequest.QiniuConfig qiniu = request.getQiniu();
            require(qiniu == null ? null : qiniu.getBucket(), "七牛云存储空间不能为空");
            require(qiniu == null ? null : qiniu.getAccessKey(), "七牛云 AccessKey 不能为空");
            requireSecret(qiniu == null ? null : qiniu.getSecretKey(), records, UploadConfigKeys.QINIU_SECRET_KEY,
                    "七牛云 SecretKey 不能为空");
            require(qiniu == null ? null : qiniu.getDomain(), "七牛云空间域名不能为空");
        }
        else if (provider == StorageProvider.ALIYUN)
        {
            UploadSettingsRequest.AliyunConfig aliyun = request.getAliyun();
            require(aliyun == null ? null : aliyun.getBucket(), "阿里云 OSS 存储空间不能为空");
            require(aliyun == null ? null : aliyun.getEndpoint(), "阿里云 OSS 访问节点不能为空");
            require(aliyun == null ? null : aliyun.getAccessKeyId(), "阿里云 AccessKeyId 不能为空");
            requireSecret(aliyun == null ? null : aliyun.getAccessKeySecret(), records,
                    UploadConfigKeys.ALIYUN_ACCESS_KEY_SECRET, "阿里云 AccessKeySecret 不能为空");
            require(aliyun == null ? null : aliyun.getDomain(), "阿里云空间域名不能为空");
        }
        else if (provider == StorageProvider.TENCENT)
        {
            UploadSettingsRequest.TencentConfig tencent = request.getTencent();
            require(tencent == null ? null : tencent.getBucket(), "腾讯云 COS 存储空间不能为空");
            require(tencent == null ? null : tencent.getRegion(), "腾讯云 Region 不能为空");
            require(tencent == null ? null : tencent.getSecretId(), "腾讯云 SecretId 不能为空");
            requireSecret(tencent == null ? null : tencent.getSecretKey(), records,
                    UploadConfigKeys.TENCENT_SECRET_KEY, "腾讯云 SecretKey 不能为空");
            require(tencent == null ? null : tencent.getDomain(), "腾讯云空间域名不能为空");
        }
    }

    private void require(String value, String message)
    {
        if (StringUtils.isEmpty(value))
        {
            throw new IllegalArgumentException(message);
        }
    }

    private void requireSecret(String submitted, Map<String, SysConfig> records, String key, String message)
    {
        if (StringUtils.isEmpty(submitted)
                && (!records.containsKey(key) || StringUtils.isEmpty(records.get(key).getConfigValue())))
        {
            throw new IllegalArgumentException(message);
        }
    }

    private Map<String, String> loadValues()
    {
        Map<String, String> values = new HashMap<>();
        for (SysConfig config : configService.selectConfigList(new SysConfig()))
        {
            values.put(config.getConfigKey(), config.getConfigValue());
        }
        return values;
    }

    private Map<String, SysConfig> loadRecords()
    {
        Map<String, SysConfig> records = new HashMap<>();
        for (SysConfig config : configService.selectConfigList(new SysConfig()))
        {
            records.put(config.getConfigKey(), config);
        }
        return records;
    }

    private String value(Map<String, String> values, String key)
    {
        return value(values, key, "");
    }

    private String value(Map<String, String> values, String key, String defaultValue)
    {
        return StringUtils.isEmpty(values.get(key)) ? defaultValue : values.get(key);
    }

    private String secret(String submitted, Map<String, SysConfig> records, String key)
    {
        return StringUtils.isEmpty(submitted) && records.containsKey(key)
                ? records.get(key).getConfigValue() : submitted;
    }

    private void save(Map<String, SysConfig> records, String key, String name, String value, String username)
    {
        SysConfig config = records.get(key);
        if (config == null)
        {
            config = new SysConfig();
            config.setConfigName(CONFIG_NAME_PREFIX + name);
            config.setConfigKey(key);
            config.setConfigType("N");
            config.setCreateBy(username);
            config.setRemark("上传设置");
            config.setConfigValue(value == null ? "" : value.trim());
            configService.insertConfig(config);
            records.put(key, config);
        }
        else
        {
            config.setConfigValue(value == null ? "" : value.trim());
            config.setUpdateBy(username);
            configService.updateConfig(config);
        }
    }

    private <T> T safe(T value, java.util.function.Supplier<T> supplier)
    {
        return value == null ? supplier.get() : value;
    }
}
