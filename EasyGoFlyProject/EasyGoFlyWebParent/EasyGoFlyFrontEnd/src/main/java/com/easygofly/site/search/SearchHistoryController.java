package com.easygofly.site.search;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.easygofly.entity.Customer;
import com.easygofly.entity.Product;
import com.easygofly.entity.ProductDetail;
import com.easygofly.entity.SearchHistory;
import com.easygofly.entity.exception.ProductNotFoundException;
import com.easygofly.site.customer.CustomerService;
import com.easygofly.site.flight.ProductDetailService;
import com.easygofly.site.flight.ProductDetailsRepository;
import com.easygofly.site.flight.ProductSaveHelper;
import com.easygofly.site.security.EasyGoFlyCustomerDetails;
import com.easygofly.site.security.oauth.CustomerOAuth2User;

@Controller
public class SearchHistoryController {

	@Autowired
	private SearchHistoryService searchService;
	
	@Autowired
	private CustomerService customerService;
	
	@Autowired
	private ProductDetailService productService;
	
	@Autowired
	private SearchHistoryRepository searchRepo;
	
	@Autowired
	private ProductDetailsRepository productRepo;
	
	@GetMapping("/search_result")
	public String viewSearchResult(@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, @AuthenticationPrincipal CustomerOAuth2User googleLogin, Model model) {
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
				
		return "flight/search";
	}
	
	@GetMapping("/flight_search_{id}")
	public String searchFlightDetailsSingles(@PathVariable(name = "id") Integer id, SearchHistory searchHistory, 
			@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, 
			@AuthenticationPrincipal CustomerOAuth2User googleLogin, 
			Model model, RedirectAttributes redirectAttributes) {
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
		
		SearchHistory search = searchRepo.findById(id).get();

		System.out.println("Date: " + search.getDate());
		
		List<ProductDetail> listProductDetails = productService.listAllFlights(search.getCityOne(), search.getCityTwo(), search.getDate());
		List<Product> getProductBrand = productRepo.findProductByCity(search.getCityOne(), search.getCityTwo(), Sort.by("name").ascending());
		
		model.addAttribute("listProducts", listProductDetails);
		model.addAttribute("getProductBrand", getProductBrand);
		model.addAttribute("search", search);
		
		return "flight/search-result";
	}
	
	@GetMapping("/flight_search-noUser_{cityOne}_{cityTwo}_{journeyClass}_{tripType}_{adultNum}_{childNum}_{infantNum}_{strDate}")
	public String searchFlightDetailsSinglesNoUser(
			@PathVariable(name = "cityOne") String cityOne,
			@PathVariable(name = "cityTwo") String cityTwo,
			@PathVariable(name = "journeyClass") String journeyClass,
			@PathVariable(name = "tripType") String tripType,
			@PathVariable(name = "adultNum") Integer adultNum,
			@PathVariable(name = "childNum") Integer childNum,
			@PathVariable(name = "infantNum") Integer infantNum,
			@PathVariable(name = "strDate") String strDate,
			Model model, RedirectAttributes redirectAttributes) throws ParseException {
		 
	    Date date = new SimpleDateFormat("yyyy-MM-dd").parse(strDate);

		System.out.println("no User Date: " + date);
		
		List<ProductDetail> listProductDetails = productService.listAllFlights(cityOne, cityTwo, date);
		List<Product> getProductBrand = productRepo.findProductByCity(cityOne, cityTwo, Sort.by("name").ascending());
		
		Integer passengerNum = adultNum + childNum + infantNum;
		
		model.addAttribute("listProducts", listProductDetails);
		model.addAttribute("getProductBrand", getProductBrand);
		model.addAttribute("cityOne", cityOne);
		model.addAttribute("cityTwo", cityTwo);
		model.addAttribute("date", date);
		model.addAttribute("journeyClass", journeyClass);
		model.addAttribute("tripType", tripType);
		model.addAttribute("adultNum", adultNum);
		model.addAttribute("childNum", childNum);
		model.addAttribute("infantNum", infantNum);
		model.addAttribute("passengerNum", passengerNum);
		
		return "flight/search-result-noUser";
		
	}
	
	@PostMapping("/flight_search_save")
	public String searchHistorySave(@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer,
			@AuthenticationPrincipal CustomerOAuth2User googleLogin, 
			@RequestParam(name = "cityOne", required = false) String cityOne, 
			@RequestParam(name = "cityTwo", required = false) String cityTwo, 
			@RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date date, 
			@RequestParam(name = "passengerNum", required = false) Integer passengerNum,
			@RequestParam(name = "journeyClass", required = false) String journeyClass,
			@RequestParam(name = "tripType", required = false) String tripType,
			@RequestParam(name = "adultNum", required = false) Integer adultNum,
			@RequestParam(name = "childNum", required = false) Integer childNum,
			@RequestParam(name = "infantNum", required = false) Integer infantNum,
			Model model, 
			RedirectAttributes redirectAttributes) throws ProductNotFoundException {
		
			String email; 
			Customer customer;
			if (loggedCustomer != null) {
				email = loggedCustomer.getUsername();
				customer = customerService.getByEmail(email);
				model.addAttribute("customer", customer);
				Integer searchId = saveHistoryPart(cityOne, cityTwo, date, journeyClass, tripType, adultNum, childNum,
						infantNum, customer);
				System.out.println("Last Value of Search: " + searchId);
				return "redirect:/flight_search_" + searchId;
			} else if (googleLogin != null) {
				email = googleLogin.getEmail();
				customer = customerService.getByEmail(email);
				model.addAttribute("customer", customer);
				Integer searchId = saveHistoryPart(cityOne, cityTwo, date, journeyClass, tripType, adultNum, childNum,
						infantNum, customer);
				System.out.println("Last Value of Search: " + searchId);
				return "redirect:/flight_search_" + searchId;
			}else {
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
			    String strDate = dateFormat.format(date);
				return "redirect:/flight_search-noUser_"+ cityOne +"_"+ cityTwo +"_"+ journeyClass +"_"+ tripType +"_"+ adultNum +"_"+ childNum +"_"+ infantNum +"_"+ strDate;
			}
			
	}
	
	//+"_"+ journeyClass +"_"+ tripType +"_"+ adultNum +"_"+ childNum +"_"+ infantNum

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
}
