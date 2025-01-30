package com.parkit.parkingsystem.model;

import com.parkit.parkingsystem.constants.ParkingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParkingSpotTest {

    private ParkingSpot parkingSpot;

    @BeforeEach
    void setUp() {
        parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
    }

    @Test
    void setId_ShouldUpdateNumber() {
        parkingSpot.setId(2);
        assertEquals(2, parkingSpot.getId());
    }

    @Test
    void setParkingType_ShouldUpdateParkingType() {
        parkingSpot.setParkingType(ParkingType.BIKE);
        assertEquals(ParkingType.BIKE, parkingSpot.getParkingType());
    }

    @Test
    void equals_ShouldReturnTrue_WhenSameObject() {
        assertTrue(parkingSpot.equals(parkingSpot));
    }

    @Test
    void equals_ShouldReturnTrue_WhenSameNumber() {
        ParkingSpot otherSpot = new ParkingSpot(1, ParkingType.BIKE, false);
        assertTrue(parkingSpot.equals(otherSpot));
    }

    @Test
    void equals_ShouldReturnFalse_WhenNull() {
        assertNotEquals(null, parkingSpot);
    }

    @Test
    void equals_ShouldReturnFalse_WhenDifferentClass() {
        assertNotEquals("Not a ParkingSpot", parkingSpot);
    }

    @Test
    void equals_ShouldReturnFalse_WhenDifferentNumber() {
        ParkingSpot otherSpot = new ParkingSpot(2, ParkingType.CAR, true);
        assertNotEquals(parkingSpot, otherSpot);
    }

    @Test
    void hashCode_ShouldReturnNumber() {
        assertEquals(parkingSpot.getId(), parkingSpot.hashCode());
    }

    @Test
    void hashCode_ShouldBeConsistentWithEquals() {
        ParkingSpot spot1 = new ParkingSpot(1, ParkingType.CAR, true);
        ParkingSpot spot2 = new ParkingSpot(1, ParkingType.BIKE, false);
        assertEquals(spot1.hashCode(), spot2.hashCode());
    }
}
