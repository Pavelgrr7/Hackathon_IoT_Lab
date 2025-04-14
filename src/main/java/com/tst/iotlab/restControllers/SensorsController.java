package com.tst.iotlab.restControllers;

import com.tst.iotlab.data.WebDataContainer;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SensorsController implements DataChangeListener {
    private static final Logger logger = LoggerFactory.getLogger(SensorsController.class);
    private final WebDataContainer webDataContainer;

    @Autowired
    public SensorsController(WebDataContainer webDataContainer) {
        this.webDataContainer = webDataContainer;
        this.webDataContainer.registerListener(this);
    }

    //formerly /rgb
    @PostMapping("/rgb")
    public ResponseEntity<Map<String, Object>> OnDeviceStatusChanged(@RequestBody Map<String, String> credentials, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        String color = credentials.get("color");
        String brightness = credentials.get("brightness");
        String toggle = credentials.get("toggle");
        logger.info("пришло сообщение на /rgb");
        if (color != null) {
            logger.info("data is {} {} {}", color, brightness, toggle);
            webDataContainer.updateFieldValue("color", color, false);
            webDataContainer.updateFieldValue("brightness", brightness, false);
            if (toggle != null) webDataContainer.updateFieldValue("toggle", toggle, false);
            logger.info("container: {}",webDataContainer.getAllValues());
        } else {
            response.put("error", "color is required");
            logger.warn("Request does not contain a valid color! {}", credentials);
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
    @PostMapping("/device")
    public ResponseEntity<Map<String, Object>> PostIfStatusChanged(@RequestBody Map<String, String> credentials, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        String device = credentials.get("device");
        logger.info("Пришло сообщение на /device {}", credentials);
        if (device.equals("rgb")) {
            webDataContainer.updateFieldValue("rgb", credentials.get("toggle"), true);
//            response.put("status", true); // зазаа
        } else if (device.equals("servo")) {

        }
        if (!response.isEmpty()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/get/rgb")
    public ResponseEntity<Map<String, Object>> onWebDataChanged() {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> data = webDataContainer.getAllValues();
        for (String key : data.keySet()) {
            String value = webDataContainer.getFieldValue(key);
            response.put(key, value);
//            logger.info("sedning to /get/rgb {}", response);
        }
        if (!response.isEmpty()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public void onDataChanged() {
        this.onWebDataChanged();
    }
}

