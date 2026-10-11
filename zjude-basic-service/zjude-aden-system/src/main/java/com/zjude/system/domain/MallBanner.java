package com.zjude.system.domain;

import java.io.Serial;
import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjude.common.annotation.Excel;
import com.zjude.common.core.domain.BaseEntity;
import com.zjude.common.xss.Xss;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 轮播图表 zjudg_mall_banner
 *
 * @author bai
 */
public class MallBanner extends BaseEntity
{
    @Serial
    private static final long serialVersionUID = 1L;

    /** 自增主键 */
    private Integer seqNo;

    /** 轮播图ID */
    @Excel(name = "轮播图编号")
    private String bannerId;

    /** 轮播图标题 */
    @Excel(name = "轮播图标题")
    private String bannerTitle;

    /** 图片地址 */
    @Excel(name = "图片地址")
    private String imageUrl;

    /** 跳转类型（0无跳转 1内部页面 2外部链接） */
    @Excel(name = "跳转类型", readConverterExp = "0=无跳转,1=内部页面,2=外部链接")
    private String linkType;

    /** 跳转地址 */
    @Excel(name = "跳转地址")
    private String linkUrl;

    /** 广告位置（index首页 category分类页 activity活动页） */
    @Excel(name = "广告位置", readConverterExp = "index=首页,category=分类页,activity=活动页")
    private String bannerPosition;

    /** 显示顺序 */
    @Excel(name = "显示顺序")
    private Integer bannerSort;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 生效开始时间 */
    @Excel(name = "生效开始时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /** 生效结束时间 */
    @Excel(name = "生效结束时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    public Integer getSeqNo()
    {
        return seqNo;
    }

    public void setSeqNo(Integer seqNo)
    {
        this.seqNo = seqNo;
    }

    public String getBannerId()
    {
        return bannerId;
    }

    public void setBannerId(String bannerId)
    {
        this.bannerId = bannerId;
    }

    @Xss(message = "轮播图标题不能包含脚本字符")
    @NotBlank(message = "轮播图标题不能为空")
    @Size(min = 0, max = 100, message = "轮播图标题长度不能超过100个字符")
    public String getBannerTitle()
    {
        return bannerTitle;
    }

    public void setBannerTitle(String bannerTitle)
    {
        this.bannerTitle = bannerTitle;
    }

    @NotBlank(message = "轮播图片不能为空")
    @Size(min = 0, max = 500, message = "图片地址长度不能超过500个字符")
    public String getImageUrl()
    {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }

    @NotBlank(message = "跳转类型不能为空")
    public String getLinkType()
    {
        return linkType;
    }

    public void setLinkType(String linkType)
    {
        this.linkType = linkType;
    }

    @Size(min = 0, max = 500, message = "跳转地址长度不能超过500个字符")
    public String getLinkUrl()
    {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl)
    {
        this.linkUrl = linkUrl;
    }

    @NotBlank(message = "广告位置不能为空")
    @Size(min = 0, max = 64, message = "广告位置长度不能超过64个字符")
    public String getBannerPosition()
    {
        return bannerPosition;
    }

    public void setBannerPosition(String bannerPosition)
    {
        this.bannerPosition = bannerPosition;
    }

    @NotNull(message = "显示顺序不能为空")
    public Integer getBannerSort()
    {
        return bannerSort;
    }

    public void setBannerSort(Integer bannerSort)
    {
        this.bannerSort = bannerSort;
    }

    @NotBlank(message = "状态不能为空")
    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Date getStartTime()
    {
        return startTime;
    }

    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }

    public Date getEndTime()
    {
        return endTime;
    }

    public void setEndTime(Date endTime)
    {
        this.endTime = endTime;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("bannerId", getBannerId())
            .append("bannerTitle", getBannerTitle())
            .append("imageUrl", getImageUrl())
            .append("linkType", getLinkType())
            .append("linkUrl", getLinkUrl())
            .append("bannerPosition", getBannerPosition())
            .append("bannerSort", getBannerSort())
            .append("status", getStatus())
            .append("startTime", getStartTime())
            .append("endTime", getEndTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
