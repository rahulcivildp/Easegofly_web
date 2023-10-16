package com.easygofly.site.flight;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.easygofly.entity.CartItem;
import com.easygofly.entity.Customer;
import com.easygofly.entity.ProductDetail;
import com.easygofly.site.security.EasyGoFlyCustomerDetails;
import com.easygofly.site.security.oauth.CustomerOAuth2User;
import java.io.IOException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.easygofly.entity.BaggageOnline;
import com.easygofly.entity.FlightMap;
import com.easygofly.entity.MealsOnline;
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
import com.easygofly.site.shoppingCart.CartItemRepository;
import com.easygofly.site.shoppingCart.CartItemService;

@Controller
public class ProductDetailsInternationController {
	
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
	@Autowired private ProductDetailsController productDetailsController;
	@Autowired private OnlineFlightService onlineFlightService;
	@Autowired private SearchHistoryController searchHistoryController;
	@Autowired private TravelerService travelerService;
	@Autowired private LogService logService;
	
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
	public boolean lcc = true;
	public boolean lccReturn = true;
	
	public String discount ="" , tdsOnIncentive ="", tdsOnCommission ="", tdsOnPLB ="", otherCharges ="", publishedFare ="", offeredFare ="", serviceFee ="";
	public String discountReturn ="" , tdsOnIncentiveReturn ="", tdsOnCommissionReturn ="", tdsOnPLBReturn ="", otherChargesReturn ="", publishedFareReturn ="", offeredFareReturn ="", serviceFeeReturn ="";
	

	@PostMapping("/flight_international_booking_save")
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
			cartItem = productDetailsController.travelerDetailsPart(searchIdInt, flightId, customer);
			ProductDetail productDetail = cartItem.getProductDetail();
			return "redirect:/flight_international_booking" + searchIdInt + "&" + productDetail.getId() + "&" + cartItem.getId();
			
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
			cartItem = productDetailsController.travelerDetailsPart(searchIdInt, flightId, customer);
			ProductDetail productDetail = cartItem.getProductDetail();
			return "redirect:/flight_international_booking" + searchIdInt + "&" + productDetail.getId() + "&" + cartItem.getId();
		} else {
			return "redirect:/";
		}
	}
	
	@GetMapping("/flight_international_booking{search_id}&{flight_id}&{item_id}")
	public String filghtBookingSave(@PathVariable(name = "search_id") Integer search_id, 
			@PathVariable(name = "flight_id") Integer flight_id,
			@PathVariable(name = "item_id") Integer item_id, 
			@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, 
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
            
            onlineFlightService.apiOnlineFarerule_quote(connectionFarerule, responseBodyFarerule, searchHistoryController.traceId, flight.getResultIndex());

        	JSONObject jsonObjFarerules = new JSONObject(responseBodyFarerule.toString());
        	System.out.println(jsonObjFarerules);
            logService.generateLog(jsonObjFarerules.toString());
        	try {
				JSONArray jsonObjFareruleResponse = jsonObjFarerules.getJSONObject("Response").getJSONArray("FareRules");
				JSONObject jsonObjFarerule = jsonObjFareruleResponse.getJSONObject(0);
				String fareRuleDetail = jsonObjFarerule.get("FareRuleDetail").toString();
				
				model.addAttribute("jsonObjFarerule", fareRuleDetail);
			} catch (Exception e) {
				return "/flight_search_international_" + search.getId() +"_"+ sort +"_"+ brand +"_"+ stop +"_"+ arrayPrice +"_"+ activeTime;
			}
        	
		} 
		
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
		
		return "flight/inter_booking/flight_booking";
	}
	
	@PostMapping("/traveller_international_details")
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
			@RequestParam(name = "passportNo", required = false) String[] passportNo,
			@RequestParam(name = "passportExpiry", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date[] passportExpiry, 
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
				ProductSaveHelper.setTravellerDetailInternational(salutation[i], firstName[i], lastName[i], dob[i], flight, item, paxType[i], flight.getBaggage(), flight.getCabinBaggage(), i, passportNo[i], passportExpiry[i]);
				productService.saveFlightPassengerDetails(flight);
				ProductDetail flightDetails = productService.saveFlightPassengerDetails(flight);
				model.addAttribute("flightDetails", flightDetails);
			}
			
			model.addAttribute("item", item);
			model.addAttribute("search", search);
			model.addAttribute("flight", flight);

			return  "redirect:/flight_international_traveler_details" + searchId + "&" + flightId + "&" + item_id;
		} catch (Exception e) {
			return  "redirect:/flight_international_traveler_details" + searchId + "&" + flightId + "&" + item_id;
		}
	}
	
	@PostMapping("/traveller_detail_international_edit")
	public String updateTravellerDetail(@RequestParam(name = "traveler_id") Integer traveler_id,
			@RequestParam(name = "search_id") Integer searchId, 
			@RequestParam(name = "flight_id") Integer flightId,
			@RequestParam(name = "item_id") Integer item_id,
			@RequestParam(name = "saveSalutation", required = false) String salutation, 
			@RequestParam(name = "saveFirstName", required = false) String firstName,  
			@RequestParam(name = "saveLastName", required = false) String lastName, 
			@RequestParam(name = "savedob", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date dob,  
			@RequestParam(name = "savePassportNo", required = false) String savePassportNo, 
			@RequestParam(name = "savePassportExpiry", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date savePassportExpiry) {
		TravellerDetail traveler = travelerRepo.findById(traveler_id).get();
		
		cartService.updateTravelerPassport(traveler, salutation, firstName, lastName, dob, savePassportNo, savePassportExpiry);
		
		return  "redirect:/flight_international_traveler_details" + searchId + "&" + flightId + "&" + item_id;
	}
	
	@GetMapping("/flight_international_traveler_details{search_id}&{flight_id}&{item_id}")
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
		
		
		try {
			productDetailsController.fareQuoteMethod(model, flight, mealsOnlineList, baggageOnlineList, seatsOnlineList);
		}  catch (IOException e) {
			return "redirect:/flight_international_booking" + search.getId() + "&" + flight.getId() + "&" + item.getId();
		}
		
		model.addAttribute("checkoutInfo", checkoutInfo);
		model.addAttribute("travelers", travelers);
		model.addAttribute("item", item);
		model.addAttribute("search", search);
		model.addAttribute("flight", flight);
		model.addAttribute("falied", "Please provide a correct coupon code!!!");
		model.addAttribute("success", "The coupon is verified!");
		
		
		return "flight/inter_booking/flight_traveler_details";
	}
}
