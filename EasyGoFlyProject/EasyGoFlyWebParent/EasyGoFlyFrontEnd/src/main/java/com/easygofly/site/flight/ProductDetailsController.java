package com.easygofly.site.flight;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.easygofly.entity.CartItem;
import com.easygofly.entity.Customer;
import com.easygofly.entity.ProductDetail;
import com.easygofly.entity.SearchHistory;
import com.easygofly.entity.TravellerDetail;
import com.easygofly.site.checkout.CheckoutInfo;
import com.easygofly.site.checkout.CheckoutService;
import com.easygofly.site.customer.CustomerService;
import com.easygofly.site.flightAPI.OnlineFlightService;
import com.easygofly.site.search.SearchHistoryRepository;
import com.easygofly.site.search.SearchHistoryService;
import com.easygofly.site.security.EasyGoFlyCustomerDetails;
import com.easygofly.site.security.oauth.CustomerOAuth2User;
import com.easygofly.site.shoppingCart.CartItemRepository;
import com.easygofly.site.shoppingCart.CartItemService;

@Controller
public class ProductDetailsController {
	
	@Autowired private ProductDetailService productService;
	@Autowired private CustomerService customerService;
	@Autowired private FlightRepository flightRepo;
	@Autowired private SearchHistoryRepository searchRepo;
	@Autowired private SearchHistoryService searchService;
	@Autowired private CartItemRepository cartRepo;
	@Autowired private CartItemService cartService;
	@Autowired private TravellerRepository travelerRepo;
	@Autowired private CheckoutService checkoutService;
	@Autowired private ProductDetailCrudRepository productDetailCrudRepo;
	@Autowired private OnlineFlightService onlineFlightService ;
	@Autowired private EntityManager entityManager;
	
	public List<ProductDetail> listProductDetailsOnline;
	private Integer flightIdLocal = 0;
	
	@GetMapping("/flight_traveler_details{search_id}&{flight_id}&{item_id}")
	public String filghtTravelerDetailsSave(@PathVariable(name = "search_id") Integer search_id, 
			@PathVariable(name = "flight_id") Integer flight_id, 
			@PathVariable(name = "item_id") Integer item_id, 
			@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, 
			@AuthenticationPrincipal CustomerOAuth2User googleLogin,
			Model model, CartItem cartItem) throws IOException {
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
		
		ProductDetail flight = flightRepo.findById(flight_id).get();
		SearchHistory search = searchRepo.findById(search_id).get();
		CartItem item = cartRepo.findById(item_id).get();
		
		List<TravellerDetail> travelers = productService.findTraveller(flight, item);
		CheckoutInfo checkoutInfo = checkoutService.prepareCheckout(item);

		List<String> travelerDetailsArray = new ArrayList<String>();
		
		
		if (!flight.getTraceId().equals(null)) {
        	/* Fare-quote details */
        	URL urlFarequote = new URL("http://api.tektravels.com/BookingEngineService_Air/AirService.svc/rest/FareQuote");
            // Open a connection
            HttpURLConnection connectionFarequote = (HttpURLConnection) urlFarequote.openConnection();
            
            StringBuilder responseBodyFarequote = new StringBuilder();
            
        	int responseCodeFarequote = onlineFlightService.apiOnlineFarerule_quote(connectionFarequote, responseBodyFarequote, flight.getTraceId(), flight.getResultIndex());
        	if (responseCodeFarequote != HttpURLConnection.HTTP_OK) {
    			if (responseCodeFarequote == HttpURLConnection.HTTP_MOVED_TEMP
    				|| responseCodeFarequote == HttpURLConnection.HTTP_MOVED_PERM
    					|| responseCodeFarequote == HttpURLConnection.HTTP_SEE_OTHER)
    				return "redirect:/";
    		}
    		
        	System.out.println(responseCodeFarequote);
        	
        	JSONObject jsonObjFareQuotes = new JSONObject(responseBodyFarequote.toString()); 
        	JSONObject jsonResult = jsonObjFareQuotes.getJSONObject("Response").getJSONObject("Results");
        	JSONArray jsonObjSegment = jsonResult.getJSONArray("Segments").getJSONArray(0);
        	JSONObject mainObjSegment = jsonObjSegment.getJSONObject(0);
    		JSONObject mainObjOrigin = mainObjSegment.getJSONObject("Origin");
    		JSONObject mainObjDestination = mainObjSegment.getJSONObject("Destination");
    		JSONObject mainObjAirline = mainObjSegment.getJSONObject("Airline");
    		JSONArray jsonObjFareBreakdown = jsonResult.getJSONArray("FareBreakdown");
    		JSONObject mainObjFareBreakdownAdult = jsonObjFareBreakdown.getJSONObject(0);
    		JSONObject mainObjFareBreakdownChild, mainObjFareBreakdownInfant;
    		String basefareTravelerChild = "", taxTravelerChild = "", passengerTypeChild = "";
    		String basefareTravelerInfant = "", taxTravelerInfant = "", passengerTypeInfant = "";
    		
    		if (jsonObjFareBreakdown.length() == 2 ) {
    			mainObjFareBreakdownChild = jsonObjFareBreakdown.getJSONObject(1);
        		basefareTravelerChild = mainObjFareBreakdownAdult.get("BaseFare").toString();
        		taxTravelerChild = mainObjFareBreakdownAdult.get("Tax").toString();
        		passengerTypeChild = mainObjFareBreakdownAdult.get("PassengerType").toString();
			} else if (jsonObjFareBreakdown.length() == 3 ) {
				mainObjFareBreakdownInfant = jsonObjFareBreakdown.getJSONObject(2);
				basefareTravelerInfant = mainObjFareBreakdownAdult.get("BaseFare").toString();
				taxTravelerInfant = mainObjFareBreakdownAdult.get("Tax").toString();
				passengerTypeInfant = mainObjFareBreakdownAdult.get("PassengerType").toString();
			}
    		
    		String depTerminal = mainObjOrigin.getJSONObject("Airport").get("Terminal").toString();
    		String arrTerminal = mainObjDestination.getJSONObject("Airport").get("Terminal").toString();
    		String airlineCOde = mainObjAirline.get("AirlineCode").toString();
    		String flightNumber = mainObjAirline.get("FlightNumber").toString();
    		String flightClass = mainObjAirline.get("FareClass").toString();
    		String airlineName = mainObjAirline.get("AirlineName").toString();
    		String cabinBaggage = mainObjSegment.get("CabinBaggage").toString();
    		String baggage = mainObjSegment.get("Baggage").toString();
    		String duration = mainObjSegment.get("Duration").toString();
    		String flightStatus = mainObjSegment.get("FlightStatus").toString();
    		String stopOver = mainObjSegment.get("StopOver").toString();
    		String basefareTravelerAdult = mainObjFareBreakdownAdult.get("BaseFare").toString();
    		String taxTravelerAdult = mainObjFareBreakdownAdult.get("Tax").toString();
    		String passengerTypeAdult = mainObjFareBreakdownAdult.get("PassengerType").toString();
    		String passengerCount = mainObjFareBreakdownAdult.get("PassengerCount").toString();
    		String airportCodeOrigin = mainObjOrigin.getJSONObject("Airport").get("AirportCode").toString();
    		String airportCodeDestination = mainObjDestination.getJSONObject("Airport").get("AirportCode").toString();
    		String craftType = mainObjSegment.get("Craft").toString();
//    		String resultIndex = jsonResult.get("ResultIndex").toString();

//        	model.addAttribute("jsonObjFare_quote", jsonObjFareQuotes);
        	
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
    				baseFare = basefareTravelerAdult;
    				tax = taxTravelerAdult;
				} else if (travellerDetail.getPaxType().equals("2")) {
    				baseFare = basefareTravelerChild;
    				tax = taxTravelerChild;
				} else {
    				baseFare = basefareTravelerInfant;
    				tax = taxTravelerInfant;
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
    					+ "		\"FFAirlineCode\": \"" + airlineCOde + "\",\r\n"
    					+ "		\"FFNumber\": \"" + flightNumber + "\",\r\n"
    					+ "		\"Baggage\":[\r\n"
    					+ "            {\r\n"
    					+ "                \"AirlineCode\": \"" + airlineCOde + "\",\r\n"
    					+ "                \"FlightNumber\": \"" + flightNumber + "\",\r\n"
    					+ "                \"WayType\": 2,\r\n"
    					+ "                \"Code\": \"No Baggage\",\r\n"
    					+ "                \"Description\": 2,\r\n"
    					+ "                \"Weight\": 0,\r\n"
    					+ "                \"Currency\": \"INR\",\r\n"
    					+ "                 \"Price\": 0,\r\n"
    					+ "                 \"Origin\": \"" + airportCodeOrigin + "\",\r\n"
    					+ "                \"Destination\": \"" + airportCodeDestination + "\"\r\n"
    					+ "				}\r\n"
    					+ "			],\r\n"
    					+ "     \"MealDynamic\": [\r\n"
    					+ "        {\r\n"
    					+ "          \"AirlineCode\": \"" + airlineCOde + "\",\r\n"
    					+ "          \"FlightNumber\": \"" + flightNumber + "\",\r\n"
    					+ "          \"WayType\": 2,\r\n"
    					+ "          \"Code\": \"No Meal\",\r\n"
    					+ "          \"Description\": 2,\r\n"
    					+ "          \"AirlineDescription\": \"\",\r\n"
    					+ "          \"Quantity\": 0,\r\n"
    					+ "          \"Currency\": \"INR\",\r\n"
    					+ "          \"Price\": 0,\r\n"
    					+ "          \"Origin\": \"" + airportCodeOrigin + "\",\r\n"
    					+ "          \"Destination\": \"" + airportCodeDestination + "\"\r\n"
    					+ "        }],\r\n"
    					+ "		\"SeatDynamic\": [\r\n"
    					+ "        {\r\n"
    					+ "	    \"AirlineCode\": \"" + airlineCOde + "\",\r\n"
    					+ "             \"FlightNumber\": \"" + flightNumber + "\",\r\n"
    					+ "              \"CraftType\": \"" + craftType + "\",\r\n"
    					+ "               \"Origin\": \"" + airportCodeOrigin + "\",\r\n"
    					+ "                \"Destination\": \"" + airportCodeDestination + "\",\r\n"
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
            
        	int responseCodeTicket = onlineFlightService.apiOnlineTicket(connectionTicket, responseBodyTicket, flight.getTraceId(), flight.getResultIndex(), arrayTraveler);
        	
        	System.out.println(responseCodeTicket);
        	
        	JSONObject jsonObjTicket = new JSONObject(responseBodyTicket.toString()); 

        	System.out.println(jsonObjTicket);
        	
		}
		
		model.addAttribute("checkoutInfo", checkoutInfo);
		model.addAttribute("travelers", travelers);
		model.addAttribute("item", item);
		model.addAttribute("search", search);
		model.addAttribute("flight", flight);
		model.addAttribute("falied", "Please provide a correct coupon code!!!");
		model.addAttribute("success", "The coupon is verified!");
		
		return "flight/booking/flight_traveler_details";
	}
	
	
	private CartItem travelerDetailsPart(Integer searchId, Integer flightId, Customer customer) {
		ProductDetail flight = null;
		SearchHistory search = searchRepo.findById(searchId).get();
		
		try {
			String mode = "Offline-data";
			ProductDetail newFlight  = flightRepo.findProductDetailByIdMode(flightId, mode);
			
			newFlight.addBooking(customer);
			productService.saveCartItem(newFlight);
			
			ProductDetail savedCart = productService.saveCartItem(newFlight);
			List<CartItem> savedCartItemProduct = savedCart.getCartItems();
			for (CartItem cartItem : savedCartItemProduct) {
				cartItem.setCartMode("offline");
				cartRepo.save(cartItem);
			}
			CartItem LastItem = savedCartItemProduct.get(savedCartItemProduct.size() - 1);
			float totalAdultPrice = (newFlight.getPriceADT() + newFlight.getMarkupADT()) * (search.getAdultNum() + search.getChildNum());
			float totalInfantPrice = (newFlight.getPriceINF() + newFlight.getMarkupINF()) * search.getInfantNum();
			double totalPrice = totalAdultPrice + totalInfantPrice;
			
			System.out.println("totalAdultPrice : " + totalAdultPrice + " totalInfantPrice : " + totalInfantPrice + " totalPrice : " + totalPrice);
			
			cartService.updateTotalPrice(LastItem, totalPrice);
			searchService.updateSearchHistory(search, LastItem);
			
			flightIdLocal = newFlight.getId();
			
			return LastItem;
			
		} catch (Exception e) {
			for (ProductDetail flightOnline : listProductDetailsOnline) {
				if (flightOnline.getId() == flightId) {
					flight = flightOnline;
					ProductDetail newFlightOnlineSaved = productDetailCrudRepo.save(flight);
					
					String modeOnline = "Online-data";
					ProductDetail newFlightOnline  = flightRepo.findProductDetailByIdMode(newFlightOnlineSaved.getId(), modeOnline);
					
					newFlightOnline.addBooking(customer);
					productService.saveCartItem(newFlightOnline);
					
					ProductDetail savedCartOnline = productService.saveCartItem(newFlightOnline);
					List<CartItem> savedCartItemProductOnline = savedCartOnline.getCartItems();
					for (CartItem cartItem : savedCartItemProductOnline) {
						cartItem.setCartMode("online");
						cartRepo.save(cartItem);
					}
					CartItem LastItemOnline = savedCartItemProductOnline.get(savedCartItemProductOnline.size() - 1);
					float totalAdultPriceOnline = (newFlightOnline.getPriceADT() + newFlightOnline.getMarkupADT()) * (search.getAdultNum() + search.getChildNum());
					float totalInfantPriceOnline = (newFlightOnline.getPriceINF() + newFlightOnline.getMarkupINF()) * search.getInfantNum();
					double totalPriceOnline = totalAdultPriceOnline + totalInfantPriceOnline;
					
					System.out.println("totalAdultPrice : " + totalAdultPriceOnline + " totalInfantPrice : " + totalInfantPriceOnline + " totalPrice : " + totalPriceOnline);
					
					cartService.updateTotalPrice(LastItemOnline, totalPriceOnline);
					searchService.updateSearchHistory(search, LastItemOnline);
					
					flightIdLocal = newFlightOnlineSaved.getId();
					
					return LastItemOnline;
				}
			}
		}
		
		return null;
	}
	
	@PostMapping("/flight_booking_save")
	public String filghtBookingSave(@RequestParam(name = "search_id") Integer searchId, 
			@RequestParam(name = "flight_id") Integer flightId, 
			@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, 
			@AuthenticationPrincipal CustomerOAuth2User googleLogin, Model model) {
		String email; 
		Customer customer; 
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByEmail(email);
			CartItem cartItem = travelerDetailsPart(searchId, flightId, customer);
			System.out.println("flightIdLocal: " + flightIdLocal);
			return "redirect:/flight_booking" + searchId + "&" + flightIdLocal + "&" + cartItem.getId();
			
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByEmail(email);
			CartItem cartItem = travelerDetailsPart(searchId, flightId, customer);
			return "redirect:/flight_booking" + searchId + "&" + flightIdLocal + "&" + cartItem.getId();
		} else {
			return "redirect:/";
		}
	}
	
	@GetMapping("/flight_booking{search_id}&{flight_id}&{item_id}")
	public String filghtBookingSave(@PathVariable(name = "search_id") Integer search_id, 
			@PathVariable(name = "flight_id") Integer flight_id,
			@PathVariable(name = "item_id") Integer item_id, 
			@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, 
			@AuthenticationPrincipal CustomerOAuth2User googleLogin,
			Model model, 
			CartItem cartItem ) throws IOException {
		
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
		System.out.println("Flight ID: " + flight_id);
		ProductDetail flight = flightRepo.findById(flight_id).get();
		SearchHistory search = searchRepo.findById(search_id).get();
		CartItem item = cartRepo.findById(item_id).get();

		if (!flight.getTraceId().equals(null)) {
        	/* Fare-rule details */
        	URL urlFarerule = new URL("http://api.tektravels.com/BookingEngineService_Air/AirService.svc/rest/FareRule");
            // Open a connection
            HttpURLConnection connectionFarerule = (HttpURLConnection) urlFarerule.openConnection();
            
            StringBuilder responseBodyFarerule = new StringBuilder();
            
        	int responseCode = onlineFlightService.apiOnlineFarerule_quote(connectionFarerule, responseBodyFarerule, flight.getTraceId(), flight.getResultIndex());
        	if (responseCode != HttpURLConnection.HTTP_OK) {
    			if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP
    				|| responseCode == HttpURLConnection.HTTP_MOVED_PERM
    					|| responseCode == HttpURLConnection.HTTP_SEE_OTHER)
    				return "redirect:/";
    		}
    		
        	System.out.println(responseCode);
        	
        	JSONObject jsonObjFarerules = new JSONObject(responseBodyFarerule.toString());System.out.println(jsonObjFarerules);
        	JSONArray jsonObjFareruleResponse = jsonObjFarerules.getJSONObject("Response").getJSONArray("FareRules");
        	JSONObject jsonObjFarerule = jsonObjFareruleResponse.getJSONObject(0);
        	String fareRuleDetail = jsonObjFarerule.get("FareRuleDetail").toString();
        	
        	model.addAttribute("jsonObjFarerule", fareRuleDetail);
		}
		
		model.addAttribute("listProductDetailsOnline", listProductDetailsOnline);
		
		List<TravellerDetail> travelers = productService.findTraveller(flight, item);
		for (TravellerDetail travellerDetail : travelers) {
			System.out.println("working " + travellerDetail.getFirstName());
			model.addAttribute("travellerDetail", travellerDetail);
		}
		model.addAttribute("travelers", travelers);
		
		
		int[] list= new int[search.getPassengerNum()];
		
		model.addAttribute("list", list);
		model.addAttribute("item", item);
		model.addAttribute("search", search);
		model.addAttribute("flight", flight);
		
		return "flight/booking/flight_booking";
	}
	
	@PostMapping("/flight_activity_booking_save")
	public String filghtActivityBookingSave(
			@RequestParam(name = "flight_id") Integer flightId,  
			@RequestParam(name = "cityOne", required = false) String cityOne, 
			@RequestParam(name = "cityTwo", required = false) String cityTwo,  
			@RequestParam(name = "passengerNum", required = false) Integer passengerNum,
			@RequestParam(name = "journeyClass", required = false) String journeyClass,
			@RequestParam(name = "tripType", required = false) String tripType,
			@RequestParam(name = "adultNum", required = false) Integer adultNum,
			@RequestParam(name = "childNum", required = false) Integer childNum,
			@RequestParam(name = "infantNum", required = false) Integer infantNum,
			@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, 
			@AuthenticationPrincipal CustomerOAuth2User googleLogin, Model model) {
		ProductDetail productDetail = flightRepo.findById(flightId).get();
		
		String email; 
		Customer customer; 
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByEmail(email);
			
			SearchHistory history = searcHistorySave(productDetail, cityOne, cityTwo, passengerNum, journeyClass, tripType, adultNum,
					childNum, infantNum, customer);
			
			return "redirect:/flight_booking" + history.getId() + "&" + flightId + "&" + travelerDetailsPart(history.getId(), flightId, customer);
			
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByEmail(email);
			
			SearchHistory history = searcHistorySave(productDetail, cityOne, cityTwo, passengerNum, journeyClass, tripType, adultNum,
					childNum, infantNum, customer);
			
			model.addAttribute("customer", customer);
			return "redirect:/flight_booking" + history.getId() + "&" + flightId + "&" + travelerDetailsPart(history.getId(), flightId, customer);
		} else {
			return "redirect:/login";
		}
	}

	private SearchHistory searcHistorySave(ProductDetail productDetail, String cityOne, String cityTwo, Integer passengerNum, String journeyClass,
			String tripType, Integer adultNum, Integer childNum, Integer infantNum, Customer customer) {
		SearchHistory history = new SearchHistory();
		history.setDate(productDetail.getDate());
		history.setCityOne(cityOne);
		history.setCityTwo(cityTwo);
		history.setPassengerNum(passengerNum);
		history.setJourneyClass(journeyClass);
		history.setTripType(tripType);
		history.setAdultNum(adultNum);
		history.setChildNum(childNum);
		history.setInfantNum(infantNum);
		history.setCustomer(customer);
		
		return searchRepo.save(history);
	}

	
	
	@PostMapping("/traveller_details")
	public String saveTravellerDetails(@RequestParam(name = "search_id") Integer searchId, 
			@RequestParam(name = "flight_id") Integer flightId,
			@RequestParam(name = "item_id") Integer item_id,
			@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer,  
			@AuthenticationPrincipal CustomerOAuth2User googleLogin,
			Model model,
			@RequestParam(name = "salutation", required = false) String[] salutation, 
			@RequestParam(name = "firstName", required = false) String[] firstName,  
			@RequestParam(name = "lastName", required = false) String[] lastName, 
			@RequestParam(name = "dob", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date[] dob, 
			@RequestParam(name = "paxType", required = false) String[] paxType, 
			CartItem cartItem	 ) {
		try {
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
			
			ProductDetail flight = flightRepo.findById(flightId).get();
			SearchHistory search = searchRepo.findById(searchId).get();
			CartItem item = cartRepo.findById(item_id).get();
			
			cartService.updateCartItem(item, cartItem.getEmail(), cartItem.getPhoneNum(), search.getPassengerNum(), false); 
			
			
				
			for (int i = 0; i < search.getPassengerNum(); i++) {
				ProductSaveHelper.setTravellerDetail(salutation[i], firstName[i], lastName[i], dob[i], flight, item, paxType[i]);
				productService.saveFlightPassengerDetails(flight);
				ProductDetail flightDetails = productService.saveFlightPassengerDetails(flight);
				model.addAttribute("flightDetails", flightDetails);
			}
			
			model.addAttribute("item", item);
			model.addAttribute("search", search);
			model.addAttribute("flight", flight);
			System.out.println("Item Id: " + item_id);
			System.out.println("Item Id: " + item.getEmail());

			return  "redirect:/flight_traveler_details" + searchId + "&" + flightId + "&" + item_id;
		} catch (Exception e) {
			return  "redirect:/flight_traveler_details" + searchId + "&" + flightId + "&" + item_id;
		}
	}
	
	@PostMapping("/traveller_detail_edit")
	public String updateTravellerDetail(@RequestParam(name = "traveler_id") Integer traveler_id,
			@RequestParam(name = "search_id") Integer searchId, 
			@RequestParam(name = "flight_id") Integer flightId,
			@RequestParam(name = "item_id") Integer item_id,
			@RequestParam(name = "saveSalutation", required = false) String salutation, 
			@RequestParam(name = "saveFirstName", required = false) String firstName,  
			@RequestParam(name = "saveLastName", required = false) String lastName, 
			@RequestParam(name = "savedob", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date dob) {
		TravellerDetail traveler = travelerRepo.findById(traveler_id).get();
		
		cartService.updateTraveler(traveler, salutation, firstName, lastName, dob);
		
		return  "redirect:/flight_traveler_details" + searchId + "&" + flightId + "&" + item_id;
	}
	@PostMapping("/cartItem_edit")
	public String updateCartItem(
			@RequestParam(name = "search_id") Integer searchId, 
			@RequestParam(name = "flight_id") Integer flightId,
			@RequestParam(name = "item_id") Integer item_id, CartItem cartItem) {

		CartItem item = cartRepo.findById(item_id).get();
		SearchHistory search = searchRepo.findById(searchId).get();
		
		cartService.updateCartItem(item, cartItem.getEmail(), cartItem.getPhoneNum(), search.getPassengerNum(), false); 
		
		return  "redirect:/flight_traveler_details" + searchId + "&" + flightId + "&" + item_id;
		
	}
}
