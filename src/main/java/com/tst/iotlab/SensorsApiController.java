package com.tst.iotlab;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sensors")
public class SensorsApiController {
    private final SensorsController sensorsController;

    @Autowired
    public SensorsApiController(SensorsController sensorsController) {
        this.sensorsController = sensorsController;
    }

    @PostMapping("/status")
    public ResponseEntity<?> setSensorStatus(@RequestBody Map<String, Object> payload) {
        String sensorName = (String) payload.get("sensor");
        boolean status = Boolean.parseBoolean(payload.get("status").toString());
        sensorsController.setSensorStatus(sensorName, status);
        return ResponseEntity.ok("Sensor status updated");
    }

    @GetMapping("/{sensorName}")
    public ResponseEntity<?> getSensorStatus(@PathVariable String sensorName) {
        String status = sensorsController.getSensorStatus(sensorName);
        return ResponseEntity.ok(Map.of("sensor", sensorName, "status", status));
    }
}
