package com.cariq.backend.common;

import com.cariq.backend.car.Car;
import com.cariq.backend.car.CarRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = "openai.api-key=test-key")
class DataLoaderTest {

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private DataLoader dataLoader;

    @Test
    void load_seeds95Cars() {
        assertThat(carRepository.count()).isEqualTo(96);
    }

    @Test
    void load_isIdempotent_doesNotDuplicateOnSecondCall() {
        dataLoader.load();
        assertThat(carRepository.count()).isEqualTo(96);
    }

    @Test
    void load_containsExpectedMakes() {
        List<String> makes = carRepository.findAll().stream()
                .map(Car::getMake)
                .distinct()
                .toList();

        assertThat(makes).contains("Maruti", "Hyundai", "Tata", "Toyota", "Honda",
                "Kia", "Mahindra", "Skoda", "Volkswagen", "MG", "Renault", "Audi",
                "Mercedes-Benz", "BMW", "Volvo", "Land Rover", "Jeep", "Lexus",
                "Porsche", "BYD", "Genesis");
    }

    @Test
    void load_containsFuelTypeVariety() {
        List<String> fuelTypes = carRepository.findAll().stream()
                .map(Car::getFuelType)
                .distinct()
                .toList();

        assertThat(fuelTypes).containsExactlyInAnyOrder("Petrol", "Electric", "CNG", "Diesel");
    }

    @Test
    void load_nexonEvHasKmRangeMileage() {
        Car nexonEv = carRepository.findAll().stream()
                .filter(c -> "Nexon EV".equals(c.getModel()))
                .findFirst()
                .orElseThrow();

        assertThat(nexonEv.getMileage()).isGreaterThan(400.0);
        assertThat(nexonEv.getFuelType()).isEqualTo("Electric");
    }

    @Test
    void load_allCarsHaveRequiredFields() {
        carRepository.findAll().forEach(car -> {
            assertThat(car.getMake()).isNotBlank();
            assertThat(car.getModel()).isNotBlank();
            assertThat(car.getVariant()).isNotBlank();
            assertThat(car.getPrice()).isPositive();
            assertThat(car.getFuelType()).isNotBlank();
            assertThat(car.getTransmission()).isNotBlank();
            assertThat(car.getMileage()).isPositive();
            assertThat(car.getSeating()).isGreaterThan(0);
            assertThat(car.getPros()).isNotBlank();
            assertThat(car.getCons()).isNotBlank();
        });
    }

    @Test
    void load_priceRangeIsReasonable() {
        List<Car> cars = carRepository.findAll();

        double minPrice = cars.stream().mapToDouble(Car::getPrice).min().orElseThrow();
        double maxPrice = cars.stream().mapToDouble(Car::getPrice).max().orElseThrow();

        assertThat(minPrice).isLessThan(500000.0);
        assertThat(maxPrice).isGreaterThan(9000000.0);
    }

    @Test
    void load_sevenSeaterCarsExist() {
        long sevenSeaterCount = carRepository.findAll().stream()
                .filter(c -> c.getSeating() >= 7)
                .count();

        assertThat(sevenSeaterCount).isGreaterThanOrEqualTo(12);
    }
}
