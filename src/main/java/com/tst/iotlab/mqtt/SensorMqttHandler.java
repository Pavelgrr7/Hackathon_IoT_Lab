package com.tst.iotlab.mqtt;
import com.tst.iotlab.SensorDataContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

@Component
@DependsOn("mqttService")
public class SensorMqttHandler extends AbstractMqttMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(SensorMqttHandler.class);

    private MqttService mqttService;

    @Autowired
    public void setMqttService(MqttService mqttService) {
        this.mqttService = mqttService;
    }

    @Autowired
    private SensorDataContainer sensorDataContainer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        try {
            logger.info("Initializing SensorMqttHandler");

            if (mqttService == null) {
                logger.error("MqttService is not initialized!");
                return;
            }

            mqttService.registerHandler("mqtt/ht", this);
            mqttService.registerHandler("mqtt/gas", this);

            logger.info("SensorMqttHandler initialized successfully");
        } catch (Exception e) {
            logger.error("Error initializing SensorMqttHandler", e);
        }
    }

    @Override
    protected void processMessage(String topic, String payload) {
        logger.info("Received sensor data on topic {}: {}", topic, payload);

        try {
            String sensorType = extractSensorTypeFromTopic(topic);
            if (sensorType.equals("ht")) {
                String[] parts = payload.split(":");
                processSingleValueSensorData("humidity", parts[0]);
                processSingleValueSensorData("temperature", parts[1]);
            }
            if (isJsonPayload(payload)) {
                // анные в формате JSON
                Map<String, Object> dataMap = objectMapper.readValue(payload, Map.class);
                processJsonSensorData(sensorType, dataMap);
            } else {
                processSingleValueSensorData(sensorType, payload);
            }
        } catch (Exception e) {
            logger.error("Error processing sensor data from topic {}: {}", topic, e.getMessage(), e);
        }
    }

    private String extractSensorTypeFromTopic(String topic) {
        String[] parts = topic.split("/");
        if (parts.length >= 2) {
            return parts[1].toLowerCase();
        }
        return "unknown";
    }

    private boolean isJsonPayload(String payload) {
        return payload.trim().startsWith("{") && payload.trim().endsWith("}");
    }

    private void processJsonSensorData(String sensorType, Map<String, Object> dataMap) {
        logger.debug("Processing JSON sensor data for {}: {}", sensorType, dataMap);

        if (dataMap.containsKey("value")) {
            // JSON вида {"value": 25.5}
            Object value = dataMap.get("value");
            sensorDataContainer.updateSensorValue(sensorType, value.toString());
        } else if (dataMap.containsKey(sensorType)) {
            // JSON вида {"temperature": 25.5}
            Object value = dataMap.get(sensorType);
            sensorDataContainer.updateSensorValue(sensorType, value.toString());
        } else {
            // JSON содержит несколько значений с датчиков
            dataMap.forEach((key, value) -> {
                if (isSensorType(key)) {
                    sensorDataContainer.updateSensorValue(key, value.toString());
                }
            });
        }
    }

    private boolean isSensorType(String key) {
        String normalized = key.toLowerCase();
        return normalized.equals("temp") ||
                normalized.equals("temperature") ||
                normalized.equals("humid") ||
                normalized.equals("humidity") ||
                normalized.equals("gas") ||
                normalized.equals("co2") ||
                normalized.equals("pressure");
    }

    private void processSingleValueSensorData(String sensorType, String value) {
        logger.debug("Processing single value sensor data for {}: {}", sensorType, value);

        String cleanValue = cleanSensorValue(value);

        sensorDataContainer.updateSensorValue(sensorType, cleanValue);
        //todo тут можно добавить alert
    }

    private String cleanSensorValue(String value) {
        return value.replaceAll("[^0-9.-]", "").trim();
    }
}
