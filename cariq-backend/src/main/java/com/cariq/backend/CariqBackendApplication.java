package com.cariq.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CariqBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CariqBackendApplication.class, args);
    }
}
