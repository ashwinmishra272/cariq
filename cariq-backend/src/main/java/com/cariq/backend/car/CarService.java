package com.cariq.backend.car;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;

    @Transactional(readOnly = true)
    public List<Car> findAll() {
        return carRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Car> filter(Double maxPriceLakh, String fuelType, Integer minSeating) {
        Stream<Car> stream = carRepository.findAll().stream();

        if (maxPriceLakh != null) {
            stream = stream.filter(car -> car.getPrice() / 100_000.0 <= maxPriceLakh);
        }
        if (fuelType != null) {
            stream = stream.filter(car -> car.getFuelType().equalsIgnoreCase(fuelType));
        }
        if (minSeating != null) {
            stream = stream.filter(car -> car.getSeating() >= minSeating);
        }

        return stream.toList();
    }

    @Transactional(readOnly = true)
    public List<Car> findByBudgetRange(double minPriceInr, double maxPriceInr) {
        return carRepository.findAll().stream()
                .filter(car -> car.getPrice() >= minPriceInr && car.getPrice() <= maxPriceInr)
                .toList();
    }
}
