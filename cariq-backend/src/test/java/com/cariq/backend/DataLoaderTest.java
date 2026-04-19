package com.cariq.backend;

import com.cariq.backend.model.Car;
import com.cariq.backend.repository.CarRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = "openai.api.key=test-key")
class DataLoaderTest {

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private DataLoader dataLoader;

    @Test
    void load_seeds25Cars() {
        assertThat(carRepository.count()).isEqualTo(25);
    }

    @Test
    void load_isIdempotent_doesNotDuplicateOnSecondCall() {
        dataLoader.load(); // second call — should be a no-op
        assertThat(carRepository.count()).isEqualTo(25);
    }

    @Test
    void load_containsExpectedMakes() {
        List<String> makes = carRepository.findAll().stream()
                .map(Car::getMake)
                .distinct()
                .toList();

        assertThat(makes).contains("Maruti", "Hyundai", "Tata", "Toyota", "Honda",
                "Kia", "Mahindra", "Skoda", "Volkswagen", "MG", "Renault", "Audi");
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

        // 489 km ARAI range stored as mileage
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

        // Swift is the cheapest (~6.65L), Audi Q7 is the most expensive (~89.9L)
        assertThat(minPrice).isLessThan(700000.0);
        assertThat(maxPrice).isGreaterThan(8000000.0);
    }

    @Test
    void load_sevenSeaterCarsExist() {
        long sevenSeaterCount = carRepository.findAll().stream()
                .filter(c -> c.getSeating() >= 7)
                .count();

        assertThat(sevenSeaterCount).isGreaterThanOrEqualTo(3); // Ertiga, Innova, Bolero, Fortuner, Scorpio, Q7
    }
}