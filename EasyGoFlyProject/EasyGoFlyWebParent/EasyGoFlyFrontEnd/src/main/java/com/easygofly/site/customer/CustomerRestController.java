package com.easygofly.site.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class CustomerRestController {

	@Autowired private CustomerService service;
	
	@PostMapping("/customers/check_email")
	public String chechDuplicateEmail(@Param("id") Integer id, @Param("email") String email) {
		return service.isEmailUnique(id, email) ? "OK" : "Duplicate Email";
	}
	
	@PostMapping("/customers/check_phone")
	public String chechDuplicatePhone(@Param("id") Integer id, @Param("phone") String phone) {
		return service.isPhoneUnique(id, phone) ? "OK" : "Duplicate Phone Number";
	}
	
}
