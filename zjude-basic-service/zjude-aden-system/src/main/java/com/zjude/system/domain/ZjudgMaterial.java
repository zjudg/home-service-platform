package com.zjude.system.domain;

import java.io.Serial;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.zjude.common.core.domain.BaseEntity;

/**
 * 素材（图片 / 视频）。
 *
 * <p>素材库只登记文件地址与归属分组，文件本身由上传组件落到文件存储服务，
 * 因此这里不保存二进制内容。</p>
 */
public class ZjudgMaterial extends BaseEntity
{
    @Serial
    private static final long serialVersionUID = 1L;

    /** 素材类型：图片 */
    public static final String TYPE_IMAGE = "image";

    /** 素材类型：视频 */
    public static final String TYPE_VIDEO = "video";

    private String materialId;

    private String groupId;

    private String materialName;

    private String materialType;

    private String materialUrl;

    private Long fileSize;

    public String getMaterialId() { return materialId; }
    public void setMaterialId(String value) { materialId = value; }

    @NotBlank(message = "请选择素材分组")
    public String getGroupId() { return groupId; }
    public void setGroupId(String value) { groupId = value; }

    @NotBlank(message = "素材名称不能为空")
    @Size(max = 128, message = "素材名称长度不能超过 128 个字符")
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String value) { materialName = value; }

    @NotBlank(message = "素材类型不能为空")
    public String getMaterialType() { return materialType; }
    public void setMaterialType(String value) { materialType = value; }

    @NotBlank(message = "请上传素材文件")
    @Size(max = 500, message = "素材地址长度不能超过 500 个字符")
    public String getMaterialUrl() { return materialUrl; }
    public void setMaterialUrl(String value) { materialUrl = value; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long value) { fileSize = value; }
}
