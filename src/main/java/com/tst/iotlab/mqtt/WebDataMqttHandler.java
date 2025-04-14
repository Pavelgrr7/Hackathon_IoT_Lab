package com.tst.iotlab.mqtt;

import com.tst.iotlab.data.WebDataContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Service
public class WebDataMqttHandler extends AbstractMqttMessageHandler{


    private static final Logger logger = LoggerFactory.getLogger(WebDataMqttHandler.class);

    private MqttService mqttService;

    @Autowired
    public void setMqttService(MqttService mqttService) {
        this.mqttService = mqttService;
    }

    @Autowired
    private WebDataContainer webDataContainer;

    @PostConstruct
    public void init() {
        try {
            logger.info("Initializing SensorMqttHandler");

            if (mqttService == null) {
                logger.error("MqttService is not initialized!");
                return;
            }

            mqttService.registerHandler("mqtt/rgb", this);

            logger.info("SensorMqttHandler initialized successfully");
        } catch (Exception e) {
            logger.error("Error initializing SensorMqttHandler", e);
        }
    }

    @Override
    protected void processMessage(String topic, String payload) {
        logger.info("Received sensor data on topic {}: {}", topic, payload);
        try {
            List<String> field = Arrays.stream(payload.split(":")).toList();
            processSingleValueFieldData("color", field.getFirst());
            processSingleValueFieldData("brightness", field.get(1));
            processSingleValueFieldData("toggle", field.get(2));
            logger.info("splited: {}", field);
        } catch (Exception e) {
            logger.error("Error processing sensor data from topic {}: {}", topic, e.getMessage(), e);
        }
    }

    private void processSingleValueFieldData(String field, String value) {
        logger.info("Processing single value sensor data for {}: {}", field, value);

        webDataContainer.updateFieldValue(field, value, true);
    }
}
