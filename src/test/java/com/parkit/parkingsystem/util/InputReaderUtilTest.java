package com.parkit.parkingsystem.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class InputReaderUtilTest {

    private InputReaderUtil inputReaderUtil;
    private final InputStream systemIn = System.in;

    @BeforeEach
    void setUp() {
        inputReaderUtil = new InputReaderUtil();
    }

    @AfterEach
    void tearDown() {
        System.setIn(systemIn);
        InputReaderUtil.resetScanner();
    }

    private void provideInput(String data) {
        ByteArrayInputStream testIn = new ByteArrayInputStream(data.getBytes());
        System.setIn(testIn);
        InputReaderUtil.resetScanner();
    }

    @Test
    void readSelection_ShouldReturnValidNumber() {
        provideInput("1\n");
        assertEquals(1, inputReaderUtil.readSelection());
    }

    @Test
    void readSelection_ShouldReturnNegativeOne_WhenInvalidInput() {
        provideInput("invalid\n");
        assertEquals(-1, inputReaderUtil.readSelection());
    }

    @Test
    void readVehicleRegistrationNumber_ShouldReturnValidString() throws Exception {
        String testInput = "ABC123\n";
        provideInput(testInput);
        assertEquals("ABC123", inputReaderUtil.readVehicleRegistrationNumber());
    }

    @Test
    void readVehicleRegistrationNumber_ShouldThrowException_WhenEmptyInput() {
        provideInput("\n");
        assertThrows(IllegalArgumentException.class, () -> inputReaderUtil.readVehicleRegistrationNumber());
    }

    @Test
    void readVehicleRegistrationNumber_ShouldThrowException_WhenOnlySpaces() {
        provideInput("   \n");
        assertThrows(IllegalArgumentException.class, () -> inputReaderUtil.readVehicleRegistrationNumber());
    }
}
