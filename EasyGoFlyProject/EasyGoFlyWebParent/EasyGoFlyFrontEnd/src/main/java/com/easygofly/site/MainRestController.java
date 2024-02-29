package com.easygofly.site;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.easygofly.entity.City;
import com.easygofly.site.flight.CityRepository;

@RestController
public class MainRestController {
	@Autowired private CityRepository cityRepo;
	
	@GetMapping("/find_city_name_{name}")
	public String findCityName(@PathVariable(name = "name") String name) {
		City city =  cityRepo.getCityByName(name);
		System.out.println(city.getCityName());
		return city.getCode() + " - " + city.getName();
	}

}
