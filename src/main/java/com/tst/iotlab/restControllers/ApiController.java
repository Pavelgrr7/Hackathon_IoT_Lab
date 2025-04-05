package com.tst.iotlab.restControllers;

import com.tst.iotlab.SensorDataContainer;
import com.tst.iotlab.WebDataContainer;
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
    final static int voc_alert_value = 1400;
    private final MqttService ms;
    private final Map<String, String> notifies = new HashMap<>();

    @Autowired // Инъекция через конструктор
    public ApiController(MqttService mqttService) {
        this.ms = mqttService;
    }
    @Autowired
    private SensorDataContainer sensorDataContainer;
    @Autowired
    private WebDataContainer webDataContainer;

//    private static final String UPLOAD_DIR = "uploads";

//    @PostMapping(value = "/audio", consumes = "multipart/form-data")
//            public ResponseEntity<String> onAudioReceived(
//            @RequestPart("file") MultipartFile file, // Сам файл
//            @RequestPart("filename") String filename) throws IOException { // Имя файла (теперь RequestPart)
//
//        if (file.isEmpty() || filename.isEmpty()) {
//            return ResponseEntity.badRequest().body("Файл или имя файла отсутствует!");
//        }
//
//        Files.createDirectories(Paths.get(UPLOAD_DIR));
//
////        String fileExtension = getFileExtension(Objects.requireNonNull(file.getOriginalFilename()));
//        File savedFile = new File(UPLOAD_DIR, filename);
//
//        try (FileOutputStream fos = new FileOutputStream(savedFile)) {
//            fos.write(file.getBytes());
//        }
//
//        logger.info("Файл сохранён: {}", savedFile.getAbsolutePath());
//
//        return ResponseEntity.ok("Файл сохранён: " + savedFile.getAbsolutePath());
//    }

    @PostMapping("/rgb")
    public ResponseEntity<Map<String, Object>> OnDeviceStatusChanged(@RequestBody Map<String, String> credentials, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        String color = credentials.get("color");
        String brightness = credentials.get("brightness");
        String toggle = credentials.get("toggle");
        logger.info("пришло сообщение на /rgb");
        if (color != null) {
            logger.info("data is {} {} {}", color, brightness, toggle);
            webDataContainer.updateFieldValue("color", color);
            webDataContainer.updateFieldValue("brightness", brightness);
            if (toggle != null) webDataContainer.updateFieldValue("toggle", toggle);
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
//            logger.info("device = rgb");
            webDataContainer.updateFieldValue("rgb", credentials.get("toggle"));
            response.put("status", true); // зазаа
            response.put("alarm", false); // зазаа
        } else if (device.equals("servo")) {

        }
        if (!response.isEmpty()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

//    @GetMapping("/sensors")
//    public ResponseEntity<Map<String, Object>> OnRequest(HttpSession session) {
//        Map<String, Object> response = new HashMap<>();
//        try {
//            List<String> data = List.of(MqttService.getCurrentPayload().split(";"));
////            logger.info("got data to proceed {}", data.toString());
//
////            List<String> data = List.of("water-level;50000;water-flow;3.5;color;60,220,180;temp;22;humid;1013;light;150;current;2.5;voc;402;co2;400;distance;5;gyro;0;door-reed;HIGH;window-reed;LOW".split(";"));
//            for (int i = 0; i < data.size(); i += 2) {
//                if (i + 1 < data.size()) {
//                    try {
//                        String sensorType = data.get(i);
//                        String sensorValueStr = data.get(i + 1);
////                        String color = "";
////                        while (data.contains("red") || data.contains("green") || data.contains("blue")) {
////                            sensorType = String.join(color, sensorValueStr + " ");
////                        }
////                        if (sensorType.equals("gyro") && sensorValueStr.equals("1")) notifies.put(sensorType, "true");
////                        else if (sensorType.equals("gyro") && sensorValueStr.equals("0") && notifies.containsKey("gyro") && notifies.get("gyro").equals("1")) notifies.remove(sensorType);
//                        if ((sensorType.equals("voc"))) {
//                            if (Integer.parseInt(sensorValueStr) > voc_alert_value) {
//                                notifies.put(sensorType, sensorValueStr);
//                            }
////                            logger.info("added to response: {} {}", sensorType, sensorValueStr);
//                            response.put(sensorType, sensorValueStr);
//                        } else {
//                            response.put(sensorType, sensorValueStr);
//                        }
//                    } catch (NumberFormatException e) {
//                        System.err.println("Invalid number format for value: " + data.get(i + 1));
//                    }
//                }
//            }
//        } catch (NullPointerException e) {
//            logger.error("MQTT IS NULL BUT REQUEST WAS ASKING");
//        }
//
//
//
//        response.put("water-level", 4560000);
//        response.put("water-flow", 456);
//        response.put("colors", "25, 200, 110");
//        response.put("temp", "22, 45, 1013");
//        response.put("humid", "22, 45, 1013");
//        response.put("light", "150");
//        response.put("current", "2.5");
//        response.put("voc", "400");
//        response.put("co2", "400");
//        response.put("distance", "2.5");
//        response.put("door-reed", "0.01");
//        response.put("window-reed", "0.02");

        //logger.info("send to client {}", response.toString());
//        if (!response.isEmpty()) {
//            return ResponseEntity.ok(response);
//        } else {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }

    @GetMapping("/alert")
    public ResponseEntity<Map<String, Object>> OnNewAlert() {
        String TYPE = "type";
        String VOC = "voc";
        Map<String, Object> response = new HashMap<>();
//        if (notifies.containsKey(CO2)) {
//            response.put(TYPE,CO2);
//            notifies.remove(CO2);
//            return ResponseEntity.ok(response);
        if (notifies.containsKey(VOC)) {
            response.put(TYPE, VOC);
            notifies.remove(VOC);
            return ResponseEntity.ok(response);
        }
//        logger.info("BAD REQUEST");
        return ResponseEntity.badRequest().body(new HashMap<>());
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
                ms.publishMessage(topic, key+ ":" + action);
            } catch (MqttException e) {
                logger.error("Mqtt Exception \n {}", e.getMessage());
            }
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping("/get/data")
    public ResponseEntity<Map<String, Object>> onDeviceDataReceived(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        List<String> list = List.of("temp", "humid", "gas");

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
                ms.publishMessage(topic,key + ":" + action);
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
                ms.publishMessage(topic,"brightness" + ":" + brightness);
            } catch (MqttException e) {
                logger.error("Mqtt Exception \n {}", e.getMessage());
            }
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }


//
//    @GetMapping("/get/all")
//    public ResponseEntity<Map<String, String>> getAllSensorData() {
//        Map<String, String> allData = sensorDataContainer.getAllSensorValues();
//
//        if (!allData.isEmpty()) {
//            return ResponseEntity.ok(allData);
//        } else {
//            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
//        }
//    }
}
