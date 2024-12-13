package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        // Vérification de la validité des dates d'entrée et sortie
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        // Calcul de la durée en millisecondes
        long inTime = ticket.getInTime().getTime();
        long outTime = ticket.getOutTime().getTime();
        
        // Conversion de la différence de temps en heures
        // On divise par 1000 pour passer des millisecondes aux secondes
        // Puis par 60 pour passer aux minutes
        // Puis encore par 60 pour passer aux heures
        double duration = (outTime - inTime) / (1000.0 * 60.0 * 60.0);

        // Si la durée est inférieure à 30 minutes (0.5 heure), pas de frais
        if (duration <= 0.5) {
            ticket.setPrice(0);
            return;
        }

        // Calcul du prix en fonction du type de véhicule
        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                break;
            }
            case BIKE: {
                ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown Parking Type");
        }
    }
}