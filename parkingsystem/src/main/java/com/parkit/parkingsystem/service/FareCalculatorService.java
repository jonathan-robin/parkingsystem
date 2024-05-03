package com.parkit.parkingsystem.service;


import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.*;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

	//Logger logger = LoggerManager.getLogger(FareCalculatorService.class);
	private static final Logger LOGGER = LogManager.getLogger();
	
	TicketDAO ticketDAO = new TicketDAO();
	
    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long diff = ticket.getOutTime().getTime() - ticket.getInTime().getTime();
        long durationInMinutes = TimeUnit.MILLISECONDS.toSeconds(diff) / 60;
        
        double durationWithFreeTime = (double) ((durationInMinutes - 30) > 0 ? (durationInMinutes - 30) : 0);

        /* apply 5% off if customer is found in db */
        ticket = ticketDAO.applyFivePercentOff(ticket);
        
        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
            	FareCalculatorService.LOGGER.info(durationWithFreeTime / 60 );
                ticket.setPrice((durationWithFreeTime / 60 ) * Fare.CAR_RATE_PER_HOUR);
                break;
            }
            case BIKE: {
                ticket.setPrice((durationWithFreeTime /60) * Fare.BIKE_RATE_PER_HOUR);
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
    }
}