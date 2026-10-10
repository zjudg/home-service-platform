package com.zjude.integrated.storage;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

/**
 * 统一文件存储能力，供管理端和小程序业务复用。
 */
public interface FileStorageService
{
    UploadFileResult upload(MultipartFile file) throws IOException;
}
