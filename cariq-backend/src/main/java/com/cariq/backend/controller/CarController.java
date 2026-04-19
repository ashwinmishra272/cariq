package com.cariq.backend.controller;

import com.cariq.backend.model.Car;
import com.cariq.backend.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/cars")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CarController {

    private final CarRepository carRepository;

    @GetMapping
    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Car>> filterCars(
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String fuelType,
            @RequestParam(required = false) Integer seating) {

        Stream<Car> stream = carRepository.findAll().stream();

        if (maxPrice != null) {
            stream = stream.filter(car -> car.getPrice() / 100000.0 <= maxPrice);
        }
        if (fuelType != null) {
            stream = stream.filter(car -> car.getFuelType().equalsIgnoreCase(fuelType));
        }
        if (seating != null) {
            stream = stream.filter(car -> car.getSeating() >= seating);
        }

        return ResponseEntity.ok(stream.toList());
    }
}