package com.zjude.system.domain;

import java.io.Serial;
import com.zjude.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 家政服务分类。 */
public class ZjudgServiceCategory extends BaseEntity {
    @Serial private static final long serialVersionUID = 1L;
    private String categoryId;
    private String parentId;
    private String categoryName;
    private String imageUrl;
    private Integer sortNum;
    private String homeRecommend;
    private String status;
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String value) { categoryId = value; }
    public String getParentId() { return parentId; }
    public void setParentId(String value) { parentId = value; }
    @NotBlank(message = "分类名称不能为空") public String getCategoryName() { return categoryName; }
    public void setCategoryName(String value) { categoryName = value; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String value) { imageUrl = value; }
    @NotNull(message = "排序不能为空") public Integer getSortNum() { return sortNum; }
    public void setSortNum(Integer value) { sortNum = value; }
    public String getHomeRecommend() { return homeRecommend; }
    public void setHomeRecommend(String value) { homeRecommend = value; }
    @NotBlank(message = "状态不能为空") public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
}
