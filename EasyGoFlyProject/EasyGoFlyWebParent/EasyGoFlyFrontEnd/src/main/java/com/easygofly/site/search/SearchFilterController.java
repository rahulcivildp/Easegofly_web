package com.easygofly.site.search;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SearchFilterController {

	@PostMapping("/noUser_search_filter")
	public String searchFilter(
			@RequestParam(name = "brand") String brand,
			@RequestParam(name = "stop") String stop,
			@RequestParam(name = "totalPrice", required = false) String totalPrice,
			@RequestParam(name = "cityOne") String cityOne,
			@RequestParam(name = "cityTwo") String cityTwo, 
			@RequestParam(name = "date", required = false) String date,
			@RequestParam(name = "passengerNum", required = false) Integer passengerNum,
			@RequestParam(name = "journeyClass", required = false) String journeyClass,
			@RequestParam(name = "tripType", required = false) String tripType,
			@RequestParam(name = "activeTime", required = false) String activeTime,
			@RequestParam(name = "adultNum", required = false) Integer adultNum,
			@RequestParam(name = "childNum", required = false) Integer childNum,
			@RequestParam(name = "infantNum", required = false) Integer infantNum, Model model) {
	    String sort = "pnr";
	    return "redirect:/flight_search-noUser_"+ cityOne +"_"+ cityTwo +"_"+ journeyClass +"_"+ tripType +"_"+ adultNum 
	    		+"_"+ childNum +"_"+ infantNum +"_"+ date +"_"+ sort +"_"+ brand +"_"+ stop +"_"+ totalPrice +"_"+ activeTime;
	}
	
	@PostMapping("/user_search_filter")
	public String userSearchFilter(
			@RequestParam(name = "brand") String brand,
			@RequestParam(name = "stop") String stop,
			@RequestParam(name = "totalPrice", required = false) String totalPrice,
			@RequestParam(name = "searchId") String searchId, 
			@RequestParam(name = "date", required = false) String date,
			@RequestParam(name = "activeTime", required = false) String activeTime, Model model) {
	    String sort = "pnr";
	    return "redirect:/flight_search_" + searchId +"_"+ sort +"_"+ brand +"_"+ stop +"_"+ totalPrice +"_"+ activeTime;
	}
}
