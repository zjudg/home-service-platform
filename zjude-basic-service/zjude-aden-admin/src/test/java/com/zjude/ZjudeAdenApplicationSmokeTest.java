package com.zjude;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verifies that the Spring Boot test infrastructure can start without a
 * database, Redis, network access, or other external services.
 */
@ActiveProfiles("test")
@SpringBootTest(
        classes = ZjudeAdenApplicationSmokeTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.main.banner-mode=off",
                "jasypt.encryptor.password=test-only-key",
                "spring.autoconfigure.exclude="
                        + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration,"
                        + "com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceAutoConfiguration,"
                        + "com.alibaba.druid.spring.boot3.autoconfigure.DruidDataSourceAutoConfigure"
        })
class ZjudeAdenApplicationSmokeTest
{
    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoadsWithoutExternalServices()
    {
        assertThat(applicationContext).isNotNull();
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication
    {
    }
}
