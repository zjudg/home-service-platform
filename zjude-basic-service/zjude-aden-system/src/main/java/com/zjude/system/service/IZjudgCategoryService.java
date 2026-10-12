package com.zjude.system.service;
import java.util.List;
import com.zjude.system.domain.ZjudgServiceCategory;
public interface IZjudgCategoryService { List<ZjudgServiceCategory> selectList(ZjudgServiceCategory q); ZjudgServiceCategory selectById(String id); int insert(ZjudgServiceCategory item); int update(ZjudgServiceCategory item); int deleteByIds(String[] ids); }
