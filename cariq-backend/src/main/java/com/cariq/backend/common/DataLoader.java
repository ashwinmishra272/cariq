package com.cariq.backend.common;

import com.cariq.backend.car.Car;
import com.cariq.backend.car.CarRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader {

    private final CarRepository carRepository;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void load() {
        if (carRepository.count() > 0) return;

        try {
            InputStream is = new ClassPathResource("cars.json").getInputStream();
            List<Car> cars = objectMapper.readValue(is, new TypeReference<>() {});
            carRepository.saveAll(cars);
            log.info("Loaded {} cars from cars.json", cars.size());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load cars from cars.json", e);
        }
    }
}
