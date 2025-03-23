package com.easygofly.api.flight;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easygofly.api.customer.CustomerRepository;
import com.easygofly.entity.Customer;
import com.easygofly.entity.Order;
import com.easygofly.entity.OrderStatus;
import com.easygofly.entity.PaymentMethod;
import com.easygofly.entity.ProductDetail;
import com.easygofly.entity.TravellerDetail;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class OrderRestController {
	@Autowired private OrderRepository orderRepo;
	@Autowired private CustomerRepository customerRepo;
	@Autowired private TravellerRepository travellerRepo;
	

	@PostMapping("/api/flight/save_order")
    public String saveOrderDetail(HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
        response.setContentType("application/json");

        SaveOrder saveOrder = new ObjectMapper().readValue(request.getInputStream(), SaveOrder.class);
        Customer existingCustomer = customerRepo.findById(saveOrder.user_id).get();
        Date createdTime = new Date();
        PaymentMethod newPayment = PaymentMethod.WALLET;
        
        if (saveOrder.payment_method.equals("PayGroupEnum.wallet")) {
        	newPayment = PaymentMethod.WALLET;
		} else {
        	newPayment = PaymentMethod.PAYMENT_GATEWAY;
		}
        
        String strPax = saveOrder.travellers.replace("[", "");
        String rmStrPax = strPax.replace("]", "");
        String[] arrPax = rmStrPax.split(", ");
        List<TravellerDetail> listPax = new ArrayList<TravellerDetail>();
        
        for (int i = 0; i < arrPax.length; i++) {
			TravellerDetail pax = travellerRepo.findById(Integer.parseInt(arrPax[i])).get();
			listPax.add(pax);
		}
        
        Order newOrder = new Order();
        newOrder.setName(saveOrder.name);
        newOrder.setCreatedTime(createdTime);
        newOrder.setAddressLine1(saveOrder.address_line1);
        newOrder.setAddressLine2(saveOrder.address_line2);
        newOrder.setOrderStatus(OrderStatus.NEW);
        newOrder.setPassengerNum(saveOrder.passenger_num);
        newOrder.setPaymentMethod(newPayment);
        newOrder.setPostalCode(saveOrder.postal_code);
        newOrder.setCouponCode(saveOrder.coupon_code);
        newOrder.setTravellerDetails(listPax);
        newOrder.setFlightIds(saveOrder.flights);
        newOrder.setPrice(saveOrder.total_amount);
        newOrder.setCustomer(existingCustomer);
        
        Order savedOrder = orderRepo.save(newOrder);
        
        String travelerBody =  "{"
        		+ "\"id\": " + savedOrder.getId() + ""
        		+ "}";

        System.out.println("Code is working");
        
        String responseBody = "{"
        		+ "\"code\": 0, "
        		+ "\"msg\": \"Traveler Details Id.\", "
        		+ "\"data\": " + travelerBody + ""
        		+ "}";
        

      return responseBody;
    }
	
	@PostMapping("/api/flight/show_order")
    public String showOrderDetail(HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
        response.setContentType("application/json");

        ShowOrder showOrder = new ObjectMapper().readValue(request.getInputStream(), ShowOrder.class);
        Customer existingCustomer = customerRepo.findById(showOrder.id).get();
        List<Order> orders = existingCustomer.getOrders();
		List<String> orderList = new ArrayList<String>();
        
        for (Order order : orders) {
            List<TravellerDetail> travelers = order.getTravellerDetails();
    		List<String> paxList = new ArrayList<String>();
    		ProductDetail flight = order.getProductDetail();
            
    		for (TravellerDetail pax : travelers) {

    	        String paxBody =  "{"
    	        		+ "\"id\": " + pax.getId() + ", "
    	                + "\"firstName\": \"" + pax.getFirstName() + "\", "
    	        		+ "\"lastName\": \"" + pax.getLastName() + "\", "
    	        		+ "\"salutation\": \"" + pax.getSalutation() + "\", "
    	    	        + "\"paxType\": \"" + pax.getPaxType() + "\", "
    	    	    	+ "\"passportNo\": \"" + pax.getPassportNo() + "\", "
    	    	    	+ "\"baggageWT\": \"" + pax.getBaggageWT() + "\", "
    	    	    	+ "\"cabinBaggage\": \"" + pax.getCabinBaggage() + "\", "
    	    	    	+ "\"dob\": \"" + pax.getDob() + "\", "
    	    	    	+ "\"passportExpiry\": \"" + pax.getPassportExpiry() + "\""
    	        		+ "}";	 
    	        
    	        paxList.add(paxBody);
			}
    		
           	String arrayPaxList = orderList.stream().map(val -> String.valueOf(val)).collect(Collectors.joining(",", "[", "]"));
            

	        String orderBody =  "{"
	        		+ "\"id\": " + order.getId() + ", "
	                + "\"adult_num\": " + order.getAdultNum() + ", "
	        		+ "\"child_num\": " + order.getChildNum() + ", "
	        		+ "\"infant_num\": " + order.getInfantNum() + ", "
	    	        + "\"journey_class\": \"" + order.getJourneyClass() + "\", "
	    	    	+ "\"city_one\": \"" + order.getCityOne() + "\", "
	    	    	+ "\"city_two\": \"" + order.getCityTwo() + "\", "
	    	    	+ "\"created_time\": \"" + order.getCreatedTime() + "\", "
	    	    	+ "\"order_name\": \"" + order.getName() + "\", "
	    	    	+ "\"trip_type\": \"" + order.getTripType() + "\", "
	    	    	+ "\"passenger_num\": " + order.getPassengerNum() + ", "
	    	    	+ "\"payment_type\": \"" + order.getPaymentMethod() + "\", "
	    	    	+ "\"pin\": \"" + order.getPostalCode() + "\", "
	    	    	+ "\"price\": " + order.getPrice() + ", "
	    	    	+ "\"booking_id\": \"" + order.getBookingId() + "\", "
	    	    	+ "\"status\": \"" + order.getOrderStatus() + "\", "
	    	    	+ "\"transaction_id\": \"" + order.getTransactionId() + "\", "
	    	    	+ "\"total_transaction\": \"" + order.getTotalTransaction() + "\", "
	    	    	+ "\"brand_logo\": \"" + flight.getAirlineLogo() + "\", "
	    	    	+ "\"arr_time\": \"" + flight.getArrTime() + "\", "
	    	    	+ "\"brand\": \"" + flight.getBrand() + "\", "
	    	    	+ "\"city_one\": \"" + flight.getCityOne() + "\", "
	    	    	+ "\"city_two\": \"" + flight.getCityTwo() + "\", "
	    	    	+ "\"dep_time\": \"" + flight.getDepTime() + "\", "
	    	    	+ "\"flight_num\": \"" + flight.getFlightNum() + "\", "
	    	    	+ "\"pnr\": \"" + flight.getPnr() + "\", "
	    	    	+ "\"stop_num\": " + flight.getStopNum() + ", "
	    	    	+ "\"terminal_arr\": \"" + flight.getTerminalArr() + "\", "
	    	    	+ "\"terminal_dep\": \"" + flight.getTerminalDep() + "\", "
	    	    	+ "\"date\": \"" + flight.getDate() + "\", "
	    	    	+ "\"duration\": " + flight.getDuration() + ", "
	        		+ "\"pax_list\": " + arrayPaxList + ""
	        		+ "}";
	        
	        orderList.add(orderBody);
		}

       	String arrayOrderList = orderList.stream().map(val -> String.valueOf(val)).collect(Collectors.joining(",", "[", "]"));

        String responseBody = "{"
        		+ "\"code\": 0, "
        		+ "\"msg\": \"Show Flight Order List\", "
        		+ "\"data\": " + arrayOrderList + ""
        		+ "}";

      return responseBody;
    }
	
	
	// Static POJO List
    
	@SuppressWarnings("unused")
	private static class SaveOrder {
    	private String name;
        private String address_line1;
        private String address_line2;
        private Integer passenger_num;
        private String payment_method;
        private String postal_code;
        private String coupon_code;
        private String travellers;
        private String flights;
        private double total_amount;
        private Integer user_id;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getAddress_line1() {
			return address_line1;
		}
		public void setAddress_line1(String address_line1) {
			this.address_line1 = address_line1;
		}
		public String getAddress_line2() {
			return address_line2;
		}
		public void setAddress_line2(String address_line2) {
			this.address_line2 = address_line2;
		}
		public Integer getPassenger_num() {
			return passenger_num;
		}
		public void setPassenger_num(Integer passenger_num) {
			this.passenger_num = passenger_num;
		}
		public String getPayment_method() {
			return payment_method;
		}
		public void setPayment_method(String payment_method) {
			this.payment_method = payment_method;
		}
		public String getPostal_code() {
			return postal_code;
		}
		public void setPostal_code(String postal_code) {
			this.postal_code = postal_code;
		}
		public String getCoupon_code() {
			return coupon_code;
		}
		public void setCoupon_code(String coupon_code) {
			this.coupon_code = coupon_code;
		}
		public String getTravellers() {
			return travellers;
		}
		public void setTravellers(String travellers) {
			this.travellers = travellers;
		}
		public String getFlights() {
			return flights;
		}
		public void setFlights(String flights) {
			this.flights = flights;
		}
		public double getTotal_amount() {
			return total_amount;
		}
		public void setTotal_amount(double total_amount) {
			this.total_amount = total_amount;
		}
		public Integer getUser_id() {
			return user_id;
		}
		public void setUser_id(Integer user_id) {
			this.user_id = user_id;
		}
	}
	

	@SuppressWarnings("unused")
	private static class ShowOrder {
    	private Integer id;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}
	}
    
}
