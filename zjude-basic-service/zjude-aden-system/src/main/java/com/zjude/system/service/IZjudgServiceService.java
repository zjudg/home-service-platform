package com.zjude.system.service;
import java.util.List;
import com.zjude.system.domain.ZjudgService;
public interface IZjudgServiceService { List<ZjudgService> selectList(ZjudgService q); ZjudgService selectById(String id); int insert(ZjudgService item); int update(ZjudgService item); int deleteByIds(String[] ids); }
