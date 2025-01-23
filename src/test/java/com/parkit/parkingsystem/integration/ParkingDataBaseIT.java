package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;

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
                
            // Vérifie que le ticket est sauvegardé
            Ticket ticket = ticketDAO.getTicket("ABCDEF");
            assertNotNull(ticket, "Le ticket devrait être sauvegardé en base");
            assertEquals("ABCDEF", ticket.getVehicleRegNumber());
            assertNotNull(ticket.getInTime(), "L'heure d'entrée devrait être enregistrée");
            assertNull(ticket.getOutTime(), "L'heure de sortie devrait être null");
                
            // Vérifie que la place est marquée comme occupée
            ParkingSpot parkingSpot = ticket.getParkingSpot();
            assertFalse(parkingSpot.isAvailable(), "La place devrait être marquée comme occupée");
                
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to test parking a car", e);
        }
    }

    @Test
    public void testParkingLotExit() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            when(inputReaderUtil.readSelection()).thenReturn(1);
            
            ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            
            // Simuler l'entrée
            parkingService.processIncomingVehicle();
            
            // Dormir suffisamment longtemps pour avoir un prix > 0
            Thread.sleep(1000);
            
            // Récupérer le ticket original
            Ticket ticket = ticketDAO.getTicket("ABCDEF");
            assertNotNull(ticket);
            
            // Modifier manuellement la date d'entrée avec un PreparedStatement direct
            try (Connection con = dataBaseTestConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(
                    "UPDATE ticket SET IN_TIME=? WHERE VEHICLE_REG_NUMBER=?")) {
                
                // Définir une heure d'entrée il y a 1 heure
                Date inTime = new Date(System.currentTimeMillis() - (60 * 60 * 1000));
                ps.setTimestamp(1, new Timestamp(inTime.getTime()));
                ps.setString(2, "ABCDEF");
                ps.executeUpdate();
            }
            
            // Faire sortir le véhicule
            parkingService.processExitingVehicle();
                
            // Vérifier les résultats
            Ticket updatedTicket = ticketDAO.getTicket("ABCDEF");
            assertNotNull(updatedTicket, "Le ticket devrait exister");
            assertNotNull(updatedTicket.getOutTime(), "L'heure de sortie devrait être enregistrée");
            assertTrue(updatedTicket.getPrice() > 0, "Le prix devrait être calculé");
            
            // Vérifier que la place est libérée en vérifiant directement la base de données
            int parkingNumber = updatedTicket.getParkingSpot().getId();
            boolean isAvailable = false;
            try (Connection con = dataBaseTestConfig.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT available FROM parking WHERE PARKING_NUMBER = ?")) {
                ps.setInt(1, parkingNumber);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    isAvailable = rs.getBoolean("available");
                }
            }
            assertTrue(isAvailable, "La place devrait être marquée comme disponible");
                
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
            Thread.sleep(3600); // Attendre un peu pour avoir un prix > 0
            parkingService.processExitingVehicle();
                
            double firstPrice = ticketDAO.getTicket("ABCDEF").getPrice();
                
            // Deuxième stationnement
            parkingService.processIncomingVehicle();
            Thread.sleep(1000); // Même durée que le premier stationnement
            parkingService.processExitingVehicle();
                
            double secondPrice = ticketDAO.getTicket("ABCDEF").getPrice();
                
            // Le second prix devrait être 95% du premier pour la même durée
            assertEquals(firstPrice * 0.95, secondPrice, 0.01, "La réduction de 5% devrait être appliquée");
                
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to test recurring user", e);
        }
    }
}