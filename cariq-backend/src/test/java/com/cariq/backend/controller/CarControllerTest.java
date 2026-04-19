package com.cariq.backend.controller;

import com.cariq.backend.model.Car;
import com.cariq.backend.repository.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CarControllerTest {

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private CarController carController;

    private MockMvc mockMvc;

    private Car swift;    // 6.65L  Petrol   5 seats
    private Car creta;    // 18.5L  Petrol   5 seats
    private Car nexonEv;  // 19.95L Electric 5 seats
    private Car ertiga;   // 10.99L CNG      7 seats

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(carController).build();

        swift   = car(1L, "Maruti",  "Swift",     665000.0, "Petrol",   "Manual",    5);
        creta   = car(2L, "Hyundai", "Creta",    1850000.0, "Petrol",   "Automatic", 5);
        nexonEv = car(3L, "Tata",    "Nexon EV", 1995000.0, "Electric", "Automatic", 5);
        ertiga  = car(4L, "Maruti",  "Ertiga",   1099000.0, "CNG",      "Manual",    7);
    }

    // ── GET /api/cars ──────────────────────────────────────────────────────────

    @Test
    void getAllCars_returnsAllCars() throws Exception {
        when(carRepository.findAll()).thenReturn(List.of(swift, creta, nexonEv, ertiga));

        mockMvc.perform(get("/api/cars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].make", is("Maruti")))
                .andExpect(jsonPath("$[1].make", is("Hyundai")));
    }

    @Test
    void getAllCars_emptyRepository_returnsEmptyList() throws Exception {
        when(carRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/cars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ── GET /api/cars/filter ───────────────────────────────────────────────────

    @Test
    void filterCars_noParams_returnsAll() throws Exception {
        when(carRepository.findAll()).thenReturn(List.of(swift, creta, nexonEv, ertiga));

        mockMvc.perform(get("/api/cars/filter"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    void filterCars_maxPrice10Lakhs_returnsOnlySwift() throws Exception {
        when(carRepository.findAll()).thenReturn(List.of(swift, creta, nexonEv, ertiga));

        mockMvc.perform(get("/api/cars/filter").param("maxPrice", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].model", is("Swift")));
    }

    @Test
    void filterCars_maxPrice_noMatch_returnsEmptyList() throws Exception {
        when(carRepository.findAll()).thenReturn(List.of(swift, creta, nexonEv, ertiga));

        mockMvc.perform(get("/api/cars/filter").param("maxPrice", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void filterCars_fuelTypeElectric_returnsNexonEv() throws Exception {
        when(carRepository.findAll()).thenReturn(List.of(swift, creta, nexonEv, ertiga));

        mockMvc.perform(get("/api/cars/filter").param("fuelType", "Electric"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].model", is("Nexon EV")));
    }

    @Test
    void filterCars_fuelType_isCaseInsensitive() throws Exception {
        when(carRepository.findAll()).thenReturn(List.of(swift, creta, nexonEv, ertiga));

        mockMvc.perform(get("/api/cars/filter").param("fuelType", "electric"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].model", is("Nexon EV")));
    }

    @Test
    void filterCars_seating7_returnsOnlyErtiga() throws Exception {
        when(carRepository.findAll()).thenReturn(List.of(swift, creta, nexonEv, ertiga));

        mockMvc.perform(get("/api/cars/filter").param("seating", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].model", is("Ertiga")));
    }

    @Test
    void filterCars_seating5_returnsAllFourCars() throws Exception {
        when(carRepository.findAll()).thenReturn(List.of(swift, creta, nexonEv, ertiga));

        mockMvc.perform(get("/api/cars/filter").param("seating", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    void filterCars_maxPriceAndFuelType_appliesBothConstraints() throws Exception {
        when(carRepository.findAll()).thenReturn(List.of(swift, creta, nexonEv, ertiga));

        mockMvc.perform(get("/api/cars/filter")
                        .param("maxPrice", "20")
                        .param("fuelType", "Petrol"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].fuelType", everyItem(is("Petrol"))));
    }

    @Test
    void filterCars_allThreeParams_returnsOnlyMatching() throws Exception {
        when(carRepository.findAll()).thenReturn(List.of(swift, creta, nexonEv, ertiga));

        mockMvc.perform(get("/api/cars/filter")
                        .param("maxPrice", "7")
                        .param("fuelType", "Petrol")
                        .param("seating", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].model", is("Swift")));
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private Car car(Long id, String make, String model, Double price,
                    String fuelType, String transmission, int seating) {
        Car c = new Car();
        c.setId(id);
        c.setMake(make);
        c.setModel(model);
        c.setPrice(price);
        c.setFuelType(fuelType);
        c.setTransmission(transmission);
        c.setSeating(seating);
        return c;
    }
}