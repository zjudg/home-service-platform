package com.zjude.integrated.storage;

/**
 * 文件上传结果。
 */
public record UploadFileResult(String url, String fileName, String newFileName, String originalFilename)
{
}
