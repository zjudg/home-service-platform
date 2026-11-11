package com.zjude.system.domain;

import java.io.Serial;
import java.util.Date;
import com.zjude.common.core.domain.BaseEntity;

/**
 * 用户服务收藏。
 *
 * <p>列表查询会联表带出服务名称、价格与封面，供小程序直接渲染，
 * 这些字段只读，不参与落库。</p>
 */
public class ZjudgUserFavorite extends BaseEntity
{
    @Serial
    private static final long serialVersionUID = 1L;

    private String favoriteId;
    private String userId;
    private String serviceId;

    /** 以下为 zjudg_service 联表字段 */
    private String serviceName;
    private String serviceIntro;
    private String imageUrl;
    private String price;
    private String unitName;
    private String serviceStatus;

    public String getFavoriteId() { return favoriteId; }
    public void setFavoriteId(String value) { favoriteId = value; }

    public String getUserId() { return userId; }
    public void setUserId(String value) { userId = value; }

    public String getServiceId() { return serviceId; }
    public void setServiceId(String value) { serviceId = value; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String value) { serviceName = value; }

    public String getServiceIntro() { return serviceIntro; }
    public void setServiceIntro(String value) { serviceIntro = value; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String value) { imageUrl = value; }

    public String getPrice() { return price; }
    public void setPrice(String value) { price = value; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String value) { unitName = value; }

    public String getServiceStatus() { return serviceStatus; }
    public void setServiceStatus(String value) { serviceStatus = value; }

    /** 创建时间由 BaseEntity 提供，这里仅声明便于前端按时间排序展示。 */
    public Date getFavoriteTime() { return getCreateTime(); }
}
