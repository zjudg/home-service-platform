package com.zjude.web.controller.zjudg;

import java.util.List;
import java.util.Map;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.common.annotation.Log;
import com.zjude.common.enums.BusinessType;
import org.springframework.web.bind.annotation.RestController;
import com.zjude.common.core.controller.BaseController;
import com.zjude.common.core.page.TableDataInfo;
import com.zjude.system.service.IZjudgUserQueryService;

/**
 * 后台用户管理接口。
 *
 * <p>只返回小程序端注册用户，不包含后台员工账号；员工账号仍由
 * {@code /system/user} 管理，两者的权限与数据范围互不干扰。</p>
 */
@RestController
@RequestMapping("/zjudg/user")
public class ZjudgUserController extends BaseController
{
    @Resource
    private IZjudgUserQueryService userQueryService;

    /**
     * 用户列表。
     *
     * @param keyword 昵称 / 账号 / 手机号
     * @param source 注册来源：mini_program / account，为空不过滤
     * @param beginTime 注册开始日期 yyyy-MM-dd
     * @param endTime 注册结束日期 yyyy-MM-dd
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果，字段已按前端展示命名
     */
    @PreAuthorize("@ss.hasPerm('zjudg:user:list')")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String beginTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        // 目前只存在小程序注册用户，来源筛选传 account 时返回空列表而不是报错
        if ("account".equals(source))
        {
            TableDataInfo empty = getDataTable(List.of());
            empty.setTotal(0);
            return empty;
        }
        Map<String, Object> page = userQueryService.pageMiniUsers(keyword, beginTime, endTime, pageNum, pageSize);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rows = (List<Map<String, Object>>) page.get("rows");
        TableDataInfo data = getDataTable(rows);
        data.setTotal(((Number) page.get("total")).longValue());
        return data;
    }

    @Log(title = "小程序用户权限", businessType = BusinessType.GRANT)
    @PreAuthorize("@ss.hasPerm('zjudg:user:admin') and @ss.hasRole('admin')")
    @PutMapping("/{userId}/mini-admin")
    public AjaxResult setMiniAdmin(@PathVariable String userId, @Valid @RequestBody MiniAdminBody body)
    {
        userQueryService.setMiniAdmin(userId, body.enabled());
        return success();
    }

    public record MiniAdminBody(@NotNull Boolean enabled) { }
}
