package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.ResultSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.mockito.Mockito;


@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    private static final Logger logger = LogManager.getLogger("TicketDAO");

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @Mock
    private static ParkingService parkingService;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    //TODO: check that a ticket is actualy saved in DB and Parking table is updated with availability
    @Test
    public void testParkingACar(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        Boolean assertTicket = false;
        Boolean assertParking = true;
        Connection con = null; 
        try {
            con = dataBaseTestConfig.getConnection();
            PreparedStatement isTicket = con.prepareStatement("SELECT * from ticket where VEHICLE_REG_NUMBER = 'ABCDEF' and OUT_TIME is null;");
            ResultSet ticketSaved = isTicket.executeQuery();
            if(ticketSaved.next()){
                assertTicket = true;
                /* parking spot */
                Integer parkingNumber = ticketSaved.getInt(1);
                PreparedStatement isParkingTableUpdated = con.prepareStatement("SELECT available from parking where PARKING_NUMBER = ?;");
                isParkingTableUpdated.setInt(1, parkingNumber);
                ResultSet parkingResult = isParkingTableUpdated.executeQuery();
                if(parkingResult.next()){
                    assertParking = parkingResult.getBoolean(1);
                }
            }
         }catch (Exception ex){
            logger.error("Error fetching next available slot",ex);
        }finally {
            dataBaseTestConfig.closeConnection(con);
        }
        assertTrue(assertTicket); 
        assertFalse(assertParking);
    }

    @Test
    public void testParkingLotExit(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        //when(ticketDAO.getNbTicket(anyString())).thenReturn(0);
        parkingService.processIncomingVehicle();
        parkingService.processExitingVehicle();

        Boolean ticketOutTime = false;
        Boolean assertParking = false;

        Connection con = null; 
        try {
            con = dataBaseTestConfig.getConnection();
            PreparedStatement isTicket = con.prepareStatement("SELECT * from ticket where VEHICLE_REG_NUMBER = 'ABCDEF' and OUT_TIME is not null;");
            ResultSet ticket = isTicket.executeQuery();
            if(ticket.next()){
                logger.info("ticket.next()");
                ticketOutTime = true;
                /* parking spot */
                Integer parkingNumber = ticket.getInt(1);
                PreparedStatement isParkingTableUpdated = con.prepareStatement("SELECT available from parking where PARKING_NUMBER = ?;");
                isParkingTableUpdated.setInt(1, parkingNumber);
                ResultSet parkingResult = isParkingTableUpdated.executeQuery();
                if(parkingResult.next()){
                    assertParking = parkingResult.getBoolean(1);
                }
            }
        }catch(Exception e){ 
            System.err.print("error catch exitProcess");
            logger.info("error"+ e);
        }finally {
            
            dataBaseTestConfig.closeConnection(con);
        }
        assertTrue(ticketOutTime); 
        assertTrue(assertParking);

        /*  */


        //TODO: check that the fare generated and out time are populated correctly in the database
    }

    public void testParkingLotExitRecurringUser(){ 
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        when(ticketDAO.getNbTicket(anyString())).thenReturn(2);

        Boolean assertTicket = false; 

        parkingService.processIncomingVehicle();
        /* check that ticket populated correctly in db */
        
        Connection con = null; 
        try {
            con = dataBaseTestConfig.getConnection();
            PreparedStatement isTicket = con.prepareStatement("SELECT * from ticket where VEHICLE_REG_NUMBER = 'ABCDEF' and OUT_TIME is null;");
            ResultSet ticketSaved = isTicket.executeQuery();
            if(ticketSaved.next()){
                assertTicket = true;
            }
        }catch(Exception e){ 
            System.err.print("error catch exitProcess");
            logger.info("error"+ e);
        }finally {
            
            dataBaseTestConfig.closeConnection(con);
        }

        assertTrue(assertTicket); 
        verify(parkingService, Mockito.times(1))
        .fareCalculatorService.calculateFare(any(Ticket.class), true);
    }



}
