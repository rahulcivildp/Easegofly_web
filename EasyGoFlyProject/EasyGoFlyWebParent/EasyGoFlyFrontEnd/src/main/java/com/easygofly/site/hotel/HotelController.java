package com.easygofly.site.hotel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.easygofly.entity.Customer;
import com.easygofly.entity.HotelHistory;
import com.easygofly.entity.TBOCity;
import com.easygofly.site.customer.CustomerService;
import com.easygofly.site.flightAPI.TBOCityRepository;
import com.easygofly.site.security.EasegoflyPhoneCustomerDetails;

@Controller
public class HotelController {
	@Autowired private TBOCityRepository tboRepo;
	@Autowired private HotelService hotelService;
	@Autowired private CustomerService customerService;

	private String searchURL = "";
	
	@GetMapping("/hotel")
	public String viewHotelPage(Model model) {
		Iterable<TBOCity> cities = tboRepo.findAll();
		List<String> cityList = new ArrayList<String>();
		List<String> cityIds = new ArrayList<String>();
		List<String> cityStates = new ArrayList<String>();
		List<String> cityCountries = new ArrayList<String>();
		Date date = new Date();
		
		for (TBOCity city : cities) {
			cityList.add(city.getDestination());
			cityIds.add(city.getCityId().toString());
			cityStates.add(city.getStateProvince());
			cityCountries.add(city.getCountry());
		}

		model.addAttribute("today", date);
		model.addAttribute("tboCities", cities);
		model.addAttribute("cityList", cityList);
		model.addAttribute("cityIds", cityIds);
		model.addAttribute("cityStates", cityStates);
		model.addAttribute("cityCountries", cityCountries);
		
		
		hotelService.authenticationFlight(model);
		
		return "hotel/hotel";
	}
	

	@PostMapping("/hotel/saveSearchHotel")
	public String saveSearchHotel(@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer,
			@RequestParam(name = "hotelCity", required = false) String hotelCity, 
			@RequestParam(name = "checkInDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date checkInDate, 
			@RequestParam(name = "checkOutDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date checkOutDate, 
			@RequestParam(name = "noOfAdults", required = false) Integer noOfAdults,
			@RequestParam(name = "noOfChildren", required = false) Integer noOfChildren,
			@RequestParam(name = "noOfRooms", required = false) Integer noOfRooms) {
		
		TBOCity city = tboRepo.getCityByCityId(hotelCity);
	    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		if (loggedCustomer != null) {
			String email = loggedCustomer.getUsername();
			Customer customer = customerService.getByPhone(email);
			HotelHistory history = new HotelHistory();
			history.setCheckInDate(checkInDate);
			history.setCheckOutDate(checkOutDate);
			history.setChildrenAge(null);
			history.setCityId(city.getCityId().toString());
			history.setCountryCode(city.getCountryCode());
			history.setNearBySearchAllowed(false);
			history.setNoOfAdults(noOfAdults.toString());
			history.setNoOfChild(noOfChildren.toString());
			history.setNoOfRooms(noOfRooms.toString());
			history.setCustomer(customer);
			
			hotelService.saveHotelHistory(history, customer);
		} else {
			
		}
		searchURL = "/hotel/search_" + hotelCity + "_" + dateFormat.format(checkInDate) + "_" + dateFormat.format(checkOutDate) + "_" + noOfAdults + "_" + noOfChildren + "_" + noOfRooms;
		
		return "redirect:/hotel_loading...";
	}

	@GetMapping("/hotel/search_{hotelCity}_{checkInDate}_{checkOutDate}_{noOfAdults}_{noOfChildren}_{noOfRooms}")
	public String viewHotelSearchResult(Model model, 
			@PathVariable(name = "hotelCity") String hotelCity,
			@PathVariable(name = "checkInDate") String checkInDate,
			@PathVariable(name = "checkOutDate") String checkOutDate,
			@PathVariable(name = "noOfAdults") String noOfAdults,
			@PathVariable(name = "noOfChildren") String noOfChildren,
			@PathVariable(name = "noOfRooms") String noOfRooms) {
		
		
		model.addAttribute("hotelCity", hotelCity);
		return "hotel/search/hotel-search-result";
	}
	

	@GetMapping("/hotel_loading...")
    public String performApiRequest(Model model) {
        model.addAttribute("searchURL", searchURL);
        return "loading/loading";
    }
}
