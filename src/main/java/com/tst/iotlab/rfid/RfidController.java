package com.tst.iotlab.rfid;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;

@RestController
@RequestMapping("/rfid")
public class RfidController {
    private static final Logger logger = LoggerFactory.getLogger(RfidController.class);

    @Value("${application.data.dir:./data}")
    private String dataDir;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Path logFilePath;

    @GetMapping(value = "/logs/latest")
    public ResponseEntity<String> sendNewRfid() {
        try {
            logFilePath = Paths.get(dataDir, "static/data/log.json").toAbsolutePath();
            logger.info("Log file path initialized to: {}", logFilePath);
            if (!Files.exists(logFilePath)) {
                return ResponseEntity.ok("{\"message\":\"No RFID records file found\"}");
            }

            String jsonContent = Files.readString(logFilePath, StandardCharsets.UTF_8);

            JsonNode rootNode = objectMapper.readTree(jsonContent);
            JsonNode listNode = rootNode.get("list");

            if (listNode != null && listNode.isArray() && !listNode.isEmpty()) {
                JsonNode latestEntry = listNode.get(0);
                String latestTag = latestEntry.get("tag").asText();
                String latestTimestamp = latestEntry.get("timestamp").asText();

                // JSON ответ с последним RFID
                ObjectNode responseNode = objectMapper.createObjectNode();
                responseNode.put("tag", latestTag);
                responseNode.put("timestamp", latestTimestamp);

                return ResponseEntity.ok(objectMapper.writeValueAsString(responseNode));
            } else {
                return ResponseEntity.ok("{\"message\":\"No RFID records found\"}");
            }
        } catch (IOException e) {
            logger.error("Ошибка чтения JSON файла: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Failed to read JSON data file\"}");
        }
    }

    @GetMapping(value = "/logs", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getJsonData() {
        try {
            this.logFilePath = Paths.get(dataDir, "static/data/log.json").toAbsolutePath();
            logger.info("Log file path initialized to: {}", logFilePath);
            if (!Files.exists(logFilePath)) {
                logger.error("Файл не найден по пути: {}", logFilePath);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"error\":\"JSON data file not found\"}");
            }

            String jsonContent = Files.readString(logFilePath, StandardCharsets.UTF_8);

            objectMapper.readTree(jsonContent); // JSON валиден

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonContent);

        } catch (JsonProcessingException e) {
            logger.error("Невалидный JSON в файле: {}", logFilePath, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Invalid JSON format in data file\"}");
        } catch (IOException e) {
            logger.error("Ошибка чтения файла: {}", logFilePath, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Failed to read data file\"}");
        }
    }


}