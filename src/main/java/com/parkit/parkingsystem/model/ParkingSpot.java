package com.parkit.parkingsystem.model;

import com.parkit.parkingsystem.constants.ParkingType;

/**
 * Represents a parking spot in the parking system.
 * Each spot has a unique number, a type (CAR or BIKE), and availability status.
 * This class is used to track and manage individual parking spaces within the facility.
 */
public class ParkingSpot {
    // Unique identifier for the parking spot
    private int number;
    // Type of vehicles that can park in this spot (CAR or BIKE)
    private ParkingType parkingType;
    // Current availability status of the spot
    private boolean isAvailable;

    /**
     * Creates a new parking spot with specified attributes.
     *
     * @param number The unique identifier for the parking spot
     * @param parkingType The type of vehicle this spot can accommodate
     * @param isAvailable The initial availability status of the spot
     */
    public ParkingSpot(int number, ParkingType parkingType, boolean isAvailable) {
        this.number = number;
        this.parkingType = parkingType;
        this.isAvailable = isAvailable;
    }

    /**
     * Gets the unique identifier of the parking spot.
     *
     * @return The parking spot number
     */
    public int getId() {
        return number;
    }

    /**
     * Sets the unique identifier of the parking spot.
     *
     * @param number The new parking spot number
     */
    public void setId(int number) {
        this.number = number;
    }

    /**
     * Gets the type of vehicle this spot can accommodate.
     *
     * @return The parking type (CAR or BIKE)
     */
    public ParkingType getParkingType() {
        return parkingType;
    }

    /**
     * Sets the type of vehicle this spot can accommodate.
     *
     * @param parkingType The new parking type
     */
    public void setParkingType(ParkingType parkingType) {
        this.parkingType = parkingType;
    }

    /**
     * Checks if the parking spot is currently available.
     *
     * @return true if the spot is available, false otherwise
     */
    public boolean isAvailable() {
        return isAvailable;
    }

    /**
     * Sets the availability status of the parking spot.
     *
     * @param available The new availability status
     */
    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    /**
     * Compares this parking spot with another object for equality.
     * Two parking spots are considered equal if they have the same spot number,
     * regardless of their type or availability status.
     *
     * @param o The object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParkingSpot that = (ParkingSpot) o;
        return number == that.number;
    }

    /**
     * Generates a hash code for this parking spot.
     * The hash code is based solely on the spot number to match the equals implementation.
     *
     * @return The hash code value for this parking spot
     */
    @Override
    public int hashCode() {
        return number;
    }
}
