package com.tst.iotlab.mqtt;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tst.iotlab.data.SensorDataContainer;
import com.tst.iotlab.rfid.RfidController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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

            logger.info("SensorMqttHandler initialized successfully");
        } catch (Exception e) {
            logger.error("Error initializing SensorMqttHandler", e);
        }
        try {
            outputLogFilePath = Paths.get(dataDir, "static/data/log.json").toAbsolutePath();
            logger.info("Using log file path: {}", outputLogFilePath);

            Files.createDirectories(outputLogFilePath.getParent());

            if (!Files.exists(outputLogFilePath)) {
                try {
                    Resource resource = resourceLoader.getResource("classpath:" + INITIAL_JSON_CLASSPATH_PATH);
                    if (resource.exists()) {
                        try (InputStream is = resource.getInputStream()) {
                            Files.copy(is, outputLogFilePath);
                        }
                    } else {
                        createEmptyLogFile(outputLogFilePath);
                    }
                } catch (IOException e) {
                    logger.error("Failed to initialize log file", e);
                    createEmptyLogFile(outputLogFilePath);
                }
            }
        } catch (Exception e) {
            logger.error("Critical error during RfidMqttHandler initialization", e);
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
                processSingleValueSensorData("co2", parts[2]);
                processSingleValueSensorData("lpg", parts[3]);
                processSingleValueSensorData("ch4", parts[4]);
                processSingleValueSensorData("rfid", parts[5]);
            }
//            if (isJsonPayload(payload)) {
//                // анные в формате JSON
//                Map<String, Object> dataMap = objectMapper.readValue(payload, Map.class);
//                processJsonSensorData(sensorType, dataMap);
//            } else {
//                processSingleValueSensorData(sensorType, payload);
//            }
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
            Object value = dataMap.get("value");
            sensorDataContainer.updateSensorValue(sensorType, value.toString());
        } else if (dataMap.containsKey(sensorType)) {

            Object value = dataMap.get(sensorType);
            sensorDataContainer.updateSensorValue(sensorType, value.toString());
        } else {
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
        if (sensorType.equals("rfid")) {
            saveRfidToJson(value);
            return;
        }
        sensorDataContainer.updateSensorValue(sensorType, value);
        //todo тут можно добавить alert
    }
    private void saveRfidToJson(String rfidTag) {
        Path outputPath = Paths.get(outputLogFilePath.toUri());

        try {
            String content = Files.readString(outputPath, StandardCharsets.UTF_8);
            JsonNode rootNode = objectMapper.readTree(content);

            if (!(rootNode.has("list") && rootNode.get("list").isArray())) {
                logger.error("Invalid JSON structure in log file {}: 'list' array not found or is not an array.", outputLogFilePath);
                createEmptyLogFile(outputPath);
                rootNode = objectMapper.readTree(Files.readString(outputPath, StandardCharsets.UTF_8)); // Читаем заново
                if (!(rootNode.has("list") && rootNode.get("list").isArray())) {
                    logger.error("Failed to correct JSON structure in log file {}. Aborting save.", outputLogFilePath);
                    return;
                }
            }
            ArrayNode listNode = (ArrayNode) rootNode.get("list");

            ObjectNode newRfidEntry = objectMapper.createObjectNode();
            newRfidEntry.put("tag", rfidTag);
            newRfidEntry.put("timestamp", System.currentTimeMillis()); // не стандартный не ISO-8601 формат

            // запись в начало массива
            listNode.insert(0, newRfidEntry);

            // во ВНЕШНИЙ файл
            // WRITE (перезапись) и TRUNCATE_EXISTING (обрезать до 0 перед записью)
            String updatedJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
            Files.writeString(outputPath, updatedJson, StandardCharsets.UTF_8,
                    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

            logger.info("Successfully saved RFID: {} to JSON file: {}", rfidTag, outputLogFilePath);

        } catch (IOException e) {
            logger.error("IOException while reading/writing RFID to JSON file {}: {}", outputLogFilePath, e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error saving RFID to JSON file {}: {}", outputLogFilePath, e.getMessage(), e);
        }
    }

    private static final String INITIAL_JSON_CLASSPATH_PATH = "static/data/log.json";

    @Value("${application.data.dir:./data}")
    private String dataDir;
    private Path outputLogFilePath;


    @Autowired
    private RfidController rfid;

    @Autowired
    private ResourceLoader resourceLoader;
    private void createEmptyLogFile(Path path) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.putArray("list");
            Files.writeString(path,
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            logger.info("Created empty log file with initial structure at {}", path);
        } catch (IOException ex) {
            logger.error("Failed to create empty log file at {}: {}", path, ex.getMessage());
        }
    }

}
