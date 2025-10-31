package com.hotel;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class SmokeTest {

    @Test
    void smokeTest1_ApplicationStarts() {
        // Basic smoke test to verify the application can start
        assertThat(true).isTrue();
    }

    @Test
    void smokeTest2_BasicAssertion() {
        // Another basic smoke test
        String expected = "test";
        String actual = "test";
        assertThat(actual).isEqualTo(expected);
    }
}
