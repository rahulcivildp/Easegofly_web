package com.easygofly.site.hotel;

import org.springframework.stereotype.Service;

import com.easygofly.entity.Hotel;
import com.easygofly.entity.HotelRoom;

@Service
public class HotelSaveHelper {

	public Hotel setGuests(String title, String firstName, String lastName, String phoneNo, String email, Integer paxType,
			Integer age, boolean leadPassenger, String passportNo, String passportExpDate, String passportIssueDate,
			String pan, HotelRoom room, Hotel hotel) {
		if(hotel != null ) {
			
			hotel.addGuests(title, firstName, lastName, phoneNo, email, paxType, age, leadPassenger, passportNo, passportExpDate, passportIssueDate, pan, room);
			
		}
		return hotel;
	}

	public Hotel setRooms(HotelRoom room, Hotel hotel) {
		if(hotel != null ) {
			
			hotel.addRooms(room.getRoomTypeCode(), room.getRoomIndex(), room.getRoomStatus(), room.getRoomId(), room.isRequireAllPaxDetails(), room.getRoomDescription(), room.getRoomTypeName(), 
					room.getRatePlanCode(), room.getRatePlan(), room.getRatePlanName(), room.getInfoSource(), room.getSequenceNo(), room.getChildCount(), room.getRoomPromotion(), room.getAmenities(), 
					room.getAmenity(), room.getSmokingPreference(), room.getBedTypes(), room.getHotelSupplements(), room.getLastCancellationDate(), room.getHotelCancelPolicies(), room.getRoomPrice(), 
					room.getTax(), room.getExtraGuestCharge(), room.getChildCharge(), room.getDiscount(), room.getAvailabilityType(), room.getPublishedPrice(), room.getOtherCharges(), room.getOfferedPrice(), 
					room.getPublishedPriceRoundedOff(), room.getOfferedPriceRoundedOff(), room.getAgentCommission(), room.getAgentMarkUp(), room.getServiceTax(), room.getTds(), room.getLastVoucherDate(), 
					room.getCancellationPolicy(), room.getInclusion(), room.isPassportMandatory(), room.isPANMandatory(), room.getRoomDayRates());
			
		}
		return hotel;
	}
	
}
