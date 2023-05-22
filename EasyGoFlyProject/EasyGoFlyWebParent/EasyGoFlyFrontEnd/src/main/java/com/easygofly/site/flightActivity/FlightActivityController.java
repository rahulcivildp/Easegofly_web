package com.easygofly.site.flightActivity;

import java.text.DateFormat;
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

import com.easygofly.entity.City;
import com.easygofly.entity.Customer;
import com.easygofly.entity.Product;
import com.easygofly.entity.ProductDetail;
import com.easygofly.entity.exception.ProductNotFoundException;
import com.easygofly.site.customer.CustomerService;
import com.easygofly.site.flight.CityRepository;
import com.easygofly.site.flight.ProductDetailService;
import com.easygofly.site.flight.ProductDetailsRepository;
import com.easygofly.site.security.EasyGoFlyCustomerDetails;
import com.easygofly.site.security.oauth.CustomerOAuth2User;

@Controller
public class FlightActivityController {
	@Autowired CityRepository cityRepo;
	@Autowired CustomerService customerService;
	@Autowired ProductDetailService productDetailService;
	@Autowired ProductDetailsRepository productRepo;
	
	@PostMapping("/flight_activity_search")
	public String searchHistorySave(@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer,
			@AuthenticationPrincipal CustomerOAuth2User googleLogin, 
			@RequestParam(name = "cityOne", required = true) String cityOne, 
			@RequestParam(name = "cityTwo", required = true) String cityTwo,  
			@RequestParam(name = "passengerNum", required = false) Integer passengerNum,
			@RequestParam(name = "journeyClass", required = false) String journeyClass,
			@RequestParam(name = "tripType", required = false) String tripType,
			@RequestParam(name = "adultNum", required = false) Integer adultNum,
			@RequestParam(name = "childNum", required = false) Integer childNum,
			@RequestParam(name = "infantNum", required = false) Integer infantNum,
			Model model) throws ProductNotFoundException {
		
			String email; 
			Customer customer;
	
			City city1 = cityRepo.getCityByCode(cityOne);
		    City city2 = cityRepo.getCityByCode(cityTwo);
		   
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

			return "redirect:/flight_activity_" + city1.getCode() +"_"+ city2.getCode() +"_"+ journeyClass +"_"+ tripType +"_"+ adultNum +"_"+ childNum +"_"+ infantNum +"_"+ sort +"_"+ brand +"_"+ stop +"_"+ arrayPrice +"_"+ activeTime;
			
	}
	
	@GetMapping("/flight_activity")
	public String flightActivity() {
		return "flight-activity/flight-activity";
	}
	
	@GetMapping("/flight_activity_{cityOne}_{cityTwo}_{journeyClass}_{tripType}_{adultNum}_{childNum}_{infantNum}_{sortName}_{brand}_{stop}_{totalPrice}_{activeTime}")
	public String flightActivitySearch(
			@PathVariable(name = "cityOne") String cityOne,
			@PathVariable(name = "cityTwo") String cityTwo,
			@PathVariable(name = "journeyClass") String journeyClass,
			@PathVariable(name = "tripType") String tripType,
			@PathVariable(name = "adultNum") Integer adultNum,
			@PathVariable(name = "childNum") Integer childNum,
			@PathVariable(name = "infantNum") Integer infantNum,
			@PathVariable(name = "sortName") String sortName,
			@PathVariable(name = "activeTime") String[] activeTime,
			@PathVariable(name = "brand") String[] brands,
			@PathVariable(name = "stop") Integer[] stops,
			@PathVariable(name = "totalPrice") Integer[] totalPrice,
			Model model, RedirectAttributes redirectAttributes) {
		List<ProductDetail> productDetails = productDetailService.listAllFLightByCity(cityOne, cityTwo, Sort.by(sortName).ascending());
		for (ProductDetail productDetail : productDetails) {
			System.out.println("getCode: " + productDetail.getPnr());
		}
		
		Integer passengerNum = adultNum + childNum + infantNum;
		List<Product> getProductBrand = productRepo.findProductByCity(cityOne, cityTwo, Sort.by("name").ascending());
		
		model.addAttribute("getProductBrand", getProductBrand);
		model.addAttribute("listProducts", productDetails);
		model.addAttribute("cityOne", cityOne);
		model.addAttribute("cityTwo", cityTwo);
		model.addAttribute("journeyClass", journeyClass);
		model.addAttribute("tripType", tripType);
		model.addAttribute("adultNum", adultNum);
		model.addAttribute("childNum", childNum);
		model.addAttribute("infantNum", infantNum);
		model.addAttribute("passengerNum", passengerNum);
		model.addAttribute("brands", brands);
		model.addAttribute("stops", stops);
		model.addAttribute("totalPrice", totalPrice);
		return "flight-activity/search-result-noUser-noDate";
	}

}
