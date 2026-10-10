package com.zjude.settings.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import com.zjude.common.exception.ServiceException;
import com.zjude.system.domain.SysConfig;
import com.zjude.system.service.ISysConfigService;

@ExtendWith(MockitoExtension.class)
class PageSettingsServiceImplTest
{
    @Mock
    private ISysConfigService configService;

    @InjectMocks
    private PageSettingsServiceImpl service;

    @Test
    void supportsDecorationPages()
    {
        SysConfig home = config("decorate.home.navItems");
        SysConfig profile = config("decorate.profile.navItems");
        SysConfig category = config("decorate.category.layout");
        SysConfig tabbar = config("decorate.tabbar.items");
        when(configService.selectConfigList(any(SysConfig.class))).thenReturn(List.of(home, profile, category, tabbar));

        assertThat(service.list("decorate-home")).containsExactly(home);
        assertThat(service.list("decorate-profile")).containsExactly(profile);
        assertThat(service.list("decorate-category")).containsExactly(category);
        assertThat(service.list("decorate-tabbar")).containsExactly(tabbar);
    }

    @Test
    void rejectsUnknownPage()
    {
        assertThatThrownBy(() -> service.list("unknown"))
                .isInstanceOf(ServiceException.class)
                .hasMessage("不支持的页面配置：unknown");
    }

    private SysConfig config(String key)
    {
        SysConfig config = new SysConfig();
        config.setConfigKey(key);
        config.setConfigValue("value");
        return config;
    }
}
