package com.hannofleet.backend.controller;

import com.hannofleet.backend.entity.Vehicle;
import com.hannofleet.backend.entity.VehicleHistory;
import com.hannofleet.backend.repository.VehicleHistoryRepository;
import com.hannofleet.backend.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/vehicle-history")
@CrossOrigin(origins = "*")
public class VehicleHistoryController {

    @Autowired
    private VehicleHistoryRepository repository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @GetMapping
    public ResponseEntity<List<VehicleHistory>> getAllHistory() {
        return ResponseEntity.ok(repository.findAllByOrderByChangeDateDesc());
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<VehicleHistory>> getHistoryByVehicle(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(repository.findByVehicleIdOrderByChangeDateDesc(vehicleId));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<VehicleHistory>> getHistoryByType(@PathVariable String type) {
        return ResponseEntity.ok(repository.findByHistoryTypeOrderByChangeDateDesc(type));
    }

    @PostMapping
    public ResponseEntity<VehicleHistory> createHistory(@RequestBody Map<String, Object> payload) {
        String dateStr = payload.get("changeDate").toString();
        LocalDateTime changeDate;
        try {
            changeDate = LocalDateTime.parse(dateStr);
        } catch (Exception e) {
            changeDate = LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        VehicleHistory history = VehicleHistory.builder()
                .vehicleId(Long.parseLong(payload.get("vehicleId").toString()))
                .historyType(payload.get("historyType").toString())
                .oldValue(payload.get("oldValue") != null ? payload.get("oldValue").toString() : null)
                .newValue(payload.get("newValue") != null ? payload.get("newValue").toString() : null)
                .changeDate(changeDate)
                .note(payload.get("note") != null ? payload.get("note").toString() : null)
                .build();

        VehicleHistory saved = repository.save(history);

        if ("STATUS".equals(payload.get("historyType"))) {
            String newStatus = payload.get("newValue") != null ? payload.get("newValue").toString() : null;
            if (newStatus != null) {
                vehicleRepository.findById(Long.parseLong(payload.get("vehicleId").toString()))
                        .ifPresent(vehicle -> {
                            vehicle.setStatus(Vehicle.VehicleStatus.valueOf(newStatus));
                            vehicleRepository.save(vehicle);
                        });
            }
        }

        if ("DRIVER".equals(payload.get("historyType"))) {
            String newDriver = payload.get("newValue") != null ? payload.get("newValue").toString() : null;
            if (newDriver != null) {
                vehicleRepository.findById(Long.parseLong(payload.get("vehicleId").toString()))
                        .ifPresent(vehicle -> {
                            vehicle.setDriver(newDriver);
                            vehicleRepository.save(vehicle);
                        });
            }
        }

        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHistory(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
