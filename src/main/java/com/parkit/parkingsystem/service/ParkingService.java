package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;

/**
 * Service class responsible for managing parking operations including vehicle entry and exit.
 * Handles parking spot allocation, ticket generation, and fare calculation.
 * Includes special handling for recurring users with discount benefits.
 */
public class ParkingService {

    private static final Logger logger = LogManager.getLogger("ParkingService");

    private static FareCalculatorService fareCalculatorService = new FareCalculatorService();

    private InputReaderUtil inputReaderUtil;
    private ParkingSpotDAO parkingSpotDAO;
    private TicketDAO ticketDAO;

    /**
     * Constructs a new ParkingService with required dependencies.
     * 
     * @param inputReaderUtil Utility for reading user input
     * @param parkingSpotDAO Data access object for parking spot operations
     * @param ticketDAO Data access object for ticket operations
     */
    public ParkingService(InputReaderUtil inputReaderUtil, ParkingSpotDAO parkingSpotDAO, TicketDAO ticketDAO){
        this.inputReaderUtil = inputReaderUtil;
        this.parkingSpotDAO = parkingSpotDAO;
        this.ticketDAO = ticketDAO;
    }

    /**
     * Processes an incoming vehicle by allocating a parking spot and generating a ticket.
     * Includes special handling for recurring users who receive a welcome message.
     */
    public void processIncomingVehicle() {
        try{
            // Get next available parking spot
            ParkingSpot parkingSpot = getNextParkingNumberIfAvailable();
            if(parkingSpot !=null && parkingSpot.getId() > 0){
                String vehicleRegNumber = getVehichleRegNumber();
                
                // Check if vehicle belongs to a recurring user
                if(ticketDAO.getNbTicket(vehicleRegNumber) > 0) {
                    System.out.println("Welcome back! As a regular user, you will receive a 5% discount");
                }
                
                // Update parking spot availability
                parkingSpot.setAvailable(false);
                parkingSpotDAO.updateParking(parkingSpot);

                // Generate and save new ticket
                Date inTime = new Date();
                Ticket ticket = new Ticket();
                ticket.setParkingSpot(parkingSpot);
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(0);
                ticket.setInTime(inTime);
                ticket.setOutTime(null);
                ticketDAO.saveTicket(ticket);
                
                // Display parking information to user
                System.out.println("Generated Ticket and saved in DB");
                System.out.println("Please park your vehicle in spot number:"+parkingSpot.getId());
                System.out.println("Recorded in-time for vehicle number:"+vehicleRegNumber+" is:"+inTime);
            }
        }catch(Exception e){
            logger.error("Unable to process incoming vehicle",e);
        }
    }

    /**
     * Reads and returns the vehicle registration number from user input.
     * 
     * @return The vehicle registration number
     * @throws Exception if there's an error reading the input
     */
    private String getVehichleRegNumber() throws Exception {
        System.out.println("Please type the vehicle registration number and press enter key");
        return inputReaderUtil.readVehicleRegistrationNumber();
    }

    /**
     * Finds and returns the next available parking spot based on vehicle type.
     * 
     * @return Available ParkingSpot object or null if none available
     */
    public ParkingSpot getNextParkingNumberIfAvailable(){
        int parkingNumber=0;
        ParkingSpot parkingSpot = null;
        try{
            // Get vehicle type and find available spot
            ParkingType parkingType = getVehichleType();
            parkingNumber = parkingSpotDAO.getNextAvailableSlot(parkingType);
            if(parkingNumber > 0){
                parkingSpot = new ParkingSpot(parkingNumber,parkingType, true);
            }else{
                throw new Exception("Error fetching parking number from DB. Parking slots might be full");
            }
        }catch(IllegalArgumentException ie){
            logger.error("Error parsing user input for type of vehicle", ie);
        }catch(Exception e){
            logger.error("Error fetching next available parking slot", e);
        }
        return parkingSpot;
    }

    /**
     * Gets the vehicle type from user input.
     * 
     * @return ParkingType enum value based on user selection
     * @throws IllegalArgumentException if input is invalid
     */
    private ParkingType getVehichleType(){
        System.out.println("Please select vehicle type from menu");
        System.out.println("1 CAR");
        System.out.println("2 BIKE");
        int input = inputReaderUtil.readSelection();
        switch(input){
            case 1: return ParkingType.CAR;
            case 2: return ParkingType.BIKE;
            default: {
                System.out.println("Incorrect input provided");
                throw new IllegalArgumentException("Entered input is invalid");
            }
        }
    }

    /**
     * Processes a vehicle exit by calculating parking fare and updating records.
     * Applies discount for recurring users and updates parking spot availability.
     */
    public void processExitingVehicle() {
        try {
            // Get vehicle details and ticket
            String vehicleRegNumber = getVehichleRegNumber();
            Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
            Date outTime = new Date();
            ticket.setOutTime(outTime);
            
            // Check if user qualifies for recurring user discount
            boolean discount = ticketDAO.getNbTicket(vehicleRegNumber) > 1;
            
            // Calculate parking fare
            fareCalculatorService.calculateFare(ticket, discount);
            
            // Update ticket and parking spot status
            if(ticketDAO.updateTicket(ticket)) {
                ParkingSpot parkingSpot = ticket.getParkingSpot();
                parkingSpot.setAvailable(true);
                parkingSpotDAO.updateParking(parkingSpot);
                System.out.println("Please pay the parking fare:" + ticket.getPrice());
                System.out.println("Recorded out-time for vehicle number:" + ticket.getVehicleRegNumber() + " is:" + outTime);
            } else {
                System.out.println("Unable to update ticket information. Error occurred");
            }
        } catch(Exception e) {
            logger.error("Unable to process exiting vehicle",e);
        }
    }
}

