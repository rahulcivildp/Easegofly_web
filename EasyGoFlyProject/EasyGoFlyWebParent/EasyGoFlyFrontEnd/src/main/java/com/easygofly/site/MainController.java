package com.easygofly.site;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.easygofly.entity.City;
import com.easygofly.entity.Customer;
import com.easygofly.entity.SearchHistory;
import com.easygofly.site.flight.CityRepository;
import com.easygofly.site.search.SearchHistoryService;
import com.easygofly.site.security.EasyGoFlyCustomerDetails;
import com.easygofly.site.security.oauth.CustomerOAuth2User;


@Controller
public class MainController {
	
	@Autowired private SearchHistoryService searchHistoryService ;
	@Autowired private CityRepository cityRepo;
	
	@GetMapping("/")
	public String viewHomePage(@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, 
			@AuthenticationPrincipal CustomerOAuth2User googleLogin, Model model) {
		Iterable<City> cities = cityRepo.findAll();
		model.addAttribute("cities", cities);
		String email; 
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			Customer customer = searchHistoryService.getByEmail(email);
			historyPart(model, customer);
			
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			Customer customer = searchHistoryService.getByEmail(email);
			historyPart(model, customer);	
		}
		
		return "index";
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
	public String viewLoginPage() {
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
