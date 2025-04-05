package com.tst.iotlab.mqtt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMqttMessageHandler implements MqttMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(AbstractMqttMessageHandler.class);

    @Override
    public void handleMessage(String topic, String payload) {
        logger.info("Handling message from topic: {}", topic);
        processMessage(topic, payload);
    }

    protected abstract void processMessage(String topic, String payload);
}

