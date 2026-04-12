package com.hannofleet.backend.repository;

import com.hannofleet.backend.entity.VehicleHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VehicleHistoryRepository extends JpaRepository<VehicleHistory, Long> {
    List<VehicleHistory> findByVehicleIdOrderByChangeDateDesc(Long vehicleId);
    List<VehicleHistory> findByHistoryTypeOrderByChangeDateDesc(String historyType);
    List<VehicleHistory> findAllByOrderByChangeDateDesc();
}
