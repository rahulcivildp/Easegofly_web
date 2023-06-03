package com.easygofly.site;

import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.easygofly.entity.Brand;
import com.easygofly.entity.City;
import com.easygofly.entity.Customer;
import com.easygofly.entity.ProductDetail;
import com.easygofly.entity.SearchHistory;
import com.easygofly.entity.Wallet;
import com.easygofly.entity.WebDetails;
import com.easygofly.site.flight.BrandRepositoy;
import com.easygofly.site.flight.CityRepository;
import com.easygofly.site.flight.FlightRepository;
import com.easygofly.site.search.SearchHistoryService;
import com.easygofly.site.security.DatabaseLoginSuccessHandler;
import com.easygofly.site.security.EasyGoFlyCustomerDetails;
import com.easygofly.site.security.oauth.CustomerOAuth2User;
import com.easygofly.site.setting.web.WebSettingService;


@Controller
public class MainController {
	
	@Autowired private SearchHistoryService searchHistoryService ;
	@Autowired private CityRepository cityRepo;
	@Autowired private WebSettingService webSettingService;
	@Autowired private FlightRepository flightRepo;
	@Autowired private BrandRepositoy brandRepo;
	
	@GetMapping("/")
	public String viewHomePage(@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, 
			@AuthenticationPrincipal CustomerOAuth2User googleLogin, Model model) {
		Iterable<City> cities = cityRepo.findAll();
		model.addAttribute("cities", cities);
		String email; 
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			Customer customer = searchHistoryService.getByEmail(email);
			Wallet wallet = customer.getWallet();
			model.addAttribute("balance", wallet.getBalance());
			historyPart(model, customer);
			
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			Customer customer = searchHistoryService.getByEmail(email);
			Wallet wallet = customer.getWallet();
			model.addAttribute("balance", wallet.getBalance());
			historyPart(model, customer);	
		}
		
		List<WebDetails> webDetails = webSettingService.listAllSettings();
		for (WebDetails detail : webDetails) {
			model.addAttribute(detail.getKey(), detail.getValue());
			if (detail.getKey().equals("PRICE_1_LINK")) {
				ProductDetail flight = flightFareDetails(detail);
				if (flight != null ) {
					Brand brand = brandRepo.getBrandByName(flight.getBrand());
					model.addAttribute("flightDate1", flight.getDate());
					model.addAttribute("brand1", brand);
				}
				
			} else if (detail.getKey().equals("PRICE_2_LINK")) {
				ProductDetail flight = flightFareDetails(detail);
				if (flight != null ) {
					Brand brand = brandRepo.getBrandByName(flight.getBrand());
					model.addAttribute("flightDate2", flight.getDate());
					model.addAttribute("brand2", brand);
				}
				
			} else if (detail.getKey().equals("PRICE_3_LINK")) {
				ProductDetail flight = flightFareDetails(detail);
				if (flight != null ) {
					Brand brand = brandRepo.getBrandByName(flight.getBrand());
					model.addAttribute("brand3", brand);
					model.addAttribute("flightDate3", flight.getDate());
				}
				
			} else if (detail.getKey().equals("PRICE_4_LINK")) {
				ProductDetail flight = flightFareDetails(detail);
				if (flight != null ) {
					Brand brand = brandRepo.getBrandByName(flight.getBrand());
					model.addAttribute("flightDate4", flight.getDate());
					model.addAttribute("brand4", brand);
				}
				
			} else if (detail.getKey().equals("PRICE_5_LINK")) {
				ProductDetail flight = flightFareDetails(detail);
				if (flight != null ) {
					Brand brand = brandRepo.getBrandByName(flight.getBrand());
					model.addAttribute("flightDate5", flight.getDate());
					model.addAttribute("brand5", brand);
				}
				
			} else if (detail.getKey().equals("PRICE_6_LINK")) {
				ProductDetail flight = flightFareDetails(detail);
				if (flight != null ) {
					Brand brand = brandRepo.getBrandByName(flight.getBrand());
					model.addAttribute("flightDate6", flight.getDate());
					model.addAttribute("brand6", brand);
				}
			}
		}
		
		return "index";
	}

	private ProductDetail flightFareDetails(WebDetails detail) {
		String priceLink1 = detail.getValue();
		String[] parts = priceLink1.split("_");
		if (parts.length != 1) {
			Integer convInteger = Integer.parseInt(parts[4]);
			ProductDetail flight = flightRepo.findById(convInteger).get();
			return flight;
		} else {
			return null;
		}
		
	}

	@GetMapping("/loading")
	public String loading() {
		return "loading";
	}
	
	private void historyPart(Model model, Customer customer) {
		List<SearchHistory> searches =  customer.getSearchHistory();
		
		if (searches.size() != 0) {
			Integer size = searches.size();
			
			if (searches.size() >= 1) {
				SearchHistory lastValue1 = searches.get(size-1);
				model.addAttribute("lastValue1", lastValue1);
			}
			if (searches.size() >= 2) {
				SearchHistory lastValue2 = searches.get(size-2);
				model.addAttribute("lastValue2", lastValue2);
			}
			if (searches.size() >= 3) {
				SearchHistory lastValue3 = searches.get(size-3);
				model.addAttribute("lastValue3", lastValue3);
			}
		}
		
		model.addAttribute("customer", customer);
	}
	
	@GetMapping("/google5435ca7c0eebdeac.html")
	public String googleVerification() {
		return "google5435ca7c0eebdeac";
	}
	
	@GetMapping("/login")
	public String viewLoginPage(Principal principal, HttpServletRequest request) {
		String referer = request.getHeader("Referer");
		request.getSession().setAttribute(DatabaseLoginSuccessHandler.REDIRECT_URL_SESSION_ATTRIBUTE_NAME, referer);
		Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
			return "user_credential/login";
		}
		
		return "redirect:/logged";
	}
	
	@GetMapping("/logged")
	public String viewLoggedPage() {
		return "redirect:/";
	}
	 
	@GetMapping("/error")
	public String viewErrorPage() {
		return "error";
	}
	
	@GetMapping("/about")
	public String viewAboutPage() {
		return "about/about";
	}
	
	@GetMapping("/hotel")
	public String viewHotelPage() {
		return "hotel/hotel";
	}
}
