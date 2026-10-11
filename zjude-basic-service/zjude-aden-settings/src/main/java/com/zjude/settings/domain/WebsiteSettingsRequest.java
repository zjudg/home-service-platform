package com.zjude.settings.domain;

/**
 * 网站设置表单。
 */
public class WebsiteSettingsRequest
{
    private String shopName;
    private String favicon;
    private String mobileLoginLogo;
    private String mobileLogo;
    private String pcLogo;
    private String adminLoginLogo;
    private String adminHomeImage;
    private String adminLoginName;
    private String adminLogo;
    private String memberDefaultAvatar;
    private String goodsDefaultImage;

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public String getFavicon() { return favicon; }
    public void setFavicon(String favicon) { this.favicon = favicon; }
    public String getMobileLoginLogo() { return mobileLoginLogo; }
    public void setMobileLoginLogo(String mobileLoginLogo) { this.mobileLoginLogo = mobileLoginLogo; }
    public String getMobileLogo() { return mobileLogo; }
    public void setMobileLogo(String mobileLogo) { this.mobileLogo = mobileLogo; }
    public String getPcLogo() { return pcLogo; }
    public void setPcLogo(String pcLogo) { this.pcLogo = pcLogo; }
    public String getAdminLoginLogo() { return adminLoginLogo; }
    public void setAdminLoginLogo(String adminLoginLogo) { this.adminLoginLogo = adminLoginLogo; }
    public String getAdminHomeImage() { return adminHomeImage; }
    public void setAdminHomeImage(String adminHomeImage) { this.adminHomeImage = adminHomeImage; }
    public String getAdminLoginName() { return adminLoginName; }
    public void setAdminLoginName(String adminLoginName) { this.adminLoginName = adminLoginName; }
    public String getAdminLogo() { return adminLogo; }
    public void setAdminLogo(String adminLogo) { this.adminLogo = adminLogo; }
    public String getMemberDefaultAvatar() { return memberDefaultAvatar; }
    public void setMemberDefaultAvatar(String memberDefaultAvatar) { this.memberDefaultAvatar = memberDefaultAvatar; }
    public String getGoodsDefaultImage() { return goodsDefaultImage; }
    public void setGoodsDefaultImage(String goodsDefaultImage) { this.goodsDefaultImage = goodsDefaultImage; }
}
