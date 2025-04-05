//package com.tst.iotlab.restControllers;
//
//import com.tst.iotlab.mqtt.MqttService;
//import org.eclipse.paho.client.mqttv3.MqttException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Set;
//
//import org.springframework.stereotype.Service;
//
//
//import java.util.Collections;
//
//
//@Service
//public class SensorsController {
//    private static final Logger logger = LoggerFactory.getLogger(SensorsController.class);
//
//    private final MqttService mqttService;
//    private final Map<String, Boolean> sensors;
//    private final Map<String, Integer> values;
//
//    public SensorsController(MqttService mqttService) {
//        this.mqttService = mqttService;
//        this.sensors = Collections.synchronizedMap(new HashMap<>(Map.of(
//                "rgb", false,
//                "gas", false,
//                "temp", false,
//                "humid", false,
//                "servo", false
//        )));
//        this.values = Collections.synchronizedMap(new HashMap<>(Map.of(
//                "rgb", -1,
//                "gas", -1,
//                "temp", -1,
//                "humid", -1,
//                "servo", -1
//        )));
//    }
//
//    public Set<String> getKeys() {
//        return sensors.keySet();
//    }
//
//    public void setSensorStatus(String sensorName, boolean status) {
//        logger.info("Setting sensor [{}] to status [{}]", sensorName, status);
//        sensors.put(sensorName, status);
//        try {
//            int data = 0;
//            if (status) {
//                data = 1;
//            }
//            mqttService.publishMessage("test/topic", sensorName + ":" + data);
//        } catch (Exception e) {
//            logger.error("Error while publishing sensor status to MQTT", e);
//        }
//    }
//
//    public Boolean getSensorStatus(String sensorName) {
//        return sensors.getOrDefault(sensorName, false);
//    }
//
//    public Integer getSensorValue(String sensorName) {
//        return values.getOrDefault(sensorName, -1);
//    }
//
//    public void sendToMqtt(String topic, String data) {
//        try {
//            mqttService.publishMessage(topic, data);
//        } catch (MqttException e) {
//            logger.error("Error while sending data to MQTT IN SensorsController");
//        }
//    }
//
//    public boolean waitForResponse(String correlationId, int timeoutMs) {
//        try {
//            return mqttService.waitForResponse(correlationId, timeoutMs);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return  false;
//        }
//    }
//
//    // Инициализация сенсоров
//    public void init(Map<String, Boolean> initSensors) {
//        sensors.clear();
//        sensors.putAll(initSensors);
//        logger.info("Sensors initialized: {}", sensors);
//    }
//}
//
