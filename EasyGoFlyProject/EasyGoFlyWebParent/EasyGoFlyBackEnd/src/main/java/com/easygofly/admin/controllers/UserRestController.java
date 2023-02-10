package com.easygofly.admin.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easygofly.admin.user.UserService;


@RestController
public class UserRestController {
	
	@Autowired
	private UserService service;
	
	@PostMapping("/users/check_email")
	public String chechDuplicateEmail(@Param("id") Integer id, @Param("email") String email) {
		return service.isEmailUnique(id, email) ? "OK" : "Duplicate Email";
	}
}
