package com.zjude.common.exception.user;

/**
 * 黑名单IP异常�? * 
 * @author bai
 */
public class BlackListException extends UserException
{
    private static final long serialVersionUID = 1L;

    public BlackListException()
    {
        super("login.blocked", null);
    }
}
