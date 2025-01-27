package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingSpotDAOTest {

    @Mock
    private DataBaseConfig dataBaseConfig;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private ParkingSpotDAO parkingSpotDAO;

    @BeforeEach
    void setUp() {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseConfig;
    }

    @Test
    void getNextAvailableSlot_WhenSpotAvailable_ReturnsSpotNumber() throws Exception {
        // Arrange
        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(2);

        // Act
        int result = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

        // Assert
        assertEquals(2, result);
        verify(preparedStatement).setString(1, ParkingType.CAR.toString());
        verify(dataBaseConfig).closeResultSet(resultSet);
        verify(dataBaseConfig).closePreparedStatement(preparedStatement);
        verify(dataBaseConfig).closeConnection(connection);
    }

    @Test
    void getNextAvailableSlot_WhenNoSpotAvailable_ReturnsMinusOne() throws Exception {
        // Arrange
        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // Act
        int result = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

        // Assert
        assertEquals(-1, result);
        verify(dataBaseConfig).closeResultSet(resultSet);
        verify(dataBaseConfig).closePreparedStatement(preparedStatement);
        verify(dataBaseConfig).closeConnection(connection);
    }

    @Test
    void getNextAvailableSlot_WhenDatabaseError_ReturnsMinusOne() throws Exception {
        // Arrange
        when(dataBaseConfig.getConnection()).thenThrow(new RuntimeException("DB Connection error"));

        // Act
        int result = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

        // Assert
        assertEquals(-1, result);
        verify(dataBaseConfig).closeConnection(null);
    }

    @Test
    void updateParking_WhenSuccessful_ReturnsTrue() throws Exception {
        // Arrange
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = parkingSpotDAO.updateParking(parkingSpot);

        // Assert
        assertTrue(result);
        verify(preparedStatement).setBoolean(1, true);
        verify(preparedStatement).setInt(2, 1);
        verify(dataBaseConfig).closePreparedStatement(preparedStatement);
        verify(dataBaseConfig).closeConnection(connection);
    }

    @Test
    void updateParking_WhenNoRowsUpdated_ReturnsFalse() throws Exception {
        // Arrange
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = parkingSpotDAO.updateParking(parkingSpot);

        // Assert
        assertFalse(result);
        verify(dataBaseConfig).closePreparedStatement(preparedStatement);
        verify(dataBaseConfig).closeConnection(connection);
    }

    @Test
    void updateParking_WhenDatabaseError_ReturnsFalse() throws Exception {
        // Arrange
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
        when(dataBaseConfig.getConnection()).thenThrow(new RuntimeException("DB Connection error"));

        // Act
        boolean result = parkingSpotDAO.updateParking(parkingSpot);

        // Assert
        assertFalse(result);
        verify(dataBaseConfig).closeConnection(null);
    }
}
