package com.tst.iotlab.mqtt;

public interface MqttMessageHandler {
    void handleMessage(String topic, String payload);
}

