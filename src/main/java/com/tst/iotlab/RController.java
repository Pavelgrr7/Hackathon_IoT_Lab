package com.tst.iotlab;

import jakarta.servlet.http.HttpSession;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@CrossOrigin(origins = "http://192.168.0.139:8080")
@RestController
@RequestMapping("/api")
public class RController {
    private static final Logger logger = LoggerFactory.getLogger(RController.class);
    private final SensorsController sc;
    private Map<String, String> notifies = new HashMap<>();


    @Autowired // Инъекция через конструктор
    public RController(SensorsController sensorsController) {
        this.sc = sensorsController;
    }

    @GetMapping("/sensors")
    public ResponseEntity<Map<String, Object>> OnRequest(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<String> data = List.of(MqttService.getCurrentPayload().split(";"));
//            logger.info("got data to proceed {}", data.toString());

//            List<String> data = List.of("water-level;50000;water-flow;3.5;color;60,220,180;temp;22;humid;1013;light;150;current;2.5;voc;402;co2;400;distance;5;gyro;0;door-reed;HIGH;window-reed;LOW".split(";"));
            for (int i = 0; i < data.size(); i += 2) {
                if (i + 1 < data.size()) {
                    try {
                        String sensorType = data.get(i);
                        String sensorValueStr = data.get(i + 1);
//                        String color = "";
//                        while (data.contains("red") || data.contains("green") || data.contains("blue")) {
//                            sensorType = String.join(color, sensorValueStr + " ");
//                        }
                        if (sensorType.equals("gyro") && sensorValueStr.equals("1")) notifies.put(sensorType, "true");
//                        else if (sensorType.equals("gyro") && sensorValueStr.equals("0") && notifies.containsKey("gyro") && notifies.get("gyro").equals("1")) notifies.remove(sensorType);
                        else if ((sensorType.equals("co2") || sensorType.equals("voc"))) {
                            if (Integer.parseInt(sensorValueStr) > 1400) {
                                notifies.put(sensorType, sensorValueStr);
                            }
//                            logger.info("added to response: {} {}", sensorType, sensorValueStr);
                            response.put(sensorType, sensorValueStr);
                        } else {
                            response.put(sensorType, sensorValueStr);
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid number format for value: " + data.get(i + 1));
                    }
                }
            }
        } catch (NullPointerException e) {
            logger.error("MQTT IS NULL BUT REQUEST WAS ASKING");
        }
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
        if (!response.isEmpty()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/alert")
    public ResponseEntity<Map<String, Object>> OnNewAlert() {
        String GYRO = "gyro";
        String TYPE = "type";
        String CO2 = "co2";
        String VOC = "voc";
        //logger.info("NOTIFIES {}", notifies.toString());
        Map<String, Object> response = new HashMap<>();
        if (notifies.containsKey(GYRO)) {
            response.put(TYPE,GYRO);
            notifies.remove(GYRO);
            return ResponseEntity.ok(response);
        } else if (notifies.containsKey(CO2)) {
            response.put(TYPE,CO2);
            notifies.remove(CO2);
            return ResponseEntity.ok(response);
        } else if (notifies.containsKey(VOC)) {
            response.put(TYPE, VOC);
            notifies.remove(VOC);
            return ResponseEntity.ok(response);
        }
//        logger.info("BAD REQUEST");
        return ResponseEntity.badRequest().body(new HashMap<>());
    }



    List<String> serviceDevices = List.of("door-servo", "window-servo", "pump", "fan","led-strip");
    @PostMapping("/service")
    public ResponseEntity<Map<String, Object>> OnServiceRequest(@RequestBody Map<String, String> credentials) {
        String device = credentials.get("device");
        logger.info("got device to service {}", device);
        Map<String, Object> response = new HashMap<>();

        if (device == null || device.isEmpty()) {
            response.put("error", "Device name is missing");
            return ResponseEntity.badRequest().body(response);
        }

        if (!serviceDevices.contains(device)) {
            response.put("error", "Unsupported device");
            return ResponseEntity.badRequest().body(response);
        }

        // Уникальный идентификатор для ожидания ответа
        String correlationId = UUID.randomUUID().toString();

        try {
            sc.sendToMqtt("test/topic", device + ":" + correlationId);

            // Ожидание ответа
            boolean isCheckPassed = sc.waitForResponse(correlationId, 45000);

            response.put("device", device);
            response.put("status", isCheckPassed ? "passed" : "failed");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Failed to process service request");
            response.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    }

    @PostMapping("/device")
    public ResponseEntity<Map<String, Object>> OnDeviceStatusChanged(@RequestBody Map<String, String> credentials, HttpSession session) {
        final String TOGGLE = "toggle";
        String device = credentials.get("device"); // Имя устройства
        Map<String, Object> response = new HashMap<>();
        if (device.equals("servo1") || device.equals("servo2")) {return ResponseEntity.badRequest().body(response);}
//        sc.sayHello();
        if (sc.getKeys().contains(device)) {
            if (device != null && !device.isEmpty()) {
            if (credentials.get(TOGGLE) != null) {
                if (credentials.get(TOGGLE).equals("on")) {

                    boolean newStatus = true;
                    sc.setSensorStatus(device, newStatus);
                    response.put("status", newStatus);
                    session.setAttribute(device, newStatus);

                } else if (credentials.get(TOGGLE).equals("off")) {
                    boolean newStatus = false;
                    sc.setSensorStatus(device, newStatus);
                    response.put("status", newStatus);
                    session.setAttribute(device, newStatus);

                } else if (credentials.get(TOGGLE).isEmpty()) {
                        if (sc.getSensorStatus(device).equals("true")) {
                            boolean status = true;
                            response.put("status", status);
                            session.setAttribute(device, status);
//                            logger.info("Returned status for device '{}': {}", device, status);
                        } else if (sc.getSensorStatus(device).equals("false")) {
                            boolean status = false;
                            response.put("status", status);
                            session.setAttribute(device, status);
//                            logger.info("Returned status for device '{}': {}", device, status);
                        }
                    }
                }
            }
        } else {
                response.put("error", "Device name is required");
                logger.warn("Request does not contain a valid device name {}", device);
                return ResponseEntity.badRequest().body(response);
            }

            return ResponseEntity.ok(response);
        }
}
