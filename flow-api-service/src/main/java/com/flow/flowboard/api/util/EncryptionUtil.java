package com.flow.flowboard.api.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for encrypting and decrypting sensitive data using AES-256-GCM
 *
 * Security Features:
 * - AES-256-GCM (Galois/Counter Mode) for authenticated encryption
 * - Random IV (Initialization Vector) for each encryption
 * - Protection against tampering via authentication tag
 * - Base64 encoding for storage compatibility
 *
 * Best Practices:
 * - Master key should be stored in environment variable or secrets manager
 * - Never log decrypted values
 * - Use unique IV for each encryption operation
 */
@Component
@Slf4j
public class EncryptionUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // bits
    private static final int GCM_IV_LENGTH = 12; // bytes (96 bits recommended for GCM)
    private static final int AES_KEY_SIZE = 256; // bits

    private final SecretKey secretKey;
    private final SecureRandom secureRandom;

    public EncryptionUtil(@Value("${app.security.encryption.key:}") String base64Key) {
        this.secureRandom = new SecureRandom();

        if (base64Key == null || base64Key.isBlank()) {
            log.warn("Encryption key not configured. Generating temporary key. " +
                    "Set app.security.encryption.key in production!");
            this.secretKey = generateKey();
        } else {
            byte[] decodedKey = Base64.getDecoder().decode(base64Key);
            this.secretKey = new SecretKeySpec(decodedKey, "AES");
        }
    }

    /**
     * Encrypts plaintext using AES-256-GCM
     *
     * @param plaintext the text to encrypt
     * @return Base64 encoded string containing IV + ciphertext + auth tag
     * @throws RuntimeException if encryption fails
     */
    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return null;
        }

        try {
            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            // Encrypt
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Combine IV + ciphertext for storage
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            byteBuffer.put(iv);
            byteBuffer.put(ciphertext);

            // Encode to Base64 for safe storage
            return Base64.getEncoder().encodeToString(byteBuffer.array());

        } catch (Exception e) {
            log.error("Encryption failed", e);
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }

    /**
     * Decrypts ciphertext that was encrypted using encrypt()
     *
     * @param ciphertext Base64 encoded string containing IV + ciphertext + auth tag
     * @return decrypted plaintext
     * @throws RuntimeException if decryption fails or data is tampered
     */
    public String decrypt(String ciphertext) {
        if (ciphertext == null || ciphertext.isEmpty()) {
            return null;
        }

        try {
            // Decode from Base64
            byte[] combined = Base64.getDecoder().decode(ciphertext);

            // Extract IV and ciphertext
            ByteBuffer byteBuffer = ByteBuffer.wrap(combined);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] actualCiphertext = new byte[byteBuffer.remaining()];
            byteBuffer.get(actualCiphertext);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            // Decrypt
            byte[] plaintext = cipher.doFinal(actualCiphertext);
            return new String(plaintext, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("Decryption failed - possible data tampering or corruption", e);
            throw new RuntimeException("Failed to decrypt data", e);
        }
    }

    /**
     * Masks a sensitive value for safe display (shows first and last few chars)
     *
     * @param value the value to mask
     * @param prefixLength number of characters to show at start
     * @param suffixLength number of characters to show at end
     * @return masked value (e.g., "sk-***xyz")
     */
    public static String mask(String value, int prefixLength, int suffixLength) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        if (value.length() <= prefixLength + suffixLength) {
            return "***";
        }

        String prefix = value.substring(0, prefixLength);
        String suffix = value.substring(value.length() - suffixLength);
        return prefix + "***" + suffix;
    }

    /**
     * Masks a value with default lengths (3 chars prefix, 3 chars suffix)
     */
    public static String mask(String value) {
        return mask(value, 3, 3);
    }

    /**
     * Generates a new AES-256 key
     * For production use, store this key securely in environment variable or secrets manager
     *
     * @return new AES-256 secret key
     */
    private SecretKey generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(AES_KEY_SIZE, secureRandom);
            SecretKey key = keyGenerator.generateKey();

            // Log the key in Base64 format for configuration (only in development!)
            String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());
            log.info("Generated encryption key (save this to app.security.encryption.key): {}", base64Key);

            return key;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate encryption key", e);
        }
    }
}
