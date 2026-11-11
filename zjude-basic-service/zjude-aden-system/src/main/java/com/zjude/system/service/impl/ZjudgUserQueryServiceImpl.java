package com.zjude.system.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zjude.common.exception.ServiceException;
import com.zjude.common.core.domain.entity.SysUser;
import com.zjude.common.core.domain.entity.SysRole;
import com.zjude.system.mapper.SysUserMapper;
import com.zjude.system.mapper.SysUserRoleMapper;
import com.zjude.system.mapper.ZjudgUserQueryMapper;
import com.zjude.system.service.IZjudgUserQueryService;

/**
 * 后台用户管理查询实现。
 *
 * <p>分页在服务层用内存切片完成，而不是 PageHelper：这里的 SQL 只有两列聚合，
 * 数据量受「小程序注册用户」范围限制，内存切片可避免 count 与列表条件漂移
 * 导致的分页错位。</p>
 */
@Service
public class ZjudgUserQueryServiceImpl implements IZjudgUserQueryService
{
    /** 单页最大条数，防止前端传入过大的 pageSize 拖垮内存 */
    private static final int MAX_PAGE_SIZE = 200;

    /** 注册来源：微信小程序（绑定 openid） */
    private static final String SOURCE_MINI = "mini_program";

    /** 来源编码到展示文案的映射，新增渠道时只需在此追加 */
    private static final Map<String, String> SOURCE_LABELS = Map.of(SOURCE_MINI, "微信小程序");

    @Resource
    private ZjudgUserQueryMapper userQueryMapper;

    @Resource
    private SysUserMapper userMapper;

    @Resource
    private SysUserRoleMapper userRoleMapper;

    @Override
    public Map<String, Object> pageMiniUsers(String keyword, String beginTime, String endTime,
            int pageNum, int pageSize)
    {
        int safeSize = pageSize <= 0 ? 10 : Math.min(pageSize, MAX_PAGE_SIZE);
        int safeNum = pageNum <= 0 ? 1 : pageNum;

        List<Map<String, Object>> all = userQueryMapper.selectMiniUsers(
                trimToNull(keyword), trimToNull(beginTime), trimToNull(endTime));

        List<Map<String, Object>> rows = new ArrayList<>();
        int from = Math.min((safeNum - 1) * safeSize, all.size());
        int to = Math.min(from + safeSize, all.size());
        for (int index = from; index < to; index++)
        {
            rows.add(present(all.get(index)));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", all.size());
        result.put("rows", rows);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setMiniAdmin(String userId, boolean enabled)
    {
        SysUser user = userMapper.selectUserById(userId);
        if (user == null || user.getWxOpenid() == null || user.getWxOpenid().isBlank())
        {
            throw new ServiceException("只能设置已注册的小程序用户");
        }
        if (enabled)
        {
            boolean alreadyGranted = user.getRoles() != null && user.getRoles().stream()
                    .map(SysRole::getRoleKey).anyMatch("mini_admin"::equals);
            if (!alreadyGranted && userRoleMapper.grantMiniAdmin(userId) == 0)
            {
                throw new ServiceException("小程序管理员角色未初始化，请先执行数据库迁移");
            }
        }
        else
        {
            userRoleMapper.revokeMiniAdmin(userId);
        }
    }

    /**
     * 补齐展示字段：注册来源文案、空值占位，避免前端对 null 做判断。
     *
     * @param row 数据库行
     * @return 展示用行
     */
    private Map<String, Object> present(Map<String, Object> row)
    {
        Map<String, Object> view = new LinkedHashMap<>(row);
        String nickName = row.get("nickName") == null ? "" : String.valueOf(row.get("nickName"));
        // 小程序未设置昵称时使用固定占位，不把登录账号泄露到用户列表。
        if (nickName.isEmpty())
        {
            nickName = "未设置昵称";
        }
        view.put("nickName", nickName);
        view.put("phoneNumber", row.get("phoneNumber") == null ? "" : String.valueOf(row.get("phoneNumber")));
        view.put("address", row.get("address") == null ? "" : String.valueOf(row.get("address")));
        view.put("addressContactName", row.get("addressContactName") == null ? "" : String.valueOf(row.get("addressContactName")));
        view.put("addressContactPhone", row.get("addressContactPhone") == null ? "" : String.valueOf(row.get("addressContactPhone")));
        // 来源以 SQL 推导出的 source 为准，不再写死：将来若接入其它注册渠道，这里自动跟随
        String source = row.get("source") == null ? SOURCE_MINI : String.valueOf(row.get("source"));
        view.put("source", source);
        view.put("sourceLabel", SOURCE_LABELS.getOrDefault(source, "其他"));
        view.put("miniAdmin", Boolean.TRUE.equals(row.get("miniAdmin")) || "1".equals(String.valueOf(row.get("miniAdmin"))));
        return view;
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
}
