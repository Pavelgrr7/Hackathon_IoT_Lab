package com.tst.iotlab;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebDataContainer {
    private static final Logger logger = LoggerFactory.getLogger(WebDataContainer.class);

    private final Map<String, FieldValue> fieldValues = new ConcurrentHashMap<>();

    public record FieldValue(String value) {

        @Override
            public String toString() {
                return value;
            }
        }

    public void updateFieldValue(String sensorType, String value) {
        String normalizedType = normalizeFiledType(sensorType);

        fieldValues.put(normalizedType, new FieldValue(value));
        logger.debug("Updated sensor data: {} = {}", normalizedType, value);
    }

    public String getFieldValue(String sensorType) {
        String normalizedType = normalizeFiledType(sensorType);

        FieldValue value = fieldValues.get(normalizedType);
        if (value != null) {
            return value.value();
        }
        return null;
    }

    public Map<String, String> getAllValues() {
        Map<String, String> result = new ConcurrentHashMap<>();

        fieldValues.forEach((key, value) -> {
            result.put(key, value.value());
        });

        return result;
    }

    private String normalizeFiledType(String sensorType) {
        String type = sensorType.toLowerCase();
        return switch (type) {
            case "temperature", "temp" -> "temp";
            case "humidity", "humid", "hum" -> "humid";
            case "gas", "co2", "air" -> "gas";
            default -> type;
        };
    }
}
