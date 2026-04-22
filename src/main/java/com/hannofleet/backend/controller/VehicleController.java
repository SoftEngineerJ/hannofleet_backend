package com.hannofleet.backend.controller;

import com.hannofleet.backend.entity.Vehicle;
import com.hannofleet.backend.entity.Vehicle.VehicleStatus;
import com.hannofleet.backend.entity.VehicleHistory;
import com.hannofleet.backend.repository.VehicleRepository;
import com.hannofleet.backend.repository.VehicleHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VehicleController {

    private final VehicleRepository vehicleRepository;
    private final VehicleHistoryRepository vehicleHistoryRepository;

    @GetMapping
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        return ResponseEntity.ok(vehicleRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable Long id) {
        return vehicleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Vehicle>> getVehiclesByStatus(@PathVariable VehicleStatus status) {
        return ResponseEntity.ok(vehicleRepository.findByStatus(status));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Vehicle>> searchVehicles(
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String driver) {

        if (model != null && !model.isBlank()) {
            return ResponseEntity.ok(vehicleRepository.findByModelContainingIgnoreCase(model));
        }
        if (driver != null && !driver.isBlank()) {
            return ResponseEntity.ok(vehicleRepository.findByDriverContainingIgnoreCase(driver));
        }
        return ResponseEntity.ok(vehicleRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<?> createVehicle(@RequestBody Vehicle vehicle) {
        if (vehicle.getLicensePlate() == null || vehicle.getLicensePlate().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Kennzeichen ist erforderlich"));
        }
        if (vehicle.getStatus() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Status ist erforderlich"));
        }

        vehicle.setLicensePlate(vehicle.getLicensePlate().trim().replaceAll("\\s+", " "));

        try {
            Vehicle saved = vehicleRepository.save(vehicle);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", "Fahrzeug existiert bereits"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vehicle> updateVehicle(@PathVariable Long id, @RequestBody Vehicle vehicle) {
        return vehicleRepository.findById(id)
                .map(existing -> {
                    // Driver-Änderung in History speichern
                    String oldDriver = existing.getDriver();
                    String newDriver = vehicle.getDriver();
                    if (newDriver != null && !newDriver.equals(oldDriver)) {
                        // Vorherigen aktuellen DRIVER-Eintrag schließen
                        List<VehicleHistory> driverHistories = vehicleHistoryRepository
                                .findByVehicleIdOrderByChangeDateDesc(id);
                        for (VehicleHistory h : driverHistories) {
                            if ("DRIVER".equals(h.getHistoryType()) && "aktuell".equals(h.getNote())) {
                                h.setNote("bis " + java.time.LocalDate.now().minusDays(1));
                                vehicleHistoryRepository.save(h);
                                break;
                            }
                        }

                        VehicleHistory history = VehicleHistory.builder()
                                .vehicleId(id)
                                .historyType("DRIVER")
                                .oldValue(oldDriver != null ? oldDriver : "")
                                .newValue(newDriver)
                                .changeDate(java.time.LocalDateTime.now())
                                .note("aktuell")
                                .build();
                        vehicleHistoryRepository.save(history);
                    }

                    // Felder nur aktualisieren wenn sie im Request enthalten sind
                    if (vehicle.getLicensePlate() != null)
                        existing.setLicensePlate(vehicle.getLicensePlate());
                    if (vehicle.getModel() != null)
                        existing.setModel(vehicle.getModel());
                    if (vehicle.getMileage() != null)
                        existing.setMileage(vehicle.getMileage());
                    if (vehicle.getStatus() != null)
                        existing.setStatus(vehicle.getStatus());
                    if (vehicle.getTourNumber() != null)
                        existing.setTourNumber(vehicle.getTourNumber().isEmpty() ? null : vehicle.getTourNumber());
                    if (vehicle.getDriver() != null)
                        existing.setDriver(vehicle.getDriver().isEmpty() ? null : vehicle.getDriver());
                    if (vehicle.getNextInspection() != null)
                        existing.setNextInspection(vehicle.getNextInspection());
                    if (vehicle.getNextWorkshopAppointment() != null)
                        existing.setNextWorkshopAppointment(vehicle.getNextWorkshopAppointment());
                    if (vehicle.getLastInspection() != null)
                        existing.setLastInspection(vehicle.getLastInspection());
                    if (vehicle.getLastTuev() != null)
                        existing.setLastTuev(vehicle.getLastTuev());

                    return ResponseEntity.ok(vehicleRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Vehicle> updateStatus(@PathVariable Long id, @RequestParam VehicleStatus status,
            @RequestParam(required = false) String tourNumber) {
        return vehicleRepository.findById(id)
                .map(vehicle -> {
                    String oldStatus = vehicle.getStatus().name();

                    // Tour-Nummer immer aktualisieren wenn vorhanden
                    if (tourNumber != null && !tourNumber.isEmpty()) {
                        vehicle.setTourNumber(tourNumber);
                    }

                    // Nur Status ändern wenn er sich unterscheidet
                    if (oldStatus.equals(status.name())) {
                        return ResponseEntity.ok(vehicleRepository.save(vehicle));
                    }

                    vehicle.setStatus(status);
                    Vehicle savedVehicle = vehicleRepository.save(vehicle);

                    // Vorherigen aktuellen STATUS-Eintrag schließen
                    List<VehicleHistory> statusHistories = vehicleHistoryRepository
                            .findByVehicleIdOrderByChangeDateDesc(id);
                    for (VehicleHistory h : statusHistories) {
                        if ("STATUS".equals(h.getHistoryType()) && "aktuell".equals(h.getNote())) {
                            h.setNote("bis " + java.time.LocalDate.now().minusDays(1));
                            vehicleHistoryRepository.save(h);
                            break;
                        }
                    }

                    VehicleHistory history = VehicleHistory.builder()
                            .vehicleId(id)
                            .historyType("STATUS")
                            .oldValue(oldStatus)
                            .newValue(status.name())
                            .changeDate(java.time.LocalDateTime.now())
                            .note("aktuell")
                            .build();
                    vehicleHistoryRepository.save(history);

                    return ResponseEntity.ok(savedVehicle);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        if (vehicleRepository.existsById(id)) {
            vehicleRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/batch")
    public ResponseEntity<?> createVehiclesBatch(@RequestBody List<Vehicle> vehicles) {
        for (Vehicle v : vehicles) {
            if (v.getLicensePlate() != null) {
                v.setLicensePlate(v.getLicensePlate().trim().replaceAll("\\s+", " "));
            }
        }
        try {
            List<Vehicle> saved = vehicleRepository.saveAll(vehicles);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ein oder mehrere Fahrzeuge existieren bereits"));
        }
    }
}
