package com.tst.iotlab.data;

import com.tst.iotlab.restControllers.DataChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class WebDataContainer {
    private static final Logger logger = LoggerFactory.getLogger(WebDataContainer.class);
    private final List<DataChangeListener> listeners = new CopyOnWriteArrayList<>();
    private volatile boolean dataChanged = false;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void registerListener(DataChangeListener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(DataChangeListener listener) {
        listeners.remove(listener);
    }
    private final Map<String, String> fieldValues = new ConcurrentHashMap<>();
    public void updateFieldValue(String type, String value, boolean notifyListeners) {
        String normalizedType = normalizeFiledType(type);
        String oldValue = getFieldValue(normalizedType);

        // Проверяем, действительно ли значение изменилось
        if (!value.equals(oldValue)) {
            fieldValues.put(normalizedType, value);
            dataChanged = true; // Устанавливаем флаг изменения

            if (notifyListeners) {
                listeners.forEach(DataChangeListener::onDataChanged);
            }
            logger.debug("Updated sensor data: {} = {}", normalizedType, value);
        }
    }

    public boolean hasDataChanged() {
        return dataChanged;
    }

    public void resetDataChangedFlag() {
        dataChanged = false;
    }

    public String getFieldValue(String sensorType) {
        String normalizedType = normalizeFiledType(sensorType);

        return fieldValues.get(normalizedType);
    }

    public Map<String, String> getAllValues() {

        return new ConcurrentHashMap<>(fieldValues);
    }

    private String normalizeFiledType(String sensorType) {
        String type = sensorType.toLowerCase();
        return switch (type) {
            case "temperature", "temp" -> "temp";
            case "humidity", "humid", "hum" -> "humid";
            case "gas", "co2", "air" -> "gas";
            default -> type;
        };
    }
}
