package com.easygofly.api.flight;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easygofly.api.customer.CustomerRepository;
import com.easygofly.entity.City;
import com.easygofly.entity.Customer;
import com.easygofly.entity.SearchHistory;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class SearchRestController {
	@Autowired CityRepository cityRepo;
	@Autowired CustomerRepository customerRepo;
	@Autowired SearchHistoryRepository historyRepo;


	@GetMapping("/api/flight/cities")
    public String cityList(HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        
		Iterable<City> cityList = cityRepo.findAll();
		List<String> strCityList = new ArrayList<String>();
		
		for (City city : cityList) {

	        String cityBody =  "{"
	        		+ "\"id\": " + city.getId() + ", "
	                + "\"name\": \"" + city.getName() + "\", "
	        		+ "\"code\": \"" + city.getCode() + "\", "
	        		+ "\"city_name\": \"" + city.getCityName() + "\", "
	        		+ "\"country_id\": " + city.getCountry().getId() + ""
	        		+ "}";
	        strCityList.add(cityBody);
		}

       	String arrayCityList = strCityList.stream().map(val -> String.valueOf(val)).collect(Collectors.joining(",", "[", "]"));
       	
        String responseBody = "{"
        		+ "\"code\": 0, "
        		+ "\"msg\": \"List of Cities.\", "
        		+ "\"data\": " + arrayCityList + ""
        		+ "}";

      return responseBody;
    }
	
	@PostMapping("/api/flight/view_history")
    public String flightHistory(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        UserHistory user = new ObjectMapper().readValue(request.getInputStream(), UserHistory.class);
        Customer existingUser = customerRepo.findById(user.user_id).get();
        
        List<SearchHistory> historyList = existingUser.getSearchHistory();
        List<SearchHistory> historyThree = new ArrayList<SearchHistory>();
        if (historyList.size() > 0) {
            historyThree.add(historyList.get(historyList.size() - 1));
        } else if (historyList.size() > 1) {
            historyThree.add(historyList.get(historyList.size() - 2));
		} else if (historyList.size() > 2) {
	        historyThree.add(historyList.get(historyList.size() - 3));
		}
        
		List<String> strHistoryList = new ArrayList<String>();
		
		for (SearchHistory history : historyThree) {

	        String historyBody =  "{"
	        		+ "\"id\": " + history.getId() + ", "
	                + "\"adult_num\": " + history.getAdultNum() + ", "
	        		+ "\"child_num\": " + history.getChildNum() + ", "
	        		+ "\"infant_num\": " + history.getInfantNum() + ", "
	    	        + "\"journey_class\": \"" + history.getJourneyClass() + "\", "
	    	    	+ "\"city_one\": \"" + history.getCityOne() + "\", "
	    	    	+ "\"city_two\": \"" + history.getCityTwo() + "\", "
	    	    	+ "\"date\": \"" + history.getDate() + "\", "
	    	    	+ "\"return_date\": \"" + history.getReturnDate() + "\", "
	    	    	+ "\"trip_type\": \"" + history.getTripType() + "\", "
	        		+ "\"user_id\": " + existingUser.getId() + ""
	        		+ "}";
	        strHistoryList.add(historyBody);
		}

       	String arrayHistoryList = strHistoryList.stream().map(val -> String.valueOf(val)).collect(Collectors.joining(",", "[", "]"));
       	
        String responseBody = "{"
        		+ "\"code\": 0, "
        		+ "\"msg\": \"List of Flight Search Result.\", "
        		+ "\"data\": " + arrayHistoryList + ""
        		+ "}";

      return responseBody;
    }
	

    private static class UserHistory {
        private Integer user_id;

		@SuppressWarnings("unused")
		public Integer getUser_id() {
			return user_id;
		}

		@SuppressWarnings("unused")
		public void setUser_id(Integer user_id) {
			this.user_id = user_id;
		}
        
        
    }
}
