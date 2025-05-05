package com.easygofly.api.flight;

import java.io.IOException;
import java.math.BigInteger;
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
import com.easygofly.entity.SearchHistory;
import com.easygofly.entity.TravellerDetail;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.lowagie.text.Document;

@RestController
public class OrderRestController {
	@Autowired
	private OrderRepository orderRepo;
	@Autowired
	private CustomerRepository customerRepo;
	@Autowired
	private TravellerRepository travellerRepo;
	@Autowired
	private ProductDetailCrudRepository productDetailCrudRepo;
	@Autowired
	private OrderService orderService;
	@Autowired
	private SearchHistoryRepository searchHistoryRepo;
	@Autowired
	private OrderPDFExporter exporter;
	
	@PostMapping("/api/flight/save_order")
	public String saveOrderDetail(HttpServletRequest request, HttpServletResponse response)
			throws IOException, Exception {
		response.setContentType("application/json");

		SaveOrder saveOrder = new ObjectMapper().readValue(request.getInputStream(), SaveOrder.class);
		Customer existingCustomer = customerRepo.findById(saveOrder.user_id).get();
		Date createdTime = new Date();
		PaymentMethod newPayment = (saveOrder.payment_method.equals("PayGroupEnum.wallet")) ? PaymentMethod.WALLET : PaymentMethod.PAYMENT_GATEWAY;

		List<TravellerDetail> listPax = new ArrayList<TravellerDetail>();
		List<ProductDetail> flightList = new ArrayList<ProductDetail>();

		saveOrder.travellers.forEach(traveller -> {
			TravellerDetail pax = travellerRepo.findById(traveller.id).get();
			listPax.add(pax);
		});

		saveOrder.flights.forEach(traveller -> {
			ProductDetail flight = productDetailCrudRepo.findById(traveller.id).get();
			flightList.add(flight);
		});

		Order newOrder = new Order();
		newOrder.setName(saveOrder.name);
		newOrder.setFirstName(existingCustomer.getFullName());
		newOrder.setLastName(existingCustomer.getLastName());
		newOrder.setPhoneNumber(new BigInteger(saveOrder.phone));
		newOrder.setContactEmail(saveOrder.email);
		newOrder.setCreatedTime(createdTime);
		newOrder.setAddressLine1(saveOrder.address_line1);
		newOrder.setAddressLine2(saveOrder.address_line2);
		newOrder.setOrderStatus(OrderStatus.NEW);
		newOrder.setPassengerNum(saveOrder.passenger_num);
		newOrder.setPaymentMethod(newPayment);
		newOrder.setPostalCode(saveOrder.postal_code);
		newOrder.setCouponCode(saveOrder.coupon_code);
		newOrder.setTravellerDetails(listPax);
		newOrder.setPrice(saveOrder.total_amount);
		newOrder.setCustomer(existingCustomer);
		newOrder.setProductDetails(flightList);

		Order savedOrder = orderRepo.save(newOrder);

		savedOrder.getTravellerDetails().forEach(traveller -> {
			traveller.setOrder(savedOrder);
			travellerRepo.save(traveller);
		});

		savedOrder.getProductDetails().forEach(productDetail -> {
			productDetail.setOrderId(savedOrder);
			productDetailCrudRepo.save(productDetail);
		});

		String orderBody = "{" + "\"id\": " + savedOrder.getId() + "" + "}";

		System.out.println("Code is working");

		String responseBody = "{" + "\"code\": 0, " + "\"msg\": \"New Order Response.\", " + "\"data\": " + orderBody
				+ "" + "}";

		return responseBody;
	}

	@PostMapping("/api/flight/order/ticket")
	public String getFlightOrder(HttpServletRequest request, HttpServletResponse response)
			throws IOException, Exception {
		response.setContentType("application/json");

		BookingRequest bookingRequest = new ObjectMapper().readValue(request.getInputStream(), BookingRequest.class);
		Customer existingCustomer = customerRepo.findById(bookingRequest.userId).get();
		Order existingOrder = orderRepo.findById(bookingRequest.orderId).get();
		ProductDetail flight = productDetailCrudRepo.findById(existingOrder.getProductDetails().get(0).getId()).get();
		SearchHistory searchHistory = searchHistoryRepo.findById(bookingRequest.searchId).get();

		PaymentMethod paymentMethod = bookingRequest.paymentMethod.equals("wallet") ? PaymentMethod.WALLET
				: PaymentMethod.PAYMENT_GATEWAY;
		
		String error = "{\r\n"
				+ "	\"Response\": {\r\n"
				+ "		\"ResponseStatus\": -1,\r\n"
				+ "		\"CurrentStatus\": -1,\r\n"
				+ "		\"Error\": {\r\n"
				+ "			\"ErrorCode\": -1,\r\n"
				+ "			\"ErrorMessage\": \"The order has been cancelled.\"\r\n"
				+ "		}\r\n"
				+ "	}\r\n"
				+ "}";
		
		String respnseBody = (existingOrder.getOrderStatus().equals(OrderStatus.SUCCESSFULL)) ? (flight.isLcc()) ? orderService.ticketDetails(existingCustomer, existingOrder, flight,
				bookingRequest.fareBreakdowns, searchHistory, paymentMethod, bookingRequest.traceId) : orderService.bookingDetails(existingCustomer, existingOrder, flight,
						bookingRequest.fareBreakdowns, bookingRequest.fare, searchHistory, paymentMethod,
						bookingRequest.traceId) : (paymentMethod.equals(PaymentMethod.WALLET)) ? (flight.isLcc()) ? orderService.ticketDetails(existingCustomer, existingOrder, flight,
								bookingRequest.fareBreakdowns, searchHistory, paymentMethod, bookingRequest.traceId) : orderService.bookingDetails(existingCustomer, existingOrder, flight,
										bookingRequest.fareBreakdowns, bookingRequest.fare, searchHistory, paymentMethod,
										bookingRequest.traceId) : error;

		return respnseBody;
	}

	@PostMapping("/api/flight/order_offline/ticket")
	public String getFlightOrderOffline(HttpServletRequest request, HttpServletResponse response)
			throws IOException, Exception {
		response.setContentType("application/json");

		BookingOfflineRequest bookingRequest = new ObjectMapper().readValue(request.getInputStream(),
				BookingOfflineRequest.class);
		Customer existingCustomer = customerRepo.findById(bookingRequest.userId).get();
		Order existingOrder = orderRepo.findById(bookingRequest.orderId).get();
		ProductDetail flight = productDetailCrudRepo.findById(existingOrder.getProductDetails().get(0).getId()).get();

		PaymentMethod paymentMethod = bookingRequest.paymentMethod.equals("wallet") ? PaymentMethod.WALLET
				: PaymentMethod.PAYMENT_GATEWAY;

		Integer seatCount = Integer.parseInt(flight.getTotalSeats()) - existingOrder.getTravellerDetails().size();

		flight.setTotalSeats(seatCount.toString());
		productDetailCrudRepo.save(flight);

		existingOrder.setPaymentMethod(paymentMethod);
		existingOrder.setOrderStatus(OrderStatus.SUCCESSFULL);
		orderService.updateBookingId(existingOrder, flight.getPnr());

		if (paymentMethod == PaymentMethod.WALLET) {
			orderService.walletPayOrder(existingCustomer, existingOrder);
		}

		String orderBody = orderService.showUpdateOrderByTicket(existingOrder);

		System.out.println("Response: " + orderBody);

		// Final Response JSON
		String responseBody = "{" + "\"code\": 0, " + "\"msg\": \"Updated Order Response With Final Payment.\", "
				+ "\"data\": " + orderBody + "}";

		return responseBody;
	}

	@PostMapping("/api/flight/show_order")
	public String showOrderDetail(HttpServletRequest request, HttpServletResponse response)
			throws IOException, Exception {
		response.setContentType("application/json");

		ShowOrder showOrder = new ObjectMapper().readValue(request.getInputStream(), ShowOrder.class);
		Customer existingCustomer = customerRepo.findById(showOrder.id).get();
		List<Order> orders = existingCustomer.getOrders();
		List<String> orderList = new ArrayList<String>();

		for (Order order : orders) {
			String orderBody = orderService.showUpdateOrderByTicket(order);
			orderList.add(orderBody);
		}

		String arrayOrderList = orderList.stream().map(val -> String.valueOf(val))
				.collect(Collectors.joining(",", "[", "]"));

		String responseBody = "{" + "\"code\": 0, " + "\"msg\": \"Show Flight Order List\", " + "\"data\": "
				+ arrayOrderList + "" + "}";

		return responseBody;
	}

	//// Export to PDF

	@PostMapping("/api/flight/order/export_pdf")
	public void exportToPDF(HttpServletRequest request, HttpServletResponse response) throws Exception {
		PdfOrder saveOrder = new ObjectMapper().readValue(request.getInputStream(), PdfOrder.class);
		Order order = orderRepo.findById(saveOrder.id).get();
		
		exporter.export(order, response);
	}

	// Static POJO List

	@SuppressWarnings("unused")
	private static class PdfOrder{
		private Integer id;

		public PdfOrder() {}

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}
		
	}
	
	@SuppressWarnings("unused")
	private static class SaveOrder {
		private String name;
		private String address_line1;
		private String address_line2;
		private Integer passenger_num;
		private String payment_method;
		private String postal_code;
		private String coupon_code;
		private List<CountId> travellers;
		private List<CountId> flights;
		private String phone;
		private String email;
		private double total_amount;
		private Integer user_id;

		public SaveOrder() {
		}

		public String getPhone() {
			return phone;
		}

		public void setPhone(String phone) {
			this.phone = phone;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

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

		public List<CountId> getTravellers() {
			return travellers;
		}

		public void setTravellers(List<CountId> travellers) {
			this.travellers = travellers;
		}

		public List<CountId> getFlights() {
			return flights;
		}

		public void setFlights(List<CountId> flights) {
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
	private static class CountId {
		private Integer id;

		public CountId() {
		}

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
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

	public static class BookingRequest {

		public Integer userId;

		public Integer orderId;

		public Integer searchId;

		public String paymentMethod;

		public String traceId;

		public List<FareBreakdown> fareBreakdowns;

		public Fare fare;

		public BookingRequest() {
		}

		public Fare getFare() {
			return fare;
		}

		public void setFare(Fare fare) {
			this.fare = fare;
		}

		public Integer getUserId() {
			return userId;
		}

		public void setUserId(Integer userId) {
			this.userId = userId;
		}

		public Integer getOrderId() {
			return orderId;
		}

		public void setOrderId(Integer orderId) {
			this.orderId = orderId;
		}

		public Integer getSearchId() {
			return searchId;
		}

		public void setSearchId(Integer searchId) {
			this.searchId = searchId;
		}

		public String getPaymentMethod() {
			return paymentMethod;
		}

		public void setPaymentMethod(String paymentMethod) {
			this.paymentMethod = paymentMethod;
		}

		public String getTraceId() {
			return traceId;
		}

		public void setTraceId(String traceId) {
			this.traceId = traceId;
		}

		public List<FareBreakdown> getFareBreakdowns() {
			return fareBreakdowns;
		}

		public void setFareBreakdowns(List<FareBreakdown> fareBreakdowns) {
			this.fareBreakdowns = fareBreakdowns;
		}

	}

	public static class FareBreakdown {

		private Integer passengerType;
		private String currency;
		private Double baseFare;
		private Double tax;
		private Integer passengerCount;
		private Double additionalTxnFeePub;
		private Double additionalTxnFeeOfrd;
		private Integer yQTax;

		// Default constructor
		public FareBreakdown() {
		}

		// Getters and setters
		public Integer getPassengerType() {
			return passengerType;
		}

		public void setPassengerType(Integer passengerType) {
			this.passengerType = passengerType;
		}

		public String getCurrency() {
			return currency;
		}

		public void setCurrency(String currency) {
			this.currency = currency;
		}

		public Double getBaseFare() {
			return baseFare;
		}

		public void setBaseFare(Double baseFare) {
			this.baseFare = baseFare;
		}

		public Double getTax() {
			return tax;
		}

		public void setTax(Double tax) {
			this.tax = tax;
		}

		public Integer getPassengerCount() {
			return passengerCount;
		}

		public void setPassengerCount(Integer passengerCount) {
			this.passengerCount = passengerCount;
		}

		public Double getAdditionalTxnFeePub() {
			return additionalTxnFeePub;
		}

		public void setAdditionalTxnFeePub(Double additionalTxnFeePub) {
			this.additionalTxnFeePub = additionalTxnFeePub;
		}

		public Double getAdditionalTxnFeeOfrd() {
			return additionalTxnFeeOfrd;
		}

		public void setAdditionalTxnFeeOfrd(Double additionalTxnFeeOfrd) {
			this.additionalTxnFeeOfrd = additionalTxnFeeOfrd;
		}

		public Integer getYQTax() {
			return yQTax;
		}

		public void setYQTax(Integer yQTax) {
			this.yQTax = yQTax;
		}
	}

	public static class Fare {

		public double Discount;
		public double TdsOnIncentive;
		public double TdsOnCommission;
		public double TdsOnPLB;
		public double OtherCharges;
		public double OfferedFare;
		public double PublishedFare;
		public double ServiceFee;

		public Fare() {
		}

		public double getDiscount() {
			return Discount;
		}

		public void setDiscount(double discount) {
			Discount = discount;
		}

		public double getTdsOnIncentive() {
			return TdsOnIncentive;
		}

		public void setTdsOnIncentive(double tdsOnIncentive) {
			TdsOnIncentive = tdsOnIncentive;
		}

		public double getTdsOnCommission() {
			return TdsOnCommission;
		}

		public void setTdsOnCommission(double tdsOnCommission) {
			TdsOnCommission = tdsOnCommission;
		}

		public double getTdsOnPLB() {
			return TdsOnPLB;
		}

		public void setTdsOnPLB(double tdsOnPLB) {
			TdsOnPLB = tdsOnPLB;
		}

		public double getOtherCharges() {
			return OtherCharges;
		}

		public void setOtherCharges(double otherCharges) {
			OtherCharges = otherCharges;
		}

		public double getOfferedFare() {
			return OfferedFare;
		}

		public void setOfferedFare(double offeredFare) {
			OfferedFare = offeredFare;
		}

		public double getPublishedFare() {
			return PublishedFare;
		}

		public void setPublishedFare(double publishedFare) {
			PublishedFare = publishedFare;
		}

		public double getServiceFee() {
			return ServiceFee;
		}

		public void setServiceFee(double serviceFee) {
			ServiceFee = serviceFee;
		}

	}

	public static class BookingOfflineRequest {

		public Integer userId;

		public Integer orderId;

		public String paymentMethod;

		public BookingOfflineRequest() {
		}

		public Integer getUserId() {
			return userId;
		}

		public void setUserId(Integer userId) {
			this.userId = userId;
		}

		public Integer getOrderId() {
			return orderId;
		}

		public void setOrderId(Integer orderId) {
			this.orderId = orderId;
		}

		public String getPaymentMethod() {
			return paymentMethod;
		}

		public void setPaymentMethod(String paymentMethod) {
			this.paymentMethod = paymentMethod;
		}

	}
}
