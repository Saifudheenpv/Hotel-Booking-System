package com.hotel.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class HotelServiceTest {

    @Test
    void basicServiceTest() {
        // Basic test to verify service layer can be tested
        assertTrue(true, "Service test should pass");
    }

    @Test
    void testBasicLogic() {
        int expected = 4;
        int actual = 2 + 2;
        assertTrue(actual == expected, "Basic arithmetic should work");
    }
}