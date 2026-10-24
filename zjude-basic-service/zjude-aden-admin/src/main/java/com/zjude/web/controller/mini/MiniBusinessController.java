package com.zjude.web.controller.mini;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.zjude.common.annotation.Anonymous;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.common.utils.StringUtils;
import com.zjude.system.domain.ZjudgBookingSlot;
import com.zjude.system.domain.ZjudgService;
import com.zjude.system.domain.ZjudgServiceCategory;
import com.zjude.system.domain.ZjudgCity;
import com.zjude.system.service.IZjudgCategoryService;
import com.zjude.system.service.IZjudgCityService;
import com.zjude.system.service.IZjudgCityScopeService;
import com.zjude.system.service.IZjudgOrderService;
import com.zjude.system.service.IZjudgServiceService;

/**
 * 小程序公开业务数据，只读读取后台维护的数据。
 */
@RestController
@RequestMapping("/api/mini")
@Anonymous
public class MiniBusinessController
{
    /** 预约日期格式 */
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Resource
    private IZjudgServiceService serviceService;

    @Resource
    private IZjudgCategoryService categoryService;

    @Resource
    private IZjudgCityService cityService;

    @Resource
    private IZjudgOrderService orderService;

    @Resource
    private IZjudgCityScopeService cityScopeService;

    /**
     * 上架服务列表。
     *
     * @param categoryId 分类主键
     * @param serviceName 名称关键字
     * @return 服务列表
     */
    @GetMapping("/services")
    public AjaxResult services(@RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) String cityId)
    {
        ZjudgService query = new ZjudgService();
        query.setStatus("0");
        query.setCategoryId(categoryId);
        query.setServiceName(serviceName);
        query.setCityId(StringUtils.isEmpty(cityId) ? null : cityId);
        // 城市范围统一由 cityScopeService 处理：默认「全国 + 本地」，严格模式只返回本地
        return AjaxResult.success(cityScopeService.selectServices(query));
    }

    /**
     * 服务详情。
     *
     * @param id 服务主键
     * @return 服务
     */
    @GetMapping("/services/{id}")
    public AjaxResult service(@PathVariable String id)
    {
        ZjudgService item = serviceService.selectById(id);
        if (item == null || !"0".equals(item.getStatus()))
        {
            return AjaxResult.error("服务不存在或已下架");
        }
        return AjaxResult.success(item);
    }

    /**
     * 服务分类。
     *
     * @return 分类列表
     */
    @GetMapping("/categories")
    public AjaxResult categories()
    {
        ZjudgServiceCategory query = new ZjudgServiceCategory();
        query.setStatus("0");
        return AjaxResult.success(categoryService.selectList(query));
    }

    /**
     * 预约时段，附带指定日期的剩余可预约数。
     *
     * @param appointmentDate 预约日期，默认今天
     * @return 时段列表
     */
    @GetMapping("/slots")
    public AjaxResult slots(@RequestParam(required = false) String appointmentDate,
            @RequestParam(required = false) String cityId)
    {
        String date = resolveDate(appointmentDate);
        ZjudgBookingSlot query = new ZjudgBookingSlot();
        query.setStatus("0");
        // 时段城市范围同样跟随 zjudg.city.strict：默认全国 + 本地，严格模式只返回本地
        query.setCityId(StringUtils.isEmpty(cityId) ? null : cityId);
        List<ZjudgBookingSlot> slots = cityScopeService.selectSlots(query);
        for (ZjudgBookingSlot slot : slots)
        {
            int maxReserve = slot.getMaxReserve() == null ? 0 : slot.getMaxReserve();
            int used = orderService.countActiveBySlot(slot.getSlotId(), date);
            slot.setAvailableCount(Math.max(maxReserve - used, 0));
        }
        return AjaxResult.success(slots);
    }

    /**
     * 开通城市。
     *
     * @param keyword 城市名称关键字
     * @return 城市列表
     */
    @GetMapping("/cities")
    public AjaxResult cities(@RequestParam(required = false) String keyword)
    {
        ZjudgCity query = new ZjudgCity();
        query.setStatus("0");
        query.setCityName(keyword);
        return AjaxResult.success(cityService.selectList(query));
    }

    /**
     * 解析预约日期，非法或为空时回退到今天。
     *
     * @param appointmentDate 日期字符串
     * @return yyyy-MM-dd 字符串
     */
    private String resolveDate(String appointmentDate)
    {
        if (StringUtils.isNotEmpty(appointmentDate))
        {
            try
            {
                return LocalDate.parse(appointmentDate.trim(), DATE_FORMAT).format(DATE_FORMAT);
            }
            catch (DateTimeParseException ignored)
            {
                // 非法日期回退到当天，避免小程序因参数错误白屏
            }
        }
        return LocalDate.now().format(DATE_FORMAT);
    }
}
