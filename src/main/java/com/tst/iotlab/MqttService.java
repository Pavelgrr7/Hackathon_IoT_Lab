package com.tst.iotlab;


import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class MqttService {
    private static final Logger logger = LoggerFactory.getLogger(MqttService.class);

    private static final String BROKER_URL = "tcp://localhost:1884";
    private static final String CLIENT_ID = "JavaServerClient";
    private static final String SUB_TOPIC = "test/topic";
    private final IMqttClient client;
    private static String payload ="";

    public MqttService() throws MqttException {
        client = new MqttClient(BROKER_URL, CLIENT_ID);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        client.connect(options);
        subscribeToTopic(SUB_TOPIC);
    }

    private final Map<String, Boolean> serviceResponses = new ConcurrentHashMap<>();

    boolean waitForResponse(String correlationId, int timeoutMs) throws InterruptedException, MqttException {
        CountDownLatch latch = new CountDownLatch(1);

        // Подписываемся на топик с ответами
        client.subscribe(SUB_TOPIC, (t, msg) -> {
            String payload = new String(msg.getPayload());
            String[] parts = payload.split(":");
            if (parts.length == 2 && parts[0].equals(correlationId)) {
                serviceResponses.put(correlationId, "true".equalsIgnoreCase(parts[1]));
                latch.countDown();
            }
        });

        // Ожидание до таймаута
        boolean isResponseReceived = latch.await(timeoutMs, TimeUnit.MILLISECONDS);
        return isResponseReceived && serviceResponses.getOrDefault(correlationId, false);
    }


    public static String getCurrentPayload() {
        return payload;
    }

    public void subscribeToTopic(String topic) throws MqttException {
        client.subscribe(topic, (t, msg) -> {
            payload = new String(msg.getPayload());
            logger.info("Message received: {} ", payload);
//
//            if (payload.length() > 10) {
//            } else {
//                logger.info("Recives msg less than 10 symbols: {} ", payload.length());
//                payload = "";
//            }
        });
    }
    public void publishMessage(String topic, String message) throws MqttException {
        MqttMessage mqttMessage = new MqttMessage(message.getBytes());
        mqttMessage.setQos(1);
        logger.info("Message sent: {} ", message);
        client.publish(topic, mqttMessage);
    }
}
