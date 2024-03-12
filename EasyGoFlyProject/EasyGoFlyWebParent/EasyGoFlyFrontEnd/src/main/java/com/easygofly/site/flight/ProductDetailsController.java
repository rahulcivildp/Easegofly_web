package com.easygofly.site.flight;

import java.io.IOException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import com.easygofly.entity.City;
import com.easygofly.entity.Customer;
import com.easygofly.entity.FlightMap;
import com.easygofly.entity.MealsOnline;
import com.easygofly.entity.ProductDetail;
import com.easygofly.entity.SearchHistory;
import com.easygofly.entity.SeatsOnline;
import com.easygofly.entity.TravellerDetail;
import com.easygofly.site.LogService;
import com.easygofly.site.checkout.CheckoutInfo;
import com.easygofly.site.checkout.CheckoutService;
import com.easygofly.site.customer.CustomerService;
import com.easygofly.site.flightAPI.OnlineFlightService;
import com.easygofly.site.search.SearchHistoryController;
import com.easygofly.site.search.SearchHistoryRepository;
import com.easygofly.site.search.SearchHistoryService;
import com.easygofly.site.security.EasegoflyPhoneCustomerDetails;
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
	@Autowired private OnlineFlightService onlineFlightService;
	@Autowired private LogService logService;
	@Autowired private ProductDetailCrudRepository productDetailCrudRepo;
	@Autowired private SearchHistoryController sHistoryController;
	@Autowired private CityRepository cityRepo;

	public List<ProductDetail> listProductDetails;
	public List<ProductDetail> listProductDetailsInSearch = new ArrayList<ProductDetail>();
	public List<ProductDetail> listProductDetailsOnline;
	public List<ProductDetail> listProductDetailsOnlineReturn;
	public List<FlightMap> flightMaps;
	
	List<BaggageOnline> baggageOnlineList = new ArrayList<BaggageOnline>();
	List<MealsOnline> mealsOnlineList = new ArrayList<MealsOnline>();
	List<SeatsOnline>  seatsOnlineList = new ArrayList<SeatsOnline>();
	List<BaggageOnline> baggageOnlineListReturn = new ArrayList<BaggageOnline>();
	List<MealsOnline> mealsOnlineListReturn = new ArrayList<MealsOnline>();
	List<SeatsOnline>  seatsOnlineListReturn = new ArrayList<SeatsOnline>();
	
	public JSONObject mainObjFareBreakdown = new JSONObject();
	public JSONObject mainObjFareBreakdownAdult = new JSONObject();
	public JSONObject mainObjFareBreakdownChild = new JSONObject(), mainObjFareBreakdownInfant = new JSONObject();
	
	public String basefareTravelerAdult = "", taxTravelerAdult = "", passengerTypeAdult = "";
	public String basefareTravelerChild = "", taxTravelerChild = "", passengerTypeChild = "";
	public String basefareTravelerInfant = "", taxTravelerInfant = "", passengerTypeInfant = "";
	public String basefareTravelerAdultReturn = "", taxTravelerAdultReturn = "", passengerTypeAdultReturn = "";
	public String basefareTravelerChildReturn = "", taxTravelerChildReturn = "", passengerTypeChildReturn = "";
	public String basefareTravelerInfantReturn = "", taxTravelerInfantReturn = "", passengerTypeInfantReturn = "";
	public String depTerminal = "",arrTerminal = "",airlineCOde = "", flightNumber = "", flightClass = "", 
			airlineName = "", cabinBaggage = "", baggage = "", duration = "", flightStatus = "", stopOver = "", 
			airportCodeOrigin = "", airportCodeDestination = "", craftType = "";
	public String traceId ="" , resultIndex ="";
	public String discount ="" , tdsOnIncentive ="", tdsOnCommission ="", tdsOnPLB ="", otherCharges ="", publishedFare ="", offeredFare ="", serviceFee ="";
	public String discountReturn ="" , tdsOnIncentiveReturn ="", tdsOnCommissionReturn ="", tdsOnPLBReturn ="", otherChargesReturn ="", publishedFareReturn ="", offeredFareReturn ="", serviceFeeReturn ="";
	
	public boolean lcc = true;
	public boolean lccReturn = true;

	public Integer timeRemainingPro = 0;
	public Integer timeRemainingProOne = 0;
	////Flight one-way segment

	@GetMapping("/flight_traveler_details{search_id}&{flight_id}&{item_id}")
	public String filghtTravelerDetailsSave(@PathVariable(name = "search_id") Integer search_id, 
			@PathVariable(name = "flight_id") Integer flight_id, 
			@PathVariable(name = "item_id") Integer item_id, 
			@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer, 
			@AuthenticationPrincipal CustomerOAuth2User googleLogin,
			Model model, CartItem cartItem) throws IOException {
		String email; 
		Customer customer; 
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByPhone(email);
			model.addAttribute("customer", customer);
			
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByPhone(email);
			model.addAttribute("customer", customer);
		}
		
		ProductDetail flight = flightRepo.findById(flight_id).get();
		SearchHistory search = searchRepo.findById(search_id).get();
		CartItem item = cartRepo.findById(item_id).get();
	    City cityOneFound = cityRepo.getCityByCode(search.getCityOne());
	    City cityTwoFound = cityRepo.getCityByCode(search.getCityTwo());
		
		List<TravellerDetail> travelers = productService.findTraveller(flight, item);
		CheckoutInfo checkoutInfo = checkoutService.prepareCheckout(item);
		
		try {
			fareQuoteSSRMethod(model, flight);
			
		}  catch (IOException e) {
			return "redirect:/flight_booking" + search.getId() + "&" + flight.getId() + "&" + item.getId();
		}
		
		model.addAttribute("checkoutInfo", checkoutInfo);
		model.addAttribute("travelers", travelers);
		model.addAttribute("item", item);
		model.addAttribute("search", search);
		model.addAttribute("flight", flight);
		model.addAttribute("falied", "Please provide a correct coupon code!!!");
		model.addAttribute("success", "The coupon is verified!");
		model.addAttribute("cityOneName", cityOneFound.getCityName());
		model.addAttribute("cityTwoName", cityTwoFound.getCityName());
		model.addAttribute("timeRemainingPro", timeRemainingProOne);
		
		
		return "flight/booking/flight_traveler_details";
	}

	@PostMapping("/traveller_details")
	public String saveTravellerDetails(@RequestParam(name = "search_id") Integer searchId, 
			@RequestParam(name = "timeRemaining") Integer timeRemaining,
			@RequestParam(name = "flight_id") Integer flightId,
			@RequestParam(name = "item_id") Integer item_id,
			@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer,  
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
				customer = customerService.getByPhone(email);
				model.addAttribute("customer", customer);
				
			} else if (googleLogin != null) {
				email = googleLogin.getEmail();
				customer = customerService.getByPhone(email);
				model.addAttribute("customer", customer);
			}
			timeRemainingProOne = timeRemaining;
			Date newDate = new Date();
			Calendar c = Calendar.getInstance();
			c.add(Calendar.YEAR, 2);
			
			newDate = c.getTime();
			
			System.out.println(newDate);
			
			ProductDetail flight = flightRepo.findById(flightId).get();
			SearchHistory search = searchRepo.findById(searchId).get();
			CartItem item = cartRepo.findById(item_id).get();
			
			cartService.updateCartItem(item, cartItem.getEmail(), cartItem.getPhoneNum(), search.getPassengerNum(), false); 
				
			for (int i = 0; i < search.getPassengerNum(); i++) {
				ProductSaveHelper.setTravellerDetail(salutation[i], firstName[i], lastName[i], dob[i], flight, item, paxType[i], flight.getBaggage(), flight.getCabinBaggage(), i, "KJHHJKHKJH", newDate);
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
	
	@PostMapping("/flight_booking_save")
	public String filghtBookingSave(@RequestParam(name = "search_id") float searchId, 
			@RequestParam(name = "timeRemaining") Integer timeRemaining,
			@RequestParam(name = "flight_id") Integer flightId, 
			@RequestParam(name = "adultNum") Integer adultNum,
			@RequestParam(name = "childNum") Integer childNum,
			@RequestParam(name = "infantNum") Integer infantNum,
			@RequestParam(name = "cityOne") String cityOne,
			@RequestParam(name = "cityTwo") String cityTwo,
			@RequestParam(name = "journeyClass") String journeyClass,
			@RequestParam(name = "date") String date,
			@RequestParam(name = "device") String device,
			@RequestParam(name = "deviceInfo") String deviceInfo,
			@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer, 
			@AuthenticationPrincipal CustomerOAuth2User googleLogin, Model model) throws ParseException {
		String email; 
		Customer customer; 
		CartItem cartItem = new CartItem();
		Integer searchIdInt = 0;
		Date dateFlight = new SimpleDateFormat("yyyy-MM-dd").parse(date);
		timeRemainingProOne = timeRemaining;
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByPhone(email);
			if (searchId == 1.5f) {
				Integer savedSearchId = sHistoryController.saveHistoryPart(cityOne, cityTwo, dateFlight, journeyClass, "oneWay", adultNum, childNum,
						infantNum, customer);
				searchIdInt = savedSearchId;
			} else {
				searchIdInt = (int)searchId;
			}
			cartItem = travelerDetailsPart(searchIdInt, flightId, customer);
			ProductDetail productDetail = cartItem.getProductDetail();
			
			productService.updateDeviceInfo(productDetail, device, deviceInfo);
			
			return "redirect:/flight_booking" + searchIdInt + "&" + productDetail.getId() + "&" + cartItem.getId();
			
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByPhone(email);
			if (searchId == 1.5f) {
				Integer savedSearchId = sHistoryController.saveHistoryPart(cityOne, cityTwo, dateFlight, journeyClass, "oneWay", adultNum, childNum,
						infantNum, customer);
				searchIdInt = savedSearchId;
			} else {
				searchIdInt = (int)searchId;
			}
			cartItem = travelerDetailsPart(searchIdInt, flightId, customer);
			ProductDetail productDetail = cartItem.getProductDetail();
			
			productService.updateDeviceInfo(productDetail, device, deviceInfo);
			
			return "redirect:/flight_booking" + searchIdInt + "&" + productDetail.getId() + "&" + cartItem.getId();
		} else {
			return "redirect:/";
		}
	}
	
	@GetMapping("/flight_booking{search_id}&{flight_id}&{item_id}")
	public String filghtBookingSave(@PathVariable(name = "search_id") Integer search_id, 
			@PathVariable(name = "flight_id") Integer flight_id,
			@PathVariable(name = "item_id") Integer item_id, 
			@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer, 
			@AuthenticationPrincipal CustomerOAuth2User googleLogin,
			Model model, 
			CartItem cartItem ) throws IOException {
		
		String email; 
		Customer customer; 

	    String sort = "pnr";
	    String brand = "";
	    Integer stop = 0;
	    String activeTime = "active";
	    String arrayPrice = "0,0";
	    
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByPhone(email);
			model.addAttribute("customer", customer);
			
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByPhone(email);
			model.addAttribute("customer", customer);
		}
		ProductDetail flight = flightRepo.findById(flight_id).get();
		SearchHistory search = searchRepo.findById(search_id).get();
		CartItem item = cartRepo.findById(item_id).get();
	    City cityOneFound = cityRepo.getCityByCode(search.getCityOne());
	    City cityTwoFound = cityRepo.getCityByCode(search.getCityTwo());

		if (flight.getMode().equals("Online-data")) {
        	/* Fare-rule details */
        	URL urlFarerule = new URL("https://tboapi.travelboutiqueonline.com/AirAPI_V10/AirService.svc/rest/FareRule");
            // Open a connection
            HttpURLConnection connectionFarerule = (HttpURLConnection) urlFarerule.openConnection();
            
            StringBuilder responseBodyFarerule = new StringBuilder();
            
            onlineFlightService.apiOnlineFarerule_quote(connectionFarerule, responseBodyFarerule, traceId, flight.getResultIndex());

        	JSONObject jsonObjFarerules = new JSONObject(responseBodyFarerule.toString());
        	System.out.println(jsonObjFarerules);
            logService.generateLog(jsonObjFarerules.toString());
        	try {
				JSONArray jsonObjFareruleResponse = jsonObjFarerules.getJSONObject("Response").getJSONArray("FareRules");
				JSONObject jsonObjFarerule = jsonObjFareruleResponse.getJSONObject(0);
				String fareRuleDetail = jsonObjFarerule.get("FareRuleDetail").toString();
				
				model.addAttribute("jsonObjFarerule", fareRuleDetail);
			} catch (Exception e) {
				return "/flight_search_" + search.getId() +"_"+ sort +"_"+ brand +"_"+ stop +"_"+ arrayPrice +"_"+ activeTime;
			}
        	
		} 
		
//		else if (flight.getMode().equals("AirIQ")) {
//			
//		}
		
		model.addAttribute("listProductDetailsOnline", listProductDetailsOnline);
		
		List<TravellerDetail> travelers = productService.findTraveller(flight, item);
		for (TravellerDetail travellerDetail : travelers) {
			model.addAttribute("travellerDetail", travellerDetail);
		}
		model.addAttribute("travelers", travelers);
		

		int[] adultList = new int[search.getAdultNum()];
		int[] childList = new int[search.getChildNum()];
		int[] infantList = new int[search.getInfantNum()];
		int[] list= new int[search.getPassengerNum()];
		
		model.addAttribute("list", list);
		model.addAttribute("adultList", adultList);
		model.addAttribute("childList", childList);
		model.addAttribute("infantList", infantList);
		model.addAttribute("item", item);
		model.addAttribute("search", search);
		model.addAttribute("flight", flight);
		model.addAttribute("cityOneName", cityOneFound.getCityName());
		model.addAttribute("cityTwoName", cityTwoFound.getCityName());
		model.addAttribute("timeRemainingPro", timeRemainingProOne);
		
		return "flight/booking/flight_booking";
	}

	public CartItem travelerDetailsPart(Integer searchId, Integer flightId, Customer customer) {
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
			
			return LastItem;
			
		} catch (Exception e) {
			for (ProductDetail flightOnline : listProductDetailsOnline) {
				if (flightOnline.getId() == flightId) {
					if (flightOnline.getMode() == "Online-data") {
						flight = new ProductDetail(flightOnline.getPnr(), flightOnline.getTotalSeats(), flightOnline.getUploadSeats(), flightOnline.getFlightNum(), flightOnline.getDate(), 
								flightOnline.getDepTime(), flightOnline.getArrTime(), flightOnline.getPriceADT(), flightOnline.getPriceINF(), flightOnline.getMarkupADT(), flightOnline.getMarkupINF(), 
								flightOnline.getCityOne(), flightOnline.getCityTwo(), flightOnline.isInStock(), flightOnline.isEnabled(), flightOnline.getStopNum(), flightOnline.getDuration(), 
								flightOnline.getBrand(), flightOnline.getDepTimeInteger(), flightOnline.getArrTimeInteger(), flightOnline.getTraceId(), flightOnline.getResultIndex(), flightOnline.getAirlineRemarks(), 
								flightOnline.getMode(), flightOnline.getJourneyClass(), flightOnline.getTerminalDep(), flightOnline.getTerminalArr(), flightOnline.getBaggage(), flightOnline.getCabinBaggage(), 
								flightOnline.getDevice(), flightOnline.getDeviceDescription(), null, flightOnline.getCraftType());
						
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
						
						return LastItemOnline;
					} else if (flightOnline.getMode() == "AirIQ") {

						flight = new ProductDetail(flightOnline.getPnr(), flightOnline.getTotalSeats(), flightOnline.getUploadSeats(), flightOnline.getFlightNum(), flightOnline.getDate(), 
								flightOnline.getDepTime(), flightOnline.getArrTime(), flightOnline.getPriceADT(), flightOnline.getPriceINF(), flightOnline.getMarkupADT(), flightOnline.getMarkupINF(), 
								flightOnline.getCityOne(), flightOnline.getCityTwo(), flightOnline.isInStock(), flightOnline.isEnabled(), flightOnline.getStopNum(), flightOnline.getDuration(), 
								flightOnline.getBrand(), flightOnline.getDepTimeInteger(), flightOnline.getArrTimeInteger(), flightOnline.getTraceId(), flightOnline.getResultIndex(), flightOnline.getAirlineRemarks(), 
								flightOnline.getMode(), flightOnline.getJourneyClass(), flightOnline.getTerminalDep(), flightOnline.getTerminalArr(), flightOnline.getBaggage(), flightOnline.getCabinBaggage(), 
								flightOnline.getDevice(), flightOnline.getDeviceDescription(), null, flightOnline.getCraftType());
						
						flight.setId(0);
						ProductDetail newFlightOnlineSaved = productDetailCrudRepo.save(flight);
						
						String modeOnline = "AirIQ";
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
						
						return LastItemOnline;
						
					}
					
				}
			}
		}
		
		return null;
	}

	public void fareQuoteSSRMethod(Model model, ProductDetail flight) throws MalformedURLException, IOException {
		if (flight.getMode().equals("Online-data")) {
        	
			/* Fare-quote details */
        	URL urlFarequote = new URL("https://tboapi.travelboutiqueonline.com/AirAPI_V10/AirService.svc/rest/FareQuote");
        	
			/* Fare-quote details Test Cred*/
//        	URL urlFarequote = new URL("http://api.tektravels.com/BookingEngineService_Air/AirService.svc/rest/FareQuote");
            // Open a connection
            HttpURLConnection connectionFarequote = (HttpURLConnection) urlFarequote.openConnection();
            
            StringBuilder responseBodyFarequote = new StringBuilder();
            
        	onlineFlightService.apiOnlineFarerule_quote(connectionFarequote, responseBodyFarequote, traceId, flight.getResultIndex());
        	
        	JSONObject jsonObjFareQuotes = new JSONObject(responseBodyFarequote.toString()); 
        	System.out.println(jsonObjFareQuotes);
            logService.generateLog(jsonObjFareQuotes.toString());
        	
        	JSONObject jsonResult = jsonObjFareQuotes.getJSONObject("Response").getJSONObject("Results");
        	JSONArray jsonObjSegment = jsonResult.getJSONArray("Segments").getJSONArray(0);
        	JSONObject mainObjSegment = jsonObjSegment.getJSONObject(0);
    		JSONObject mainObjOrigin = mainObjSegment.getJSONObject("Origin");
    		JSONObject mainObjDestination = mainObjSegment.getJSONObject("Destination");
    		JSONObject mainObjAirline = mainObjSegment.getJSONObject("Airline");
    		JSONArray jsonObjFareBreakdown = jsonResult.getJSONArray("FareBreakdown");
    		JSONObject mainObjFare = jsonResult.getJSONObject("Fare");
    		
    		String passengerTypeInner = "";
    		for (int i = 0; i < jsonObjFareBreakdown.length(); i++) {
    				mainObjFareBreakdown = jsonObjFareBreakdown.getJSONObject(i);
    				passengerTypeInner = mainObjFareBreakdown.get("PassengerType").toString();
				if (passengerTypeInner.equals("2")) {
	    			mainObjFareBreakdownChild = jsonObjFareBreakdown.getJSONObject(i);
	        		basefareTravelerChild = mainObjFareBreakdownChild.get("BaseFare").toString();
	        		taxTravelerChild = mainObjFareBreakdownChild.get("Tax").toString();
	        		passengerTypeChild = mainObjFareBreakdownChild.get("PassengerType").toString();
				} else if (passengerTypeInner.equals("3")) {
					mainObjFareBreakdownInfant = jsonObjFareBreakdown.getJSONObject(i);
					basefareTravelerInfant = mainObjFareBreakdownInfant.get("BaseFare").toString();
					taxTravelerInfant = mainObjFareBreakdownInfant.get("Tax").toString();
					passengerTypeInfant = mainObjFareBreakdownInfant.get("PassengerType").toString();
				} else {
					mainObjFareBreakdownAdult = jsonObjFareBreakdown.getJSONObject(i);
					basefareTravelerAdult = mainObjFareBreakdownAdult.get("BaseFare").toString();
		    		taxTravelerAdult = mainObjFareBreakdownAdult.get("Tax").toString();
		    		passengerTypeAdult = mainObjFareBreakdownAdult.get("PassengerType").toString();
				}
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
    		discount = mainObjFare.get("Discount").toString();
    		otherCharges = mainObjFare.get("OtherCharges").toString();
    		publishedFare = mainObjFare.get("PublishedFare").toString();
    		offeredFare = mainObjFare.get("OfferedFare").toString();
    		tdsOnCommission = mainObjFare.get("TdsOnCommission").toString();
    		tdsOnIncentive = mainObjFare.get("TdsOnIncentive").toString();
    		tdsOnPLB = mainObjFare.get("TdsOnPLB").toString();
    		serviceFee = mainObjFare.get("ServiceFee").toString();
    		
    		airportCodeOrigin = mainObjOrigin.getJSONObject("Airport").get("AirportCode").toString();
    		airportCodeDestination = mainObjDestination.getJSONObject("Airport").get("AirportCode").toString();
    		craftType = mainObjSegment.get("Craft").toString();
//    		String resultIndex = jsonResult.get("ResultIndex").toString();

//        	model.addAttribute("jsonObjFare_quote", jsonObjFareQuotes);
    		

    		/* SSR details */
        	URL urlSSR = new URL("https://tboapi.travelboutiqueonline.com/AirAPI_V10/AirService.svc/rest/SSR");
        	
    		/* SSR details Test Cred*/
//        	URL urlSSR = new URL("http://api.tektravels.com/BookingEngineService_Air/AirService.svc/rest/SSR");
            // Open a connection
            HttpURLConnection connectionSSR = (HttpURLConnection) urlSSR.openConnection();
            
            StringBuilder responseBodySSR = new StringBuilder();
            
        	onlineFlightService.apiOnlineFarerule_quote(connectionSSR, responseBodySSR, traceId, flight.getResultIndex());
        	
        	JSONObject jsonObjSSR = new JSONObject(responseBodySSR.toString()); 
        	System.out.println(jsonObjSSR);
            logService.generateLog(jsonObjSSR.toString());
        	
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
					
					lcc = true;

					productService.methodLCC(flight, lcc);
				}
			} catch (JSONException e) {
				try {
					JSONArray jsonResultArrayMeal = jsonObjSSR.getJSONObject("Response").getJSONArray("Meal"); 
					MealsOnline[] mealsOnline = new MealsOnline[100];
					for (int i = 0; i < jsonResultArrayMeal.length(); i++) {
						JSONObject jsonObjectMeals = jsonResultArrayMeal.getJSONObject(i);
						String mealPrice = "0";
						String mealAirlineDescription = jsonObjectMeals.get("Description").toString();
						String mealCode = jsonObjectMeals.get("Code").toString();
						String mealQuantity = "1";
						
						mealsOnline[i] = new MealsOnline(i, mealAirlineDescription, mealPrice, mealCode, mealQuantity);
						mealsOnlineList.add(mealsOnline[i]);
						
						lcc = false;

						productService.methodLCC(flight, lcc);
					}
				} catch (Exception e1) {
					MealsOnline mealsOnline = new MealsOnline(1, "No meal", "0", "NoMeal", "0");
					mealsOnlineList.add(mealsOnline);
					lcc = true;

					productService.methodLCC(flight, lcc);
				}
			} catch (Exception e) {
				MealsOnline mealsOnline = new MealsOnline(1, "No meal", "0", "NoMeal", "0");
				mealsOnlineList.add(mealsOnline);
				lcc = true;

				productService.methodLCC(flight, lcc);
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
			
			} catch (JSONException e) {
				JSONArray jsonArraySeats = jsonObjSSR.getJSONObject("Response").getJSONArray("SeatPreference"); 
	        	SeatsOnline[] seatsOnline = new SeatsOnline[500];
	        	for (int i = 0; i < jsonArraySeats.length(); i++) {
	        		JSONObject jsonObjectSeats = jsonArraySeats.getJSONObject(i);
	        		String code = jsonObjectSeats.get("Code").toString();
	        		String description = jsonObjectSeats.get("Description").toString();
	        		
	        		seatsOnline[i] = new SeatsOnline(i, "0", 0, 0, 0, "0", code, 0, description, "0");
	        		seatsOnlineList.add(seatsOnline[i]);
        			
					lcc = false;
	        	}
			} catch (Exception e) {
				SeatsOnline seatsOnline = new SeatsOnline(1, "0", 0, 0, 0, "0", "NoSeat", 0, "0", "0");
				seatsOnlineList.add(seatsOnline);
			}

    		model.addAttribute("seatsOnlineList", seatsOnlineList);
    		model.addAttribute("mealsOnlineList", mealsOnlineList);
    		model.addAttribute("baggageOnlineList", baggageOnlineList);
        	
		} else if (flight.getTraceId().equals("offline")) {
			
			BaggageOnline baggageOnline = new BaggageOnline(1, "0", "NoBaggage", "0");
			baggageOnlineList.add(baggageOnline);

			MealsOnline mealsOnline = new MealsOnline(1, "No meal", "0", "NoMeal", "0");
			mealsOnlineList.add(mealsOnline);

			SeatsOnline seatsOnline = new SeatsOnline(1, "0", 0, 0, 0, "0", "NoSeat", 0, "0", "0");
			seatsOnlineList.add(seatsOnline);

    		model.addAttribute("seatsOnlineList", seatsOnlineList);
    		model.addAttribute("mealsOnlineList", mealsOnlineList);
    		model.addAttribute("baggageOnlineList", baggageOnlineList);
			System.out.println("Offline offline offline");
			
		}

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

	
	////Flight return segment

	@PostMapping("/flight_booking_return_save")
	public String filghtBookingReturnSave(@RequestParam(name = "search_id") float searchId, 
			@RequestParam(name = "timeRemaining") Integer timeRemaining,
			@RequestParam(name = "flightOne_id") Integer flightOneId, 
			@RequestParam(name = "flightTwo_id") Integer flightTwoId, 
			@RequestParam(name = "adultNum") Integer adultNum,
			@RequestParam(name = "childNum") Integer childNum,
			@RequestParam(name = "infantNum") Integer infantNum,
			@RequestParam(name = "cityOne") String cityOne,
			@RequestParam(name = "cityTwo") String cityTwo,
			@RequestParam(name = "journeyClass") String journeyClass,
			@RequestParam(name = "date") String date,
			@RequestParam(name = "returnDate") String retunDate,
			@RequestParam(name = "device") String device,
			@RequestParam(name = "deviceInfo") String deviceInfo,
			@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer, 
			@AuthenticationPrincipal CustomerOAuth2User googleLogin, Model model) throws ParseException {
		String email; 
		Customer customer; 
		Integer searchIdInt = 0;
		Date dateFlight = new SimpleDateFormat("yyyy-MM-dd").parse(date);
		Date returnDateFlight = new SimpleDateFormat("yyyy-MM-dd").parse(retunDate);
		timeRemainingPro = timeRemaining;
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByPhone(email);
			if (searchId == 1.5f) {
				Integer savedSearchId = sHistoryController.saveHistoryReturnPart(cityOne, cityTwo, dateFlight, returnDateFlight, journeyClass, "twoWay", adultNum, childNum, infantNum, customer); 
				searchIdInt = savedSearchId;
			} else {
				searchIdInt = (int)searchId;
			}
			CartItem cartItemOne = travelerDetailsPart(searchIdInt, flightOneId, customer);
			ProductDetail productDetailOne = cartItemOne.getProductDetail();
			productService.updateDeviceInfo(productDetailOne, device, deviceInfo);
			
			CartItem cartItemTwo = travelerDetailsPartReturn(searchIdInt, flightTwoId, customer);
			ProductDetail productDetailTwo = cartItemTwo.getProductDetail();
			productService.updateDeviceInfo(productDetailTwo, device, deviceInfo);
			
			return "redirect:/flight_return_booking" + searchIdInt + "&" + productDetailOne.getId() + "&" + cartItemOne.getId() + "&" + productDetailTwo.getId() + "&" + cartItemTwo.getId();
			
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByPhone(email);
			if (searchId == 1.5f) {
				Integer savedSearchId = sHistoryController.saveHistoryReturnPart(cityOne, cityTwo, dateFlight, returnDateFlight, journeyClass, "twoWay", adultNum, childNum, infantNum, customer);
				searchIdInt = savedSearchId;
			} else {
				searchIdInt = (int)searchId;
			}
			CartItem cartItemOne = travelerDetailsPart(searchIdInt, flightOneId, customer);
			ProductDetail productDetailOne = cartItemOne.getProductDetail();
			productService.updateDeviceInfo(productDetailOne, device, deviceInfo);
			
			CartItem cartItemTwo = travelerDetailsPartReturn(searchIdInt, flightTwoId, customer);
			ProductDetail productDetailTwo = cartItemTwo.getProductDetail();
			productService.updateDeviceInfo(productDetailTwo, device, deviceInfo);
			
			return "redirect:/flight_return_booking" + searchIdInt + "&" + productDetailOne.getId() + "&" + cartItemOne.getId() + "&" + productDetailTwo.getId() + "&" + cartItemTwo.getId();
		} else {
			return "redirect:/";
		}
	}
	
	public CartItem travelerDetailsPartReturn(Integer searchId, Integer flightId, Customer customer) {
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
			
			return LastItem;
			
		} catch (Exception e) {
			for (ProductDetail flightOnline : listProductDetailsOnlineReturn) {
				if (flightOnline.getId() == flightId) {
					flight = flightOnline;
					ProductDetail newFlightOnlineSaved = productDetailCrudRepo.save(flight);
					System.out.println(newFlightOnlineSaved.getPriceADT());
					System.out.println(newFlightOnlineSaved.getId());
					System.out.println(flightId);
					
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
					
					return LastItemOnline;
				}
			}
		}
		
		return null;
	}
	
	@GetMapping("/flight_return_booking{search_id}&{flightOne_id}&{itemOne_id}&{flightTwo_id}&{itemTwo_id}")
	public String filghtBookingSaveReturn(@PathVariable(name = "search_id") Integer search_id, 
			@PathVariable(name = "flightOne_id") Integer flightOneId,
			@PathVariable(name = "itemOne_id") Integer itemOneId, 
			@PathVariable(name = "flightTwo_id") Integer flightTwoId,
			@PathVariable(name = "itemTwo_id") Integer itemTwoId, 
			@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer, 
			@AuthenticationPrincipal CustomerOAuth2User googleLogin,
			Model model, 
			CartItem cartItem ) throws IOException {
		
		String email; 
		Customer customer; 
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByPhone(email);
			model.addAttribute("customer", customer);
			
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByPhone(email);
			model.addAttribute("customer", customer);
		}
		ProductDetail flightOne = flightRepo.findById(flightOneId).get();
		ProductDetail flightTwo = flightRepo.findById(flightTwoId).get();
		SearchHistory search = searchRepo.findById(search_id).get();
		CartItem itemOne = cartRepo.findById(itemOneId).get();
		CartItem itemTwo = cartRepo.findById(itemTwoId).get();

		if (!flightOne.getTraceId().equals("offline")) {
        	/* Fare-rule details */
        	URL urlFarerule = new URL("https://tboapi.travelboutiqueonline.com/AirAPI_V10/AirService.svc/rest/FareRule");
            // Open a connection
            HttpURLConnection connectionFarerule = (HttpURLConnection) urlFarerule.openConnection();
            
            StringBuilder responseBodyFarerule = new StringBuilder();
            
        	int responseCode = onlineFlightService.apiOnlineFarerule_quote(connectionFarerule, responseBodyFarerule, traceId, flightOne.getResultIndex());
        	if (responseCode != HttpURLConnection.HTTP_OK) {
    			if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP
    				|| responseCode == HttpURLConnection.HTTP_MOVED_PERM
    					|| responseCode == HttpURLConnection.HTTP_SEE_OTHER)
    				return "redirect:/";
    		}
  
        	
        	JSONObject jsonObjFarerules = new JSONObject(responseBodyFarerule.toString());
        	System.out.println(jsonObjFarerules);
            logService.generateLog(jsonObjFarerules.toString());
        	try {
				JSONArray jsonObjFareruleResponse = jsonObjFarerules.getJSONObject("Response").getJSONArray("FareRules");
				JSONObject jsonObjFarerule = jsonObjFareruleResponse.getJSONObject(0);
				String fareRuleDetail = jsonObjFarerule.get("FareRuleDetail").toString();
				
				model.addAttribute("jsonObjFareruleOne", fareRuleDetail);
			} catch (Exception e) {
				return "redirect:/";
			}
        	
		} 
		
		if (!flightTwo.getTraceId().equals("offline")) {
        	/* Fare-rule details */
        	URL urlFarerule = new URL("https://tboapi.travelboutiqueonline.com/AirAPI_V10/AirService.svc/rest/FareRule");
            // Open a connection
            HttpURLConnection connectionFarerule = (HttpURLConnection) urlFarerule.openConnection();
            
            StringBuilder responseBodyFarerule = new StringBuilder();
            
        	int responseCode = onlineFlightService.apiOnlineFarerule_quote(connectionFarerule, responseBodyFarerule, traceId, flightTwo.getResultIndex());
        	if (responseCode != HttpURLConnection.HTTP_OK) {
    			if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP
    				|| responseCode == HttpURLConnection.HTTP_MOVED_PERM
    					|| responseCode == HttpURLConnection.HTTP_SEE_OTHER)
    				return "redirect:/";
    		}
  
        	
        	JSONObject jsonObjFarerules = new JSONObject(responseBodyFarerule.toString());
        	System.out.println(jsonObjFarerules);
            logService.generateLog(jsonObjFarerules.toString());
        	try {
				JSONArray jsonObjFareruleResponse = jsonObjFarerules.getJSONObject("Response").getJSONArray("FareRules");
				JSONObject jsonObjFarerule = jsonObjFareruleResponse.getJSONObject(0);
				String fareRuleDetail = jsonObjFarerule.get("FareRuleDetail").toString();
				
				model.addAttribute("jsonObjFareruleTwo", fareRuleDetail);
			} catch (Exception e) {
				return "redirect:/";
			}
        	
		} 
		
		model.addAttribute("listProductDetailsOnline", listProductDetailsOnline);
		
		List<TravellerDetail> travelers = productService.findTraveller(flightOne, itemOne);
		for (TravellerDetail travellerDetail : travelers) {
			model.addAttribute("travellerDetail", travellerDetail);
		}
		model.addAttribute("travelers", travelers);
		
		Double totalPrice = itemOne.getTotalPrice() + itemTwo.getTotalPrice();
		
		int[] adultList = new int[search.getAdultNum()];
		int[] childList = new int[search.getChildNum()];
		int[] infantList = new int[search.getInfantNum()];
		int[] list= new int[search.getPassengerNum()];
		
		model.addAttribute("adultList", adultList);
		model.addAttribute("childList", childList);
		model.addAttribute("infantList", infantList);
		model.addAttribute("list", list);
		model.addAttribute("totalPrice", totalPrice);
		model.addAttribute("itemOne", itemOne);
		model.addAttribute("itemTwo", itemTwo);
		model.addAttribute("search", search);
		model.addAttribute("flightOne", flightOne);
		model.addAttribute("flightTwo", flightTwo);
		model.addAttribute("timeRemainingPro", timeRemainingPro);
		
		return "flight/booking_return/flight_booking_return";
	}
	
	@PostMapping("/traveller_details_return")
	public String saveTravellerDetailsReturn(@RequestParam(name = "search_id") Integer searchId, 
			@RequestParam(name = "timeRemaining") Integer timeRemaining,
			@RequestParam(name = "flightOne_id") Integer flightOneId,
			@RequestParam(name = "itemOne_id") Integer itemOne_id,
			@RequestParam(name = "flightTwo_id") Integer flightTwoId,
			@RequestParam(name = "itemTwo_id") Integer itemTwo_id,
			@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer,  
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
				customer = customerService.getByPhone(email);
				model.addAttribute("customer", customer);
				
			} else if (googleLogin != null) {
				email = googleLogin.getEmail();
				customer = customerService.getByPhone(email);
				model.addAttribute("customer", customer);
			}
			timeRemainingPro = timeRemaining;
			
			ProductDetail flightOne = flightRepo.findById(flightOneId).get();
			ProductDetail flightTwo = flightRepo.findById(flightTwoId).get();
			SearchHistory search = searchRepo.findById(searchId).get();
			CartItem itemOne = cartRepo.findById(itemOne_id).get();
			CartItem itemTwo = cartRepo.findById(itemTwo_id).get();
			
			cartService.updateCartItem(itemOne, cartItem.getEmail(), cartItem.getPhoneNum(), search.getPassengerNum(), false); 
			cartService.updateCartItem(itemTwo, cartItem.getEmail(), cartItem.getPhoneNum(), search.getPassengerNum(), false); 
			
			
				
			for (int i = 0; i < search.getPassengerNum(); i++) {
				ProductDetail travelers1 = travelerSaveMethod(model, salutation, firstName, lastName, dob, paxType, flightOne, itemOne, i);
				model.addAttribute("flightDetailsTwo", travelers1);
				
				ProductDetail travelers2 = travelerSaveMethod(model, salutation, firstName, lastName, dob, paxType, flightTwo, itemTwo, i);
				model.addAttribute("flightDetailsTwo", travelers2);
				
			}
			
			model.addAttribute("item", itemOne);
			model.addAttribute("search", search);
			model.addAttribute("flight", flightOne);
			model.addAttribute("itemTwo", itemTwo);
			model.addAttribute("flightTwo", flightTwo);

			return  "redirect:/flight_traveler_return_details" + searchId + "&" + flightOneId + "&" + itemOne_id + "&" + flightTwoId + "&" + itemTwo_id;
		} catch (Exception e) {
			return  "redirect:/flight_traveler_return_details" + searchId + "&" + flightOneId + "&" + itemOne_id + "&" + flightTwoId + "&" + itemTwo_id;
		}
	}

	private ProductDetail travelerSaveMethod(Model model, String[] salutation, String[] firstName, String[] lastName, Date[] dob,
			String[] paxType, ProductDetail flightOne, CartItem itemOne, int i) {
		Date newDate = new Date();
		Calendar c = Calendar.getInstance();
		c.add(Calendar.YEAR, 2);
		
		newDate = c.getTime();
		
		ProductSaveHelper.setTravellerDetailReturn(salutation[i], firstName[i], lastName[i], dob[i], flightOne, itemOne, paxType[i], flightOne.getBaggage(), flightOne.getCabinBaggage(), i, "KJHHJKHKJH", newDate);
		productService.saveFlightPassengerDetails(flightOne);
		ProductDetail flightDetailsOne = productService.saveFlightPassengerDetails(flightOne);
		return flightDetailsOne;
	}
	
	@PostMapping("/traveller_detail_return_edit")
	public String updateTravellerDetailReturn(@RequestParam(name = "traveler_id") Integer traveler_id,
			@RequestParam(name = "search_id") Integer searchId, 
			@RequestParam(name = "flightOne") Integer flightOne,
			@RequestParam(name = "itemOne") Integer itemOne,
			@RequestParam(name = "flightTwo") Integer flightTwo,
			@RequestParam(name = "itemTwo") Integer itemTwo,
			@RequestParam(name = "saveSalutation", required = false) String salutation, 
			@RequestParam(name = "saveFirstName", required = false) String firstName,  
			@RequestParam(name = "saveLastName", required = false) String lastName, 
			@RequestParam(name = "savedob", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date dob) {
		TravellerDetail traveler = travelerRepo.findById(traveler_id).get();
		ProductDetail productDetailTwo = flightRepo.findById(flightTwo).get();
		CartItem cartItemTwo = cartRepo.findById(itemOne).get();

		cartService.updateTraveler(traveler, salutation, firstName, lastName, dob);
		
		List<TravellerDetail> travelersOne = productService.findTraveller(productDetailTwo, cartItemTwo);
		for (TravellerDetail travellerDetail : travelersOne) {
			if (traveler.getTravelerCountSerial() == travellerDetail.getTravelerCountSerial()) {
				cartService.updateTraveler(travellerDetail, salutation, firstName, lastName, dob);
			}
		}
		
		return  "redirect:/flight_traveler_return_details" + searchId + "&" + flightOne + "&" + itemOne + "&" + flightTwo + "&" + itemTwo;
	}
	
	@GetMapping("/flight_traveler_return_details{search_id}&{flightOne_id}&{itemOne_id}&{flightTwo_id}&{itemTwo_id}")
	public String filghtTravelerDetailsReturnSave(@PathVariable(name = "search_id") Integer search_id, 
			@PathVariable(name = "flightOne_id") Integer flightOne_id, 
			@PathVariable(name = "itemOne_id") Integer itemOne_id, 
			@PathVariable(name = "flightTwo_id") Integer flightTwo_id, 
			@PathVariable(name = "itemTwo_id") Integer itemTwo_id, 
			@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer, 
			@AuthenticationPrincipal CustomerOAuth2User googleLogin,
			Model model, CartItem cartItem) throws IOException {
		String email; 
		Customer customer; 
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByPhone(email);
			model.addAttribute("customer", customer);
			
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByPhone(email);
			model.addAttribute("customer", customer);
		}

		SearchHistory search = searchRepo.findById(search_id).get();
		ProductDetail flightOne = flightRepo.findById(flightOne_id).get();
		CartItem itemOne = cartRepo.findById(itemOne_id).get();
		ProductDetail flightTwo = flightRepo.findById(flightTwo_id).get();
		CartItem itemTwo = cartRepo.findById(itemTwo_id).get();
		
		List<TravellerDetail> travelersOne = productService.findTraveller(flightOne, itemOne);

		List<TravellerDetail> travelersTwo = productService.findTraveller(flightTwo, itemTwo);
		Double totalPayment = itemOne.getTotalPrice() + itemTwo.getTotalPrice();
		CheckoutInfo checkoutInfo = new CheckoutInfo();
		CheckoutInfo checkoutInfoTwo = new CheckoutInfo();
		checkoutInfo.setPaymentTotal(itemOne.getTotalPrice());
		checkoutInfoTwo.setPaymentTotal(itemTwo.getTotalPrice());
																
		try {
			fareQuoteSSRMethodReturn(model, flightOne);
			
			fareQuoteSSRMethodReturnTwo(model, flightTwo);
			
		}  catch (Exception e) {
			return "redirect:/flight_return_booking" + search.getId() + "&" + flightOne.getId() + "&" + itemOne.getId() + "&" + flightTwo.getId() + "&" + itemTwo.getId();
		}

		model.addAttribute("checkoutInfo", checkoutInfo);
		model.addAttribute("checkoutInfoTwo", checkoutInfoTwo);
		model.addAttribute("travelersOne", travelersOne);
		model.addAttribute("travelersTwo", travelersTwo);
		model.addAttribute("totalPayment", totalPayment);
		model.addAttribute("itemOne", itemOne);
		model.addAttribute("itemTwo", itemTwo);
		model.addAttribute("search", search);
		model.addAttribute("flightOne", flightOne);
		model.addAttribute("flightTwo", flightTwo);
		model.addAttribute("falied", "Please provide a correct coupon code!!!");
		model.addAttribute("success", "The coupon is verified!");
		model.addAttribute("seatsOnlineList", seatsOnlineList);
		model.addAttribute("mealsOnlineList", mealsOnlineList);
		model.addAttribute("baggageOnlineList", baggageOnlineList);
		model.addAttribute("seatsOnlineListReturn", seatsOnlineListReturn);
		model.addAttribute("mealsOnlineListReturn", mealsOnlineListReturn);
		model.addAttribute("baggageOnlineListReturn", baggageOnlineListReturn);
		model.addAttribute("timeRemainingPro", timeRemainingPro);
		
		return "flight/booking_return/flight_traveler_details_return";
	}
	
	@PostMapping("/cartItem_return_edit")
	public String updateCartItemReturn(
			@RequestParam(name = "search_id") Integer searchId, 
			@RequestParam(name = "flightOne") Integer flightOne,
			@RequestParam(name = "itemOne") Integer itemOne,
			@RequestParam(name = "flightTwo") Integer flightTwo,
			@RequestParam(name = "itemTwo") Integer itemTwo, 
			@RequestParam(name = "email") String email,
			@RequestParam(name = "itemTwo") BigInteger phoneNum) {

		CartItem cartItemOne = cartRepo.findById(itemOne).get();
		CartItem cartItemTwo = cartRepo.findById(itemTwo).get();
		SearchHistory search = searchRepo.findById(searchId).get();
		
		cartService.updateCartItem(cartItemOne, email, phoneNum, search.getPassengerNum(), false); 
		cartService.updateCartItem(cartItemTwo, email, phoneNum, search.getPassengerNum(), false); 
		
		return  "redirect:/flight_traveler_return_details" + searchId + "&" + flightOne + "&" + itemOne + "&" + flightTwo + "&" + itemTwo;
		
	}

	public void fareQuoteSSRMethodReturn(Model model, ProductDetail flight) throws MalformedURLException, IOException {
		if (!flight.getTraceId().equals("offline")) {
        	
			/* Fare-quote details */
        	URL urlFarequote = new URL("https://tboapi.travelboutiqueonline.com/AirAPI_V10/AirService.svc/rest/FareQuote");
            // Open a connection
            HttpURLConnection connectionFarequote = (HttpURLConnection) urlFarequote.openConnection();
            
            StringBuilder responseBodyFarequote = new StringBuilder();
            
        	onlineFlightService.apiOnlineFarerule_quote(connectionFarequote, responseBodyFarequote, traceId, flight.getResultIndex());
        	
        	JSONObject jsonObjFareQuotes = new JSONObject(responseBodyFarequote.toString()); 
        	System.out.println(jsonObjFareQuotes);
            logService.generateLog(jsonObjFareQuotes.toString());
        	
        	JSONObject jsonResult = jsonObjFareQuotes.getJSONObject("Response").getJSONObject("Results");
        	JSONArray jsonObjSegment = jsonResult.getJSONArray("Segments").getJSONArray(0);
        	JSONObject mainObjSegment = jsonObjSegment.getJSONObject(0);
    		JSONObject mainObjOrigin = mainObjSegment.getJSONObject("Origin");
    		JSONObject mainObjDestination = mainObjSegment.getJSONObject("Destination");
    		JSONObject mainObjAirline = mainObjSegment.getJSONObject("Airline");
    		JSONArray jsonObjFareBreakdown = jsonResult.getJSONArray("FareBreakdown");
    		JSONObject mainObjFare = jsonResult.getJSONObject("Fare");
    		
    		String passengerTypeInner = "";
    		for (int i = 0; i < jsonObjFareBreakdown.length(); i++) {
    				mainObjFareBreakdown = jsonObjFareBreakdown.getJSONObject(i);
    				passengerTypeInner = mainObjFareBreakdown.get("PassengerType").toString();
				if (passengerTypeInner.equals("2")) {
	    			mainObjFareBreakdownChild = jsonObjFareBreakdown.getJSONObject(i);
	        		basefareTravelerChild = mainObjFareBreakdownChild.get("BaseFare").toString();
	        		taxTravelerChild = mainObjFareBreakdownChild.get("Tax").toString();
	        		passengerTypeChild = mainObjFareBreakdownChild.get("PassengerType").toString();
				} else if (passengerTypeInner.equals("3")) {
					mainObjFareBreakdownInfant = jsonObjFareBreakdown.getJSONObject(i);
					basefareTravelerInfant = mainObjFareBreakdownInfant.get("BaseFare").toString();
					taxTravelerInfant = mainObjFareBreakdownInfant.get("Tax").toString();
					passengerTypeInfant = mainObjFareBreakdownInfant.get("PassengerType").toString();
				} else {
					mainObjFareBreakdownAdult = jsonObjFareBreakdown.getJSONObject(i);
					basefareTravelerAdult = mainObjFareBreakdownAdult.get("BaseFare").toString();
		    		taxTravelerAdult = mainObjFareBreakdownAdult.get("Tax").toString();
		    		passengerTypeAdult = mainObjFareBreakdownAdult.get("PassengerType").toString();
				}
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
    		discount = mainObjFare.get("Discount").toString();
    		otherCharges = mainObjFare.get("OtherCharges").toString();
    		publishedFare = mainObjFare.get("PublishedFare").toString();
    		offeredFare = mainObjFare.get("OfferedFare").toString();
    		tdsOnCommission = mainObjFare.get("TdsOnCommission").toString();
    		tdsOnIncentive = mainObjFare.get("TdsOnIncentive").toString();
    		tdsOnPLB = mainObjFare.get("TdsOnPLB").toString();
    		serviceFee = mainObjFare.get("ServiceFee").toString();
    		
    		airportCodeOrigin = mainObjOrigin.getJSONObject("Airport").get("AirportCode").toString();
    		airportCodeDestination = mainObjDestination.getJSONObject("Airport").get("AirportCode").toString();
    		craftType = mainObjSegment.get("Craft").toString();
    		
    		/* SSR details */
        	URL urlSSR = new URL("https://tboapi.travelboutiqueonline.com/AirAPI_V10/AirService.svc/rest/SSR");
            // Open a connection
            HttpURLConnection connectionSSR = (HttpURLConnection) urlSSR.openConnection();
            
            StringBuilder responseBodySSR = new StringBuilder();
            
        	onlineFlightService.apiOnlineFarerule_quote(connectionSSR, responseBodySSR, traceId, flight.getResultIndex());
        	
        	JSONObject jsonObjSSR = new JSONObject(responseBodySSR.toString()); 
        	System.out.println(jsonObjSSR);
            logService.generateLog(jsonObjSSR.toString());
        	
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

					lccReturn = true;

					productService.methodLCC(flight, lccReturn);
				}
			} catch (JSONException e) {
				try {
					JSONArray jsonResultArrayMeal = jsonObjSSR.getJSONObject("Response").getJSONArray("Meal").getJSONArray(0); 
					MealsOnline[] mealsOnline = new MealsOnline[100];
					for (int i = 0; i < jsonResultArrayMeal.length(); i++) {
						JSONObject jsonObjectMeals = jsonResultArrayMeal.getJSONObject(i);
						String mealPrice = "0";
						String mealAirlineDescription = jsonObjectMeals.get("Description").toString();
						String mealCode = jsonObjectMeals.get("Code").toString();
						String mealQuantity = "1";
						
						mealsOnline[i] = new MealsOnline(i, mealAirlineDescription, mealPrice, mealCode, mealQuantity);
						mealsOnlineList.add(mealsOnline[i]);

						lccReturn = false;
						productService.methodLCC(flight, lccReturn);
					}
				} catch (JSONException e1) {
					MealsOnline mealsOnline = new MealsOnline(1, "No meal", "0", "NoMeal", "0");
					mealsOnlineList.add(mealsOnline);
					lccReturn = true;

					productService.methodLCC(flight, lccReturn);
				}
			} catch (Exception e) {
				MealsOnline mealsOnline = new MealsOnline(1, "No meal", "0", "NoMeal", "0");
				mealsOnlineList.add(mealsOnline);
				lccReturn = true;

				productService.methodLCC(flight, lccReturn);
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
			
			} catch (JSONException e) {
				JSONArray jsonArraySeats = jsonObjSSR.getJSONObject("Response").getJSONArray("SeatPreference").getJSONArray(0); 
	        	SeatsOnline[] seatsOnline = new SeatsOnline[500];
	        	for (int i = 0; i < jsonArraySeats.length(); i++) {
	        		JSONObject jsonObjectSeats = jsonArraySeats.getJSONObject(i);
	        		String code = jsonObjectSeats.get("Code").toString();
	        		String description = jsonObjectSeats.get("Description").toString();
	        		
	        		seatsOnline[i] = new SeatsOnline(i, "0", 0, 0, 0, "0", code, 0, description, "0");
	        		seatsOnlineList.add(seatsOnline[i]);
        			
					lccReturn = false;
	        	}
			} catch (Exception e) {
				SeatsOnline seatsOnline = new SeatsOnline(1, "0", 0, 0, 0, "0", "NoSeat", 0, "0", "0");
				seatsOnlineList.add(seatsOnline);
			}

        	
		} else if (flight.getTraceId().equals("offline")) {
			
			BaggageOnline baggageOnline = new BaggageOnline(1, "0", "NoBaggage", "0");
			baggageOnlineList.add(baggageOnline);

			MealsOnline mealsOnline = new MealsOnline(1, "No meal", "0", "NoMeal", "0");
			mealsOnlineList.add(mealsOnline);

			SeatsOnline seatsOnline = new SeatsOnline(1, "0", 0, 0, 0, "0", "NoSeat", 0, "0", "0");
			seatsOnlineList.add(seatsOnline);

			
		}

	}
	
	public void fareQuoteSSRMethodReturnTwo(Model model, ProductDetail flight) throws MalformedURLException, IOException {
		if (!flight.getTraceId().equals("offline")) {
        	
			/* Fare-quote details */
        	URL urlFarequote = new URL("https://tboapi.travelboutiqueonline.com/AirAPI_V10/AirService.svc/rest/FareQuote");
            // Open a connection
            HttpURLConnection connectionFarequote = (HttpURLConnection) urlFarequote.openConnection();
            
            StringBuilder responseBodyFarequote = new StringBuilder();
            
        	onlineFlightService.apiOnlineFarerule_quote(connectionFarequote, responseBodyFarequote, traceId, flight.getResultIndex());
        	
        	JSONObject jsonObjFareQuotes = new JSONObject(responseBodyFarequote.toString()); 
        	System.out.println(jsonObjFareQuotes);
            logService.generateLog(jsonObjFareQuotes.toString());
        	
        	JSONObject jsonResult = jsonObjFareQuotes.getJSONObject("Response").getJSONObject("Results");
        	JSONArray jsonObjSegment = jsonResult.getJSONArray("Segments").getJSONArray(0);
        	JSONObject mainObjSegment = jsonObjSegment.getJSONObject(0);
    		JSONObject mainObjOrigin = mainObjSegment.getJSONObject("Origin");
    		JSONObject mainObjDestination = mainObjSegment.getJSONObject("Destination");
    		JSONObject mainObjAirline = mainObjSegment.getJSONObject("Airline");
    		JSONArray jsonObjFareBreakdown = jsonResult.getJSONArray("FareBreakdown");
    		JSONObject mainObjFare = jsonResult.getJSONObject("Fare");
    		
    		String passengerTypeInner = "";
    		for (int i = 0; i < jsonObjFareBreakdown.length(); i++) {
    				mainObjFareBreakdown = jsonObjFareBreakdown.getJSONObject(i);
    				passengerTypeInner = mainObjFareBreakdown.get("PassengerType").toString();
				if (passengerTypeInner.equals("2")) {
	    			mainObjFareBreakdownChild = jsonObjFareBreakdown.getJSONObject(i);
	        		basefareTravelerChildReturn = mainObjFareBreakdownChild.get("BaseFare").toString();
	        		taxTravelerChildReturn = mainObjFareBreakdownChild.get("Tax").toString();
	        		passengerTypeChildReturn = mainObjFareBreakdownChild.get("PassengerType").toString();
				} else if (passengerTypeInner.equals("3")) {
					mainObjFareBreakdownInfant = jsonObjFareBreakdown.getJSONObject(i);
					basefareTravelerInfantReturn = mainObjFareBreakdownInfant.get("BaseFare").toString();
					taxTravelerInfantReturn = mainObjFareBreakdownInfant.get("Tax").toString();
					passengerTypeInfantReturn = mainObjFareBreakdownInfant.get("PassengerType").toString();
				} else {
					mainObjFareBreakdownAdult = jsonObjFareBreakdown.getJSONObject(i);
					basefareTravelerAdultReturn = mainObjFareBreakdownAdult.get("BaseFare").toString();
		    		taxTravelerAdultReturn = mainObjFareBreakdownAdult.get("Tax").toString();
		    		passengerTypeAdultReturn = mainObjFareBreakdownAdult.get("PassengerType").toString();
				}
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
    		discountReturn = mainObjFare.get("Discount").toString();
    		otherChargesReturn = mainObjFare.get("OtherCharges").toString();
    		publishedFareReturn = mainObjFare.get("PublishedFare").toString();
    		offeredFareReturn = mainObjFare.get("OfferedFare").toString();
    		tdsOnCommissionReturn = mainObjFare.get("TdsOnCommission").toString();
    		tdsOnIncentiveReturn = mainObjFare.get("TdsOnIncentive").toString();
    		tdsOnPLBReturn = mainObjFare.get("TdsOnPLB").toString();
    		serviceFeeReturn = mainObjFare.get("ServiceFee").toString();
    		
    		airportCodeOrigin = mainObjOrigin.getJSONObject("Airport").get("AirportCode").toString();
    		airportCodeDestination = mainObjDestination.getJSONObject("Airport").get("AirportCode").toString();
    		craftType = mainObjSegment.get("Craft").toString();
    		
    		/* SSR details */
        	URL urlSSR = new URL("https://tboapi.travelboutiqueonline.com/AirAPI_V10/AirService.svc/rest/SSR");
            // Open a connection
            HttpURLConnection connectionSSR = (HttpURLConnection) urlSSR.openConnection();
            
            StringBuilder responseBodySSR = new StringBuilder();
            
        	onlineFlightService.apiOnlineFarerule_quote(connectionSSR, responseBodySSR, traceId, flight.getResultIndex());
        	
        	JSONObject jsonObjSSR = new JSONObject(responseBodySSR.toString()); 
        	System.out.println(jsonObjSSR);
            logService.generateLog(jsonObjSSR.toString());
        	
        	try {
				JSONArray jsonResultArrayBaggage = jsonObjSSR.getJSONObject("Response").getJSONArray("Baggage").getJSONArray(0); 
				BaggageOnline[] baggageOnline = new BaggageOnline[100];
				for (int i = 0; i < jsonResultArrayBaggage.length(); i++) {
					
					JSONObject jsonObjectBagages = jsonResultArrayBaggage.getJSONObject(i);
					
					String baggagePrice = jsonObjectBagages.get("Price").toString();
					String baggageCode = jsonObjectBagages.get("Code").toString();
					String baggageWeight = jsonObjectBagages.get("Weight").toString();
					
					baggageOnline[i] = new BaggageOnline(i, baggagePrice, baggageCode, baggageWeight);
					baggageOnlineListReturn.add(baggageOnline[i]);
				}
			} catch (JSONException e1) {
				BaggageOnline baggageOnline = new BaggageOnline(1, "0", "NoBaggage", "0");
				baggageOnlineListReturn.add(baggageOnline);
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
					mealsOnlineListReturn.add(mealsOnline[i]);

					lccReturn = true;

					productService.methodLCC(flight, lccReturn);
				}
			} catch (JSONException e) {
				try {
					JSONArray jsonResultArrayMeal = jsonObjSSR.getJSONObject("Response").getJSONArray("Meal").getJSONArray(0); 
					MealsOnline[] mealsOnline = new MealsOnline[100];
					for (int i = 0; i < jsonResultArrayMeal.length(); i++) {
						JSONObject jsonObjectMeals = jsonResultArrayMeal.getJSONObject(i);
						String mealPrice = "0";
						String mealAirlineDescription = jsonObjectMeals.get("Description").toString();
						String mealCode = jsonObjectMeals.get("Code").toString();
						String mealQuantity = "1";
						
						mealsOnline[i] = new MealsOnline(i, mealAirlineDescription, mealPrice, mealCode, mealQuantity);
						mealsOnlineListReturn.add(mealsOnline[i]);
						
						lccReturn = false;

						productService.methodLCC(flight, lccReturn);
					}
				} catch (JSONException e1) {
					MealsOnline mealsOnline = new MealsOnline(1, "No meal", "0", "NoMeal", "0");
					mealsOnlineListReturn.add(mealsOnline);
					lccReturn = true;

					productService.methodLCC(flight, lccReturn);
				}
			} catch (Exception e) {
				MealsOnline mealsOnline = new MealsOnline(1, "No meal", "0", "NoMeal", "0");
				mealsOnlineListReturn.add(mealsOnline);
				lccReturn = true;

				productService.methodLCC(flight, lccReturn);
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
	        			seatsOnlineListReturn.add(seatsOnline[serialNo]);
					}
				}
			
			} catch (JSONException e) {
				JSONArray jsonArraySeats = jsonObjSSR.getJSONObject("Response").getJSONArray("SeatPreference").getJSONArray(0); 
	        	SeatsOnline[] seatsOnline = new SeatsOnline[500];
	        	for (int i = 0; i < jsonArraySeats.length(); i++) {
	        		JSONObject jsonObjectSeats = jsonArraySeats.getJSONObject(i);
	        		String code = jsonObjectSeats.get("Code").toString();
	        		String description = jsonObjectSeats.get("Description").toString();
	        		
	        		seatsOnline[i] = new SeatsOnline(i, "0", 0, 0, 0, "0", code, 0, description, "0");
	        		seatsOnlineListReturn.add(seatsOnline[i]);
        			
					lccReturn = false;
	        	}
			} catch (Exception e) {
				SeatsOnline seatsOnline = new SeatsOnline(1, "0", 0, 0, 0, "0", "NoSeat", 0, "0", "0");
				seatsOnlineListReturn.add(seatsOnline);
			}

        	
		} else if (flight.getTraceId().equals("offline")) {
			
			BaggageOnline baggageOnline = new BaggageOnline(1, "0", "NoBaggage", "0");
			baggageOnlineListReturn.add(baggageOnline);

			MealsOnline mealsOnline = new MealsOnline(1, "No meal", "0", "NoMeal", "0");
			mealsOnlineListReturn.add(mealsOnline);

			SeatsOnline seatsOnline = new SeatsOnline(1, "0", 0, 0, 0, "0", "NoSeat", 0, "0", "0");
			seatsOnlineListReturn.add(seatsOnline);

			
		}

	}

	
	//// Offline flight activity

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
			@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer, 
			@AuthenticationPrincipal CustomerOAuth2User googleLogin, Model model) {
		ProductDetail productDetail = flightRepo.findById(flightId).get();
		
		String email; 
		Customer customer; 
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByPhone(email);
			
			SearchHistory history = productService.searcHistorySave(productDetail, cityOne, cityTwo, passengerNum, journeyClass, tripType, adultNum,
					childNum, infantNum, customer);
			
			return "redirect:/flight_booking" + history.getId() + "&" + flightId + "&" + travelerDetailsPart(history.getId(), flightId, customer);
			
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByPhone(email);
			
			SearchHistory history = productService.searcHistorySave(productDetail, cityOne, cityTwo, passengerNum, journeyClass, tripType, adultNum,
					childNum, infantNum, customer);
			
			model.addAttribute("customer", customer);
			return "redirect:/flight_booking" + history.getId() + "&" + flightId + "&" + travelerDetailsPart(history.getId(), flightId, customer);
		} else {
			return "redirect:/login";
		}
	}

}
