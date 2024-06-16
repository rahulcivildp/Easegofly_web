package com.easygofly.site;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.easygofly.entity.CartItem;
import com.easygofly.entity.Customer;
import com.easygofly.entity.ProductDetail;
import com.easygofly.entity.SearchHistory;
import com.easygofly.site.customer.CustomerService;
import com.easygofly.site.flight.FlightRepository;
import com.easygofly.site.flight.ProductDetailService;
import com.easygofly.site.flight.ProductSaveHelper;
import com.easygofly.site.flight.SearchHistoryRepository;
import com.easygofly.site.flight.SearchHistoryService;
import com.easygofly.site.security.EasegoflyPhoneCustomerDetails;
import com.easygofly.site.security.oauth.CustomerOAuth2User;
import com.easygofly.site.shoppingCart.CartItemService;

@Controller
public class FareFinderController {
	
	@Autowired private ProductDetailService productService;
	@Autowired private SearchHistoryService searchService;
	@Autowired private CustomerService customerService;
	@Autowired private FlightRepository flightRepo;
	@Autowired private SearchHistoryRepository searchRepo;
	@Autowired private CartItemService cartService;
	
	@GetMapping("/search_flight_lowest_fare_{flight_id}_{cityOne}_{cityTwo}_{journeyClass}_{tripType}_{strDate}")
	public String searchFlightDetailsSinglesNoUser(@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer,
			@AuthenticationPrincipal CustomerOAuth2User googleLogin, 
			@PathVariable(name = "cityOne") String cityOne,
			@PathVariable(name = "cityTwo") String cityTwo,
			@PathVariable(name = "journeyClass") String journeyClass,
			@PathVariable(name = "tripType") String tripType,
			@PathVariable(name = "strDate") String strDate,
			@PathVariable(name = "flight_id") String flight_id,
			Model model, RedirectAttributes redirectAttributes) throws ParseException {
		
		String email; 
		Customer customer;
		Date date = new SimpleDateFormat("yyyy-MM-dd").parse(strDate);
		Integer flightId = Integer.parseInt(flight_id);
		
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByPhone(email);
			model.addAttribute("customer", customer);
			Integer searchId = saveHistoryPart(cityOne, cityTwo, date, journeyClass, tripType, 1, 0,
					0, customer);
			System.out.println("Last Value of Search: " + searchId);
			return "redirect:/flight_booking" + searchId + "&" + flightId + "&" + travelerDetailsPart(searchId, flightId, customer);
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByPhone(email);
			model.addAttribute("customer", customer);
			Integer searchId = saveHistoryPart(cityOne, cityTwo, date, journeyClass, tripType, 1, 0,
					0, customer);
			System.out.println("Last Value of Search: " + searchId);
			return "redirect:/flight_booking" + searchId + "&" + flightId + "&" + travelerDetailsPart(searchId, flightId, customer);
		}else {
			return "redirect:/";
		}
		
	}
	
	private Integer saveHistoryPart(String cityOne, String cityTwo, Date date, String journeyClass, String tripType,
			Integer adultNum, Integer childNum, Integer infantNum, Customer customer) {
		Integer totalPassenger = adultNum + childNum + infantNum;
		
		ProductSaveHelper.setSearchHistory(customer, cityOne, cityTwo, totalPassenger, journeyClass, adultNum, childNum, infantNum, tripType, date);
		
		searchService.saveSearchHistory(customer);
		Customer savedSearch = searchService.saveSearchHistory(customer);
		
		
		List<SearchHistory> savedSearchResult = savedSearch.getSearchHistory();
		SearchHistory lastValue = savedSearchResult.get(savedSearchResult.size() - 1);
		
		Integer searchId = lastValue.getId();
		return searchId;
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
}
