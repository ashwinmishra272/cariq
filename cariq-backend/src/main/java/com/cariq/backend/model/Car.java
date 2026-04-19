package com.cariq.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String make;
    private String model;
    private String variant;
    private Double price;
    private String fuelType;
    private String transmission;
    private Double mileage;
    private Integer seating;
    private Integer safetyRating;
    private String useCases; // e.g. "city", "family", "highway"
    private String pros;
    private String cons;
}