package com.zjude.system.service.impl;

import java.util.List;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import com.zjude.common.utils.uuid.base.BaseKeyGenerate;
import com.zjude.system.domain.ZjudgServiceReview;
import com.zjude.system.mapper.ZjudgServiceReviewMapper;
import com.zjude.system.service.IZjudgReviewService;

@Service("zjudgReviewService")
public class ZjudgReviewServiceImpl implements IZjudgReviewService {
    @Resource private ZjudgServiceReviewMapper mapper;
    @Resource private BaseKeyGenerate keyGenerate;
    public List<ZjudgServiceReview> selectList(ZjudgServiceReview q) { return mapper.selectList(q); }
    public ZjudgServiceReview selectById(String id) { return mapper.selectById(id); }
    public int insert(ZjudgServiceReview item) { item.setReviewId(keyGenerate.snowflakeStringKey("ZR_")); return mapper.insert(item); }
    public int deleteByIds(String[] ids) { return mapper.deleteByIds(ids); }
    public int updateReply(ZjudgServiceReview item) { item.setReplyStatus(item.getReplyContent() == null || item.getReplyContent().isBlank() ? "0" : "1"); return mapper.updateReply(item); }
    public int updateStatus(ZjudgServiceReview item) { return mapper.updateStatus(item); }
}
