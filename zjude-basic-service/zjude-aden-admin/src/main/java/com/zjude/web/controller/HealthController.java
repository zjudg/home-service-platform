package com.zjude.web.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zjude.common.annotation.Anonymous;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.common.core.redis.RedisCache;

/**
 * 健康检查接口。
 *
 * <p>供负载均衡与容器编排探活使用。必须匿名可访问，且不返回任何内部配置细节，
 * 仅报告各依赖是否可用。</p>
 */
@RestController
@RequestMapping("/health")
public class HealthController
{
    private static final Logger log = LoggerFactory.getLogger(HealthController.class);

    @Resource
    private RedisCache redisCache;

    @Resource
    private javax.sql.DataSource dataSource;

    /**
     * 存活探针：仅表示进程可响应，不检查依赖。
     *
     * @return 固定成功
     */
    @Anonymous
    @GetMapping("/live")
    public AjaxResult live()
    {
        return AjaxResult.success("UP");
    }

    /**
     * 就绪探针：检查数据库与 Redis 是否可用。
     *
     * <p>依赖不可用时返回 {@code status=DOWN}，由调用方决定是否摘除实例。
     * 不暴露异常堆栈与连接串。</p>
     *
     * @return 依赖状态
     */
    @Anonymous
    @GetMapping("/ready")
    public AjaxResult ready()
    {
        Map<String, Object> detail = new LinkedHashMap<>();
        boolean database = checkDatabase();
        boolean redis = checkRedis();
        detail.put("database", database ? "UP" : "DOWN");
        detail.put("redis", redis ? "UP" : "DOWN");
        boolean up = database && redis;
        AjaxResult result = up ? AjaxResult.success() : AjaxResult.error("部分依赖不可用");
        result.put("status", up ? "UP" : "DOWN");
        result.put("detail", detail);
        return result;
    }

    private boolean checkDatabase()
    {
        try (var connection = dataSource.getConnection())
        {
            return connection.isValid(2);
        }
        catch (Exception exception)
        {
            log.warn("健康检查：数据库不可用", exception);
            return false;
        }
    }

    private boolean checkRedis()
    {
        try
        {
            redisCache.hasKey("health:probe");
            return true;
        }
        catch (Exception exception)
        {
            log.warn("健康检查：Redis 不可用", exception);
            return false;
        }
    }
}
