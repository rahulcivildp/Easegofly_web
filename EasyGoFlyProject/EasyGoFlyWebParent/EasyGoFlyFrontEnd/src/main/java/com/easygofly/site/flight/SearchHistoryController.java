package com.easygofly.site.flight;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.easygofly.entity.Brand;
import com.easygofly.entity.City;
import com.easygofly.entity.Country;
import com.easygofly.entity.Customer;
import com.easygofly.entity.FlightMap;
import com.easygofly.entity.Product;
import com.easygofly.entity.ProductDetail;
import com.easygofly.entity.SearchHistory;
import com.easygofly.entity.exception.ProductNotFoundException;
import com.easygofly.site.LogService;
import com.easygofly.site.customer.CustomerService;
import com.easygofly.site.security.EasegoflyPhoneCustomerDetails;
import com.easygofly.site.security.oauth.CustomerOAuth2User;
import com.easygofly.site.setting.CountryRepository;

@Controller
public class SearchHistoryController {

	@Autowired private SearchHistoryService searchService;
	@Autowired private CustomerService customerService;
	@Autowired private SearchHistoryRepository searchRepo;
	@Autowired private ProductDetailsRepository productRepo;
	@Autowired private CityRepository cityRepo;
	@Autowired private CountryRepository countryRepo;
	@Autowired private ProductDetailsController pController;
	@Autowired private ProductDetailService productService;
	@Autowired private OnlineFlightService onlineFlightService;
	@Autowired private LogService logService;
	@Autowired private BrandRepositoy brandRepo;
	
	public String searchURL = "";
	public String searchReturnURL = "";
	private List<Object> searchObj = new ArrayList<>();
	public FlightHistory history = new FlightHistory();
	Integer pageNum = 0;
	
	public List<ProductDetail> listProductDetails;
	public List<ProductDetail> listProductDetailsInSearch = new ArrayList<ProductDetail>();
	public List<ProductDetail> listProductDetailsInSearchReturn = new ArrayList<ProductDetail>();
	public List<ProductDetail> listProductDetailsOnline;
	public List<ProductDetail> listProductDetailsOnlineReturn;
	public List<FlightMap> flightMaps;

	@GetMapping("/flight_search_")
	public String searchFlightDetailsSinglesNoUser(
			Model model, RedirectAttributes redirectAttributes) throws IllegalArgumentException, Exception {

		Integer iq = 111;
		
		listProductDetailsInSearch = new ArrayList<>();
	    Date currentDate = new Date();
	    Calendar c = Calendar.getInstance();
	    Calendar ca = Calendar.getInstance();
	    ca.setTime(currentDate);
	    System.out.println(history.cityOne);
	    
	    City cityOneFound = cityRepo.getCityByName(history.getCityOne());
	    City cityTwoFound = cityRepo.getCityByName(history.getCityTwo());

		List<Product> getProductBrand = productRepo.findProductByCity(cityOneFound.getCode(), cityTwoFound.getCode(), Sort.by("name").ascending());
		
		Integer passengerNum = history.getAdultNum() + history.getChildNum() + history.getInfantNum();
		Country country = countryRepo.findById(106).get();
		Iterable<City> cities = cityRepo.getCityByCountry(country);
		Iterable<Brand> brands = brandRepo.findAll();
		
		model.addAttribute("searchId", history.getId());
		model.addAttribute("value_int", iq);
		model.addAttribute("cities", cities);
		model.addAttribute("brands", brands);
		model.addAttribute("getProductBrand", getProductBrand);
		model.addAttribute("cityOne", cityOneFound.getCode());
		model.addAttribute("cityTwo", cityTwoFound.getCode());
		model.addAttribute("cityOneName", history.getCityOne());
		model.addAttribute("cityTwoName", history.getCityTwo());
		model.addAttribute("date", history.getDate());
		model.addAttribute("strDate", history.getDate());
		model.addAttribute("journeyClass", history.getJourneyClass());
		model.addAttribute("tripType", history.getTripType());
		model.addAttribute("adultNum", history.getAdultNum());
		model.addAttribute("childNum", history.getChildNum());
		model.addAttribute("infantNum", history.getInfantNum());
		model.addAttribute("passengerNum", passengerNum);
		model.addAttribute("sortName", history.getSort());
		model.addAttribute("currentDay", ca.get(Calendar.DAY_OF_YEAR));
		model.addAttribute("searchedDay", c.get(Calendar.DAY_OF_YEAR));
		
		
		model.addAttribute("listFlight", new ArrayList<>());
		
		List<SortName> listSortName = new ArrayList<SortName>();
		listSortName.add(new SortName("priceADT", "Price"));
		listSortName.add(new SortName("arrTimeInteger", "Arrival"));
		listSortName.add(new SortName("depTimeInteger", "Departure"));
		listSortName.add(new SortName("brand", "Brand"));
		listSortName.add(new SortName("duration", "Duration"));
		model.addAttribute("listSortName", listSortName);

		return "flight/search/search-result-noUser";
		
	}

	@GetMapping("/flight_search_save")	
	public String searchHistorySave(@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer,
			@AuthenticationPrincipal CustomerOAuth2User googleLogin, 
			@RequestParam(name = "cityOne", required = false) String cityOne, 
			@RequestParam(name = "cityTwo", required = false) String cityTwo, 
			@RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date date, 
			@RequestParam(name = "return_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date returnDate, 
			@RequestParam(name = "journeyClass", required = false) String journeyClass,
			@RequestParam(name = "adultNum", required = false) Integer adultNum,
			@RequestParam(name = "childNum", required = false) Integer childNum,
			@RequestParam(name = "infantNum", required = false) Integer infantNum,
			Model model) throws IllegalArgumentException, Exception {
		 
			Customer customer;
			City city1 = cityRepo.getCityByName(cityOne);
		    City city2 = cityRepo.getCityByName(cityTwo);
		    String strDateReturn = "";
		    String tripType = "oneWay";
		    Integer totalPax = adultNum + childNum + infantNum;
		    
		    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
		    String strDate = dateFormat.format(date);
		    System.out.println(date);
		    System.out.println("Return - " + returnDate);
		    String sort = "priceADT";
		    double searchId = 0;
		
		    if (returnDate != null) {
			    strDateReturn = dateFormat.format(returnDate);
			    tripType = "twoWays";
			}
		    history = new FlightHistory(cityOne, cityTwo, totalPax, journeyClass, adultNum, childNum, infantNum, tripType, date, returnDate, sort, false);
		    
            if (loggedCustomer != null) {
				customer = customerService.getByPhone(loggedCustomer.getUsername());
				model.addAttribute("customer", customer);
				history.id = saveHistoryReturnPart(city1.getCode(), city2.getCode(), date, returnDate, journeyClass, tripType, adultNum, childNum, infantNum, customer);
				
				
			} else if (googleLogin != null) {
				customer = customerService.getByEmail(googleLogin.getEmail());
				model.addAttribute("customer", customer);
				history.id = saveHistoryReturnPart(city1.getCode(), city2.getCode(), date, returnDate, journeyClass, tripType, adultNum, childNum, infantNum, customer);
				
			} else {
				history.id = null;
			}
            
            System.out.println("History Static: " + history);
            
            if (returnDate != null) {

				return "redirect:/flight_search/return_" + searchId + "_" + city1.getCode() + "_" + city2.getCode() +"_"+ journeyClass +"_"+ tripType +"_"+ adultNum 
						+"_"+ childNum +"_"+ infantNum +"_"+ strDate +"_"+ strDateReturn +"_" + sort;
			} else {
				return "redirect:/flight_search_";
			}
	}
	

	public Integer saveHistoryPart(String cityOne, String cityTwo, Date date, String journeyClass, String tripType,
			Integer adultNum, Integer childNum, Integer infantNum, Customer customer) {
		Integer totalPassenger = adultNum + childNum + infantNum;
		
		ProductSaveHelper.setSearchHistory(customer, cityOne, cityTwo, totalPassenger, journeyClass, adultNum, childNum, infantNum, tripType, date);
		
		searchService.saveSearchHistory(customer);
		Customer savedSearch = searchService.saveSearchHistory(customer);
		
		
		List<SearchHistory> savedSearchResult = savedSearch.getSearchHistory();
		SearchHistory lastValue = savedSearchResult.get(savedSearchResult.size() - 1);
		
		Integer searchId = lastValue.getId();
		return searchId;
	}
	
	public Integer saveHistoryPartWithouLogin(String cityOne, String cityTwo, Date date, Date returnDate, String journeyClass, String tripType,
			Integer adultNum, Integer childNum, Integer infantNum) {
		Integer totalPassenger = adultNum + childNum + infantNum;
		
		SearchHistory lastValue = searchService.setSearchHistoryWithouLogin(cityOne, cityTwo, totalPassenger, journeyClass, adultNum, childNum, infantNum, tripType, date, returnDate);
		
		Integer searchId = lastValue.getId();
		return searchId;
	}

	public String filterFlight() {
		
		return searchReturnURL;
	}
	


	
	//Previous day and next day
	
	@GetMapping("/get_previous_day_flight")
    public String getResultPreviousDay(@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer, @AuthenticationPrincipal CustomerOAuth2User oauthCustomer,
			@AuthenticationPrincipal CustomerOAuth2User googleLogin, 
			@RequestParam(name = "search_id") String searchId, 
			@RequestParam(name = "cityOne", required = false) String cityOne, 
			@RequestParam(name = "cityTwo", required = false) String cityTwo, 
			@RequestParam(name = "date", required = false) String stringDate, 
			@RequestParam(name = "journeyClass", required = false) String journeyClass,
			@RequestParam(name = "tripType", required = false) String tripType,
			@RequestParam(name = "adultNum", required = false) Integer adultNum,
			@RequestParam(name = "childNum", required = false) Integer childNum,
			@RequestParam(name = "infantNum", required = false) Integer infantNum,
			Model model) throws ProductNotFoundException, ParseException {
		 
			Customer customer;

		    City city1 = cityRepo.getCityByCode(cityOne);
		    City city2 = cityRepo.getCityByCode(cityTwo);

		    Date date = new SimpleDateFormat("yyyy-MM-dd").parse(stringDate);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			c.add(Calendar.DATE, -1);  // number of days to add
		    String strDate = sdf.format(c.getTime());
		    String sort = "pnr";
		    String brand = "";
		    double searchIddbl = Double.parseDouble(searchId);
		    
			searchObj = new ArrayList<>();
			searchObj.add(city1.getCode());
			searchObj.add(city2.getCode());
			searchObj.add(adultNum);
			searchObj.add(childNum);
			searchObj.add(infantNum);
			searchObj.add(sort);
			searchObj.add(c.getTime());
			searchObj.add(onlineFlightService.tokenAirIQ);
		    
            if (loggedCustomer != null) {
				customer = customerService.getByPhone(loggedCustomer.getUsername());
				model.addAttribute("customer", customer);
				searchIddbl = saveHistoryPart(city1.getCode(), city2.getCode(), date, journeyClass, tripType, adultNum, childNum,
						infantNum, customer);
				
			} else if (oauthCustomer != null) {
				customer = customerService.getByEmail(oauthCustomer.getEmail());
				model.addAttribute("customer", customer);
				searchIddbl = saveHistoryPart(city1.getCode(), city2.getCode(), date, journeyClass, tripType, adultNum, childNum,
						infantNum, customer);
				
			} else {
				searchIddbl = 1.5;
			}
            
			searchURL = "/flight_search_"+ searchIddbl +"_"+ city1.getCode() +"_"+ city2.getCode() +"_"+ journeyClass +"_"+ tripType +"_"+ adultNum 
					+"_"+ childNum +"_"+ infantNum +"_"+ strDate +"_"+ sort +"_"+ brand;
			return "redirect:/loading_";
    }

	@GetMapping("/get_next_day_flight")
    public String getResultNextDay(@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer, @AuthenticationPrincipal CustomerOAuth2User oauthCustomer,
			@AuthenticationPrincipal CustomerOAuth2User googleLogin, 
			@RequestParam(name = "search_id") String searchId, 
			@RequestParam(name = "cityOne", required = false) String cityOne, 
			@RequestParam(name = "cityTwo", required = false) String cityTwo, 
			@RequestParam(name = "date", required = false) String stringDate, 
			@RequestParam(name = "journeyClass", required = false) String journeyClass,
			@RequestParam(name = "tripType", required = false) String tripType,
			@RequestParam(name = "adultNum", required = false) Integer adultNum,
			@RequestParam(name = "childNum", required = false) Integer childNum,
			@RequestParam(name = "infantNum", required = false) Integer infantNum,
			Model model) throws ProductNotFoundException, ParseException {
		 
			Customer customer;
		    City city1 = cityRepo.getCityByCode(cityOne);
		    City city2 = cityRepo.getCityByCode(cityTwo);

		    Date date = new SimpleDateFormat("yyyy-MM-dd").parse(stringDate);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			c.add(Calendar.DATE, 1);  // number of days to add
		    String strDate = sdf.format(c.getTime());
		    String sort = "pnr";
		    String brand = "";
		    Integer stop = 0;
		    String activeTime = "active";
		    String arrayPrice = "0,0";
		    double searchIddbl = Double.parseDouble(searchId);
		    

			searchObj = new ArrayList<>();
			searchObj.add(city1.getCode());
			searchObj.add(city2.getCode());
			searchObj.add(adultNum);
			searchObj.add(childNum);
			searchObj.add(infantNum);
			searchObj.add(sort);
			searchObj.add(c.getTime());
			searchObj.add(onlineFlightService.tokenAirIQ);
		    
            if (loggedCustomer != null) {
				customer = customerService.getByPhone(loggedCustomer.getUsername());
				model.addAttribute("customer", customer);
				searchIddbl = saveHistoryPart(city1.getCode(), city2.getCode(), date, journeyClass, tripType, adultNum, childNum,
						infantNum, customer);

			} else if (oauthCustomer != null) {
				customer = customerService.getByEmail(oauthCustomer.getEmail());
				model.addAttribute("customer", customer);
				searchIddbl = saveHistoryPart(city1.getCode(), city2.getCode(), date, journeyClass, tripType, adultNum, childNum,
						infantNum, customer);

				customer = customerService.getByEmail(oauthCustomer.getEmail());
			} else {
				searchIddbl = 1.5;
			}
            
			searchURL = "/flight_search_"+ searchIddbl +"_"+ city1.getCode() +"_"+ city2.getCode() +"_"+ journeyClass +"_"+ tripType +"_"+ adultNum 
					+"_"+ childNum +"_"+ infantNum +"_"+ strDate +"_"+ sort +"_"+ brand +"_"+ stop +"_"+ arrayPrice +"_"+ activeTime;
			return "redirect:/loading_";
    }
	
		

	
	
	////Flight return segment.
	
	@GetMapping("/flight_search/return_{historyId}_{cityOne}_{cityTwo}_{journeyClass}_{tripType}_{adultNum}_{childNum}_{infantNum}_{strDate}_{strReturnDate}_{sortName}")
	public String searchFlightDetailsReturn(
			@PathVariable(name = "historyId") String searchId,
			@PathVariable(name = "cityOne") String cityOne,
			@PathVariable(name = "cityTwo") String cityTwo,
			@PathVariable(name = "journeyClass") String journeyClass,
			@PathVariable(name = "tripType") String tripType,
			@PathVariable(name = "adultNum") String adultNum,
			@PathVariable(name = "childNum") String childNum,
			@PathVariable(name = "infantNum") String infantNum,
			@PathVariable(name = "strDate") String strDate,
			@PathVariable(name = "strReturnDate") String strReturnDate,
			@PathVariable(name = "sortName") String sortName,
			Model model, RedirectAttributes redirectAttributes) throws ParseException, IOException {

		String[] arrSearchId = searchId.split("_");
		double decimal = Double.parseDouble(arrSearchId[0]);
		SearchHistory search = searchRepo.findById((int)decimal).get();
		
	    City cityOneFound = cityRepo.getCityByCode(cityOne);
	    City cityTwoFound = cityRepo.getCityByCode(cityTwo);
		
	    Date date = new SimpleDateFormat("yyyy-MM-dd").parse(strDate);
	    Date returnDate = new SimpleDateFormat("yyyy-MM-dd").parse(strReturnDate);

		List<Product> getProductBrand = productRepo.findProductByCity(cityOne, cityTwo, Sort.by("name").ascending());
		
		Integer passengerNum = Integer.parseInt(adultNum) + Integer.parseInt(childNum) + Integer.parseInt(infantNum);
		
		Country country = countryRepo.findById(106).get();
		Iterable<City> cities = cityRepo.getCityByCountry(country);
		
		System.out.println(date);
		System.out.println(strDate);

		model.addAttribute("flightMaps", flightMaps);
		model.addAttribute("cities", cities);
		model.addAttribute("getProductBrand", getProductBrand);
		model.addAttribute("cityOne", cityOne);
		model.addAttribute("cityTwo", cityTwo);
		model.addAttribute("cityOneName", cityOneFound.getCityName());
		model.addAttribute("cityTwoName", cityTwoFound.getCityName());
		model.addAttribute("date", date);
		model.addAttribute("returnDate", returnDate);
		model.addAttribute("strReturnDate", strReturnDate);
		model.addAttribute("strDate", strDate);
		model.addAttribute("journeyClass", journeyClass);
		model.addAttribute("tripType", tripType);
		model.addAttribute("adultNum", adultNum);
		model.addAttribute("childNum", childNum);
		model.addAttribute("infantNum", infantNum);
		model.addAttribute("passengerNum", passengerNum);
		model.addAttribute("search", search);

		model.addAttribute("listFlight", listProductDetailsInSearch);
		model.addAttribute("listFlightReturn", listProductDetailsOnlineReturn);

		System.out.println(flightMaps.size() + " size");
		System.out.println(listProductDetailsOnlineReturn.size() + " size");
		
		List<SortName> listSortName = new ArrayList<SortName>();
		listSortName.add(new SortName("priceADT", "Price"));
		listSortName.add(new SortName("arrTimeInteger", "Arrival"));
		listSortName.add(new SortName("depTimeInteger", "Departure"));
		listSortName.add(new SortName("brand", "Brand"));
		listSortName.add(new SortName("duration", "Duration"));
		model.addAttribute("listSortName", listSortName);
		

		
		return "flight/search_return/search-result-noUser_return";
		
	}

	@GetMapping("/loading_return_")
    public String performApiRequestReturn(Model model) {
        model.addAttribute("searchURL", searchReturnURL);
        return "loading/loading";
    }

	public String[] searchReturnFlightAPI(String cityOne, String cityTwo, Integer adultNum, Integer childNum, Integer infantNum, String sortName, Date date, 
			Date returnDate, List<ProductDetail> listProduct, List<ProductDetail> listProductReturn) throws MalformedURLException, IOException {
		String[] hasErrorArr = new String[2];
		
		listProductDetailsOnline = new ArrayList<ProductDetail>(); 
		listProductDetailsInSearch = new ArrayList<ProductDetail>(); 
		listProductDetailsOnlineReturn = new ArrayList<ProductDetail>(); 
		listProductDetailsInSearchReturn = new ArrayList<ProductDetail>(); 
        flightMaps = new ArrayList<FlightMap>();
        
		for (int i = 0; i < listProduct.size(); i++) {
			FlightMap flightMap = new FlightMap();
			flightMap.setFlightIdOne(listProduct.get(i).getId());
			listProductDetailsOnline.add(listProduct.get(i));
			listProductDetailsInSearch.add(listProduct.get(i));
			
			if (listProductReturn.size() != 0) {
				try {
					flightMap.setFlightIdTwo(listProductReturn.get(i).getId());
					listProductDetailsOnlineReturn.add(listProductReturn.get(i));
					listProductDetailsInSearchReturn.add(listProductReturn.get(i));
					flightMaps.add(flightMap);
					
				} catch (Exception e) {
					e.printStackTrace();
					// TODO: handle exception
				}
			}
		}
		
		try {
			// Create URL object with the API end-point
	        StringBuilder responseBodySearch = onlineFlightService.apiOnlineSearchModReturn(cityOne, cityTwo, adultNum, childNum, infantNum, date, returnDate);
	        
	        JSONObject jsonObjSearch = new JSONObject(responseBodySearch.toString());
	        System.out.println(jsonObjSearch);
	        logService.generateLog(jsonObjSearch.toString());
	        try {
	        	JSONArray jsonArrays = jsonObjSearch.getJSONObject("Response").getJSONArray("Results").getJSONArray(0);
	            JSONArray jsonObjSegment = new JSONArray();
	    		JSONObject mainObj = new JSONObject();
	    		JSONObject mainObjSegment = new JSONObject();
	    		JSONObject mainObjOrigin = new JSONObject();
	    		JSONObject mainObjDestination = new JSONObject();
	    		JSONObject mainObjAirline = new JSONObject();
	    		JSONObject mainObjFare = new JSONObject();
	    		JSONArray jsonArrayFareBreakdown = new JSONArray();
	           
	    		pController.traceId = jsonObjSearch.getJSONObject("Response").get("TraceId").toString();
	            
	    		for (int i = 0; i < jsonArrays.length(); i++) {
	    			JSONObject mainObjOriginList = new JSONObject();
	    			JSONObject mainObjDestinationList = new JSONObject();
	    			
	    	        mainObj.put("Result-" + i, jsonArrays.getJSONObject(i));
	    	        jsonObjSegment.put(mainObj.getJSONObject("Result-" + i).getJSONArray("Segments").getJSONArray(0));
	    	        mainObjSegment.put("Segment-" + i, mainObj.getJSONObject("Result-" + i).getJSONArray("Segments").getJSONArray(0));
	    	        mainObjFare.put("Fare-" + i, mainObj.getJSONObject("Result-" + i).getJSONObject("Fare"));
	    	        jsonArrayFareBreakdown.put(mainObj.getJSONObject("Result-" + i).getJSONArray("FareBreakdown"));
	    	        mainObjOrigin.put("Origin-" + i, mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(0).getJSONObject("Origin"));
	    	        mainObjDestination.put("Destination-" + i, mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(0).getJSONObject("Destination"));
	    	        mainObjAirline.put("Airline-" + i, mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(0).getJSONObject("Airline"));
	    	        
	    	        Integer innerSegmentArrayLength = mainObj.getJSONObject("Result-" + i).getJSONArray("Segments").getJSONArray(0).length();
	    	        Integer stopNumber = innerSegmentArrayLength - 1;
	    	        @SuppressWarnings("unused")
					String depAirportCode = "",depAirportName = "", depTerminal = "", depTime = "";
	    	        @SuppressWarnings("unused")
					String arrAirportCode = "",arrAirportName = "", arrTerminal = "", arrTime = "";
	    	        for (int j = 0; j < innerSegmentArrayLength; j++) {
	    	        	if (j == 0) {
	    		        	mainObjOriginList.put("Origin-" + j, mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(j).getJSONObject("Origin"));
	    		        	depAirportCode = mainObjOriginList.getJSONObject("Origin-" + j).getJSONObject("Airport").get("AirportCode").toString();
	    		        	depAirportName = mainObjOriginList.getJSONObject("Origin-" + j).getJSONObject("Airport").get("AirportName").toString();
	    		        	depTerminal = mainObjOriginList.getJSONObject("Origin-" + j).getJSONObject("Airport").get("Terminal").toString();
	    		        	depTime = mainObjOriginList.getJSONObject("Origin-" + j).get("DepTime").toString();
	    		        	
	    					mainObjDestinationList.put("Destination-" + j, mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(j).getJSONObject("Destination"));
	    					arrAirportCode = mainObjDestinationList.getJSONObject("Destination-" + j).getJSONObject("Airport").get("AirportCode").toString();
	    				    arrAirportName = mainObjDestinationList.getJSONObject("Destination-" + j).getJSONObject("Airport").get("AirportName").toString();
	    				    arrTerminal = mainObjDestinationList.getJSONObject("Destination-" + j).getJSONObject("Airport").get("Terminal").toString();
	    				    arrTime = mainObjDestinationList.getJSONObject("Destination-" + j).get("ArrTime").toString();
	    				    
	    				} else if (j > 0) {
	    					mainObjDestinationList.put("Destination-" + j, mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(j).getJSONObject("Destination"));
	    					arrAirportCode = mainObjDestinationList.getJSONObject("Destination-" + j).getJSONObject("Airport").get("AirportCode").toString();
	    				    arrAirportName = mainObjDestinationList.getJSONObject("Destination-" + j).getJSONObject("Airport").get("AirportName").toString();
	    				    arrTerminal = mainObjDestinationList.getJSONObject("Destination-" + j).getJSONObject("Airport").get("Terminal").toString();
	    				    arrTime = mainObjDestinationList.getJSONObject("Destination-" + j).get("ArrTime").toString();
	    				    
	    				}
	    			}

	    	        String airlineName = mainObjAirline.getJSONObject("Airline-" + i).get("AirlineName").toString();
	    	        @SuppressWarnings("unused")
					String fareClass = mainObjAirline.getJSONObject("Airline-" + i).get("FareClass").toString();
	    	        String flightNumber = mainObjAirline.getJSONObject("Airline-" + i).get("AirlineCode").toString() + "-" + mainObjAirline.getJSONObject("Airline-" + i).get("FlightNumber").toString();
	    	        
	    	        String[] departureTimeParts = depTime.split("T");
	    			String[] departureTimeInnerParts = departureTimeParts[1].split(":");
	    			String stringDepTime = departureTimeInnerParts[0] + ":" + departureTimeInnerParts[1];
	    			String depTimeString = departureTimeInnerParts[0] + "." + departureTimeInnerParts[1].charAt(0);
	    			Float depTimeFloat = Float.parseFloat(depTimeString);
	    			
	    			String[] arrivalTimeParts = arrTime.split("T");
	    			String[] arrivalTimeInnerParts = arrivalTimeParts[1].split(":");
	    			String stringArrTime = arrivalTimeInnerParts[0] + ":" + arrivalTimeInnerParts[1];
	    			String arrTimeString = arrivalTimeInnerParts[0] + "." + arrivalTimeInnerParts[1].charAt(0);
	    			Float arrTimeFloat = Float.parseFloat(arrTimeString);
	    			
	    			JSONObject jsonObjectAdult = new JSONObject();
	    			Integer adultPrice = 0, childPrice = 0, infantPrice = 0, adultTax = 0, childTax = 0, infantTax = 0;
	    			Integer intTotalAdultChildPrice = 0;
	    			
	    			for (int j = 0; j < jsonArrayFareBreakdown.getJSONArray(i).length(); j++) {
	    				jsonObjectAdult.put("FareAdult-" + j, jsonArrayFareBreakdown.getJSONArray(i).getJSONObject(j));
	    				String priceOnline = jsonObjectAdult.getJSONObject("FareAdult-" + j).get("BaseFare").toString();
	    				String taxOnline = jsonObjectAdult.getJSONObject("FareAdult-" + j).get("Tax").toString();
	    				String passengerTypeOnline = jsonObjectAdult.getJSONObject("FareAdult-" + j).get("PassengerType").toString();
	    				if (passengerTypeOnline.equals("3")) {
	    					infantPrice = Integer.parseInt(priceOnline) / infantNum;
	    					infantTax = Integer.parseInt(taxOnline) / infantNum;
	    				} else if (passengerTypeOnline.equals("2")) {
	    					childPrice = Integer.parseInt(priceOnline) / childNum;
	    					childTax = Integer.parseInt(taxOnline) / childNum;
	    				} else {
	    					adultPrice = Integer.parseInt(priceOnline) / adultNum;
	    					adultTax = Integer.parseInt(taxOnline) / adultNum;
	    				}
	    			}
	    			
	    			if (childPrice != 0) {
	    				intTotalAdultChildPrice = ((adultPrice + childPrice) / 2) + ((adultTax + childTax) / 2);
	    			} else {
	    				intTotalAdultChildPrice = adultPrice + adultTax;
	    			}

	    			Integer intTotalInfantPrice = infantPrice + infantTax;
	    			
	    			Integer duration = Integer.parseInt(mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(0).get("Duration").toString());
	    			@SuppressWarnings("unused")
					String flightStatus = mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(0).get("FlightStatus").toString();
	    			String noOfSeatAvailable = "";
	    			try {
	    				noOfSeatAvailable = mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(0).get("NoOfSeatAvailable").toString();
	    			} catch (JSONException e) {
	    				System.out.println(i);
	    				noOfSeatAvailable = "0";
	    			}

	    			String resultIndex = mainObj.getJSONObject("Result-" + i).get("ResultIndex").toString();
	    			String airlineRemark = mainObj.getJSONObject("Result-" + i).get("AirlineRemark").toString();
					
					
	    			
	    			String mode = "Online-data";
	    			
	    			String craftType = mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(0).get("Craft").toString();
	    			
	    			ProductDetail productDetail = new ProductDetail(i + 1, "waiting...", noOfSeatAvailable, noOfSeatAvailable, flightNumber, date, 
	                		stringDepTime, stringArrTime, intTotalAdultChildPrice, intTotalInfantPrice, 0, 0, depAirportCode, arrAirportCode, true, true, stopNumber, duration, 
	                		airlineName, depTimeFloat, arrTimeFloat, pController.traceId, resultIndex, airlineRemark, mode, "2", depTerminal, arrTerminal, 15, 7, "", "", null, craftType);
	    			
	    			listProductDetailsOnline.add(productDetail);
	    			
	    			listProductDetailsInSearch.add(productDetail);
	    		}
				
				JSONObject jsonObjTicketResponseError = jsonObjSearch.getJSONObject("Response").getJSONObject("Error");
				hasErrorArr[0] = jsonObjTicketResponseError.get("ErrorCode").toString();
				hasErrorArr[1] = jsonObjTicketResponseError.get("ErrorMessage").toString();
				
			} catch (Exception e) {
				JSONObject jsonObjTicketResponseError = jsonObjSearch.getJSONObject("Response").getJSONObject("Error");
				hasErrorArr[0] = jsonObjTicketResponseError.get("ErrorCode").toString();
				hasErrorArr[1] = jsonObjTicketResponseError.get("ErrorMessage").toString();
			}

			JSONArray jsonArraysReturn = new JSONArray();
			try {
				jsonArraysReturn = jsonObjSearch.getJSONObject("Response").getJSONArray("Results").getJSONArray(1);
				JSONObject mainObjReturn = new JSONObject();
				JSONObject mainObjSegmentReturn = new JSONObject();
				JSONObject mainObjOriginReturn = new JSONObject();
				JSONObject mainObjDestinationReturn = new JSONObject();
				JSONObject mainObjAirlineReturn = new JSONObject();
				JSONObject mainObjFareReturn = new JSONObject();
				JSONArray jsonArrayFareBreakdownReturn = new JSONArray();
		        JSONArray jsonObjSegmentReturn = new JSONArray();


//		        ProductDetail[] productDetailTwo = new ProductDetail[500];
		        
				for (int i = 0; i < jsonArraysReturn.length(); i++) {
					JSONObject mainObjOriginList = new JSONObject();
					JSONObject mainObjDestinationList = new JSONObject();
					
			        mainObjReturn.put("Result-" + i, jsonArraysReturn.getJSONObject(i));
			        jsonObjSegmentReturn.put(mainObjReturn.getJSONObject("Result-" + i).getJSONArray("Segments").getJSONArray(0));
			        mainObjSegmentReturn.put("Segment-" + i, mainObjReturn.getJSONObject("Result-" + i).getJSONArray("Segments").getJSONArray(0));
			        mainObjFareReturn.put("Fare-" + i, mainObjReturn.getJSONObject("Result-" + i).getJSONObject("Fare"));
			        jsonArrayFareBreakdownReturn.put(mainObjReturn.getJSONObject("Result-" + i).getJSONArray("FareBreakdown"));
			        mainObjOriginReturn.put("Origin-" + i, mainObjSegmentReturn.getJSONArray("Segment-" + i).getJSONObject(0).getJSONObject("Origin"));
			        mainObjDestinationReturn.put("Destination-" + i, mainObjSegmentReturn.getJSONArray("Segment-" + i).getJSONObject(0).getJSONObject("Destination"));
			        mainObjAirlineReturn.put("Airline-" + i, mainObjSegmentReturn.getJSONArray("Segment-" + i).getJSONObject(0).getJSONObject("Airline"));
			        
			        Integer innerSegmentArrayLength = mainObjReturn.getJSONObject("Result-" + i).getJSONArray("Segments").getJSONArray(0).length();
			        Integer stopNumber = innerSegmentArrayLength - 1;
			        @SuppressWarnings("unused")
					String depAirportCode = "",depAirportName = "", depTerminal = "", depTime = "";
			        @SuppressWarnings("unused")
					String arrAirportCode = "",arrAirportName = "", arrTerminal = "", arrTime = "", craftType = "";
			        for (int j = 0; j < innerSegmentArrayLength; j++) {
			        	if (j == 0) {
				        	mainObjOriginList.put("Origin-" + j, mainObjSegmentReturn.getJSONArray("Segment-" + i).getJSONObject(j).getJSONObject("Origin"));
				        	depAirportCode = mainObjOriginList.getJSONObject("Origin-" + j).getJSONObject("Airport").get("AirportCode").toString();
				        	depAirportName = mainObjOriginList.getJSONObject("Origin-" + j).getJSONObject("Airport").get("AirportName").toString();
				        	depTerminal = mainObjOriginList.getJSONObject("Origin-" + j).getJSONObject("Airport").get("Terminal").toString();
				        	depTime = mainObjOriginList.getJSONObject("Origin-" + j).get("DepTime").toString();
				        	
							mainObjDestinationList.put("Destination-" + j, mainObjSegmentReturn.getJSONArray("Segment-" + i).getJSONObject(j).getJSONObject("Destination"));
							arrAirportCode = mainObjDestinationList.getJSONObject("Destination-" + j).getJSONObject("Airport").get("AirportCode").toString();
						    arrAirportName = mainObjDestinationList.getJSONObject("Destination-" + j).getJSONObject("Airport").get("AirportName").toString();
						    arrTerminal = mainObjDestinationList.getJSONObject("Destination-" + j).getJSONObject("Airport").get("Terminal").toString();
						    arrTime = mainObjDestinationList.getJSONObject("Destination-" + j).get("ArrTime").toString();
						    
							craftType = mainObjSegmentReturn.getJSONArray("Segment-" + i).getJSONObject(j).get("Craft").toString();
						    
						} else if (j > 0) {
							mainObjDestinationList.put("Destination-" + j, mainObjSegmentReturn.getJSONArray("Segment-" + i).getJSONObject(j).getJSONObject("Destination"));
							arrAirportCode = mainObjDestinationList.getJSONObject("Destination-" + j).getJSONObject("Airport").get("AirportCode").toString();
						    arrAirportName = mainObjDestinationList.getJSONObject("Destination-" + j).getJSONObject("Airport").get("AirportName").toString();
						    arrTerminal = mainObjDestinationList.getJSONObject("Destination-" + j).getJSONObject("Airport").get("Terminal").toString();
						    arrTime = mainObjDestinationList.getJSONObject("Destination-" + j).get("ArrTime").toString();
						    
							craftType = mainObjSegmentReturn.getJSONArray("Segment-" + i).getJSONObject(j).get("Craft").toString();
						}
					}
			        
			        String airlineName = mainObjAirlineReturn.getJSONObject("Airline-" + i).get("AirlineName").toString();
			        @SuppressWarnings("unused")
					String fareClass = mainObjAirlineReturn.getJSONObject("Airline-" + i).get("FareClass").toString();
			        String flightNumber = mainObjAirlineReturn.getJSONObject("Airline-" + i).get("AirlineCode").toString() + "-" + mainObjAirlineReturn.getJSONObject("Airline-" + i).get("FlightNumber").toString();
			        
			        String[] departureTimeParts = depTime.split("T");
					String[] departureTimeInnerParts = departureTimeParts[1].split(":");
					String stringDepTime = departureTimeInnerParts[0] + ":" + departureTimeInnerParts[1];
					String depTimeString = departureTimeInnerParts[0] + "." + departureTimeInnerParts[1].charAt(0);
					Float depTimeFloat = Float.parseFloat(depTimeString);
					
					String[] arrivalTimeParts = arrTime.split("T");
					String[] arrivalTimeInnerParts = arrivalTimeParts[1].split(":");
					String stringArrTime = arrivalTimeInnerParts[0] + ":" + arrivalTimeInnerParts[1];
					String arrTimeString = arrivalTimeInnerParts[0] + "." + arrivalTimeInnerParts[1].charAt(0);
					Float arrTimeFloat = Float.parseFloat(arrTimeString);
					
					JSONObject jsonObjectAdult = new JSONObject();
					Integer adultPrice = 0, childPrice = 0, infantPrice = 0, adultTax = 0, childTax = 0, infantTax = 0;
					Integer intTotalAdultChildPrice = 0;
					
					for (int j = 0; j < jsonArrayFareBreakdownReturn.getJSONArray(i).length(); j++) {
						jsonObjectAdult.put("FareAdult-" + j, jsonArrayFareBreakdownReturn.getJSONArray(i).getJSONObject(j));
						String priceOnline = jsonObjectAdult.getJSONObject("FareAdult-" + j).get("BaseFare").toString();
						String taxOnline = jsonObjectAdult.getJSONObject("FareAdult-" + j).get("Tax").toString();
						String passengerTypeOnline = jsonObjectAdult.getJSONObject("FareAdult-" + j).get("PassengerType").toString();
						if (passengerTypeOnline.equals("3")) {
							infantPrice = Integer.parseInt(priceOnline) / infantNum;
							infantTax = Integer.parseInt(taxOnline) / infantNum;
						} else if (passengerTypeOnline.equals("2")) {
							childPrice = Integer.parseInt(priceOnline) / childNum;
							childTax = Integer.parseInt(taxOnline) / childNum;
						} else {
							adultPrice = Integer.parseInt(priceOnline) / adultNum;
							adultTax = Integer.parseInt(taxOnline) / adultNum;
						}
					}
					
					if (childPrice != 0) {
						intTotalAdultChildPrice = ((adultPrice + childPrice) / 2) + ((adultTax + childTax) / 2);
					} else {
						intTotalAdultChildPrice = adultPrice + adultTax;
					}

					Integer intTotalInfantPrice = infantPrice + infantTax;
					
					Integer duration = Integer.parseInt(mainObjSegmentReturn.getJSONArray("Segment-" + i).getJSONObject(0).get("Duration").toString());
					@SuppressWarnings("unused")
					String flightStatus = mainObjSegmentReturn.getJSONArray("Segment-" + i).getJSONObject(0).get("FlightStatus").toString();
					String noOfSeatAvailable = "";
					try {
						noOfSeatAvailable = mainObjSegmentReturn.getJSONArray("Segment-" + i).getJSONObject(0).get("NoOfSeatAvailable").toString();
					} catch (JSONException e) {
						System.out.println(i);
						noOfSeatAvailable = "0";
					}

					String resultIndex = mainObjReturn.getJSONObject("Result-" + i).get("ResultIndex").toString();
					String airlineRemark = mainObjReturn.getJSONObject("Result-" + i).get("AirlineRemark").toString();
					String mode = "Online-data";
					
					
					
					
					ProductDetail productDetailTwo = new ProductDetail(i, "waiting...", noOfSeatAvailable, noOfSeatAvailable, flightNumber, returnDate, 
		            		stringDepTime, stringArrTime, intTotalAdultChildPrice, intTotalInfantPrice, 0, 0, depAirportCode, arrAirportCode, true, true, stopNumber, duration, 
		            		airlineName, depTimeFloat, arrTimeFloat, pController.traceId, resultIndex, airlineRemark, mode, "2", depTerminal, arrTerminal, 15, 7, "", "", null, craftType);
					
					
					listProductDetailsOnlineReturn.add(productDetailTwo);
					listProductDetailsInSearchReturn.add(productDetailTwo);
					
					String resultStrTwo = productDetailTwo.getResultIndex();
					String[] arrayResultTwo = resultStrTwo.split("B");

					FlightMap flightMap = new FlightMap();
					
					for (ProductDetail productDetail2 : listProductDetailsOnline) {
						String resultStr = productDetail2.getResultIndex();
						String[] arrayResult = resultStr.split("B");
						if (arrayResultTwo[1].equals(arrayResult[1])) {
							flightMap.setId(i);
							flightMap.setFlightIdOne(productDetailTwo.getId());
							flightMap.setFlightIdTwo(productDetail2.getId());
							flightMaps.add(flightMap);
						}
					}		
				}
				JSONObject jsonObjTicketResponseError = jsonObjSearch.getJSONObject("Response").getJSONObject("Error");
				hasErrorArr[0] = jsonObjTicketResponseError.get("ErrorCode").toString();
				hasErrorArr[1] = jsonObjTicketResponseError.get("ErrorMessage").toString();
				
			} catch (Exception e) {
				JSONObject jsonObjTicketResponseError = jsonObjSearch.getJSONObject("Response").getJSONObject("Error");
				hasErrorArr[0] = jsonObjTicketResponseError.get("ErrorCode").toString();
				hasErrorArr[1] = jsonObjTicketResponseError.get("ErrorMessage").toString();
			}
	        
		} catch (Exception e2) {
			
			hasErrorArr[0] = "-1";
			hasErrorArr[1] = "Could not connect to the API.";
			e2.printStackTrace();
			// TODO: handle exception
		}

        return hasErrorArr;
	}
	
	public String[] searchReturnInternationalFlightAPI(String cityOne, String cityTwo, Integer adultNum, Integer childNum, Integer infantNum, String sortName, Model model, Date date, 
			Date returnDate, List<ProductDetail> listProductDetailsOnline, List<ProductDetail> listProductDetails, List<ProductDetail> listProductDetailsInSearch, 
			List<ProductDetail> listProductDetailsOnlineReturn, List<FlightMap> flightMaps) throws MalformedURLException, IOException {

		String[] hasErrorArr = new String[2];
        StringBuilder responseBodySearch = onlineFlightService.apiOnlineSearchModReturn(cityOne, cityTwo, adultNum, childNum, infantNum, date, returnDate);
        
		String traceIdStr = "offline";
		
		listProductDetailsOnline = new ArrayList<ProductDetail>(); 
		listProductDetails = productService.listAllFlights(cityOne, cityTwo, date, traceIdStr, Sort.by(sortName).ascending());
		for (ProductDetail productDetailOffline : listProductDetails) {
			listProductDetailsOnline.add(productDetailOffline);
		}

		listProductDetailsOnlineReturn = new ArrayList<ProductDetail>(); 
		flightMaps = new ArrayList<FlightMap>();
		
        ProductDetail[] productDetail = new ProductDetail[500];
        ProductDetail[] productDetailTwo = new ProductDetail[500];
        
        Integer count = 0;
        Integer countTwo = 0;
        
        JSONObject jsonObjSearch = new JSONObject(responseBodySearch.toString());
        System.out.println(jsonObjSearch);
        logService.generateLog(jsonObjSearch.toString());
        try {
        	JSONArray jsonArrays = jsonObjSearch.getJSONObject("Response").getJSONArray("Results").getJSONArray(0);
            JSONArray jsonObjSegment = new JSONArray();
            JSONArray jsonObjSegmentTwo = new JSONArray();
    		JSONObject mainObj = new JSONObject();
    		JSONObject mainObjSegment = new JSONObject();
    		JSONObject mainObjSegmentTwo = new JSONObject();
    		JSONObject mainObjOrigin = new JSONObject();
    		JSONObject mainObjDestination = new JSONObject();
    		JSONObject mainObjAirline = new JSONObject();
    		JSONObject mainObjOriginTwo = new JSONObject();
    		JSONObject mainObjDestinationTwo = new JSONObject();
    		JSONObject mainObjAirlineTwo = new JSONObject();
    		JSONObject mainObjFare = new JSONObject();
    		JSONArray jsonArrayFareBreakdown = new JSONArray();
           
    		pController.traceId = jsonObjSearch.getJSONObject("Response").get("TraceId").toString();
            
    		for (int i = 0; i < jsonArrays.length(); i++) {
    			
    	        mainObj.put("Result-" + i, jsonArrays.getJSONObject(i));
    	        jsonObjSegment.put(mainObj.getJSONObject("Result-" + i).getJSONArray("Segments").getJSONArray(0));
    	        mainObjFare.put("Fare-" + i, mainObj.getJSONObject("Result-" + i).getJSONObject("Fare"));
    	        jsonArrayFareBreakdown.put(mainObj.getJSONObject("Result-" + i).getJSONArray("FareBreakdown"));
    	        
    	        mainObjSegment.put("Segment-" + i, mainObj.getJSONObject("Result-" + i).getJSONArray("Segments").getJSONArray(0));
    	        
    	        mainObjOrigin.put("Origin-" + i, mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(0).getJSONObject("Origin"));
    	        mainObjDestination.put("Destination-" + i, mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(0).getJSONObject("Destination"));
    	        mainObjAirline.put("Airline-" + i, mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(0).getJSONObject("Airline"));

    			JSONObject mainObjOriginList = new JSONObject();
    			JSONObject mainObjDestinationList = new JSONObject();
    	        Integer innerSegmentArrayLength = mainObj.getJSONObject("Result-" + i).getJSONArray("Segments").getJSONArray(0).length();
    	        Integer stopNumber = innerSegmentArrayLength - 1;
    	        @SuppressWarnings("unused")
				String depAirportCode = "",depAirportName = "", depTerminal = "", depTime = "";
    	        @SuppressWarnings("unused")
				String arrAirportCode = "",arrAirportName = "", arrTerminal = "", arrTime = "";
    	        for (int k = 0; k < innerSegmentArrayLength; k++) {
    	        	if (k == 0) {
    		        	mainObjOriginList.put("Origin-" + k, mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(k).getJSONObject("Origin"));
    		        	depAirportCode = mainObjOriginList.getJSONObject("Origin-" + k).getJSONObject("Airport").get("AirportCode").toString();
    		        	depAirportName = mainObjOriginList.getJSONObject("Origin-" + k).getJSONObject("Airport").get("AirportName").toString();
    		        	depTerminal = mainObjOriginList.getJSONObject("Origin-" + k).getJSONObject("Airport").get("Terminal").toString();
    		        	depTime = mainObjOriginList.getJSONObject("Origin-" + k).get("DepTime").toString();
    		        	
    					mainObjDestinationList.put("Destination-" + k, mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(k).getJSONObject("Destination"));
    					arrAirportCode = mainObjDestinationList.getJSONObject("Destination-" + k).getJSONObject("Airport").get("AirportCode").toString();
    				    arrAirportName = mainObjDestinationList.getJSONObject("Destination-" + k).getJSONObject("Airport").get("AirportName").toString();
    				    arrTerminal = mainObjDestinationList.getJSONObject("Destination-" + k).getJSONObject("Airport").get("Terminal").toString();
    				    arrTime = mainObjDestinationList.getJSONObject("Destination-" + k).get("ArrTime").toString();
    				    
    				} else if (k == innerSegmentArrayLength - 1) {
    					mainObjDestinationList.put("Destination-" + k, mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(k).getJSONObject("Destination"));
    					arrAirportCode = mainObjDestinationList.getJSONObject("Destination-" + k).getJSONObject("Airport").get("AirportCode").toString();
    				    arrAirportName = mainObjDestinationList.getJSONObject("Destination-" + k).getJSONObject("Airport").get("AirportName").toString();
    				    arrTerminal = mainObjDestinationList.getJSONObject("Destination-" + k).getJSONObject("Airport").get("Terminal").toString();
    				    arrTime = mainObjDestinationList.getJSONObject("Destination-" + k).get("ArrTime").toString();
    				}
				}
    	        

    	        String airlineName = mainObjAirline.getJSONObject("Airline-" + i).get("AirlineName").toString();
    	        @SuppressWarnings("unused")
				String fareClass = mainObjAirline.getJSONObject("Airline-" + i).get("FareClass").toString();
    	        String flightNumber = mainObjAirline.getJSONObject("Airline-" + i).get("AirlineCode").toString() + "-" + mainObjAirline.getJSONObject("Airline-" + i).get("FlightNumber").toString();
    	        
    	        String[] departureTimeParts = depTime.split("T");
    			String[] departureTimeInnerParts = departureTimeParts[1].split(":");
    			String stringDepTime = departureTimeInnerParts[0] + ":" + departureTimeInnerParts[1];
    			String depTimeString = departureTimeInnerParts[0] + "." + departureTimeInnerParts[1].charAt(0);
    			Float depTimeFloat = Float.parseFloat(depTimeString);
    			
    			String[] arrivalTimeParts = arrTime.split("T");
    			String[] arrivalTimeInnerParts = arrivalTimeParts[1].split(":");
    			String stringArrTime = arrivalTimeInnerParts[0] + ":" + arrivalTimeInnerParts[1];
    			String arrTimeString = arrivalTimeInnerParts[0] + "." + arrivalTimeInnerParts[1].charAt(0);
    			Float arrTimeFloat = Float.parseFloat(arrTimeString);
    			
    			JSONObject jsonObjectAdult = new JSONObject();
    			Integer adultPrice = 0, childPrice = 0, infantPrice = 0,  childTax = 0, infantTax = 0;
    			Double adultTax = 0d;
    			Integer intTotalAdultChildPrice = 0;
    			
    			for (int k = 0; k < jsonArrayFareBreakdown.getJSONArray(i).length(); k++) {
    				jsonObjectAdult.put("FareAdult-" + k, jsonArrayFareBreakdown.getJSONArray(i).getJSONObject(k));
    				String priceOnline = jsonObjectAdult.getJSONObject("FareAdult-" + k).get("BaseFare").toString();
    				String taxOnline = jsonObjectAdult.getJSONObject("FareAdult-" + k).get("Tax").toString();
    				String passengerTypeOnline = jsonObjectAdult.getJSONObject("FareAdult-" + k).get("PassengerType").toString();
    				if (passengerTypeOnline.equals("3")) {
    					infantPrice = Integer.parseInt(priceOnline) / infantNum;
    					infantTax = Integer.parseInt(taxOnline) / infantNum;
    				} else if (passengerTypeOnline.equals("2")) {
    					childPrice = Integer.parseInt(priceOnline) / childNum;
    					childTax = Integer.parseInt(taxOnline) / childNum;
    				} else {
    					adultPrice = Integer.parseInt(priceOnline) / adultNum;
    					adultTax = Double.parseDouble(taxOnline) / adultNum;
    				}
    			}
    			
    			if (childPrice != 0) {
    				intTotalAdultChildPrice = (int) (((adultPrice + childPrice) / 2) + ((adultTax + childTax) / 2));
    			} else {
    				intTotalAdultChildPrice = (int) (adultPrice + adultTax);
    			}

    			Integer intTotalInfantPrice = infantPrice + infantTax;
    			
    			Integer duration = Integer.parseInt(mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(0).get("Duration").toString());
    			@SuppressWarnings("unused")
				String flightStatus = mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(0).get("FlightStatus").toString();
    			String noOfSeatAvailable = "";
    			try {
    				noOfSeatAvailable = mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(0).get("NoOfSeatAvailable").toString();
    			} catch (JSONException e) {
    				System.out.println(i);
    				noOfSeatAvailable = "0";
    			}

    			String resultIndex = mainObj.getJSONObject("Result-" + i).get("ResultIndex").toString();
    			String airlineRemark = mainObj.getJSONObject("Result-" + i).get("AirlineRemark").toString();

				
				
				
    			String mode = "Online-data";
    			
    			String craftType = mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(0).get("Craft").toString();
    			
    			productDetail[i] = new ProductDetail(i, "waiting...", noOfSeatAvailable, noOfSeatAvailable, flightNumber, date, 
                		stringDepTime, stringArrTime, intTotalAdultChildPrice, intTotalInfantPrice, 0, 0, depAirportCode, arrAirportCode, true, true, stopNumber, duration, 
                		airlineName, depTimeFloat, arrTimeFloat, pController.traceId, resultIndex, airlineRemark, mode, "2", depTerminal, arrTerminal, 15, 7, "", "", null, craftType);
    			
    			listProductDetailsOnline.add(productDetail[i]);
    			
    			listProductDetailsInSearch.add(productDetail[i]);

    			FlightMap flightMap = new FlightMap();
    			
				flightMap.setId(i);
				flightMap.setFlightIdTwo(productDetail[i].getId());
				flightMap.setFlightIdOne(productDetail[i].getId());
				flightMaps.add(flightMap);
    			
    			count++;
    			
    	        
    	        
    			try {
    				jsonObjSegmentTwo.put(mainObj.getJSONObject("Result-" + i).getJSONArray("Segments").getJSONArray(1));
        	        
        	        mainObjSegmentTwo.put("Segment-" + i, mainObj.getJSONObject("Result-" + i).getJSONArray("Segments").getJSONArray(1));
        	        
        	        
        	        mainObjOriginTwo.put("Origin-" + i, mainObjSegmentTwo.getJSONArray("Segment-" + i).getJSONObject(0).getJSONObject("Origin"));
        	        mainObjDestinationTwo.put("Destination-" + i, mainObjSegmentTwo.getJSONArray("Segment-" + i).getJSONObject(0).getJSONObject("Destination"));
        	        mainObjAirlineTwo.put("Airline-" + i, mainObjSegmentTwo.getJSONArray("Segment-" + i).getJSONObject(0).getJSONObject("Airline"));

        			JSONObject mainObjOriginListTwo = new JSONObject();
        			JSONObject mainObjDestinationListTwo = new JSONObject();
        	        Integer innerSegmentArrayLengthTwo = mainObj.getJSONObject("Result-" + i).getJSONArray("Segments").getJSONArray(1).length();
        	        Integer stopNumberTwo = innerSegmentArrayLengthTwo - 1;
        	        @SuppressWarnings("unused")
    				String depAirportCodeTwo = "",depAirportNameTwo = "", depTerminalTwo = "", depTimeTwo = "";
        	        @SuppressWarnings("unused")
    				String arrAirportCodeTwo = "",arrAirportNameTwo = "", arrTerminalTwo = "", arrTimeTwo = "";
        	        
        	        for (int k = 0; k < innerSegmentArrayLengthTwo; k++) {
        	        	if (k == 0) {
        	        		mainObjOriginListTwo.put("Origin-" + k, mainObjSegmentTwo.getJSONArray("Segment-" + i).getJSONObject(k).getJSONObject("Origin"));
        		        	depAirportCodeTwo = mainObjOriginListTwo.getJSONObject("Origin-" + k).getJSONObject("Airport").get("AirportCode").toString();
        		        	depAirportNameTwo = mainObjOriginListTwo.getJSONObject("Origin-" + k).getJSONObject("Airport").get("AirportName").toString();
        		        	depTerminalTwo = mainObjOriginListTwo.getJSONObject("Origin-" + k).getJSONObject("Airport").get("Terminal").toString();
        		        	depTimeTwo = mainObjOriginListTwo.getJSONObject("Origin-" + k).get("DepTime").toString();
        		        	
        		        	mainObjDestinationListTwo.put("Destination-" + k, mainObjSegmentTwo.getJSONArray("Segment-" + i).getJSONObject(k).getJSONObject("Destination"));
        					arrAirportCodeTwo = mainObjDestinationListTwo.getJSONObject("Destination-" + k).getJSONObject("Airport").get("AirportCode").toString();
        				    arrAirportNameTwo = mainObjDestinationListTwo.getJSONObject("Destination-" + k).getJSONObject("Airport").get("AirportName").toString();
        				    arrTerminalTwo = mainObjDestinationListTwo.getJSONObject("Destination-" + k).getJSONObject("Airport").get("Terminal").toString();
        				    arrTimeTwo = mainObjDestinationListTwo.getJSONObject("Destination-" + k).get("ArrTime").toString();
        				    
        				} else if (k == innerSegmentArrayLength - 1) {
        					mainObjDestinationListTwo.put("Destination-" + k, mainObjSegmentTwo.getJSONArray("Segment-" + i).getJSONObject(k).getJSONObject("Destination"));
        					arrAirportCodeTwo = mainObjDestinationListTwo.getJSONObject("Destination-" + k).getJSONObject("Airport").get("AirportCode").toString();
        				    arrAirportNameTwo = mainObjDestinationListTwo.getJSONObject("Destination-" + k).getJSONObject("Airport").get("AirportName").toString();
        				    arrTerminalTwo = mainObjDestinationListTwo.getJSONObject("Destination-" + k).getJSONObject("Airport").get("Terminal").toString();
        				    arrTimeTwo = mainObjDestinationListTwo.getJSONObject("Destination-" + k).get("ArrTime").toString();
        				}
    				}
        	        
        	        String airlineNameTwo = mainObjAirlineTwo.getJSONObject("Airline-" + i).get("AirlineName").toString();
        	        @SuppressWarnings("unused")
    				String fareClassTwo = mainObjAirlineTwo.getJSONObject("Airline-" + i).get("FareClass").toString();
        	        String flightNumberTwo = mainObjAirlineTwo.getJSONObject("Airline-" + i).get("AirlineCode").toString() + "-" + mainObjAirlineTwo.getJSONObject("Airline-" + i).get("FlightNumber").toString();
        	        
        	        String[] departureTimePartsTwo = depTimeTwo.split("T");
        			String[] departureTimeInnerPartsTwo = departureTimePartsTwo[1].split(":");
        			String stringDepTimeTwo = departureTimeInnerPartsTwo[0] + ":" + departureTimeInnerPartsTwo[1];
        			String depTimeStringTwo = departureTimeInnerPartsTwo[0] + "." + departureTimeInnerPartsTwo[1].charAt(0);
        			Float depTimeFloatTwo = Float.parseFloat(depTimeStringTwo);
        			
        			String[] arrivalTimePartsTwo = arrTimeTwo.split("T");
        			String[] arrivalTimeInnerPartsTwo = arrivalTimePartsTwo[1].split(":");
        			String stringArrTimeTwo = arrivalTimeInnerPartsTwo[0] + ":" + arrivalTimeInnerPartsTwo[1];
        			String arrTimeStringTwo = arrivalTimeInnerPartsTwo[0] + "." + arrivalTimeInnerPartsTwo[1].charAt(0);
        			Float arrTimeFloatTwo = Float.parseFloat(arrTimeStringTwo);
        			
        			JSONObject jsonObjectAdultTwo = new JSONObject();
        			Integer adultPriceTwo = 0, childPriceTwo = 0, infantPriceTwo = 0, adultTaxTwo = 0, childTaxTwo = 0, infantTaxTwo = 0;
        			Integer intTotalAdultChildPriceTwo = 0;
        			
        			for (int k = 0; k < jsonArrayFareBreakdown.getJSONArray(i).length(); k++) {
        				jsonObjectAdultTwo.put("FareAdult-" + k, jsonArrayFareBreakdown.getJSONArray(i).getJSONObject(k));
        				String priceOnline = jsonObjectAdultTwo.getJSONObject("FareAdult-" + k).get("BaseFare").toString();
        				String taxOnline = jsonObjectAdultTwo.getJSONObject("FareAdult-" + k).get("Tax").toString();
        				String passengerTypeOnline = jsonObjectAdultTwo.getJSONObject("FareAdult-" + k).get("PassengerType").toString();
        				if (passengerTypeOnline.equals("3")) {
        					infantPriceTwo = Integer.parseInt(priceOnline) / infantNum;
        					infantTaxTwo = Integer.parseInt(taxOnline) / infantNum;
        				} else if (passengerTypeOnline.equals("2")) {
        					childPriceTwo = Integer.parseInt(priceOnline) / childNum;
        					childTaxTwo = Integer.parseInt(taxOnline) / childNum;
        				} else {
        					adultPriceTwo = Integer.parseInt(priceOnline) / adultNum;
        					adultTaxTwo = Integer.parseInt(taxOnline) / adultNum;
        				}
        			}
        			
        			if (childPriceTwo != 0) {
        				intTotalAdultChildPriceTwo = ((adultPriceTwo + childPriceTwo) / 2) + ((adultTaxTwo + childTaxTwo) / 2);
        			} else {
        				intTotalAdultChildPriceTwo = adultPriceTwo + adultTaxTwo;
        			}

        			Integer intTotalInfantPriceTwo = infantPriceTwo + infantTaxTwo;
        			
        			Integer durationTwo = Integer.parseInt(mainObjSegmentTwo.getJSONArray("Segment-" + i).getJSONObject(0).get("Duration").toString());
        			@SuppressWarnings("unused")
    				String flightStatusTwo = mainObjSegmentTwo.getJSONArray("Segment-" + i).getJSONObject(0).get("FlightStatus").toString();
        			String noOfSeatAvailableTwo = "";
        			try {
        				noOfSeatAvailableTwo = mainObjSegmentTwo.getJSONArray("Segment-" + i).getJSONObject(0).get("NoOfSeatAvailable").toString();
        			} catch (JSONException e) {
        				System.out.println(i);
        				noOfSeatAvailableTwo = "0";
        			}

        			String resultIndexTwo = mainObj.getJSONObject("Result-" + i).get("ResultIndex").toString();
        			String airlineRemarkTwo = mainObj.getJSONObject("Result-" + i).get("AirlineRemark").toString();

//    				String[] arrRIndexTwo = resultIndex.split("TBO");
//    				resultIndexTwo = arrRIndexTwo[0].replace("[", "");
    				
        			String modeTwo = "Online-data";
        			
        			String craftTypeTwo = mainObjSegmentTwo.getJSONArray("Segment-" + i).getJSONObject(0).get("Craft").toString();
        			
        			productDetailTwo[i] = new ProductDetail(i, "waiting...", noOfSeatAvailableTwo, noOfSeatAvailableTwo, flightNumberTwo, date, 
                    		stringDepTimeTwo, stringArrTimeTwo, intTotalAdultChildPriceTwo, intTotalInfantPriceTwo, 0, 0, depAirportCodeTwo, arrAirportCodeTwo, true, true, stopNumberTwo, durationTwo, 
                    		airlineNameTwo, depTimeFloatTwo, arrTimeFloatTwo, pController.traceId, resultIndexTwo, airlineRemarkTwo, modeTwo, "2", depTerminalTwo, arrTerminalTwo, 15, 7, "", "", null, craftTypeTwo);
        			
        			listProductDetailsOnlineReturn.add(productDetailTwo[i]);
    				
        			countTwo++;

    				JSONObject jsonObjTicketResponseError = jsonObjSearch.getJSONObject("Response").getJSONObject("Error");
    				hasErrorArr[0] = jsonObjTicketResponseError.get("ErrorCode").toString();
    				hasErrorArr[1] = jsonObjTicketResponseError.get("ErrorMessage").toString();
    				
				} catch (JSONException e) {
					e.printStackTrace();
				}
    		}
			model.addAttribute("success", jsonObjSearch.getJSONObject("Response").get("ResponseStatus").toString());
			
		JSONObject jsonObjTicketResponseError = jsonObjSearch.getJSONObject("Response").getJSONObject("Error");
		hasErrorArr[0] = jsonObjTicketResponseError.get("ErrorCode").toString();
		hasErrorArr[1] = jsonObjTicketResponseError.get("ErrorMessage").toString();
			
		} catch (JSONException e) {
			e.printStackTrace();
		}

		model.addAttribute("flightMaps", flightMaps);
		model.addAttribute("listProducts", listProductDetailsOnline);
		model.addAttribute("listProductsReturn", listProductDetailsOnlineReturn);

        return hasErrorArr;
	}
	
	@GetMapping("/test")
    public String testLoadingAnimation(Model model) {
        model.addAttribute("searchURL", searchReturnURL);
        return "loading/loading";
    }
	
	public Integer saveHistoryReturnPart(String cityOne, String cityTwo, Date date, Date returnDate, String journeyClass, String tripType,
			Integer adultNum, Integer childNum, Integer infantNum, Customer customer) {
		Integer totalPassenger = adultNum + childNum + infantNum;
		
		ProductSaveHelper.setSearchHistoryReturn(customer, cityOne, cityTwo, totalPassenger, journeyClass, adultNum, childNum, infantNum, tripType, date, returnDate);
		
		searchService.saveSearchHistory(customer);
		Customer savedSearch = searchService.saveSearchHistory(customer);
		
		
		List<SearchHistory> savedSearchResult = savedSearch.getSearchHistory();
		SearchHistory lastValue = savedSearchResult.get(savedSearchResult.size() - 1);
		
		Integer searchId = lastValue.getId();
		return searchId;
	}

	////Flight sorting segment.
	
	public List<ProductDetail> searchOffline(String cityOne, String cityTwo, String sortName, Date date) {
		String traceIdStr = "offline";
		
		return productService.listAllFlightsByPrice(cityOne, cityTwo, date, traceIdStr);
	}

	
	
	// Pojo 
	
	public static class FlightHistory {
		
		private Integer id;

		private String cityOne;
		
		private String cityTwo;
		
		private Integer passengerNum;
		
		private String journeyClass;
		
		private Integer adultNum;
		
		private Integer childNum;
		
		private Integer infantNum;
		
		private String tripType;
		
		private Date date;
		
		private Date returnDate;
		
		private String sort;
		
		private boolean filter;
		
		
		public FlightHistory() {}
		
		

		public FlightHistory(String cityOne, String cityTwo, Integer passengerNum, String journeyClass,
				Integer adultNum, Integer childNum, Integer infantNum, String tripType, Date date,
				Date returnDate, String sort, boolean filter) {
			this.cityOne = cityOne;
			this.cityTwo = cityTwo;
			this.passengerNum = passengerNum;
			this.journeyClass = journeyClass;
			this.adultNum = adultNum;
			this.childNum = childNum;
			this.infantNum = infantNum;
			this.tripType = tripType;
			this.date = date;
			this.returnDate = returnDate;
			this.sort = sort;
			this.filter = filter;
		}


		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}
		
		public String getSort() {
			return sort;
		}

		public void setSort(String sort) {
			this.sort = sort;
		}

		public boolean isFilter() {
			return filter;
		}

		public void setFilter(boolean filter) {
			this.filter = filter;
		}

		public String getCityOne() {
			return cityOne;
		}

		public void setCityOne(String cityOne) {
			this.cityOne = cityOne;
		}

		public String getCityTwo() {
			return cityTwo;
		}

		public void setCityTwo(String cityTwo) {
			this.cityTwo = cityTwo;
		}

		public Integer getPassengerNum() {
			return passengerNum;
		}

		public void setPassengerNum(Integer passengerNum) {
			this.passengerNum = passengerNum;
		}

		public String getJourneyClass() {
			return journeyClass;
		}

		public void setJourneyClass(String journeyClass) {
			this.journeyClass = journeyClass;
		}

		public Integer getAdultNum() {
			return adultNum;
		}

		public void setAdultNum(Integer adultNum) {
			this.adultNum = adultNum;
		}

		public Integer getChildNum() {
			return childNum;
		}

		public void setChildNum(Integer childNum) {
			this.childNum = childNum;
		}

		public Integer getInfantNum() {
			return infantNum;
		}

		public void setInfantNum(Integer infantNum) {
			this.infantNum = infantNum;
		}

		public String getTripType() {
			return tripType;
		}

		public void setTripType(String tripType) {
			this.tripType = tripType;
		}

		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}

		public Date getReturnDate() {
			return returnDate;
		}

		public void setReturnDate(Date returnDate) {
			this.returnDate = returnDate;
		}
		
	}
}
