package com.tst.iotlab.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SensorDataContainer {
    final static int alert_value = 13;
    final static int alert_ch4_value = 33;
    private static final Logger logger = LoggerFactory.getLogger(SensorDataContainer.class);

    private final Map<String, SensorValue> sensorValues = new ConcurrentHashMap<>();

    public static class SensorValue {
        private final String value;

        public SensorValue(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public void updateSensorValue(String sensorType, String value) {
        String normalizedType = normalizeSensorType(sensorType);
        if (normalizedType.equals("co2")){
            if (Integer.parseInt(value) > alert_value) {
                sensorValues.put("alert", new SensorValue("1"));
                logger.info("alert is now 1");
            } else {
                sensorValues.put("alert", new SensorValue("0"));
                logger.info("alert is now 0");
            }
        } else if (normalizedType.equals("ch4")) {
            if (Integer.parseInt(value) > alert_ch4_value) {
                sensorValues.put("alert", new SensorValue("1"));
                logger.info("alert is now 1");
            } else {
                sensorValues.put("alert", new SensorValue("0"));
                logger.info("alert is now 0");
            }
        }
        sensorValues.put(normalizedType, new SensorValue(value));
        logger.debug("Updated sensor data: {} = {}", normalizedType, value);
    }

    public String getSensorValue(String sensorType) {
        String normalizedType = normalizeSensorType(sensorType);

        SensorValue value = sensorValues.get(normalizedType);
        if (value != null) {
            return value.getValue();
        }
        return null;
    }

    public Map<String, String> getAllSensorValues() {
        Map<String, String> result = new ConcurrentHashMap<>();

        sensorValues.forEach((key, value) -> {
            result.put(key, value.getValue());
        });

        return result;
    }

    private String normalizeSensorType(String sensorType) {
        String type = sensorType.toLowerCase();
        return switch (type) {
            case "temperature", "temp" -> "temp";
            case "humidity", "humid", "hum" -> "humid";
            case "gas", "co2", "air" -> "co2";
            default -> type;
        };
    }
}