package com.parkit.parkingsystem;

import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.constants.ParkingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import org.junit.jupiter.api.BeforeAll;
import java.util.Date;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.model.ParkingSpot;
import java.sql.Timestamp;



@ExtendWith(MockitoExtension.class)
public class TicketDaoTest {

    private static TicketDAO ticketDAO;
    private static ParkingSpotDAO parkingSpotDAO;
    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();

   @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
    }

    @Test
    public void testGetDiscountWithNoTicket(){ 
        Boolean found = ticketDAO.getDiscount("test");
        assertFalse(found);
    }    

    @Test
    public void testGetDiscountWithTicket(){ 
        Ticket ticket = new Ticket();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);

        parkingSpot.setId(1); 
        parkingSpot.setParkingType(ParkingType.CAR);
        parkingSpot.setAvailable(true);
        
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setPrice(0);
        ticket.setInTime(new Timestamp(new Date().getTime()));
        ticket.setOutTime(new Timestamp(new Date().getTime()));

        ticketDAO.saveTicket(ticket);

        

        Boolean found = ticketDAO.getDiscount("ABCDEF");
        assertTrue(found);
    }   
}
