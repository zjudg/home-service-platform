package com.zjude.schedule;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.zjude.system.mapper.ZjudgScheduleLockMapper;

/**
 * 定时任务的跨实例租约。
 *
 * <p>解决的问题：多实例部署时 {@code @Scheduled} 在每个实例都会触发，
 * 自动派单这类写操作会重复执行。这里用一张锁表 + 条件 UPDATE 实现互斥，
 * 不引入 Redis 或 ShedLock。</p>
 *
 * <p>租约带过期时间：若持锁实例宕机，租约到期后其他实例可接管，
 * 不会因为某次异常退出导致任务永久停摆。</p>
 */
@Component
public class ZjudgScheduleLease
{
    private static final Logger log = LoggerFactory.getLogger(ZjudgScheduleLease.class);

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 实例标识：主机名 + 进程号 + 随机后缀，保证跨实例唯一 */
    private final String instanceId = buildInstanceId();

    @Resource
    private ZjudgScheduleLockMapper lockMapper;

    /**
     * 在租约保护下执行任务。
     *
     * <p>抢不到锁说明其他实例正在执行，直接跳过（不排队），
     * 因为这类任务是周期性的，下个周期自然会再跑。</p>
     *
     * @param lockName 锁名
     * @param ttl 租约有效期，必须大于任务的最长执行时间
     * @param task 任务体
     * @return 是否实际执行
     */
    public boolean runExclusively(String lockName, Duration ttl, Runnable task)
    {
        LocalDateTime now = LocalDateTime.now();
        String nowText = now.format(TIME);
        String expiresText = now.plus(ttl).format(TIME);

        int acquired;
        try
        {
            acquired = lockMapper.tryAcquire(lockName, instanceId, nowText, expiresText);
        }
        catch (Exception exception)
        {
            // 抢锁失败时宁可跳过，也不要冒险重复执行
            log.warn("定时任务获取租约失败，本周期跳过 lock={}", lockName, exception);
            return false;
        }

        if (acquired == 0)
        {
            log.debug("定时任务已被其他实例持有，跳过 lock={}", lockName);
            return false;
        }

        try
        {
            task.run();
            return true;
        }
        catch (Exception exception)
        {
            log.warn("定时任务执行失败 lock={}", lockName, exception);
            return false;
        }
        finally
        {
            // 提前释放，让其他实例不必等到 TTL 结束
            try
            {
                lockMapper.release(lockName, instanceId);
            }
            catch (Exception exception)
            {
                log.warn("定时任务释放租约失败，将在 TTL 后自动失效 lock={}", lockName, exception);
            }
        }
    }

    /** 当前实例标识，供日志与测试使用。 */
    public String getInstanceId()
    {
        return instanceId;
    }

    private static String buildInstanceId()
    {
        String host;
        try
        {
            host = InetAddress.getLocalHost().getHostName();
        }
        catch (Exception exception)
        {
            host = "unknown-host";
        }
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return host + "-" + pid + "-" + suffix;
    }
}
