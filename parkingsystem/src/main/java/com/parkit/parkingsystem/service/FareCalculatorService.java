package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        calculateFare(ticket, false);
    }

    public void calculateFare(Ticket ticket, Boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inHour = ticket.getInTime().getTime(); 
        long outHour = ticket.getOutTime().getTime();

        long duration = (outHour - inHour) / 1000 / 60;
        

        // if duration < 30 minutes, should cost 0 
        if (duration <= 30)
            duration = 0L;

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                ticket.setPrice((duration * Fare.CAR_RATE_PER_HOUR) / 60);
                break;
            }
            case BIKE: {
                ticket.setPrice((duration * Fare.BIKE_RATE_PER_HOUR) / 60);
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }

        Double price = discount ? ticket.getPrice() * 0.95 : ticket.getPrice();

        System.out.println("tickerPrice: " + price);
        ticket.setPrice(discount ? ticket.getPrice() * 0.95 : ticket.getPrice());

    }
}