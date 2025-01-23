package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main interface class that provides the command-line interface for the parking system.
 * Handles user interaction and routes commands to appropriate service classes.
 * This shell provides options for vehicle entry, exit, and system shutdown.
 */
public class InteractiveShell {

    private static final Logger logger = LogManager.getLogger("InteractiveShell");

    /**
     * Initializes and runs the main application interface.
     * Creates necessary service instances and maintains the main application loop.
     * Processes user input and delegates to appropriate service methods.
     */
    public static void loadInterface(){
        logger.info("App initialized!!!");
        System.out.println("Welcome to Parking System!");

        // Initialize system components
        boolean continueApp = true;
        InputReaderUtil inputReaderUtil = new InputReaderUtil();
        ParkingSpotDAO parkingSpotDAO = new ParkingSpotDAO();
        TicketDAO ticketDAO = new TicketDAO();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        // Main application loop
        while(continueApp){
            loadMenu();
            int option = inputReaderUtil.readSelection();
            
            // Process user selection
            switch(option){
                case 1: {
                    // Handle incoming vehicle
                    parkingService.processIncomingVehicle();
                    break;
                }
                case 2: {
                    // Handle exiting vehicle
                    parkingService.processExitingVehicle();
                    break;
                }
                case 3: {
                    // Shutdown system
                    System.out.println("Exiting from the system!");
                    continueApp = false;
                    break;
                }
                default: System.out.println("Unsupported option. Please enter a number corresponding to the provided menu");
            }
        }
    }

    /**
     * Displays the main menu options to the user.
     * Shows available actions for parking management:
     * 1. New vehicle entry
     * 2. Vehicle exit and payment
     * 3. System shutdown
     */
    private static void loadMenu(){
        System.out.println("Please select an option. Simply enter the number to choose an action");
        System.out.println("1 New Vehicle Entering - Allocate Parking Space");
        System.out.println("2 Vehicle Exiting - Generate Ticket Price");
        System.out.println("3 Shutdown System");
    }
}
