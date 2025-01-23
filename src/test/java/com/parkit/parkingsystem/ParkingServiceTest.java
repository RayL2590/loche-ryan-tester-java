package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for the ParkingService class.
 * Uses Mockito for mocking dependencies (InputReaderUtil, ParkingSpotDAO, TicketDAO).
 * Tests cover the main parking operations:
 * - Vehicle entry processing
 * - Vehicle exit processing
 * - Parking spot allocation
 * - Error handling scenarios
 */
@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private ParkingService parkingService;

    @Mock
    private InputReaderUtil inputReaderUtil;
    @Mock
    private ParkingSpotDAO parkingSpotDAO;
    @Mock
    private TicketDAO ticketDAO;

    /**
     * Sets up a fresh ParkingService instance before each test.
     * Uses mocked dependencies injected by Mockito.
     */
    @BeforeEach
    private void setUpPerTest() {
        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    }

    /**
     * Tests the complete flow of a vehicle exiting the parking lot.
     * Verifies:
     * - Correct ticket retrieval and update
     * - Parking spot status update
     * - Recurring user discount application
     */
    @Test
    public void processExitingVehicleTest() throws Exception {
        // Setup test data
        String regNumber = "ABCDEF";
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000))); // 1 hour parking time
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber(regNumber);

        // Configure mock behaviors
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(regNumber);
        when(ticketDAO.getTicket(regNumber)).thenReturn(ticket);
        when(ticketDAO.getNbTicket(regNumber)).thenReturn(2); // Recurring user
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

        // Execute the test
        parkingService.processExitingVehicle();

        // Verify all expected interactions
        verify(ticketDAO).getTicket(regNumber);
        verify(ticketDAO).getNbTicket(regNumber);
        verify(ticketDAO).updateTicket(any(Ticket.class));
        verify(parkingSpotDAO).updateParking(any(ParkingSpot.class));
    }

    /**
     * Tests the complete flow of a vehicle entering the parking lot.
     * Verifies:
     * - Vehicle type selection
     * - Registration number reading
     * - Parking spot allocation
     * - Ticket generation
     */
    @Test
    public void testProcessIncomingVehicle() throws Exception {
        // Setup test data
        String regNumber = "ABCDEF";
        
        // Configure mock behaviors
        when(inputReaderUtil.readSelection()).thenReturn(1); // CAR selection
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(regNumber);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        when(ticketDAO.getNbTicket(regNumber)).thenReturn(2); // Recurring user

        // Execute the test
        parkingService.processIncomingVehicle();

        // Verify all expected interactions
        verify(inputReaderUtil).readSelection();
        verify(inputReaderUtil).readVehicleRegistrationNumber();
        verify(parkingSpotDAO).getNextAvailableSlot(ParkingType.CAR);
        verify(parkingSpotDAO).updateParking(any(ParkingSpot.class));
        verify(ticketDAO).saveTicket(any(Ticket.class));
    }

    /**
     * Tests error handling when ticket update fails during vehicle exit.
     * Verifies that parking spot status remains unchanged if ticket update fails.
     */
    @Test
    public void processExitingVehicleTestUnableUpdate() throws Exception {
        // Setup test data
        String regNumber = "ABCDEF";
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber(regNumber);

        // Configure mock behaviors
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(regNumber);
        when(ticketDAO.getTicket(regNumber)).thenReturn(ticket);
        when(ticketDAO.getNbTicket(regNumber)).thenReturn(1);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

        // Execute the test
        parkingService.processExitingVehicle();

        // Verify expected behavior on failure
        verify(ticketDAO).updateTicket(any(Ticket.class));
        verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));
    }

    /**
     * Tests successful parking spot allocation for a valid vehicle type.
     * Verifies correct spot number and type assignment.
     */
    @Test
    public void testGetNextParkingNumberIfAvailable() {
        // Configure mock behaviors
        when(inputReaderUtil.readSelection()).thenReturn(1); // CAR selection
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);

        // Execute the test
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        // Verify parking spot properties
        assertNotNull(parkingSpot);
        assertEquals(1, parkingSpot.getId());
        assertEquals(ParkingType.CAR, parkingSpot.getParkingType());
        assertTrue(parkingSpot.isAvailable());
    }

    /**
     * Tests error handling when no parking spot is available.
     * Verifies null return when no spot can be allocated.
     */
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
        // Configure mock behaviors
        when(inputReaderUtil.readSelection()).thenReturn(1); // CAR selection
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(-1);

        // Execute and verify
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        assertNull(parkingSpot);
    }

    /**
     * Tests error handling for invalid vehicle type selection.
     * Verifies that no parking spot is allocated for invalid vehicle types.
     */
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
        // Configure mock for invalid vehicle type
        when(inputReaderUtil.readSelection()).thenReturn(3); // Invalid type

        // Execute and verify
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        assertNull(parkingSpot);
        verify(parkingSpotDAO, never()).getNextAvailableSlot(any(ParkingType.class));
    }
}