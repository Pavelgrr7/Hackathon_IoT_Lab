package com.tst.iotlab;

import jakarta.servlet.http.HttpSession;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import org.slf4j.Logger;
import java.util.Map;

@CrossOrigin(origins = "http://192.168.0.139:8080")
@RestController
@RequestMapping("/api")
public class RController {
    private static final Logger logger = LoggerFactory.getLogger(RController.class);
    private final SensorsController sc;

    @Autowired // Инъекция через конструктор
    public RController(SensorsController sensorsController) {
        this.sc = sensorsController;
    }
    // Метод для обработки POST-запроса
//    @PostMapping("/sensors")
//    public ResponseEntity<Map<String, String>> sendData(HttpSession session) {
//        String payload = "water-level;500000000000;water-flow;3.5;leakage;LOW;color;255, 0, 0;temp-humid-press;22, 45, 1013;light;150;current: 2.5;voc-co2;400;distance;5; gyro;0.01, 0.02, 15; reed;HIGH; rotation;1200; led-light;300;";
////        session.setAttribute("sensors", formatToJson(payload).toString());
//        session.setAttribute("sensors", payload);
//        Map<String, String> response = new HashMap<>();
//        response.put("success", String.valueOf(true));
//        return ResponseEntity.ok(response);
//    }

    // Метод для обработки GET-запроса
    @GetMapping("/sensors")
    public ResponseEntity<Map<String, Object>> OnRequest(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
//        try {
//            String[] data = MqttService.getCurrentPayload().split(";");
//            for (int i = 0; i < data.length; i += 2) {
//                if (i + 1 < data.length) { // Убедимся, что есть значение для ключа
//                    String key = data[i];
//                    String value = data[i + 1];
//                    response.put(key, value);
//                }
//            }
//        } catch (NullPointerException e) {
//            logger.error("MQTT IS NULL BUT REQUEST WAS ASKING");
//        }
//        String[] data = "water-level;50000;water-flow;3.5;leakage;LOW;color;255, 0, 0;temp-humid-press;22, 45, 1013;light;150;current: 2.5;voc-co2;400;distance;5; gyro;0.01, 0.02, 15; reed;HIGH; rotation;1200; led-light;300;".split(";");



        response.put("water-level", 4560000);
        response.put("water-flow", 456);
        response.put("leakage", "LOW");
        response.put("colors", "255, 0, 0");
        response.put("temp-humid-press", "22, 45, 1013");
        response.put("light", "150");
        response.put("current", "2.5");
        response.put("voc-co2", "400");
        response.put("distance", "2.5");
        response.put("gyro", "0.01, 0.02, 15");
        return ResponseEntity.ok(response);
    }
    @PostMapping("/service")
    public ResponseEntity<Map<String, Object>> OnServiceRequest(@RequestBody Map<String, String> credentials, HttpSession session) {
        // Определение параметров
        String device = credentials.get("device"); // Имя устройства
        Map<String, Object> response = new HashMap<>();

//        sc.sayHello();
//        if (sc.getKeys().contains(device)) {
//            if (device != null && !device.isEmpty()) {
//                // Если указан параметр toggle
//                if (credentials.get(TOGGLE) != null) {
//                    if (credentials.get(TOGGLE).equals("on")) {
////                    logger.info("im in fisrt if");
//
//                        boolean newStatus = true;
//                        sc.setSensorStatus(device, newStatus);
//                        response.put("status", newStatus);
//                        session.setAttribute(device, newStatus);
//
//                    } else if (credentials.get(TOGGLE).equals("off")) {
////                    logger.info("im in second if");
//                        boolean newStatus = false;
//                        sc.setSensorStatus(device, newStatus);
//                        response.put("status", newStatus);
//                        session.setAttribute(device, newStatus);
//
//                    } else if (credentials.get(TOGGLE).isEmpty()) {
//
//                        // Получение текущего статуса устройства
////                        logger.info("im in third if {} {}", SensorsController.getSensorStatus(device), device);
//                        if (sc.getSensorStatus(device).equals("true")) {
//                            boolean status = true;
//                            response.put("status", status);
//                            session.setAttribute(device, status);
////                            logger.info("Returned status for device '{}': {}", device, status);
//                        } else if (sc.getSensorStatus(device).equals("false")) {
//                            boolean status = false;
//                            response.put("status", status);
//                            session.setAttribute(device, status);
////                            logger.info("Returned status for device '{}': {}", device, status);
//                        }
//                    }
//                }
//            }
//        } else {
//            // Если device не указан, возвращаем ошибку
//            response.put("error", "Device name is required");
//            logger.warn("Request does not contain a valid device name");
//            return ResponseEntity.badRequest().body(response);
//        }

        // Возврат успешного ответа
        return ResponseEntity.ok(response);
    }

    @PostMapping("/device")
    public ResponseEntity<Map<String, Object>> OnDeviceStatusChanged(@RequestBody Map<String, String> credentials, HttpSession session) {
        // Определение параметров
        final String TOGGLE = "toggle";
        String device = credentials.get("device"); // Имя устройства
        Map<String, Object> response = new HashMap<>();

//        sc.sayHello();
        if (sc.getKeys().contains(device)) {
            if (device != null && !device.isEmpty()) {
            // Если указан параметр toggle
            if (credentials.get(TOGGLE) != null) {
                if (credentials.get(TOGGLE).equals("on")) {
//                    logger.info("im in fisrt if");

                    boolean newStatus = true;
                    sc.setSensorStatus(device, newStatus);
                    response.put("status", newStatus);
                    session.setAttribute(device, newStatus);

                } else if (credentials.get(TOGGLE).equals("off")) {
//                    logger.info("im in second if");
                    boolean newStatus = false;
                    sc.setSensorStatus(device, newStatus);
                    response.put("status", newStatus);
                    session.setAttribute(device, newStatus);

                } else if (credentials.get(TOGGLE).isEmpty()) {

                        // Получение текущего статуса устройства
//                        logger.info("im in third if {} {}", SensorsController.getSensorStatus(device), device);
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
                // Если device не указан, возвращаем ошибку
                response.put("error", "Device name is required");
                logger.warn("Request does not contain a valid device name");
                return ResponseEntity.badRequest().body(response);
            }

            // Возврат успешного ответа
            return ResponseEntity.ok(response);
        }
//
//    // Метод для форматирования данных в JSON
//    public static JsonObject formatToJson(String payload) {
//        String[] vals = payload.split(";");
//        JsonObject jsonObject = new JsonObject();
//        for (int i = 0; i < vals.length; i += 2) {
//            if (i + 1 < vals.length) { // Убедимся, что есть значение для ключа
//                String key = vals[i];
//                String value = vals[i + 1];
//                try {
//                    // Попробуем интерпретировать значение как число
//                    jsonObject.addProperty(key, Integer.parseInt(value));
//                } catch (NumberFormatException e) {
//                    // Если это не число, добавим как строку
//                    jsonObject.addProperty(key, value);
//                }
//            }
//        }
//        return jsonObject;
//    }


//    @PostMapping("/sensors")
//    public ResponseEntity<Map<String, Object>> sendData(HttpSession session){
//        String payload = "water-level;456;leakage;101112";
//        session.setAttribute("sensors",  formatToJson(payload).toString());
//        Map<String, Object> response = new HashMap<>();
//        response.put("success", true);
//        return ResponseEntity.ok(response);
//    }
//    public Map<String, String> processData(@RequestBody Map<String, String> data) {
////        for (String key : data.keySet()) {
////            if (users.containsKey(key)) {
////                if (users.get(key).equals(data.get(key))) {
////                    data.put("Nice", "Success");
////                }
////            }
////        }
//        data.put("status", "processed");
//        return data;
//    }


//
//    @PostMapping("/recivedData")
//    public ResponseEntity<String> receiveData(@RequestBody Map<String, String> requestData) {
//        System.out.println("Полученные данные: " + requestData);
//        String response = "Данные успешно получены";
//        return ResponseEntity.ok(response);
//    }
}
