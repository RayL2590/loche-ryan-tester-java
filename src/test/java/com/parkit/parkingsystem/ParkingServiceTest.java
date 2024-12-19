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

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private ParkingService parkingService;

    @Mock
    private InputReaderUtil inputReaderUtil;
    @Mock
    private ParkingSpotDAO parkingSpotDAO;
    @Mock
    private TicketDAO ticketDAO;

    @BeforeEach
    private void setUpPerTest() {
        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    }

    @Test
    public void processExitingVehicleTest() throws Exception {
        // Préparation des données
        String regNumber = "ABCDEF";
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber(regNumber);

        // Configuration des mocks
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(regNumber);
        when(ticketDAO.getTicket(regNumber)).thenReturn(ticket);
        when(ticketDAO.getNbTicket(regNumber)).thenReturn(2);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

        // Exécution
        parkingService.processExitingVehicle();

        // Vérifications
        verify(ticketDAO).getTicket(regNumber);
        verify(ticketDAO).getNbTicket(regNumber);
        verify(ticketDAO).updateTicket(any(Ticket.class));
        verify(parkingSpotDAO).updateParking(any(ParkingSpot.class));
    }

    @Test
    public void testProcessIncomingVehicle() throws Exception {
        // Préparation des données
        String regNumber = "ABCDEF";
        
        // Configuration des mocks
        when(inputReaderUtil.readSelection()).thenReturn(1); // CAR
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(regNumber);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        when(ticketDAO.getNbTicket(regNumber)).thenReturn(2);

        // Exécution
        parkingService.processIncomingVehicle();

        // Vérifications
        verify(inputReaderUtil).readSelection();
        verify(inputReaderUtil).readVehicleRegistrationNumber();
        verify(parkingSpotDAO).getNextAvailableSlot(ParkingType.CAR);
        verify(parkingSpotDAO).updateParking(any(ParkingSpot.class));
        verify(ticketDAO).saveTicket(any(Ticket.class));
    }

    @Test
    public void processExitingVehicleTestUnableUpdate() throws Exception {
        // Préparation des données
        String regNumber = "ABCDEF";
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber(regNumber);

        // Configuration des mocks
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(regNumber);
        when(ticketDAO.getTicket(regNumber)).thenReturn(ticket);
        when(ticketDAO.getNbTicket(regNumber)).thenReturn(1);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

        // Exécution
        parkingService.processExitingVehicle();

        // Vérifications
        verify(ticketDAO).updateTicket(any(Ticket.class));
        verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));
    }

    @Test
    public void testGetNextParkingNumberIfAvailable() {
        // Configuration des mocks
        when(inputReaderUtil.readSelection()).thenReturn(1); // CAR
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);

        // Exécution
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        // Vérifications
        assertNotNull(parkingSpot);
        assertEquals(1, parkingSpot.getId());
        assertEquals(ParkingType.CAR, parkingSpot.getParkingType());
        assertTrue(parkingSpot.isAvailable());
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
        // Configuration des mocks
        when(inputReaderUtil.readSelection()).thenReturn(1); // CAR
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(-1);

        // Exécution
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        // Vérifications
        assertNull(parkingSpot);
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
        // Configuration des mocks
        when(inputReaderUtil.readSelection()).thenReturn(3); // Type invalide

        // Exécution
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        // Vérifications
        assertNull(parkingSpot);
        verify(parkingSpotDAO, never()).getNextAvailableSlot(any(ParkingType.class));
    }
}