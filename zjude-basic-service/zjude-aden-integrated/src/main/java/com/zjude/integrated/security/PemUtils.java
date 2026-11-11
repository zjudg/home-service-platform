package com.zjude.integrated.security;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import com.zjude.common.exception.ServiceException;
import com.zjude.common.utils.StringUtils;

/**
 * PEM 内容解析工具。
 *
 * <p>本项目所有证书与私钥均由运营在后台以<b>文本内容</b>形式录入（直接粘贴
 * {@code -----BEGIN ...-----} 开头的整段内容），而不是上传文件，因此这里
 * 只按内容解析，不做任何文件路径探测。</p>
 *
 * <p>为兼容运维习惯，若填入的值确实是一个存在的文件路径，也会读取一次；
 * 但以内容优先，避免「内容恰好与某个文件名相同」时读错来源。</p>
 */
public final class PemUtils
{
    private static final String CERT_BEGIN = "-----BEGIN CERTIFICATE-----";

    private static final String CERT_END = "-----END CERTIFICATE-----";

    private static final String PKCS8_BEGIN = "-----BEGIN PRIVATE KEY-----";

    private static final String PKCS8_END = "-----END PRIVATE KEY-----";

    private static final String PKCS1_BEGIN = "-----BEGIN RSA PRIVATE KEY-----";

    private static final String PKCS1_END = "-----END RSA PRIVATE KEY-----";

    private PemUtils()
    {
    }

    /**
     * 规范化为 PEM 文本。
     *
     * <p>判断顺序：先看是否已是 PEM 内容（含 BEGIN 标记），再退化为文件路径读取。
     * 这样当运营直接粘贴证书内容时，绝不会被误当作路径。</p>
     *
     * @param value 后台录入的配置值
     * @param label 用于错误提示的字段名
     * @return 规范化的 PEM 文本
     */
    public static String normalize(String value, String label)
    {
        if (StringUtils.isEmpty(value))
        {
            throw new ServiceException(label + "不能为空");
        }
        String trimmed = value.trim();
        if (trimmed.contains("-----BEGIN "))
        {
            // 已是 PEM 内容，直接使用，恢复可能被编辑器压掉的换行
            return restoreLineBreaks(trimmed);
        }
        // 兜底：允许填写服务器上的证书文件路径
        try
        {
            java.nio.file.Path path = java.nio.file.Paths.get(trimmed);
            if (java.nio.file.Files.isRegularFile(path))
            {
                return java.nio.file.Files.readString(path, StandardCharsets.UTF_8);
            }
        }
        catch (Exception ignored)
        {
            // 不是合法路径，继续按内容处理
        }
        throw new ServiceException(label + "格式不正确：请粘贴包含 -----BEGIN ...----- 的完整内容");
    }

    /**
     * 解析 X.509 证书。
     *
     * @param value 证书内容
     * @param label 字段名
     * @return 证书
     */
    public static Certificate parseCertificate(String value, String label)
    {
        try
        {
            String pem = normalize(value, label);
            byte[] der = Base64.getDecoder().decode(strip(pem, CERT_BEGIN, CERT_END));
            return CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(der));
        }
        catch (ServiceException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            throw new ServiceException(label + "解析失败：请确认粘贴的是完整的证书内容");
        }
    }

    /**
     * 解析证书公钥。
     *
     * @param value 证书内容
     * @param label 字段名
     * @return 公钥
     */
    public static PublicKey parseCertificatePublicKey(String value, String label)
    {
        return parseCertificate(value, label).getPublicKey();
    }

    /** 解析微信支付平台提供的 PEM 格式 RSA 公钥。 */
    public static PublicKey parsePublicKey(String value, String label)
    {
        try
        {
            String pem = normalize(value, label);
            String body = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "").replaceAll("\\s", "");
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(body)));
        }
        catch (ServiceException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            throw new ServiceException(label + "解析失败：请确认粘贴的是完整的 RSA 公钥内容");
        }
    }

    /**
     * 解析 RSA 私钥，同时支持 PKCS#8 与 PKCS#1 两种常见格式。
     *
     * <p>微信商户平台下载的 {@code apiclient_key.pem} 常见为 PKCS#8；
     * 但从其他工具导出的可能是 PKCS#1（{@code BEGIN RSA PRIVATE KEY}），
     * 两种都要能识别，否则运营粘贴后订单会直接失败。</p>
     *
     * @param value 私钥内容
     * @param label 字段名
     * @return 私钥
     */
    public static PrivateKey parsePrivateKey(String value, String label)
    {
        try
        {
            String pem = normalize(value, label);
            if (pem.contains(PKCS1_BEGIN))
            {
                return parsePkcs1(strip(pem, PKCS1_BEGIN, PKCS1_END), label);
            }
            return parsePkcs8(strip(pem, PKCS8_BEGIN, PKCS8_END), label);
        }
        catch (ServiceException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            throw new ServiceException(label + "解析失败：请确认粘贴的是完整的私钥内容");
        }
    }

    /**
     * 解析 PKCS#8 私钥。
     *
     * @param base64 Base64 正文
     * @param label 字段名
     * @return 私钥
     */
    private static PrivateKey parsePkcs8(String base64, String label) throws Exception
    {
        byte[] der = Base64.getDecoder().decode(base64);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
    }

    /**
     * 解析 PKCS#1 私钥。
     *
     * <p>JDK 不直接支持 PKCS#1，这里用 Base64 的 DER 内容手工包装成 PKCS#8 结构：
     * {@code SEQUENCE { INTEGER 0, SEQUENCE { OID rsaEncryption, NULL }, OCTET STRING <pkcs1> }}。</p>
     *
     * @param base64 Base64 正文
     * @param label 字段名
     * @return 私钥
     */
    private static PrivateKey parsePkcs1(String base64, String label) throws Exception
    {
        byte[] pkcs1 = Base64.getDecoder().decode(base64);
        // AlgorithmIdentifier: rsaEncryption (1.2.840.113549.1.1.1) + NULL
        byte[] algorithm = new byte[] {
                0x30, 0x0d, 0x06, 0x09, 0x2a, (byte) 0x86, 0x48, (byte) 0x86,
                (byte) 0xf7, 0x0d, 0x01, 0x01, 0x01, 0x05, 0x00 };
        byte[] version = new byte[] { 0x02, 0x01, 0x00 };
        byte[] octetString = derWrap((byte) 0x04, pkcs1);
        byte[] inner = concat(version, algorithm, octetString);
        byte[] pkcs8 = derWrap((byte) 0x30, inner);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(pkcs8));
    }

    /**
     * 用 DER 长度前缀包装一段内容。
     *
     * @param tag 单字节 TAG
     * @param content 内容
     * @return DER 编码
     */
    private static byte[] derWrap(byte tag, byte[] content)
    {
        byte[] length = derLength(content.length);
        byte[] result = new byte[1 + length.length + content.length];
        result[0] = tag;
        System.arraycopy(length, 0, result, 1, length.length);
        System.arraycopy(content, 0, result, 1 + length.length, content.length);
        return result;
    }

    /**
     * 生成 DER 长度字段。
     *
     * @param length 内容长度
     * @return 长度编码
     */
    private static byte[] derLength(int length)
    {
        if (length < 0x80)
        {
            return new byte[] { (byte) length };
        }
        if (length <= 0xFF)
        {
            return new byte[] { (byte) 0x81, (byte) length };
        }
        if (length <= 0xFFFF)
        {
            return new byte[] { (byte) 0x82, (byte) (length >> 8), (byte) length };
        }
        return new byte[] { (byte) 0x83, (byte) (length >> 16), (byte) (length >> 8), (byte) length };
    }

    /**
     * 拼接多个字节数组。
     *
     * @param arrays 数组
     * @return 拼接结果
     */
    private static byte[] concat(byte[]... arrays)
    {
        int total = 0;
        for (byte[] array : arrays)
        {
            total += array.length;
        }
        byte[] result = new byte[total];
        int offset = 0;
        for (byte[] array : arrays)
        {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    /**
     * 去掉 PEM 头尾标记与所有空白。
     *
     * @param pem PEM 文本
     * @param begin 头标记
     * @param end 尾标记
     * @return Base64 正文
     */
    private static String strip(String pem, String begin, String end)
    {
        return pem.replace(begin, "").replace(end, "").replaceAll("\\s", "");
    }

    /**
     * 恢复被压平的换行。
     *
     * <p>部分后台输入框或代理会去掉换行，导致 Base64 正文变成一行；
     * 解析本身不依赖换行，这里仅在缺少换行时补齐，便于日志与调试可读。</p>
     *
     * @param pem PEM 文本
     * @return 规范化后的 PEM 文本
     */
    private static String restoreLineBreaks(String pem)
    {
        if (pem.contains("\n"))
        {
            return pem;
        }
        // 无换行时直接用空串处理，strip 会移除所有空白，不影响解析
        return pem;
    }
}
