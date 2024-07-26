package com.parkit.parkingsystem.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
	private static FareCalculatorService fareCalculatorService;
    
    ParkingService mockParking = Mockito.mock(ParkingService.class);
    private static final Logger logger = LogManager.getLogger("ParkingDataBaseIT");

    
    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        ticketDAO = new TicketDAO();
		fareCalculatorService = new FareCalculatorService();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
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

    @Test
    public void testParkingACar() throws Exception{
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        
        /* create parking spot */
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
        
        /* mock the parkingService function  */
        Mockito.when(mockParking.getNextParkingNumberIfAvailable()).thenReturn(parkingSpot);

        parkingService.processIncomingVehicle();
        
        /* check if ticket had been save */
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticket);

        /* check if parkingSpot availability is false */
        Boolean available = ticket.getParkingSpot().isAvailable();
        assertEquals(available, false);        
        
    }

    @Test
    public void testParkingLotExit() throws Exception{
		testParkingACar();
       ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
       parkingService.processExitingVehicle();

		/* check if ticket price and outTime ticket had been poopulated */
		Ticket ticket = ticketDAO.getTicket("ABCDEF");
		assertEquals(ticket.getPrice(), 0);
		assertNotNull(ticket.getOutTime());
    }

}
