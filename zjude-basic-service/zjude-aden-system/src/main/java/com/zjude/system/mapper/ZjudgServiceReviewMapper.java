package com.zjude.system.mapper;
import java.util.List;
import com.zjude.system.domain.ZjudgServiceReview;
public interface ZjudgServiceReviewMapper {
    List<ZjudgServiceReview> selectList(ZjudgServiceReview query);
    ZjudgServiceReview selectById(String id);
    int insert(ZjudgServiceReview item);
    int updateReply(ZjudgServiceReview item);
    int updateStatus(ZjudgServiceReview item);
    int deleteByIds(String[] ids);
}
