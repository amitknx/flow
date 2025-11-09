package com.flow.flowboard.api.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * JPA AttributeConverter for converting objects to JSONB
 * Handles serialization/deserialization of complex objects to JSON strings
 *
 * This converter can be used for any field that needs to be stored as JSONB
 * but accessed as a String in the entity
 */
@Converter
@Component
@Slf4j
public class JsonbConverter implements AttributeConverter<String, String> {

    private static ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        JsonbConverter.objectMapper = objectMapper;
    }

    /**
     * Convert entity attribute to database column
     * The attribute is already a JSON string, so just validate it's valid JSON
     */
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }

        try {
            // Validate it's valid JSON by parsing and re-serializing
            Object parsed = objectMapper.readValue(attribute, Object.class);
            return objectMapper.writeValueAsString(parsed);
        } catch (JsonProcessingException e) {
            log.error("Invalid JSON string: {}", attribute, e);
            throw new RuntimeException("Invalid JSON data", e);
        }
    }

    /**
     * Convert database column to entity attribute
     * Just return the JSON string as-is
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        return dbData;
    }
}
