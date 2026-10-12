package com.zjude.system.service;
import java.util.List;
import com.zjude.system.domain.ZjudgServiceReview;
public interface IZjudgReviewService { List<ZjudgServiceReview> selectList(ZjudgServiceReview q); ZjudgServiceReview selectById(String id); int insert(ZjudgServiceReview item); int updateReply(ZjudgServiceReview item); int updateStatus(ZjudgServiceReview item); int deleteByIds(String[] ids); }
