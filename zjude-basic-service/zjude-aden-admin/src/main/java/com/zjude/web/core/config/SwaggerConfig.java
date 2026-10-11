package com.zjude.web.core.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.zjude.common.config.BaiConfig;
import org.springframework.http.HttpHeaders;

/**
 * Swagger2的接口配�? * 
 * @author bai
 */
@Configuration
public class SwaggerConfig
{
    /** 系统基础配置 */
    @Autowired
    private BaiConfig baiConfig;

    private Info info(){
        return new Info()
                .title(baiConfig.getName())
                .version(baiConfig.getVersion());
    }

    private SecurityScheme securityScheme() {
        SecurityScheme securityScheme = new SecurityScheme();
        //类型
        securityScheme.setType(SecurityScheme.Type.APIKEY);
        //请求头的name
        securityScheme.setName(HttpHeaders.AUTHORIZATION);
        // token 所在位置
        securityScheme.setIn(SecurityScheme.In.HEADER);
        return securityScheme;
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(info())
                .schemaRequirement(HttpHeaders.AUTHORIZATION, this.securityScheme())
                .addSecurityItem(new SecurityRequirement().addList(HttpHeaders.AUTHORIZATION));
    }
}
