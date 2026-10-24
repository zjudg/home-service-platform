package com.zjude.system.mapper;
import java.util.List;
import com.zjude.system.domain.ZjudgBookingSlot;
public interface ZjudgBookingSlotMapper {
    List<ZjudgBookingSlot> selectList(ZjudgBookingSlot query);
    /** 严格城市过滤：只返回属于该城市的时段，不含全国通用时段 */
    List<ZjudgBookingSlot> selectListByCity(ZjudgBookingSlot query);
    ZjudgBookingSlot selectById(String id);

    /**
     * 按主键加行锁读取时段，用于下单时串行化同一时段的容量判断，避免并发超卖。
     *
     * <p>必须在事务中调用，锁在事务提交后释放。</p>
     *
     * @param id 时段主键
     * @return 时段
     */
    ZjudgBookingSlot selectByIdForUpdate(String id);
    int insert(ZjudgBookingSlot item);
    int update(ZjudgBookingSlot item);
    int deleteByIds(String[] ids);
}
