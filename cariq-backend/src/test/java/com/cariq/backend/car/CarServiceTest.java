package com.cariq.backend.car;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private CarService carService;

    private Car swift;    // 6.65L  Petrol   5 seats
    private Car creta;    // 18.5L  Petrol   5 seats
    private Car nexonEv;  // 19.95L Electric 5 seats
    private Car ertiga;   // 10.99L CNG      7 seats

    @BeforeEach
    void setUp() {
        swift   = car("Maruti",  "Swift",     665000.0,  "Petrol",   5);
        creta   = car("Hyundai", "Creta",    1850000.0,  "Petrol",   5);
        nexonEv = car("Tata",    "Nexon EV", 1995000.0,  "Electric", 5);
        ertiga  = car("Maruti",  "Ertiga",   1099000.0,  "CNG",      7);
        when(carRepository.findAll()).thenReturn(List.of(swift, creta, nexonEv, ertiga));
    }

    @Test
    void filter_noParams_returnsAll() {
        assertThat(carService.filter(null, null, null)).hasSize(4);
    }

    @Test
    void filter_maxPrice10Lakhs_returnsOnlySwift() {
        List<Car> result = carService.filter(10.0, null, null);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getModel()).isEqualTo("Swift");
    }

    @Test
    void filter_maxPrice5Lakhs_returnsEmpty() {
        assertThat(carService.filter(5.0, null, null)).isEmpty();
    }

    @Test
    void filter_fuelTypeElectric_returnsNexonEv() {
        List<Car> result = carService.filter(null, "Electric", null);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getModel()).isEqualTo("Nexon EV");
    }

    @Test
    void filter_fuelType_isCaseInsensitive() {
        List<Car> result = carService.filter(null, "electric", null);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getModel()).isEqualTo("Nexon EV");
    }

    @Test
    void filter_seating7_returnsOnlyErtiga() {
        List<Car> result = carService.filter(null, null, 7);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getModel()).isEqualTo("Ertiga");
    }

    @Test
    void filter_seating5_returnsAllFour() {
        assertThat(carService.filter(null, null, 5)).hasSize(4);
    }

    @Test
    void filter_maxPriceAndFuelType_appliesBothConstraints() {
        List<Car> result = carService.filter(20.0, "Petrol", null);
        assertThat(result).hasSize(2)
                .extracting(Car::getFuelType)
                .containsOnly("Petrol");
    }

    @Test
    void filter_allThreeParams_returnsOnlySwift() {
        List<Car> result = carService.filter(7.0, "Petrol", 5);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getModel()).isEqualTo("Swift");
    }

    @Test
    void findByBudgetRange_returnsOnlyCarsInRange() {
        List<Car> result = carService.findByBudgetRange(600_000, 700_000);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getModel()).isEqualTo("Swift");
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
