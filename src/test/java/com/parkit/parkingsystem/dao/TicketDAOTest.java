package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TicketDAOTest {

    private static final DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @BeforeAll
    private static void setUp() {
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() {
        dataBasePrepareService.clearDataBaseEntries();
    }

    @Test
    public void testGetNbTicketWithNoTickets() {
        // GIVEN un véhicule sans tickets
        String vehicleRegNumber = "ABCDEF";

        // WHEN on compte les tickets
        int nbTickets = ticketDAO.getNbTicket(vehicleRegNumber);

        // THEN le nombre de tickets est 0
        assertEquals(0, nbTickets, "Le nombre de tickets devrait être 0 pour un nouveau véhicule");
    }

    @Test
    public void testGetNbTicketWithOngoingParking() throws Exception {
        // GIVEN un véhicule avec un stationnement en cours
        String vehicleRegNumber = "ABCDEF";
        
        // Créer un ticket sans heure de sortie (stationnement en cours)
        Ticket ticket = new Ticket();
        ticket.setVehicleRegNumber(vehicleRegNumber);
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
        ticket.setInTime(new Date());
        ticket.setPrice(0);
        ticketDAO.saveTicket(ticket);

        // WHEN on compte les tickets
        int nbTickets = ticketDAO.getNbTicket(vehicleRegNumber);

        // THEN le nombre de tickets est 0 car le stationnement est en cours
        assertEquals(0, nbTickets, "Le nombre de tickets devrait être 0 pour un stationnement en cours");
    }

    @Test
    public void testGetNbTicketWithCompletedParking() throws Exception {
        // GIVEN un véhicule avec un stationnement terminé
        String vehicleRegNumber = "ABCDEF";
        
        // Créer un ticket avec une heure de sortie (stationnement terminé)
        Ticket ticket = new Ticket();
        ticket.setVehicleRegNumber(vehicleRegNumber);
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
        ticket.setInTime(new Date(System.currentTimeMillis() - 3600000)); // il y a 1 heure
        ticket.setOutTime(new Date()); // maintenant
        ticket.setPrice(1.5);
        ticketDAO.saveTicket(ticket);

        // WHEN on compte les tickets
        int nbTickets = ticketDAO.getNbTicket(vehicleRegNumber);

        // THEN le nombre de tickets est 1
        assertEquals(1, nbTickets, "Le nombre de tickets devrait être 1 pour un stationnement terminé");
    }

    @Test
    public void testGetNbTicketWithMultipleCompletedParkings() throws Exception {
        // GIVEN un véhicule avec plusieurs stationnements terminés
        String vehicleRegNumber = "ABCDEF";
        
        // Créer deux tickets avec des heures de sortie (stationnements terminés)
        for (int i = 0; i < 2; i++) {
            Ticket ticket = new Ticket();
            ticket.setVehicleRegNumber(vehicleRegNumber);
            ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
            ticket.setInTime(new Date(System.currentTimeMillis() - (3600000 * (i + 2)))); // il y a 2 et 3 heures
            ticket.setOutTime(new Date(System.currentTimeMillis() - (3600000 * (i + 1)))); // il y a 1 et 2 heures
            ticket.setPrice(1.5);
            ticketDAO.saveTicket(ticket);
        }

        // Ajouter un stationnement en cours (ne devrait pas être compté)
        Ticket ongoingTicket = new Ticket();
        ongoingTicket.setVehicleRegNumber(vehicleRegNumber);
        ongoingTicket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
        ongoingTicket.setInTime(new Date());
        ongoingTicket.setPrice(0);
        ticketDAO.saveTicket(ongoingTicket);

        // WHEN on compte les tickets
        int nbTickets = ticketDAO.getNbTicket(vehicleRegNumber);

        // THEN le nombre de tickets est 2
        assertEquals(2, nbTickets, "Le nombre de tickets devrait être 2 pour deux stationnements terminés");
    }

    @Test
    public void testGetTicket_WhenTicketExists() throws Exception {
        // GIVEN un véhicule avec un ticket existant
        String vehicleRegNumber = "ABCDEF";
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Date inTime = new Date(System.currentTimeMillis() - 3600000); // il y a 1 heure
        
        Ticket savedTicket = new Ticket();
        savedTicket.setVehicleRegNumber(vehicleRegNumber);
        savedTicket.setParkingSpot(parkingSpot);
        savedTicket.setInTime(inTime);
        savedTicket.setPrice(0);
        ticketDAO.saveTicket(savedTicket);

        // WHEN on récupère le ticket
        Ticket retrievedTicket = ticketDAO.getTicket(vehicleRegNumber);

        // THEN le ticket est correctement récupéré
        assertNotNull(retrievedTicket, "Le ticket devrait être trouvé");
        assertEquals(vehicleRegNumber, retrievedTicket.getVehicleRegNumber(), "Le numéro d'immatriculation devrait correspondre");
        assertEquals(parkingSpot.getId(), retrievedTicket.getParkingSpot().getId(), "L'ID de la place de parking devrait correspondre");
        assertEquals(parkingSpot.getParkingType(), retrievedTicket.getParkingSpot().getParkingType(), "Le type de parking devrait correspondre");
        assertEquals(0, retrievedTicket.getPrice(), 0.001, "Le prix initial devrait être 0");
        assertEquals(inTime.getTime() / 1000, retrievedTicket.getInTime().getTime() / 1000, "L'heure d'entrée devrait correspondre");
    }

    @Test
    public void testGetTicket_WhenTicketDoesNotExist() {
        // GIVEN un véhicule sans ticket
        String vehicleRegNumber = "NONEXISTENT";

        // WHEN on récupère le ticket
        Ticket retrievedTicket = ticketDAO.getTicket(vehicleRegNumber);

        // THEN le résultat est null
        assertNull(retrievedTicket, "Le ticket devrait être null pour un véhicule inexistant");
    }

    @Test
    public void testUpdateTicket_Success() throws Exception {
        // GIVEN un ticket existant
        String vehicleRegNumber = "ABCDEF";
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Date inTime = new Date(System.currentTimeMillis() - 3600000); // il y a 1 heure
        
        Ticket ticket = new Ticket();
        ticket.setVehicleRegNumber(vehicleRegNumber);
        ticket.setParkingSpot(parkingSpot);
        ticket.setInTime(inTime);
        ticket.setPrice(0);
        ticketDAO.saveTicket(ticket);

        // Récupérer le ticket sauvegardé pour avoir son ID
        Ticket savedTicket = ticketDAO.getTicket(vehicleRegNumber);
        assertNotNull(savedTicket, "Le ticket devrait être sauvegardé et récupérable");

        // WHEN on met à jour le ticket
        Date outTime = new Date();
        savedTicket.setPrice(15.0);
        savedTicket.setOutTime(outTime);
        boolean updateResult = ticketDAO.updateTicket(savedTicket);

        // THEN la mise à jour est réussie
        assertTrue(updateResult, "La mise à jour du ticket devrait réussir");

        // Vérifier que les modifications sont bien sauvegardées
        Ticket updatedTicket = ticketDAO.getTicket(vehicleRegNumber);
        assertNotNull(updatedTicket, "Le ticket mis à jour devrait être récupérable");
        assertEquals(15.0, updatedTicket.getPrice(), 0.001, "Le prix devrait être mis à jour");
        assertEquals(outTime.getTime() / 1000, updatedTicket.getOutTime().getTime() / 1000, "L'heure de sortie devrait être mise à jour");
    }

    @Test
    public void testUpdateTicket_WithDatabaseError() throws Exception {
        // GIVEN un ticket avec des données qui provoqueront une erreur
        Ticket ticket = new Ticket();
        ticket.setId(1); // ID existant
        ticket.setPrice(15.0);
        // Ne pas définir outTime pour provoquer une NullPointerException
        
        // WHEN on essaie de mettre à jour le ticket
        boolean updateResult = ticketDAO.updateTicket(ticket);

        // THEN la mise à jour échoue à cause de l'erreur
        assertFalse(updateResult, "La mise à jour devrait échouer en cas d'erreur de base de données");
    }

    @Test
    public void testSaveTicketWithDatabaseError() {
        // GIVEN un ticket valide mais une configuration de base de données invalide
        Ticket ticket = new Ticket();
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
        ticket.setInTime(new Date());
        
        // Simuler une erreur de base de données en utilisant une configuration invalide
        ticketDAO.dataBaseConfig = new DataBaseTestConfig() {
            @Override
            public java.sql.Connection getConnection() throws ClassNotFoundException, java.sql.SQLException {
                throw new java.sql.SQLException("Simulated database error");
            }
        };

        // WHEN on essaie de sauvegarder le ticket
        boolean result = ticketDAO.saveTicket(ticket);

        // THEN le résultat est false à cause de l'erreur
        assertFalse(result, "La sauvegarde devrait échouer en cas d'erreur de base de données");
        
        // Restaurer la configuration normale pour les autres tests
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
    }

    @Test
    public void testGetTicketWithDatabaseError() {
        // GIVEN une configuration de base de données invalide
        ticketDAO.dataBaseConfig = new DataBaseTestConfig() {
            @Override
            public java.sql.Connection getConnection() throws ClassNotFoundException, java.sql.SQLException {
                throw new java.sql.SQLException("Simulated database error");
            }
        };

        // WHEN on essaie de récupérer un ticket
        Ticket ticket = ticketDAO.getTicket("ABCDEF");

        // THEN le résultat est null à cause de l'erreur
        assertNull(ticket, "Le ticket devrait être null en cas d'erreur de base de données");
        
        // Restaurer la configuration normale pour les autres tests
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
    }

    @Test
    public void testGetNbTicketWithDatabaseError() {
        // GIVEN une configuration de base de données invalide
        ticketDAO.dataBaseConfig = new DataBaseTestConfig() {
            @Override
            public java.sql.Connection getConnection() throws ClassNotFoundException, java.sql.SQLException {
                throw new java.sql.SQLException("Simulated database error");
            }
        };

        // WHEN on essaie de compter les tickets
        int nbTickets = ticketDAO.getNbTicket("ABCDEF");

        // THEN le résultat est 0 à cause de l'erreur
        assertEquals(0, nbTickets, "Le nombre de tickets devrait être 0 en cas d'erreur de base de données");
        
        // Restaurer la configuration normale pour les autres tests
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
    }

    @Test
    public void testGetNbTicketWithEmptyResultSet() {
        // GIVEN une configuration de base de données qui retourne un ResultSet vide
        ticketDAO.dataBaseConfig = new DataBaseTestConfig() {
            @Override
            public java.sql.Connection getConnection() throws ClassNotFoundException, java.sql.SQLException {
                java.sql.Connection mockConnection = Mockito.mock(java.sql.Connection.class);
                java.sql.PreparedStatement mockPreparedStatement = Mockito.mock(java.sql.PreparedStatement.class);
                java.sql.ResultSet mockResultSet = Mockito.mock(java.sql.ResultSet.class);
                
                Mockito.when(mockConnection.prepareStatement(Mockito.anyString()))
                    .thenReturn(mockPreparedStatement);
                Mockito.when(mockPreparedStatement.executeQuery())
                    .thenReturn(mockResultSet);
                Mockito.when(mockResultSet.next())
                    .thenReturn(false); // Simuler un ResultSet vide
                
                return mockConnection;
            }
        };

        // WHEN on essaie de compter les tickets
        int nbTickets = ticketDAO.getNbTicket("ABCDEF");

        // THEN le résultat est 0 car il n'y a pas de résultats
        assertEquals(0, nbTickets, "Le nombre de tickets devrait être 0 pour un ResultSet vide");
        
        // Restaurer la configuration normale pour les autres tests
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
    }
}
