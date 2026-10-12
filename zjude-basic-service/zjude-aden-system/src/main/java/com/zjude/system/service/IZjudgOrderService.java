package com.zjude.system.service;
import java.util.List;
import com.zjude.system.domain.ZjudgOrder;
public interface IZjudgOrderService { List<ZjudgOrder> selectList(ZjudgOrder q); ZjudgOrder selectById(String id); }
