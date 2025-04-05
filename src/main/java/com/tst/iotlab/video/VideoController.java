package com.tst.iotlab.video;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate; // Для вызова Python API

@RestController
@RequestMapping("/video")
public class VideoController {

    // Сервис для отправки обработанных кадров клиентам
    @Autowired
    private VideoStreamService videoStreamService;

    // URL твоего Python API
    private static final String pythonApiUrl = "http://http://192.168.0.183:5000/"; // Пример для Flask/FastAPI

    private final RestTemplate restTemplate = new RestTemplate();

    // Класс для приема JSON с кадром
    public static class FrameData {
        public String frame; // base64 encoded frame
    }

    // Класс для ответа от Python API
    public static class ProcessedFrameData {
        public String processedFrame; // base64 encoded processed frame
    }

    @PostMapping("/upload")
    public ResponseEntity<Void> receiveFrame(@RequestBody FrameData data) {
        try {
            // 1. Отправляем кадр в Python сервис
            ResponseEntity<ProcessedFrameData> response = restTemplate.postForEntity(
                    pythonApiUrl,
                    data, // Отправляем полученный JSON
                    ProcessedFrameData.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String processedFrameBase64 = response.getBody().processedFrame;
                // 2. Отправляем обработанный кадр подписчикам (WebSockets или MJPEG)
                videoStreamService.broadcastFrame(processedFrameBase64);
                return ResponseEntity.ok().build();
            } else {
                System.err.println("Ошибка от Python сервиса: " + response.getStatusCode());
                return ResponseEntity.status(response.getStatusCode()).build();
            }

        } catch (Exception e) {
            System.err.println("Ошибка при обработке кадра: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
