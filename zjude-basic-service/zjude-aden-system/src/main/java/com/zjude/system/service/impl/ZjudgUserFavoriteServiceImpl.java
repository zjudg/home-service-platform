package com.zjude.system.service.impl;

import java.util.List;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zjude.common.exception.ServiceException;
import com.zjude.common.utils.StringUtils;
import com.zjude.common.utils.uuid.base.BaseKeyGenerate;
import com.zjude.system.domain.ZjudgUserFavorite;
import com.zjude.system.mapper.ZjudgServiceMapper;
import com.zjude.system.mapper.ZjudgUserFavoriteMapper;
import com.zjude.system.service.IZjudgUserFavoriteService;

/**
 * 用户收藏应用服务实现。
 *
 * <p>收藏与取消都是幂等操作：重复点击不会产生重复记录，
 * 唯一索引 {@code uk_zjudg_favorite_user_service} 作为并发兜底。</p>
 */
@Service
public class ZjudgUserFavoriteServiceImpl implements IZjudgUserFavoriteService
{
    private static final String FAVORITE_KEY_PREFIX = "ZF_";

    @Resource
    private ZjudgUserFavoriteMapper favoriteMapper;

    @Resource
    private ZjudgServiceMapper serviceMapper;

    @Resource
    private BaseKeyGenerate keyGenerate;

    @Override
    public List<ZjudgUserFavorite> list(String userId)
    {
        requireUser(userId);
        return favoriteMapper.selectByUser(userId);
    }

    @Override
    public boolean isFavorite(String userId, String serviceId)
    {
        if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(serviceId))
        {
            return false;
        }
        return favoriteMapper.selectByUserAndService(userId, serviceId) != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(String userId, String serviceId)
    {
        requireUser(userId);
        if (StringUtils.isEmpty(serviceId))
        {
            throw new ServiceException("请选择要收藏的服务");
        }
        if (serviceMapper.selectById(serviceId) == null)
        {
            throw new ServiceException("服务不存在或已下架");
        }
        if (favoriteMapper.selectByUserAndService(userId, serviceId) != null)
        {
            // 已收藏直接返回，前端连点不会报错
            return;
        }

        ZjudgUserFavorite favorite = new ZjudgUserFavorite();
        favorite.setFavoriteId(keyGenerate.snowflakeStringKey(FAVORITE_KEY_PREFIX));
        favorite.setUserId(userId);
        favorite.setServiceId(serviceId);
        try
        {
            favoriteMapper.insert(favorite);
        }
        catch (DuplicateKeyException ignored)
        {
            // 并发重复收藏：唯一索引拦下，按已收藏处理
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(String userId, String serviceId)
    {
        requireUser(userId);
        if (StringUtils.isEmpty(serviceId))
        {
            throw new ServiceException("请选择要取消收藏的服务");
        }
        favoriteMapper.deleteByUserAndService(userId, serviceId);
    }

    @Override
    public int count(String userId)
    {
        if (StringUtils.isEmpty(userId))
        {
            return 0;
        }
        return favoriteMapper.countByUser(userId);
    }

    private void requireUser(String userId)
    {
        if (StringUtils.isEmpty(userId))
        {
            throw new ServiceException("请先登录");
        }
    }
}
