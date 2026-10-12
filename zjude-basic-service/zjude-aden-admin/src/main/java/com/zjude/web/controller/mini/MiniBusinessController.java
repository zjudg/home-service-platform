package com.zjude.web.controller.mini;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.zjude.common.annotation.Anonymous;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.system.domain.ZjudgBookingSlot;
import com.zjude.system.domain.ZjudgService;
import com.zjude.system.domain.ZjudgServiceCategory;
import com.zjude.system.domain.ZjudgCity;
import com.zjude.system.service.IZjudgBookingService;
import com.zjude.system.service.IZjudgCategoryService;
import com.zjude.system.service.IZjudgCityService;
import com.zjude.system.service.IZjudgServiceService;

/** 小程序公开业务数据，只读读取后台维护的数据。 */
@RestController
@RequestMapping("/api/mini")
@Anonymous
public class MiniBusinessController
{
    @Resource private IZjudgServiceService serviceService;
    @Resource private IZjudgCategoryService categoryService;
    @Resource private IZjudgBookingService bookingService;
    @Resource private IZjudgCityService cityService;

    @GetMapping("/services")
    public AjaxResult services(@RequestParam(required = false) String categoryId)
    {
        ZjudgService query = new ZjudgService();
        query.setStatus("0");
        query.setCategoryId(categoryId);
        return AjaxResult.success(serviceService.selectList(query));
    }

    @GetMapping("/services/{id}")
    public AjaxResult service(@PathVariable String id)
    {
        ZjudgService item = serviceService.selectById(id);
        if (item == null || !"0".equals(item.getStatus())) return AjaxResult.error("服务不存在或已下架");
        return AjaxResult.success(item);
    }

    @GetMapping("/categories")
    public AjaxResult categories()
    {
        ZjudgServiceCategory query = new ZjudgServiceCategory();
        query.setStatus("0");
        return AjaxResult.success(categoryService.selectList(query));
    }

    @GetMapping("/slots")
    public AjaxResult slots()
    {
        ZjudgBookingSlot query = new ZjudgBookingSlot();
        query.setStatus("0");
        return AjaxResult.success(bookingService.selectList(query));
    }

    @GetMapping("/cities")
    public AjaxResult cities(@RequestParam(required = false) String keyword)
    {
        ZjudgCity query = new ZjudgCity();
        query.setStatus("0");
        query.setCityName(keyword);
        return AjaxResult.success(cityService.selectList(query));
    }
}
