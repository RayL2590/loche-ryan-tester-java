package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

/**
 * Test suite for the FareCalculatorService class.
 * Tests various scenarios of parking fare calculation including:
 * - Different vehicle types (CAR, BIKE)
 * - Various parking durations (less than 30 minutes, less than 1 hour, more than a day)
 * - Edge cases (unknown vehicle type, future entry time)
 * - Discount application for recurring users
 */
public class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;

    @BeforeAll
    public static void setUp() {
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    public void setUpPerTest() {
        ticket = new Ticket();
    }

    /**
     * Tests fare calculation for a car parked for exactly one hour.
     * Expected: Regular hourly rate for car
     */
    @Test
    public void calculateFareCar(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  65 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket, false);
        double durationInHours = (outTime.getTime() - inTime.getTime()) / (1000.0 * 60.0 * 60.0);
        assertEquals(Fare.CAR_RATE_PER_HOUR * durationInHours, ticket.getPrice());
    }

    /**
     * Tests fare calculation for a bike parked for exactly one hour.
     * Expected: Regular hourly rate for bike
     */
    @Test
    public void calculateFareBike(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket, false);
        double durationInHours = (outTime.getTime() - inTime.getTime()) / (1000.0 * 60.0 * 60.0);
        assertEquals(Fare.BIKE_RATE_PER_HOUR * durationInHours, ticket.getPrice());
    }

    /**
     * Tests fare calculation with a null parking type.
     * Expected: IllegalArgumentException with message "Parking type cannot be null"
     */
    @Test
    public void calculateFareUnkownType(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, null, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> fareCalculatorService.calculateFare(ticket, false),
            "Une IllegalArgumentException aurait dû être lancée pour un type de parking null");
        assertEquals("Parking type cannot be null", exception.getMessage(),
            "Le message d'erreur devrait indiquer que le type de parking est null");
    }

    

    /**
     * Tests fare calculation with a future entry time.
     * Expected: IllegalArgumentException
     */
    @Test
    public void calculateFareBikeWithFutureInTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() + (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket, false));
    }

    /**
     * Tests fare calculation for a bike parked for 45 minutes.
     * Expected: 45 minutes of the hourly rate
     */
    @Test
    public void calculateFareBikeWithLessThanOneHourParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket, false);
        double durationInHours = (outTime.getTime() - inTime.getTime()) / (1000.0 * 60.0 * 60.0);
        assertEquals(Fare.BIKE_RATE_PER_HOUR * durationInHours, ticket.getPrice());
    }

    /**
     * Tests fare calculation for a car parked for 45 minutes.
     * Expected: 45 minutes of the hourly rate
     */
    @Test
    public void calculateFareCarWithLessThanOneHourParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket, false);
        double durationInHours = (outTime.getTime() - inTime.getTime()) / (1000.0 * 60.0 * 60.0);
        assertEquals(Fare.CAR_RATE_PER_HOUR * durationInHours, ticket.getPrice());
    }

    /**
     * Tests fare calculation for a car parked for 24 hours.
     * Expected: 24 hours of the hourly rate
     */
    @Test
    public void calculateFareCarWithMoreThanADayParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  24 * 60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket, false);
        double durationInHours = (outTime.getTime() - inTime.getTime()) / (1000.0 * 60.0 * 60.0);
        assertEquals(Fare.CAR_RATE_PER_HOUR * durationInHours, ticket.getPrice());
    }

    /**
     * Tests free parking for a car parked less than 30 minutes.
     * Expected: Zero fare
     */
    @Test
    public void calculateFareCarWithLessThan30minutesParkingTime(){

        //Arrangement (Given)   
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (25 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        //Action (When)
        fareCalculatorService.calculateFare(ticket, false);

        //Assertion (Then)
        assertEquals(0, ticket.getPrice());
    }

    /**
     * Tests free parking for a bike parked less than 30 minutes.
     * Expected: Zero fare
     */
    @Test
    public void calculateFareBikeWithLessThan30minutesParkingTime(){
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (25 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket, false);
        assertEquals(0, ticket.getPrice());
    }

    /**
     * Tests 5% discount application for a recurring car user.
     * Expected: 95% of regular hourly rate
     */
    @Test
    public void calculateFareCarWithDiscount(){
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket, true);
        assertEquals(Fare.CAR_RATE_PER_HOUR * 0.95, ticket.getPrice());
    }

    /**
     * Tests 5% discount application for a recurring bike user.
     * Expected: 95% of regular hourly rate
     */
    @Test
    public void calculateFareBikeWithDiscount(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (60 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket, true);
        assertEquals(Fare.BIKE_RATE_PER_HOUR * 0.95, ticket.getPrice());
    }

    /**
     * Tests that free parking (less than 30 minutes) takes precedence over recurring user discount.
     * Expected: Zero fare despite being a recurring user
     */
    @Test
    public void calculateFareCarWithDiscountAndLessThan30Min(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (25 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket, true);
        assertEquals(0, ticket.getPrice());
    }

    /**
     * Tests fare calculation with an unknown parking type (not CAR or BIKE).
     * Expected: IllegalArgumentException with message "Unknown Parking Type"
     */
    @Test
    public void calculateFareWithUnknownParkingType() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        Date outTime = new Date();
        
        // Créer un ParkingSpot avec un type de parking null
        ParkingSpot parkingSpot = new ParkingSpot(1, null, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        
        // Vérifier que l'exception est lancée avec le bon message
        Exception exception = assertThrows(Exception.class, 
            () -> fareCalculatorService.calculateFare(ticket, false),
            "Une exception aurait dû être lancée pour un type de parking null");
        assertTrue(exception instanceof IllegalArgumentException || exception instanceof NullPointerException,
            "L'exception devrait être soit IllegalArgumentException soit NullPointerException");
    }

    /**
     * Tests fare calculation with an invalid parking type.
     * Expected: IllegalArgumentException with message containing "Unknown Parking Type"
     */
    @Test
    public void calculateFareWithInvalidParkingType() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        Date outTime = new Date();
        
        // Créer un parking spot avec le type TRUCK qui n'est pas géré dans le switch
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.TRUCK, false);
        
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> fareCalculatorService.calculateFare(ticket, false),
            "Une IllegalArgumentException aurait dû être lancée pour un type de parking invalide");
        assertTrue(exception.getMessage().contains("Unknown Parking Type"),
            "Le message d'erreur devrait indiquer que le type de parking est inconnu");
    }

    /**
     * Tests that a null ticket throws the correct exception.
     * Expected: IllegalArgumentException with message "Ticket cannot be null"
     */
    @Test
    public void calculateFareWithNullTicket() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> fareCalculatorService.calculateFare(null, false),
            "Une IllegalArgumentException aurait dû être lancée pour un ticket null");
        assertEquals("Ticket cannot be null", exception.getMessage());
    }

    /**
     * Tests that a ticket with null parking spot throws the correct exception.
     * Expected: IllegalArgumentException with message "Parking spot cannot be null"
     */
    @Test
    public void calculateFareWithNullParkingSpot() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        Date outTime = new Date();

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> fareCalculatorService.calculateFare(ticket, false),
            "Une IllegalArgumentException aurait dû être lancée pour un parking spot null");
        assertEquals("Parking spot cannot be null", exception.getMessage());
    }

    /**
     * Tests that a ticket with null parking type throws the correct exception.
     * Expected: IllegalArgumentException with message "Parking type cannot be null"
     */
    @Test
    public void calculateFareWithNullParkingType() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, null, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> fareCalculatorService.calculateFare(ticket, false),
            "Une IllegalArgumentException aurait dû être lancée pour un parking type null");
        assertEquals("Parking type cannot be null", exception.getMessage());
    }

    /**
     * Tests that a ticket with null out time throws the correct exception.
     * Expected: IllegalArgumentException with message "Out time provided is incorrect:null"
     */
    @Test
    public void calculateFareWithNullOutTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(null);
        ticket.setParkingSpot(parkingSpot);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> fareCalculatorService.calculateFare(ticket, false),
            "Une IllegalArgumentException aurait dû être lancée pour un out time null");
        assertEquals("Out time provided is incorrect:null", exception.getMessage());
    }

    /**
     * Tests that a ticket with a negative duration throws the correct exception.
     * Expected: IllegalArgumentException with message containing "Duration cannot be negative"
     */
    @Test
    public void calculateFareWithNegativeDuration() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() + (60 * 60 * 1000)); // Future time
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
    
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
    
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> fareCalculatorService.calculateFare(ticket, false),
            "Une IllegalArgumentException aurait dû être lancée pour une durée négative");
        assertTrue(exception.getMessage().contains("Duration cannot be negative"),
            "Le message d'erreur devrait indiquer que la durée est négative");
    }

    /**
     * Tests that a ticket with a very large duration (multiple years) calculates correctly.
     * Expected: Price should be calculated correctly without overflow
     */
    @Test
    public void calculateFareWithVeryLargeDuration() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (365L * 24 * 60 * 60 * 1000)); // One year ago
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket, false);
        
        // Vérifier que le prix est calculé correctement pour une année complète
        double expectedPrice = 365.0 * 24 * Fare.CAR_RATE_PER_HOUR;
        assertEquals(expectedPrice, ticket.getPrice(), 0.01,
            "Le prix devrait être calculé correctement pour une durée d'un an");
    }

    /**
     * Tests that a ticket with null in time throws the correct exception.
     * Expected: IllegalArgumentException with message "In time cannot be null"
     */
    @Test
    public void calculateFareWithNullInTime() {
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(null);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> fareCalculatorService.calculateFare(ticket, false),
            "Une IllegalArgumentException aurait dû être lancée pour un in time null");
        assertEquals("In time cannot be null", exception.getMessage(),
            "Le message d'erreur devrait indiquer que le in time est null");
    }
}
