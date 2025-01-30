package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
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

    @Mock(lenient = true)
    private InputReaderUtil inputReaderUtil;
    @Mock(lenient = true)
    private ParkingSpotDAO parkingSpotDAO;
    @Mock(lenient = true)
    private TicketDAO ticketDAO;

    /**
     * Sets up a fresh ParkingService instance before each test.
     * Uses mocked dependencies injected by Mockito.
     */
    @BeforeEach
    public void setUpPerTest() throws Exception {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            when(inputReaderUtil.readSelection()).thenReturn(1);
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
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
        Date inTime = new Date(System.currentTimeMillis() - (60*60*1000)); // 1 hour parking time
        Date outTime = new Date();
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber(regNumber);

        // Configure mock behaviors
        when(ticketDAO.getTicket(regNumber)).thenReturn(ticket);
        when(ticketDAO.getNbTicket(regNumber)).thenReturn(2); // Recurring user
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

        // Execute the test
        parkingService.processExitingVehicle();

        // Verify all expected interactions and price calculation
        verify(ticketDAO).getTicket(regNumber);
        verify(ticketDAO).getNbTicket(regNumber);
        verify(ticketDAO).updateTicket(argThat(updatedTicket -> {
            double durationInHours = (outTime.getTime() - inTime.getTime()) / (1000.0 * 60.0 * 60.0);
            double expectedPrice = durationInHours * Fare.CAR_RATE_PER_HOUR;
            // Apply 5% discount for recurring user
            expectedPrice = expectedPrice * 0.95;
            return Math.abs(updatedTicket.getPrice() - expectedPrice) < 0.01;
        }));
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
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        when(ticketDAO.getNbTicket(regNumber)).thenReturn(2); // Recurring user
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);

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
        when(ticketDAO.getTicket(regNumber)).thenReturn(ticket);
        when(ticketDAO.getNbTicket(regNumber)).thenReturn(1);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);

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
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);

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
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(-1);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);

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
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);

        // Execute and verify
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        assertNull(parkingSpot);
        verify(parkingSpotDAO, never()).getNextAvailableSlot(any(ParkingType.class));
    }

    @Test
    public void testProcessIncomingVehicle_WhenParkingSpotIsNull() throws Exception {
        // Configure mock behaviors pour que getNextParkingNumberIfAvailable retourne null
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(-1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);

        // Execute the test
        parkingService.processIncomingVehicle();

        // Verify qu'on n'a pas essayé de sauvegarder de ticket
        verify(ticketDAO, never()).saveTicket(any(Ticket.class));
        verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));
    }

    @Test
    public void testProcessIncomingVehicle_WithNewUser() throws Exception {
        // Setup test data
        String regNumber = "ABCDEF";
        
        // Configure mock behaviors
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        when(ticketDAO.getNbTicket(regNumber)).thenReturn(0); // Nouvel utilisateur
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);

        // Execute the test
        parkingService.processIncomingVehicle();

        // Verify all expected interactions
        verify(parkingSpotDAO).updateParking(any(ParkingSpot.class));
        verify(ticketDAO).saveTicket(any(Ticket.class));
        verify(ticketDAO).getNbTicket(regNumber);
    }

    @Test
    public void testProcessIncomingVehicle_WithBike() throws Exception {
        // Setup test data
        String regNumber = "ABCDEF";
        
        // Configure mock behaviors
        when(inputReaderUtil.readSelection()).thenReturn(2); // BIKE selection
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(regNumber);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE)).thenReturn(1);
        when(ticketDAO.getNbTicket(regNumber)).thenReturn(0);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);

        // Execute the test
        parkingService.processIncomingVehicle();

        // Verify all expected interactions
        verify(parkingSpotDAO).getNextAvailableSlot(ParkingType.BIKE);
        verify(parkingSpotDAO).updateParking(any(ParkingSpot.class));
        verify(ticketDAO).saveTicket(any(Ticket.class));
    }

    @Test
    public void testProcessIncomingVehicle_WithException() throws Exception {
        // Configure mock behaviors to throw an exception
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenThrow(new Exception("Error reading vehicle registration number"));
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);

        // Execute the test
        parkingService.processIncomingVehicle();

        // Verify no interactions occurred after the exception
        verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, never()).saveTicket(any(Ticket.class));
    }

    @Test
    public void testProcessExitingVehicle_WithException() throws Exception {
        // Configure mock to throw an exception
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenThrow(new Exception("Error reading vehicle registration number"));
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);

        // Execute the test
        parkingService.processExitingVehicle();

        // Verify no interactions occurred after the exception
        verify(ticketDAO, never()).updateTicket(any(Ticket.class));
        verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));
    }
}