package com.hotel;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SmokeTest {
    
    @Test
    void basicSmokeTest() {
        assertTrue(true, "Basic smoke test should always pass");
    }
    
    @Test
    void environmentTest() {
        String expected = "test";
        assertEquals(expected, expected, "Environment should be consistent");
    }
}
