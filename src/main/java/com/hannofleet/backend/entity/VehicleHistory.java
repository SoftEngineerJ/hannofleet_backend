package com.hannofleet.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "history_type", nullable = false, length = 20)
    private String historyType; // "STATUS" oder "DRIVER"

    @Column(name = "old_value", length = 100)
    private String oldValue;

    @Column(name = "new_value", length = 100)
    private String newValue;

    @Column(name = "change_date", nullable = false)
    private LocalDateTime changeDate;

    @Column(length = 100)
    private String note;
}
