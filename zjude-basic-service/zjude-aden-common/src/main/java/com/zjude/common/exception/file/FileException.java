package com.zjude.common.exception.file;

import com.zjude.common.exception.base.BaseException;

/**
 * 文件信息异常�? * 
 * @author bai
 */
public class FileException extends BaseException
{
    private static final long serialVersionUID = 1L;

    public FileException(String code, Object[] args)
    {
        super("file", code, args, null);
    }

}
