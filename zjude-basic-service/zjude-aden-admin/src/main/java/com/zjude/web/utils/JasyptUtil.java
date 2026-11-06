package com.zjude.web.utils;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.EnvironmentStringPBEConfig;

import java.time.LocalDateTime;

public class JasyptUtil {

    private static final String algorithm = "PBEWithMD5AndDES";

    private static final String password = "5A628EFE4DA5D06A31FACA5B4D2BB99A";

    private static final String ivGeneratorClassname = "org.jasypt.iv.RandomIvGenerator";

    /**
     * 解密
     *
     * @param text 需要加密的字符�?     * @return String
     */
    public static String encrypt(String text) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        //加密配置
        EnvironmentStringPBEConfig config = new EnvironmentStringPBEConfig();
        config.setAlgorithm(algorithm);
        config.setIvGeneratorClassName(ivGeneratorClassname);
        //自己在用的时候更改此密码
        config.setPassword(password);
        //应用配置
        encryptor.setConfig(config);
        return encryptor.encrypt(text);
    }

    /**
     * 解密
     *
     * @param text 需要解密的字符�?     * @return String
     */
    public static String decrypt(String text) {

        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        //加密配置
        EnvironmentStringPBEConfig config = new EnvironmentStringPBEConfig();
        config.setAlgorithm(algorithm);
        config.setIvGeneratorClassName(ivGeneratorClassname);
        //自己在用的时候更改此密码
        config.setPassword(password);
        //应用配置
        encryptor.setConfig(config);

        //解密
        return encryptor.decrypt(text);
    }

    public static void main(String[] args) {
        String encrypt = JasyptUtil.encrypt("Root123456@2026");
        System.out.println(encrypt);
        String decrypt = JasyptUtil.decrypt("39s4VUkY7ILj57W6DF+5tB7fZSIbSUOec4PohJjVFb4=");
        System.out.println(decrypt);
        System.out.println(LocalDateTime.now().withNano(0));
    }
}
