package com.zjude.system.domain;

import java.io.Serial;
import com.zjude.common.core.domain.BaseEntity;

/** 公共内容资源。 */
public class ZjudgContentItem extends BaseEntity
{
    @Serial
    private static final long serialVersionUID = 1L;

    private String contentId;
    private String resourceType;
    private String title;
    private String categoryId;
    private String slotId;
    private String platform;
    private String image;
    private String linkType;
    private String link;
    private String intro;
    private String content;
    private String isAnnouncement;
    private String status;
    private Integer sortNum;
    private Integer imageWidth;
    private Integer imageHeight;
    private Integer viewCount;
    private Integer likeCount;

    public String getContentId() { return contentId; }
    public void setContentId(String value) { contentId = value; }
    public String getResourceType() { return resourceType; }
    public void setResourceType(String value) { resourceType = value; }
    public String getTitle() { return title; }
    public void setTitle(String value) { title = value; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String value) { categoryId = value; }
    public String getSlotId() { return slotId; }
    public void setSlotId(String value) { slotId = value; }
    public String getPlatform() { return platform; }
    public void setPlatform(String value) { platform = value; }
    public String getImage() { return image; }
    public void setImage(String value) { image = value; }
    public String getLinkType() { return linkType; }
    public void setLinkType(String value) { linkType = value; }
    public String getLink() { return link; }
    public void setLink(String value) { link = value; }
    public String getIntro() { return intro; }
    public void setIntro(String value) { intro = value; }
    public String getContent() { return content; }
    public void setContent(String value) { content = value; }
    public String getIsAnnouncement() { return isAnnouncement; }
    public void setIsAnnouncement(String value) { isAnnouncement = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public Integer getSortNum() { return sortNum; }
    public void setSortNum(Integer value) { sortNum = value; }
    public Integer getImageWidth() { return imageWidth; }
    public void setImageWidth(Integer value) { imageWidth = value; }
    public Integer getImageHeight() { return imageHeight; }
    public void setImageHeight(Integer value) { imageHeight = value; }
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer value) { viewCount = value; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer value) { likeCount = value; }
}
