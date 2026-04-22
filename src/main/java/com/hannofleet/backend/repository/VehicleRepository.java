package com.hannofleet.backend.repository;

import com.hannofleet.backend.entity.Vehicle;
import com.hannofleet.backend.entity.Vehicle.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByLicensePlate(String licensePlate);

    List<Vehicle> findByStatus(VehicleStatus status);

    List<Vehicle> findByDriverContainingIgnoreCase(String driver);

    List<Vehicle> findByModelContainingIgnoreCase(String model);

    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM Vehicle v WHERE LOWER(v.licensePlate) = LOWER(:licensePlate)")
    boolean existsByLicensePlateIgnoreCase(@Param("licensePlate") String licensePlate);
}
