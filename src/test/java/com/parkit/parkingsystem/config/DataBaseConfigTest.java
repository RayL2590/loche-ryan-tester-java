package com.parkit.parkingsystem.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DataBaseConfigTest {

    private DataBaseConfig dataBaseConfig;

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() {
        dataBaseConfig = new DataBaseConfig();
    }

    @Test
    void getConnection_ShouldReturnValidConnection() throws SQLException, ClassNotFoundException {
        Connection connection = dataBaseConfig.getConnection();
        assertNotNull(connection);
        connection.close();
    }

    @Test
    void closeConnection_ShouldCloseConnectionProperly() throws SQLException {
        dataBaseConfig.closeConnection(mockConnection);
        verify(mockConnection, times(1)).close();
    }

    @Test
    void closeConnection_ShouldHandleNullConnection() {
        dataBaseConfig.closeConnection(null);
        // No exception should be thrown
    }

    @Test
    void closeConnection_ShouldHandleSQLException() throws SQLException {
        doThrow(new SQLException()).when(mockConnection).close();
        dataBaseConfig.closeConnection(mockConnection);
        verify(mockConnection, times(1)).close();
    }

    @Test
    void closePreparedStatement_ShouldCloseProperly() throws SQLException {
        dataBaseConfig.closePreparedStatement(mockPreparedStatement);
        verify(mockPreparedStatement, times(1)).close();
    }

    @Test
    void closePreparedStatement_ShouldHandleNullStatement() {
        dataBaseConfig.closePreparedStatement(null);
        // No exception should be thrown
    }

    @Test
    void closePreparedStatement_ShouldHandleSQLException() throws SQLException {
        doThrow(new SQLException()).when(mockPreparedStatement).close();
        dataBaseConfig.closePreparedStatement(mockPreparedStatement);
        verify(mockPreparedStatement, times(1)).close();
    }

    @Test
    void closeResultSet_ShouldCloseProperly() throws SQLException {
        dataBaseConfig.closeResultSet(mockResultSet);
        verify(mockResultSet, times(1)).close();
    }

    @Test
    void closeResultSet_ShouldHandleNullResultSet() {
        dataBaseConfig.closeResultSet(null);
        // No exception should be thrown
    }

    @Test
    void closeResultSet_ShouldHandleSQLException() throws SQLException {
        doThrow(new SQLException()).when(mockResultSet).close();
        dataBaseConfig.closeResultSet(mockResultSet);
        verify(mockResultSet, times(1)).close();
    }
}
