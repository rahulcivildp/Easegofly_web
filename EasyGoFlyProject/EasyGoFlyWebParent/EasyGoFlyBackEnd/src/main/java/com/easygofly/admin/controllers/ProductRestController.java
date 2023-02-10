package com.easygofly.admin.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easygofly.admin.product.ProductService;


@RestController
public class ProductRestController {

	@Autowired
	private ProductService service;
	
	@PostMapping("/products/check_name")
	public String chechDuplicateName(@Param("id") Integer id, @Param("name") String name) {
		return service.isNameUnique(id, name);
	}
}
