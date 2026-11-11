package com.zjude.web.controller.mini;

import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zjude.common.core.controller.BaseController;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.common.utils.SecurityUtils;
import com.zjude.system.service.IZjudgUserFavoriteService;

/**
 * 小程序用户收藏接口。
 *
 * <p>用户身份一律取自登录态，不接受客户端传入 userId。</p>
 */
@RestController
@RequestMapping("/api/mini/user/favorites")
public class MiniUserFavoriteController extends BaseController
{
    @Resource
    private IZjudgUserFavoriteService favoriteService;

    /** 我的收藏列表 */
    @GetMapping
    public AjaxResult list()
    {
        return success(favoriteService.list(SecurityUtils.getLoginUser().getUserId()));
    }

    /** 收藏数量，用于「我的」页展示 */
    @GetMapping("/count")
    public AjaxResult count()
    {
        return success(favoriteService.count(SecurityUtils.getLoginUser().getUserId()));
    }

    /** 判断当前用户是否已收藏某服务，供详情页初始化按钮状态 */
    @GetMapping("/check/{serviceId}")
    public AjaxResult check(@PathVariable String serviceId)
    {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("favorite", favoriteService.isFavorite(SecurityUtils.getLoginUser().getUserId(), serviceId));
        return success(data);
    }

    /** 收藏服务 */
    @PostMapping("/{serviceId}")
    public AjaxResult add(@PathVariable String serviceId)
    {
        favoriteService.add(SecurityUtils.getLoginUser().getUserId(), serviceId);
        return success();
    }

    /** 取消收藏 */
    @DeleteMapping("/{serviceId}")
    public AjaxResult remove(@PathVariable String serviceId)
    {
        favoriteService.remove(SecurityUtils.getLoginUser().getUserId(), serviceId);
        return success();
    }
}
