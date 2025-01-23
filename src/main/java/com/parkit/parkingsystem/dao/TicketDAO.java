package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

/**
 * Data Access Object for handling parking ticket operations in the database.
 * This class manages all database interactions related to parking tickets including:
 * - Creating new tickets
 * - Retrieving existing tickets
 * - Updating ticket information
 * - Counting tickets for recurring user detection
 */
public class TicketDAO {

    private static final Logger logger = LogManager.getLogger("TicketDAO");

    public DataBaseConfig dataBaseConfig = new DataBaseConfig();

    /**
     * Saves a new parking ticket to the database.
     * 
     * @param ticket The ticket object containing all necessary parking information
     * @return true if the save operation was successful, false otherwise
     */
    public boolean saveTicket(Ticket ticket) {
        Connection con = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.SAVE_TICKET);
            // Set all ticket fields in the prepared statement
            ps.setInt(1, ticket.getParkingSpot().getId());
            ps.setString(2, ticket.getVehicleRegNumber());
            ps.setDouble(3, ticket.getPrice());
            ps.setTimestamp(4, new Timestamp(ticket.getInTime().getTime()));
            ps.setTimestamp(5, (ticket.getOutTime() == null) ? null : (new Timestamp(ticket.getOutTime().getTime())));
            return ps.execute();
        } catch (Exception ex) {
            logger.error("Error fetching next available slot", ex);
            return false;
        } finally {
            dataBaseConfig.closeConnection(con);
        }
    }

    /**
     * Retrieves a parking ticket from the database based on vehicle registration number.
     * 
     * @param vehicleRegNumber The registration number of the vehicle
     * @return The Ticket object if found, null otherwise
     */
    public Ticket getTicket(String vehicleRegNumber) {
        Connection con = null;
        Ticket ticket = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.GET_TICKET);
            ps.setString(1,vehicleRegNumber);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                ticket = new Ticket();
                // Create parking spot object from database fields
                ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1), ParkingType.valueOf(rs.getString(6)),false);
                ticket.setParkingSpot(parkingSpot);
                // Set all ticket fields from result set
                ticket.setId(rs.getInt(2));
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(rs.getDouble(3));
                ticket.setInTime(rs.getTimestamp(4));
                ticket.setOutTime(rs.getTimestamp(5));
            }
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
        }catch (Exception ex){
            logger.error("Error fetching next available slot",ex);
        }finally {
            dataBaseConfig.closeConnection(con);
        }
        return ticket;
    }

    /**
     * Updates an existing parking ticket in the database.
     * Typically used when a vehicle exits and the final price is calculated.
     * 
     * @param ticket The ticket object with updated information
     * @return true if the update was successful, false otherwise
     */
    public boolean updateTicket(Ticket ticket) {
        Connection con = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.UPDATE_TICKET);
            // Update price and exit time
            ps.setDouble(1, ticket.getPrice());
            ps.setTimestamp(2, new Timestamp(ticket.getOutTime().getTime()));
            ps.setInt(3,ticket.getId());
            ps.execute();
            return true;
        }catch (Exception ex){
            logger.error("Error saving ticket info",ex);
        }finally {
            dataBaseConfig.closeConnection(con);
        }
        return false;
    }

    /**
     * Counts the number of completed parking sessions for a vehicle.
     * Used to determine if a vehicle owner is a recurring user for discount purposes.
     * Only counts tickets where the vehicle has already left (OUT_TIME is not null).
     * 
     * @param vehicleRegNumber The registration number of the vehicle
     * @return The number of completed parking sessions for the vehicle
     */
    public int getNbTicket(String vehicleRegNumber) {
        Connection con = null;
        int nbTickets = 0;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(
                "SELECT COUNT(*) as nb FROM ticket WHERE VEHICLE_REG_NUMBER=? AND OUT_TIME IS NOT NULL"
            );
            ps.setString(1, vehicleRegNumber);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                nbTickets = rs.getInt("nb");
            }
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
        } catch(Exception ex) {
            logger.error("Error fetching number of tickets",ex);
        } finally {
            dataBaseConfig.closeConnection(con);
        }
        return nbTickets;
    }
}
