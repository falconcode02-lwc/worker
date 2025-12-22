package io.falconFlow.DSL.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class AESUtil {
  private static final String ALGO = "AES/CBC/PKCS5Padding";
  private static final String SECRET = "0123456789abcdef0123456789abcdef"; // 32 bytes = AES-256

  public static String encrypt(String plainText) throws Exception {
    SecretKey key = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "AES");
    byte[] iv = new byte[16];
    new SecureRandom().nextBytes(iv);
    IvParameterSpec ivSpec = new IvParameterSpec(iv);

    Cipher cipher = Cipher.getInstance(ALGO);
    cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
    byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

    byte[] combined = new byte[iv.length + encrypted.length];
    System.arraycopy(iv, 0, combined, 0, iv.length);
    System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

    return Base64.getEncoder().encodeToString(combined);
  }

  public static String decrypt(String encryptedText) throws Exception {
    byte[] decoded = Base64.getDecoder().decode(encryptedText);
    byte[] iv = new byte[16];
    byte[] cipherBytes = new byte[decoded.length - 16];
    System.arraycopy(decoded, 0, iv, 0, 16);
    System.arraycopy(decoded, 16, cipherBytes, 0, cipherBytes.length);

    SecretKey key = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "AES");
    IvParameterSpec ivSpec = new IvParameterSpec(iv);

    Cipher cipher = Cipher.getInstance(ALGO);
    cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
    byte[] decrypted = cipher.doFinal(cipherBytes);

    return new String(decrypted, StandardCharsets.UTF_8);
  }
}
