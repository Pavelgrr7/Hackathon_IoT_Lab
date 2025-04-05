package com.tst.iotlab;

import com.tst.iotlab.mqtt.AbstractMqttMessageHandler;
import com.tst.iotlab.restControllers.ApiController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RestApiMqttHandler extends AbstractMqttMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(RestApiMqttHandler.class);

    @Override
    protected void processMessage(String topic, String payload) {
        logger.info("Processing message for REST API: {} from topic: {}", payload, topic);
//         Здесь логика отправки данных через REST API
//         Например:
    }
}
