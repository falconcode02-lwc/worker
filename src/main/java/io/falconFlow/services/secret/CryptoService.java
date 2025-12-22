package io.falconFlow.services.secret;

public interface CryptoService {
    String encrypt(String plainText);
    String decrypt(String cipherText);
}
