package com.easygofly.site.hotel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.easygofly.entity.TBOCity;
import com.easygofly.site.flightAPI.TBOCityRepository;

@Controller
public class HotelController {
	@Autowired private TBOCityRepository tboRepo;
	
	@GetMapping("/hotel")
	public String viewHotelPage(Model model) {
		Iterable<TBOCity> cities = tboRepo.findAll();
		List<String> cityList = new ArrayList<String>();
		List<String> cityIds = new ArrayList<String>();
		List<String> cityStates = new ArrayList<String>();
		List<String> cityCountries = new ArrayList<String>();
		
		for (TBOCity city : cities) {
			cityList.add(city.getDestination());
			cityIds.add(city.getCityId().toString());
			cityStates.add(city.getStateProvince());
			cityCountries.add(city.getCountry());
		}
		
		
		model.addAttribute("tboCities", cities);
		model.addAttribute("cityList", cityList);
		model.addAttribute("cityIds", cityIds);
		model.addAttribute("cityStates", cityStates);
		model.addAttribute("cityCountries", cityCountries);
		
		return "hotel/hotel";
	}
}
