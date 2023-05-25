package com.easygofly.site.flight;

import java.util.Date;
import java.util.List;

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
import com.easygofly.site.search.SearchHistoryRepository;
import com.easygofly.site.search.SearchHistoryService;
import com.easygofly.site.security.EasyGoFlyCustomerDetails;
import com.easygofly.site.security.oauth.CustomerOAuth2User;
import com.easygofly.site.shoppingCart.CartItemRepository;
import com.easygofly.site.shoppingCart.CartItemService;

@Controller
public class ProdtcDetailsController {
	
	@Autowired private ProductDetailService productService;
	@Autowired private CustomerService customerService;
	@Autowired private FlightRepository flightRepo;
	@Autowired private SearchHistoryRepository searchRepo;
	@Autowired private SearchHistoryService searchService;
	@Autowired private CartItemRepository cartRepo;
	@Autowired private CartItemService cartService;
	@Autowired private TravellerRepository travelerRepo;
	@Autowired private CheckoutService checkoutService;
	
	@GetMapping("/flight_traveler_details{search_id}&{flight_id}&{item_id}")
	public String filghtTravelerDetailsSave(@PathVariable(name = "search_id") Integer search_id, 
			@PathVariable(name = "flight_id") Integer flight_id, 
			@PathVariable(name = "item_id") Integer item_id, 
			@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, 
			@AuthenticationPrincipal CustomerOAuth2User googleLogin,
			Model model, CartItem cartItem) {
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
		
		model.addAttribute("checkoutInfo", checkoutInfo);
		model.addAttribute("travelers", travelers);
		model.addAttribute("item", item);
		model.addAttribute("search", search);
		model.addAttribute("flight", flight);
		
		return "flight/booking/flight_traveler_details";
	}
	
	@GetMapping("/flight_booking{search_id}&{flight_id}&{item_id}")
	public String filghtBookingSave(@PathVariable(name = "search_id") Integer search_id, 
			@PathVariable(name = "flight_id") Integer flight_id,
			@PathVariable(name = "item_id") Integer item_id, 
			@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, 
			@AuthenticationPrincipal CustomerOAuth2User googleLogin,
			Model model, 
			CartItem cartItem ) {
		
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
			return "redirect:/flight_booking" + searchId + "&" + flightId + "&" + travelerDetailsPart(searchId, flightId, customer);
			
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByEmail(email);
			model.addAttribute("customer", customer);
			return "redirect:/flight_booking" + searchId + "&" + flightId + "&" + travelerDetailsPart(searchId, flightId, customer);
		} else {
			return "redirect:/";
		}
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

	private Integer travelerDetailsPart(Integer searchId, Integer flightId, Customer customer) {
		ProductDetail flight = flightRepo.findById(flightId).get();
		SearchHistory search = searchRepo.findById(searchId).get();
		
		flight.addBooking(customer);
		productService.saveCartItem(flight);
		
		ProductDetail savedCart = productService.saveCartItem(flight);
		List<CartItem> savedCartItemProduct = savedCart.getCartItems();
		CartItem LastItem = savedCartItemProduct.get(savedCartItemProduct.size() - 1);
		float totalAdultPrice = (flight.getPriceADT() + flight.getMarkupADT()) * (search.getAdultNum() + search.getChildNum());
		float totalInfantPrice = (flight.getPriceINF() + flight.getMarkupINF()) * search.getInfantNum();
		double totalPrice = totalAdultPrice + totalInfantPrice;
		
		System.out.println("totalAdultPrice : " + totalAdultPrice + " totalInfantPrice : " + totalInfantPrice + " totalPrice : " + totalPrice);
		
		cartService.updateTotalPrice(LastItem, totalPrice);
		searchService.updateSearchHistory(search, LastItem);
		return LastItem.getId();
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
			@RequestParam(name = "dob", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date[] dob, CartItem cartItem	 ) {
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
				ProductSaveHelper.setTravellerDetail(salutation[i], firstName[i], lastName[i], dob[i], flight, item);
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
