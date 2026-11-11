package com.zjude.web.controller.common;

import java.util.ArrayList;
import java.util.List;

import com.zjude.common.exception.base.BaseException;
import com.zjude.common.annotation.Anonymous;
import com.zjude.common.annotation.RateLimiter;
import com.zjude.common.enums.LimitType;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.zjude.common.config.BaiConfig;
import com.zjude.common.constant.Constants;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.common.utils.StringUtils;
import com.zjude.common.utils.file.FileUtils;
import com.zjude.common.utils.file.FileUploadUtils;
import com.zjude.common.utils.file.MimeTypeUtils;
import com.zjude.integrated.storage.FileStorageService;
import com.zjude.integrated.storage.UploadFileResult;

/**
 * 通用请求处理
 * 
 * @author bai
 */
@RestController
@RequestMapping("/common")
public class CommonController
{
    private static final Logger log = LoggerFactory.getLogger(CommonController.class);

    @Resource
    private FileStorageService fileStorageService;

    private static final String FILE_DELIMETER = ",";

    /**
     * 通用下载请求
     * 
     * @param fileName 文件名称
     * @param delete 是否删除
     */
    @GetMapping("/download")
    public void fileDownload(String fileName, Boolean delete, HttpServletResponse response)
    {
        try
        {
            if (!FileUtils.checkAllowDownload(fileName))
            {
                throw new Exception(StringUtils.format("文件名称({})非法，不允许下载�?", fileName));
            }
            String realFileName = System.currentTimeMillis() + fileName.substring(fileName.indexOf("_") + 1);
            String filePath = BaiConfig.getDownloadPath() + fileName;

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, realFileName);
            FileUtils.writeBytes(filePath, response.getOutputStream());
            if (delete)
            {
                FileUtils.deleteFile(filePath);
            }
        }
        catch (Exception e)
        {
            log.error("下载文件失败", e);
        }
    }

    /**
     * 通用上传请求（单个）
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, headers = "content-type=multipart/form-data")
    public AjaxResult uploadFile(@RequestPart("file") MultipartFile file) throws BaseException
    {
        try
        {
            UploadFileResult result = fileStorageService.upload(file);
            AjaxResult ajax = AjaxResult.success();
            ajax.put("url", result.url());
            ajax.put("fileName", result.fileName());
            ajax.put("newFileName", result.newFileName());
            ajax.put("originalFilename", result.originalFilename());
            return ajax;
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    /** 首次完善小程序资料时上传头像；文件仍统一走后台配置的存储提供商。 */
    @Anonymous
    @RateLimiter(time = 60, count = 20, limitType = LimitType.IP)
    @PostMapping(value = "/upload/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            headers = "content-type=multipart/form-data")
    public AjaxResult uploadAvatar(@RequestPart("file") MultipartFile file) throws BaseException
    {
        try
        {
            if (file == null || file.isEmpty())
            {
                return AjaxResult.error("头像文件不能为空");
            }
            if (file.getSize() > 5 * 1024 * 1024)
            {
                return AjaxResult.error("头像文件不能超过5MB");
            }
            FileUploadUtils.assertAllowed(file, MimeTypeUtils.IMAGE_EXTENSION);
            UploadFileResult result = fileStorageService.upload(file);
            AjaxResult ajax = AjaxResult.success();
            ajax.put("url", result.url());
            return ajax;
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 通用上传请求（多个）
     */
    @PostMapping(value = "/uploads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, headers = "content-type=multipart/form-data")
    public AjaxResult uploadFiles(@RequestPart("files") List<MultipartFile> files) throws BaseException
    {
        try
        {
            List<String> urls = new ArrayList<>();
            List<String> fileNames = new ArrayList<>();
            List<String> newFileNames = new ArrayList<>();
            List<String> originalFilenames = new ArrayList<>();
            for (MultipartFile file : files)
            {
                UploadFileResult result = fileStorageService.upload(file);
                urls.add(result.url());
                fileNames.add(result.fileName());
                newFileNames.add(result.newFileName());
                originalFilenames.add(result.originalFilename());
            }
            AjaxResult ajax = AjaxResult.success();
            ajax.put("urls", StringUtils.join(urls, FILE_DELIMETER));
            ajax.put("fileNames", StringUtils.join(fileNames, FILE_DELIMETER));
            ajax.put("newFileNames", StringUtils.join(newFileNames, FILE_DELIMETER));
            ajax.put("originalFilenames", StringUtils.join(originalFilenames, FILE_DELIMETER));
            return ajax;
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 本地资源通用下载
     */
    @GetMapping("/download/resource")
    public void resourceDownload(String resource, HttpServletResponse response)
            throws BaseException
    {
        try
        {
            if (!FileUtils.checkAllowDownload(resource))
            {
                throw new Exception(StringUtils.format("资源文件({})非法，不允许下载�?", resource));
            }
            // 本地资源路径
            String localPath = BaiConfig.getProfile();
            // 数据库资源地址
            String downloadPath = localPath + StringUtils.substringAfter(resource, Constants.RESOURCE_PREFIX);
            // 下载名称
            String downloadName = StringUtils.substringAfterLast(downloadPath, "/");
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, downloadName);
            FileUtils.writeBytes(downloadPath, response.getOutputStream());
        }
        catch (Exception e)
        {
            log.error("下载文件失败", e);
        }
    }
}
