package com.cariq.backend.car;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    @GetMapping
    public List<Car> getAllCars() {
        return carService.findAll();
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Car>> filterCars(
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String fuelType,
            @RequestParam(required = false) Integer seating) {
        return ResponseEntity.ok(carService.filter(maxPrice, fuelType, seating));
    }
}
