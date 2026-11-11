package com.zjude.web.controller.zjudg;

import java.util.List;
import java.util.Locale;
import jakarta.annotation.Resource;
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
import com.zjude.common.enums.YesNo;
import com.zjude.system.domain.ZjudgCity;
import com.zjude.system.service.IZjudgCityService;

/**
 * 城市数据后台接口。
 *
 * <p>供服务发布、轮播投放、派单范围等场景选择和维护开通城市。</p>
 */
@RestController
@RequestMapping("/zjudg/city")
public class ZjudgCityController extends BaseController
{
    @Resource
    private IZjudgCityService cityService;

    /**
     * 城市列表。
     *
     * @param query 查询条件，可按名称、省份、首字母或状态过滤
     * @return 城市列表
     */
    @PreAuthorize("@ss.hasPerm('zjudg:city:list')")
    @GetMapping("/list")
    public TableDataInfo list(ZjudgCity query)
    {
        startPage();
        List<ZjudgCity> rows = cityService.selectList(query);
        return getDataTable(rows);
    }

    /**
     * 城市详情。
     *
     * @param cityId 城市ID
     * @return 城市信息
     */
    @PreAuthorize("@ss.hasPerm('zjudg:city:query')")
    @GetMapping("/{cityId}")
    public AjaxResult getInfo(@PathVariable String cityId)
    {
        return success(cityService.selectById(cityId));
    }

    /**
     * 新增城市。
     *
     * @param city 城市信息
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('zjudg:city:add')")
    @Log(title = "城市管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ZjudgCity city)
    {
        normalize(city);
        // 主键重复时数据库抛 DuplicateKeyException，会被全局处理器转成 500，
        // 用户只看到「系统异常」而不知道是城市ID重复，这里提前给出可读提示。
        if (cityService.selectById(city.getCityId()) != null)
        {
            return error("城市ID已存在：" + city.getCityId());
        }
        return toAjax(cityService.insert(city));
    }

    /**
     * 修改城市。
     *
     * @param city 城市信息
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('zjudg:city:edit')")
    @Log(title = "城市管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ZjudgCity city)
    {
        normalize(city);
        if (cityService.selectById(city.getCityId()) == null)
        {
            return error("城市不存在：" + city.getCityId());
        }
        return toAjax(cityService.update(city));
    }

    /**
     * 删除城市。
     *
     * @param cityIds 需要删除的城市ID数组
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('zjudg:city:remove')")
    @Log(title = "城市管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{cityIds}")
    public AjaxResult remove(@PathVariable String[] cityIds)
    {
        return toAjax(cityService.deleteByIds(cityIds));
    }

    /**
     * 首字母统一转大写、状态缺省为正常。
     *
     * <p>列表按首字母排序，大小写混排会打乱分组，因此在入口处统一处理，
     * 而不是依赖前端每次传对。</p>
     *
     * @param city 城市信息
     */
    private void normalize(ZjudgCity city)
    {
        if (city.getInitialLetter() != null)
        {
            city.setInitialLetter(city.getInitialLetter().trim().toUpperCase(Locale.ROOT));
        }
        if (city.getStatus() == null || city.getStatus().isEmpty())
        {
            city.setStatus("0");
        }
        if (city.getIsHot() == null || city.getIsHot().isEmpty())
        {
            city.setIsHot(YesNo.NO.getCode());
        }
    }
}
