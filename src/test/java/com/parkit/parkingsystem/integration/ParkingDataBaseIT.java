package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static final DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() {
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown() {
    }

    @Test
    public void testParkingACar() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            when(inputReaderUtil.readSelection()).thenReturn(1);
            
            ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            parkingService.processIncomingVehicle();
            //TODO: check that a ticket is actualy saved in DB and Parking table is updated with availability
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to test parking a car", e);
        }
    }

    @Test
    public void testParkingLotExit() {
        try {
            testParkingACar(); // On gare d'abord une voiture
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            
            ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            parkingService.processExitingVehicle();
            //TODO: check that the fare generated and out time are populated correctly in the database
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to test parking lot exit", e);
        }
    }

    @Test
    public void testParkingLotExitRecurringUser() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            when(inputReaderUtil.readSelection()).thenReturn(1);
            
            ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            
            // Premier stationnement
            parkingService.processIncomingVehicle();
            parkingService.processExitingVehicle();
            
            // Deuxième stationnement (devrait avoir la réduction)
            parkingService.processIncomingVehicle();
            parkingService.processExitingVehicle();
            
            // TODO: Vérifier dans la base que le prix du second ticket inclut la réduction de 5%
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to test recurring user", e);
        }
    }
}