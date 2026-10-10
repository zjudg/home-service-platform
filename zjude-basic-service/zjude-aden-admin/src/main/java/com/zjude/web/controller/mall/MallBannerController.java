package com.zjude.web.controller.mall;

import java.util.List;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zjude.common.annotation.Log;
import com.zjude.common.core.controller.BaseController;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.common.core.page.TableDataInfo;
import com.zjude.common.enums.BusinessType;
import com.zjude.common.utils.poi.ExcelUtil;
import com.zjude.system.domain.MallBanner;
import com.zjude.system.service.IMallBannerService;

/**
 * 轮播图信息操作处理
 *
 * @author bai
 */
@RestController
@RequestMapping("/mall/banner")
public class MallBannerController extends BaseController
{
    @Resource
    private IMallBannerService bannerService;

    /**
     * 获取轮播图列表
     */
    @PreAuthorize("@ss.hasPerm('mall:banner:list')")
    @GetMapping("/list")
    public TableDataInfo list(MallBanner banner)
    {
        startPage();
        List<MallBanner> list = bannerService.selectBannerList(banner);
        return getDataTable(list);
    }

    /**
     * 导出轮播图列表
     */
    @PreAuthorize("@ss.hasPerm('mall:banner:export')")
    @Log(title = "轮播图管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MallBanner banner)
    {
        List<MallBanner> list = bannerService.selectBannerList(banner);
        ExcelUtil<MallBanner> util = new ExcelUtil<>(MallBanner.class);
        util.exportExcel(response, list, "轮播图数据");
    }

    /**
     * 根据轮播图编号获取详细信息
     */
    @PreAuthorize("@ss.hasPerm('mall:banner:query')")
    @GetMapping(value = "/{bannerId}")
    public AjaxResult getInfo(@PathVariable String bannerId)
    {
        return success(bannerService.selectBannerById(bannerId));
    }

    /**
     * 新增轮播图
     */
    @PreAuthorize("@ss.hasPerm('mall:banner:add')")
    @Log(title = "轮播图管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MallBanner banner)
    {
        banner.setCreateBy(getUsername());
        return toAjax(bannerService.insertBanner(banner));
    }

    /**
     * 修改轮播图
     */
    @PreAuthorize("@ss.hasPerm('mall:banner:edit')")
    @Log(title = "轮播图管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MallBanner banner)
    {
        banner.setUpdateBy(getUsername());
        return toAjax(bannerService.updateBanner(banner));
    }

    /**
     * 删除轮播图
     */
    @PreAuthorize("@ss.hasPerm('mall:banner:remove')")
    @Log(title = "轮播图管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{bannerIds}")
    public AjaxResult remove(@PathVariable String[] bannerIds)
    {
        return toAjax(bannerService.deleteBannerByIds(bannerIds));
    }
}
