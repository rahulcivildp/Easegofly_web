package com.easygofly.site.flight;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
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

import com.easygofly.entity.BaggageOnline;
import com.easygofly.entity.CartItem;
import com.easygofly.entity.Customer;
import com.easygofly.entity.MealsOnline;
import com.easygofly.entity.ProductDetail;
import com.easygofly.entity.SearchHistory;
import com.easygofly.entity.SeatsOnline;
import com.easygofly.entity.TravellerDetail;
import com.easygofly.site.checkout.CheckoutInfo;
import com.easygofly.site.checkout.CheckoutService;
import com.easygofly.site.customer.CustomerService;
import com.easygofly.site.flightAPI.OnlineFlightService;
import com.easygofly.site.search.SearchHistoryController;
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
	@Autowired private OnlineFlightService onlineFlightService;
	@Autowired private SearchHistoryController searchHistoryController;
	
	public List<ProductDetail> listProductDetailsOnline;
	private Integer flightIdLocal = 0;
	List<BaggageOnline> baggageOnlineList = new ArrayList<BaggageOnline>();
	List<MealsOnline> mealsOnlineList = new ArrayList<MealsOnline>();
	List<SeatsOnline>  seatsOnlineList= new ArrayList<SeatsOnline>();
	
	public JSONObject mainObjFareBreakdownAdult = new JSONObject();
	public JSONObject mainObjFareBreakdownChild = new JSONObject(), mainObjFareBreakdownInfant = new JSONObject();
	
	public String basefareTravelerAdult = "", taxTravelerAdult = "", passengerTypeAdult = "";
	public String basefareTravelerChild = "", taxTravelerChild = "", passengerTypeChild = "";
	public String basefareTravelerInfant = "", taxTravelerInfant = "", passengerTypeInfant = "";
	
	public String depTerminal = "",arrTerminal = "",airlineCOde = "", flightNumber = "", flightClass = "", 
			airlineName = "", cabinBaggage = "", baggage = "", duration = "", flightStatus = "", stopOver = "", 
			passengerCount = "", airportCodeOrigin = "", airportCodeDestination = "", craftType = "";
	
	public String traceId ="" , resultIndex ="";
	
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
		
		
		if (!flight.getTraceId().equals("offline")) {
			
			/* Fare-rule details */
        	URL urlFarerule = new URL("http://api.tektravels.com/BookingEngineService_Air/AirService.svc/rest/FareRule");
            // Open a connection
            HttpURLConnection connectionFarerule = (HttpURLConnection) urlFarerule.openConnection();
            
            StringBuilder responseBodyFarerule = new StringBuilder();
            
        	int responseCode = onlineFlightService.apiOnlineFarerule_quote(connectionFarerule, responseBodyFarerule, searchHistoryController.traceId, flight.getResultIndex());
        	if (responseCode != HttpURLConnection.HTTP_OK) {
    			if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP
    				|| responseCode == HttpURLConnection.HTTP_MOVED_PERM
    					|| responseCode == HttpURLConnection.HTTP_SEE_OTHER)
    				return "redirect:/";
    		}
  
        	
        	JSONObject jsonObjFarerules = new JSONObject(responseBodyFarerule.toString());
        	JSONArray jsonObjFareruleResponse = jsonObjFarerules.getJSONObject("Response").getJSONArray("FareRules");
        	JSONObject jsonObjFarerule = jsonObjFareruleResponse.getJSONObject(0);
        	String fareRuleDetail = jsonObjFarerule.get("FareRuleDetail").toString();
        	
        	model.addAttribute("jsonObjFarerule", fareRuleDetail);
        	
        	
			/* Fare-quote details */
        	URL urlFarequote = new URL("http://api.tektravels.com/BookingEngineService_Air/AirService.svc/rest/FareQuote");
            // Open a connection
            HttpURLConnection connectionFarequote = (HttpURLConnection) urlFarequote.openConnection();
            
            StringBuilder responseBodyFarequote = new StringBuilder();
            
        	int responseCodeFarequote = onlineFlightService.apiOnlineFarerule_quote(connectionFarequote, responseBodyFarequote, searchHistoryController.traceId, flight.getResultIndex());
        	if (responseCodeFarequote != HttpURLConnection.HTTP_OK) {
    			if (responseCodeFarequote == HttpURLConnection.HTTP_MOVED_TEMP
    				|| responseCodeFarequote == HttpURLConnection.HTTP_MOVED_PERM
    					|| responseCodeFarequote == HttpURLConnection.HTTP_SEE_OTHER)
    				return "redirect:/";
    		}
        	
        	JSONObject jsonObjFareQuotes = new JSONObject(responseBodyFarequote.toString()); 
        	System.out.println(jsonObjFareQuotes);
        	
        	JSONObject jsonResult = jsonObjFareQuotes.getJSONObject("Response").getJSONObject("Results");
        	JSONArray jsonObjSegment = jsonResult.getJSONArray("Segments").getJSONArray(0);
        	JSONObject mainObjSegment = jsonObjSegment.getJSONObject(0);
    		JSONObject mainObjOrigin = mainObjSegment.getJSONObject("Origin");
    		JSONObject mainObjDestination = mainObjSegment.getJSONObject("Destination");
    		JSONObject mainObjAirline = mainObjSegment.getJSONObject("Airline");
    		JSONArray jsonObjFareBreakdown = jsonResult.getJSONArray("FareBreakdown");
    		mainObjFareBreakdownAdult = jsonObjFareBreakdown.getJSONObject(0);
    		
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
    		
    		depTerminal = mainObjOrigin.getJSONObject("Airport").get("Terminal").toString();
    		arrTerminal = mainObjDestination.getJSONObject("Airport").get("Terminal").toString();
    		airlineCOde = mainObjAirline.get("AirlineCode").toString();
    		flightNumber = mainObjAirline.get("FlightNumber").toString();
    		flightClass = mainObjAirline.get("FareClass").toString();
    		airlineName = mainObjAirline.get("AirlineName").toString();
    		cabinBaggage = mainObjSegment.get("CabinBaggage").toString();
    		baggage = mainObjSegment.get("Baggage").toString();
    		duration = mainObjSegment.get("Duration").toString();
    		flightStatus = mainObjSegment.get("FlightStatus").toString();
    		stopOver = mainObjSegment.get("StopOver").toString();
    		basefareTravelerAdult = mainObjFareBreakdownAdult.get("BaseFare").toString();
    		taxTravelerAdult = mainObjFareBreakdownAdult.get("Tax").toString();
    		passengerTypeAdult = mainObjFareBreakdownAdult.get("PassengerType").toString();
    		passengerCount = mainObjFareBreakdownAdult.get("PassengerCount").toString();
    		airportCodeOrigin = mainObjOrigin.getJSONObject("Airport").get("AirportCode").toString();
    		airportCodeDestination = mainObjDestination.getJSONObject("Airport").get("AirportCode").toString();
    		craftType = mainObjSegment.get("Craft").toString();
//    		String resultIndex = jsonResult.get("ResultIndex").toString();

//        	model.addAttribute("jsonObjFare_quote", jsonObjFareQuotes);
    		

    		/* SSR details */
        	URL urlSSR = new URL("http://api.tektravels.com/BookingEngineService_Air/AirService.svc/rest/SSR");
            // Open a connection
            HttpURLConnection connectionSSR = (HttpURLConnection) urlSSR.openConnection();
            
            StringBuilder responseBodySSR = new StringBuilder();
            
        	int responseCodeSSR = onlineFlightService.apiOnlineFarerule_quote(connectionSSR, responseBodySSR, searchHistoryController.traceId, flight.getResultIndex());
        	if (responseCodeSSR != HttpURLConnection.HTTP_OK) {
    			if (responseCodeSSR == HttpURLConnection.HTTP_MOVED_TEMP
    				|| responseCodeSSR == HttpURLConnection.HTTP_MOVED_PERM
    					|| responseCodeSSR == HttpURLConnection.HTTP_SEE_OTHER)
    				return "redirect:/";
    		}
        	
        	JSONObject jsonObjSSR = new JSONObject(responseBodySSR.toString()); 
        	System.out.println(jsonObjSSR);
        	
        	try {
				JSONArray jsonResultArrayBaggage = jsonObjSSR.getJSONObject("Response").getJSONArray("Baggage").getJSONArray(0); 
				BaggageOnline[] baggageOnline = new BaggageOnline[100];
				for (int i = 0; i < jsonResultArrayBaggage.length(); i++) {
					
					JSONObject jsonObjectBagages = jsonResultArrayBaggage.getJSONObject(i);
					
					String baggagePrice = jsonObjectBagages.get("Price").toString();
					String baggageCode = jsonObjectBagages.get("Code").toString();
					String baggageWeight = jsonObjectBagages.get("Weight").toString();
					
					baggageOnline[i] = new BaggageOnline(i, baggagePrice, baggageCode, baggageWeight);
					baggageOnlineList.add(baggageOnline[i]);
				}
			} catch (JSONException e1) {
				BaggageOnline baggageOnline = new BaggageOnline(1, "0", "NoBaggage", "0");
				baggageOnlineList.add(baggageOnline);
			}


        	try {
				JSONArray jsonResultArrayMeal = jsonObjSSR.getJSONObject("Response").getJSONArray("MealDynamic").getJSONArray(0); 
				MealsOnline[] mealsOnline = new MealsOnline[100];
				for (int i = 0; i < jsonResultArrayMeal.length(); i++) {
					JSONObject jsonObjectMeals = jsonResultArrayMeal.getJSONObject(i);
					String mealPrice = jsonObjectMeals.get("Price").toString();
					String mealAirlineDescription = jsonObjectMeals.get("AirlineDescription").toString();
					String mealCode = jsonObjectMeals.get("Code").toString();
					String mealQuantity = jsonObjectMeals.get("Quantity").toString();
					
					mealsOnline[i] = new MealsOnline(i, mealAirlineDescription, mealPrice, mealCode, mealQuantity);
					mealsOnlineList.add(mealsOnline[i]);
				}
			} catch (JSONException e) {
				MealsOnline mealsOnline = new MealsOnline(1, "No meal", "0", "NoMeal", "0");
				mealsOnlineList.add(mealsOnline);
			}
        	
        	try {
	        	JSONArray jsonArraySeats = jsonObjSSR.getJSONObject("Response").getJSONArray("SeatDynamic").getJSONObject(0).getJSONArray("SegmentSeat").getJSONObject(0).getJSONArray("RowSeats");
	        	JSONObject jsonInnerObjectSeats = new JSONObject();
	        	SeatsOnline[] seatsOnline = new SeatsOnline[500];
	        	Integer count = 1;
	        	for (int i = 0; i < jsonArraySeats.length(); i++) {
	        		JSONArray jsonInnerArraySeats = jsonArraySeats.getJSONObject(i).getJSONArray("Seats");
	        		
	        		for (int j = 0; j < jsonInnerArraySeats.length(); j++) {
	        			jsonInnerObjectSeats.put("Seat-" + (i + j) , jsonInnerArraySeats.getJSONObject(j));
	        			
	        			Integer compartment = Integer.parseInt(jsonInnerArraySeats.getJSONObject(j).get("Compartment").toString());
	        			Integer availablityType = Integer.parseInt(jsonInnerArraySeats.getJSONObject(j).get("AvailablityType").toString());
	        			Integer deck = Integer.parseInt(jsonInnerArraySeats.getJSONObject(j).get("Deck").toString());
	        			String rowNo = jsonInnerArraySeats.getJSONObject(j).get("RowNo").toString();
	        			String code = jsonInnerArraySeats.getJSONObject(j).get("Code").toString();
	        			String price = jsonInnerArraySeats.getJSONObject(j).get("Price").toString();
	        			Integer seatType = Integer.parseInt(jsonInnerArraySeats.getJSONObject(j).get("SeatType").toString());
	        			String seatNo = jsonInnerArraySeats.getJSONObject(j).get("SeatNo").toString();
	        			String craftTypeOnline = jsonInnerArraySeats.getJSONObject(j).get("CraftType").toString();
	        			
	        			Integer serialNo = count++;
	        			
	        			seatsOnline[serialNo] = new SeatsOnline(serialNo, price, compartment, availablityType, deck, rowNo, code, seatType, seatNo, craftTypeOnline);
	        			seatsOnlineList.add(seatsOnline[serialNo]);
					}
				}
			
			} catch (Exception e) {
				SeatsOnline seatsOnline = new SeatsOnline(1, "0", 0, 0, 0, "0", "NoSeat", 0, "0", "0");
    			seatsOnlineList.add(seatsOnline);
			}

        	MealsOnline mealsOnline2 = new MealsOnline();
        	
    		model.addAttribute("mealsOnline", mealsOnline2);
    		model.addAttribute("seatsOnlineList", seatsOnlineList);
    		model.addAttribute("mealsOnlineList", mealsOnlineList);
    		model.addAttribute("baggageOnlineList", baggageOnlineList);
        	
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
	public String filghtBookingSave(@RequestParam(name = "search_id") float searchId, 
			@RequestParam(name = "flight_id") Integer flightId, 
			@RequestParam(name = "adultNum") Integer adultNum,
			@RequestParam(name = "childNum") Integer childNum,
			@RequestParam(name = "infantNum") Integer infantNum,
			@RequestParam(name = "cityOne") String cityOne,
			@RequestParam(name = "cityTwo") String cityTwo,
			@RequestParam(name = "journeyClass") String journeyClass,
			@RequestParam(name = "date") String date,
			@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, 
			@AuthenticationPrincipal CustomerOAuth2User googleLogin, Model model) throws ParseException {
		String email; 
		Customer customer; 
		CartItem cartItem = new CartItem();
		Integer searchIdInt = 0;
		Date dateFlight = new SimpleDateFormat("yyyy-MM-dd").parse(date);
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByEmail(email);
			if (searchId == 1.5f) {
				Integer savedSearchId = searchHistoryController.saveHistoryPart(cityOne, cityTwo, dateFlight, journeyClass, "oneWay", adultNum, childNum,
						infantNum, customer);
				searchIdInt = savedSearchId;
			} else {
				searchIdInt = (int)searchId;
			}
			cartItem = travelerDetailsPart(searchIdInt, flightId, customer);
			return "redirect:/flight_booking" + searchIdInt + "&" + flightIdLocal + "&" + cartItem.getId();
			
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByEmail(email);
			if (searchId == 1.5f) {
				Integer savedSearchId = searchHistoryController.saveHistoryPart(cityOne, cityTwo, dateFlight, journeyClass, "oneWay", adultNum, childNum,
						infantNum, customer);
				searchIdInt = savedSearchId;
			} else {
				searchIdInt = (int)searchId;
			}
			cartItem = travelerDetailsPart(searchIdInt, flightId, customer);
			return "redirect:/flight_booking" + searchIdInt + "&" + flightIdLocal + "&" + cartItem.getId();
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
		ProductDetail flight = flightRepo.findById(flight_id).get();
		SearchHistory search = searchRepo.findById(search_id).get();
		CartItem item = cartRepo.findById(item_id).get();

		if (!flight.getTraceId().equals("offline")) {
        	/* Fare-rule details */
        	URL urlFarerule = new URL("http://api.tektravels.com/BookingEngineService_Air/AirService.svc/rest/FareRule");
            // Open a connection
            HttpURLConnection connectionFarerule = (HttpURLConnection) urlFarerule.openConnection();
            
            StringBuilder responseBodyFarerule = new StringBuilder();
            
        	int responseCode = onlineFlightService.apiOnlineFarerule_quote(connectionFarerule, responseBodyFarerule, searchHistoryController.traceId, flight.getResultIndex());
        	if (responseCode != HttpURLConnection.HTTP_OK) {
    			if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP
    				|| responseCode == HttpURLConnection.HTTP_MOVED_PERM
    					|| responseCode == HttpURLConnection.HTTP_SEE_OTHER)
    				return "redirect:/";
    		}
  
        	
        	JSONObject jsonObjFarerules = new JSONObject(responseBodyFarerule.toString());
        	System.out.println(jsonObjFarerules);
        	JSONArray jsonObjFareruleResponse = jsonObjFarerules.getJSONObject("Response").getJSONArray("FareRules");
        	JSONObject jsonObjFarerule = jsonObjFareruleResponse.getJSONObject(0);
        	String fareRuleDetail = jsonObjFarerule.get("FareRuleDetail").toString();
        	
        	model.addAttribute("jsonObjFarerule", fareRuleDetail);
        	
		} 
		
		model.addAttribute("listProductDetailsOnline", listProductDetailsOnline);
		
		List<TravellerDetail> travelers = productService.findTraveller(flight, item);
		for (TravellerDetail travellerDetail : travelers) {
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
				ProductSaveHelper.setTravellerDetail(salutation[i], firstName[i], lastName[i], dob[i], flight, item, paxType[i], flight.getBaggage(), flight.getCabinBaggage());
				productService.saveFlightPassengerDetails(flight);
				ProductDetail flightDetails = productService.saveFlightPassengerDetails(flight);
				model.addAttribute("flightDetails", flightDetails);
			}
			
			model.addAttribute("item", item);
			model.addAttribute("search", search);
			model.addAttribute("flight", flight);

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
