package com.zjude.web.core.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 生产环境启动前的密钥校验：禁止使用仓库中的占位符启动。
 *
 * <p>生产环境的密钥（jasypt 主密钥、token 密钥、数据库与 Redis 密码）必须由外部注入，
 * 例如环境变量或启动参数，禁止写入被版本控制的配置文件。</p>
 */
@Component
@Profile("prod")
public class ProductionSecretValidator implements ApplicationRunner
{
    private static final List<String> REQUIRED_SECRET_KEYS = List.of(
            "jasypt.encryptor.password",
            "token.secret",
            "zjude.security.field-key",
            "spring.data.redis.password",
            "spring.datasource.druid.stat-view-servlet.loginPassword",
            "spring.datasource.dynamic.datasource.master.password",
            "spring.datasource.dynamic.datasource.master.username",
            "spring.datasource.dynamic.datasource.master.url");

    /** 数据库地址中的占位符，未注入时会残留为空串，需单独校验 */
    private static final String DB_URL_KEY = "spring.datasource.dynamic.datasource.master.url";

    /** JDBC 前缀，解析主机与库名前先剥离 */
    private static final String JDBC_PREFIX = "jdbc:";

    private final Environment environment;

    public ProductionSecretValidator(Environment environment)
    {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args)
    {
        List<String> invalidKeys = collectPlaceholderKeys();
        String dbUrl = environment.getProperty(DB_URL_KEY);
        if (isMissingDatabaseTarget(dbUrl))
        {
            invalidKeys.add(DB_URL_KEY + " (missing host or database)");
        }

        if (!invalidKeys.isEmpty())
        {
            throw new IllegalStateException(
                    "Production configuration contains missing or placeholder secrets: "
                            + String.join(", ", invalidKeys));
        }
    }

    private List<String> collectPlaceholderKeys()
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
        return invalidKeys;
    }

    /**
     * 判断数据库地址是否缺少主机或库名。
     *
     * <p>直接解析 URL，避免用单个正则表达式只覆盖某一种拼接形态：
     * 例如 DB_PORT 使用默认值 3306 时会得到 jdbc:mysql://host:3306/（库名为空）。</p>
     *
     * @param dbUrl 数据源地址
     * @return 地址为空、含未解析占位符，或主机/库名为空时返回 true
     */
    public static boolean isMissingDatabaseTarget(String dbUrl)
    {
        if (dbUrl == null || dbUrl.isBlank() || dbUrl.contains("${"))
        {
            return true;
        }
        String candidate = dbUrl.startsWith(JDBC_PREFIX) ? dbUrl.substring(JDBC_PREFIX.length()) : dbUrl;
        int queryIndex = candidate.indexOf('?');
        if (queryIndex >= 0)
        {
            candidate = candidate.substring(0, queryIndex);
        }
        try
        {
            URI uri = new URI(candidate);
            String host = uri.getHost();
            String path = uri.getPath();
            String database = path == null ? null : path.replace("/", "");
            return host == null || host.isBlank() || database == null || database.isBlank();
        }
        catch (URISyntaxException exception)
        {
            return true;
        }
    }
}
