package com.zjude.web.core.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class ProductionSecretValidatorTest
{
    @Test
    void acceptsDirectProductionValues()
    {
        ProductionSecretValidator validator = new ProductionSecretValidator(validEnvironment());

        assertDoesNotThrow(() -> validator.run(null));
    }

    @Test
    void rejectsRepositoryPlaceholdersWithoutPrintingValues()
    {
        MockEnvironment environment = validEnvironment();
        environment.setProperty("token.secret", "CHANGE_ME_TOKEN_SECRET");
        ProductionSecretValidator validator = new ProductionSecretValidator(environment);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> validator.run(null));

        assertTrue(exception.getMessage().contains("token.secret"));
        assertTrue(!exception.getMessage().contains("CHANGE_ME_TOKEN_SECRET"));
    }

    private MockEnvironment validEnvironment()
    {
        return new MockEnvironment()
                .withProperty("jasypt.encryptor.password", "direct-jasypt-value")
                .withProperty("token.secret", "direct-token-value")
                .withProperty("spring.data.redis.password", "direct-redis-value")
                .withProperty(
                        "spring.datasource.druid.stat-view-servlet.loginPassword",
                        "direct-druid-value")
                .withProperty(
                        "spring.datasource.dynamic.datasource.master.password",
                        "direct-database-value");
    }
}
