package com.hotel;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class HotelBookingApplicationTest {

    @Test
    void contextLoads() {
        assertTrue(true, "Context should load successfully");
    }

    @Test
    void mainMethodStartsApplication() {
        HotelBookingApplication.main(new String[]{});
        assertTrue(true, "Main method should execute without errors");
    }
}
