package com.cariq.backend.car;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarRepository extends JpaRepository<Car, Long> {

    List<Car> findByPriceLessThanEqual(Double price);

    List<Car> findByFuelTypeAndPriceLessThanEqual(String fuelType, Double price);
}
