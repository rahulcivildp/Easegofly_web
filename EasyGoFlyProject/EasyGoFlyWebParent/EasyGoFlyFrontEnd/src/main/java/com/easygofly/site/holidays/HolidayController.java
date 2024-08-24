package com.easygofly.site.holidays;

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
import com.easygofly.entity.SightseeingHistory;
import com.easygofly.entity.TBOCity;
import com.easygofly.site.customer.CustomerService;
import com.easygofly.site.flight.TBOCityRepository;
import com.easygofly.site.security.EasegoflyPhoneCustomerDetails;

@Controller
public class HolidayController {
	@Autowired private TBOCityRepository tboRepo;
	@Autowired private CustomerService customerService;
	@Autowired private HolidayService service ;

	private String searchURL = "";
	private String bookingURL = "";
	private String orderURL = "";
	
	SightseeingHistory history = new SightseeingHistory();

	@GetMapping("/holiday")
	public String viewBusPage(Model model) {
		cityFinder(model);
		
		return "holiday/holiday";
	}
	
	@PostMapping("/holiday/saveSearchHoliday")
	public String saveSearchSightseeing(@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer,
			@RequestParam(name = "hotelCity", required = false) String hotelCity, 
			@RequestParam(name = "checkInDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date checkInDate, 
			@RequestParam(name = "checkOutDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date checkOutDate, 
			@RequestParam(name = "noOfAdults", required = false) Integer noOfAdults,
			@RequestParam(name = "noOfChildren", required = false) Integer noOfChildren,
			@RequestParam(name = "childAge", required = false) Integer[] childAge) {
		
		TBOCity city = tboRepo.getCityByCityName(hotelCity);
	    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	    
		if (loggedCustomer != null) {
			String email = loggedCustomer.getUsername();
			Customer customer = customerService.getByPhone(email);	
			SightseeingHistory newHistory = new SightseeingHistory();
			
			newHistory.setAdultCount(noOfAdults);
			newHistory.setChildCount(noOfChildren);
			newHistory.setFromDate(checkOutDate);
			newHistory.setToDate(checkOutDate);
			newHistory.setChildrenAge(childAge);
			newHistory.setCityId(city.getCityId());
			newHistory.setCountryCode(city.getCountryCode());
			newHistory.setCustomer(customer);
			
			history = new SightseeingHistory();
			history = service.saveSightseeingHistory(newHistory, customer);
			
		} 

		searchURL = "/holiday/search_" + hotelCity + "_" + dateFormat.format(checkInDate) + "_" + dateFormat.format(checkOutDate) + "_" + noOfAdults + "_" + noOfChildren;
				
		return "redirect:/sightseeing_loading...";

	}
	

	@GetMapping("/sightseeing_loading...")
    public String performApiRequest(Model model) {
        model.addAttribute("searchURL", searchURL);
        return "loading/loading";
    }
	

	@GetMapping("/holiday/search_{hotelCity}_{checkInDate}_{checkOutDate}_{noOfAdults}_{noOfChildren}")
	public String viewHotelSearchResult(Model model, 
			@PathVariable(name = "hotelCity") String hotelCity,
			@PathVariable(name = "checkInDate") String checkInDate,
			@PathVariable(name = "checkOutDate") String checkOutDate,
			@PathVariable(name = "noOfAdults") String noOfAdults,
			@PathVariable(name = "noOfChildren") String noOfChildren,
			@PathVariable(name = "noOfAdultsTwo") String noOfAdultsTwo,
			@PathVariable(name = "noOfChildrenTwo") String noOfChildrenTwo,
			@PathVariable(name = "noOfRooms") String noOfRooms) throws Exception {
		
		
        return "loading/loading";
	}
	
	//private methods

	private void cityFinder(Model model) {
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
	}

}
