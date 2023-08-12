package com.easygofly.site.flight;

import java.math.BigInteger;
import java.util.Date;

import com.easygofly.entity.CartItem;
import com.easygofly.entity.Customer;
import com.easygofly.entity.ProductDetail;
import com.easygofly.entity.TravellerDetail;

public class ProductSaveHelper {
	
	public static void setSearchHistory(Customer customer, String cityOne, String cityTwo, Integer passengerNum, String journeyClass, Integer adultNum, Integer childNum, Integer infantNum,
			String tripType, Date date ) {
		if(cityOne == null || cityTwo == null) return;
		
		Integer passengerNum1 = passengerNum; 
		String journeyClass1 = journeyClass; 
		Integer adultNum1 = adultNum; 
		Integer childNum1 = childNum; 
		Integer infantNum1 = infantNum;
		String tripType1 = tripType; 
		Date date1 = date;
		String cityOne1 = cityOne;
		String cityTwo1 = cityTwo;
			
		customer.addSerchHistory(cityOne1, cityTwo1, passengerNum1, journeyClass1, adultNum1, childNum1, infantNum1, tripType1, date1);
			
	}
	
	public static void setSearchHistoryReturn(Customer customer, String cityOne, String cityTwo, Integer passengerNum, String journeyClass, Integer adultNum, Integer childNum, Integer infantNum,
			String tripType, Date date, Date returnDate ) {
		if(cityOne == null || cityTwo == null) return;
		
		Integer passengerNum1 = passengerNum; 
		String journeyClass1 = journeyClass; 
		Integer adultNum1 = adultNum; 
		Integer childNum1 = childNum; 
		Integer infantNum1 = infantNum;
		String tripType1 = tripType; 
		Date date1 = date;
		Date returnDate1 = returnDate;
		String cityOne1 = cityOne;
		String cityTwo1 = cityTwo;
			
		customer.addSerchHistoryReturn(cityOne1, cityTwo1, passengerNum1, journeyClass1, adultNum1, childNum1, infantNum1, tripType1, date1, returnDate1);
			
	}
	
	public static void setContactBooking(Customer customer, ProductDetail flight, int quantity, double totalPrice, String email, BigInteger phoneNum) {
		if(flight == null || customer == null) return;
		
		
		flight.addContactBooking(customer, quantity, totalPrice, email, phoneNum);
		
	}
	
	public static void setTravellerDetail(String salutation, String firstName, String lastName, Date dob, ProductDetail flight, CartItem cartItem) {
		if(firstName == null || lastName == null) return;
		
		flight.addTravellerDetails(salutation, firstName, lastName, dob, cartItem);
	}
	
	public static void setTravellerDetail(String salutation, String firstName, String lastName, Date dob, ProductDetail flight, CartItem cartItem, String paxType, Integer baggageWT, Integer cabinBaggage) {
		if(firstName == null || lastName == null) return;
		
		flight.addTravellerDetails(salutation, firstName, lastName, dob, cartItem, paxType, baggageWT, cabinBaggage);
	}
	
	public static void setMealDetail(String name, String price, String code, String quantity, TravellerDetail travellerDetail) {
		if(code == null || name == null) return;
		
		travellerDetail.addMeal(name, price, code, quantity);
	}
	
	public static void setBaggageDetail(String price, String code, String weight, TravellerDetail travellerDetail) {
		if(code == null || weight == null) return;
		
		travellerDetail.addBaggage(price, code, weight);
	}
	
	public static void setSeatDetail(String price, Integer compartment, Integer availablityType, Integer deck, String rowNo,
			String code, Integer seatType, String seatNo, String craftType, TravellerDetail travellerDetail) {
		if(code == null || price == null) return;
		
		travellerDetail.addSeat(price, compartment, availablityType, deck, rowNo, code, seatType, seatNo, craftType);
	}
}
