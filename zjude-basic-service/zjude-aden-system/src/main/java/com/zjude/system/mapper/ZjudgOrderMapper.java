package com.zjude.system.mapper;
import java.util.List;
import com.zjude.system.domain.ZjudgOrder;
public interface ZjudgOrderMapper {
    List<ZjudgOrder> selectList(ZjudgOrder query);
    ZjudgOrder selectById(String id);
}
