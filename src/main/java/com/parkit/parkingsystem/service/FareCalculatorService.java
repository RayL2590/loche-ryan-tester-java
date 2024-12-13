package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean discount){
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }
    
        long inTime = ticket.getInTime().getTime();
        long outTime = ticket.getOutTime().getTime();
        
        double duration = (outTime - inTime) / (1000.0 * 60.0 * 60.0);
    
        if (duration <= 0.5) {
            ticket.setPrice(0);
            return;
        }
    
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
                throw new IllegalArgumentException("Unknown Parking Type");
        }
    
        if(discount) {
            price = price * 0.95; // Réduction de 5%
        }
        ticket.setPrice(price);
    }
}