package com.zjude.web.core.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Prevents the production profile from starting with repository placeholders.
 */
@Component
@Profile("prod")
public class ProductionSecretValidator implements ApplicationRunner
{
    private static final List<String> REQUIRED_SECRET_KEYS = List.of(
            "jasypt.encryptor.password",
            "token.secret",
            "spring.data.redis.password",
            "spring.datasource.druid.stat-view-servlet.loginPassword",
            "spring.datasource.dynamic.datasource.master.password");

    private final Environment environment;

    public ProductionSecretValidator(Environment environment)
    {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args)
    {
        List<String> invalidKeys = new ArrayList<>();
        for (String key : REQUIRED_SECRET_KEYS)
        {
            String value = environment.getProperty(key);
            if (value == null || value.isBlank() || value.startsWith("CHANGE_ME"))
            {
                invalidKeys.add(key);
            }
        }
        if (!invalidKeys.isEmpty())
        {
            throw new IllegalStateException(
                    "Production configuration contains missing or placeholder secrets: "
                            + String.join(", ", invalidKeys));
        }
    }
}
