package com.parkit.parkingsystem.model;

import java.util.Date;

/**
 * Represents a parking ticket in the parking system.
 * This class maintains all information related to a vehicle's parking session,
 * including entry and exit times, parking spot details, and payment information.
 * It serves as the primary record for tracking vehicle parking activities.
 */
public class Ticket {
    // Unique identifier for the ticket
    private int id;
    // The parking spot assigned to this ticket
    private ParkingSpot parkingSpot;
    // Vehicle's registration/license plate number
    private String vehicleRegNumber;
    // Calculated parking fee for the session
    private double price;
    // Time when the vehicle entered the parking lot
    private Date inTime;
    // Time when the vehicle exited the parking lot
    private Date outTime;

    /**
     * Gets the unique identifier of the ticket.
     *
     * @return The ticket ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the ticket.
     *
     * @param id The ticket ID to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the parking spot associated with this ticket.
     *
     * @return The ParkingSpot object containing spot details
     */
    public ParkingSpot getParkingSpot() {
        return parkingSpot;
    }

    /**
     * Sets the parking spot for this ticket.
     *
     * @param parkingSpot The ParkingSpot to assign to this ticket
     */
    public void setParkingSpot(ParkingSpot parkingSpot) {
        this.parkingSpot = parkingSpot;
    }

    /**
     * Gets the vehicle registration number.
     *
     * @return The vehicle's registration/license plate number
     */
    public String getVehicleRegNumber() {
        return vehicleRegNumber;
    }

    /**
     * Sets the vehicle registration number.
     *
     * @param vehicleRegNumber The vehicle's registration/license plate number to set
     */
    public void setVehicleRegNumber(String vehicleRegNumber) {
        this.vehicleRegNumber = vehicleRegNumber;
    }

    /**
     * Gets the calculated parking fee.
     *
     * @return The parking fee amount
     */
    public double getPrice() {
        return price;
    }

    /**
     * Sets the parking fee for this ticket.
     * This is typically set when the vehicle exits and the duration is calculated.
     *
     * @param price The parking fee to set
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Gets the time when the vehicle entered the parking lot.
     *
     * @return The entry time as a Date object
     */
    public Date getInTime() {
        return inTime;
    }

    /**
     * Sets the time when the vehicle entered the parking lot.
     *
     * @param inTime The entry time to set
     */
    public void setInTime(Date inTime) {
        this.inTime = inTime;
    }

    /**
     * Gets the time when the vehicle exited the parking lot.
     *
     * @return The exit time as a Date object, or null if the vehicle hasn't exited yet
     */
    public Date getOutTime() {
        return outTime;
    }

    /**
     * Sets the time when the vehicle exited the parking lot.
     *
     * @param outTime The exit time to set
     */
    public void setOutTime(Date outTime) {
        this.outTime = outTime;
    }
}
