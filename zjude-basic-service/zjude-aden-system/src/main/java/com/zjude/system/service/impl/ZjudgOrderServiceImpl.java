package com.zjude.system.service.impl;

import java.util.List;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import com.zjude.system.domain.ZjudgOrder;
import com.zjude.system.mapper.ZjudgOrderMapper;
import com.zjude.system.service.IZjudgOrderService;

@Service("zjudgOrderService")
public class ZjudgOrderServiceImpl implements IZjudgOrderService {
    @Resource private ZjudgOrderMapper mapper;
    public List<ZjudgOrder> selectList(ZjudgOrder q) { return mapper.selectList(q); }
    public ZjudgOrder selectById(String id) { return mapper.selectById(id); }
}
