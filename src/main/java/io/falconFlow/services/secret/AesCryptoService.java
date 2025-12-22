package io.falconFlow.services.secret;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class AesCryptoService implements CryptoService {

    private static final String AES = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12; // 96 bits for GCM
    private static final int TAG_LENGTH_BIT = 128;

    private final String rawKey;
    private byte[] keyBytes;

    public AesCryptoService(@Value("${secrets.aes.key:}") String rawKey) {
        this.rawKey = rawKey == null ? "" : rawKey;
    }

    @PostConstruct
    public void init() {
        try {
            // Derive a 256-bit key from the provided string using SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            this.keyBytes = digest.digest(rawKey.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize crypto service", e);
        }
    }

    @Override
    public String encrypt(String plainText) {
        if (plainText == null) return null;
        try {
            byte[] iv = new byte[IV_LENGTH];
            SecureRandom rng = new SecureRandom();
            rng.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, AES);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherText.length);
            buffer.put(iv);
            buffer.put(cipherText);
            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    @Override
    public String decrypt(String cipherText) {
        if (cipherText == null) return null;
        try {
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            ByteBuffer buffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);
            byte[] cipherBytes = new byte[buffer.remaining()];
            buffer.get(cipherBytes);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, AES);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

            byte[] plain = cipher.doFinal(cipherBytes);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
