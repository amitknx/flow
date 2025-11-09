package com.flow.flowboard.api.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * JPA AttributeConverter that encrypts/decrypts only VALUES in a Map for database storage
 * Keys remain unencrypted for queryability
 *
 * Usage:
 * <pre>
 * {@code
 * @Convert(converter = EncryptedJsonConverter.class)
 * @JdbcTypeCode(SqlTypes.JSON)
 * @Column(name = "secret_data", columnDefinition = "jsonb")
 * private Map<String, String> secretData;
 * }
 * </pre>
 *
 * Security:
 * - Encrypts only values in the map before storing in database
 * - Decrypts only values when loading from database
 * - Keys remain unencrypted for queryability and structure
 * - Uses AES-256-GCM via EncryptionUtil
 *
 * Example:
 * - Input: {"api_key": "secret123", "client_id": "abc"}
 * - Stored: {"api_key": "<encrypted>", "client_id": "<encrypted>"}
 * - Retrieved: {"api_key": "secret123", "client_id": "abc"}
 */
@Converter
@Component
@Slf4j
public class EncryptedJsonConverter implements AttributeConverter<Map<String, String>, Map<String, String>> {

    private static EncryptionUtil encryptionUtil;

    @Autowired
    public void setEncryptionUtil(EncryptionUtil encryptionUtil) {
        EncryptedJsonConverter.encryptionUtil = encryptionUtil;
    }

    /**
     * Converts entity attribute to database column (encrypts values only)
     * Keys remain unencrypted
     */
    @Override
    public Map<String, String> convertToDatabaseColumn(Map<String, String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }

        try {
            Map<String, String> encryptedMap = new HashMap<>();
            
            // Encrypt only values, keep keys as-is
            for (Map.Entry<String, String> entry : attribute.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                
                if (value != null) {
                    // Encrypt the value
                    String encryptedValue = encryptionUtil.encrypt(value);
                    encryptedMap.put(key, encryptedValue);
                } else {
                    encryptedMap.put(key, null);
                }
            }

            log.debug("Encrypted {} values in secret data", encryptedMap.size());
            return encryptedMap;

        } catch (Exception e) {
            log.error("Failed to encrypt secret data", e);
            throw new RuntimeException("Failed to encrypt secret data", e);
        }
    }

    /**
     * Converts database column to entity attribute (decrypts values only)
     * Keys remain unencrypted
     */
    @Override
    public Map<String, String> convertToEntityAttribute(Map<String, String> dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }

        try {
            Map<String, String> decryptedMap = new HashMap<>();
            
            // Decrypt only values, keep keys as-is
            for (Map.Entry<String, String> entry : dbData.entrySet()) {
                String key = entry.getKey();
                String encryptedValue = entry.getValue();
                
                if (encryptedValue != null && !encryptedValue.isEmpty()) {
                    try {
                        // Decrypt the value
                        String decryptedValue = encryptionUtil.decrypt(encryptedValue);
                        decryptedMap.put(key, decryptedValue);
                    } catch (Exception e) {
                        log.error("Failed to decrypt value for key: {}", key, e);
                        throw new RuntimeException("Failed to decrypt secret data for key: " + key, e);
                    }
                } else {
                    decryptedMap.put(key, null);
                }
            }

            log.debug("Decrypted {} values from secret data", decryptedMap.size());
            return decryptedMap;

        } catch (Exception e) {
            log.error("Failed to decrypt secret data", e);
            throw new RuntimeException("Failed to decrypt secret data", e);
        }
    }
}
