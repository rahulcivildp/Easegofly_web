package com.easygofly.admin.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.easygofly.admin.brand.BrandRepositoy;
import com.easygofly.entity.Brand;

@RestController
public class OrderRestController {
	@Autowired BrandRepositoy brandRepo;
	
	@GetMapping("/find_brand_{name}")
	public String findBrand(@PathVariable(name = "name") String name) {
		Brand brand = brandRepo.getBrandByName(name);
		if ( brand == null ) {
			return "/images/no-image.png";
		}
		String st = brand.getPhotosImagePath();
		return st;
	}
}
