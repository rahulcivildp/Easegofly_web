package com.easygofly.site.order;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.easygofly.entity.Brand;
import com.easygofly.entity.CartItem;
import com.easygofly.entity.City;
import com.easygofly.entity.Coupon;
import com.easygofly.entity.Customer;
import com.easygofly.entity.Order;
import com.easygofly.entity.OrderStatus;
import com.easygofly.entity.PaymentMethod;
import com.easygofly.entity.Product;
import com.easygofly.entity.ProductDetail;
import com.easygofly.entity.SearchHistory;
import com.easygofly.entity.TravellerDetail;
import com.easygofly.entity.User;
import com.easygofly.entity.Wallet;
import com.easygofly.site.checkout.CheckoutInfo;
import com.easygofly.site.checkout.CheckoutService;
import com.easygofly.site.customer.CustomerService;
import com.easygofly.site.flight.CityRepository;
import com.easygofly.site.flight.CityService;
import com.easygofly.site.flight.FlightRepository;
import com.easygofly.site.flight.ProductDetailService;
import com.easygofly.site.flight.ProductDetailsController;
import com.easygofly.site.flight.TravellerRepository;
import com.easygofly.site.flightAPI.OnlineFlightService;
import com.easygofly.site.order.exporter.OrderPDFExporter;
import com.easygofly.site.search.SearchHistoryRepository;
import com.easygofly.site.search.SearchHistoryService;
import com.easygofly.site.security.EasyGoFlyCustomerDetails;
import com.easygofly.site.security.oauth.CustomerOAuth2User;
import com.easygofly.site.setting.GeneralSettingBag;
import com.easygofly.site.setting.SettingService;
import com.easygofly.site.shoppingCart.CartItemRepository;
import com.easygofly.site.shoppingCart.CartItemService;
import com.easygofly.site.wallet.WalletService;
import com.easygofly.site.zaakpay.ChecksumGenerator;
import com.easygofly.site.zaakpay.Config;
import com.easygofly.site.zaakpay.Transaction;
import com.easygofly.site.zaakpay.ZaakpayApiRequestParameters;

@Controller
public class OrderController {
	
	@Autowired private CustomerService customerService;
	@Autowired private FlightRepository flightRepo;
	@Autowired private SearchHistoryRepository searchRepo;
	@Autowired private SearchHistoryService searchService;
	@Autowired private CartItemRepository cartRepo;
	@Autowired private CartItemService cartService;
	@Autowired private OrderService orderService;
	@Autowired private OrderRepository orderRepo;
	@Autowired private CheckoutService checkoutService;
	@Autowired private ProductDetailService productService;
	@Autowired private SettingService settingService;
	@Autowired private CityRepository cityRepo;
	@Autowired private CityService cityService;
	@Autowired private TravellerRepository travellerRepo;
	@Autowired private CouponService couponService ;
	@Autowired private WalletService walletService;
	@Autowired private ProductDetailsController productDetailsController;
	@Autowired private OnlineFlightService onlineFlightService ;
	
	private String[] parameter = new String[20];
	private String checksum;
	private Boolean verifiedChecksum;
	private String[] responseParameters;
	private Integer savedOrderId;
	private Integer updatedOrderId;
	
	@PostMapping("/flight_order_save")
	public String createNewOrder(@RequestParam(name = "search_id") Integer searchId, 
			@RequestParam(name = "flight_id") Integer flightId,
			@RequestParam(name = "item_id") Integer item_id,
			@RequestParam(name = "couponCode") String couponCode,
			@RequestParam(name = "couponCode1") String couponCode1,
			@RequestParam(name = "totalPayment") String totalPayment,
			@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, @AuthenticationPrincipal CustomerOAuth2User googleLogin,
			HttpServletRequest request, Order order3) {
		try {
			ProductDetail flight = flightRepo.findById(flightId).get();
			SearchHistory search = searchRepo.findById(searchId).get();
			CartItem item = cartRepo.findById(item_id).get();
			
			String paymentType = "PAYMENT_GATEWAY";
			PaymentMethod paymentMethod = PaymentMethod.valueOf(paymentType);
			
			
			
			Date date = flight.getDate();  
			DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");  
		    String dateTime = dateFormat.format(date);
			
			String orderName = flight.getCityOne() + "-" + flight.getCityTwo() + ":(" + flight.getFlightNum() + ")" + dateTime + ":" + flight.getDepTime() + "-" + flight.getArrTime() + ":(" + search.getPassengerNum() + ")";

			Order order = orderRepo.findByCartItemOrder(item_id);
			
			List<TravellerDetail> travellerDetails = travellerRepo.findTravellerByCustomerAndProductDetail(flight, item);
			
			cartService.updateCartItemOrdered(item);

			System.out.println("couponCode : "+couponCode);
			System.out.println("couponCode1 : "+couponCode1);
			Coupon coupon = couponService.findCouponByCode(couponCode);
			Coupon coupon1 = couponService.findCouponByCode(couponCode1);
			CheckoutInfo checkoutInfo = checkoutService.prepareCheckout(item);
			String email; 
			Customer customer; 
			if (loggedCustomer != null) {
				email = loggedCustomer.getUsername();
				customer = customerService.getByEmail(email);
				if (coupon1 != null) {
					CheckoutInfo checkoutInfo1 = checkoutService.prepareCheckoutWithCoupon(item, coupon1);
					if (order != null) {
						orderService.updateOrderPrice(order, checkoutInfo1);
						orderService.addCouponCode(order, couponCode1);
					}
					
					Order order2 = saveOrderCreate(flight, search, item, paymentMethod, checkoutInfo1, orderName, order, customer, travellerDetails);
					orderService.addCouponCode(order2, couponCode1);
					for (TravellerDetail travellerDetail : travellerDetails) {
						orderService.updateTravelersOrderId(travellerDetail.getId(), order2);
					}
				} else if (coupon != null) {
					CheckoutInfo checkoutInfo1 = checkoutService.prepareCheckoutWithCoupon(item, coupon);
					if (order != null) {
						orderService.updateOrderPrice(order, checkoutInfo1);
						orderService.addCouponCode(order, couponCode);
					}
					
					Order order2 = saveOrderCreate(flight, search, item, paymentMethod, checkoutInfo1, orderName, order, customer, travellerDetails);
					orderService.addCouponCode(order2, couponCode);
					for (TravellerDetail travellerDetail : travellerDetails) {
						orderService.updateTravelersOrderId(travellerDetail.getId(), order2);
					}
				} else if (order != null) {
					if (flight.getTraceId().equals(null)) {
						orderService.updateOrderPrice(order, checkoutInfo);
						orderService.deleteCouponCode(order);
					} else {
						orderService.updateOrderPriceOnline(order, totalPayment);
						orderService.deleteCouponCode(order);
					}
					
				} else {
					if (flight.getTraceId().equals(null)) {
						Order order2 = saveOrderCreate(flight, search, item, paymentMethod, checkoutInfo, orderName, order, customer, travellerDetails);
						for (TravellerDetail travellerDetail : travellerDetails) {
							orderService.updateTravelersOrderId(travellerDetail.getId(), order2);
						}
						orderService.deleteCouponCode(order2);
					} else {
						Order order2 = orderService.createOrderOnline(customer, item, flight, paymentMethod, totalPayment, search, orderName, travellerDetails);
						for (TravellerDetail travellerDetail : travellerDetails) {
							orderService.updateTravelersOrderId(travellerDetail.getId(), order2);
						}
						orderService.deleteCouponCode(order2);
					}	
				}
				
			} else if (googleLogin != null) {
				email = googleLogin.getEmail();
				customer = customerService.getByEmail(email);
				if (coupon1 != null) {
					CheckoutInfo checkoutInfo1 = checkoutService.prepareCheckoutWithCoupon(item, coupon1);
					if (order != null) {
						orderService.updateOrderPrice(order, checkoutInfo1);
						orderService.addCouponCode(order, couponCode1);
					}
					
					Order order2 = saveOrderCreate(flight, search, item, paymentMethod, checkoutInfo1, orderName, order, customer, travellerDetails);
					orderService.addCouponCode(order2, couponCode1);
					for (TravellerDetail travellerDetail : travellerDetails) {
						orderService.updateTravelersOrderId(travellerDetail.getId(), order2);
					}
				} else if (coupon != null) {
					CheckoutInfo checkoutInfo1 = checkoutService.prepareCheckoutWithCoupon(item, coupon);
					if (order != null) {
						orderService.updateOrderPrice(order, checkoutInfo1);
						orderService.addCouponCode(order, couponCode);
					}
					
					Order order2 = saveOrderCreate(flight, search, item, paymentMethod, checkoutInfo1, orderName, order, customer, travellerDetails);
					orderService.addCouponCode(order2, couponCode);
					for (TravellerDetail travellerDetail : travellerDetails) {
						orderService.updateTravelersOrderId(travellerDetail.getId(), order2);
					}
				} else if (order != null) {
					orderService.updateOrderPrice(order, checkoutInfo);
					orderService.deleteCouponCode(order);
				} else {
					if (flight.getTraceId().equals(null)) {
						Order order2 = saveOrderCreate(flight, search, item, paymentMethod, checkoutInfo, orderName, order, customer, travellerDetails);
						for (TravellerDetail travellerDetail : travellerDetails) {
							orderService.updateTravelersOrderId(travellerDetail.getId(), order2);
						}
						orderService.deleteCouponCode(order2);
					} else {
						Order order2 = orderService.createOrderOnline(customer, item, flight, paymentMethod, totalPayment, search, orderName, travellerDetails);
						for (TravellerDetail travellerDetail : travellerDetails) {
							orderService.updateTravelersOrderId(travellerDetail.getId(), order2);
						}
						orderService.deleteCouponCode(order2);
					}	
				}
			}
			
			return "redirect:/flight_order_" + search.getId() + "&" + flightId + "&" + item_id;
		} catch (Exception e) {
			return "redirect:/flight_order_" + searchId + "&" + flightId + "&" + item_id;
		}
		
	}

	private Order saveOrderCreate(ProductDetail flight, SearchHistory search, CartItem item, PaymentMethod paymentMethod,
			CheckoutInfo checkoutInfo, String orderName, Order order, Customer customer, List<TravellerDetail> travellerDetails) {
		if (order == null) {
			return orderService.createOrder(customer, item, flight, paymentMethod, checkoutInfo, search, orderName, travellerDetails);
		}else if (item.getId() != order.getCartId()) {
			return orderService.createOrder(customer, item, flight, paymentMethod, checkoutInfo, search, orderName, travellerDetails);
		} else {
			return null;
		}
	}
	
	@GetMapping("/flight_order_{search_id}&{flight_id}&{item_id}")
	public String orderPage(@PathVariable(name = "search_id") Integer search_id, 
			@PathVariable(name = "flight_id") Integer flight_id,
			@PathVariable(name = "item_id") Integer item_id, 
			@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, @AuthenticationPrincipal CustomerOAuth2User googleLogin,
			Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) throws IOException {
		
		String email; 
		Customer customer; 
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByEmail(email);
			Wallet wallet = customer.getWallet();
			Double doubleAmount = (double) (wallet.getBalance() / 100);
			model.addAttribute("balance", doubleAmount);
			model.addAttribute("customer", customer);
			
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByEmail(email);
			Wallet wallet = customer.getWallet();
			Double doubleAmount = (double) (wallet.getBalance() / 100);
			model.addAttribute("balance", doubleAmount);
			model.addAttribute("customer", customer);
		}
		
		ProductDetail flight = flightRepo.findById(flight_id).get();
		CartItem item = cartRepo.findById(item_id).get();
		
		System.out.println("item_id: " + item_id);
		
		List<TravellerDetail> travelers = productService.findTraveller(flight, item);

		CheckoutInfo checkoutInfo = checkoutService.prepareCheckout(item);
		Order order = orderRepo.findByCartItemOrder(item_id);
		
		if (!search_id.equals(null)) {
			SearchHistory search = searchRepo.findById(search_id).get();
			model.addAttribute("search", search);
		} else {
			SearchHistory search = searchRepo.findByCart_id(item_id);
			model.addAttribute("search", search);
		}
		
		List<String> travelerDetailsArray = new ArrayList<String>();
		
		if (!flight.getTraceId().equals(null)) {
			/* SSR details */
        	URL urlSSR = new URL("http://api.tektravels.com/BookingEngineService_Air/AirService.svc/rest/SSR");
            // Open a connection
            HttpURLConnection connectionSSR = (HttpURLConnection) urlSSR.openConnection();
            
            StringBuilder responseBodySSR = new StringBuilder();
            
        	int responseCodeSSR = onlineFlightService.apiOnlineFarerule_quote(connectionSSR, responseBodySSR, flight.getTraceId(), flight.getResultIndex());
        	if (responseCodeSSR != HttpURLConnection.HTTP_OK) {
    			if (responseCodeSSR == HttpURLConnection.HTTP_MOVED_TEMP
    				|| responseCodeSSR == HttpURLConnection.HTTP_MOVED_PERM
    					|| responseCodeSSR == HttpURLConnection.HTTP_SEE_OTHER)
    				return "redirect:/";
    		}
			
        	System.out.println(responseCodeSSR);
    		
        	JSONObject jsonObjSSR = new JSONObject(responseBodySSR.toString()); 
        	String traceId = jsonObjSSR.getJSONObject("Response").get("TraceId").toString(); 
        	
        	
			for (TravellerDetail travellerDetail : travelers) {
    			Date getDOB = travellerDetail.getDob();
    			Integer genNum = 0;
    			if (travellerDetail.getSalutation().equals("Mr.")) {
    				genNum = 1;
    			} else {
    				genNum = 2;
    			}
    			String baseFare = "";
    			String tax = "";
    			
    			if (travellerDetail.getPaxType().equals("1")) {
    				baseFare = productDetailsController.basefareTravelerAdult;
    				tax = productDetailsController.taxTravelerAdult;
				} else if (travellerDetail.getPaxType().equals("2")) {
    				baseFare = productDetailsController.basefareTravelerChild;
    				tax = productDetailsController.taxTravelerChild;
				} else {
    				baseFare = productDetailsController.basefareTravelerInfant;
    				tax = productDetailsController.taxTravelerInfant;
				}
    			
    			//baggage information
    			if (travellerDetail.getBaggage() != null) {
					String baggage = travellerDetail.getBaggage();
	    			String[] bagArray = baggage.split("|");
	    			String bagCode = bagArray[0];
	    			String bagWeight = bagArray[1];
	    			String bagPrice = bagArray[2];
				}
    			
    			
    			//meal information
    			if (travellerDetail.getMeal() != null) {
					String meal = travellerDetail.getMeal();
	    			String[] mealArray = meal.split("|");
	    			String mealCode = mealArray[0];
	    			String mealName = mealArray[3];
	    			String mealQuantity = mealArray[1];
	    			String mealPrice = mealArray[2];
				}
    			
    			
    			//seat information
    			if (travellerDetail.getSeat() != null) {
					String seat = travellerDetail.getSeat();
	    			String[] seatArray = seat.split("|");
	    			String seatAvailabilityType = seatArray[6];
	    			String seatCode = seatArray[8];
	    			String seatRowNo = seatArray[2];
	    			String seatNo = seatArray[3];
	    			String seatType = seatArray[5];
	    			String seatDeck = seatArray[1];
	    			String seatCompartment = seatArray[0];
	    			String seatPrice = seatArray[4];
	    			String seatCraftType = seatArray[7];
				}
    			
    					
    			
    			String details = "{\r\n"
    					+ "		\"Title\": \"" + travellerDetail.getSalutation() + "\",\r\n"
    					+ "		\"FirstName\": \"" + travellerDetail.getFirstName() + "\",\r\n"
    					+ "		\"LastName\": \"" + travellerDetail.getLastName() + "\",\r\n"
    					+ "		\"PaxType\": " + travellerDetail.getPaxType() + ",\r\n"
    					+ "		\"DateOfBirth\": \"" + getDOB + "T00:00:00\",\r\n"
    					+ "		\"Gender\": " + genNum + ",\r\n"
    					+ "		\"PassportNo\": \"KJHHJKHKJH\",\r\n"
    					+ "		\"PassportExpiry\": \"2030-12-06T00:00:00\",\r\n"
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
    					+ "		\"ContactNo\": \"" + item.getPhoneNum() + "\",\r\n"
    					+ "		\"Email\": \"" + item.getEmail() + "\",\r\n"
    					+ "		\"IsLeadPax\": true,\r\n"
    					+ "		\"FFAirlineCode\": \"" + productDetailsController.airlineCOde + "\",\r\n"
    					+ "		\"FFNumber\": \"" + productDetailsController.flightNumber + "\",\r\n"
    					+ "		\"Baggage\":[\r\n"
    					+ "            {\r\n"
    					+ "                \"AirlineCode\": \"" + productDetailsController.airlineCOde + "\",\r\n"
    					+ "                \"FlightNumber\": \"" + productDetailsController.flightNumber + "\",\r\n"
    					+ "                \"WayType\": 2,\r\n"
    					+ "                \"Code\": \"NoBaggage\",\r\n"
    					+ "                \"Description\": 2,\r\n"
    					+ "                \"Weight\": 0,\r\n"
    					+ "                \"Currency\": \"INR\",\r\n"
    					+ "                 \"Price\": 0,\r\n"
    					+ "                 \"Origin\": \"" + productDetailsController.airportCodeOrigin + "\",\r\n"
    					+ "                \"Destination\": \"" + productDetailsController.airportCodeDestination + "\"\r\n"
    					+ "				}\r\n"
    					+ "			],\r\n"
    					+ "     \"MealDynamic\": [\r\n"
    					+ "        {\r\n"
    					+ "          \"AirlineCode\": \"" + productDetailsController.airlineCOde + "\",\r\n"
    					+ "          \"FlightNumber\": \"" + productDetailsController.flightNumber + "\",\r\n"
    					+ "          \"WayType\": 2,\r\n"
    					+ "          \"Code\": \"NoMeal\",\r\n"
    					+ "          \"Description\": 2,\r\n"
    					+ "          \"AirlineDescription\": \"\",\r\n"
    					+ "          \"Quantity\": 0,\r\n"
    					+ "          \"Currency\": \"INR\",\r\n"
    					+ "          \"Price\": 0,\r\n"
    					+ "          \"Origin\": \"" + productDetailsController.airportCodeOrigin + "\",\r\n"
    					+ "          \"Destination\": \"" + productDetailsController.airportCodeDestination + "\"\r\n"
    					+ "        }],\r\n"
    					+ "		\"SeatDynamic\": [\r\n"
    					+ "        {\r\n"
    					+ "	    \"AirlineCode\": \"" + productDetailsController.airlineCOde + "\",\r\n"
    					+ "             \"FlightNumber\": \"" + productDetailsController.flightNumber + "\",\r\n"
    					+ "              \"CraftType\": \"" + productDetailsController.craftType + "\",\r\n"
    					+ "               \"Origin\": \"" + productDetailsController.airportCodeOrigin + "\",\r\n"
    					+ "                \"Destination\": \"" + productDetailsController.airportCodeDestination + "\",\r\n"
    					+ "                \"AvailablityType\": 1,\r\n"
    					+ "                \"Description\": 2,\r\n"
    					+ "                \"Code\": \"2A\",\r\n"
    					+ "                \"RowNo\": \"2\",\r\n"
    					+ "                \"SeatNo\": \"A\",\r\n"
    					+ "                \"SeatType\": 1,\r\n"
    					+ "                \"SeatWayType\": 2,\r\n"
    					+ "                \"Compartment\": 1,\r\n"
    					+ "                \"Deck\": 1,\r\n"
    					+ "                \"Currency\": \"INR\",\r\n"
    					+ "                \"Price\": 300                                                                                                                                                                                                      \r\n"
    					+ "			\r\n"
    					+ "		}],\r\n"
    					+ "		\"GSTCompanyAddress\": \"\",\r\n"
    					+ "		\"GSTCompanyContactNumber\": \"\",\r\n"
    					+ "		\"GSTCompanyName\": \"\",\r\n"
    					+ "		\"GSTNumber\": \"\",\r\n"
    					+ "		\"GSTCompanyEmail\": \"\"\r\n"
    					+ "}";
    			
//    			System.out.println(details);
    			
    			travelerDetailsArray.add(details);
    			
    		}
        	
        	String arrayTraveler = travelerDetailsArray.stream().map(n -> String.valueOf(n)).collect(Collectors.joining(",", "[", "]"));

        	/* Ticket details */
        	URL urlTicket = new URL("http://api.tektravels.com/BookingEngineService_Air/AirService.svc/rest/Ticket");
            // Open a connection
            HttpURLConnection connectionTicket = (HttpURLConnection) urlTicket.openConnection();
            
            StringBuilder responseBodyTicket = new StringBuilder();
            
        	int responseCodeTicket = onlineFlightService.apiOnlineTicket(connectionTicket, responseBodyTicket, traceId, flight.getResultIndex(), arrayTraveler);
        	
        	System.out.println(responseCodeTicket);
        	
        	JSONObject jsonObjTicket = new JSONObject(responseBodyTicket.toString()); 

        	System.out.println(jsonObjTicket);
		}
		
		
		/* ------ ZAAKPAY -------- */ /**/
		Date date = Calendar.getInstance().getTime();  
	    DateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");  
	    DateFormat dateFormat2 = new SimpleDateFormat("hhmmss");
	    String strDate1 = dateFormat1.format(date);
	    String strDate2 = dateFormat2.format(date);
		
		String orderString = "EGF" + strDate1 + "T" + strDate2 + "R"+ order.getId();
		Integer intAmount = (int) (order.getPrice() * 100);
		String amount = "" + intAmount;
		//String amount = "100";

		//Cookie cookie = request.getCookies().get("JSESSIONID");
		//String value = cookie.getValue();
		
		for (Cookie cookie : request.getCookies()) {
			if(cookie.getName().equals("JSESSIONID")) {
				String value = cookie.getValue();
				model.addAttribute("JSESSIONID", value);
			}
		}
		
		
		Transaction transaction = new Transaction();
		
		try {
			ZaakpayApiRequestParameters processPayment = transaction.processPayment(orderString, amount);
			
			model.addAttribute("entrySet", processPayment.getRequestParameters().entrySet());
			model.addAttribute("requestUrl", processPayment.getRequestUrl());
			model.addAttribute("checksum", processPayment.getChecksum());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		/* ------ CCAVENUE -------- */ /*
		String accessCode= "AVJC30KC30BM66CJMB";		//Put in the Access Code in quotes provided by CCAVENUES.
		String workingKey = "E99274310106A9AF3DB4AF4D3073863E";    //Put in the 32 Bit Working Key provided by CCAVENUES.  
		Enumeration enumeration=request.getParameterNames();
		String ccaRequest="", pname="", pvalue="";
		while(enumeration.hasMoreElements()) {
		     pname = ""+enumeration.nextElement();
		     pvalue = request.getParameter(pname);
		     ccaRequest = ccaRequest + pname + "=" + URLEncoder.encode(pvalue,"UTF-8") + "&";
		}
		AesCryptUtil aesUtil=new AesCryptUtil(workingKey);
		String encRequest = aesUtil.encrypt(ccaRequest);
		model.addAttribute("encRequest", encRequest);
		model.addAttribute("accessCode", accessCode);
		*/
		
		Coupon coupon = couponService.findCouponByCode(order.getCouponCode());
		
		savedOrderId = order.getId();
		
		model.addAttribute("order_id", order.getId());
		model.addAttribute("order", order);
		model.addAttribute("checkoutInfo", checkoutInfo);
		model.addAttribute("travelers", travelers);
		model.addAttribute("item", item);
		model.addAttribute("flight", flight);
		model.addAttribute("search_id", search_id);
		model.addAttribute("item_id", item_id);
		model.addAttribute("coupon", coupon);
		
		return "order/flight_order";
	}
	
	/**/
	@CrossOrigin(origins = {"https://easegofly.com/"})
	@RequestMapping(value = "/zaakpay/response",
			method = {RequestMethod.POST})
	public String zaakpayResponse (HttpServletRequest request, HttpServletResponse response,
			@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, @AuthenticationPrincipal CustomerOAuth2User googleLogin) throws Exception {
		//com.easygofly.entity.Transaction transactions = new com.easygofly.entity.Transaction();
		
		Transaction transaction = new Transaction();
	    ChecksumGenerator checksumGenerator = new ChecksumGenerator();
	    String checksumString = "" ;
	    Integer n= 0;
	    for (String param: transaction.getResponseParameters()) {
	        checksumString=checksumString+param+"="+request.getParameter(param);
	        checksumString=checksumString+"&";
	        //This will create the checksum string against every parameter.
	        parameter[n] = request.getParameter(param);
	        n+=1;
	    }
	    for (String string : parameter) {
	        System.out.println("Array Parameters: " + string);
		}
	    Boolean verifyChecksum = checksumGenerator.verifyChecksum(Config.ZAAKPAY_SECRET_KEY,checksumString,request.getParameter("checksum")) ;
	    verifiedChecksum = verifyChecksum;
	    checksum = request.getParameter("checksum");
	    responseParameters = transaction.getResponseParameters();
		
	    String orderParam = parameter[8];
		String[] parts = orderParam.split("R");
		String part2 = parts[1]; // 034556
		Integer convert = Integer.parseInt(part2);
		Order order = orderRepo.findById(convert).get();
		ProductDetail productDetail = order.getProductDetail();
		if (parameter[12].equals("Customer cancelled transaction. Transaction has failed")) {
			orderService.updateOrder(order, OrderStatus.CANCELLED);
		} else if (parameter[12].equals("Unfortunately the transaction has failed.Please try again. Transaction has failed")) {
			orderService.updateOrder(order, OrderStatus.FAILED);
		}else {
			if (productDetail.getPnr().equals(null) || productDetail.getPnr().equals("")) {
				orderService.updateOrder(order, OrderStatus.PENDING);
			} else {
				orderService.updateOrder(order, OrderStatus.SUCCESSFULL);
			}
			
			Integer totalSeatRemaining = Integer.parseInt(productDetail.getTotalSeats()) - order.getPassengerNum();
			orderService.updateTotalPassenger(order, totalSeatRemaining);
		}
		
		try {
			CartItem cartItem = cartRepo.findById(order.getCartId()).get();
			if (!cartItem.equals(null)) {
				if (order.getOrderStatus().equals(OrderStatus.CANCELLED) || order.getOrderStatus().equals(OrderStatus.SUCCESSFULL) || order.getOrderStatus().equals(OrderStatus.FAILED) || order.getOrderStatus().equals(OrderStatus.PENDING )) {
					List<SearchHistory> search = cartItem.getSearchHistory();
					for (SearchHistory searchHistory : search) {
						List<TravellerDetail> travellerDetail2 = travellerRepo.findTravellerByCustomerAndProductDetail(productDetail, cartItem);
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
			return "redirect:/zaakpay/response";
		}
		
		return "redirect:/zaakpay/response";
	}
	
	@CrossOrigin(origins = {"https://easegofly.com/"})
	@RequestMapping(value = "/zaakpay/response",
			method = {RequestMethod.GET})
	public String zaakpayResponseSe (Model model, 
			@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, @AuthenticationPrincipal CustomerOAuth2User googleLogin) throws Exception {
		
		String email; 
		Customer customer; 
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByEmail(email);
			model.addAttribute("customer", customer);
			
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByEmail(email);
			model.addAttribute("customer", customer);
		}

		String orderParam = parameter[8];
		model.addAttribute("orderId", orderParam);
		String[] parts = orderParam.split("R");
		String part2 = parts[1]; // 034556
		Integer convert = Integer.parseInt(part2);
		Order order = orderRepo.findById(convert).get();
		model.addAttribute("orderId", order.getId());
		ProductDetail productDetail = order.getProductDetail();
		String pnr = productDetail.getPnr();
		model.addAttribute("pnrBarcode", pnr);
		model.addAttribute("orderStatusO", order.getOrderStatus());
		
		String cityOne = productDetail.getCityOne();
		String cityTwo = productDetail.getCityTwo();
		City city1 = cityRepo.getCityByCode(cityOne);
		City city2 = cityRepo.getCityByCode(cityTwo);
		model.addAttribute("cityOne", city1.getCityName());
		model.addAttribute("cityTwo", city2.getCityName());
		model.addAttribute("city1", cityOne);
		model.addAttribute("city2", cityTwo);
		
		Date date1 = productDetail.getDate();  
		DateFormat dateFormat1 = new SimpleDateFormat("E, dd-MM-yyyy");  
	    String flightDateTime = dateFormat1.format(date1);
		model.addAttribute("flightDateTime", flightDateTime);
		
		Date date2 = order.getCreatedTime();  
		DateFormat dateFormat2 = new SimpleDateFormat("dd-MM-yyyy");  
	    String orderDateTime = dateFormat2.format(date2);
		model.addAttribute("orderDateTime", orderDateTime);
		
		model.addAttribute("passengerPhone", order.getPassengerNum());
		model.addAttribute("passengerEmail", order.getContactEmail());
		
		Product product = productDetail.getProduct();
		Brand brand = product.getBrands();
		model.addAttribute("brandPath", ".." + brand.getPhotosImagePath());
		model.addAttribute("brandName", brand.getName());
		model.addAttribute("productDetail", productDetail);
		model.addAttribute("originTerminal", product.getOriginTerminal());
		model.addAttribute("destinationTerminal", product.getDestinationTerminal());
		model.addAttribute("baggage", product.getBaggage());
		model.addAttribute("cabinBaggage", product.getCabinBaggage());
		
		Integer dateInt = productDetail.getDuration()/60;
		if (dateInt >= 10) {
			model.addAttribute("dateInt", dateInt);
		} else {
			model.addAttribute("dateInt", "0" + dateInt);
		}
		Integer timeInt = productDetail.getDuration()%60;
		if (timeInt >= 10) {
			model.addAttribute("timeInt", timeInt);
		} else {
			model.addAttribute("timeInt", "0" + timeInt);
		}
		
		Path flightUpPath = Paths.get("../pdf-images/flight-up.png");
		Path flightDownPath = Paths.get("../pdf-images/flight-down.png");
		Path demoTicketPath = Paths.get("../pdf-images/demo-ticket.png");
		Path thumbLogoPath = Paths.get("../pdf-images/thumb-logo.png");
		model.addAttribute("flightUpPath", flightUpPath);
		model.addAttribute("flightDownPath", flightDownPath);
		model.addAttribute("demoTicketPath", demoTicketPath);
		model.addAttribute("thumbLogoPath", thumbLogoPath);
		
		User user = product.getUser();
		model.addAttribute("user", user);
		
		if (parameter[9].equals("Not Found") && parameter[10].equals("unknown") ) {
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].equals("Unfortunately the transaction has failed.Please try again. Transaction has failed")) {
			model.addAttribute("paymentCancelled", parameter[12]);
		}
		
		List<TravellerDetail> travellerDetails = travellerRepo.findTravellerByProductDetailAndOrder(productDetail, order);
		model.addAttribute("travellerDetails", travellerDetails);
        
		Double amount = Double.parseDouble(parameter[0])/100;
		
		model.addAttribute("paymentSuccess", parameter[12]);
		model.addAttribute("amount", amount);
		model.addAttribute("checksum", checksum);
		model.addAttribute("verifyChecksum", verifiedChecksum);
		model.addAttribute("responseParameters", responseParameters);
		
		return "zaakpay/response";
		
		
	}

	@GetMapping("/order/export_pdf/{id}")
	public void exportToPDF(HttpServletResponse response, @PathVariable("id") Integer id) throws Exception {
		Order order = orderRepo.findById(id).get();
		ProductDetail productDetail = order.getProductDetail();
		OrderPDFExporter exporter = new OrderPDFExporter();
		City city1 = cityService.findCityOneByCode(order);
		City city2 = cityService.findCityTwoByCode(order);
		GeneralSettingBag settingBag = settingService.getGeneralSettingBag();
		String logoLink = settingBag.getSiteLogo();
		String faviconLink = settingBag.getFavicon();
		List<TravellerDetail> travellers = travellerRepo.findTravellerByProductDetailAndOrder(productDetail, order);
		
		exporter.export(order, response, city1, city2, logoLink, travellers, faviconLink); 

	}
	
	@PostMapping("/flight_wallet_check")
	public String walletPayment(@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, 
			@AuthenticationPrincipal CustomerOAuth2User googleLogin) {
		
		String email; 
		Customer customer; 
		Order order = orderRepo.findById(savedOrderId).get();
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByEmail(email);
			Wallet wallet = walletPayOrder(customer, order);
			if (wallet != null) {
				Order updatedOrder = orderUpdateWallet(order);
				updatedOrderId = updatedOrder.getId();
			}
			System.out.println("TestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTest11111111111111" + savedOrderId);
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByEmail(email);
			Wallet wallet = walletPayOrder(customer, order);
			if (wallet != null) {
				Order updatedOrder = orderUpdateWallet(order);
				updatedOrderId = updatedOrder.getId();
			}
		}
		
		return "redirect:/flight_wallet_response";
	}

	private Order orderUpdateWallet(Order order) {
		System.out.println("TestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTest44444444444444444");
		ProductDetail productDetail = order.getProductDetail();
		
		if (productDetail.getPnr().equals(null) || productDetail.getPnr().equals("")) {
			orderService.updateOrder(order, OrderStatus.PENDING);
		} else {
			orderService.updateOrder(order, OrderStatus.SUCCESSFULL);
		}
		
		Integer totalSeatRemaining = Integer.parseInt(productDetail.getTotalSeats()) - order.getPassengerNum();
		orderService.updateTotalPassenger(order, totalSeatRemaining);
		

		System.out.println("TestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTest222222222222");
		try {
			CartItem cartItem = cartRepo.findById(order.getCartId()).get();
			if (!cartItem.equals(null)) {
				if (order.getOrderStatus().equals(OrderStatus.CANCELLED) || order.getOrderStatus().equals(OrderStatus.SUCCESSFULL) || order.getOrderStatus().equals(OrderStatus.FAILED) || order.getOrderStatus().equals(OrderStatus.PENDING )) {
					List<SearchHistory> search = cartItem.getSearchHistory();
					for (SearchHistory searchHistory : search) {
						List<TravellerDetail> travellerDetail2 = travellerRepo.findTravellerByCustomerAndProductDetail(productDetail, cartItem);
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

	private Wallet walletPayOrder(Customer customer, Order order) {
		Date date = Calendar.getInstance().getTime();  
		DateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");  
		DateFormat dateFormat2 = new SimpleDateFormat("hhmmss");
		String strDate1 = dateFormat1.format(date);
		String strDate2 = dateFormat2.format(date);
		
		String orderString = "EGF" + strDate1 + "T" + strDate2 + "R"+ order.getId();
		return walletService.updateWalletBalanceByOrder(customer, order, orderString);
	}
	
	@GetMapping("/flight_wallet_response")
	public String showWalletPayment(@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, 
			@AuthenticationPrincipal CustomerOAuth2User googleLogin, Model model) {
		System.out.println("TestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTest0000000000000" + updatedOrderId);
		String email; 
		Customer customer; 
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByEmail(email);
			model.addAttribute("customer", customer);
			
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByEmail(email);
			model.addAttribute("customer", customer);
		}

		System.out.println("TestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTest3333333333333");
		
		Order order = orderRepo.findById(updatedOrderId).get();
		model.addAttribute("orderId", order.getId());
		ProductDetail productDetail = order.getProductDetail();
		String pnr = productDetail.getPnr();
		model.addAttribute("pnrBarcode", pnr);
		model.addAttribute("orderStatusO", order.getOrderStatus());
		
		String cityOne = productDetail.getCityOne();
		String cityTwo = productDetail.getCityTwo();
		City city1 = cityRepo.getCityByCode(cityOne);
		City city2 = cityRepo.getCityByCode(cityTwo);
		model.addAttribute("cityOne", city1.getCityName());
		model.addAttribute("cityTwo", city2.getCityName());
		model.addAttribute("city1", cityOne);
		model.addAttribute("city2", cityTwo);
		
		Date date1 = productDetail.getDate();  
		DateFormat dateFormat1 = new SimpleDateFormat("E, dd-MM-yyyy");  
	    String flightDateTime = dateFormat1.format(date1);
		model.addAttribute("flightDateTime", flightDateTime);
		
		Date date2 = order.getCreatedTime();  
		DateFormat dateFormat2 = new SimpleDateFormat("dd-MM-yyyy");  
	    String orderDateTime = dateFormat2.format(date2);
		model.addAttribute("orderDateTime", orderDateTime);
		
		model.addAttribute("passengerPhone", order.getPassengerNum());
		model.addAttribute("passengerEmail", order.getContactEmail());
		
		Product product = productDetail.getProduct();
		Brand brand = product.getBrands();
		model.addAttribute("brandPath", ".." + brand.getPhotosImagePath());
		model.addAttribute("brandName", brand.getName());
		model.addAttribute("productDetail", productDetail);
		model.addAttribute("originTerminal", product.getOriginTerminal());
		model.addAttribute("destinationTerminal", product.getDestinationTerminal());
		model.addAttribute("baggage", product.getBaggage());
		model.addAttribute("cabinBaggage", product.getCabinBaggage());
		
		Integer dateInt = productDetail.getDuration()/60;
		if (dateInt >= 10) {
			model.addAttribute("dateInt", dateInt);
		} else {
			model.addAttribute("dateInt", "0" + dateInt);
		}
		Integer timeInt = productDetail.getDuration()%60;
		if (timeInt >= 10) {
			model.addAttribute("timeInt", timeInt);
		} else {
			model.addAttribute("timeInt", "0" + timeInt);
		}
		
		Path flightUpPath = Paths.get("../pdf-images/flight-up.png");
		Path flightDownPath = Paths.get("../pdf-images/flight-down.png");
		Path demoTicketPath = Paths.get("../pdf-images/demo-ticket.png");
		Path thumbLogoPath = Paths.get("../pdf-images/thumb-logo.png");
		model.addAttribute("flightUpPath", flightUpPath);
		model.addAttribute("flightDownPath", flightDownPath);
		model.addAttribute("demoTicketPath", demoTicketPath);
		model.addAttribute("thumbLogoPath", thumbLogoPath);
		
		User user = product.getUser();
		model.addAttribute("user", user);
		
		List<TravellerDetail> travellerDetails = travellerRepo.findTravellerByProductDetailAndOrder(productDetail, order);
		model.addAttribute("travellerDetails", travellerDetails);
        
		model.addAttribute("paymentSuccess", OrderStatus.SUCCESSFULL);
		model.addAttribute("amount", order.getPrice());
		model.addAttribute("checksum", checksum);
		model.addAttribute("verifyChecksum", verifiedChecksum);
		model.addAttribute("responseParameters", responseParameters);
		
		return "wallet/response";
	}
}
