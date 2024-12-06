package com.tst.iotlab;

import com.google.gson.JsonObject;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Service;
@Service
public class MqttService {
    private static final String BROKER_URL = "tcp://localhost:1884";
    private static final String CLIENT_ID = "JavaServerClient";
    private static final String SUB_TOPIC = "test/connection";
    private IMqttClient client;

    public MqttService() throws MqttException {
        client = new MqttClient(BROKER_URL, CLIENT_ID);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        client.connect(options);
        subscribeToTopic(SUB_TOPIC);
    }

    public void subscribeToTopic(String topic) throws MqttException {
        client.subscribe(topic, (t, msg) -> {
            String payload = new String(msg.getPayload());
            System.out.println("Message received: " + payload);
            publishMessage(SUB_TOPIC, RController.formatToJson(payload));
        });
    }
    public void publishMessage(String topic, JsonObject message) throws MqttException {
        MqttMessage mqttMessage = new MqttMessage(message.toString().getBytes());
        mqttMessage.setQos(1);
        client.publish(topic, mqttMessage);
    }
}
