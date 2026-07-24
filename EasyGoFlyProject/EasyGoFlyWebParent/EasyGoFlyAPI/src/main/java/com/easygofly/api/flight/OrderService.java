package com.easygofly.api.flight;
 
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easygofly.api.LogService;
import com.easygofly.api.wallet.WalletService;
import com.easygofly.entity.BaggageOnline;
import com.easygofly.entity.Customer;
import com.easygofly.entity.MealsOnline;
import com.easygofly.entity.Order;
import com.easygofly.entity.OrderStatus;
import com.easygofly.entity.PaymentMethod;
import com.easygofly.entity.ProductDetail;
import com.easygofly.entity.SearchHistory;
import com.easygofly.entity.SeatsOnline;
import com.easygofly.entity.TravellerDetail;
import com.easygofly.entity.Wallet;

@Service
public class OrderService {
	@Autowired
	private ProductDetailCrudRepository productDetailCrudRepo;
	@Autowired
	private OrderRepository orderRepo;
	@Autowired
	private TravellerRepository travellerRepo;
	@Autowired
	private OnlineFlightService onlineFlightService;
	@Autowired
	private LogService logService;
	@Autowired
	private WalletService walletService;

	public Wallet walletPayOrder(Customer customer, Order order) {
		Date date = Calendar.getInstance().getTime();
		DateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");
		DateFormat dateFormat2 = new SimpleDateFormat("hhmmss");
		String strDate1 = dateFormat1.format(date);
		String strDate2 = dateFormat2.format(date);

		String orderString = "EGF" + strDate1 + "T" + strDate2 + "R" + order.getId();
		return walletService.updateWalletBalanceByOrder(customer, order, orderString, "");
	}

	public Order updateBookingId(Order order, String bookingId) {
		Order savedOrder = orderRepo.findById(order.getId()).get();
		savedOrder.setBookingId(bookingId);
		return orderRepo.save(savedOrder);
	}

	public List<TravellerDetail> findTravellerByOrderANDProductDetail(ProductDetail productDetail, Order order) {
		return travellerRepo.findTravellerByProductDetailAndOrder(productDetail, order);
	}

	public String ticketDetails(Customer customer, Order order, ProductDetail productDetail,
			List<OrderRestController.FareBreakdown> fareBreakdown, SearchHistory searchHist,
			PaymentMethod paymentMethod, String traceId) throws MalformedURLException, IOException {
		List<String> travelerDetailsArray = new ArrayList<String>();
		List<TravellerDetail> travelers = order.getTravellerDetails();

		if (productDetail.getMode().equals("Online-data")) {

			String[] airlineNoArray = productDetail.getFlightNum().split("-");
			String isLeadPax = "false";
			Integer countAdult = 0;

			for (TravellerDetail travellerDetail : travelers) {
				BaggageOnline baggageOnline = travellerDetail.getBaggageOnline();
				MealsOnline mealsOnline = travellerDetail.getMealOnline();
				SeatsOnline seatsOnline = travellerDetail.getSeatsOnline();

				Date getDOB = travellerDetail.getDob();
				Integer genNum = 0;
				if (travellerDetail.getSalutation().equals("Mr") || travellerDetail.getSalutation().equals("Mstr")) {
					genNum = 1;
				} else {
					genNum = 2;
				}
				String baseFare = "";
				String tax = "";

				// baggage information
				String bagCode = baggageOnline.getCode(), bagWeight = baggageOnline.getWeight(),
						bagPrice = baggageOnline.getPrice();

				// meal information
				String mealCode = mealsOnline.getCode(), mealName = mealsOnline.getName(),
						mealQuantity = mealsOnline.getQuantity(), mealPrice = mealsOnline.getPrice();

				// seat information
				@SuppressWarnings("unused")
				String seatAvailabilityType = seatsOnline.getAvailablityType().toString(),
						seatCode = seatsOnline.getCode(), seatRowNo = seatsOnline.getRowNo(),
						seatNo = seatsOnline.getSeatNo(), seatType = seatsOnline.getSeatType().toString(),
						seatDeck = seatsOnline.getDeck().toString(),
						seatCompartment = seatsOnline.getCompartment().toString(), seatPrice = seatsOnline.getPrice(),
						seatCraftType = seatsOnline.getCraftType();

				String baggageDetailsString = "		\"Baggage\":[\r\n" + "            {\r\n"
						+ "                \"AirlineCode\": \"" + airlineNoArray[0] + "\",\r\n"
						+ "                \"FlightNumber\": \"" + airlineNoArray[1] + "\",\r\n"
						+ "                \"WayType\": 2,\r\n" + "                \"Code\": \"" + bagCode + "\",\r\n"
						+ "                \"Description\": 2,\r\n" + "                \"Weight\": " + bagWeight
						+ ",\r\n" + "                \"Currency\": \"INR\",\r\n" + "                 \"Price\": "
						+ bagPrice + ",\r\n" + "                 \"Origin\": \"" + productDetail.getCityOne()
						+ "\",\r\n" + "                \"Destination\": \"" + productDetail.getCityTwo() + "\"\r\n"
						+ "				}\r\n" + "			],\r\n";

				String mealDetailsString = "     \"MealDynamic\": [\r\n" + "        {\r\n"
						+ "          \"AirlineCode\": \"" + airlineNoArray[0] + "\",\r\n"
						+ "          \"FlightNumber\": \"" + airlineNoArray[1] + "\",\r\n"
						+ "          \"WayType\": 2,\r\n" + "          \"Code\": \"" + mealCode + "\",\r\n"
						+ "          \"Description\": 2,\r\n" + "          \"AirlineDescription\": \"" + mealName
						+ "\",\r\n" + "          \"Quantity\": " + mealQuantity + ",\r\n"
						+ "          \"Currency\": \"INR\",\r\n" + "          \"Price\": " + mealPrice + ",\r\n"
						+ "          \"Origin\": \"" + productDetail.getCityOne() + "\",\r\n"
						+ "          \"Destination\": \"" + productDetail.getCityTwo() + "\"\r\n" + "        }],\r\n";

				String seatDetailsString = "		\"SeatDynamic\": [\r\n" + "        {\r\n"
						+ "	    \"AirlineCode\": \"" + airlineNoArray[0] + "\",\r\n"
						+ "             \"FlightNumber\": \"" + airlineNoArray[1] + "\",\r\n"
						+ "              \"CraftType\": \"" + productDetail.getCraftType() + "\",\r\n"
						+ "               \"Origin\": \"" + productDetail.getCityOne() + "\",\r\n"
						+ "                \"Destination\": \"" + productDetail.getCityTwo() + "\",\r\n"
						+ "                \"AvailablityType\": " + seatAvailabilityType + ",\r\n"
						+ "                \"Description\": 2,\r\n" + "                \"Code\": \"" + seatCode
						+ "\",\r\n" + "                \"RowNo\": \"" + seatRowNo + "\",\r\n"
						+ "                \"SeatNo\": \"" + seatNo + "\",\r\n" + "                \"SeatType\": "
						+ seatType + ",\r\n" + "                \"SeatWayType\": 2,\r\n"
						+ "                \"Compartment\": " + seatCompartment + ",\r\n" + "                \"Deck\": "
						+ seatDeck + ",\r\n" + "                \"Currency\": \"INR\",\r\n"
						+ "                \"Price\": " + seatPrice
						+ "                                                                                                                                                                                                      \r\n"
						+ "			\r\n" + "		}],\r\n";

				String baggageDetails = baggageDetailsString;
				String mealDetails = mealDetailsString;
				String seatDetails = seatDetailsString;

				fareBreakdown.forEach(fare -> {
					System.out.println("Fare Breakdown: " + fare.getBaseFare());
				});

				if (travellerDetail.getPaxType().equals("1")) {
					OrderRestController.FareBreakdown fare = fareBreakdown.stream()
							.filter(f -> f.getPassengerType() == 1).findFirst().get();

					Double fareInt = fare.getBaseFare() / searchHist.getAdultNum();
					Double taxInt = fare.getTax() / searchHist.getAdultNum();
					baseFare = "" + fareInt;
					tax = "" + taxInt;
					updateBasefareTax(travellerDetail, baseFare, tax);

					if (countAdult == 0) {
						isLeadPax = "true";
					} else {
						isLeadPax = "false";
					}
					countAdult++;
					System.out.println(countAdult);
				} else if (travellerDetail.getPaxType().equals("2")) {
					OrderRestController.FareBreakdown fare = fareBreakdown.stream()
							.filter(f -> f.getPassengerType() == 2).findFirst().get();

					Double fareInt = fare.getBaseFare() / searchHist.getChildNum();
					Double taxInt = fare.getTax() / searchHist.getChildNum();
					baseFare = "" + fareInt;
					tax = "" + taxInt;
					updateBasefareTax(travellerDetail, baseFare, tax);
					isLeadPax = "false";
				} else {
					OrderRestController.FareBreakdown fare = fareBreakdown.stream()
							.filter(f -> f.getPassengerType() == 3).findFirst().get();

					Double fareInt = fare.getBaseFare() / searchHist.getInfantNum();
					Double taxInt = fare.getTax() / searchHist.getInfantNum();
					baseFare = "" + fareInt;
					tax = "" + taxInt;
					updateBasefareTax(travellerDetail, baseFare, tax);
					isLeadPax = "false";
				}

				String details = "{\r\n" + "		\"Title\": \"" + travellerDetail.getSalutation() + "\",\r\n"
						+ "		\"FirstName\": \"" + travellerDetail.getFirstName() + "\",\r\n"
						+ "		\"LastName\": \"" + travellerDetail.getLastName() + "\",\r\n" + "		\"PaxType\": "
						+ travellerDetail.getPaxType() + ",\r\n" + "		\"DateOfBirth\": \"" + getDOB
						+ "T00:00:00\",\r\n" + "		\"Gender\": " + genNum + ",\r\n" + "		\"PassportNo\": \""
						+ travellerDetail.getPassportNo() + "\",\r\n" + "		\"PassportExpiry\": \""
						+ travellerDetail.getPassportExpiry() + "T00:00:00\",\r\n"
						+ "		\"AddressLine1\": \"123, Test\",\r\n" + "		\"AddressLine2\": \"\",\r\n"
						+ "		\"Fare\": {\r\n" + "			\"BaseFare\": " + baseFare + ",\r\n"
						+ "			\"Tax\": " + tax + ",\r\n" + "			\"YQTax\": 0.0,\r\n"
						+ "			\"AdditionalTxnFeePub\": 0.0,\r\n" + "			\"AdditionalTxnFeeOfrd\": 0.0,\r\n"
						+ "			\"OtherCharges\": 0.0\r\n" + "		},\r\n" + "		\"City\": \"Gurgaon\",\r\n"
						+ "		\"CountryCode\": \"IN\",\r\n" + "		\"CountryName\": \"India\",      \r\n"
						+ "     \"Nationality\": \"IN\",\r\n" + "		\"ContactNo\": \"" + order.getPhoneNumber()
						+ "\",\r\n" + "		\"Email\": \"" + order.getContactEmail() + "\",\r\n"
						+ "		\"IsLeadPax\": " + isLeadPax + ",\r\n" + "		\"FFAirlineCode\": \""
						+ airlineNoArray[0] + "\",\r\n" + "		\"FFNumber\": \"" + airlineNoArray[1] + "\",\r\n"
						+ baggageDetails + mealDetails + seatDetails + "		\"GSTCompanyAddress\": \"\",\r\n"
						+ "		\"GSTCompanyContactNumber\": \"\",\r\n" + "		\"GSTCompanyName\": \"\",\r\n"
						+ "		\"GSTNumber\": \"\",\r\n" + "		\"GSTCompanyEmail\": \"\"\r\n" + "}";

				travelerDetailsArray.add(details);

			}

			String arrayTraveler = travelerDetailsArray.stream().map(val -> String.valueOf(val))
					.collect(Collectors.joining(",", "[", "]"));

			/* Ticket details */
			StringBuilder responseBodyTicket = onlineFlightService.apiOnlineTicket(traceId,
					productDetail.getResultIndex(), arrayTraveler);

			JSONObject jsonObjTicket = new JSONObject(responseBodyTicket.toString());
			System.out.println(jsonObjTicket);
			logService.generateLog(jsonObjTicket.toString());
			try {
				JSONObject jsonObjTicketResponse = jsonObjTicket.getJSONObject("Response").getJSONObject("Response")
						.getJSONObject("FlightItinerary");
				JSONArray jsonArraySegment = jsonObjTicketResponse.getJSONArray("Segments");

				String terminalDep = "", terminalArr = "";
				for (int i = 0; i < jsonArraySegment.length(); i++) {
					JSONObject jsonObjectSegments = jsonArraySegment.getJSONObject(i);
					if (i == 0) {
						terminalDep = jsonObjectSegments.getJSONObject("Origin").getJSONObject("Airport")
								.get("Terminal").toString();
					}
					if (i == (jsonArraySegment.length() - 1)) {
						terminalArr = jsonObjectSegments.getJSONObject("Destination").getJSONObject("Airport")
								.get("Terminal").toString();
					}
				}

				String onlinePNR = jsonObjTicketResponse.get("PNR").toString();
				String onlineBookingId = jsonObjTicketResponse.get("BookingId").toString();

				productDetail.setTerminalDep(terminalDep);
				productDetail.setTerminalArr(terminalArr);
				productDetail.setPnr(onlinePNR);
				productDetail.setTotalSeats(productDetail.getUploadSeats());
				productDetailCrudRepo.save(productDetail);

				order.setPaymentMethod(paymentMethod);
				order.setOrderStatus(OrderStatus.SUCCESSFULL);
				updateBookingId(order, onlineBookingId);

				/* Get booking details */
				StringBuilder responseBodyBooking = onlineFlightService.apiOnlineGetBookingDetails(traceId, onlinePNR,
						onlineBookingId);

				JSONObject jsonObjBooking = new JSONObject(responseBodyBooking.toString());
				System.out.println(jsonObjBooking);
				logService.generateLog(jsonObjBooking.toString());

				if (paymentMethod.equals(PaymentMethod.WALLET)) {
					walletPayOrder(customer, order);
				}

				return responseBodyBooking.toString();

			} catch (JSONException json) {
				json.printStackTrace();

				return responseBodyTicket.toString();
			}

		}

		String responseBodyError = "{\r\n"
				+ "	\"Response\": {\r\n" 
				+ "\"Error\": {\r\n"
				+ "			\"ErrorCode\": 0,\r\n"
				+ "			\"ErrorMessage\": \"\"\r\n"
				+ "		}\r\n"
				+ "	}";

		return responseBodyError;
	}

	public String bookingDetails(Customer customer, Order order, ProductDetail productDetail,
			List<OrderRestController.FareBreakdown> fareBreakdown, OrderRestController.Fare fareDetail,
			SearchHistory searchHist, PaymentMethod paymentMethod, String traceId) throws IOException {
		List<String> travelerDetailsArray = new ArrayList<String>();
		List<TravellerDetail> travelers = findTravellerByOrderANDProductDetail(productDetail, order);

		if (productDetail.getTraceId().equals("Online-data")) {

			String isLeadPax = "false";
			Integer countAdult = 0;

			for (TravellerDetail travellerDetail : travelers) {

				Date getDOB = travellerDetail.getDob();
				Integer genNum = 0;
				if (travellerDetail.getSalutation().equals("Mr") || travellerDetail.getSalutation().equals("Mstr")) {
					genNum = 1;
				} else {
					genNum = 2;
				}
				String baseFare = "";
				String tax = "";

				if (travellerDetail.getPaxType().equals("1")) {
					OrderRestController.FareBreakdown fare = fareBreakdown.stream()
							.filter(f -> f.getPassengerType() == 1).findFirst().get();
					Double fareInt = fare.getBaseFare() / searchHist.getAdultNum();
					Double taxInt = fare.getTax() / searchHist.getAdultNum();
					baseFare = "" + fareInt;
					tax = "" + taxInt;
					updateBasefareTax(travellerDetail, baseFare, tax);

					if (countAdult == 0) {
						isLeadPax = "true";
					} else {
						isLeadPax = "false";
					}
					countAdult++;
					System.out.println(countAdult);
				} else if (travellerDetail.getPaxType().equals("2")) {
					OrderRestController.FareBreakdown fare = fareBreakdown.stream()
							.filter(f -> f.getPassengerType() == 2).findFirst().get();

					Double fareInt = fare.getBaseFare() / searchHist.getChildNum();
					Double taxInt = fare.getTax() / searchHist.getChildNum();
					baseFare = "" + fareInt;
					tax = "" + taxInt;
					updateBasefareTax(travellerDetail, baseFare, tax);
					isLeadPax = "false";
				} else {
					OrderRestController.FareBreakdown fare = fareBreakdown.stream()
							.filter(f -> f.getPassengerType() == 3).findFirst().get();

					Double fareInt = fare.getBaseFare() / searchHist.getInfantNum();
					Double taxInt = fare.getTax() / searchHist.getInfantNum();
					baseFare = "" + fareInt;
					tax = "" + taxInt;
					updateBasefareTax(travellerDetail, baseFare, tax);
					isLeadPax = "false";
				}

				String details = "{\r\n" + "		\"Title\": \"" + travellerDetail.getSalutation() + "\",\r\n"
						+ "		\"FirstName\": \"" + travellerDetail.getFirstName() + "\",\r\n"
						+ "		\"LastName\": \"" + travellerDetail.getLastName() + "\",\r\n" + "		\"PaxType\": "
						+ travellerDetail.getPaxType() + ",\r\n" + "		\"DateOfBirth\": \"" + getDOB
						+ "T00:00:00\",\r\n" + "		\"Gender\": " + genNum + ",\r\n" + "		\"PassportNo\": \""
						+ travellerDetail.getPassportNo() + "\",\r\n" + "		\"PassportExpiry\": \""
						+ travellerDetail.getPassportExpiry() + "T00:00:00\",\r\n"
						+ "		\"AddressLine1\": \"123, Test\",\r\n" + "		\"AddressLine2\": \"\",\r\n"
						+ "		\"Fare\": {\r\n" + "			\"BaseFare\": " + baseFare + ",\r\n"
						+ "			\"Tax\": " + tax + ",\r\n" + "			\"YQTax\": 0.0,\r\n"
						+ "			\"AdditionalTxnFeePub\": 0.0,\r\n" + "			\"AdditionalTxnFeeOfrd\": 0.0,\r\n"
						+ "			\"OtherCharges\": " + fareDetail.getOtherCharges() + ",\r\n"
						+ "			\"Discount\": " + fareDetail.getDiscount() + ",\r\n"
						+ "			\"PublishedFare\": " + fareDetail.getPublishedFare() + ",\r\n"
						+ "			\"OfferedFare\": " + fareDetail.getOfferedFare() + ",\r\n"
						+ "			\"TdsOnCommission\": " + fareDetail.getTdsOnCommission() + ",\r\n"
						+ "			\"TdsOnPLB\": " + fareDetail.getTdsOnPLB() + ",\r\n"
						+ "			\"TdsOnIncentive\": " + fareDetail.getTdsOnIncentive() + ",\r\n"
						+ "			\"ServiceFee\": " + fareDetail.getServiceFee() + "\r\n" + "		},\r\n"
						+ "		\"City\": \"Gurgaon\",\r\n" + "		\"CountryCode\": \"IN\",\r\n"
						+ "		\"CountryName\": \"India\",      \r\n" + "     \"Nationality\": \"IN\",\r\n"
						+ "		\"ContactNo\": \"" + order.getPhoneNumber() + "\",\r\n" + "		\"Email\": \""
						+ order.getContactEmail() + "\",\r\n" + "		\"IsLeadPax\": " + isLeadPax + ",\r\n"
						+ "		\"FFAirlineCode\": \"" + productDetail.getFlightNum().split("-")[0] + "\",\r\n"
						+ "		\"FFNumber\": \"" + productDetail.getFlightNum().split("-")[1] + "\",\r\n"
						+ "		\"GSTCompanyAddress\": \"\",\r\n" + "		\"GSTCompanyContactNumber\": \"\",\r\n"
						+ "		\"GSTCompanyName\": \"\",\r\n" + "		\"GSTNumber\": \"\",\r\n"
						+ "		\"GSTCompanyEmail\": \"\"\r\n" + "}";

				travelerDetailsArray.add(details);

			}

			String arrayTraveler = travelerDetailsArray.stream().map(val -> String.valueOf(val))
					.collect(Collectors.joining(",", "[", "]"));

			/* Ticket details */
			StringBuilder responseBodyBook = onlineFlightService.apiOnlineBookingNonLCC(traceId,
					productDetail.getResultIndex(), arrayTraveler);

			JSONObject jsonObjBooking = new JSONObject(responseBodyBook.toString());
			System.out.println(jsonObjBooking);
			logService.generateLog(jsonObjBooking.toString());

			String bookingId = "", pnrBooking = "";
			Integer bookingIdInt = 0;

			try {
				JSONObject jsonObjBookingResponse = jsonObjBooking.getJSONObject("Response").getJSONObject("Response")
						.getJSONObject("FlightItinerary");
				pnrBooking = jsonObjBookingResponse.get("PNR").toString();
				bookingId = jsonObjBookingResponse.get("BookingId").toString();
				bookingIdInt = Integer.parseInt(bookingId);

			} catch (Exception e) {
				System.out.println("Not found!!");
			}

			/* Ticket details */
			StringBuilder responseBodyTicket = onlineFlightService.apiOnlineTicketNonLcc(traceId, pnrBooking,
					bookingIdInt);

			JSONObject jsonObjTicket = new JSONObject(responseBodyTicket.toString());
			System.out.println(jsonObjTicket);
			logService.generateLog(jsonObjTicket.toString());
			try {
				JSONObject jsonObjTicketResponse = jsonObjTicket.getJSONObject("Response").getJSONObject("Response")
						.getJSONObject("FlightItinerary");
				JSONArray jsonArraySegment = jsonObjTicketResponse.getJSONArray("Segments");
//       		JSONObject jsonObjectSegments = new JSONObject();

				String terminalDep = "", terminalArr = "";
				for (int i = 0; i < jsonArraySegment.length(); i++) {
					JSONObject jsonObjectSegments = jsonArraySegment.getJSONObject(i);
					if (i == 0) {
						terminalDep = jsonObjectSegments.getJSONObject("Origin").getJSONObject("Airport")
								.get("Terminal").toString();
					}
					if (i == (jsonArraySegment.length() - 1)) {
						terminalArr = jsonObjectSegments.getJSONObject("Destination").getJSONObject("Airport")
								.get("Terminal").toString();
					}
				}

				String onlinePNR = jsonObjTicketResponse.get("PNR").toString();
				String onlineBookingId = jsonObjTicketResponse.get("BookingId").toString();

				productDetail.setTerminalDep(terminalDep);
				productDetail.setTerminalArr(terminalArr);
				productDetail.setPnr(onlinePNR);
				productDetail.setTotalSeats(productDetail.getUploadSeats());
				productDetailCrudRepo.save(productDetail);
				
				order.setPaymentMethod(paymentMethod);
				order.setOrderStatus(OrderStatus.SUCCESSFULL);
				updateBookingId(order, onlineBookingId);

				/* Get booking details */
				StringBuilder responseBodyBooking = onlineFlightService.apiOnlineGetBookingDetails(traceId, onlinePNR,
						onlineBookingId);

				JSONObject jsonObjGetBooking = new JSONObject(responseBodyBooking.toString());
				System.out.println(jsonObjGetBooking);
				logService.generateLog(jsonObjGetBooking.toString());

				if (paymentMethod.equals(PaymentMethod.WALLET)) {
					walletPayOrder(customer, order);
				}

				return responseBodyBooking.toString();

			} catch (JSONException json) {
				json.printStackTrace();

				return responseBodyTicket.toString();
			}

		}

		String responseBodyError = "{\r\n"
				+ "	\"Response\": {\r\n" 
				+ "\"Error\": {\r\n"
				+ "			\"ErrorCode\": 0,\r\n"
				+ "			\"ErrorMessage\": \"\"\r\n"
				+ "		}\r\n"
				+ "	}";

		return responseBodyError;
	}

	public void updateBasefareTax(TravellerDetail travellerDetail, String basefare, String tax) {
		travellerDetail.setBasefare(basefare);
		travellerDetail.setTax(tax);

		travellerRepo.save(travellerDetail);
	}

	public String showUpdateOrderByTicket(Order order) {
		StringBuilder orderBody = new StringBuilder();
		orderBody.append("{");

		orderBody.append("\"id\":\"").append(order.getId()).append("\",");
		orderBody.append("\"name\":\"").append(order.getName()).append("\",");
		orderBody.append("\"firstName\":\"").append(order.getFirstName()).append("\",");
		orderBody.append("\"lastName\":\"").append(order.getLastName()).append("\",");
		orderBody.append("\"phoneNumber\":\"").append(order.getPhoneNumber()).append("\",");
		orderBody.append("\"contactEmail\":\"").append(order.getContactEmail()).append("\",");
		orderBody.append("\"createdTime\":\"").append(order.getCreatedTime()).append("\",");
		orderBody.append("\"addressLine1\":\"").append(order.getAddressLine1()).append("\",");
		orderBody.append("\"addressLine2\":\"").append(order.getAddressLine2()).append("\",");
		orderBody.append("\"orderStatus\":\"").append(order.getOrderStatus()).append("\",");
		orderBody.append("\"passengerNum\":").append(order.getPassengerNum()).append(",");
		orderBody.append("\"paymentMethod\":\"").append(order.getPaymentMethod()).append("\",");
		orderBody.append("\"postalCode\":\"").append(order.getPostalCode()).append("\",");
		orderBody.append("\"couponCode\":\"").append(order.getCouponCode()).append("\",");
		orderBody.append("\"price\":").append(order.getPrice()).append(",");

		// Product Details
		orderBody.append("\"productDetails\":[");
		List<ProductDetail> productList = order.getProductDetails();
		for (int i = 0; i < productList.size(); i++) {
		    ProductDetail f = productList.get(i);
		    orderBody.append("{")
		        .append("\"id\":\"").append(f.getId()).append("\",")
		        .append("\"arrTime\":\"").append(f.getArrTime()).append("\",")
		        .append("\"depTime\":\"").append(f.getDepTime()).append("\",")
		        .append("\"cityOne\":\"").append(f.getCityOne()).append("\",")
		        .append("\"cityTwo\":\"").append(f.getCityTwo()).append("\",")
		        .append("\"date\":\"").append(f.getDate()).append("\",")
		        .append("\"pnr\":\"").append(f.getPnr()).append("\",")
		        .append("\"totalSeats\":").append(f.getTotalSeats()).append(",")
		        .append("\"flightNum\":\"").append(f.getFlightNum()).append("\",")
		        .append("\"arrTimeInteger\":").append(f.getArrTimeInteger()).append(",")
		        .append("\"depTimeInteger\":").append(f.getDepTimeInteger()).append(",")
		        .append("\"priceADT\":").append(f.getPriceADT()).append(",")
		        .append("\"priceINF\":").append(f.getPriceINF()).append(",")
		        .append("\"markupADT\":").append(f.getMarkupADT()).append(",")
		        .append("\"markupINF\":").append(f.getMarkupINF()).append(",")
		        .append("\"journeyClass\":\"").append(f.getJourneyClass()).append("\",")
		        .append("\"terminalDep\":\"").append(f.getTerminalDep()).append("\",")
		        .append("\"terminalArr\":\"").append(f.getTerminalArr()).append("\",")
		        .append("\"cabinBaggage\":\"").append(f.getCabinBaggage()).append("\",")
		        .append("\"baggage\":\"").append(f.getBaggage()).append("\",")
		        .append("\"craftType\":\"").append(f.getCraftType()).append("\",")
		        .append("\"duration\":\"").append(f.getDuration()).append("\",")
		        .append("\"brand\":\"").append(f.getBrand()).append("\",")
		        .append("\"stopNum\":").append(f.getStopNum()).append(",")
		        .append("\"traceId\":\"").append(f.getTraceId()).append("\",")
		        .append("\"resultIndex\":\"").append(f.getResultIndex()).append("\",")
		        .append("\"airlineRemarks\":\"").append(f.getAirlineRemarks()).append("\",")
		        .append("\"mode\":\"").append(f.getMode()).append("\",")
		        .append("\"device\":\"").append(f.getDevice()).append("\",")
		        .append("\"deviceDescription\":\"").append(f.getDeviceDescription()).append("\",")
		        .append("\"deviceType\":\"").append(f.getDeviceType()).append("\",")
		        .append("\"uploadSeats\":").append(f.getUploadSeats())
		        .append("}");
		    if (i < productList.size() - 1) orderBody.append(",");
		}
		orderBody.append("],");

		// Traveller Details
		orderBody.append("\"travellerDetails\":[");
		List<TravellerDetail> travelerList = order.getTravellerDetails();
		for (int i = 0; i < travelerList.size(); i++) {
		    TravellerDetail t = travelerList.get(i);
		    orderBody.append("{")
		        .append("\"id\":\"").append(t.getId()).append("\",")
		        .append("\"salutation\":\"").append(t.getSalutation()).append("\",")
		        .append("\"firstName\":\"").append(t.getFirstName()).append("\",")
		        .append("\"lastName\":\"").append(t.getLastName()).append("\",")
		        .append("\"type\":\"").append(t.getPaxType()).append("\",")
		        .append("\"passportNo\":\"").append(t.getPassportNo()).append("\",")
		        .append("\"passportExpiry\":\"").append(t.getPassportExpiry()).append("\",")
		        .append("\"dob\":\"").append(t.getDob()).append("\",");

		    // Meals
		    if (t.getMealOnline() != null) {
		    orderBody.append("\"meals\":{")
            .append("\"id\":\"").append(t.getMealOnline().getId()).append("\",")
            .append("\"description\":\"").append(t.getMealOnline().getName()).append("\",")
            .append("\"price\":\"").append(t.getMealOnline().getPrice()).append("\",")
            .append("\"code\":\"").append(t.getMealOnline().getCode()).append("\",")
            .append("\"quantity\":\"").append(t.getMealOnline().getQuantity()).append("\"")
            .append("},");
		    }
		    
		    // Baggage
		    if (t.getBaggageOnline() != null) {
		    orderBody.append("\"baggage\":{")
		            .append("\"id\":\"").append(t.getBaggageOnline().getId()).append("\",")
		            .append("\"price\":\"").append(t.getBaggageOnline().getPrice()).append("\",")
		            .append("\"code\":\"").append(t.getBaggageOnline().getCode()).append("\",")
		            .append("\"weight\":\"").append(t.getBaggageOnline().getWeight()).append("\"")
		            .append("},");
		    }
		    
		    // Seat
		    if (t.getSeatsOnline() != null) {
		        SeatsOnline s = t.getSeatsOnline();
		        orderBody.append("\"seat\":{")
		            .append("\"id\":\"").append(s.getId()).append("\",")
		            .append("\"price\":\"").append(s.getPrice()).append("\",")
		            .append("\"compartment\":\"").append(s.getCompartment()).append("\",")
		            .append("\"availablityType\":\"").append(s.getAvailablityType()).append("\",")
		            .append("\"deck\":\"").append(s.getDeck()).append("\",")
		            .append("\"rowNo\":\"").append(s.getRowNo()).append("\",")
		            .append("\"code\":\"").append(s.getCode()).append("\",")
		            .append("\"seatType\":\"").append(s.getSeatType()).append("\",")
		            .append("\"seatNo\":\"").append(s.getSeatNo()).append("\",")
		            .append("\"craftType\":\"").append(s.getCraftType()).append("\"")
		            .append("},");
		    }

		    // Clean trailing comma
		    if (orderBody.charAt(orderBody.length() - 1) == ',') {
		        orderBody.setLength(orderBody.length() - 1);
		    }

		    orderBody.append("}");
		    if (i < travelerList.size() - 1) orderBody.append(",");
		}
		orderBody.append("]");

		orderBody.append("}");

		return orderBody.toString();
	}

}
