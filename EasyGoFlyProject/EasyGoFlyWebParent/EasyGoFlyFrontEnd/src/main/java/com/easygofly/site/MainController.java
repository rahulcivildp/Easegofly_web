package com.easygofly.site;

import java.security.Principal;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.easygofly.entity.City;
import com.easygofly.entity.Country;
import com.easygofly.entity.Customer;
import com.easygofly.entity.SearchHistory;
import com.easygofly.entity.Wallet;
import com.easygofly.site.customer.CustomerService;
import com.easygofly.site.flight.CityRepository;
import com.easygofly.site.flight.SearchHistoryRepository;
import com.easygofly.site.flight.SearchHistoryService;
import com.easygofly.site.security.EasegoflyPhoneCustomerDetails;
import com.easygofly.site.security.LoginSuccessHandler;
import com.easygofly.site.security.oauth.CustomerOAuth2User;
import com.easygofly.site.setting.CountryRepository;


@Controller
@Component
@Scope("session")
public class MainController {
	
	@Autowired private SearchHistoryService searchHistoryService ;
	@Autowired private CityRepository cityRepo;
	@Autowired private CountryRepository countryRepo;
	@Autowired private CustomerService customerService;
	@Autowired private SearchHistoryRepository searchRepo;
	
	@SuppressWarnings("unused")
	private String tokenId = "";
	
	@GetMapping("/")
	public String viewHomePage(@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer, @AuthenticationPrincipal CustomerOAuth2User oauthCustomer, Model model) {
		Country country = countryRepo.findById(106).get();
		Iterable<City> cities = cityRepo.getCityByCountry(country);
		model.addAttribute("cities", cities);

		Date today = new Date();
		model.addAttribute("today", today);
		
		Iterable<City> allCities = cityRepo.findAll();
		model.addAttribute("allCities", allCities);
		String phone; 
		if (loggedCustomer != null) {
			phone = loggedCustomer.getUsername();
			Customer customer = searchHistoryService.getByPhone(phone);
			Wallet wallet = customer.getWallet();
			model.addAttribute("balance", wallet.getBalance());
			historyPart(model, customer);
			ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
			@SuppressWarnings("unused")
			HttpSession session= attr.getRequest().getSession(true);
		} else if (oauthCustomer != null) {
			phone = oauthCustomer.getEmail();
			Customer customer = customerService.getByEmail(phone);
			Wallet wallet = customer.getWallet();
			model.addAttribute("balance", wallet.getBalance());
			historyPart(model, customer);
			ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
			@SuppressWarnings("unused")
			HttpSession session= attr.getRequest().getSession(true);
		}
	
		return "index";
	}


	@GetMapping("/loading")
	public String loading() {
		return "loading";
	}

	
	@GetMapping("/loading___")
	public String loadingSearchHistory(
			@RequestParam(name = "searchId", required = false) Integer searchId, Model model) {
		Country india = countryRepo.findById(106).get();
		String searchURL = "";
		
		SearchHistory history = searchRepo.findById(searchId).get();
		City cityOne = cityRepo.getCityByCode(history.getCityOne());
		City cityTwo = cityRepo.getCityByCode(history.getCityTwo());
		
		
		if (cityOne.getCountry() == india && cityTwo.getCountry() == india) {
			searchURL = "/flight_search_" + searchId + "_pnr__0_0,0_active";
			model.addAttribute("searchURL", searchURL);
			
		} else {
			searchURL = "/flight_search_international_" + searchId + "_pnr__0_0,0_active";
			model.addAttribute("searchURL", searchURL);
		}
		
		return "loading/loading";
	}
	
	@GetMapping("/find_brand")
	public String findBrand() {
		
		return "find_brand";
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
	public String viewLoginPage(Principal principal, HttpServletRequest request, HttpServletResponse response) {
		String referer = request.getHeader("Referer");
		request.getSession().setAttribute(LoginSuccessHandler.REDIRECT_URL_SESSION_ATTRIBUTE_NAME, referer);
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

	@GetMapping("/contact-us")
	public String viewContactPage() {
		return "contact/contact";
	}
	
	@GetMapping("/mode")
	public String settingMode() {
		return "settings/mode";
	}

	@GetMapping("/coming_soon")
	public String commingSoon() {
		return "coming-soon";
	}
	


}
