package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

/**
 * Service responsible for calculating parking fees based on vehicle type and duration.
 * Handles different pricing rules including free parking for short stays and recurring user discounts.
 */
public class FareCalculatorService {

    /**
     * Calculates the parking fare for a given ticket.
     * 
     * @param ticket The parking ticket containing entry and exit times, and vehicle information
     * @param discount Whether to apply a 5% recurring user discount
     * @throws IllegalArgumentException if the exit time is null or before entry time,
     *         or if the parking type is unknown
     */
    public void calculateFare(Ticket ticket, boolean discount){
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket cannot be null");
        }
        if (ticket.getParkingSpot() == null) {
            throw new IllegalArgumentException("Parking spot cannot be null");
        }
        if (ticket.getParkingSpot().getParkingType() == null) {
            throw new IllegalArgumentException("Parking type cannot be null");
        }
        if (ticket.getOutTime() == null) {
            throw new IllegalArgumentException("Out time provided is incorrect:null");
        }
        if (ticket.getInTime() == null) {
            throw new IllegalArgumentException("In time cannot be null");
        }
        
        // Get entry and exit times in milliseconds
        long inTime = ticket.getInTime().getTime();
        long outTime = ticket.getOutTime().getTime();
        
        // Calculate duration in hours
        double duration = (outTime - inTime) / (1000.0 * 60.0 * 60.0);
    
        // Check for negative duration (including future entry time)
        if (duration < 0) {
            throw new IllegalArgumentException("Duration cannot be negative: " + duration);
        }
        
        // Free parking for stays under 30 minutes (0.5 hours)
        if (duration <= 0.5) {
            ticket.setPrice(0);
            return;
        }
    
        // Calculate base price according to vehicle type
        double price = 0;
        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                price = duration * Fare.CAR_RATE_PER_HOUR;
                break;
            }
            case BIKE: {
                price = duration * Fare.BIKE_RATE_PER_HOUR;
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown Parking Type: " + ticket.getParkingSpot().getParkingType());
        }
    
        // Apply 5% discount for recurring users if applicable
        if(discount) {
            price = price * 0.95; // 5% reduction
        }
        ticket.setPrice(price);
    }
}