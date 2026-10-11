package com.zjude.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class DatabaseMigrationContractTest
{
    private static final String BASELINE = "db/migration/V1__baseline.sql";
    private static final Pattern CREATE_TABLE = Pattern.compile(
            "(?im)^\\s*CREATE\\s+TABLE\\s+`([^`]+)`");
    private static final Set<String> FRAMEWORK_TABLES = Set.of(
            "gen_table",
            "gen_table_column",
            "sys_config",
            "sys_dept",
            "sys_dict_data",
            "sys_dict_type",
            "sys_login_info",
            "sys_menu",
            "sys_notice",
            "sys_oper_log",
            "sys_post",
            "sys_role",
            "sys_role_dept",
            "sys_role_menu",
            "sys_user",
            "sys_user_post",
            "sys_user_role");

    @Test
    void baselineContainsTheCompleteNonDestructiveFrameworkSchema() throws IOException
    {
        String sql = loadBaseline();
        Matcher matcher = CREATE_TABLE.matcher(sql);
        Set<String> actualTables = new java.util.HashSet<>();
        while (matcher.find())
        {
            actualTables.add(matcher.group(1));
        }

        assertEquals(FRAMEWORK_TABLES, actualTables);
        assertFalse(Pattern.compile("(?i)\\bDROP\\s+(DATABASE|TABLE|COLUMN)\\b|\\bTRUNCATE\\b")
                .matcher(sql)
                .find());
    }

    @Test
    void baselineExcludesHistoricalAuditDataAndNonAdminUsers() throws IOException
    {
        String sql = loadBaseline();

        assertFalse(Pattern.compile("(?im)^\\s*INSERT\\s+INTO\\s+`?sys_(login_info|oper_log)`?")
                .matcher(sql)
                .find());
        assertFalse(sql.contains("U_a2f65ff722bb4b799657a346d6ccb461"));
    }

    private String loadBaseline() throws IOException
    {
        return loadResource(BASELINE);
    }

    private String loadResource(String resource) throws IOException
    {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource))
        {
            assertNotNull(input, resource + " must be packaged as an application resource");
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
