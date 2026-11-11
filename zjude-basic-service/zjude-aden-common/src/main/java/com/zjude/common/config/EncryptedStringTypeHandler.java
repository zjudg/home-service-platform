package com.zjude.common.config;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.zjude.common.utils.FieldCryptoUtils;

/**
 * 敏感字段透明加密的 MyBatis 类型处理器。
 *
 * <p>写入时自动加密、读取时自动解密，业务代码无需感知。密钥来自
 * {@code zjude.security.field-key}，缺失时写入会立即失败，避免明文落库。</p>
 */
@Component
@MappedTypes(String.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class EncryptedStringTypeHandler extends BaseTypeHandler<String>
{
    private static String fieldKey;

    /**
     * 由 Spring 注入密钥，静态字段便于 MyBatis 自行实例化 handler 时也能取到。
     *
     * @param key 字段加密密钥
     */
    @Value("${zjude.security.field-key:}")
    public void setFieldKey(String key)
    {
        EncryptedStringTypeHandler.fieldKey = key;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)
            throws SQLException
    {
        ps.setString(i, FieldCryptoUtils.encrypt(parameter, fieldKey));
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException
    {
        return FieldCryptoUtils.decrypt(rs.getString(columnName), fieldKey);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException
    {
        return FieldCryptoUtils.decrypt(rs.getString(columnIndex), fieldKey);
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException
    {
        return FieldCryptoUtils.decrypt(cs.getString(columnIndex), fieldKey);
    }
}
