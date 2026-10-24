package com.zjude.settings.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 小程序轮播项。
 *
 * <p>字段名对齐已有装修配置的轮播结构（{@code id/title/subtitle/image/link}），
 * 前端无需为两个数据源写两套渲染逻辑。</p>
 */
@Schema(name = "MiniBanner", description = "小程序轮播项")
public class MiniBanner
{
    @Schema(description = "轮播图编号") private String id;
    @Schema(description = "标题") private String title;
    @Schema(description = "副标题") private String subtitle;
    @Schema(description = "图片地址") private String image;
    @Schema(description = "跳转类型：0无跳转 1内部页面 2外部链接") private String linkType;
    @Schema(description = "跳转地址") private String link;

    public String getId() { return id; }
    public void setId(String value) { id = value; }
    public String getTitle() { return title; }
    public void setTitle(String value) { title = value; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String value) { subtitle = value; }
    public String getImage() { return image; }
    public void setImage(String value) { image = value; }
    public String getLinkType() { return linkType; }
    public void setLinkType(String value) { linkType = value; }
    public String getLink() { return link; }
    public void setLink(String value) { link = value; }
}
