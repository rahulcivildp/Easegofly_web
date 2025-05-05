package com.easygofly.api.driver;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easygofly.api.customer.CustomerRepository;
import com.easygofly.entity.Customer;
import com.easygofly.entity.Driver;
import com.easygofly.entity.OrderStatus;
import com.easygofly.entity.RideHistory;
import com.easygofly.entity.RideOrder;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class DriverRestController {
	@Autowired
	private DriverRepository driverRepo;
	@Autowired
	private RideHistoryRepository rideHistoryRepo;
	@Autowired
	private RideOrderRepository rideOrderRepo;
	@Autowired
	private CustomerRepository customerRepo;
	@Autowired
	private DriverRestService driverRestService;

	@GetMapping("/drivers/list")
	public String rideList(HttpServletResponse response) throws IOException {
		response.setContentType("application/json");

		Iterable<Driver> driverList = driverRepo.findAll();
		List<String> strDriverList = new ArrayList<String>();

		for (Driver driver : driverList) {

			String driverJson = "{\"id\": " + driver.getId() + ", \"name\": \"" + driver.getName() + "\", "
					+ "\"driverPhoto\": \"" + driver.getPhotosImagePath() + "\", \"rating\": " + driver.getRating()
					+ ", \"experience\": " + driver.getExperience() + ", \"location\": \""
					+ driver.getLocation() + "\", \"contact\": \"" + driver.getContact() + "\", "
					+ "\"address\": \"" + driver.getAddress() + "\", \"coveringDistance\": "
					+ driver.getCoveringDistance() + ", \"latitude\": " + driver.getLatitude() + ", "
					+ "\"longitude\": " + driver.getLongitude() + ", \"cab\": {\"id\": "
					+ driver.getCab().getId() + ", \"name\": \"" + driver.getCab().getName() + "\", "
					+ "\"cabPhoto\": \"" + driver.getCab().getPhotosImagePath() + "\", \"type\": \""
					+ driver.getCab().getType() + "\", \"seating\": " + driver.getCab().getSeating() + ", "
					+ "\"bookingFare\": " + driver.getCab().getBookingFare() + ", \"fuelType\": \""
					+ driver.getCab().getFuelType() + "\", \"color\": \"" + driver.getCab().getColor() + "\", "
					+ "\"maxSpeed\": " + driver.getCab().getMaxSpeed() + ", \"airConditioning\": \""
					+ driver.getCab().getAirConditioning() + "\", \"wifi\": \"" + driver.getCab().getWifi()
					+ "\", \"license\": \"" + driver.getCab().getLicense() + "\", \"features\": \""
					+ driver.getCab().getFeatures() + "\"}}";

			strDriverList.add(driverJson);
		}

		String arrayDriverList = strDriverList.stream().map(val -> String.valueOf(val))
				.collect(Collectors.joining(",", "[", "]"));

		String responseBody = "{\"code\": 0, \"msg\": \"List of Drivers and Cabs.\", \"data\": "
				+ arrayDriverList + "}";

		return responseBody;
	}

	@PostMapping("/drivers/history_save")
	public String userRIdeHistory(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("application/json");

		RideRequest requestRide = new ObjectMapper().readValue(request.getInputStream(), RideRequest.class);
		Customer customer = customerRepo.findById(requestRide.customerId).get();
		RideHistory rideHistory = new RideHistory(requestRide.pickupLocation, requestRide.dropoffLocation,
				requestRide.date, requestRide.time, requestRide.route, customer);
		RideHistory savedRideHistory = rideHistoryRepo.save(rideHistory);

		String historyJson = "{\"id\": " + savedRideHistory.getId() + ", \"pickupLocation\": \""
				+ savedRideHistory.getPickupLocation() + "\", \"dropoffLocation\": \""
				+ savedRideHistory.getDropoffLocation() + "\", \"date\": \"" + savedRideHistory.getDate() + "\", "
				+ "\"time\": \"" + savedRideHistory.getTime() + "\", \"route\": " + savedRideHistory.getRoute()
				+ "}";

		String responseBody = "{\"code\": 0, \"msg\": \"Ride History.\", \"data\": " + historyJson + ""
				+ "}";

		return responseBody;
	}

	@PostMapping("/drivers/order_save")
	public String userRIdeOrder(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("application/json");

		RideOrderRequest requestOrder = new ObjectMapper().readValue(request.getInputStream(), RideOrderRequest.class);
		Driver driver = driverRepo.findById(requestOrder.cabId).get();
		Customer customer = customerRepo.findById(requestOrder.customerId).get();
		RideHistory rideHistory = rideHistoryRepo.findById(requestOrder.historyId).get();

		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		String formattedDate = sdf.format(date);
		OrderStatus status = OrderStatus.NEW;

		String orderString = "TRN" + formattedDate + "ID" + customer.getId() + "ID" + driver.getId();

		RideOrder rideOrder = new RideOrder(formattedDate, orderString, status, requestOrder.totalAmount,
				requestOrder.baseFare, requestOrder.taxes, requestOrder.discount, requestOrder.convenience,
				requestOrder.paxNum, requestOrder.cabNum, requestOrder.phone, driver);

		rideOrder.setHistoryId(rideHistory);
		rideOrder.setCustomer(customer);

		RideOrder savedRideOrder = rideOrderRepo.save(rideOrder);

		String rideOrderJson = "{\"id\": " + savedRideOrder.getId() + ", \"orderName\": \""
				+ savedRideOrder.getOrderName() + "\", \"totalAmount\": " + savedRideOrder.getTotalAmount() + ", "
				+ "\"baseFare\": " + savedRideOrder.getBaseFare() + ", \"taxes\": " + savedRideOrder.getTaxes()
				+ ", \"discount\": " + savedRideOrder.getDiscount() + ", \"convenience\": "
				+ savedRideOrder.getConvenience() + ", \"createdTime\": \"" + savedRideOrder.getDate() + "\", "
				+ "\"status\": \"" + savedRideOrder.getStatus() + "\", \"paxNum\": " + savedRideOrder.getPaxNum()
				+ ", \"cabNum\": " + savedRideOrder.getCabNum() + ", \"phone\": \""
				+ savedRideOrder.getPhone() + "\", \"driver\": {\"id\": "
				+ savedRideOrder.getDriverId().getId() + ", \"name\": \"" + savedRideOrder.getDriverId().getName()
				+ "\", \"rating\": " + savedRideOrder.getDriverId().getRating() + ", \"experience\": "
				+ savedRideOrder.getDriverId().getExperience() + ", \"location\": \""
				+ savedRideOrder.getDriverId().getLocation() + "\", \"contact\": \""
				+ savedRideOrder.getDriverId().getContact() + "\", \"address\": \""
				+ savedRideOrder.getDriverId().getAddress() + "\", \"coveringDistance\": "
				+ savedRideOrder.getDriverId().getCoveringDistance() + ", \"latitude\": "
				+ savedRideOrder.getDriverId().getLatitude() + ", \"longitude\": "
				+ savedRideOrder.getDriverId().getLongitude() + ", \"driverPhoto\": \""
				+ savedRideOrder.getDriverId().getPhotosImagePath() + "\", \"cab\": {\"id\": "
				+ savedRideOrder.getDriverId().getCab().getId() + ", \"name\": \""
				+ savedRideOrder.getDriverId().getCab().getName() + "\", \"cabPhoto\": \""
				+ savedRideOrder.getDriverId().getCab().getPhotosImagePath() + "\", \"type\": \""
				+ savedRideOrder.getDriverId().getCab().getType() + "\", \"seating\": "
				+ savedRideOrder.getDriverId().getCab().getSeating() + ", \"bookingFare\": "
				+ savedRideOrder.getDriverId().getCab().getBookingFare() + ", \"fuelType\": \""
				+ savedRideOrder.getDriverId().getCab().getFuelType() + "\", \"color\": \""
				+ savedRideOrder.getDriverId().getCab().getColor() + "\", \"maxSpeed\": "
				+ savedRideOrder.getDriverId().getCab().getMaxSpeed() + ", \"airConditioning\": \""
				+ savedRideOrder.getDriverId().getCab().getAirConditioning() + "\", \"wifi\": \""
				+ savedRideOrder.getDriverId().getCab().getWifi() + "\", \"license\": \""
				+ savedRideOrder.getDriverId().getCab().getLicense() + "\", \"features\": \""
				+ savedRideOrder.getDriverId().getCab().getFeatures() + "\"}}}";

		String responseBody = "{\"code\": 0, \"msg\": \"Ride Order.\", \"data\": " + rideOrderJson + ""
				+ "}";

		return responseBody;
	}

	@PostMapping("/drivers/order_update")
	public String userRIdeOrderUpdateWallet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, MessagingException {
		response.setContentType("application/json");

		UpdateRequest requestOrder = new ObjectMapper().readValue(request.getInputStream(), UpdateRequest.class);
		RideOrder rideOrder = rideOrderRepo.findById(requestOrder.orderId).get();
		RideOrder savedRideOrder = new RideOrder();
		List<RideOrder> rideOrders = new ArrayList<RideOrder>();
		rideOrders.add(rideOrder);
		Customer customer = customerRepo.findById(requestOrder.customerId).get();
		Double totalAmount = rideOrder.getTotalAmount() * 100;
		Integer totalAmountInt = totalAmount.intValue();

		if ((customer.getWallet().getBalance() / 100) > rideOrder.getTotalAmount()) {

			rideOrder.setStatus(OrderStatus.SUCCESSFULL);
			driverRestService.createTotalTransactionRide(customer, rideOrder.getTotalAmount(), true, false, rideOrders,
					null, OrderStatus.SUCCESSFULL);
			driverRestService.deductWalletValue(customer, totalAmountInt);
			savedRideOrder = rideOrderRepo.save(rideOrder);
			driverRestService.sendSuccessEmail(customer, null, true, rideOrder);
		} else {
			rideOrder.setStatus(OrderStatus.FAILED);
			driverRestService.createTotalTransactionRide(customer, rideOrder.getTotalAmount(), true, false, rideOrders,
					null, OrderStatus.FAILED);

			savedRideOrder = rideOrderRepo.save(rideOrder);
			driverRestService.sendSuccessEmail(customer, null, false, rideOrder);
		}

		String rideOrderJson = "{\"id\": " + savedRideOrder.getId() + ", \"orderName\": \""
				+ savedRideOrder.getOrderName() + "\", \"totalAmount\": " + savedRideOrder.getTotalAmount() + ", "
				+ "\"baseFare\": " + savedRideOrder.getBaseFare() + ", \"taxes\": " + savedRideOrder.getTaxes()
				+ ", \"discount\": " + savedRideOrder.getDiscount() + ", \"convenience\": "
				+ savedRideOrder.getConvenience() + ", \"createdTime\": \"" + savedRideOrder.getDate() + "\", "
				+ "\"status\": \"" + savedRideOrder.getStatus() + "\", \"paxNum\": " + savedRideOrder.getPaxNum()
				+ ", \"cabNum\": " + savedRideOrder.getCabNum() + ", \"phone\": \""
				+ savedRideOrder.getPhone() + "\", \"driver\": {\"id\": "
				+ savedRideOrder.getDriverId().getId() + ", \"name\": \"" + savedRideOrder.getDriverId().getName()
				+ "\", \"rating\": " + savedRideOrder.getDriverId().getRating() + ", \"experience\": "
				+ savedRideOrder.getDriverId().getExperience() + ", \"location\": \""
				+ savedRideOrder.getDriverId().getLocation() + "\", \"contact\": \""
				+ savedRideOrder.getDriverId().getContact() + "\", \"address\": \""
				+ savedRideOrder.getDriverId().getAddress() + "\", \"coveringDistance\": "
				+ savedRideOrder.getDriverId().getCoveringDistance() + ", \"latitude\": "
				+ savedRideOrder.getDriverId().getLatitude() + ", \"longitude\": "
				+ savedRideOrder.getDriverId().getLongitude() + ", \"driverPhoto\": \""
				+ savedRideOrder.getDriverId().getPhotosImagePath() + "\", \"cab\": {\"id\": "
				+ savedRideOrder.getDriverId().getCab().getId() + ", \"name\": \""
				+ savedRideOrder.getDriverId().getCab().getName() + "\", \"cabPhoto\": \""
				+ savedRideOrder.getDriverId().getCab().getPhotosImagePath() + "\", \"type\": \""
				+ savedRideOrder.getDriverId().getCab().getType() + "\", \"seating\": "
				+ savedRideOrder.getDriverId().getCab().getSeating() + ", \"bookingFare\": "
				+ savedRideOrder.getDriverId().getCab().getBookingFare() + ", \"fuelType\": \""
				+ savedRideOrder.getDriverId().getCab().getFuelType() + "\", \"color\": \""
				+ savedRideOrder.getDriverId().getCab().getColor() + "\", \"maxSpeed\": "
				+ savedRideOrder.getDriverId().getCab().getMaxSpeed() + ", \"airConditioning\": \""
				+ savedRideOrder.getDriverId().getCab().getAirConditioning() + "\", \"wifi\": \""
				+ savedRideOrder.getDriverId().getCab().getWifi() + "\", \"license\": \""
				+ savedRideOrder.getDriverId().getCab().getLicense() + "\", \"features\": \""
				+ savedRideOrder.getDriverId().getCab().getFeatures() + "\"}}}";

		String responseBody = "{\"code\": 0, \"msg\": \"Ride Order.\", \"data\": " + rideOrderJson + ""
				+ "}";

		return responseBody;
	}

	@PostMapping("/drivers/order_update_zaakpay")
	public String userRIdeOrderUpdateZaakpay(HttpServletRequest request, HttpServletResponse response)
			throws IOException, MessagingException {
		response.setContentType("application/json");

		UpdateRequest requestOrder = new ObjectMapper().readValue(request.getInputStream(), UpdateRequest.class);
		RideOrder rideOrder = rideOrderRepo.findById(requestOrder.orderId).get();
		RideOrder savedRideOrder = new RideOrder();
		Customer customer = customerRepo.findById(requestOrder.customerId).get();
		List<RideOrder> rideOrders = customer.getRideOrders();

		for (RideOrder rideOrder2 : rideOrders) {
			if (rideOrder2 == rideOrder) {
				savedRideOrder = rideOrder2;
			}
		}
		
		Integer code = (rideOrder.getStatus().equals(OrderStatus.SUCCESSFULL)) ? 0 : -1;

		String rideOrderJson = (rideOrder.getStatus().equals(OrderStatus.SUCCESSFULL))
				? "{\"id\": " + savedRideOrder.getId() + ", \"orderName\": \"" + savedRideOrder.getOrderName()
						+ "\", \"totalAmount\": " + savedRideOrder.getTotalAmount() + ", \"baseFare\": "
						+ savedRideOrder.getBaseFare() + ", \"taxes\": " + savedRideOrder.getTaxes() + ", "
						+ "\"discount\": " + savedRideOrder.getDiscount() + ", \"convenience\": "
						+ savedRideOrder.getConvenience() + ", \"createdTime\": \"" + savedRideOrder.getDate()
						+ "\", \"status\": \"" + savedRideOrder.getStatus() + "\", \"paxNum\": "
						+ savedRideOrder.getPaxNum() + ", \"cabNum\": " + savedRideOrder.getCabNum() + ", "
						+ "\"phone\": \"" + savedRideOrder.getPhone() + "\", \"driver\": {\"id\": "
						+ savedRideOrder.getDriverId().getId() + ", \"name\": \""
						+ savedRideOrder.getDriverId().getName() + "\", \"rating\": "
						+ savedRideOrder.getDriverId().getRating() + ", \"experience\": "
						+ savedRideOrder.getDriverId().getExperience() + ", \"location\": \""
						+ savedRideOrder.getDriverId().getLocation() + "\", \"contact\": \""
						+ savedRideOrder.getDriverId().getContact() + "\", \"address\": \""
						+ savedRideOrder.getDriverId().getAddress() + "\", \"coveringDistance\": "
						+ savedRideOrder.getDriverId().getCoveringDistance() + ", \"latitude\": "
						+ savedRideOrder.getDriverId().getLatitude() + ", \"longitude\": "
						+ savedRideOrder.getDriverId().getLongitude() + ", \"driverPhoto\": \""
						+ savedRideOrder.getDriverId().getPhotosImagePath() + "\", \"cab\": {\"id\": "
						+ savedRideOrder.getDriverId().getCab().getId() + ", \"name\": \""
						+ savedRideOrder.getDriverId().getCab().getName() + "\", \"cabPhoto\": \""
						+ savedRideOrder.getDriverId().getCab().getPhotosImagePath() + "\", \"type\": \""
						+ savedRideOrder.getDriverId().getCab().getType() + "\", \"seating\": "
						+ savedRideOrder.getDriverId().getCab().getSeating() + ", \"bookingFare\": "
						+ savedRideOrder.getDriverId().getCab().getBookingFare() + ", \"fuelType\": \""
						+ savedRideOrder.getDriverId().getCab().getFuelType() + "\", \"color\": \""
						+ savedRideOrder.getDriverId().getCab().getColor() + "\", \"maxSpeed\": "
						+ savedRideOrder.getDriverId().getCab().getMaxSpeed() + ", \"airConditioning\": \""
						+ savedRideOrder.getDriverId().getCab().getAirConditioning() + "\", \"wifi\": \""
						+ savedRideOrder.getDriverId().getCab().getWifi() + "\", \"license\": \""
						+ savedRideOrder.getDriverId().getCab().getLicense() + "\", \"features\": \""
						+ savedRideOrder.getDriverId().getCab().getFeatures() + "\"}}}"
				: "{		\"Error\": {\r\n			\"ErrorCode\": " + code + ",\r\n		\"ErrorMessage\": \"The ride order has been cancelled.\"\r\n		}\r\n}";

		String responseBody = "{\"code\": " + code + ", \"msg\": \"Ride Order.\", \"data\": " + rideOrderJson + "}";
		
		System.out.println(responseBody);

		return responseBody;
	}

	// Pojo List

	@SuppressWarnings("unused")
	private static class UpdateRequest {
		private Integer orderId;
		private Integer customerId;

		public Integer getOrderId() {
			return orderId;
		}

		public void setOrderId(Integer orderId) {
			this.orderId = orderId;
		}

		public Integer getCustomerId() {
			return customerId;
		}

		public void setCustomerId(Integer customerId) {
			this.customerId = customerId;
		}

	}

	@SuppressWarnings("unused")
	private static class RideRequest {
		private String pickupLocation;
		private String dropoffLocation;
		private String date;
		private String time;
		private Double route;
		private Integer customerId;

		public String getPickupLocation() {
			return pickupLocation;
		}

		public void setPickupLocation(String pickupLocation) {
			this.pickupLocation = pickupLocation;
		}

		public String getDropoffLocation() {
			return dropoffLocation;
		}

		public void setDropoffLocation(String dropoffLocation) {
			this.dropoffLocation = dropoffLocation;
		}

		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public String getTime() {
			return time;
		}

		public void setTime(String time) {
			this.time = time;
		}

		public Double getRoute() {
			return route;
		}

		public void setRoute(Double route) {
			this.route = route;
		}

		public Integer getCustomerId() {
			return customerId;
		}

		public void setCustomerId(Integer customerId) {
			this.customerId = customerId;
		}

	}

	@SuppressWarnings("unused")
	private static class RideOrderRequest {
		private Double totalAmount;
		private Double baseFare;
		private Double taxes;
		private Double discount;
		private Double convenience;
		private Integer paxNum;
		private Integer cabNum;
		private String phone;
		private Integer cabId;
		private Integer historyId;
		private Integer customerId;

		public Double getTotalAmount() {
			return totalAmount;
		}

		public void setTotalAmount(Double totalAmount) {
			this.totalAmount = totalAmount;
		}

		public Double getBaseFare() {
			return baseFare;
		}

		public void setBaseFare(Double baseFare) {
			this.baseFare = baseFare;
		}

		public Double getTaxes() {
			return taxes;
		}

		public void setTaxes(Double taxes) {
			this.taxes = taxes;
		}

		public Double getDiscount() {
			return discount;
		}

		public void setDiscount(Double discount) {
			this.discount = discount;
		}

		public Double getConvenience() {
			return convenience;
		}

		public void setConvenience(Double convenience) {
			this.convenience = convenience;
		}

		public Integer getPaxNum() {
			return paxNum;
		}

		public void setPaxNum(Integer paxNum) {
			this.paxNum = paxNum;
		}

		public Integer getCabNum() {
			return cabNum;
		}

		public void setCabNum(Integer cabNum) {
			this.cabNum = cabNum;
		}

		public String getPhone() {
			return phone;
		}

		public void setPhone(String phone) {
			this.phone = phone;
		}

		public Integer getCabId() {
			return cabId;
		}

		public void setCabId(Integer cabId) {
			this.cabId = cabId;
		}

		public Integer getHistoryId() {
			return historyId;
		}

		public void setHistoryId(Integer historyId) {
			this.historyId = historyId;
		}

		public Integer getCustomerId() {
			return customerId;
		}

		public void setCustomerId(Integer customerId) {
			this.customerId = customerId;
		}

	}

}
