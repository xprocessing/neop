package com.gongziyu.neop.util;

import com.gongziyu.neop.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES加密解密工具类（文档22.3节）
 */
@Slf4j
@Component
public class AesEncryptUtil {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static String key;

    @Value("${neop.encrypt.key}")
    public void setKey(String key) {
        AesEncryptUtil.key = key;
    }

    public static String encrypt(String content) {
        if (StringUtils.isBlank(content)) {
            return content;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec iv = new IvParameterSpec(key.substring(0, 16).getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
            byte[] encrypted = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("[AES加密] 失败", e);
            throw BusinessException.of("加密失败");
        }
    }

    public static String decrypt(String encrypted) {
        if (StringUtils.isBlank(encrypted)) {
            return encrypted;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec iv = new IvParameterSpec(key.substring(0, 16).getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("[AES解密] 失败", e);
            throw BusinessException.of("解密失败");
        }
    }
}
