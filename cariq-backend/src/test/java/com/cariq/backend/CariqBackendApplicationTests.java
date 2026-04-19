package com.cariq.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "openai.api.key=test-key")
class CariqBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
