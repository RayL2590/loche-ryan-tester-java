package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InteractiveShellTest {

    @Mock
    private InputReaderUtil inputReaderUtil;
    @Mock
    private ParkingSpotDAO parkingSpotDAO;
    @Mock
    private TicketDAO ticketDAO;
    @Mock
    private ParkingService parkingService;

    @Test
    void loadInterface_ShouldProcessIncomingVehicle_WhenOptionIsOne() {
        // Arrange
        when(inputReaderUtil.readSelection())
                .thenReturn(1)  // Premier appel : option 1
                .thenReturn(3); // Deuxième appel : quitter

        // Act
        InteractiveShell.loadInterface(inputReaderUtil, parkingSpotDAO, ticketDAO, parkingService);

        // Assert
        verify(parkingService, times(1)).processIncomingVehicle();
        verify(parkingService, never()).processExitingVehicle();
    }

    @Test
    void loadInterface_ShouldProcessExitingVehicle_WhenOptionIsTwo() {
        // Arrange
        when(inputReaderUtil.readSelection())
                .thenReturn(2)  // Premier appel : option 2
                .thenReturn(3); // Deuxième appel : quitter

        // Act
        InteractiveShell.loadInterface(inputReaderUtil, parkingSpotDAO, ticketDAO, parkingService);

        // Assert
        verify(parkingService, times(1)).processExitingVehicle();
        verify(parkingService, never()).processIncomingVehicle();
    }

    @Test
    void loadInterface_ShouldHandleInvalidOption() {
        // Arrange
        when(inputReaderUtil.readSelection())
                .thenReturn(5)  // Premier appel : option invalide
                .thenReturn(3); // Deuxième appel : quitter

        // Act
        InteractiveShell.loadInterface(inputReaderUtil, parkingSpotDAO, ticketDAO, parkingService);

        // Assert
        verify(parkingService, never()).processIncomingVehicle();
        verify(parkingService, never()).processExitingVehicle();
    }

    @Test
    void loadInterface_ShouldExitImmediately_WhenOptionIsThree() {
        // Arrange
        when(inputReaderUtil.readSelection()).thenReturn(3);

        // Act
        InteractiveShell.loadInterface(inputReaderUtil, parkingSpotDAO, ticketDAO, parkingService);

        // Assert
        verify(parkingService, never()).processIncomingVehicle();
        verify(parkingService, never()).processExitingVehicle();
        verify(inputReaderUtil, times(1)).readSelection();
    }
}
