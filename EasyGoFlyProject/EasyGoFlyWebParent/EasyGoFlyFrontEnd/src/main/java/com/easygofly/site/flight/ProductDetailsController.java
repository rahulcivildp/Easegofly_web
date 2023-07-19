package com.easygofly.site.flight;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;

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
import com.easygofly.entity.PaxType;
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
	@Autowired private PaxTypeRepository paxTypeRepo ;
	
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
		
		if (!flight.getTraceId().equals(null)) {
        	/* Fare-quote details */
        	URL urlFarequote = new URL("http://api.tektravels.com/BookingEngineService_Air/AirService.svc/rest/FareQuote");
            // Open a connection
            HttpURLConnection connectionFarequote = (HttpURLConnection) urlFarequote.openConnection();
            
            StringBuilder responseBodyFarequote = new StringBuilder();
            
        	int responseCodeFarequote = onlineFlightService.apiOnlineFarerule_quote(connectionFarequote, responseBodyFarequote, flight.getTraceId(), flight.getResultIndex());
        	
        	System.out.println(responseCodeFarequote);
        	
        	JSONObject jsonObjFareQuotes = new JSONObject(responseBodyFarequote.toString());
        	
        	model.addAttribute("jsonObjFare_quote", jsonObjFareQuotes);
        	
		}
		
		List<TravellerDetail> travelers = productService.findTraveller(flight, item);

		CheckoutInfo checkoutInfo = checkoutService.prepareCheckout(item);
		
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
		Iterable<PaxType> paxTypes = paxTypeRepo.findAll();

		if (!flight.getTraceId().equals(null)) {
        	/* Fare-rule details */
        	URL urlFarerule = new URL("http://api.tektravels.com/BookingEngineService_Air/AirService.svc/rest/FareRule");
            // Open a connection
            HttpURLConnection connectionFarerule = (HttpURLConnection) urlFarerule.openConnection();
            
            StringBuilder responseBodyFarerule = new StringBuilder();
            
        	int responseCode = onlineFlightService.apiOnlineFarerule_quote(connectionFarerule, responseBodyFarerule, flight.getTraceId(), flight.getResultIndex());
        	
        	System.out.println(responseCode);
        	
        	JSONObject jsonObjFarerules = new JSONObject(responseBodyFarerule.toString());System.out.println(jsonObjFarerules);
        	JSONArray jsonObjFareruleResponse = jsonObjFarerules.getJSONObject("Response").getJSONArray("FareRules");
        	JSONObject jsonObjFarerule = jsonObjFareruleResponse.getJSONObject(0);
        	String fareRuleDetail = jsonObjFarerule.get("FareRuleDetail").toString();
        	model.addAttribute("jsonObjFarerule", fareRuleDetail);
        	
        	System.out.println(jsonObjFarerules);

        	/* Fare-quote details */
        	URL urlFarequote = new URL("http://api.tektravels.com/BookingEngineService_Air/AirService.svc/rest/FareQuote");
            // Open a connection
            HttpURLConnection connectionFarequote = (HttpURLConnection) urlFarequote.openConnection();
            
            StringBuilder responseBodyFarequote = new StringBuilder();
            
        	int responseCodeFarequote = onlineFlightService.apiOnlineFarerule_quote(connectionFarequote, responseBodyFarequote, flight.getTraceId(), flight.getResultIndex());
        	
        	System.out.println(responseCodeFarequote);
        	
        	JSONObject jsonObjFareQuotes = new JSONObject(responseBodyFarequote.toString()); 
        	JSONObject jsonResult = jsonObjFareQuotes.getJSONObject("Response").getJSONObject("Results");
        	JSONArray jsonObjSegment = jsonResult.getJSONArray("Segments").getJSONArray(0);
        	JSONObject mainObjSegment = jsonObjSegment.getJSONObject(0);
    		JSONObject mainObjOrigin = mainObjSegment.getJSONObject("Origin");
    		JSONObject mainObjDestination = mainObjSegment.getJSONObject("Destination");
    		JSONObject mainObjAirline = mainObjSegment.getJSONObject("Airline");
//    		JSONObject mainObjFare = mainObj.getJSONObject("Results").getJSONObject("Fare");
    		
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
//           
//    		String traceId = jsonObjFareQuotes.getJSONObject("Response").get("TraceId").toString();
//    		System.out.println(onlineFlightService.traceId);
//            
//	        String depAirportCode = mainObjOrigin.getJSONObject("Origin-" + i).getJSONObject("Airport").get("AirportCode").toString();
//	        String depAirportName = mainObjOrigin.getJSONObject("Origin-" + i).getJSONObject("Airport").get("AirportName").toString();
//	        String depTerminal = mainObjOrigin.getJSONObject("Origin-" + i).getJSONObject("Airport").get("Terminal").toString();
//	        
//	        String arrAirportCode = mainObjDestination.getJSONObject("Destination-" + i).getJSONObject("Airport").get("AirportCode").toString();
//	        String arrAirportName = mainObjDestination.getJSONObject("Destination-" + i).getJSONObject("Airport").get("AirportName").toString();
//	        String arrTerminal = mainObjDestination.getJSONObject("Destination-" + i).getJSONObject("Airport").get("Terminal").toString();
//	        
//	        String airlineName = mainObjAirline.getJSONObject("Airline-" + i).get("AirlineName").toString();
//	        String fareClass = mainObjAirline.getJSONObject("Airline-" + i).get("FareClass").toString();
//	        String flightNumber = mainObjAirline.getJSONObject("Airline-" + i).get("AirlineCode").toString() + "-" + mainObjAirline.getJSONObject("Airline-" + i).get("FlightNumber").toString();
//	        
//	        String depTime = mainObjOrigin.getJSONObject("Origin-" + i).get("DepTime").toString();
//	        String[] departureTimeParts = depTime.split("T");
//			String[] departureTimeInnerParts = departureTimeParts[1].split(":");
//			String stringDepTime = departureTimeInnerParts[0] + ":" + departureTimeInnerParts[1];
//			String depTimeString = departureTimeInnerParts[0] + "." + departureTimeInnerParts[1].charAt(0);
//			Float depTimeFloat = Float.parseFloat(depTimeString);
//			
//			String arrTime = mainObjDestination.getJSONObject("Destination-" + i).get("ArrTime").toString();
//			String[] arrivalTimeParts = arrTime.split("T");
//			String[] arrivalTimeInnerParts = arrivalTimeParts[1].split(":");
//			String stringArrTime = arrivalTimeInnerParts[0] + ":" + arrivalTimeInnerParts[1];
//			String arrTimeString = arrivalTimeInnerParts[0] + "." + arrivalTimeInnerParts[1].charAt(0);
//			Float arrTimeFloat = Float.parseFloat(arrTimeString);
//			
//			Integer duration = Integer.parseInt(mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(0).get("Duration").toString());
//			String flightStatus = mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(0).get("FlightStatus").toString();
//			
//			String strTotalFare = mainObjFare.getJSONObject("Fare-" + i).get("PublishedFare").toString();
//			Integer fareAdjustment = adultNum + childNum + infantNum;
//			Double doubleFare = Double.parseDouble(strTotalFare);
//			Integer intTotalFare = ((int) Math.round(doubleFare)) / fareAdjustment;
//			
//			String resultIndex = mainObj.getJSONObject("Result-" + i).get("ResultIndex").toString();
//			String airlineRemark = mainObj.getJSONObject("Result-" + i).get("AirlineRemark").toString();
//		
        	System.out.println(mainObjOrigin);
        	System.out.println(mainObjDestination);
        	System.out.println(depTerminal);
        	System.out.println(arrTerminal);
        	System.out.println(airlineCOde);
        	
        	System.out.println(flightNumber);
        	System.out.println(flightClass);
        	
        	System.out.println(airlineName);
        	System.out.println(cabinBaggage);
        	System.out.println(baggage);
        	System.out.println(duration);
        	System.out.println(flightStatus);
        	System.out.println(stopOver);
        	
        	model.addAttribute("jsonObjFare_quote", jsonObjFareQuotes);
        	
		}
		
		model.addAttribute("listProductDetailsOnline", listProductDetailsOnline);
		
		List<TravellerDetail> travelers = productService.findTraveller(flight, item);
		for (TravellerDetail travellerDetail : travelers) {
			System.out.println("working " + travellerDetail.getFirstName());
			model.addAttribute("travellerDetail", travellerDetail);
		}
		model.addAttribute("travelers", travelers);
		
		
		int[] list= new int[search.getPassengerNum()];
		
		model.addAttribute("paxTypes", paxTypes);
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
				PaxType passengerType = paxTypeRepo.findById(Integer.parseInt(paxType[i])).get();
				ProductSaveHelper.setTravellerDetail(salutation[i], firstName[i], lastName[i], dob[i], flight, item, passengerType);
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
