package com.tst.iotlab.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class MqttService {
    private static final Logger logger = LoggerFactory.getLogger(MqttService.class);

    @Value("${mqtt.broker.url}")
    private String brokerUrl = "6a41760a26ec43f2b0e532601ce780e1.s1.eu.hivemq.cloud:8883";

    @Value("${mqtt.client.id}")
    private String clientId = "121";

    @Value("${mqtt.topic}")
    private String defaultTopic = "mqtt/test";

    @Value("${mqtt.username}")
    private String username = "JavaSrv";

    @Value("${mqtt.password}")
    private String password = "ww23W789";

    private IMqttClient client;

    @Autowired
    private MqttHandlerManager handlerManager;

    private final Map<String, Boolean> serviceResponses = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        try {
            logger.info("Initializing MQTT client with clientId: {}", clientId);
            if (clientId == null) {
                throw new IllegalStateException("clientId is null! Check @Value injection");
            }

            client = new MqttClient("ssl://" + brokerUrl, clientId);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setUserName(username);
            options.setPassword(password.toCharArray());
            options.setSocketFactory(SSLContext.getDefault().getSocketFactory());

            client.connect(options);
            logger.info("MQTT client connected successfully");

            subscribeToTopic(defaultTopic);

            publishMessage(defaultTopic, "test!");
        } catch (Exception e) {
            logger.error("Failed to initialize MQTT client", e);
        }
    }

    public boolean waitForResponse(String correlationId, int timeoutMs) throws InterruptedException, MqttException {
        CountDownLatch latch = new CountDownLatch(1);

        client.subscribe(defaultTopic, (topic, msg) -> {
            String payload = new String(msg.getPayload());
            String[] parts = payload.split(":");
            if (parts.length == 2 && parts[0].equals(correlationId)) {
                serviceResponses.put(correlationId, "true".equalsIgnoreCase(parts[1]));
                latch.countDown();
            }
        });

        boolean isResponseReceived = latch.await(timeoutMs, TimeUnit.MILLISECONDS);
        return isResponseReceived && serviceResponses.getOrDefault(correlationId, false);
    }

    public void subscribeToTopic(String topic) throws MqttException {
        logger.info("Subscribing to topic: {}", topic);
        client.subscribe(topic, (t, msg) -> {
            String payload = new String(msg.getPayload());
            logger.info("Message received on topic {}: {}", t, payload);

            handlerManager.processMessage(t, payload);
        });
    }

    public void unsubscribeFromTopic(String topic) throws MqttException {
        logger.info("Unsubscribing from topic: {}", topic);
        client.unsubscribe(topic);
    }

    public void publishMessage(String topic, String message) throws MqttException {
        MqttMessage mqttMessage = new MqttMessage(message.getBytes());
        mqttMessage.setQos(1);
        logger.info("Publishing message to topic {}: {}", topic, message);
        client.publish(topic, mqttMessage);
    }

    public void registerHandler(String topic, MqttMessageHandler handler) throws MqttException {
        handlerManager.registerHandler(topic, handler);

        if (!client.isConnected()) {
            logger.warn("MQTT client is not connected, reconnecting...");
            client.reconnect();
        }

        subscribeToTopic(topic);
    }

    public void unregisterHandler(String topic, MqttMessageHandler handler) {
        handlerManager.unregisterHandler(topic, handler);
    }
}