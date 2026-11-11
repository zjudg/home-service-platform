package com.zjude.common.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 敏感字段加解密（AES/GCM）。
 *
 * <p>用于身份证号等需要落库的个人敏感信息。密钥由外部注入
 * （{@code zjude.security.field-key}），未配置时抛异常而不是降级为明文，
 * 避免误把明文写进生产库。</p>
 *
 * <p>密文格式：{@code ENC(v1:base64(iv):base64(cipher))}，带版本前缀便于将来轮换算法。</p>
 */
public final class FieldCryptoUtils
{
    /** 密文前缀，用于识别是否需要解密，兼容历史明文数据 */
    private static final String PREFIX = "ENC(";

    private static final String SUFFIX = ")";

    private static final String VERSION = "v1";

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    /** GCM 认证标签长度 */
    private static final int TAG_LENGTH_BITS = 128;

    /** IV 长度 */
    private static final int IV_LENGTH = 12;

    private FieldCryptoUtils()
    {
    }

    /**
     * 判断字符串是否为本工具生成的密文。
     *
     * @param value 待判断的值
     * @return 是密文返回 true
     */
    public static boolean isEncrypted(String value)
    {
        return value != null && value.startsWith(PREFIX) && value.endsWith(SUFFIX)
                && value.contains(VERSION + ":");
    }

    /**
     * 加密敏感字段。
     *
     * @param plain 明文
     * @param key 密钥材料，任意长度，内部做 SHA-256 派生
     * @return 密文；明文为空时原样返回
     */
    public static String encrypt(String plain, String key)
    {
        if (plain == null || plain.isEmpty())
        {
            return plain;
        }
        if (isEncrypted(plain))
        {
            // 已是密文，避免重复加密
            return plain;
        }
        // 密钥缺失属于配置错误，必须原样抛出，不能被下面的包装吞掉原因
        SecretKeySpec keySpec = keySpec(key);
        try
        {
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] encrypted = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            return PREFIX + VERSION + ":" + Base64.getEncoder().encodeToString(iv)
                    + ":" + Base64.getEncoder().encodeToString(encrypted) + SUFFIX;
        }
        catch (Exception exception)
        {
            // 加密失败不能静默退回明文，否则敏感数据会泄露到数据库
            throw new IllegalStateException("敏感字段加密失败", exception);
        }
    }

    /**
     * 解密敏感字段。
     *
     * @param cipherText 密文
     * @param key 密钥材料
     * @return 明文；非密文输入按历史明文原样返回，便于平滑迁移
     */
    public static String decrypt(String cipherText, String key)
    {
        if (cipherText == null || cipherText.isEmpty())
        {
            return cipherText;
        }
        if (!isEncrypted(cipherText))
        {
            // 历史明文数据：原样返回，由迁移任务逐步加密
            return cipherText;
        }
        // 密钥缺失属于配置错误，需原样抛出
        SecretKeySpec keySpec = keySpec(key);
        try
        {
            String body = cipherText.substring(PREFIX.length(), cipherText.length() - SUFFIX.length());
            String[] parts = body.split(":");
            if (parts.length != 3 || !VERSION.equals(parts[0]))
            {
                throw new IllegalStateException("不支持的密文格式");
            }
            byte[] iv = Base64.getDecoder().decode(parts[1]);
            byte[] encrypted = Base64.getDecoder().decode(parts[2]);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        }
        catch (IllegalStateException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            throw new IllegalStateException("敏感字段解密失败，请检查字段密钥是否变更", exception);
        }
    }

    /**
     * 派生固定长度密钥。
     *
     * @param key 原始密钥材料
     * @return AES 密钥
     */
    private static SecretKeySpec keySpec(String key)
    {
        if (key == null || key.isBlank())
        {
            throw new IllegalStateException("敏感字段密钥未配置（zjude.security.field-key）");
        }
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] derived = digest.digest(key.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(derived, "AES");
        }
        catch (Exception exception)
        {
            throw new IllegalStateException("敏感字段密钥派生失败", exception);
        }
    }
}
