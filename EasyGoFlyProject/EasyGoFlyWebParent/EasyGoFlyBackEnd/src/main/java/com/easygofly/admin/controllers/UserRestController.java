package com.easygofly.admin.controllers;

import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easygofly.admin.user.UserRepository;
import com.easygofly.entity.User;

@RestController
public class UserRestController {
	
	@Autowired private UserRepository userRepo;

	@PostMapping("/users/check_email_{email}")
	public String emailCheckUser(@PathParam("email") String email) {
		User existingUser = userRepo.getUserByEmail(email);
		if(existingUser == null) {
			return "OK";
		} else {
			return "Duplicate Email";
		}
	}
}
