package com.tst.iotlab.restControllers;

import com.tst.iotlab.data.SensorDataContainer;
import com.tst.iotlab.mqtt.MqttService;
import jakarta.servlet.http.HttpSession;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import org.slf4j.Logger;

@RestController
@RequestMapping("/api")
public class ApiController {
    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);
    private final MqttService ms;

    @Autowired
    public ApiController(MqttService mqttService) {
        this.ms = mqttService;
    }

    @Autowired
    private SensorDataContainer sensorDataContainer;

    @GetMapping("/alert")
    public ResponseEntity<Map<String, Object>> OnNewAlert() {
        Map<String, Object> response = new HashMap<>();
        if (Objects.equals(sensorDataContainer.getSensorValue("alert"), "1")) {
            response.put("type","gas");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/vehicle")
    public ResponseEntity<Map<String, Object>> onVehicleControlSent(@RequestBody Map<String, String> credentials, HttpSession session) {
        logger.info("control received");
        String key = credentials.get("key");
        String action = credentials.get("action");
        String topic = "mqtt/move";
        Map<String, Object> response = new HashMap<>();
        if (key != null && action != null) {
            try {
                ms.publishMessage(topic, "m"+ ":" + key);
            } catch (MqttException e) {
                logger.error("Mqtt Exception vehicle \n {}", e.getMessage());
            }
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping("/get/data")
    public ResponseEntity<Map<String, Object>> onDeviceDataReceived(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        List<String> list = List.of("temp", "humid", "co2", "lpg", "ch4");

        for (String key : list) {
            String value = sensorDataContainer.getSensorValue(key);
            if (value != null) {
                if (Integer.parseInt(value) == -1) {
                    Random rnd = new Random();
                    value = String.valueOf(rnd.nextInt());
                }
                response.put(key, value);
            }
        }

        if (!response.isEmpty()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/servo")
    public ResponseEntity<Map<String, Object>> onServoControlSent(@RequestBody Map<String, String> credentials, HttpSession session) {
        logger.info("servo control received");
        String key = credentials.get("key");
        String action = credentials.get("action");
        String topic = "mqtt/actions";
        Map<String, Object> response = new HashMap<>();
        if (key != null && action != null) {
            try {
                ms.publishMessage(topic,"a" + ":" + key);
            } catch (MqttException e) {
                logger.error("Mqtt Exception \n {}", e.getMessage());
            }
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @PostMapping("/flash")
    public ResponseEntity<Map<String, Object>> onFlashChanged(@RequestBody Map<String, String> credentials, HttpSession session) {
        logger.info("flash value changed");
        String brightness = credentials.get("brightness");
        String topic = "mqtt/actions";
        Map<String, Object> response = new HashMap<>();
        if (brightness != null) {
            try {
                ms.publishMessage(topic,"b" + ":" + brightness);
            } catch (MqttException e) {
                logger.error("Mqtt Exception \n {}", e.getMessage());
            }
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    @PostMapping("/servo2")
    public ResponseEntity<Map<String, Object>> onServo2ControlSent(@RequestBody Map<String, String> credentials, HttpSession session) {
        logger.info("servo2 control received");
        String key = credentials.get("key");
        String action = credentials.get("action");
        String topic = "mqtt/mine";
        Map<String, Object> response = new HashMap<>();
        if (key != null && action != null) {
            try {
                ms.publishMessage(topic,"s" + ":" + key);
            } catch (MqttException e) {
                logger.error("Mqtt Exception \n {}", e.getMessage());
            }
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @PostMapping("/flash2")
    public ResponseEntity<Map<String, Object>> onFlash2Changed(@RequestBody Map<String, String> credentials, HttpSession session) {
        logger.info("flash2 value changed");
        String brightness = credentials.get("brightness");
        String topic = "mqtt/mine";
        Map<String, Object> response = new HashMap<>();
        if (brightness != null) {
            try {
                ms.publishMessage(topic,"f" + ":" + brightness);
            } catch (MqttException e) {
                logger.error("Mqtt Exception \n {}", e.getMessage());
            }
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}
