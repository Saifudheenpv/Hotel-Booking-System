package com.hotel;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class HotelBookingApplicationTests {

    @Test
    void contextLoads() {
        // This test verifies that the Spring application context loads successfully
        assertTrue(true, "Spring context should load successfully");
    }

    @Test
    void mainMethodStartsApplication() {
        // Test that main method can be invoked without errors
        HotelBookingApplication.main(new String[] {});
        assertTrue(true, "Main method should execute without errors");
    }
}