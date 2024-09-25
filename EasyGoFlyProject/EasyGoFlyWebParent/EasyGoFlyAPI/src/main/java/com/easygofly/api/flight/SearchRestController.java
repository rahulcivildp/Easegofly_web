package com.easygofly.api.flight;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easygofly.api.customer.CustomerRepository;
import com.easygofly.entity.City;
import com.easygofly.entity.Customer;
import com.easygofly.entity.ProductDetail;
import com.easygofly.entity.SearchHistory;
import com.easygofly.entity.Stop;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class SearchRestController {
	@Autowired private CityRepository cityRepo;
	@Autowired private CustomerRepository customerRepo;
	@Autowired private SearchHistoryRepository historyRepo;
	@Autowired private ProductDetailCrudRepository flightRepo;


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
        		+ "\"msg\": \"Show Flight Search History\", "
        		+ "\"data\": " + arrayHistoryList + ""
        		+ "}";

      return responseBody;
    }
	
	@PostMapping("/api/flight/save_history")
    public String flightHistorySave(HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
        response.setContentType("application/json");

        UserHistorySave historySave = new ObjectMapper().readValue(request.getInputStream(), UserHistorySave.class);
        Customer existingUser = customerRepo.findById(historySave.user_id).get();
        
        Integer totalPax = historySave.adultCount + historySave.childCount + historySave.infantCount;
        Date origin = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(historySave.preferredDepartureTime);
        Date destination = null;
        if (historySave.preferredDepartureTimeReturn != null) {
            destination = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(historySave.preferredDepartureTimeReturn);
		}
        
        SearchHistory newSearch = new SearchHistory(historySave.origin, historySave.destination, totalPax, historySave.journey_class, historySave.adultCount, historySave.childCount, historySave.infantCount, historySave.trip_type, origin, destination, existingUser);
        
        SearchHistory savedHistory = historyRepo.save(newSearch);
        
		String historyBody =  "{"
	        		+ "\"id\": " + savedHistory.getId() + ", "
	                + "\"adult_num\": " + savedHistory.getAdultNum() + ", "
	        		+ "\"child_num\": " + savedHistory.getChildNum() + ", "
	        		+ "\"infant_num\": " + savedHistory.getInfantNum() + ", "
	    	        + "\"journey_class\": \"" + savedHistory.getJourneyClass() + "\", "
	    	    	+ "\"city_one\": \"" + savedHistory.getCityOne() + "\", "
	    	    	+ "\"city_two\": \"" + savedHistory.getCityTwo() + "\", "
	    	    	+ "\"date\": \"" + savedHistory.getDate() + "\", "
	    	    	+ "\"return_date\": \"" + savedHistory.getReturnDate() + "\", "
	    	    	+ "\"trip_type\": \"" + savedHistory.getTripType() + "\", "
	        		+ "\"user_id\": " + existingUser.getId() + ""
	        		+ "}";
		
        String responseBody = "{"
        		+ "\"code\": 0, "
        		+ "\"msg\": \"Flight History Save.\", "
        		+ "\"data\": " + historyBody + ""
        		+ "}";

      return responseBody;
    }
	
	@PostMapping("/api/flight/search/oneway")
    public String flightOnewaySearch(HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
        response.setContentType("application/json");

        SearchOnewayOfflineRequest searchOneway = new ObjectMapper().readValue(request.getInputStream(), SearchOnewayOfflineRequest.class);
        
        if (searchOneway.journey_class.equals("1")) {
			searchOneway.setJourney_class("Economy");
		}
		Integer passengerNum = searchOneway.adultCount + searchOneway.childCount + searchOneway.infantCount;

        Date origin = new SimpleDateFormat("yyyy-MM-dd").parse(searchOneway.preferredDepartureTime);
        System.out.println(origin);
        
		List<ProductDetail> flightListOfflineRepo = flightRepo.findFlights(searchOneway.origin, searchOneway.destination, searchOneway.journey_class, searchOneway.trace_id, searchOneway.mode, origin, Sort.by("id").ascending());
		List<ProductDetail> flightListOffline = new ArrayList<ProductDetail>();
		
		for (ProductDetail productDetail : flightListOfflineRepo) {
			if (passengerNum <= Integer.parseInt(productDetail.getTotalSeats())) {
				flightListOffline.add(productDetail);
			}
		}
		
		List<String> strFlightList = new ArrayList<String>();
		
		for (ProductDetail offlineFlight : flightListOffline) {
			List<String> strStopList = new ArrayList<String>();
			
			for (Stop stop : offlineFlight.getStops()) {
				
				String stopBody =  "{"
		        		+ "\"id\": " + stop.getId() + ", "
		                + "\"city_name\": \"" + stop.getCityName() + "\", "
		        		+ "\"dep_time\": \"" + stop.getDepTime() + "\", "
		        		+ "\"arr_time\": \"" + stop.getArrTime() + "\", "
		    	        + "\"total_time\": \"" + stop.getTotalTime() + "\""
		        		+ "}";
				
				strStopList.add(stopBody);
			}
			
	       	String arrayStopList = strStopList.stream().map(val -> String.valueOf(val)).collect(Collectors.joining(",", "[", "]"));

	        String flightBody =  "{"
	        		+ "\"id\": " + offlineFlight.getId() + ", "
	                + "\"date\": \"" + offlineFlight.getDate() + "\", "
	        		+ "\"city_one\": \"" + offlineFlight.getCityOne() + "\", "
	        		+ "\"city_two\": \"" + offlineFlight.getCityTwo() + "\", "
	    	        + "\"arr_time\": \"" + offlineFlight.getArrTime() + "\", "
	    	    	+ "\"dep_time\": \"" + offlineFlight.getDepTime() + "\", "
	    	    	+ "\"duration\": " + offlineFlight.getDuration() + ", "
	    	    	+ "\"flight_num\": \"" + offlineFlight.getFlightNum() + "\", "
	    	    	+ "\"priceadt\": " + (int)offlineFlight.getPriceADT() + ", "
	    	    	+ "\"priceinf\": " + (int)offlineFlight.getPriceINF() + ", "
	        		+ "\"markupadt\": " + (int)offlineFlight.getMarkupADT() + ", "
	        		+ "\"markupinf\": " + (int)offlineFlight.getMarkupINF() + ", "
	    	        + "\"pnr\": \"" + offlineFlight.getPnr() + "\", "
	    	    	+ "\"stop_num\": " + offlineFlight.getStopNum() + ", "
	    	    	+ "\"total_seats\": \"" + offlineFlight.getTotalSeats() + "\", "
	    	    	+ "\"upload_seats\": \"" + offlineFlight.getUploadSeats() + "\", "
	    	    	+ "\"terminal_arr\": \"" + offlineFlight.getTerminalArr() + "\", "
	    	    	+ "\"terminal_dep\": \"" + offlineFlight.getTerminalDep() + "\", "
	    	    	+ "\"baggage\": \"" + offlineFlight.getBaggage() + "\", "
	    	    	+ "\"cabin_baggage\": \"" + offlineFlight.getCabinBaggage() + "\", "
	    	    	+ "\"craft_type\": \"" + offlineFlight.getCraftType() + "\", "
	        		+ "\"brand\": \"" + offlineFlight.getBrand() + "\", "
	    	        + "\"stops\": " + arrayStopList + ""
	        		+ "}";
	        strFlightList.add(flightBody);
		}

       	String arrayFlightList = strFlightList.stream().map(val -> String.valueOf(val)).collect(Collectors.joining(",", "[", "]"));

        String responseBody = "{"
        		+ "\"code\": 0, "
        		+ "\"msg\": \"List of Flight Search Result.\", "
        		+ "\"data\": " + arrayFlightList + ""
        		+ "}";
        

      return responseBody;
    }
	
	// Static POJO List
	
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
    
    @SuppressWarnings("unused")
	private static class UserHistorySave {
        private Integer adultCount;
        private Integer childCount;
        private Integer infantCount;
        private String journey_class;
        private String origin;
        private String destination;
        private String preferredDepartureTime;
        private String preferredDepartureTimeReturn;
        private String trip_type;
        private Integer user_id;
		public Integer getAdultCount() {
			return adultCount;
		}
		public void setAdultCount(Integer adultCount) {
			this.adultCount = adultCount;
		}
		public Integer getChildCount() {
			return childCount;
		}
		public void setChildCount(Integer childCount) {
			this.childCount = childCount;
		}
		public Integer getInfantCount() {
			return infantCount;
		}
		public void setInfantCount(Integer infantCount) {
			this.infantCount = infantCount;
		}
		public String getJourney_class() {
			return journey_class;
		}
		public void setJourney_class(String journey_class) {
			this.journey_class = journey_class;
		}
		public String getOrigin() {
			return origin;
		}
		public void setOrigin(String origin) {
			this.origin = origin;
		}
		public String getDestination() {
			return destination;
		}
		public void setDestination(String destination) {
			this.destination = destination;
		}
		public String getPreferredDepartureTime() {
			return preferredDepartureTime;
		}
		public void setPreferredDepartureTime(String preferredDepartureTime) {
			this.preferredDepartureTime = preferredDepartureTime;
		}
		public String getPreferredDepartureTimeReturn() {
			return preferredDepartureTimeReturn;
		}
		public void setPreferredDepartureTimeReturn(String preferredDepartureTimeReturn) {
			this.preferredDepartureTimeReturn = preferredDepartureTimeReturn;
		}
		public String getTrip_type() {
			return trip_type;
		}
		public void setTrip_type(String trip_type) {
			this.trip_type = trip_type;
		}
		public Integer getUser_id() {
			return user_id;
		}
		public void setUser_id(Integer user_id) {
			this.user_id = user_id;
		}

		
    }
    
    @SuppressWarnings("unused")
	private static class SearchOnewayOfflineRequest {
    	private Integer adultCount;
        private Integer childCount;
        private Integer infantCount;
        private String journey_class;
        private String origin;
        private String destination;
        private String preferredDepartureTime;
        private String trace_id;
        private String mode;
        
		public Integer getAdultCount() {
			return adultCount;
		}
		public void setAdultCount(Integer adultCount) {
			this.adultCount = adultCount;
		}
		public Integer getChildCount() {
			return childCount;
		}
		public void setChildCount(Integer childCount) {
			this.childCount = childCount;
		}
		public Integer getInfantCount() {
			return infantCount;
		}
		public void setInfantCount(Integer infantCount) {
			this.infantCount = infantCount;
		}
		public String getJourney_class() {
			return journey_class;
		}
		public void setJourney_class(String journey_class) {
			this.journey_class = journey_class;
		}
		public String getOrigin() {
			return origin;
		}
		public void setOrigin(String origin) {
			this.origin = origin;
		}
		public String getDestination() {
			return destination;
		}
		public void setDestination(String destination) {
			this.destination = destination;
		}
		public String getPreferredDepartureTime() {
			return preferredDepartureTime;
		}
		public void setPreferredDepartureTime(String preferredDepartureTime) {
			this.preferredDepartureTime = preferredDepartureTime;
		}
		public String getTrace_id() {
			return trace_id;
		}
		public void setTrace_id(String trace_id) {
			this.trace_id = trace_id;
		}
		public String getMode() {
			return mode;
		}
		public void setMode(String mode) {
			this.mode = mode;
		}
	}
    
    
}
