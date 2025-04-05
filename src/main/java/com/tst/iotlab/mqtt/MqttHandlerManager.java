package com.tst.iotlab.mqtt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

// Менеджер обработчиков сообщений
@Component
public class MqttHandlerManager {
    private static final Logger logger = LoggerFactory.getLogger(MqttHandlerManager.class);

    // Map: topic -> список обработчиков
    private final Map<String, Set<MqttMessageHandler>> topicHandlers = new ConcurrentHashMap<>();

    public void registerHandler(String topic, MqttMessageHandler handler) {
        topicHandlers.computeIfAbsent(topic, k -> new CopyOnWriteArraySet<>()).add(handler);
        logger.info("Registered handler {} for topic: {}", handler.getClass().getSimpleName(), topic);
    }

    public void unregisterHandler(String topic, MqttMessageHandler handler) {
        if (topicHandlers.containsKey(topic)) {
            topicHandlers.get(topic).remove(handler);
            logger.info("Unregistered handler {} from topic: {}", handler.getClass().getSimpleName(), topic);
        }
    }

    public void processMessage(String topic, String payload) {
        logger.debug("Processing message on topic: {}", topic);

        if (topicHandlers.containsKey(topic)) {
            for (MqttMessageHandler handler : topicHandlers.get(topic)) {
                try {
                    handler.handleMessage(topic, payload);
                } catch (Exception e) {
                    logger.error("Error in handler {} for topic {}: {}",
                            handler.getClass().getSimpleName(), topic, e.getMessage(), e);
                }
            }
        }

        // wildcard топики
        for (Map.Entry<String, Set<MqttMessageHandler>> entry : topicHandlers.entrySet()) {
            String registeredTopic = entry.getKey();
            if (isTopicMatch(registeredTopic, topic)) {
                for (MqttMessageHandler handler : entry.getValue()) {
                    try {
                        handler.handleMessage(topic, payload);
                    } catch (Exception e) {
                        logger.error("Error in wildcard handler {} for topic {}: {}",
                                handler.getClass().getSimpleName(), topic, e.getMessage(), e);
                    }
                }
            }
        }
    }

    private boolean isTopicMatch(String subscription, String actualTopic) {
        if (subscription.equals(actualTopic)) {
            return true;
        }

        String[] subscriptionLevels = subscription.split("/");
        String[] actualLevels = actualTopic.split("/");

        if (subscriptionLevels[subscriptionLevels.length - 1].equals("#")) {
            for (int i = 0; i < subscriptionLevels.length - 1; i++) {
                if (i >= actualLevels.length) {
                    return false;
                }

                if (!subscriptionLevels[i].equals("+") && !subscriptionLevels[i].equals(actualLevels[i])) {
                    return false;
                }
            }
            return true;
        }

        if (subscriptionLevels.length != actualLevels.length) {
            return false;
        }

        for (int i = 0; i < subscriptionLevels.length; i++) {
            if (!subscriptionLevels[i].equals("+") && !subscriptionLevels[i].equals(actualLevels[i])) {
                return false;
            }
        }

        return true;
    }
}
