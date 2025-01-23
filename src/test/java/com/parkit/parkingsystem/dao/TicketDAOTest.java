package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

        // THEN le nombre de tickets est 2 (seulement les stationnements terminés)
        assertEquals(2, nbTickets, "Le nombre de tickets devrait être 2 pour deux stationnements terminés");
    }
}
