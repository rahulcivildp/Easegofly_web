package com.easygofly.site;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.easygofly.entity.City;
import com.easygofly.entity.Customer;
import com.easygofly.site.customer.CustomerService;
import com.easygofly.site.flight.CityRepository;
import com.easygofly.site.security.EasegoflyPhoneCustomerDetails;

@RestController
public class MainRestController {
	@Autowired private CityRepository cityRepo;
	@Autowired private CustomerService customerService;
	
	@GetMapping("/find_city_name_{name}")
	public String findCityName(@PathVariable(name = "name") String name) {
		City city =  cityRepo.getCityByName(name);
		return city.getCode() + " - " + city.getName();
	}
	
	@GetMapping("/find_city_by_code_{code}")
	public String findCityNameByCode(@PathVariable(name = "code") String code) {
		City city =  cityRepo.getCityByCode(code);
		return city.getCityName();
	}

	@PostMapping("/update_name_rest")
	public String updateNameCustomer(RedirectAttributes redirectAttributes, 
			@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer,
			@Param("firstName") String firstName, @Param("lastName") String lastName) throws IOException {
			Customer customer;
			
		if (loggedCustomer != null) {
			customer = customerService.getByPhone(loggedCustomer.getUsername());
			Customer updatedCustomer = customerService.updateCustomeName(customer, firstName, lastName);
			
			loggedCustomer.setFirstName(updatedCustomer.getFirstName());
			loggedCustomer.setLastName(updatedCustomer.getLastName());
		} 
		
		redirectAttributes.addFlashAttribute("message", "Your account is updated.");
		return "success";
	}
}
