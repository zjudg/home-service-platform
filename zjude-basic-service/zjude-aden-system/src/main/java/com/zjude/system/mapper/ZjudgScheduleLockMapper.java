package com.zjude.system.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * 定时任务分布式租约。
 *
 * <p>项目是多实例部署的单体，{@code @Scheduled} 在每个实例上都会触发。
 * 自动派单属于「同一订单不能派两次」的写操作，必须保证同一时刻只有一个实例执行。
 * 这里不引入 ShedLock 或 Redis，改用一条带条件的 UPDATE 做租约：
 * 只有当租约已过期（或为空）时才能抢到，抢到后用 {@code expiresAt} 控制有效期。</p>
 */
public interface ZjudgScheduleLockMapper
{
    /**
     * 尝试抢占租约。
     *
     * <p>原子性依赖 MySQL 的行锁：相同 {@code lock_name} 的并发 UPDATE 会串行化，
     * 后到者在 WHERE 条件里看到的是已被修改的值，因此影响行数为 0。</p>
     *
     * @param lockName 锁名
     * @param owner 持有者标识（实例 ID）
     * @param now 当前时间 yyyy-MM-dd HH:mm:ss
     * @param expiresAt 租约到期时间，到期后允许其他实例抢占
     * @return 影响行数，1 表示抢到
     */
    int tryAcquire(@Param("lockName") String lockName, @Param("owner") String owner,
            @Param("now") String now, @Param("expiresAt") String expiresAt);

    /**
     * 释放租约，只释放自己持有的那把。
     *
     * @param lockName 锁名
     * @param owner 持有者标识
     * @return 影响行数
     */
    int release(@Param("lockName") String lockName, @Param("owner") String owner);
}
