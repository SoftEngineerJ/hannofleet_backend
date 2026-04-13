package com.hannofleet.backend.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "vehicles", schema = "hannofleet")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "license_plate", nullable = false, unique = true, length = 20)
    private String licensePlate;

    @Column(length = 50)
    private String model;

    @Column
    private Integer mileage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleStatus status;

    @Column(name = "tour_number", length = 20)
    private String tourNumber;

    @Column(length = 100)
    private String driver;

    @JsonDeserialize(using = CustomLocalDateDeserializer.class)
    @Column(name = "next_inspection")
    private LocalDate nextInspection;

    @JsonDeserialize(using = CustomLocalDateDeserializer.class)
    @Column(name = "next_workshop_appointment")
    private LocalDate nextWorkshopAppointment;

    @JsonDeserialize(using = CustomLocalDateDeserializer.class)
    @Column(name = "next_insurance")
    private LocalDate nextInsurance;

    public enum VehicleStatus {
        FREI,
        AKTIV,
        WERKSTATT,
        UNFALL,
        ABGEMELDET
    }
}
