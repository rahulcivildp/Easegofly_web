package com.easygofly.site.flight.order;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.easygofly.entity.BaggageOnline;
import com.easygofly.entity.CartItem;
import com.easygofly.entity.CheckoutInfo;
import com.easygofly.entity.Country;
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
import com.easygofly.entity.exception.UserNotFoundException;
import com.easygofly.site.LogService;
import com.easygofly.site.Utility;
import com.easygofly.site.flight.OnlineFlightService;
import com.easygofly.site.flight.ProductDetailService;
import com.easygofly.site.flight.ProductDetailsController;
import com.easygofly.site.flight.TravelerService;
import com.easygofly.site.flight.TravellerRepository;
import com.easygofly.site.flight.SearchHistoryRepository;
import com.easygofly.site.flight.SearchHistoryService;
import com.easygofly.site.setting.EmailSettingBag;
import com.easygofly.site.setting.SettingService;
import com.easygofly.site.shoppingCart.CartItemRepository;
import com.easygofly.site.shoppingCart.CartItemService;
import com.easygofly.site.wallet.WalletService;

@Service
public class OrderService {
	
	public static final int ORDER_PER_PAGE = 6;
	
	@Autowired OrderRepository orderRepo;
	@Autowired TravellerRepository travellerRepo;
	@Autowired private CartItemService cartService;
	@Autowired private CartItemRepository cartRepo;
	@Autowired private SearchHistoryRepository searchRepo;
	@Autowired private SearchHistoryService searchService;
	@Autowired private ProductDetailService productService;
	@Autowired private WalletService walletService;
	@Autowired private ProductDetailsController productDetailsController;
	@Autowired private OnlineFlightService onlineFlightService;
	@Autowired private TravelerService travelerService;
	@Autowired private LogService logService;
	@Autowired private SettingService settingService;

	public Order createOrder(Customer customer, Order newOrder, CartItem cartItem, ProductDetail productDetail, PaymentMethod paymentMethod, CheckoutInfo checkoutInfo, SearchHistory searchHistory, String orderName, List<TravellerDetail> travellerDetails) {
		newOrder.setCreatedTime(new Date());
		newOrder.setOrderStatus(OrderStatus.NEW);
		newOrder.setCustomer(customer);
		newOrder.setProductDetail(productDetail);
		newOrder.setPrice(checkoutInfo.getPaymentTotal());
		newOrder.setPaymentMethod(paymentMethod);
		newOrder.setName(orderName);

		newOrder.setAddressLine1(customer.getAddressLine1());
		newOrder.setAddressLine2(customer.getAddressLine2());
		newOrder.setCity(customer.getCity());
		
		Country country = customer.getCountry();
		newOrder.setCountry(country.getName());
		
		newOrder.setPostalCode(customer.getPostalCode());
		newOrder.setState(customer.getState());
		newOrder.setFirstName(customer.getFirstName());
		newOrder.setLastName(customer.getLastName());
		newOrder.setPhoneNumber(cartItem.getPhoneNum());
		newOrder.setAdultNum(searchHistory.getAdultNum());
		newOrder.setChildNum(searchHistory.getChildNum());
		newOrder.setInfantNum(searchHistory.getInfantNum());
		newOrder.setCityOne(productDetail.getCityOne());
		newOrder.setCityTwo(productDetail.getCityTwo());
		newOrder.setJourneyClass(searchHistory.getJourneyClass());
		newOrder.setPassengerNum(searchHistory.getPassengerNum());
		newOrder.setTripType(searchHistory.getTripType());
		newOrder.setCartId(cartItem.getId());
		newOrder.setContactEmail(cartItem.getEmail());
		newOrder.setTravellerDetails(travellerDetails);
		newOrder.setDevice(productDetail.getDevice());
		newOrder.setDeviceDescription(productDetail.getDeviceDescription());
		newOrder.setDeviceType(productDetail.getDeviceType());
		
		String transaction_id = "UIGIK&*^HJAS585789";
		String transaction_token = "ashdjgh3284270&^%@#*&)asahj31";
		
		newOrder.setTransactionId(transaction_id);
		newOrder.setTransactionToken(transaction_token);
		
		return orderRepo.save(newOrder);
	}
	
	public Order createOrderOnline(Customer customer, CartItem cartItem, ProductDetail productDetail, PaymentMethod paymentMethod, String price, SearchHistory searchHistory, String orderName, List<TravellerDetail> travellerDetails) {
		Order newOrder = new Order();
		newOrder.setCreatedTime(new Date());
		newOrder.setOrderStatus(OrderStatus.NEW);
		newOrder.setCustomer(customer);
		newOrder.setProductDetail(productDetail);
		newOrder.setPrice(Double.parseDouble(price));
		newOrder.setPaymentMethod(paymentMethod);
		newOrder.setName(orderName);

		newOrder.setAddressLine1(customer.getAddressLine1());
		newOrder.setAddressLine2(customer.getAddressLine2());
		newOrder.setCity(customer.getCity());
		
		Country country = customer.getCountry();
		newOrder.setCountry(country.getName());
		
		newOrder.setPostalCode(customer.getPostalCode());
		newOrder.setState(customer.getState());
		newOrder.setFirstName(customer.getFirstName());
		newOrder.setLastName(customer.getLastName());
		newOrder.setPhoneNumber(cartItem.getPhoneNum());
		newOrder.setAdultNum(searchHistory.getAdultNum());
		newOrder.setChildNum(searchHistory.getChildNum());
		newOrder.setInfantNum(searchHistory.getInfantNum());
		newOrder.setCityOne(productDetail.getCityOne());
		newOrder.setCityTwo(productDetail.getCityTwo());
		newOrder.setJourneyClass(searchHistory.getJourneyClass());
		newOrder.setPassengerNum(searchHistory.getPassengerNum());
		newOrder.setTripType(searchHistory.getTripType());
		newOrder.setCartId(cartItem.getId());
		newOrder.setContactEmail(cartItem.getEmail());
		newOrder.setTravellerDetails(travellerDetails);
		newOrder.setDevice(productDetail.getDevice());
		newOrder.setDeviceDescription(productDetail.getDeviceDescription());
		newOrder.setDeviceType(productDetail.getDeviceType());
		
		String transaction_id = "UIGIK&*^HJAS585789";
		String transaction_token = "ashdjgh3284270&^%@#*&)asahj31";
		
		newOrder.setTransactionId(transaction_id);
		newOrder.setTransactionToken(transaction_token);
		
		return orderRepo.save(newOrder);
	}
	
	public Order updateOrder(Order order, OrderStatus orderStatus) {
		order.setOrderStatus(orderStatus);
		return orderRepo.save(order);
	}
	
	public Order updateOrderPrice(Order order, CheckoutInfo checkoutInfo) {
		Order savedOrder = orderRepo.findById(order.getId()).get();
		
		savedOrder.setPrice(checkoutInfo.getPaymentTotal());
		return orderRepo.save(savedOrder);
	}
	
	public Order updateOrderPriceOnline(Order order, List<TravellerDetail> travellerDetails, CheckoutInfo checkoutInfo) {
		Order savedOrder = orderRepo.findById(order.getId()).get();
		Double checkoutPrice = checkoutInfo.getPaymentTotal();
		Double calPriceArray = 0d;
		for (TravellerDetail travellerDetail : travellerDetails) {
			MealsOnline mealsOnline = travellerDetail.getMealOnline();
			BaggageOnline baggageOnline = travellerDetail.getBaggageOnline();
			SeatsOnline seatsOnline = travellerDetail.getSeatsOnline();
			
			Double calPrice = Double.parseDouble(mealsOnline.getPrice()) + Double.parseDouble(baggageOnline.getPrice()) + Double.parseDouble(seatsOnline.getPrice());
			calPriceArray = calPriceArray + calPrice;
		}
		
		savedOrder.setPrice(calPriceArray + checkoutPrice);
		return orderRepo.save(savedOrder);
	}
	
	public Order addCouponCode(Order order, String couponCode) {
		Order savedOrder = orderRepo.findById(order.getId()).get();
		
		savedOrder.setCouponCode(couponCode);
		return orderRepo.save(savedOrder);
	}
	
	public Order deleteCouponCode(Order order) {
		Order savedOrder = orderRepo.findById(order.getId()).get();
		
		savedOrder.setCouponCode(null);
		return orderRepo.save(savedOrder);
	}
	
	public Order updateTotalPassenger(Order order, Integer totalSeat) {
		ProductDetail productDetail = order.getProductDetail();
		productDetail.setTotalSeats(totalSeat.toString());
		return orderRepo.save(order);
	}
	
	public Order updateBookingId(Order order, String bookingId) {
		Order savedOrder = orderRepo.findById(order.getId()).get();
		savedOrder.setBookingId(bookingId);
		return orderRepo.save(savedOrder);
	}
	
	public void deleteOrder(Integer id) throws UserNotFoundException {
		Long count = orderRepo.countById(id);
		if(count == null || count == 0) {
			throw new UserNotFoundException("Could not find any Cart Item with ID: " + id);
		}
		
		orderRepo.deleteById(id);
	}

	public List<Order> listAll() {
		return (List<Order>) orderRepo.findAll(Sort.by("firstName").ascending());
	}
	
	public Page<Order> listByPageOrder(Customer customer, int pageNum, String sortField, String sortDir) {
		Sort sort = Sort.by(sortField);
		
		sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();
		
		Pageable pageable = PageRequest.of(pageNum - 1, ORDER_PER_PAGE, sort);
		
		return orderRepo.findByCustomer(customer, pageable);
	}
	
	public TravellerDetail updateTravelersOrderId(Integer travelerId, Order order) {
		TravellerDetail travellerDetail = travellerRepo.findById(travelerId).get();
		travellerDetail.setOrder(order);
		return travellerRepo.save(travellerDetail);
	}
	
	public void loginControl(String totalPayment, Customer customer, ProductDetail flight, SearchHistory search, CartItem item, PaymentMethod paymentMethod, String orderName, Order order,
			List<TravellerDetail> travellerDetails, CheckoutInfo checkoutInfo) {
		if (order != null) {
			if (flight.getTraceId().equals("offline")) {
				updateOrderPrice(order, checkoutInfo);
				deleteCouponCode(order);
			} else {
				updateOrderPriceOnline(order, travellerDetails, checkoutInfo);
				deleteCouponCode(order);
			}
			
		} else {
			if (flight.getTraceId().equals("offline")) {
				Order orderSaved = saveOrderCreate(flight, search, item, paymentMethod, checkoutInfo, orderName, order, customer, travellerDetails);
				for (TravellerDetail travellerDetail : travellerDetails) {
					updateTravelersOrderId(travellerDetail.getId(), orderSaved);
				}
				deleteCouponCode(orderSaved);
			} else {
				Order orderSaved =  createOrderOnline(customer, item, flight, paymentMethod, totalPayment, search, orderName, travellerDetails);
				for (TravellerDetail travellerDetail : travellerDetails) {
					updateTravelersOrderId(travellerDetail.getId(), orderSaved);
				}
				deleteCouponCode(orderSaved);
			}	
		}
	}

	public Order saveOrderCreate(ProductDetail flight, SearchHistory search, CartItem item, PaymentMethod paymentMethod,
			CheckoutInfo checkoutInfo, String orderName, Order order, Customer customer, List<TravellerDetail> travellerDetails) {
		if (order == null) {
			Order newOrder = new Order();
			return  createOrder(customer, newOrder, item, flight, paymentMethod, checkoutInfo, search, orderName, travellerDetails);
		} else {
			return createOrder(customer, order, item, flight, paymentMethod, checkoutInfo, search, orderName, travellerDetails);
		}
	}

	public void orderTravelerSaveMethod(SearchHistory search, ProductDetail flight, Date date, CartItem item, String returnType, String totalPayment, 
			Customer customer, PaymentMethod paymentMethod, CheckoutInfo checkoutInfo) { 
		DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");  
		String dateTime = dateFormat.format(date);
		
		String orderName = flight.getCityOne() + "-" + flight.getCityTwo() + ":(" + flight.getFlightNum() + ")" + dateTime + ":" + flight.getDepTime() + "-" + flight.getArrTime() 
		+ ":(" + search.getPassengerNum() + ")" + ":(" + returnType + ")";
		
		Order order = orderRepo.findByCartItemOrder(item.getId());
		
		List<TravellerDetail> travellerDetails = travellerRepo.findTravellerByCurtItemAndProductDetail(flight, item);

		cartService.updateCartItemOrdered(item);

		 loginControl(totalPayment, customer, flight, search, item,
				paymentMethod, orderName, order, travellerDetails, checkoutInfo);
		
	}

	public void methodSSR(ProductDetail productDetail) throws MalformedURLException, IOException {
		/* SSR details */
        StringBuilder responseBodySSR = onlineFlightService.apiOnlineFareruleQuoteSSR(productDetailsController.traceId, productDetail.getResultIndex(), "/AirService.svc/rest/SSR");
    	
    	JSONObject jsonObjSSR = new JSONObject(responseBodySSR.toString()); 
    	System.out.println("jsonObjSSR" + jsonObjSSR);
	}
	
	public Order orderUpdateWallet(Order order) {
		ProductDetail productDetail = order.getProductDetail();
		
		if (productDetail.getPnr().equals(null) || productDetail.getPnr().equals("")) {
			 updateOrder(order, OrderStatus.PENDING);
		} else {
			 updateOrder(order, OrderStatus.SUCCESSFULL);
		}
		
		Integer totalSeatRemaining = Integer.parseInt(productDetail.getTotalSeats()) - order.getPassengerNum();
		 updateTotalPassenger(order, totalSeatRemaining);
		
		try {
			CartItem cartItem = cartRepo.findById(order.getCartId()).get();
			if (!cartItem.equals(null)) {
				if (order.getOrderStatus().equals(OrderStatus.CANCELLED) || order.getOrderStatus().equals(OrderStatus.SUCCESSFULL) || order.getOrderStatus().equals(OrderStatus.FAILED) || order.getOrderStatus().equals(OrderStatus.PENDING )) {
					List<SearchHistory> search = cartItem.getSearchHistory();
					for (SearchHistory searchHistory : search) {
						List<TravellerDetail> travellerDetail2 = travellerRepo.findTravellerByCurtItemAndProductDetail(productDetail, cartItem);
						for (TravellerDetail travellerDetail : travellerDetail2) {
							travellerDetail.setCartItem(null);
							travellerRepo.save(travellerDetail);
						}
							
						searchService.updateSearchHistoryCart(searchHistory, cartItem);
						searchHistory.setCartItem(null);
						searchRepo.save(searchHistory);
					}
					cartItem.setSearchHistory(null);
					cartRepo.save(cartItem);
					
					cartService.deleteCartItem(cartItem.getId());
				}
			}
		} catch (Exception e) {
			return order;
		}
		return order;
	}

	public Wallet walletPayOrder(Customer customer, Order order, String lastVal) {
		Date date = Calendar.getInstance().getTime();  
		DateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");  
		DateFormat dateFormat2 = new SimpleDateFormat("hhmmss");
		String strDate1 = dateFormat1.format(date);
		String strDate2 = dateFormat2.format(date);
		
		String orderString = "EGF" + strDate1 + "T" + strDate2 + "R"+ order.getId() + lastVal;
		return walletService.updateWalletBalanceByOrder(customer, order, orderString, "");
	}

	public Wallet walletPayOrderCancel(Customer customer, Order order, String lastVal) {
		Date date = Calendar.getInstance().getTime();  
		DateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");  
		DateFormat dateFormat2 = new SimpleDateFormat("hhmmss");
		String strDate1 = dateFormat1.format(date);
		String strDate2 = dateFormat2.format(date);
		
		String orderString = "EGF" + strDate1 + "T" + strDate2 + "R"+ order.getId() + lastVal;
		return walletService.cancelWalletBalanceByOrder(customer, order, orderString, "");
	}

	public Wallet walletPayOrderReturn(Customer customer, Order order1, Order order2, String lastVal) {
		Date date = Calendar.getInstance().getTime();  
		DateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");  
		DateFormat dateFormat2 = new SimpleDateFormat("hhmmss");
		String strDate1 = dateFormat1.format(date);
		String strDate2 = dateFormat2.format(date);
		
		String orderString = "EGF" + strDate1 + "T" + strDate2 + "R"+ order1.getId() + "&"+ order2.getId() + lastVal;
		return walletService.updateWalletBalanceByOrderReturn(customer, order1, order2, orderString, "");
	}

	public String[] ticketDetails(Order order, ProductDetail productDetail, String basefareTravelerAdult, String taxTravelerAdult, String basefareTravelerChild, String taxTravelerChild, 
			 String basefareTravelerInfant, String taxTravelerInfant, SearchHistory searchId, String traceId ) throws MalformedURLException, IOException {
		List<String> travelerDetailsArray = new ArrayList<String>();
		List<TravellerDetail> travelers = productService.findTravellerByOrderANDProductDetail(productDetail, order);
		String[] hasErrorArr = new String[2];
		
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
   			
   			//baggage information
   			String bagCode = baggageOnline.getCode(), bagWeight = baggageOnline.getWeight(), bagPrice = baggageOnline.getPrice();
   			
   			//meal information
   			String mealCode = mealsOnline.getCode(), mealName = mealsOnline.getName(), mealQuantity = mealsOnline.getQuantity(), mealPrice = mealsOnline.getPrice();
   			
   			//seat information
   			@SuppressWarnings("unused")
				String seatAvailabilityType = seatsOnline.getAvailablityType().toString(), seatCode = seatsOnline.getCode(), seatRowNo = seatsOnline.getRowNo(), 
   					seatNo = seatsOnline.getSeatNo(), seatType = seatsOnline.getSeatType().toString(), seatDeck = seatsOnline.getDeck().toString(), seatCompartment = seatsOnline.getCompartment().toString(), 
   					seatPrice = seatsOnline.getPrice(), seatCraftType = seatsOnline.getCraftType();
   			
   			
   			String baggageDetailsString = "		\"Baggage\":[\r\n"
   					+ "            {\r\n"
   					+ "                \"AirlineCode\": \"" + airlineNoArray[0] + "\",\r\n"
   					+ "                \"FlightNumber\": \"" + airlineNoArray[1] + "\",\r\n"
   					+ "                \"WayType\": 2,\r\n"
   					+ "                \"Code\": \"" + bagCode + "\",\r\n"
   					+ "                \"Description\": 2,\r\n"
   					+ "                \"Weight\": " + bagWeight + ",\r\n"
   					+ "                \"Currency\": \"INR\",\r\n"
   					+ "                 \"Price\": " + bagPrice + ",\r\n"
   					+ "                 \"Origin\": \"" + productDetail.getCityOne() + "\",\r\n"
   					+ "                \"Destination\": \"" + productDetail.getCityTwo() + "\"\r\n"
   					+ "				}\r\n"
   					+ "			],\r\n";
   			
   			String mealDetailsString = "     \"MealDynamic\": [\r\n"
   					+ "        {\r\n"
   					+ "          \"AirlineCode\": \"" + airlineNoArray[0] + "\",\r\n"
   					+ "          \"FlightNumber\": \"" + airlineNoArray[1] + "\",\r\n"
   					+ "          \"WayType\": 2,\r\n"
   					+ "          \"Code\": \"" + mealCode + "\",\r\n"
   					+ "          \"Description\": 2,\r\n"
   					+ "          \"AirlineDescription\": \"" + mealName + "\",\r\n"
   					+ "          \"Quantity\": " + mealQuantity + ",\r\n"
   					+ "          \"Currency\": \"INR\",\r\n"
   					+ "          \"Price\": " + mealPrice + ",\r\n"
   					+ "          \"Origin\": \"" + productDetail.getCityOne() + "\",\r\n"
   					+ "          \"Destination\": \"" + productDetail.getCityTwo() + "\"\r\n"
   					+ "        }],\r\n";
   			
   			String seatDetailsString = "		\"SeatDynamic\": [\r\n"
   					+ "        {\r\n"
   					+ "	    \"AirlineCode\": \"" + airlineNoArray[0] + "\",\r\n"
   					+ "             \"FlightNumber\": \"" + airlineNoArray[1] + "\",\r\n"
   					+ "              \"CraftType\": \"" + productDetail.getCraftType() + "\",\r\n"
   					+ "               \"Origin\": \"" + productDetail.getCityOne() + "\",\r\n"
   					+ "                \"Destination\": \"" + productDetail.getCityTwo() + "\",\r\n"
   					+ "                \"AvailablityType\": " + seatAvailabilityType + ",\r\n"
   					+ "                \"Description\": 2,\r\n"
   					+ "                \"Code\": \"" + seatCode + "\",\r\n"
   					+ "                \"RowNo\": \"" + seatRowNo + "\",\r\n"
   					+ "                \"SeatNo\": \"" + seatNo + "\",\r\n"
   					+ "                \"SeatType\": " + seatType + ",\r\n"
   					+ "                \"SeatWayType\": 2,\r\n"
   					+ "                \"Compartment\": " + seatCompartment + ",\r\n"
   					+ "                \"Deck\": " + seatDeck + ",\r\n"
   					+ "                \"Currency\": \"INR\",\r\n"
   					+ "                \"Price\": " + seatPrice + "                                                                                                                                                                                                      \r\n"
   					+ "			\r\n"
   					+ "		}],\r\n";
   			
   			String baggageDetails = baggageDetailsString;
   			String mealDetails = mealDetailsString;
   			String seatDetails = seatDetailsString;
   			
   			if (travellerDetail.getPaxType().equals("1")) {
   				Integer fareInt = Integer.parseInt(basefareTravelerAdult) / searchId.getAdultNum();
   				Double taxInt = Double.parseDouble(taxTravelerAdult) / searchId.getAdultNum();
   				baseFare = "" + fareInt;
   				tax = "" + taxInt;
   				travelerService.updateBasefareTax(travellerDetail, baseFare, tax);
   				
   				if (countAdult == 0) {
						isLeadPax = "true";
					} else {
						isLeadPax = "false";
					}
   				countAdult++;
   				System.out.println(countAdult);
				} else if (travellerDetail.getPaxType().equals("2")) {
   				Integer fareInt = Integer.parseInt(basefareTravelerChild) / searchId.getChildNum();
   				Double taxInt = Double.parseDouble(taxTravelerChild) / searchId.getChildNum();
   				baseFare = "" + fareInt;
   				tax = "" + taxInt;
   				travelerService.updateBasefareTax(travellerDetail, baseFare, tax);
					isLeadPax = "false";
				} else {
   				Integer fareInt = Integer.parseInt(basefareTravelerInfant) / searchId.getInfantNum();
   				Double taxInt = Double.parseDouble(taxTravelerInfant) / searchId.getInfantNum();
   				baseFare = "" + fareInt;
   				tax = "" + taxInt;
   				travelerService.updateBasefareTax(travellerDetail, baseFare, tax);
					isLeadPax = "false";
				}
   			
   			String details = "{\r\n"
   					+ "		\"Title\": \"" + travellerDetail.getSalutation() + "\",\r\n"
   					+ "		\"FirstName\": \"" + travellerDetail.getFirstName() + "\",\r\n"
   					+ "		\"LastName\": \"" + travellerDetail.getLastName() + "\",\r\n"
   					+ "		\"PaxType\": " + travellerDetail.getPaxType() + ",\r\n"
   					+ "		\"DateOfBirth\": \"" + getDOB + "T00:00:00\",\r\n"
   					+ "		\"Gender\": " + genNum + ",\r\n"
   					+ "		\"PassportNo\": \"" + travellerDetail.getPassportNo() + "\",\r\n"
   					+ "		\"PassportExpiry\": \"" + travellerDetail.getPassportExpiry() + "T00:00:00\",\r\n"
   					+ "		\"AddressLine1\": \"123, Test\",\r\n"
   					+ "		\"AddressLine2\": \"\",\r\n"
   					+ "		\"Fare\": {\r\n"
   					+ "			\"BaseFare\": " + baseFare + ",\r\n"
   					+ "			\"Tax\": " + tax + ",\r\n"
   					+ "			\"YQTax\": 0.0,\r\n"
   					+ "			\"AdditionalTxnFeePub\": 0.0,\r\n"
   					+ "			\"AdditionalTxnFeeOfrd\": 0.0,\r\n"
   					+ "			\"OtherCharges\": 0.0\r\n"
   					+ "		},\r\n"
   					+ "		\"City\": \"Gurgaon\",\r\n"
   					+ "		\"CountryCode\": \"IN\",\r\n"
   					+ "		\"CountryName\": \"India\",      \r\n"
   					+ "     \"Nationality\": \"IN\",\r\n"
   					+ "		\"ContactNo\": \"" + order.getPhoneNumber() + "\",\r\n"
   					+ "		\"Email\": \"" + order.getContactEmail() + "\",\r\n"
   					+ "		\"IsLeadPax\": " + isLeadPax + ",\r\n"
   					+ "		\"FFAirlineCode\": \"" + airlineNoArray[0] + "\",\r\n"
   					+ "		\"FFNumber\": \"" + airlineNoArray[1] + "\",\r\n"
   					+ baggageDetails
   					+ mealDetails
   					+ seatDetails
   					+ "		\"GSTCompanyAddress\": \"\",\r\n"
   					+ "		\"GSTCompanyContactNumber\": \"\",\r\n"
   					+ "		\"GSTCompanyName\": \"\",\r\n"
   					+ "		\"GSTNumber\": \"\",\r\n"
   					+ "		\"GSTCompanyEmail\": \"\"\r\n"
   					+ "}";
   			
   			travelerDetailsArray.add(details);

				
   		}
       	
       	String arrayTraveler = travelerDetailsArray.stream().map(val -> String.valueOf(val)).collect(Collectors.joining(",", "[", "]"));

       	/* Ticket details */
       	StringBuilder responseBodyTicket = onlineFlightService.apiOnlineTicket(traceId, productDetail.getResultIndex(), arrayTraveler);
       	
       	JSONObject jsonObjTicket = new JSONObject(responseBodyTicket.toString()); 
       	System.out.println(jsonObjTicket);
       	logService.generateLog(jsonObjTicket.toString());
       	try {
				JSONObject jsonObjTicketResponse = jsonObjTicket.getJSONObject("Response").getJSONObject("Response").getJSONObject("FlightItinerary");
				JSONArray jsonArraySegment = jsonObjTicketResponse.getJSONArray("Segments");
				
				String terminalDep = "", terminalArr = "";
				for (int i = 0; i < jsonArraySegment.length(); i++) {
					JSONObject jsonObjectSegments = jsonArraySegment.getJSONObject(i);
					if (i == 0) {
						terminalDep = jsonObjectSegments.getJSONObject("Origin").getJSONObject("Airport").get("Terminal").toString();
					}
					if (i == (jsonArraySegment.length() - 1)) {
						terminalArr = jsonObjectSegments.getJSONObject("Destination").getJSONObject("Airport").get("Terminal").toString();
					}
				}
				
				String onlinePNR = jsonObjTicketResponse.get("PNR").toString();
				String onlineBookingId = jsonObjTicketResponse.get("BookingId").toString();

				productService.updateOtherDetails(productDetail, terminalDep, terminalArr);
				productService.updatePNROnline(productDetail, onlinePNR);
				productService.setTotalSeatOnline(productDetail, productDetail.getUploadSeats());
				updateBookingId(order, onlineBookingId);
				
				JSONObject jsonObjTicketResponseError = jsonObjTicket.getJSONObject("Response").getJSONObject("Error");
				hasErrorArr[0] = jsonObjTicketResponseError.get("ErrorCode").toString();
				hasErrorArr[1] = jsonObjTicketResponseError.get("ErrorMessage").toString();
				
				/* Get booking details */
		       	StringBuilder responseBodyBooking = onlineFlightService.apiOnlineGetBookingDetails(traceId, onlinePNR, onlineBookingId);
		       	
		       	JSONObject jsonObjBooking = new JSONObject(responseBodyBooking.toString());
		       	System.out.println(jsonObjBooking);
		       	logService.generateLog(jsonObjBooking.toString()); 
				
			} catch (JSONException json) {
//				json.printStackTrace();
				JSONObject jsonObjTicketResponseError = jsonObjTicket.getJSONObject("Response").getJSONObject("Error");
				hasErrorArr[0] = jsonObjTicketResponseError.get("ErrorCode").toString();
				hasErrorArr[1] = jsonObjTicketResponseError.get("ErrorMessage").toString();
				
			} 
		}
		
		return hasErrorArr;
	}
	
	public String[] bookingDetails(Order order, ProductDetail productDetail, String basefareTravelerAdult, String taxTravelerAdult, String basefareTravelerChild, String taxTravelerChild, 
			 String basefareTravelerInfant, String taxTravelerInfant, SearchHistory searchId, String discount, String tdsOnIncentive, String tdsOnCommission, String tdsOnPLB, String otherCharges, 
			 String publishedFare, String offeredFare, String serviceFee, String traceId) throws IOException {
		List<String> travelerDetailsArray = new ArrayList<String>();
		List<TravellerDetail> travelers = productService.findTravellerByOrderANDProductDetail(productDetail, order);
		String[] hasErrorArr = new String[2];
		
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
   				Integer fareInt = Integer.parseInt(basefareTravelerAdult) / searchId.getAdultNum();
   				Integer taxInt = Integer.parseInt(taxTravelerAdult) / searchId.getAdultNum();
   				baseFare = "" + fareInt;
   				tax = "" + taxInt;
   				travelerService.updateBasefareTax(travellerDetail, baseFare, tax);
   				
   				if (countAdult == 0) {
						isLeadPax = "true";
					} else {
						isLeadPax = "false";
					}
   				countAdult++;
   				System.out.println(countAdult);
				} else if (travellerDetail.getPaxType().equals("2")) {
   				Integer fareInt = Integer.parseInt(basefareTravelerChild) / searchId.getChildNum();
   				Integer taxInt = Integer.parseInt(taxTravelerChild) / searchId.getChildNum();
   				baseFare = "" + fareInt;
   				tax = "" + taxInt;
   				travelerService.updateBasefareTax(travellerDetail, baseFare, tax);
					isLeadPax = "false";
				} else {
   				Integer fareInt = Integer.parseInt(basefareTravelerInfant) / searchId.getInfantNum();
   				Integer taxInt = Integer.parseInt(taxTravelerInfant) / searchId.getInfantNum();
   				baseFare = "" + fareInt;
   				tax = "" + taxInt;
   				travelerService.updateBasefareTax(travellerDetail, baseFare, tax);
					isLeadPax = "false";
				}
   			
   			String details = "{\r\n"
   					+ "		\"Title\": \"" + travellerDetail.getSalutation() + "\",\r\n"
   					+ "		\"FirstName\": \"" + travellerDetail.getFirstName() + "\",\r\n"
   					+ "		\"LastName\": \"" + travellerDetail.getLastName() + "\",\r\n"
   					+ "		\"PaxType\": " + travellerDetail.getPaxType() + ",\r\n"
   					+ "		\"DateOfBirth\": \"" + getDOB + "T00:00:00\",\r\n"
   					+ "		\"Gender\": " + genNum + ",\r\n"
   					+ "		\"PassportNo\": \"" + travellerDetail.getPassportNo() + "\",\r\n"
   					+ "		\"PassportExpiry\": \"" + travellerDetail.getPassportExpiry() + "T00:00:00\",\r\n"
   					+ "		\"AddressLine1\": \"123, Test\",\r\n"
   					+ "		\"AddressLine2\": \"\",\r\n"
   					+ "		\"Fare\": {\r\n"
   					+ "			\"BaseFare\": " + baseFare + ",\r\n"
   					+ "			\"Tax\": " + tax + ",\r\n"
   					+ "			\"YQTax\": 0.0,\r\n"
   					+ "			\"AdditionalTxnFeePub\": 0.0,\r\n"
   					+ "			\"AdditionalTxnFeeOfrd\": 0.0,\r\n"
   					+ "			\"OtherCharges\": " + otherCharges + ",\r\n"
   					+ "			\"Discount\": " + discount + ",\r\n"
   					+ "			\"PublishedFare\": " + publishedFare + ",\r\n"
   					+ "			\"OfferedFare\": " + offeredFare + ",\r\n"
   					+ "			\"TdsOnCommission\": " + tdsOnCommission + ",\r\n"
   					+ "			\"TdsOnPLB\": " + tdsOnPLB + ",\r\n"
   					+ "			\"TdsOnIncentive\": " + tdsOnIncentive + ",\r\n"
   					+ "			\"ServiceFee\": " + serviceFee + "\r\n"
   					+ "		},\r\n"
   					+ "		\"City\": \"Gurgaon\",\r\n"
   					+ "		\"CountryCode\": \"IN\",\r\n"
   					+ "		\"CountryName\": \"India\",      \r\n"
   					+ "     \"Nationality\": \"IN\",\r\n"
   					+ "		\"ContactNo\": \"" + order.getPhoneNumber() + "\",\r\n"
   					+ "		\"Email\": \"" + order.getContactEmail() + "\",\r\n"
   					+ "		\"IsLeadPax\": " + isLeadPax + ",\r\n"
   					+ "		\"FFAirlineCode\": \"" + productDetailsController.airlineCOde + "\",\r\n"
   					+ "		\"FFNumber\": \"" + productDetailsController.flightNumber + "\",\r\n"
   					+ "		\"GSTCompanyAddress\": \"\",\r\n"
   					+ "		\"GSTCompanyContactNumber\": \"\",\r\n"
   					+ "		\"GSTCompanyName\": \"\",\r\n"
   					+ "		\"GSTNumber\": \"\",\r\n"
   					+ "		\"GSTCompanyEmail\": \"\"\r\n"
   					+ "}";
   			
   			travelerDetailsArray.add(details);

				
   		}
       	
       	String arrayTraveler = travelerDetailsArray.stream().map(val -> String.valueOf(val)).collect(Collectors.joining(",", "[", "]"));

       	/* Ticket details */
       	StringBuilder responseBodyBook = onlineFlightService.apiOnlineBookingNonLCC(traceId, productDetail.getResultIndex(), arrayTraveler);
       	
       	JSONObject jsonObjBooking = new JSONObject(responseBodyBook.toString()); 
       	System.out.println(jsonObjBooking);
       	logService.generateLog(jsonObjBooking.toString());
       	
       	String bookingId = "", pnrBooking = "";
       	Integer bookingIdInt = 0;
       	
       	try {
				JSONObject jsonObjBookingResponse = jsonObjBooking.getJSONObject("Response").getJSONObject("Response").getJSONObject("FlightItinerary");
					pnrBooking = jsonObjBookingResponse.get("PNR").toString();
					bookingId  = jsonObjBookingResponse.get("BookingId").toString();
					bookingIdInt = Integer.parseInt(bookingId);
				
			} catch (Exception e) {
				System.out.println("Not found!!");
			}
       	
       	
       	/* Ticket details */
        StringBuilder responseBodyTicket = onlineFlightService.apiOnlineTicketNonLcc(traceId, pnrBooking, bookingIdInt);
       	
       	JSONObject jsonObjTicket = new JSONObject(responseBodyTicket.toString()); 
       	System.out.println(jsonObjTicket);
       	logService.generateLog(jsonObjTicket.toString());
       	try {
				JSONObject jsonObjTicketResponse = jsonObjTicket.getJSONObject("Response").getJSONObject("Response").getJSONObject("FlightItinerary");
				JSONArray jsonArraySegment = jsonObjTicketResponse.getJSONArray("Segments");
//       		JSONObject jsonObjectSegments = new JSONObject();
				
				String terminalDep = "", terminalArr = "";
				for (int i = 0; i < jsonArraySegment.length(); i++) {
					JSONObject jsonObjectSegments = jsonArraySegment.getJSONObject(i);
					if (i == 0) {
						terminalDep = jsonObjectSegments.getJSONObject("Origin").getJSONObject("Airport").get("Terminal").toString();
					}
					if (i == (jsonArraySegment.length() - 1)) {
						terminalArr = jsonObjectSegments.getJSONObject("Destination").getJSONObject("Airport").get("Terminal").toString();
					}
				}
				
				String onlinePNR = jsonObjTicketResponse.get("PNR").toString();
				String onlineBookingId = jsonObjTicketResponse.get("BookingId").toString();

				productService.updateOtherDetails(productDetail, terminalDep, terminalArr);
				productService.updatePNROnline(productDetail, onlinePNR);
				productService.setTotalSeatOnline(productDetail, productDetail.getUploadSeats());
				updateBookingId(order, onlineBookingId);
				
				JSONObject jsonObjTicketResponseError = jsonObjTicket.getJSONObject("Response").getJSONObject("Error");
				hasErrorArr[0] = jsonObjTicketResponseError.get("ErrorCode").toString();
				hasErrorArr[1] = jsonObjTicketResponseError.get("ErrorMessage").toString();
				

				/* Get booking details */
		       	StringBuilder responseBodyBooking = onlineFlightService.apiOnlineGetBookingDetails(traceId, onlinePNR, onlineBookingId);
		       	
		       	JSONObject jsonObjGetBooking = new JSONObject(responseBodyBooking.toString());
		       	System.out.println(jsonObjGetBooking);
		       	logService.generateLog(jsonObjGetBooking.toString()); 
						
			} catch (JSONException json) {
				 //Error response
				JSONObject jsonObjTicketResponseError = jsonObjTicket.getJSONObject("Response").getJSONObject("Error");
				hasErrorArr[0] = jsonObjTicketResponseError.get("ErrorCode").toString();
				hasErrorArr[1] = jsonObjTicketResponseError.get("ErrorMessage").toString();
			} 

		}
		return hasErrorArr;
	}

	public String[] ticketDetailsInternational(Order order, ProductDetail productDetail, String basefareTravelerAdult, String taxTravelerAdult, String basefareTravelerChild, String taxTravelerChild, 
			 String basefareTravelerInfant, String taxTravelerInfant, SearchHistory searchId, String traceId ) throws MalformedURLException, IOException {
		String[] hasErrorArr = new String[2];
		
		if (!productDetail.getTraceId().equals("offline")) {
			
			List<String> travelerDetailsArray = new ArrayList<String>();
			List<TravellerDetail> travelers = productService.findTravellerByOrderANDProductDetail(productDetail, order);
			
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
	   			
	   			//baggage information
	   			String bagCode = baggageOnline.getCode(), bagWeight = baggageOnline.getWeight(), bagPrice = baggageOnline.getPrice();
	   			
	   			//meal information
	   			String mealCode = mealsOnline.getCode(), mealName = mealsOnline.getName(), mealQuantity = mealsOnline.getQuantity(), mealPrice = mealsOnline.getPrice();
	   			
	   			//seat information
	   			@SuppressWarnings("unused")
					String seatAvailabilityType = seatsOnline.getAvailablityType().toString(), seatCode = seatsOnline.getCode(), seatRowNo = seatsOnline.getRowNo(), 
	   					seatNo = seatsOnline.getSeatNo(), seatType = seatsOnline.getSeatType().toString(), seatDeck = seatsOnline.getDeck().toString(), seatCompartment = seatsOnline.getCompartment().toString(), 
	   					seatPrice = seatsOnline.getPrice(), seatCraftType = seatsOnline.getCraftType();
	   			String baggageDetailsString = "";
	   			String mealDetailsString = "";
	   			String seatDetailsString = "";
	   			
	   			if (!bagCode.equals("NoBaggage")) {
					baggageDetailsString = "		\"Baggage\":[\r\n"
	   					+ "            {\r\n"
	   					+ "                \"AirlineCode\": \"" + airlineNoArray[0] + "\",\r\n"
	   					+ "                \"FlightNumber\": \"" + airlineNoArray[1] + "\",\r\n"
	   					+ "                \"WayType\": 2,\r\n"
	   					+ "                \"Code\": \"" + bagCode + "\",\r\n"
	   					+ "                \"Description\": 2,\r\n"
	   					+ "                \"Weight\": " + bagWeight + ",\r\n"
	   					+ "                \"Currency\": \"INR\",\r\n"
	   					+ "                 \"Price\": " + bagPrice + ",\r\n"
	   					+ "                 \"Origin\": \"" + productDetail.getCityOne() + "\",\r\n"
	   					+ "                \"Destination\": \"" + productDetail.getCityTwo() + "\"\r\n"
	   					+ "				}\r\n"
	   					+ "			],\r\n";
				}
	   			
	   			if (!mealCode.equals("NoMeal")) {
					mealDetailsString = "     \"MealDynamic\": [\r\n"
	   					+ "        {\r\n"
	   					+ "          \"AirlineCode\": \"" + airlineNoArray[0] + "\",\r\n"
	   					+ "          \"FlightNumber\": \"" + airlineNoArray[1] + "\",\r\n"
	   					+ "          \"WayType\": 2,\r\n"
	   					+ "          \"Code\": \"" + mealCode + "\",\r\n"
	   					+ "          \"Description\": 2,\r\n"
	   					+ "          \"AirlineDescription\": \"" + mealName + "\",\r\n"
	   					+ "          \"Quantity\": " + mealQuantity + ",\r\n"
	   					+ "          \"Currency\": \"INR\",\r\n"
	   					+ "          \"Price\": " + mealPrice + ",\r\n"
	   					+ "          \"Origin\": \"" + productDetail.getCityOne() + "\",\r\n"
	   					+ "          \"Destination\": \"" + productDetail.getCityTwo() + "\"\r\n"
	   					+ "        }],\r\n";
				}
	   			
	   			if (!seatCode.equals("NoSeat")) {
					seatDetailsString = "		\"SeatDynamic\": [\r\n"
	   					+ "        {\r\n"
	   					+ "	    \"AirlineCode\": \"" + airlineNoArray[0] + "\",\r\n"
	   					+ "             \"FlightNumber\": \"" + airlineNoArray[1] + "\",\r\n"
	   					+ "              \"CraftType\": \"" + productDetail.getCraftType() + "\",\r\n"
	   					+ "               \"Origin\": \"" + productDetail.getCityOne() + "\",\r\n"
	   					+ "                \"Destination\": \"" + productDetail.getCityTwo() + "\",\r\n"
	   					+ "                \"AvailablityType\": " + seatAvailabilityType + ",\r\n"
	   					+ "                \"Description\": 2,\r\n"
	   					+ "                \"Code\": \"" + seatCode + "\",\r\n"
	   					+ "                \"RowNo\": \"" + seatRowNo + "\",\r\n"
	   					+ "                \"SeatNo\": \"" + seatNo + "\",\r\n"
	   					+ "                \"SeatType\": " + seatType + ",\r\n"
	   					+ "                \"SeatWayType\": 2,\r\n"
	   					+ "                \"Compartment\": " + seatCompartment + ",\r\n"
	   					+ "                \"Deck\": " + seatDeck + ",\r\n"
	   					+ "                \"Currency\": \"INR\",\r\n"
	   					+ "                \"Price\": " + seatPrice + "                                                                                                                                                                                                      \r\n"
	   					+ "			\r\n"
	   					+ "		}],\r\n";
				}
	   			
	   			
	   			String baggageDetails = baggageDetailsString;
	   			String mealDetails = mealDetailsString;
	   			String seatDetails = seatDetailsString;
	   			
	   			if (travellerDetail.getPaxType().equals("1")) {
	   				Integer fareInt = Integer.parseInt(basefareTravelerAdult) / searchId.getAdultNum();
	   				Double taxInt = Double.parseDouble(taxTravelerAdult) / searchId.getAdultNum();
	   				baseFare = "" + fareInt;
	   				tax = "" + taxInt;
	   				travelerService.updateBasefareTax(travellerDetail, baseFare, tax);
	   				
	   				if (countAdult == 0) {
							isLeadPax = "true";
						} else {
							isLeadPax = "false";
						}
	   				countAdult++;
	   				System.out.println(countAdult);
					} else if (travellerDetail.getPaxType().equals("2")) {
	   				Integer fareInt = Integer.parseInt(basefareTravelerChild) / searchId.getChildNum();
	   				Double taxInt = Double.parseDouble(taxTravelerChild) / searchId.getChildNum();
	   				baseFare = "" + fareInt;
	   				tax = "" + taxInt;
	   				travelerService.updateBasefareTax(travellerDetail, baseFare, tax);
						isLeadPax = "false";
					} else {
	   				Integer fareInt = Integer.parseInt(basefareTravelerInfant) / searchId.getInfantNum();
	   				Double taxInt = Double.parseDouble(taxTravelerInfant) / searchId.getInfantNum();
	   				baseFare = "" + fareInt;
	   				tax = "" + taxInt;
	   				travelerService.updateBasefareTax(travellerDetail, baseFare, tax);
						isLeadPax = "false";
					}
	   			
	   			String details = "{\r\n"
	   					+ "		\"Title\": \"" + travellerDetail.getSalutation() + "\",\r\n"
	   					+ "		\"FirstName\": \"" + travellerDetail.getFirstName() + "\",\r\n"
	   					+ "		\"LastName\": \"" + travellerDetail.getLastName() + "\",\r\n"
	   					+ "		\"PaxType\": " + travellerDetail.getPaxType() + ",\r\n"
	   					+ "		\"DateOfBirth\": \"" + getDOB + "T00:00:00\",\r\n"
	   					+ "		\"Gender\": " + genNum + ",\r\n"
	   					+ "		\"PassportNo\": \"" + travellerDetail.getPassportNo() + "\",\r\n"
	   					+ "		\"PassportExpiry\": \"" + travellerDetail.getPassportExpiry() + "T00:00:00\",\r\n"
	   					+ "		\"AddressLine1\": \"123, Test\",\r\n"
	   					+ "		\"AddressLine2\": \"\",\r\n"
	   					+ "		\"Fare\": {\r\n"
	   					+ "			\"BaseFare\": " + baseFare + ",\r\n"
	   					+ "			\"Tax\": " + tax + ",\r\n"
	   					+ "			\"YQTax\": 0.0,\r\n"
	   					+ "			\"AdditionalTxnFeePub\": 0.0,\r\n"
	   					+ "			\"AdditionalTxnFeeOfrd\": 0.0,\r\n"
	   					+ "			\"OtherCharges\": 0.0\r\n"
	   					+ "		},\r\n"
	   					+ "		\"City\": \"Gurgaon\",\r\n"
	   					+ "		\"CountryCode\": \"IN\",\r\n"
	   					+ "		\"CountryName\": \"India\",      \r\n"
	   					+ "     \"Nationality\": \"IN\",\r\n"
	   					+ "		\"ContactNo\": \"" + order.getPhoneNumber() + "\",\r\n"
	   					+ "		\"Email\": \"" + order.getContactEmail() + "\",\r\n"
	   					+ "		\"IsLeadPax\": " + isLeadPax + ",\r\n"
	   					+ "		\"FFAirlineCode\": \"" + airlineNoArray[0] + "\",\r\n"
	   					+ "		\"FFNumber\": \"" + airlineNoArray[1] + "\",\r\n"
	   					+ baggageDetails
	   					+ mealDetails
	   					+ seatDetails
	   					+ "		\"GSTCompanyAddress\": \"\",\r\n"
	   					+ "		\"GSTCompanyContactNumber\": \"\",\r\n"
	   					+ "		\"GSTCompanyName\": \"\",\r\n"
	   					+ "		\"GSTNumber\": \"\",\r\n"
	   					+ "		\"GSTCompanyEmail\": \"\"\r\n"
	   					+ "}";
	   			
	   			travelerDetailsArray.add(details);
	
					
   		}
       	
       	String arrayTraveler = travelerDetailsArray.stream().map(val -> String.valueOf(val)).collect(Collectors.joining(",", "[", "]"));

       	/* Ticket details */
       	StringBuilder responseBodyTicket = onlineFlightService.apiOnlineTicket(traceId, productDetail.getResultIndex(), arrayTraveler);
       	
       	JSONObject jsonObjTicket = new JSONObject(responseBodyTicket.toString()); 
       	System.out.println(jsonObjTicket);
       	logService.generateLog(jsonObjTicket.toString());
       	try {
				JSONObject jsonObjTicketResponse = jsonObjTicket.getJSONObject("Response").getJSONObject("Response").getJSONObject("FlightItinerary");
				JSONArray jsonArraySegment = jsonObjTicketResponse.getJSONArray("Segments");
				
				String terminalDep = "", terminalArr = "";
				for (int i = 0; i < jsonArraySegment.length(); i++) {
					JSONObject jsonObjectSegments = jsonArraySegment.getJSONObject(i);
					if (i == 0) {
						terminalDep = jsonObjectSegments.getJSONObject("Origin").getJSONObject("Airport").get("Terminal").toString();
					}
					if (i == (jsonArraySegment.length() - 1)) {
						terminalArr = jsonObjectSegments.getJSONObject("Destination").getJSONObject("Airport").get("Terminal").toString();
					}
				}
				
				String onlinePNR = jsonObjTicketResponse.get("PNR").toString();
				String onlineBookingId = jsonObjTicketResponse.get("BookingId").toString();

				productService.updateOtherDetails(productDetail, terminalDep, terminalArr);
				productService.updatePNROnline(productDetail, onlinePNR);
				productService.setTotalSeatOnline(productDetail, productDetail.getUploadSeats());
				updateBookingId(order, onlineBookingId);
				
				/* Get Booking Details */
				StringBuilder responseBodyGetBookingDetails = onlineFlightService.apiOnlineGetBookingDetails(traceId, onlinePNR, onlineBookingId);
				
				JSONObject jsonObjGetBookingDetails = new JSONObject(responseBodyGetBookingDetails.toString());
		       	System.out.println(jsonObjGetBookingDetails);
		       	logService.generateLog(jsonObjGetBookingDetails.toString()); 
				
				JSONObject jsonObjTicketResponseError = jsonObjTicket.getJSONObject("Response").getJSONObject("Error");
				hasErrorArr[0] = jsonObjTicketResponseError.get("ErrorCode").toString();
				hasErrorArr[1] = jsonObjTicketResponseError.get("ErrorMessage").toString();
				
				
			} catch (JSONException json) {
				
				JSONObject jsonObjTicketResponseError = jsonObjTicket.getJSONObject("Response").getJSONObject("Error");
				hasErrorArr[0] = jsonObjTicketResponseError.get("ErrorCode").toString();
				hasErrorArr[1] = jsonObjTicketResponseError.get("ErrorMessage").toString();
			} 

		}
		return hasErrorArr;
	}

	public String[] ticketDetailsInternationalReturn(Order order, ProductDetail productDetail, Order orderTwo, ProductDetail productDetailTwo, String basefareTravelerAdult, String taxTravelerAdult, String basefareTravelerChild, String taxTravelerChild, 
			 String basefareTravelerInfant, String taxTravelerInfant, SearchHistory searchId, String traceId ) throws MalformedURLException, IOException {
		String[] hasErrorArr = new String[2];
		
		if (!productDetail.getTraceId().equals("offline")) {
			
			List<String> travelerDetailsArray = new ArrayList<String>();
			List<TravellerDetail> travelers = productService.findTravellerByOrderANDProductDetail(productDetail, order);
			
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
	   			
	   			//baggage information
	   			String bagCode = baggageOnline.getCode(), bagWeight = baggageOnline.getWeight(), bagPrice = baggageOnline.getPrice();
	   			
	   			//meal information
	   			String mealCode = mealsOnline.getCode(), mealName = mealsOnline.getName(), mealQuantity = mealsOnline.getQuantity(), mealPrice = mealsOnline.getPrice();
	   			
	   			//seat information
	   			@SuppressWarnings("unused")
					String seatAvailabilityType = seatsOnline.getAvailablityType().toString(), seatCode = seatsOnline.getCode(), seatRowNo = seatsOnline.getRowNo(), 
	   					seatNo = seatsOnline.getSeatNo(), seatType = seatsOnline.getSeatType().toString(), seatDeck = seatsOnline.getDeck().toString(), seatCompartment = seatsOnline.getCompartment().toString(), 
	   					seatPrice = seatsOnline.getPrice(), seatCraftType = seatsOnline.getCraftType();
	   			String baggageDetailsString = "";
	   			String mealDetailsString = "";
	   			String seatDetailsString = "";
	   			
	   			if (!bagCode.equals("NoBaggage")) {
					baggageDetailsString = "		\"Baggage\":[\r\n"
	   					+ "            {\r\n"
	   					+ "                \"AirlineCode\": \"" + airlineNoArray[0] + "\",\r\n"
	   					+ "                \"FlightNumber\": \"" + airlineNoArray[1] + "\",\r\n"
	   					+ "                \"WayType\": 2,\r\n"
	   					+ "                \"Code\": \"" + bagCode + "\",\r\n"
	   					+ "                \"Description\": 2,\r\n"
	   					+ "                \"Weight\": " + bagWeight + ",\r\n"
	   					+ "                \"Currency\": \"INR\",\r\n"
	   					+ "                 \"Price\": " + bagPrice + ",\r\n"
	   					+ "                 \"Origin\": \"" + productDetail.getCityOne() + "\",\r\n"
	   					+ "                \"Destination\": \"" + productDetail.getCityTwo() + "\"\r\n"
	   					+ "				}\r\n"
	   					+ "			],\r\n";
				}
	   			
	   			if (!mealCode.equals("NoMeal")) {
					mealDetailsString = "     \"MealDynamic\": [\r\n"
	   					+ "        {\r\n"
	   					+ "          \"AirlineCode\": \"" + airlineNoArray[0] + "\",\r\n"
	   					+ "          \"FlightNumber\": \"" + airlineNoArray[1] + "\",\r\n"
	   					+ "          \"WayType\": 2,\r\n"
	   					+ "          \"Code\": \"" + mealCode + "\",\r\n"
	   					+ "          \"Description\": 2,\r\n"
	   					+ "          \"AirlineDescription\": \"" + mealName + "\",\r\n"
	   					+ "          \"Quantity\": " + mealQuantity + ",\r\n"
	   					+ "          \"Currency\": \"INR\",\r\n"
	   					+ "          \"Price\": " + mealPrice + ",\r\n"
	   					+ "          \"Origin\": \"" + productDetail.getCityOne() + "\",\r\n"
	   					+ "          \"Destination\": \"" + productDetail.getCityTwo() + "\"\r\n"
	   					+ "        }],\r\n";
				}
	   			
	   			if (!seatCode.equals("NoSeat")) {
					seatDetailsString = "		\"SeatDynamic\": [\r\n"
	   					+ "        {\r\n"
	   					+ "	    \"AirlineCode\": \"" + airlineNoArray[0] + "\",\r\n"
	   					+ "             \"FlightNumber\": \"" + airlineNoArray[1] + "\",\r\n"
	   					+ "              \"CraftType\": \"" + productDetail.getCraftType() + "\",\r\n"
	   					+ "               \"Origin\": \"" + productDetail.getCityOne() + "\",\r\n"
	   					+ "                \"Destination\": \"" + productDetail.getCityTwo() + "\",\r\n"
	   					+ "                \"AvailablityType\": " + seatAvailabilityType + ",\r\n"
	   					+ "                \"Description\": 2,\r\n"
	   					+ "                \"Code\": \"" + seatCode + "\",\r\n"
	   					+ "                \"RowNo\": \"" + seatRowNo + "\",\r\n"
	   					+ "                \"SeatNo\": \"" + seatNo + "\",\r\n"
	   					+ "                \"SeatType\": " + seatType + ",\r\n"
	   					+ "                \"SeatWayType\": 2,\r\n"
	   					+ "                \"Compartment\": " + seatCompartment + ",\r\n"
	   					+ "                \"Deck\": " + seatDeck + ",\r\n"
	   					+ "                \"Currency\": \"INR\",\r\n"
	   					+ "                \"Price\": " + seatPrice + "                                                                                                                                                                                                      \r\n"
	   					+ "			\r\n"
	   					+ "		}],\r\n";
				}
	   			
	   			
	   			String baggageDetails = baggageDetailsString;
	   			String mealDetails = mealDetailsString;
	   			String seatDetails = seatDetailsString;
	   			
	   			if (travellerDetail.getPaxType().equals("1")) {
	   				Integer fareInt = Integer.parseInt(basefareTravelerAdult) / searchId.getAdultNum();
	   				Double taxInt = Double.parseDouble(taxTravelerAdult) / searchId.getAdultNum();
	   				baseFare = "" + fareInt;
	   				tax = "" + taxInt;
	   				travelerService.updateBasefareTax(travellerDetail, baseFare, tax);
	   				
	   				if (countAdult == 0) {
							isLeadPax = "true";
						} else {
							isLeadPax = "false";
						}
	   				countAdult++;
	   				System.out.println(countAdult);
					} else if (travellerDetail.getPaxType().equals("2")) {
	   				Integer fareInt = Integer.parseInt(basefareTravelerChild) / searchId.getChildNum();
	   				Double taxInt = Double.parseDouble(taxTravelerChild) / searchId.getChildNum();
	   				baseFare = "" + fareInt;
	   				tax = "" + taxInt;
	   				travelerService.updateBasefareTax(travellerDetail, baseFare, tax);
						isLeadPax = "false";
					} else {
	   				Integer fareInt = Integer.parseInt(basefareTravelerInfant) / searchId.getInfantNum();
	   				Double taxInt = Double.parseDouble(taxTravelerInfant) / searchId.getInfantNum();
	   				baseFare = "" + fareInt;
	   				tax = "" + taxInt;
	   				travelerService.updateBasefareTax(travellerDetail, baseFare, tax);
						isLeadPax = "false";
					}
	   			
	   			String details = "{\r\n"
	   					+ "		\"Title\": \"" + travellerDetail.getSalutation() + "\",\r\n"
	   					+ "		\"FirstName\": \"" + travellerDetail.getFirstName() + "\",\r\n"
	   					+ "		\"LastName\": \"" + travellerDetail.getLastName() + "\",\r\n"
	   					+ "		\"PaxType\": " + travellerDetail.getPaxType() + ",\r\n"
	   					+ "		\"DateOfBirth\": \"" + getDOB + "T00:00:00\",\r\n"
	   					+ "		\"Gender\": " + genNum + ",\r\n"
	   					+ "		\"PassportNo\": \"" + travellerDetail.getPassportNo() + "\",\r\n"
	   					+ "		\"PassportExpiry\": \"" + travellerDetail.getPassportExpiry() + "T00:00:00\",\r\n"
	   					+ "		\"AddressLine1\": \"123, Test\",\r\n"
	   					+ "		\"AddressLine2\": \"\",\r\n"
	   					+ "		\"Fare\": {\r\n"
	   					+ "			\"BaseFare\": " + baseFare + ",\r\n"
	   					+ "			\"Tax\": " + tax + ",\r\n"
	   					+ "			\"YQTax\": 0.0,\r\n"
	   					+ "			\"AdditionalTxnFeePub\": 0.0,\r\n"
	   					+ "			\"AdditionalTxnFeeOfrd\": 0.0,\r\n"
	   					+ "			\"OtherCharges\": 0.0\r\n"
	   					+ "		},\r\n"
	   					+ "		\"City\": \"Gurgaon\",\r\n"
	   					+ "		\"CountryCode\": \"IN\",\r\n"
	   					+ "		\"CountryName\": \"India\",      \r\n"
	   					+ "     \"Nationality\": \"IN\",\r\n"
	   					+ "		\"ContactNo\": \"" + order.getPhoneNumber() + "\",\r\n"
	   					+ "		\"Email\": \"" + order.getContactEmail() + "\",\r\n"
	   					+ "		\"IsLeadPax\": " + isLeadPax + ",\r\n"
	   					+ "		\"FFAirlineCode\": \"" + airlineNoArray[0] + "\",\r\n"
	   					+ "		\"FFNumber\": \"" + airlineNoArray[1] + "\",\r\n"
	   					+ baggageDetails
	   					+ mealDetails
	   					+ seatDetails
	   					+ "		\"GSTCompanyAddress\": \"\",\r\n"
	   					+ "		\"GSTCompanyContactNumber\": \"\",\r\n"
	   					+ "		\"GSTCompanyName\": \"\",\r\n"
	   					+ "		\"GSTNumber\": \"\",\r\n"
	   					+ "		\"GSTCompanyEmail\": \"\"\r\n"
	   					+ "}";
	   			
	   			travelerDetailsArray.add(details);
	
					
   		}
       	
       	String arrayTraveler = travelerDetailsArray.stream().map(val -> String.valueOf(val)).collect(Collectors.joining(",", "[", "]"));

       	/* Ticket details */
       	StringBuilder responseBodyTicket = onlineFlightService.apiOnlineTicket(traceId, productDetail.getResultIndex(), arrayTraveler);
       	
       	JSONObject jsonObjTicket = new JSONObject(responseBodyTicket.toString()); 
       	System.out.println(jsonObjTicket);
       	logService.generateLog(jsonObjTicket.toString());
       	try {
				JSONObject jsonObjTicketResponse = jsonObjTicket.getJSONObject("Response").getJSONObject("Response").getJSONObject("FlightItinerary");
				JSONArray jsonArraySegment = jsonObjTicketResponse.getJSONArray("Segments");
				
				String terminalDep = "", terminalArr = "";
				for (int i = 0; i < jsonArraySegment.length(); i++) {
					JSONObject jsonObjectSegments = jsonArraySegment.getJSONObject(i);
					if (i == 0) {
						terminalDep = jsonObjectSegments.getJSONObject("Origin").getJSONObject("Airport").get("Terminal").toString();
					}
					if (i == (jsonArraySegment.length() - 1)) {
						terminalArr = jsonObjectSegments.getJSONObject("Destination").getJSONObject("Airport").get("Terminal").toString();
					}
				}
				
				String onlinePNR = jsonObjTicketResponse.get("PNR").toString();
				String onlineBookingId = jsonObjTicketResponse.get("BookingId").toString();

				productService.updateOtherDetails(productDetail, terminalDep, terminalArr);
				productService.updatePNROnline(productDetail, onlinePNR);
				productService.setTotalSeatOnline(productDetail, productDetail.getUploadSeats());
				updateBookingId(order, onlineBookingId);

				productService.updateOtherDetails(productDetailTwo, terminalDep, terminalArr);
				productService.updatePNROnline(productDetailTwo, onlinePNR);
				productService.setTotalSeatOnline(productDetailTwo, productDetailTwo.getUploadSeats());
				updateBookingId(orderTwo, onlineBookingId);
				
				/* Get Booking Details */
				StringBuilder responseBodyGetBookingDetails = onlineFlightService.apiOnlineGetBookingDetails(traceId, onlinePNR, onlineBookingId);
				
				JSONObject jsonObjGetBookingDetails = new JSONObject(responseBodyGetBookingDetails.toString());
		       	System.out.println(jsonObjGetBookingDetails);
		       	logService.generateLog(jsonObjGetBookingDetails.toString()); 
				
				JSONObject jsonObjTicketResponseError = jsonObjTicket.getJSONObject("Response").getJSONObject("Error");
				hasErrorArr[0] = jsonObjTicketResponseError.get("ErrorCode").toString();
				hasErrorArr[1] = jsonObjTicketResponseError.get("ErrorMessage").toString();
				
				
			} catch (JSONException json) {
				
				JSONObject jsonObjTicketResponseError = jsonObjTicket.getJSONObject("Response").getJSONObject("Error");
				hasErrorArr[0] = jsonObjTicketResponseError.get("ErrorCode").toString();
				hasErrorArr[1] = jsonObjTicketResponseError.get("ErrorMessage").toString();
			} 

		}
		return hasErrorArr;
	}

	public String[] bookingDetailsInternationalReturn(Order order, ProductDetail productDetail, Order orderTwo, ProductDetail productDetailTwo, String basefareTravelerAdult, String taxTravelerAdult, String basefareTravelerChild, String taxTravelerChild, 
			 String basefareTravelerInfant, String taxTravelerInfant, SearchHistory searchId, String discount, String tdsOnIncentive, String tdsOnCommission, String tdsOnPLB, String otherCharges, 
			 String publishedFare, String offeredFare, String serviceFee, String traceId) throws IOException {
		List<String> travelerDetailsArray = new ArrayList<String>();
		List<TravellerDetail> travelers = productService.findTravellerByOrderANDProductDetail(productDetail, order);
		String[] hasErrorArr = new String[2];
		
		if (!productDetail.getTraceId().equals("offline")) {
			
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
   				Integer fareInt = Integer.parseInt(basefareTravelerAdult) / searchId.getAdultNum();
   				Integer taxInt = Integer.parseInt(taxTravelerAdult) / searchId.getAdultNum();
   				baseFare = "" + fareInt;
   				tax = "" + taxInt;
   				travelerService.updateBasefareTax(travellerDetail, baseFare, tax);
   				
   				if (countAdult == 0) {
						isLeadPax = "true";
					} else {
						isLeadPax = "false";
					}
   				countAdult++;
   				System.out.println(countAdult);
				} else if (travellerDetail.getPaxType().equals("2")) {
   				Integer fareInt = Integer.parseInt(basefareTravelerChild) / searchId.getChildNum();
   				Integer taxInt = Integer.parseInt(taxTravelerChild) / searchId.getChildNum();
   				baseFare = "" + fareInt;
   				tax = "" + taxInt;
   				travelerService.updateBasefareTax(travellerDetail, baseFare, tax);
					isLeadPax = "false";
				} else {
   				Integer fareInt = Integer.parseInt(basefareTravelerInfant) / searchId.getInfantNum();
   				Integer taxInt = Integer.parseInt(taxTravelerInfant) / searchId.getInfantNum();
   				baseFare = "" + fareInt;
   				tax = "" + taxInt;
   				travelerService.updateBasefareTax(travellerDetail, baseFare, tax);
					isLeadPax = "false";
				}
   			
   			String details = "{\r\n"
   					+ "		\"Title\": \"" + travellerDetail.getSalutation() + "\",\r\n"
   					+ "		\"FirstName\": \"" + travellerDetail.getFirstName() + "\",\r\n"
   					+ "		\"LastName\": \"" + travellerDetail.getLastName() + "\",\r\n"
   					+ "		\"PaxType\": " + travellerDetail.getPaxType() + ",\r\n"
   					+ "		\"DateOfBirth\": \"" + getDOB + "T00:00:00\",\r\n"
   					+ "		\"Gender\": " + genNum + ",\r\n"
   					+ "		\"PassportNo\": \"" + travellerDetail.getPassportNo() + "\",\r\n"
   					+ "		\"PassportExpiry\": \"" + travellerDetail.getPassportExpiry() + "T00:00:00\",\r\n"
   					+ "		\"AddressLine1\": \"123, Test\",\r\n"
   					+ "		\"AddressLine2\": \"\",\r\n"
   					+ "		\"Fare\": {\r\n"
   					+ "			\"BaseFare\": " + baseFare + ",\r\n"
   					+ "			\"Tax\": " + tax + ",\r\n"
   					+ "			\"YQTax\": 0.0,\r\n"
   					+ "			\"AdditionalTxnFeePub\": 0.0,\r\n"
   					+ "			\"AdditionalTxnFeeOfrd\": 0.0,\r\n"
   					+ "			\"OtherCharges\": " + otherCharges + ",\r\n"
   					+ "			\"Discount\": " + discount + ",\r\n"
   					+ "			\"PublishedFare\": " + publishedFare + ",\r\n"
   					+ "			\"OfferedFare\": " + offeredFare + ",\r\n"
   					+ "			\"TdsOnCommission\": " + tdsOnCommission + ",\r\n"
   					+ "			\"TdsOnPLB\": " + tdsOnPLB + ",\r\n"
   					+ "			\"TdsOnIncentive\": " + tdsOnIncentive + ",\r\n"
   					+ "			\"ServiceFee\": " + serviceFee + "\r\n"
   					+ "		},\r\n"
   					+ "		\"City\": \"Gurgaon\",\r\n"
   					+ "		\"CountryCode\": \"IN\",\r\n"
   					+ "		\"CountryName\": \"India\",      \r\n"
   					+ "     \"Nationality\": \"IN\",\r\n"
   					+ "		\"ContactNo\": \"" + order.getPhoneNumber() + "\",\r\n"
   					+ "		\"Email\": \"" + order.getContactEmail() + "\",\r\n"
   					+ "		\"IsLeadPax\": " + isLeadPax + ",\r\n"
   					+ "		\"FFAirlineCode\": \"" + productDetailsController.airlineCOde + "\",\r\n"
   					+ "		\"FFNumber\": \"" + productDetailsController.flightNumber + "\",\r\n"
   					+ "		\"GSTCompanyAddress\": \"\",\r\n"
   					+ "		\"GSTCompanyContactNumber\": \"\",\r\n"
   					+ "		\"GSTCompanyName\": \"\",\r\n"
   					+ "		\"GSTNumber\": \"\",\r\n"
   					+ "		\"GSTCompanyEmail\": \"\"\r\n"
   					+ "}";
   			
   			travelerDetailsArray.add(details);

				
   		}
       	
       	String arrayTraveler = travelerDetailsArray.stream().map(val -> String.valueOf(val)).collect(Collectors.joining(",", "[", "]"));

       	/* Ticket details */
       	StringBuilder responseBodyBook = onlineFlightService.apiOnlineBookingNonLCC(traceId, productDetail.getResultIndex(), arrayTraveler);
       	
       	JSONObject jsonObjBooking = new JSONObject(responseBodyBook.toString()); 
       	System.out.println(jsonObjBooking);
       	logService.generateLog(jsonObjBooking.toString());
       	
       	String bookingId = "", pnrBooking = "";
       	Integer bookingIdInt = 0;
       	
       	try {
				JSONObject jsonObjBookingResponse = jsonObjBooking.getJSONObject("Response").getJSONObject("Response").getJSONObject("FlightItinerary");
					pnrBooking = jsonObjBookingResponse.get("PNR").toString();
					bookingId  = jsonObjBookingResponse.get("BookingId").toString();
					bookingIdInt = Integer.parseInt(bookingId);
				
			} catch (Exception e) {
				System.out.println("Not found!!");
			}
       	
       	
       	/* Ticket details */
        StringBuilder responseBodyTicket = onlineFlightService.apiOnlineTicketNonLcc(traceId, pnrBooking, bookingIdInt);
       	
       	JSONObject jsonObjTicket = new JSONObject(responseBodyTicket.toString()); 
       	System.out.println(jsonObjTicket);
       	logService.generateLog(jsonObjTicket.toString());
       	try {
				JSONObject jsonObjTicketResponse = jsonObjTicket.getJSONObject("Response").getJSONObject("Response").getJSONObject("FlightItinerary");
				JSONArray jsonArraySegment = jsonObjTicketResponse.getJSONArray("Segments");
//       		JSONObject jsonObjectSegments = new JSONObject();
				
				String terminalDep = "", terminalArr = "";
				for (int i = 0; i < jsonArraySegment.length(); i++) {
					JSONObject jsonObjectSegments = jsonArraySegment.getJSONObject(i);
					if (i == 0) {
						terminalDep = jsonObjectSegments.getJSONObject("Origin").getJSONObject("Airport").get("Terminal").toString();
					}
					if (i == (jsonArraySegment.length() - 1)) {
						terminalArr = jsonObjectSegments.getJSONObject("Destination").getJSONObject("Airport").get("Terminal").toString();
					}
				}
				
				String onlinePNR = jsonObjTicketResponse.get("PNR").toString();
				String onlineBookingId = jsonObjTicketResponse.get("BookingId").toString();

				productService.updateOtherDetails(productDetail, terminalDep, terminalArr);
				productService.updatePNROnline(productDetail, onlinePNR);
				productService.setTotalSeatOnline(productDetail, productDetail.getUploadSeats());
				updateBookingId(order, onlineBookingId);

				productService.updateOtherDetails(productDetailTwo, terminalDep, terminalArr);
				productService.updatePNROnline(productDetailTwo, onlinePNR);
				productService.setTotalSeatOnline(productDetailTwo, productDetailTwo.getUploadSeats());
				updateBookingId(orderTwo, onlineBookingId);
				
				JSONObject jsonObjTicketResponseError = jsonObjTicket.getJSONObject("Response").getJSONObject("Error");
				hasErrorArr[0] = jsonObjTicketResponseError.get("ErrorCode").toString();
				hasErrorArr[1] = jsonObjTicketResponseError.get("ErrorMessage").toString();

				/* Get booking details */
		       	StringBuilder responseBodyBooking = onlineFlightService.apiOnlineGetBookingDetails(traceId, onlinePNR, onlineBookingId);
		       	
		       	JSONObject jsonObjGetBooking = new JSONObject(responseBodyBooking.toString());
		       	System.out.println(jsonObjGetBooking);
		       	logService.generateLog(jsonObjGetBooking.toString()); 
						
			} catch (JSONException json) {
				 //Error response
				JSONObject jsonObjTicketResponseError = jsonObjTicket.getJSONObject("Response").getJSONObject("Error");
				hasErrorArr[0] = jsonObjTicketResponseError.get("ErrorCode").toString();
				hasErrorArr[1] = jsonObjTicketResponseError.get("ErrorMessage").toString();
			} 

		}
		return hasErrorArr;
	}



	public String searchReturnInternationalFlightAPIOnlyTraceId(String cityOne, String cityTwo, Integer adultNum, Integer childNum, Integer infantNum, String sortName, Model model, Date date, 
			Date returnDate, String traceIdReturn) throws MalformedURLException, IOException {
		// Create URL object with the API end-point
        StringBuilder responseBodySearch = onlineFlightService.apiOnlineSearchModReturn(cityOne, cityTwo, adultNum, childNum, infantNum, date, returnDate);
		
        JSONObject jsonObjSearch = new JSONObject(responseBodySearch.toString());
        System.out.println(jsonObjSearch);
//        logService.generateLog(jsonObjSearch.toString());
        try {
    		traceIdReturn = jsonObjSearch.getJSONObject("Response").get("TraceId").toString();
            
		} catch (JSONException e) {
			e.printStackTrace();
		}

        return traceIdReturn;
	}

	public void fareqouteAPI(ProductDetail flight, String traceId) throws IOException {

		if (!flight.getTraceId().equals("offline")) {
			/* Fare-quote details */
            StringBuilder responseBodyFarequote = onlineFlightService.apiOnlineFareruleQuoteSSR(traceId, flight.getResultIndex(), "/AirService.svc/rest/FareQuote");

        	JSONObject jsonObjFarerules = new JSONObject(responseBodyFarequote.toString());
        	System.out.println(jsonObjFarerules);
            logService.generateLog(jsonObjFarerules.toString());
		}
	}

	
	// Email Service
	
	public void sendSuccessEmail (Customer customer, String pnr, String trn) throws UnsupportedEncodingException, MessagingException {
		
		EmailSettingBag emailSettings = settingService.getEmailSettings();
		JavaMailSenderImpl mailSender = Utility.prepareMailSender(emailSettings);
		
		String toAddress = customer.getEmail();
		String subject = emailSettings.getFlightSuccessSubject();
		String content = emailSettings.getFlightSuccessContent();
		
		MimeMessage message= mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		
		helper.setFrom(emailSettings.getFromAddress(), emailSettings.getSenderName());
		helper.setTo(toAddress);
		helper.setSubject(subject);
		
		subject = subject.replace("[[PNR]]", pnr);
		
		content = content.replace("[[name]]", customer.getFullName());
		
		content = content.replace("[[TRN]]", trn);
		
		helper.setText(content, true);
		
		mailSender.send(message);
		
		System.out.println("To Address: " + toAddress);
		    
	}
}
