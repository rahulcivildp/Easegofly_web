package com.easygofly.site.search;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.easygofly.site.flight.CityRepository;
import com.easygofly.site.flight.ProductDetailService;
import com.easygofly.site.flight.ProductDetailsController;
import com.easygofly.site.flight.ProductDetailsRepository;
import com.easygofly.site.flight.ProductSaveHelper;
import com.easygofly.site.flightAPI.OnlineFlightService;
import com.easygofly.site.security.EasyGoFlyCustomerDetails;
import com.easygofly.site.security.oauth.CustomerOAuth2User;
import com.easygofly.site.setting.CountryRepository;

@Controller
public class SearchHistoryController {

	@Autowired private SearchHistoryService searchService;
	@Autowired private CustomerService customerService;
	@Autowired private ProductDetailService productService;
	@Autowired private SearchHistoryRepository searchRepo;
	@Autowired private ProductDetailsRepository productRepo;
	@Autowired private CityRepository cityRepo;
	@Autowired private CountryRepository countryRepo;
	@Autowired private OnlineFlightService onlineFlightService;
	@Autowired private ProductDetailsController productDetailsController;
	@Autowired private LogService logService;
	
	private String searchURL = "";
	private String searchReturnURL = "";
	public List<ProductDetail> listProductDetails;
	public List<ProductDetail> listProductDetailsInSearch = new ArrayList<ProductDetail>();
	public String traceId = "";
			
	@GetMapping("/search_result")
	public String viewSearchResult(@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, @AuthenticationPrincipal CustomerOAuth2User googleLogin, Model model) {
		String email; 
		Customer customer;
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByEmail(email);
			model.addAttribute("customer", customer);
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByEmail(email);
			model.addAttribute("customer", customer);
		}
				
		return "flight/search";
	}
	
	private void authenticationFlight(Model model) {
		try {
        	
        	// Create URL object with the API end-point
            URL url = new URL("http://api.tektravels.com/SharedServices/SharedData.svc/rest/Authenticate");

            // Open a connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
        	StringBuilder responseBody = new StringBuilder();
        	
            int authCode = onlineFlightService.apiAuthentication(connection, responseBody);
            
            JSONObject jsonObj = new JSONObject(responseBody.toString());
            JSONObject jsonObjInnerError = jsonObj.getJSONObject("Error");
            JSONObject jsonObjInnerMember = jsonObj.getJSONObject("Member");
             
            model.addAttribute("authCode", authCode);
            model.addAttribute("responseBody", jsonObj);
            model.addAttribute("memberName", jsonObjInnerMember.get("FirstName") + " " + jsonObjInnerMember.get("LastName"));
            model.addAttribute("memberEmail", jsonObjInnerMember.get("Email"));
            model.addAttribute("memberId", jsonObjInnerMember.get("MemberId"));
            model.addAttribute("memberAgencyId", jsonObjInnerMember.get("AgencyId"));
            model.addAttribute("memberLoginName", jsonObjInnerMember.get("LoginName"));
            model.addAttribute("memberLoginDetails", jsonObjInnerMember.get("LoginDetails"));
            model.addAttribute("memberIsPrimaryAgent", jsonObjInnerMember.get("isPrimaryAgent"));
            model.addAttribute("errorCode", jsonObjInnerError.get("ErrorCode"));
            model.addAttribute("errorMessage", jsonObjInnerError.get("ErrorMessage"));
            
            onlineFlightService.tokenId = (String) jsonObj.get("TokenId");
            System.out.println(jsonObj);
            logService.generateLog(jsonObj.toString());
            
            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	@GetMapping("/flight_search_{id}_{sortName}_{brand}_{stop}_{totalPrice}_{activeTime}")
	public String searchFlightDetailsSingles(@PathVariable(name = "id") Integer id, SearchHistory searchHistory, 
			@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, 
			@AuthenticationPrincipal CustomerOAuth2User googleLogin,
			@PathVariable(name = "sortName") String sortName,
			@PathVariable(name = "activeTime") String[] activeTime,
			@PathVariable(name = "brand") String[] brands,
			@PathVariable(name = "stop") Integer[] stops,
			@PathVariable(name = "totalPrice") Integer[] totalPrice, 
			Model model, RedirectAttributes redirectAttributes) throws MalformedURLException, IOException {
		String email; 
		Customer customer;
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByEmail(email);
			model.addAttribute("customer", customer);
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByEmail(email);
			model.addAttribute("customer", customer);
		}
		
		authenticationFlight(model);
		
		SearchHistory search = searchRepo.findById(id).get();
		
		searchSort(search.getCityOne(), search.getCityTwo(), sortName, model, search.getDate());

		List<Product> getProductBrand = productRepo.findProductByCity(search.getCityOne(), search.getCityTwo(), Sort.by("name").ascending());
		
		Country country = countryRepo.findById(106).get();
		Iterable<City> cities = cityRepo.getCityByCountry(country);
		
		model.addAttribute("cities", cities);
		model.addAttribute("getProductBrand", getProductBrand);
		model.addAttribute("search", search);
		
		int responseCode = searchFlightAPI(search.getCityOne(), search.getCityTwo(), search.getAdultNum(), search.getChildNum(), search.getInfantNum(), sortName, model, search.getDate());
		if (responseCode != HttpURLConnection.HTTP_OK) {
			if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP
				|| responseCode == HttpURLConnection.HTTP_MOVED_PERM
					|| responseCode == HttpURLConnection.HTTP_SEE_OTHER)
				return "redirect:/";
		}
		
		return "flight/search/search-result";
	}
	
	@GetMapping("/flight_search-noUser_{cityOne}_{cityTwo}_{journeyClass}_{tripType}_{adultNum}_{childNum}_{infantNum}_{strDate}_{sortName}_{brand}_{stop}_{totalPrice}_{activeTime}")
	public String searchFlightDetailsSinglesNoUser(
			@PathVariable(name = "cityOne") String cityOne,
			@PathVariable(name = "cityTwo") String cityTwo,
			@PathVariable(name = "journeyClass") String journeyClass,
			@PathVariable(name = "tripType") String tripType,
			@PathVariable(name = "adultNum") Integer adultNum,
			@PathVariable(name = "childNum") Integer childNum,
			@PathVariable(name = "infantNum") Integer infantNum,
			@PathVariable(name = "strDate") String strDate,
			@PathVariable(name = "sortName") String sortName,
			@PathVariable(name = "activeTime") String[] activeTime,
			@PathVariable(name = "brand") String[] brands,
			@PathVariable(name = "stop") Integer[] stops,
			@PathVariable(name = "totalPrice") Integer[] totalPrice,
			Model model, RedirectAttributes redirectAttributes) throws ParseException, IOException {

		authenticationFlight(model);
		
	    Date date = new SimpleDateFormat("yyyy-MM-dd").parse(strDate);

		searchSort(cityOne, cityTwo, sortName, model, date);

		List<Product> getProductBrand = productRepo.findProductByCity(cityOne, cityTwo, Sort.by("name").ascending());
		
		Integer passengerNum = adultNum + childNum + infantNum;
		Country country = countryRepo.findById(106).get();
		Iterable<City> cities = cityRepo.getCityByCountry(country);
		
		System.out.println(date);
		System.out.println(strDate);
		
		model.addAttribute("cities", cities);
		model.addAttribute("getProductBrand", getProductBrand);
		model.addAttribute("cityOne", cityOne);
		model.addAttribute("cityTwo", cityTwo);
		model.addAttribute("date", date);
		model.addAttribute("strDate", strDate);
		model.addAttribute("journeyClass", journeyClass);
		model.addAttribute("tripType", tripType);
		model.addAttribute("adultNum", adultNum);
		model.addAttribute("childNum", childNum);
		model.addAttribute("infantNum", infantNum);
		model.addAttribute("passengerNum", passengerNum);
		model.addAttribute("brands", brands);
		model.addAttribute("stops", stops);
		model.addAttribute("totalPrice", totalPrice);
		
		
		int responseCode = searchFlightAPI(cityOne, cityTwo, adultNum, childNum, infantNum, sortName, model, date);
		if (responseCode != HttpURLConnection.HTTP_OK) {
			if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP
				|| responseCode == HttpURLConnection.HTTP_MOVED_PERM
					|| responseCode == HttpURLConnection.HTTP_SEE_OTHER)
				return "redirect:/";
		}
		
		return "flight/search/search-result-noUser";
		
	}


	public Integer searchFlightAPI(String cityOne, String cityTwo, Integer adultNum, Integer childNum, Integer infantNum,
			String sortName, Model model, Date date) throws MalformedURLException, IOException {
		// Create URL object with the API end-point
        URL urlSearch = new URL("http://api.tektravels.com/BookingEngineService_Air/AirService.svc/rest/Search");

        // Open a connection
        HttpURLConnection connectionSearch = (HttpURLConnection) urlSearch.openConnection();
        
        StringBuilder responseBodySearch = new StringBuilder();
        
        int responseCode = onlineFlightService.apiOnlineSearchMod(connectionSearch, responseBodySearch, cityOne, cityTwo, adultNum, childNum, infantNum, date);

		String traceIdStr = "offline";
		
        productDetailsController.listProductDetailsOnline = new ArrayList<ProductDetail>(); 
		listProductDetails = productService.listAllFlights(cityOne, cityTwo, date, traceIdStr, Sort.by(sortName).ascending());
		for (ProductDetail productDetailOffline : listProductDetails) {
			productDetailsController.listProductDetailsOnline.add(productDetailOffline);
		}
		
        ProductDetail[] productDetail = new ProductDetail[500];
        
        JSONObject jsonObjSearch = new JSONObject(responseBodySearch.toString());
        System.out.println(jsonObjSearch);
        logService.generateLog(jsonObjSearch.toString());
        JSONArray jsonArrays = jsonObjSearch.getJSONObject("Response").getJSONArray("Results").getJSONArray(0);
        JSONArray jsonObjSegment = new JSONArray();
		JSONObject mainObj = new JSONObject();
		JSONObject mainObjSegment = new JSONObject();
		JSONObject mainObjOrigin = new JSONObject();
		JSONObject mainObjDestination = new JSONObject();
		JSONObject mainObjAirline = new JSONObject();
		JSONObject mainObjFare = new JSONObject();
		JSONArray jsonArrayFareBreakdown = new JSONArray();
       
		traceId = jsonObjSearch.getJSONObject("Response").get("TraceId").toString();
        
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
			
			productDetail[i] = new ProductDetail(i, "waiting...", noOfSeatAvailable, noOfSeatAvailable, flightNumber, date, 
            		stringDepTime, stringArrTime, intTotalAdultChildPrice, intTotalInfantPrice, 0, 0, depAirportCode, arrAirportCode, true, true, stopNumber, duration, 
            		airlineName, depTimeFloat, arrTimeFloat, traceId, resultIndex, airlineRemark, mode, "1", depTerminal, arrTerminal, 15, 7, null, craftType);
			
			productDetailsController.listProductDetailsOnline.add(productDetail[i]);
			
			listProductDetailsInSearch.add(productDetail[i]);
		}
		
		model.addAttribute("listProducts", productDetailsController.listProductDetailsOnline);
        model.addAttribute("responseCode", responseCode);
        
        // Close the connection
        connectionSearch.disconnect();
        
        
        
        return responseCode;
	}

	public void searchSort(String cityOne, String cityTwo, String sortName, Model model, Date date) {
		String traceIdStr = "offline";
		
		if (sortName.equals("pnr")) {
			listProductDetails = productService.listAllFlights(cityOne, cityTwo, date, traceIdStr, Sort.by(sortName).ascending());
			model.addAttribute("listProducts", listProductDetails);
		} else if (sortName.equals("price")) {
			listProductDetails = productService.listAllFlightsByPrice(cityOne, cityTwo, date, traceIdStr);
			model.addAttribute("listProducts", listProductDetails);
		} else if (sortName.equals("duration")) {
			listProductDetails = productService.listAllFlights(cityOne, cityTwo, date, traceIdStr, Sort.by(sortName).ascending());
			model.addAttribute("listProducts", listProductDetails);
		} else if (sortName.equals("arrTime")) {
			listProductDetails = productService.listAllFlightsByArrival(cityOne, cityTwo, date, traceIdStr);
			model.addAttribute("listProducts", listProductDetails);
		} else if (sortName.equals("depTime")) {
			listProductDetails = productService.listAllFlightsByDeparture(cityOne, cityTwo, date, traceIdStr);
			model.addAttribute("listProducts", listProductDetails);
		} else if (sortName.equals("brand")) {
			listProductDetails = productService.listAllFlights(cityOne, cityTwo, date, traceIdStr, Sort.by(sortName).ascending());
			model.addAttribute("listProducts", listProductDetails);
		}
	}
	
	@GetMapping("/flight_search_save")
	public String searchHistorySave(@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer,
			@AuthenticationPrincipal CustomerOAuth2User googleLogin, 
			@RequestParam(name = "cityOne", required = false) String cityOne, 
			@RequestParam(name = "cityTwo", required = false) String cityTwo, 
			@RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date date, 
			@RequestParam(name = "journeyClass", required = false) String journeyClass,
			@RequestParam(name = "tripType", required = false) String tripType,
			@RequestParam(name = "adultNum", required = false) Integer adultNum,
			@RequestParam(name = "childNum", required = false) Integer childNum,
			@RequestParam(name = "infantNum", required = false) Integer infantNum,
			Model model) throws ProductNotFoundException {
		
			String email; 
			Customer customer;
			City city1 = cityRepo.getCityByName(cityOne);
		    City city2 = cityRepo.getCityByName(cityTwo);
		    
		    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
		    String strDate = dateFormat.format(date);
		    String sort = "pnr";
		    String brand = "";
		    Integer stop = 0;
		    String activeTime = "active";
		    String arrayPrice = "0,0";
		    
		    
            if (loggedCustomer != null) {
				email = loggedCustomer.getUsername();
				customer = customerService.getByEmail(email);
				model.addAttribute("customer", customer);
				Integer searchId = saveHistoryPart(city1.getCode(), city2.getCode(), date, journeyClass, tripType, adultNum, childNum,
						infantNum, customer);
				searchURL = "/flight_search_" + searchId +"_"+ sort +"_"+ brand +"_"+ stop +"_"+ arrayPrice +"_"+ activeTime;
				return "redirect:/loading_";
			} else if (googleLogin != null) {
				email = googleLogin.getEmail();
				customer = customerService.getByEmail(email);
				model.addAttribute("customer", customer);
				Integer searchId = saveHistoryPart(city1.getCode(), city2.getCode(), date, journeyClass, tripType, adultNum, childNum,
						infantNum, customer);
				searchURL = "/flight_search_" + searchId +"_"+ sort +"_"+ brand +"_"+ stop +"_"+ arrayPrice +"_"+ activeTime;
				return "redirect:/loading_";
			}else {
				searchURL = "/flight_search-noUser_"+ city1.getCode() +"_"+ city2.getCode() +"_"+ journeyClass +"_"+ tripType +"_"+ adultNum 
						+"_"+ childNum +"_"+ infantNum +"_"+ strDate +"_"+ sort +"_"+ brand +"_"+ stop +"_"+ arrayPrice +"_"+ activeTime;
				return "redirect:/loading_";
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
	
	
	////Flight return segment.
	
	@GetMapping("/flight_return_search_{id}_{sortName}_{brand}_{stop}_{activeTime}")
	public String searchFlightDetailsSinglesReturn(@PathVariable(name = "id") Integer id, SearchHistory searchHistory, 
			@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, 
			@AuthenticationPrincipal CustomerOAuth2User googleLogin,
			@PathVariable(name = "sortName") String sortName,
			@PathVariable(name = "activeTime") String[] activeTime,
			@PathVariable(name = "brand") String[] brands,
			@PathVariable(name = "stop") Integer[] stops,
			Model model, RedirectAttributes redirectAttributes) throws MalformedURLException, IOException {
		String email; 
		Customer customer;
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByEmail(email);
			model.addAttribute("customer", customer);
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByEmail(email);
			model.addAttribute("customer", customer);
		}

		authenticationFlight(model);
		
		SearchHistory search = searchRepo.findById(id).get();
		Integer passengerNum = search.getAdultNum() + search.getChildNum() + search.getInfantNum();
		
		searchSort(search.getCityOne(), search.getCityTwo(), sortName, model, search.getDate());

		List<Product> getProductBrand = productRepo.findProductByCity(search.getCityOne(), search.getCityTwo(), Sort.by("name").ascending());

		Country country = countryRepo.findById(106).get();
		Iterable<City> cities = cityRepo.getCityByCountry(country);
		
		model.addAttribute("cities", cities);
		model.addAttribute("getProductBrand", getProductBrand);
		model.addAttribute("passengerNum", passengerNum);
		model.addAttribute("search", search);
		
		int responseCode = searchReturnFlightAPI(search.getCityOne(), search.getCityTwo(), search.getAdultNum(), search.getChildNum(), 
				search.getInfantNum(), sortName, model, search.getDate(), search.getReturnDate());
		if (responseCode != HttpURLConnection.HTTP_OK) {
			if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP
				|| responseCode == HttpURLConnection.HTTP_MOVED_PERM
					|| responseCode == HttpURLConnection.HTTP_SEE_OTHER)
				return "redirect:/";
		}
		
		return "flight/search_return/search-result_return";
	}
	
	@GetMapping("/flight_search-noUser_return_{cityOne}_{cityTwo}_{journeyClass}_{tripType}_{adultNum}_{childNum}_{infantNum}_{strDate}_{strReturnDate}_{sortName}_{brand}_{stop}_{activeTime}")
	public String searchFlightDetailsSinglesNoUserReturn(
			@PathVariable(name = "cityOne") String cityOne,
			@PathVariable(name = "cityTwo") String cityTwo,
			@PathVariable(name = "journeyClass") String journeyClass,
			@PathVariable(name = "tripType") String tripType,
			@PathVariable(name = "adultNum") Integer adultNum,
			@PathVariable(name = "childNum") Integer childNum,
			@PathVariable(name = "infantNum") Integer infantNum,
			@PathVariable(name = "strDate") String strDate,
			@PathVariable(name = "strReturnDate") String strReturnDate,
			@PathVariable(name = "sortName") String sortName,
			@PathVariable(name = "activeTime") String[] activeTime,
			@PathVariable(name = "brand") String[] brands,
			@PathVariable(name = "stop") Integer[] stops,
			Model model, RedirectAttributes redirectAttributes) throws ParseException, IOException {

		authenticationFlight(model);
		
	    Date date = new SimpleDateFormat("yyyy-MM-dd").parse(strDate);
		 
	    Date returnDate = new SimpleDateFormat("yyyy-MM-dd").parse(strReturnDate);

		List<Product> getProductBrand = productRepo.findProductByCity(cityOne, cityTwo, Sort.by("name").ascending());
		
		Integer passengerNum = adultNum + childNum + infantNum;
		
		Country country = countryRepo.findById(106).get();
		Iterable<City> cities = cityRepo.getCityByCountry(country);
		
		System.out.println(date);
		System.out.println(strDate);
		
		model.addAttribute("cities", cities);
		model.addAttribute("getProductBrand", getProductBrand);
		model.addAttribute("cityOne", cityOne);
		model.addAttribute("cityTwo", cityTwo);
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
		model.addAttribute("brands", brands);
		model.addAttribute("stops", stops);
		
		
		int responseCode = searchReturnFlightAPI(cityOne, cityTwo, adultNum, childNum, infantNum, sortName, model, date, returnDate);
		if (responseCode != HttpURLConnection.HTTP_OK) {
			if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP
				|| responseCode == HttpURLConnection.HTTP_MOVED_PERM
					|| responseCode == HttpURLConnection.HTTP_SEE_OTHER)
				return "redirect:/";
		}
		
		return "flight/search_return/search-result-noUser_return";
		
	}
	

	private Integer searchReturnFlightAPI(String cityOne, String cityTwo, Integer adultNum, Integer childNum, Integer infantNum,
			String sortName, Model model, Date date, Date returnDate) throws MalformedURLException, IOException {
		// Create URL object with the API end-point
        URL urlSearch = new URL("http://api.tektravels.com/BookingEngineService_Air/AirService.svc/rest/Search");

        // Open a connection
        HttpURLConnection connectionSearch = (HttpURLConnection) urlSearch.openConnection();
        
        StringBuilder responseBodySearch = new StringBuilder();
        
        int responseCode = onlineFlightService.apiOnlineSearchModReturn(connectionSearch, responseBodySearch, cityOne, cityTwo, adultNum, childNum, infantNum, date, returnDate);

		String traceIdStr = "offline";
		
        productDetailsController.listProductDetailsOnline = new ArrayList<ProductDetail>(); 
		listProductDetails = productService.listAllFlights(cityOne, cityTwo, date, traceIdStr, Sort.by(sortName).ascending());
		for (ProductDetail productDetailOffline : listProductDetails) {
			productDetailsController.listProductDetailsOnline.add(productDetailOffline);
		}
		
        ProductDetail[] productDetail = new ProductDetail[500];
        
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
           
    		traceId = jsonObjSearch.getJSONObject("Response").get("TraceId").toString();
            
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
    			
    			productDetail[i] = new ProductDetail(i, "waiting...", noOfSeatAvailable, noOfSeatAvailable, flightNumber, date, 
                		stringDepTime, stringArrTime, intTotalAdultChildPrice, intTotalInfantPrice, 0, 0, depAirportCode, arrAirportCode, true, true, stopNumber, duration, 
                		airlineName, depTimeFloat, arrTimeFloat, traceId, resultIndex, airlineRemark, mode, "2", depTerminal, arrTerminal, 15, 7, null, craftType);
    			
    			productDetailsController.listProductDetailsOnline.add(productDetail[i]);
    			
    			listProductDetailsInSearch.add(productDetail[i]);
    		}
		} catch (Exception e) {
			
		}
        

		productDetailsController.listProductDetailsOnlineReturn = new ArrayList<ProductDetail>(); 
		productDetailsController.flightMaps = new ArrayList<FlightMap>();
		try {
			JSONArray jsonArraysReturn = jsonObjSearch.getJSONObject("Response").getJSONArray("Results").getJSONArray(1);
			JSONObject mainObjReturn = new JSONObject();
			JSONObject mainObjSegmentReturn = new JSONObject();
			JSONObject mainObjOriginReturn = new JSONObject();
			JSONObject mainObjDestinationReturn = new JSONObject();
			JSONObject mainObjAirlineReturn = new JSONObject();
			JSONObject mainObjFareReturn = new JSONObject();
			JSONArray jsonArrayFareBreakdownReturn = new JSONArray();
	        JSONArray jsonObjSegmentReturn = new JSONArray();


	        ProductDetail[] productDetailTwo = new ProductDetail[500];
	        
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
				
				
				productDetailTwo[i] = new ProductDetail(i, "waiting...", noOfSeatAvailable, noOfSeatAvailable, flightNumber, returnDate, 
	            		stringDepTime, stringArrTime, intTotalAdultChildPrice, intTotalInfantPrice, 0, 0, depAirportCode, arrAirportCode, true, true, stopNumber, duration, 
	            		airlineName, depTimeFloat, arrTimeFloat, traceId, resultIndex, airlineRemark, mode, "2", depTerminal, arrTerminal, 15, 7, null, craftType);
				
				productDetailsController.listProductDetailsOnlineReturn.add(productDetailTwo[i]);

				String resultStrTwo = productDetailTwo[i].getResultIndex();
				String[] arrayResultTwo = resultStrTwo.split("B");

				FlightMap flightMap = new FlightMap();
				
				for (ProductDetail productDetail2 : productDetailsController.listProductDetailsOnline) {
					String resultStr = productDetail2.getResultIndex();
					String[] arrayResult = resultStr.split("B");
					if (arrayResultTwo[1].equals(arrayResult[1])) {
						flightMap.setId(i);
						flightMap.setFlightIdOne(productDetailTwo[i].getId());
						flightMap.setFlightIdTwo(productDetail2.getId());
						productDetailsController.flightMaps.add(flightMap);
					}
				}		
			}
		} catch (Exception e) {
			
		}
        

		model.addAttribute("flightMaps", productDetailsController.flightMaps);
		model.addAttribute("listProducts", productDetailsController.listProductDetailsOnline);
		model.addAttribute("listProductsReturn", productDetailsController.listProductDetailsOnlineReturn);
        model.addAttribute("responseCode", responseCode);
        
        // Close the connection
        connectionSearch.disconnect();

        return responseCode;
	}
	
	@GetMapping("/flight_search_return_save")
	public String searchHistorySaveReturn(@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer,
			@AuthenticationPrincipal CustomerOAuth2User googleLogin, 
			@RequestParam(name = "cityOne", required = false) String cityOne, 
			@RequestParam(name = "cityTwo", required = false) String cityTwo, 
			@RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date date, 
			@RequestParam(name = "return_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date returnDate, 
			@RequestParam(name = "journeyClass", required = false) String journeyClass,
			@RequestParam(name = "tripType", required = false) String tripType,
			@RequestParam(name = "adultNum", required = false) Integer adultNum,
			@RequestParam(name = "childNum", required = false) Integer childNum,
			@RequestParam(name = "infantNum", required = false) Integer infantNum,
			Model model) throws ProductNotFoundException {
		
			String email; 
			Customer customer;
			City city1 = cityRepo.getCityByName(cityOne);
		    City city2 = cityRepo.getCityByName(cityTwo);
		    
		    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
		    String strDate = dateFormat.format(date);
		    String strDateReturn = dateFormat.format(returnDate);
		    String sort = "pnr";
		    String brand = "";
		    Integer stop = 0;
		    String activeTime = "active";
		    
		    
            if (loggedCustomer != null) {
				email = loggedCustomer.getUsername();
				customer = customerService.getByEmail(email);
				model.addAttribute("customer", customer);
				Integer searchId = saveHistoryReturnPart(city1.getCode(), city2.getCode(), date, returnDate, journeyClass, tripType, adultNum, childNum, infantNum, customer);

				searchReturnURL = "/flight_return_search_" + searchId +"_"+ sort +"_"+ brand +"_"+ stop +"_"+ activeTime;
				return "redirect:/loading_return_";
			} else if (googleLogin != null) {
				email = googleLogin.getEmail();
				customer = customerService.getByEmail(email);
				model.addAttribute("customer", customer);
				Integer searchId = saveHistoryReturnPart(city1.getCode(), city2.getCode(), date, returnDate, journeyClass, tripType, adultNum, childNum, infantNum, customer);
				searchReturnURL = "/flight_return_search_" + searchId +"_"+ sort +"_"+ brand +"_"+ stop +"_"+ activeTime;
				return "redirect:/loading_return_";
			}else {
				searchReturnURL = "/flight_search-noUser_return_"+ city1.getCode() +"_"+ city2.getCode() +"_"+ journeyClass +"_"+ tripType +"_"+ adultNum 
						+"_"+ childNum +"_"+ infantNum +"_"+ strDate +"_"+ strDateReturn +"_"+ sort +"_"+ brand +"_"+ stop +"_"+ activeTime;
				return "redirect:/loading_return_";
			}
         
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

	@GetMapping("/loading_")
    public String performApiRequest(Model model) {
        model.addAttribute("searchURL", searchURL);
        return "loading/loading";
    }

	@GetMapping("/loading_return_")
    public String performApiRequestReturn(Model model) {
        model.addAttribute("searchURL", searchReturnURL);
        return "loading/loading";
    }


	@GetMapping("/test")
    public String testLoadingAnimation(Model model) {
        model.addAttribute("searchURL", searchReturnURL);
        return "loading/loading";
    }
	
	
}
