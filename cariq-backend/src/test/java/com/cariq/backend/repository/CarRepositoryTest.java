package com.cariq.backend.repository;

import com.cariq.backend.model.Car;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = "openai.api.key=test-key")
@Transactional
class CarRepositoryTest {

    @Autowired
    private CarRepository carRepository;

    @BeforeEach
    void setUp() {
        // Clear DataLoader seed data; @Transactional rolls this back after each test
        carRepository.deleteAll();
        carRepository.saveAll(List.of(
                car("Maruti",  "Swift",    665000.0,  "Petrol",   5),
                car("Hyundai", "Creta",   1850000.0,  "Petrol",   5),
                car("Tata",    "Nexon EV",1995000.0,  "Electric", 5),
                car("Maruti",  "Ertiga",  1099000.0,  "CNG",      7)
        ));
    }

    // ── findAll ────────────────────────────────────────────────────────────────

    @Test
    void findAll_returnsAllSavedCars() {
        assertThat(carRepository.findAll()).hasSize(4);
    }

    // ── save / findById ────────────────────────────────────────────────────────

    @Test
    void save_persistsCarAndAssignsId() {
        Car saved = carRepository.save(car("Honda", "City", 1660000.0, "Petrol", 5));

        assertThat(saved.getId()).isNotNull();
        Optional<Car> found = carRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getModel()).isEqualTo("City");
    }

    @Test
    void findById_unknownId_returnsEmpty() {
        assertThat(carRepository.findById(999L)).isEmpty();
    }

    // ── findByPriceLessThanEqual ───────────────────────────────────────────────

    @Test
    void findByPriceLessThanEqual_returnsOnlyCarsWithinBudget() {
        List<Car> result = carRepository.findByPriceLessThanEqual(1000000.0); // ≤ 10L
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getModel()).isEqualTo("Swift");
    }

    @Test
    void findByPriceLessThanEqual_exactBoundary_includesThatCar() {
        List<Car> result = carRepository.findByPriceLessThanEqual(665000.0);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getModel()).isEqualTo("Swift");
    }

    @Test
    void findByPriceLessThanEqual_highLimit_returnsAllCars() {
        List<Car> result = carRepository.findByPriceLessThanEqual(5000000.0);
        assertThat(result).hasSize(4);
    }

    @Test
    void findByPriceLessThanEqual_belowCheapestCar_returnsEmpty() {
        List<Car> result = carRepository.findByPriceLessThanEqual(100000.0);
        assertThat(result).isEmpty();
    }

    // ── findByFuelTypeAndPriceLessThanEqual ────────────────────────────────────

    @Test
    void findByFuelTypeAndPrice_petrolUnder10L_returnsSwiftOnly() {
        List<Car> result = carRepository.findByFuelTypeAndPriceLessThanEqual("Petrol", 1000000.0);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getModel()).isEqualTo("Swift");
    }

    @Test
    void findByFuelTypeAndPrice_petrolUnder20L_returnsSwiftAndCreta() {
        List<Car> result = carRepository.findByFuelTypeAndPriceLessThanEqual("Petrol", 2000000.0);
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Car::getModel).containsExactlyInAnyOrder("Swift", "Creta");
    }

    @Test
    void findByFuelTypeAndPrice_electricUnder20L_returnsNexonEv() {
        List<Car> result = carRepository.findByFuelTypeAndPriceLessThanEqual("Electric", 2000000.0);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getModel()).isEqualTo("Nexon EV");
    }

    @Test
    void findByFuelTypeAndPrice_dieselFuelType_returnsEmpty() {
        List<Car> result = carRepository.findByFuelTypeAndPriceLessThanEqual("Diesel", 5000000.0);
        assertThat(result).isEmpty();
    }

    @Test
    void findByFuelTypeAndPrice_priceTooLowForMatchingFuel_returnsEmpty() {
        List<Car> result = carRepository.findByFuelTypeAndPriceLessThanEqual("CNG", 500000.0);
        assertThat(result).isEmpty();
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private Car car(String make, String model, Double price, String fuelType, int seating) {
        Car c = new Car();
        c.setMake(make);
        c.setModel(model);
        c.setPrice(price);
        c.setFuelType(fuelType);
        c.setSeating(seating);
        return c;
    }
}