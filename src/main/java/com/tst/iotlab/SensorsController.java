package com.tst.iotlab;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//@Service
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class SensorsController {
    private static final Logger logger = LoggerFactory.getLogger(SensorsController.class);

    private final MqttService mqttService;
    private final Map<String, String> sensors;

    // Конструктор с инъекцией зависимости
    public SensorsController(MqttService mqttService) {
        this.mqttService = mqttService;
        this.sensors = Collections.synchronizedMap(new HashMap<>(Map.of(
                "fan", "false",
                "pump", "false",
                "servo1", "false",
                "servo2", "false",
                "led", "false"
        )));
    }

    // Получение всех ключей сенсоров
    public Set<String> getKeys() {
        return sensors.keySet();
    }

    // Установить статус сенсора
    public void setSensorStatus(String sensorName, boolean status) {
        logger.info("Setting sensor [{}] to status [{}]", sensorName, status);
        sensors.put(sensorName, Boolean.toString(status));
        try {
            mqttService.publishMessage("test/topic", sensorName + ":" + status);
        } catch (Exception e) {
            logger.error("Error while publishing sensor status to MQTT", e);
        }
    }

    // Получить статус сенсора
    public String getSensorStatus(String sensorName) {
        return sensors.getOrDefault(sensorName, "unknown");
    }

    // Приветственное сообщение
//    public void sayHello() {
//        try {
////            mqttService.publishMessage("test/topic", "Hello, esp32 ^_^");
//        } catch (Exception e) {
//            logger.error("Error sending MQTT message in sayHello", e);
//        }
//    }

    // Инициализация сенсоров
    public void init(Map<String, String> initSensors) {
        sensors.clear();
        sensors.putAll(initSensors);
        logger.info("Sensors initialized: {}", sensors);
    }
}

