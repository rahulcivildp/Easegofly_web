package com.easygofly.site.flight;


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
import com.easygofly.site.security.EasegoflyPhoneCustomerDetails;
import com.easygofly.site.security.oauth.CustomerOAuth2User;
import com.easygofly.site.setting.CountryRepository;

@Controller
public class SearchHistoryInternationalController {
	@Autowired private CustomerService customerService;
	@Autowired private SearchHistoryRepository searchRepo;
	@Autowired private ProductDetailsRepository productRepo;
	@Autowired private CityRepository cityRepo;
	@Autowired private CountryRepository countryRepo;
	@Autowired private SearchHistoryService searchService;
	@Autowired private ProductDetailService productService;
	@Autowired private OnlineFlightService onlineFlightService;
	@Autowired private LogService logService;
	@Autowired private ProductDetailsInternationController pInternationController;
	
	private String searchURL = "";
	private String searchReturnURL = "";
	
	//International one-way segment******
	
	@GetMapping("/flight_international_search_save")
	public String searchHistoryInternationalSave(@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer,
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
				customer = customerService.getByPhone(email);
				model.addAttribute("customer", customer);
				Integer searchId = saveHistoryPart(city1.getCode(), city2.getCode(), date, journeyClass, tripType, adultNum, childNum,
						infantNum, customer);
				searchURL = "/flight_search_international_" + searchId +"_"+ sort +"_"+ brand +"_"+ stop +"_"+ arrayPrice +"_"+ activeTime;
				return "redirect:/loading_international_";
			} else if (googleLogin != null) {
				email = googleLogin.getEmail();
				customer = customerService.getByPhone(email);
				model.addAttribute("customer", customer);
				Integer searchId = saveHistoryPart(city1.getCode(), city2.getCode(), date, journeyClass, tripType, adultNum, childNum,
						infantNum, customer);
				searchURL = "/flight_search_international_" + searchId +"_"+ sort +"_"+ brand +"_"+ stop +"_"+ arrayPrice +"_"+ activeTime;
				return "redirect:/loading_international_";
			}else {
				searchURL = "/flight_search-noUser_international_"+ city1.getCode() +"_"+ city2.getCode() +"_"+ journeyClass +"_"+ tripType +"_"+ adultNum 
						+"_"+ childNum +"_"+ infantNum +"_"+ strDate +"_"+ sort +"_"+ brand +"_"+ stop +"_"+ arrayPrice +"_"+ activeTime;
				return "redirect:/loading_international_";
			}
            
	}
	
	@GetMapping("/flight_search_international_{id}_{sortName}_{brand}_{stop}_{totalPrice}_{activeTime}")
	public String searchFlightDetailsSingles(@PathVariable(name = "id") Integer id, SearchHistory searchHistory, 
			@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer, 
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
			customer = customerService.getByPhone(email);
			model.addAttribute("customer", customer);
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByPhone(email);
			model.addAttribute("customer", customer);
		}
		
		SearchHistory search = searchRepo.findById(id).get();
		
		searchSort(search.getCityOne(), search.getCityTwo(), sortName, model, search.getDate());

		List<Product> getProductBrand = productRepo.findProductByCity(search.getCityOne(), search.getCityTwo(), Sort.by("name").ascending());
		
		Country country = countryRepo.findById(106).get();
		Iterable<City> cities = cityRepo.getCityByCountry(country);
		
		model.addAttribute("cities", cities);
		model.addAttribute("getProductBrand", getProductBrand);
		model.addAttribute("search", search);
		
		String[] responseCode = searchFlightAPI(search.getCityOne(), search.getCityTwo(), search.getAdultNum(), search.getChildNum(), search.getInfantNum(), sortName, model, search.getDate());

		if (Integer.parseInt(responseCode[0]) != 0) {
			model.addAttribute("errorMsg", responseCode[1]);
		}
		
		return "flight/inter_search/search-result-international";
	}
	
	@GetMapping("/flight_search-noUser_international_{cityOne}_{cityTwo}_{journeyClass}_{tripType}_{adultNum}_{childNum}_{infantNum}_{strDate}_{sortName}_{brand}_{stop}_{totalPrice}_{activeTime}")
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
		
		
		String[] responseCode = searchFlightAPI(cityOne, cityTwo, adultNum, childNum, infantNum, sortName, model, date);

		if (Integer.parseInt(responseCode[0]) != 0) {
			model.addAttribute("errorMsg", responseCode[1]);
		}
		
		return "flight/inter_search/search-result-international-noUser";
		
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

	public String[] searchFlightAPI(String cityOne, String cityTwo, Integer adultNum, Integer childNum, Integer infantNum, String sortName, Model model, Date date) throws MalformedURLException, IOException {
		// Create URL object with the API end-point
        URL urlSearch = new URL("https://tboapi.travelboutiqueonline.com/AirAPI_V10/AirService.svc/rest/Search");

        // Open a connection
        HttpURLConnection connectionSearch = (HttpURLConnection) urlSearch.openConnection();
        
        StringBuilder responseBodySearch = new StringBuilder();
        
        int responseCode = onlineFlightService.apiOnlineSearchMod(connectionSearch, responseBodySearch, cityOne, cityTwo, adultNum, childNum, infantNum, date);

		String traceIdStr = "offline";
		String[] hasErrorArr = new String[2];
		
		pInternationController.listProductDetailsOnline = new ArrayList<ProductDetail>(); 
		pInternationController.listProductDetails = productService.listAllFlights(cityOne, cityTwo, date, traceIdStr, Sort.by(sortName).ascending());
		for (ProductDetail productDetailOffline : pInternationController.listProductDetails) {
			pInternationController.listProductDetailsOnline.add(productDetailOffline);
		}
		
//        ProductDetail[] productDetail = new ProductDetail[500];
        
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
      
			pInternationController.traceId = jsonObjSearch.getJSONObject("Response").get("TraceId").toString();
			
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
				
				ProductDetail productDetail = new ProductDetail(i, "waiting...", noOfSeatAvailable, noOfSeatAvailable, flightNumber, date, 
			    		stringDepTime, stringArrTime, intTotalAdultChildPrice, intTotalInfantPrice, 0, 0, depAirportCode, arrAirportCode, true, true, stopNumber, duration, 
			    		airlineName, depTimeFloat, arrTimeFloat, pInternationController.traceId, resultIndex, airlineRemark, mode, "1", depTerminal, arrTerminal, 15, 7, "", "", null, craftType);
				
				pInternationController.listProductDetailsOnline.add(productDetail);
				
				pInternationController.listProductDetailsInSearch.add(productDetail);
			}
			
			model.addAttribute("listProducts", pInternationController.listProductDetailsOnline);
			model.addAttribute("responseCode", responseCode);
			JSONObject jsonObjTicketResponseError = jsonObjSearch.getJSONObject("Response").getJSONObject("Error");
			hasErrorArr[0] = jsonObjTicketResponseError.get("ErrorCode").toString();
			hasErrorArr[1] = jsonObjTicketResponseError.get("ErrorMessage").toString();
			
		} catch (JSONException e) {
			
			JSONObject jsonObjTicketResponseError = jsonObjSearch.getJSONObject("Response").getJSONObject("Error");
			hasErrorArr[0] = jsonObjTicketResponseError.get("ErrorCode").toString();
			hasErrorArr[1] = jsonObjTicketResponseError.get("ErrorMessage").toString();
		}
        
        // Close the connection
		connectionSearch.disconnect();
        
        return hasErrorArr;
	}

	@GetMapping("/loading_international_")
    public String performApiRequest(Model model) {
        model.addAttribute("searchURL", searchURL);
        return "loading/loading";
    }

	
	//International return segment******
	
	@GetMapping("/flight_international_search_return_save")
	public String searchHistoryInternationalReturnSave(@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer,
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
				customer = customerService.getByPhone(email);
				model.addAttribute("customer", customer);
				Integer searchId = saveHistoryReturnPart(city1.getCode(), city2.getCode(), date, returnDate, journeyClass, tripType, adultNum, childNum, infantNum, customer);
				searchReturnURL = "/flight_search_international_return_" + searchId +"_"+ sort +"_"+ brand +"_"+ stop +"_"+ activeTime;
				return "redirect:/loading_international_return_";
			} else if (googleLogin != null) {
				email = googleLogin.getEmail();
				customer = customerService.getByPhone(email);
				model.addAttribute("customer", customer);
				Integer searchId = saveHistoryReturnPart(city1.getCode(), city2.getCode(), date, returnDate, journeyClass, tripType, adultNum, childNum, infantNum, customer);
				searchReturnURL = "/flight_search_international_return_" + searchId +"_"+ sort +"_"+ brand +"_"+ stop +"_"+ activeTime;
				return "redirect:/loading_international_return_";
			}else {
				searchReturnURL = "/flight_search-noUser_return_international_"+ city1.getCode() +"_"+ city2.getCode() +"_"+ journeyClass +"_"+ tripType +"_"+ adultNum 
						+"_"+ childNum +"_"+ infantNum +"_"+ strDate +"_"+ strDateReturn +"_"+ sort +"_"+ brand +"_"+ stop +"_"+ activeTime;
				return "redirect:/loading_international_return_";
			}
            
	}
	
	@GetMapping("/flight_search_international_return_{id}_{sortName}_{brand}_{stop}_{activeTime}")
	public String searchFlightDetailsSinglesInter(@PathVariable(name = "id") Integer id, SearchHistory searchHistory, 
			@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer, 
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
			customer = customerService.getByPhone(email);
			model.addAttribute("customer", customer);
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByPhone(email);
			model.addAttribute("customer", customer);
		}
	
		SearchHistory search = searchRepo.findById(id).get();
		
		searchSort(search.getCityOne(), search.getCityTwo(), sortName, model, search.getDate());

		List<Product> getProductBrand = productRepo.findProductByCity(search.getCityOne(), search.getCityTwo(), Sort.by("name").ascending());
		
		Country country = countryRepo.findById(106).get();
		Iterable<City> cities = cityRepo.getCityByCountry(country);
		
		model.addAttribute("cities", cities);
		model.addAttribute("getProductBrand", getProductBrand);
		model.addAttribute("search", search);
		
		String[] responseCode = searchReturnInternationalFlightAPI(search.getCityOne(), search.getCityTwo(), search.getAdultNum(), search.getChildNum(), 
				search.getInfantNum(), sortName, model, search.getDate(), search.getReturnDate());
		
		if (Integer.parseInt(responseCode[0]) != 0) {
			model.addAttribute("errorMsg", responseCode[1]);
		}
		
		return "flight/inter_search_return/search-result_return";
	}
	
	@GetMapping("/flight_search-noUser_return_international_{cityOne}_{cityTwo}_{journeyClass}_{tripType}_{adultNum}_{childNum}_{infantNum}_{strDate}_{strReturnDate}_{sortName}_{brand}_{stop}_{activeTime}")
	public String searchFlightDetailsSinglesNoUserInter(
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

	    Date date = new SimpleDateFormat("yyyy-MM-dd").parse(strDate);
		 
	    Date returnDate = new SimpleDateFormat("yyyy-MM-dd").parse(strReturnDate);

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
		model.addAttribute("returnDate", returnDate);
		model.addAttribute("strReturnDate", strReturnDate);
		model.addAttribute("journeyClass", journeyClass);
		model.addAttribute("tripType", tripType);
		model.addAttribute("adultNum", adultNum);
		model.addAttribute("childNum", childNum);
		model.addAttribute("infantNum", infantNum);
		model.addAttribute("passengerNum", passengerNum);
		model.addAttribute("brands", brands);
		model.addAttribute("stops", stops);
		
		
		String[] responseCode = searchReturnInternationalFlightAPI(cityOne, cityTwo, adultNum, childNum, infantNum, sortName, model, date, returnDate);

		if (Integer.parseInt(responseCode[0]) != 0) {
			model.addAttribute("errorMsg", responseCode[1]);
		}
		
		return "flight/inter_search_return/search-result-noUser_return";
		
	}

	public String[] searchReturnInternationalFlightAPI(String cityOne, String cityTwo, Integer adultNum, Integer childNum, Integer infantNum, String sortName, Model model, Date date, 
			Date returnDate) throws MalformedURLException, IOException {
		// Create URL object with the API end-point
        URL urlSearch =  new URL("https://tboapi.travelboutiqueonline.com/AirAPI_V10/AirService.svc/rest/Search");

        // Open a connection
        HttpURLConnection connectionSearch = (HttpURLConnection) urlSearch.openConnection();
        
        StringBuilder responseBodySearch = new StringBuilder();
        
        int responseCode = onlineFlightService.apiOnlineSearchModReturn(connectionSearch, responseBodySearch, cityOne, cityTwo, adultNum, childNum, infantNum, date, returnDate);

		String traceIdStr = "offline";
		String[] hasErrorArr = new String[2];
		
		pInternationController.listProductDetailsOnline = new ArrayList<ProductDetail>(); 
		pInternationController.listProductDetails = productService.listAllFlights(cityOne, cityTwo, date, traceIdStr, Sort.by(sortName).ascending());
		for (ProductDetail productDetailOffline : pInternationController.listProductDetails) {
			pInternationController.listProductDetailsOnline.add(productDetailOffline);
		}

		pInternationController.listProductDetailsOnlineReturn = new ArrayList<ProductDetail>(); 
		pInternationController.flightMaps = new ArrayList<FlightMap>();
		
//        ProductDetail[] productDetail = new ProductDetail[500];
//        ProductDetail[] productDetailTwo = new ProductDetail[500];
        
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
           
    		pInternationController.traceId = jsonObjSearch.getJSONObject("Response").get("TraceId").toString();
            
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
    			Integer adultPrice = 0, childPrice = 0, infantPrice = 0;
    			Double adultTax = 0d, childTax = 0d, infantTax = 0d;
    			Integer intTotalAdultChildPrice = 0;
    			
    			for (int k = 0; k < jsonArrayFareBreakdown.getJSONArray(i).length(); k++) {
    				jsonObjectAdult.put("FareAdult-" + k, jsonArrayFareBreakdown.getJSONArray(i).getJSONObject(k));
    				String priceOnline = jsonObjectAdult.getJSONObject("FareAdult-" + k).get("BaseFare").toString();
    				String taxOnline = jsonObjectAdult.getJSONObject("FareAdult-" + k).get("Tax").toString();
    				String passengerTypeOnline = jsonObjectAdult.getJSONObject("FareAdult-" + k).get("PassengerType").toString();
    				if (passengerTypeOnline.equals("3")) {
    					infantPrice = Integer.parseInt(priceOnline) / infantNum;
    					infantTax = Double.parseDouble(taxOnline) / infantNum;
    				} else if (passengerTypeOnline.equals("2")) {
    					childPrice = Integer.parseInt(priceOnline) / childNum;
    					childTax = Double.parseDouble(taxOnline) / childNum;
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

    			Integer intTotalInfantPrice = (int) (infantPrice + infantTax);
    			
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
    			
    			StringBuilder buf = new StringBuilder(airlineRemark);
    		    if (buf.length() > 150) {
    		      buf.setLength(150);
    		      buf.append("...");
    		    }
    		    
    		    airlineRemark = buf.toString();
    		    
    			String mode = "Online-data";
    			
    			String craftType = mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(0).get("Craft").toString();
    			
    			ProductDetail productDetail = new ProductDetail(i, "waiting...", noOfSeatAvailable, noOfSeatAvailable, flightNumber, date, 
                		stringDepTime, stringArrTime, intTotalAdultChildPrice/2, intTotalInfantPrice/2, 0, 0, depAirportCode, arrAirportCode, true, true, stopNumber, duration, 
                		airlineName, depTimeFloat, arrTimeFloat, pInternationController.traceId, resultIndex, airlineRemark, mode, "2", depTerminal, arrTerminal, 15, 7, "", "", null, craftType);
    			
    			pInternationController.listProductDetailsOnline.add(productDetail);
    			
    			pInternationController.listProductDetailsInSearch.add(productDetail);

    			FlightMap flightMap = new FlightMap();
    			
				flightMap.setId(i);
				flightMap.setFlightIdTwo(productDetail.getId());
				flightMap.setFlightIdOne(productDetail.getId());
				pInternationController.flightMaps.add(flightMap);
    			
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
        			Integer adultPriceTwo = 0, childPriceTwo = 0, infantPriceTwo = 0;
        			Double adultTaxTwo = 0d, childTaxTwo = 0d, infantTaxTwo = 0d;
        			Integer intTotalAdultChildPriceTwo = 0;
        			
        			for (int k = 0; k < jsonArrayFareBreakdown.getJSONArray(i).length(); k++) {
        				jsonObjectAdultTwo.put("FareAdult-" + k, jsonArrayFareBreakdown.getJSONArray(i).getJSONObject(k));
        				String priceOnline = jsonObjectAdultTwo.getJSONObject("FareAdult-" + k).get("BaseFare").toString();
        				String taxOnline = jsonObjectAdultTwo.getJSONObject("FareAdult-" + k).get("Tax").toString();
        				String passengerTypeOnline = jsonObjectAdultTwo.getJSONObject("FareAdult-" + k).get("PassengerType").toString();
        				if (passengerTypeOnline.equals("3")) {
        					infantPriceTwo = Integer.parseInt(priceOnline) / infantNum;
        					infantTaxTwo = Double.parseDouble(taxOnline) / infantNum;
        				} else if (passengerTypeOnline.equals("2")) {
        					childPriceTwo = Integer.parseInt(priceOnline) / childNum;
        					childTaxTwo = Double.parseDouble(taxOnline) / childNum;
        				} else {
        					adultPriceTwo = Integer.parseInt(priceOnline) / adultNum;
        					adultTaxTwo = Double.parseDouble(taxOnline) / adultNum;
        				}
        			}
        			
        			if (childPriceTwo != 0) {
        				intTotalAdultChildPriceTwo = (int) (((adultPriceTwo + childPriceTwo) / 2) + ((adultTaxTwo + childTaxTwo) / 2));
        			} else {
        				intTotalAdultChildPriceTwo = (int) (adultPriceTwo + adultTaxTwo);
        			}

        			Integer intTotalInfantPriceTwo = (int) (infantPriceTwo + infantTaxTwo);
        			
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
        			
        			StringBuilder buf2 = new StringBuilder(airlineRemarkTwo);
        		    if (buf2.length() > 150) {
        		      buf2.setLength(150);
        		      buf2.append("...");
        		    }
        		    
        		    airlineRemarkTwo = buf2.toString();
        			
        			String modeTwo = "Online-data";
        			
        			String craftTypeTwo = mainObjSegmentTwo.getJSONArray("Segment-" + i).getJSONObject(0).get("Craft").toString();
        			
        			ProductDetail productDetailTwo = new ProductDetail(i, "waiting...", noOfSeatAvailableTwo, noOfSeatAvailableTwo, flightNumberTwo, date, 
                    		stringDepTimeTwo, stringArrTimeTwo, intTotalAdultChildPriceTwo/2, intTotalInfantPriceTwo/2, 0, 0, depAirportCodeTwo, arrAirportCodeTwo, true, true, stopNumberTwo, durationTwo, 
                    		airlineNameTwo, depTimeFloatTwo, arrTimeFloatTwo, pInternationController.traceId, resultIndexTwo, airlineRemarkTwo, modeTwo, "2", depTerminalTwo, arrTerminalTwo, 15, 7, "", "", null, craftTypeTwo);
        			
        			pInternationController.listProductDetailsOnlineReturn.add(productDetailTwo);
    				
        			countTwo++;
	        	    
    				
				} catch (JSONException e) {
					JSONObject jsonObjTicketResponseError2 = jsonObjSearch.getJSONObject("Response").getJSONObject("Error");
					hasErrorArr[0] = jsonObjTicketResponseError2.get("ErrorCode").toString();
					hasErrorArr[1] = jsonObjTicketResponseError2.get("ErrorMessage").toString();
				}
    		}
			model.addAttribute("success", jsonObjSearch.getJSONObject("Response").get("ResponseStatus").toString());
			JSONObject jsonObjTicketResponseError = jsonObjSearch.getJSONObject("Response").getJSONObject("Error");
			hasErrorArr[0] = jsonObjTicketResponseError.get("ErrorCode").toString();
			hasErrorArr[1] = jsonObjTicketResponseError.get("ErrorMessage").toString();
			
		} catch (JSONException e) {
			JSONObject jsonObjTicketResponseError = jsonObjSearch.getJSONObject("Response").getJSONObject("Error");
			hasErrorArr[0] = jsonObjTicketResponseError.get("ErrorCode").toString();
			hasErrorArr[1] = jsonObjTicketResponseError.get("ErrorMessage").toString();
		}

		model.addAttribute("flightMaps", pInternationController.flightMaps);
		model.addAttribute("listProducts", pInternationController.listProductDetailsOnline);
		model.addAttribute("listProductsReturn", pInternationController.listProductDetailsOnlineReturn);
        model.addAttribute("responseCode", responseCode);
        
        // Close the connection
        connectionSearch.disconnect();

        return hasErrorArr;
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

	@GetMapping("/loading_international_return_")
    public String performApiRequestReturn(Model model) {
        model.addAttribute("searchURL", searchReturnURL);
        return "loading/loading";
    }
	

	////Flight sorting segment.
	
	public void searchSort(String cityOne, String cityTwo, String sortName, Model model, Date date) {
		String traceIdStr = "offline";
		
		if (sortName.equals("pnr")) {
			pInternationController.listProductDetails = productService.listAllFlights(cityOne, cityTwo, date, traceIdStr, Sort.by(sortName).ascending());
			model.addAttribute("listProducts", pInternationController.listProductDetails);
		} else if (sortName.equals("price")) {
			pInternationController.listProductDetails = productService.listAllFlightsByPrice(cityOne, cityTwo, date, traceIdStr);
			model.addAttribute("listProducts", pInternationController.listProductDetails);
		} else if (sortName.equals("duration")) {
			pInternationController.listProductDetails = productService.listAllFlights(cityOne, cityTwo, date, traceIdStr, Sort.by(sortName).ascending());
			model.addAttribute("listProducts", pInternationController.listProductDetails);
		} else if (sortName.equals("arrTime")) {
			pInternationController.listProductDetails = productService.listAllFlightsByArrival(cityOne, cityTwo, date, traceIdStr);
			model.addAttribute("listProducts", pInternationController.listProductDetails);
		} else if (sortName.equals("depTime")) {
			pInternationController.listProductDetails = productService.listAllFlightsByDeparture(cityOne, cityTwo, date, traceIdStr);
			model.addAttribute("listProducts", pInternationController.listProductDetails);
		} else if (sortName.equals("brand")) {
			pInternationController.listProductDetails = productService.listAllFlights(cityOne, cityTwo, date, traceIdStr, Sort.by(sortName).ascending());
			model.addAttribute("listProducts", pInternationController.listProductDetails);
		}
	}

}
