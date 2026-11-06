package com.zjude.integrated.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.zjude.common.config.BaiConfig;
import com.zjude.common.utils.StringUtils;
import com.zjude.common.utils.file.FileUploadUtils;
import com.zjude.common.utils.file.FileUtils;
import com.zjude.common.utils.file.MimeTypeUtils;
import com.zjude.framework.config.ServerConfig;
import com.zjude.system.service.ISysConfigService;

/**
 * 根据 sys_config 动态选择本地、七牛、阿里云或腾讯云存储。
 */
@Service
public class ConfigurableFileStorageService implements FileStorageService
{
    @Resource
    private ISysConfigService configService;

    @Resource
    private ServerConfig serverConfig;

    @Override
    public UploadFileResult upload(MultipartFile file) throws IOException
    {
        Objects.requireNonNull(file, "上传文件不能为空");
        try
        {
            FileUploadUtils.assertAllowed(file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
        }
        catch (Exception exception)
        {
            throw new IOException(exception.getMessage(), exception);
        }
        if (file.getOriginalFilename() == null
                || file.getOriginalFilename().length() > FileUploadUtils.DEFAULT_FILE_NAME_LENGTH)
        {
            throw new IOException("上传文件名长度不能超过" + FileUploadUtils.DEFAULT_FILE_NAME_LENGTH + "个字符");
        }
        String key = FileUploadUtils.extractFilename(file);
        StorageProvider provider = StorageProvider.fromCode(configService.selectConfigByKey(UploadConfigKeys.PROVIDER));
        return switch (provider)
        {
            case LOCAL -> uploadLocal(file, key);
            case QINIU -> uploadQiniu(file, key);
            case ALIYUN -> uploadAliyun(file, key);
            case TENCENT -> uploadTencent(file, key);
        };
    }

    private UploadFileResult uploadLocal(MultipartFile file, String key) throws IOException
    {
        String fileName = FileUploadUtils.upload(BaiConfig.getUploadPath(), file);
        return result(serverConfig.getUrl() + fileName, fileName, file);
    }

    private UploadFileResult uploadQiniu(MultipartFile file, String key) throws IOException
    {
        String bucket = required(UploadConfigKeys.QINIU_BUCKET);
        String accessKey = required(UploadConfigKeys.QINIU_ACCESS_KEY);
        String secretKey = required(UploadConfigKeys.QINIU_SECRET_KEY);
        String domain = required(UploadConfigKeys.QINIU_DOMAIN);

        Configuration configuration = new Configuration(Zone.autoZone());
        UploadManager uploadManager = new UploadManager(configuration);
        Auth auth = Auth.create(accessKey, secretKey);
        try (InputStream inputStream = file.getInputStream())
        {
            Response response = uploadManager.put(inputStream, key, auth.uploadToken(bucket), null,
                    file.getContentType());
            if (!response.isOK())
            {
                throw new IOException("七牛云上传失败：" + response.error);
            }
            DefaultPutRet putRet = response.jsonToObject(DefaultPutRet.class);
            String objectKey = StringUtils.isNotEmpty(putRet.key) ? putRet.key : key;
            return result(joinDomain(domain, objectKey), objectKey, file);
        }
        catch (QiniuException exception)
        {
            throw new IOException("七牛云上传失败：" + exception.response.error, exception);
        }
    }

    private UploadFileResult uploadAliyun(MultipartFile file, String key) throws IOException
    {
        String bucket = required(UploadConfigKeys.ALIYUN_BUCKET);
        String endpoint = required(UploadConfigKeys.ALIYUN_ENDPOINT);
        String accessKeyId = required(UploadConfigKeys.ALIYUN_ACCESS_KEY_ID);
        String accessKeySecret = required(UploadConfigKeys.ALIYUN_ACCESS_KEY_SECRET);
        String dir = configService.selectConfigByKey(UploadConfigKeys.ALIYUN_DIR);
        String domain = required(UploadConfigKeys.ALIYUN_DOMAIN);
        // Aliyun Dir is the complete relative prefix; an empty Dir means the bucket root.
        String objectKey = prependDirectory(dir, fileNameOnly(key));

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try (InputStream inputStream = file.getInputStream())
        {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            ossClient.putObject(bucket, objectKey, inputStream, metadata);
            return result(joinDomain(domain, objectKey), objectKey, file);
        }
        finally
        {
            ossClient.shutdown();
        }
    }

    private UploadFileResult uploadTencent(MultipartFile file, String key) throws IOException
    {
        String bucket = required(UploadConfigKeys.TENCENT_BUCKET);
        String region = required(UploadConfigKeys.TENCENT_REGION);
        String secretId = required(UploadConfigKeys.TENCENT_SECRET_ID);
        String secretKey = required(UploadConfigKeys.TENCENT_SECRET_KEY);
        String domain = required(UploadConfigKeys.TENCENT_DOMAIN);

        COSCredentials credentials = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        clientConfig.setHttpProtocol(HttpProtocol.https);
        COSClient cosClient = new COSClient(credentials, clientConfig);
        try (InputStream inputStream = file.getInputStream())
        {
            com.qcloud.cos.model.ObjectMetadata metadata = new com.qcloud.cos.model.ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            cosClient.putObject(new PutObjectRequest(bucket, key, inputStream, metadata));
            return result(joinDomain(domain, key), key, file);
        }
        catch (CosClientException exception)
        {
            throw new IOException("腾讯云 COS 上传失败：" + exception.getMessage(), exception);
        }
        finally
        {
            cosClient.shutdown();
        }
    }

    private UploadFileResult result(String url, String fileName, MultipartFile file)
    {
        return new UploadFileResult(url, fileName, FileUtils.getName(fileName), file.getOriginalFilename());
    }

    private String required(String key)
    {
        String value = configService.selectConfigByKey(key);
        if (StringUtils.isEmpty(value))
        {
            throw new IllegalStateException("上传配置未完成：" + key);
        }
        return value.trim();
    }

    private String joinDomain(String domain, String key)
    {
        String normalizedDomain = domain.trim();
        while (normalizedDomain.endsWith("/"))
        {
            normalizedDomain = normalizedDomain.substring(0, normalizedDomain.length() - 1);
        }
        return normalizedDomain + "/" + key;
    }

    private String prependDirectory(String directory, String key)
    {
        if (StringUtils.isEmpty(directory))
        {
            return key;
        }
        String normalizedDirectory = directory.trim().replace('\\', '/');
        while (normalizedDirectory.startsWith("/"))
        {
            normalizedDirectory = normalizedDirectory.substring(1);
        }
        while (normalizedDirectory.endsWith("/"))
        {
            normalizedDirectory = normalizedDirectory.substring(0, normalizedDirectory.length() - 1);
        }
        return normalizedDirectory.isEmpty() ? key : normalizedDirectory + "/" + key;
    }

    private String fileNameOnly(String key)
    {
        int separator = key.lastIndexOf('/');
        return separator < 0 ? key : key.substring(separator + 1);
    }
}
