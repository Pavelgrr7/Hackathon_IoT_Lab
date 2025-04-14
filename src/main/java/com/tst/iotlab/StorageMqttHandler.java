//package com.tst.iotlab;
//
//import com.tst.iotlab.mqtt.AbstractMqttMessageHandler;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//
//@Component
//public class StorageMqttHandler extends AbstractMqttMessageHandler {
//    private static final Logger logger = LoggerFactory.getLogger(StorageMqttHandler.class);
//
//    @Override
//    protected void processMessage(String topic, String payload) {
//        logger.info("Saving data to storage: {} from topic: {}", payload, topic);
//        // Логика сохранения данных
//        // Например:
//        // repository.save(new DataEntity(topic, payload));
//    }
//}
