package com.zjude.web.controller.mini;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import com.zjude.common.annotation.Anonymous;
import com.zjude.common.core.controller.BaseController;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.common.exception.ServiceException;
import com.zjude.system.domain.ZjudgBookingSlot;
import com.zjude.system.domain.ZjudgCity;
import com.zjude.system.domain.ZjudgService;
import com.zjude.system.domain.ZjudgServiceCategory;
import com.zjude.system.service.IZjudgCategoryService;
import com.zjude.system.service.IZjudgCityScopeService;
import com.zjude.system.service.IZjudgCityService;
import com.zjude.system.service.IZjudgOrderService;
import com.zjude.system.service.IZjudgServiceService;

/**
 * 小程序公开目录接口。
 *
 * <p>这些接口只返回后台已启用的数据，不需要用户登录。服务和时段的城市范围
 * 统一交给 {@link IZjudgCityScopeService} 处理，避免首页、分类页和预约页各自
 * 实现一套城市过滤规则。</p>
 */
@RestController
@Validated
@Anonymous
@RequestMapping("/api/mini")
public class MiniCatalogController extends BaseController
{
    private static final DateTimeFormatter APPOINTMENT_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    @Resource private IZjudgServiceService serviceService;
    @Resource private IZjudgCategoryService categoryService;
    @Resource private IZjudgCityService cityService;
    @Resource private IZjudgCityScopeService cityScopeService;
    @Resource private IZjudgOrderService orderService;

    /** 查询已上架服务。 */
    @GetMapping("/services")
    public AjaxResult services(
            @RequestParam(required = false) @Size(max = 64) String cityId,
            @RequestParam(required = false) @Size(max = 64) String categoryId,
            @RequestParam(required = false) @Size(max = 128) String serviceName)
    {
        ZjudgService query = new ZjudgService();
        query.setStatus("0");
        query.setExtraTimeOnly(0);
        query.setCityId(trimToNull(cityId));
        query.setCategoryId(trimToNull(categoryId));
        query.setServiceName(trimToNull(serviceName));
        List<ZjudgService> rows = cityScopeService.selectServices(query);
        rows.forEach(item -> { item.setMasterPrice(null); item.setCommissionType(null); item.setCommissionRate(null); });
        return success(rows);
    }

    /** 查询单个已上架服务。 */
    @GetMapping("/services/{serviceId}")
    public AjaxResult service(@PathVariable String serviceId)
    {
        ZjudgService item = serviceService.selectById(serviceId);
        if (item == null || !"0".equals(item.getStatus()) || Integer.valueOf(1).equals(item.getExtraTimeOnly()))
        {
            return AjaxResult.error("服务不存在或已下架");
        }
        item.setMasterPrice(null);
        item.setCommissionType(null);
        item.setCommissionRate(null);
        return success(item);
    }

    /** 查询已启用服务分类。 */
    @GetMapping("/categories")
    public AjaxResult categories(@RequestParam(required = false) @Size(max = 128) String categoryName)
    {
        ZjudgServiceCategory query = new ZjudgServiceCategory();
        query.setStatus("0");
        query.setCategoryName(trimToNull(categoryName));
        return success(categoryService.selectList(query));
    }

    /** 查询已启用预约时段，并按指定日期计算剩余容量。 */
    @GetMapping("/slots")
    public AjaxResult slots(
            @RequestParam(required = false) @Size(max = 64) String cityId,
            @RequestParam(required = false) @Size(max = 32) String appointmentDate)
    {
        String date = trimToNull(appointmentDate);
        validateAppointmentDate(date);
        ZjudgBookingSlot query = new ZjudgBookingSlot();
        query.setStatus("0");
        query.setCityId(trimToNull(cityId));
        List<ZjudgBookingSlot> rows = cityScopeService.selectSlots(query);
        if (date != null)
        {
            rows.forEach(item -> {
                int maxReserve = item.getMaxReserve() == null ? 0 : item.getMaxReserve();
                item.setAvailableCount(Math.max(0,
                        maxReserve - orderService.countActiveBySlot(item.getSlotId(), date)));
            });
        }
        return success(rows);
    }

    /** 查询已启用城市。 */
    @GetMapping("/cities")
    public AjaxResult cities(@RequestParam(required = false) @Size(max = 64) String keyword)
    {
        String value = trimToNull(keyword);
        ZjudgCity query = new ZjudgCity();
        query.setStatus("0");
        List<ZjudgCity> rows = cityService.selectList(query);
        if (value != null)
        {
            rows = rows.stream()
                    .filter(item -> contains(item.getCityName(), value) || contains(item.getProvinceName(), value))
                    .toList();
        }
        return success(rows);
    }

    private void validateAppointmentDate(String value)
    {
        if (value == null)
        {
            return;
        }
        try
        {
            LocalDate.parse(value, APPOINTMENT_DATE);
        }
        catch (DateTimeParseException exception)
        {
            throw new ServiceException("预约日期格式不正确");
        }
    }

    private String trimToNull(String value)
    {
        if (value == null)
        {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean contains(String source, String keyword)
    {
        return source != null && source.contains(keyword);
    }
}
