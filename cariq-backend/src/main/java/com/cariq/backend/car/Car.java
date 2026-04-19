package com.cariq.backend.car;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
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
    private Double userRating;
    private String useCases;
    private String pros;
    private String cons;
}
