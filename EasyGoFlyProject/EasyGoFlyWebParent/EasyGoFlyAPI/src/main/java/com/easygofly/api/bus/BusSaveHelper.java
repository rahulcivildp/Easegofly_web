package com.easygofly.api.bus;

import org.springframework.stereotype.Service;

import com.easygofly.entity.Bus;
import com.easygofly.entity.BusSeat;

@Service
public class BusSaveHelper {

	public Bus setPax(Bus bus, String title, String firstName, String lastName, String phoneNo, String email, String idNumber,
			String idType, Integer gender, Integer age, Integer seatId, boolean leadPassenger, String address) {
		if(bus != null ) {
			
			bus.addPax(title, firstName, lastName, phoneNo, email, idNumber, idType, gender, age, seatId, leadPassenger, address);
			
		}
		return bus;
	}
	
	public Bus setSeats(BusSeat seat, Bus bus) {
		if(bus != null ) {
			
			bus.addSeats(seat.getColumnNo(), seat.getRowNo(), seat.getHeight(), seat.getWidth(), seat.getSeatType(), seat.getSeatName(), seat.getSeatIndex(), seat.getSeatFare(), 
					seat.isLadiesSeat(), seat.isMalesSeat(), seat.isSeatStatus(), "INR", seat.getTax(), seat.getDiscount(), seat.getBasePrice(), seat.getPublishedPrice(), seat.getOtherCharges(), 
					seat.getOfferedPrice(), seat.getPublishedPriceRoundedOff(), seat.getOfferedPriceRoundedOff(), seat.getAgentCommission(), seat.getAgentMarkUp(), seat.getTds(), 
					seat.getcGSTAmount(), seat.getcGSTRate(), seat.getCessAmount(), seat.getCessRate(), seat.getiGSTAmount(), seat.getiGSTRate(), seat.getsGSTAmount(), seat.getsGSTRate(), 
					seat.getTaxableAmount());
			
		}
		return bus;
	}
}
