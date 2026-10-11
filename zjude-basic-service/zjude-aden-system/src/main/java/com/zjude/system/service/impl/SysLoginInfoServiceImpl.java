package com.zjude.system.service.impl;

import java.util.List;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import com.zjude.system.domain.SysLoginInfo;
import com.zjude.system.mapper.SysLoginInfoMapper;
import com.zjude.system.service.ISysLoginInfoService;

/**
 * 系统访问日志情况信息 服务层处�? * 
 * @author bai
 */
@Service("sysLoginInfoService")
public class SysLoginInfoServiceImpl implements ISysLoginInfoService
{

    @Resource
    private SysLoginInfoMapper loginInfoMapper;

    /**
     * 新增系统登录日志
     * 
     * @param loginInfo 访问日志对象
     */
    @Override
    public void insertLoginInfo(SysLoginInfo loginInfo)
    {
        loginInfoMapper.insertLoginInfo(loginInfo);
    }

    /**
     * 查询系统登录日志集合
     * 
     * @param loginInfo 访问日志对象
     * @return 登录记录集合
     */
    @Override
    public List<SysLoginInfo> selectLoginInfoList(SysLoginInfo loginInfo)
    {
        return loginInfoMapper.selectLoginInfoList(loginInfo);
    }

    /**
     * 批量删除系统登录日志
     * 
     * @param infoIds 需要删除的登录日志ID
     * @return 结果
     */
    @Override
    public int deleteLoginInfoByIds(Long[] infoIds)
    {
        return loginInfoMapper.deleteLoginInfoByIds(infoIds);
    }

    /**
     * 清空系统登录日志
     */
    @Override
    public void cleanLoginInfo()
    {
        loginInfoMapper.cleanLoginInfo();
    }
}
