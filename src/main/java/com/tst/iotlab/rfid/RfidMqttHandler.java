//package com.tst.iotlab.rfid;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ArrayNode;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import com.tst.iotlab.mqtt.AbstractMqttMessageHandler;
//import com.tst.iotlab.mqtt.MqttService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.DependsOn;
//import org.springframework.core.io.Resource;
//import org.springframework.core.io.ResourceLoader;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.PostConstruct;
//
//import java.io.IOException;
//import java.time.Instant;
//
//import java.io.InputStream;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.nio.file.StandardOpenOption;
//import java.nio.charset.StandardCharsets;
//
//
//@Component
//@DependsOn("mqttService")
//public class RfidMqttHandler extends AbstractMqttMessageHandler {
//    private static final Logger logger = LoggerFactory.getLogger(RfidMqttHandler.class);
//    private static final String INITIAL_JSON_CLASSPATH_PATH = "static/data/log.json";
//
//    @Value("${application.data.dir:./data}")
//    private String dataDir;
//    private Path outputLogFilePath;
//
//    @Autowired
//    private MqttService mqttService;
//
//    @Autowired
//    private RfidController rfid;
//
//    @Autowired
//    private ResourceLoader resourceLoader;
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    @PostConstruct
//    public void init() {
//        try {
//            outputLogFilePath = Paths.get(dataDir, "static/data/log.json").toAbsolutePath();
//            logger.info("Using log file path: {}", outputLogFilePath);
//
//            Files.createDirectories(outputLogFilePath.getParent());
//
//            if (!Files.exists(outputLogFilePath)) {
//                try {
//                    Resource resource = resourceLoader.getResource("classpath:" + INITIAL_JSON_CLASSPATH_PATH);
//                    if (resource.exists()) {
//                        try (InputStream is = resource.getInputStream()) {
//                            Files.copy(is, outputLogFilePath);
//                        }
//                    } else {
//                        createEmptyLogFile(outputLogFilePath);
//                    }
//                } catch (IOException e) {
//                    logger.error("Failed to initialize log file", e);
//                    createEmptyLogFile(outputLogFilePath);
//                }
//            }
//            mqttService.registerHandler("mqtt/rfid", this);
//            logger.info("RfidMqttHandler initialized successfully and registered for topic 'rfid/'");
//
//        } catch (Exception e) {
//            logger.error("Critical error during RfidMqttHandler initialization", e);
//        }
//    }
//    private void createEmptyLogFile(Path path) {
//        try {
//            ObjectNode root = objectMapper.createObjectNode();
//            root.putArray("list");
//            Files.writeString(path,
//                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root),
//                    StandardCharsets.UTF_8,
//                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
//            logger.info("Created empty log file with initial structure at {}", path);
//        } catch (IOException ex) {
//            logger.error("Failed to create empty log file at {}: {}", path, ex.getMessage());
//        }
//    }
//
//
//    @Override
//    protected void processMessage(String topic, String payload) {
//        logger.info("Received RFID data on topic {}: {}", topic, payload);
//        try {
//            String rfidTag = payload.trim();
//            if (rfidTag.isEmpty()) {
//                logger.warn("Received empty RFID tag on topic {}", topic);
//                return;
//            }
//            saveRfidToJson(rfidTag);
//
//            ResponseEntity<String> response = rfid.sendNewRfid();
//            logger.info("Controller response status: {}, body: {}", response.getStatusCode(), response.getBody());
//
//        } catch (Exception e) {
//            logger.error("Error processing MQTT message or calling controller: {}", e.getMessage(), e);
//        }
//    }
//
//    private void saveRfidToJson(String rfidTag) {
//        Path outputPath = Paths.get(outputLogFilePath.toUri());
//
//        try {
//            String content = Files.readString(outputPath, StandardCharsets.UTF_8);
//            JsonNode rootNode = objectMapper.readTree(content);
//
//            if (!(rootNode.has("list") && rootNode.get("list").isArray())) {
//                logger.error("Invalid JSON structure in log file {}: 'list' array not found or is not an array.", outputLogFilePath);
//                createEmptyLogFile(outputPath);
//                rootNode = objectMapper.readTree(Files.readString(outputPath, StandardCharsets.UTF_8)); // Читаем заново
//                if (!(rootNode.has("list") && rootNode.get("list").isArray())) {
//                    logger.error("Failed to correct JSON structure in log file {}. Aborting save.", outputLogFilePath);
//                    return;
//                }
//            }
//            ArrayNode listNode = (ArrayNode) rootNode.get("list");
//
//            ObjectNode newRfidEntry = objectMapper.createObjectNode();
//            newRfidEntry.put("tag", rfidTag);
//            newRfidEntry.put("timestamp", System.currentTimeMillis()); // не стандартный не ISO-8601 формат
//
//            // запись в начало массива
//            listNode.insert(0, newRfidEntry);
//
//            // во ВНЕШНИЙ файл
//            // WRITE (перезапись) и TRUNCATE_EXISTING (обрезать до 0 перед записью)
//            String updatedJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
//            Files.writeString(outputPath, updatedJson, StandardCharsets.UTF_8,
//                    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
//
//            logger.info("Successfully saved RFID: {} to JSON file: {}", rfidTag, outputLogFilePath);
//
//        } catch (IOException e) {
//            logger.error("IOException while reading/writing RFID to JSON file {}: {}", outputLogFilePath, e.getMessage(), e);
//        } catch (Exception e) {
//            logger.error("Unexpected error saving RFID to JSON file {}: {}", outputLogFilePath, e.getMessage(), e);
//        }
//    }
//}