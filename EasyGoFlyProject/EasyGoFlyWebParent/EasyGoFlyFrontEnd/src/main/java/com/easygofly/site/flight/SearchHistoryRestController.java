package com.easygofly.site.flight;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easygofly.entity.Brand;
import com.easygofly.entity.City;
import com.easygofly.entity.Country;
import com.easygofly.entity.FlightMap;
import com.easygofly.entity.ProductDetail;
import com.easygofly.entity.Stop;
import com.easygofly.site.LogService;
import com.easygofly.site.setting.CountryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class SearchHistoryRestController {

	@Autowired private OnlineFlightService onlineFlightService;
	@Autowired private SearchHistoryController searchHistoryController;
	@Autowired private ProductDetailService productService;
	@Autowired private LogService logService;
	@Autowired private BrandRepositoy brandRepo;
	@Autowired private ProductDetailsController pController;
	@Autowired private CityRepository cityRepo;
	@Autowired private CountryRepository countryRepo;

	

	
	@PostMapping("/api/flight/tbo-search/calendar-fare")
    public String calendarFareSearchTBO(HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
        response.setContentType("application/json");

        CalendarFareRequest searchOneway = new ObjectMapper().readValue(request.getInputStream(), CalendarFareRequest.class);
        
        if (searchOneway.journey_class.equals("1")) {
			searchOneway.setJourney_class("Economy");
		}
        
        Date origin = new SimpleDateFormat("yyyy-MM-dd").parse(searchOneway.preferredDepartureTime);
        
	    City cityOneFound = cityRepo.getCityByName(searchOneway.origin);
	    City cityTwoFound = cityRepo.getCityByName(searchOneway.destination);
        
        StringBuilder responseBody = onlineFlightService.apiOnlineCalendarFare(cityOneFound.getCode(), cityTwoFound.getCode(), origin);

        return responseBody.toString();
    }
	
	@PostMapping("/api/flight/tbo-search/oneway")
    public List<ProductDetail> flightOnewaySearchTBO(HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
        response.setContentType("application/json");

        SearchOnewayOfflineRequest searchOneway = new ObjectMapper().readValue(request.getInputStream(), SearchOnewayOfflineRequest.class);
        
        Date origin = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH).parse(searchOneway.preferredDepartureTime);
        
        
        if (searchOneway.isFilter) {
        	if (searchHistoryController.pageNum == searchOneway.page) {
        		searchHistoryController.listProductDetailsInSearch = new ArrayList<>();
			} else {
				searchHistoryController.pageNum = searchOneway.page;
			}
		}
        searchHistoryController.listProductDetailsInSearch.addAll(searchFlightAPI(searchOneway, origin));
        
        return searchHistoryController.listProductDetailsInSearch;
    }
	
	@GetMapping("/api/flight/leastFare")
	public List<ProductDetail> leastFareFlights() {
	    return findCheapestFlights(searchHistoryController.listProductDetailsInSearch);
	}
	
	public List<ProductDetail> findCheapestFlights(List<ProductDetail> flights) {
        Map<String, ProductDetail> cheapestFlights = new HashMap<>();

        for (ProductDetail flight : flights) {
            if (!cheapestFlights.containsKey(flight.getBrand().toLowerCase()) ||
                (flight.getPriceADT() + flight.getPriceINF()) < (cheapestFlights.get(flight.getBrand().toLowerCase()).getPriceADT() + cheapestFlights.get(flight.getBrand().toLowerCase()).getPriceINF())) {
                cheapestFlights.put(flight.getBrand().toLowerCase(), flight);
            }
        }

        return new ArrayList<>(cheapestFlights.values());
    }
	
	@GetMapping("/api/fetch_cities")
	public String cityList() {
		Country country = countryRepo.findById(106).get();
		Iterable<City> cities = cityRepo.getCityByCountry(country);

		List<String> cityList = new ArrayList<String>();
	    
	    cities.forEach(city -> {     
	    	String cityBody =  "{"
        		+ "\"id\": " + city.getId() + ", "
                + "\"name\": \"" + city.getName() + "\", "
        		+ "\"code\": \"" + city.getCode() + "\", "
        		+ "\"cityName\": \"" + city.getCityName() + "\""
        		+ "}";	
	        cityList.add(cityBody);
	    });
		
       	String arrayCityList = cityList.stream().map(val -> String.valueOf(val)).collect(Collectors.joining(",", "[", "]"));
	    
	    return arrayCityList;
	}
	
	
	@PostMapping("/api/flight/tbo-search/return")
    public List<FlightMap> flightOnewaySearchReturnTBO(HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
        response.setContentType("application/json");

        SearchOnewayOfflineReturnRequest search = new ObjectMapper().readValue(request.getInputStream(), SearchOnewayOfflineReturnRequest.class);
        
        Date origin = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH).parse(search.preferredDepartureTime);
        Date returnDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH).parse(search.preferredReturnTime);
        
        
        if (search.isFilter) {
        	if (searchHistoryController.pageNum == search.page) {
        		searchHistoryController.listProductDetailsInSearch = new ArrayList<>();
			} else {
				searchHistoryController.pageNum = search.page;
			}
		}
        
        return searchFlightReturnAPI(search, origin, returnDate);
    }


	@GetMapping("/api/brand_list")
	public String brandList() {
		Iterable<Brand> brands = brandRepo.findAll();
		List<String> brandList = new ArrayList<String>();
	    
		brands.forEach(brand -> {     
	    	String brandBody =  "{"
        		+ "\"id\": " + brand.getId() + ", "
                + "\"name\": \"" + brand.getName() + "\", "
        		+ "\"photo\": \"" + brand.getPhotosImagePath() + "\""
        		+ "}";	
	    	brandList.add(brandBody);
	    });
		
       	String arrayBrandList = brandList.stream().map(val -> String.valueOf(val)).collect(Collectors.joining(",", "[", "]"));

		return arrayBrandList;
	}
	
	public List<ProductDetail> searchOffline(String cityOne, String cityTwo, Date date) {
		String traceIdStr = "offline";
		
		return productService.listAllFlightsByPrice(cityOne, cityTwo, date, traceIdStr);
	}
	
	public List<ProductDetail> searchFlightAPI(SearchOnewayOfflineRequest searchOneway, Date date) throws IllegalArgumentException, Exception {
		List<ProductDetail> allFlights = new ArrayList<>();
		searchOneway.getPage();
	    allFlights = searchOffline(searchOneway.getOrigin(), searchOneway.getDestination(), date);

        //AirIQ ......
        
//	    try {
//	    	StringBuilder responseBodySearchAirIQ = onlineFlightService.apiAirIQSearch(searchOneway.getOrigin(), searchOneway.getDestination(), searchOneway.getAdultCount(), searchOneway.getChildCount(), searchOneway.getInfantCount(), date);
//        
//			//AirIQ response.......
//	
//	        JSONObject jsonObjSearchAirIQ = new JSONObject(responseBodySearchAirIQ.toString());
//	        System.out.println(jsonObjSearchAirIQ);
//	        logService.generateLog(jsonObjSearchAirIQ.toString());
//			
//		
//			JSONArray jsonArraysAirIQ = jsonObjSearchAirIQ.getJSONArray("data");
//			JSONObject mainObjAirIQ = new JSONObject();
//			
//			System.out.println("JSON data: " + jsonObjSearchAirIQ);
//			String brandImage = "";
//			
//			for (int i = 0; i < jsonArraysAirIQ.length(); i++) {
//				mainObjAirIQ.put("data-" + i, jsonArraysAirIQ.getJSONObject(i));
//				
//				System.out.println(mainObjAirIQ);
//		        logService.generateLog("Main : " + mainObjAirIQ.toString());
//				
//				String noOfSeatAvailable = mainObjAirIQ.getJSONObject("data-" + i).get("pax").toString();
//
//		        logService.generateLog("Seats" + noOfSeatAvailable);
//		        
//				String flightNumber = mainObjAirIQ.getJSONObject("data-" + i).get("flight_number").toString();
//				String stringDepTime = mainObjAirIQ.getJSONObject("data-" + i).get("departure_time").toString();
//				String stringArrTime = mainObjAirIQ.getJSONObject("data-" + i).get("arival_time").toString();
//				double parsePriceADT = Double.parseDouble(mainObjAirIQ.getJSONObject("data-" + i).get("price").toString());
//				double parsePriceINF = Double.parseDouble(mainObjAirIQ.getJSONObject("data-" + i).get("infant_price").toString());
//				float intTotalAdultChildPrice = (float) (parsePriceADT * (searchOneway.getAdultCount() + searchOneway.getChildCount()));
//				float intTotalInfantPrice = (float) (parsePriceINF * searchOneway.getInfantCount());
//				String depAirportCode = mainObjAirIQ.getJSONObject("data-" + i).get("origin").toString();
//				String arrAirportCode = mainObjAirIQ.getJSONObject("data-" + i).get("destination").toString();
//				int stopNumber = 0;
//				if (mainObjAirIQ.getJSONObject("data-" + i).get("flight_route").toString() == "Non - Stop") {
//					stopNumber = 0;
//				}
//				String[] arrayDepDate = mainObjAirIQ.getJSONObject("data-" + i).get("departure_date").toString().split("/");
//				String[] arrayArrDate = mainObjAirIQ.getJSONObject("data-" + i).get("arival_date").toString().split("/");
//				String[] arrayDepTime = mainObjAirIQ.getJSONObject("data-" + i).get("departure_time").toString().split(":");
//				String[] arrayArrTime = mainObjAirIQ.getJSONObject("data-" + i).get("arival_time").toString().split(":");
//				int totalDepinMin = (Integer.parseInt(arrayDepTime[0]) * 60)  + Integer.parseInt(arrayDepTime[1]);
//				int totalArrinMin = 0;
//				if (Integer.parseInt(arrayDepDate[2]) < Integer.parseInt(arrayArrDate[2])) {
//					totalArrinMin = ((Integer.parseInt(arrayArrTime[0]) + 24 ) * 60 )  + Integer.parseInt(arrayArrTime[1]);
//				} else {
//					totalArrinMin = (Integer.parseInt(arrayArrTime[0]) * 60)  + Integer.parseInt(arrayArrTime[1]);
//				}
//				int duration = totalArrinMin - totalDepinMin;
//				String airlineName = mainObjAirIQ.getJSONObject("data-" + i).get("airline").toString();
//				float depTimeFloat = 0;
//				float arrTimeFloat = 0;
//				String resultIndex = "" + i;
//				String airlineRemark = "";
//				String mode = "AirIQ";
//				String depTerminal = "T1";
//				String arrTerminal = "T1";
//				String craftType = mainObjAirIQ.getJSONObject("data-" + i).get("flight_number").toString();
//				String ticketId = mainObjAirIQ.getJSONObject("data-" + i).get("ticket_id").toString();
//				float totalPayablePrice = 0;
//
//				Brand brand =  brandRepo.getBrandByName(airlineName);
//				if ( brand == null ) {
//					brandImage = "/images/no-image.png";
//				} else {
//					brandImage = brand.getPhotosImagePath();
//				}
//
//		        logService.generateLog("Seats" + noOfSeatAvailable);
//
//				ProductDetail productDetail = new ProductDetail(i + 1000, "waiting...", noOfSeatAvailable, noOfSeatAvailable, flightNumber, date, 
//			    		stringDepTime, stringArrTime, (float) intTotalAdultChildPrice, intTotalInfantPrice, 0, 0, depAirportCode, arrAirportCode, true, true, stopNumber, duration, 
//			    		airlineName, depTimeFloat, arrTimeFloat, ticketId, resultIndex, airlineRemark, mode, "1", depTerminal, arrTerminal, 15, 7, "", "", null, craftType, brandImage, "" + totalPayablePrice);
//				
//
//				System.out.println(productDetail);
//
//				allFlights.add(productDetail);
//			}
//		} catch (Exception e) {
//
//			e.printStackTrace();			
//			
//		}
//		
//
//        //Master Travels ......
//		 try {
//			StringBuilder responseBodySearchMTravels = onlineFlightService.apiMasterSearch(searchOneway.getOrigin(), searchOneway.getDestination(), searchOneway.getAdultCount(), searchOneway.getChildCount(), searchOneway.getInfantCount(), date);
//		    
//			//Master Travels response.......
//		
//		    JSONObject jsonObjSearchMTravels = new JSONObject(responseBodySearchMTravels.toString());
//		    System.out.println(jsonObjSearchMTravels);
//		    logService.generateLog(jsonObjSearchMTravels.toString());
//        
//       
//			JSONArray jsonArraysMTravels = jsonObjSearchMTravels.getJSONArray("data");
//			JSONObject mainObjMTravels = new JSONObject();
//			String bookingTokenId = jsonObjSearchMTravels.get("booking_token_id").toString();
//			String brandImage = "";
//
//			for (int i = 0; i < jsonArraysMTravels.length(); i++) {
//				mainObjMTravels.put("data-" + i, jsonArraysMTravels.getJSONObject(i));
//				String noOfSeatAvailable = mainObjMTravels.getJSONObject("data-" + i).get("available_seats").toString();
//
//		        logService.generateLog("Seats" + noOfSeatAvailable);
//		        
//				String flightNumber = mainObjMTravels.getJSONObject("data-" + i).get("flight_number").toString();
//				String stringDepTime = mainObjMTravels.getJSONObject("data-" + i).get("dep_time").toString();
//				String stringArrTime = mainObjMTravels.getJSONObject("data-" + i).get("arr_time").toString();
//				double parsePriceADT = Double.parseDouble(mainObjMTravels.getJSONObject("data-" + i).get("per_adult_child_price").toString());
//				double parsePriceINF = Double.parseDouble(mainObjMTravels.getJSONObject("data-" + i).get("per_infant_price").toString());
//				float intTotalAdultChildPrice = (float) (parsePriceADT * (searchOneway.getAdultCount() + searchOneway.getChildCount()));
//				float intTotalInfantPrice = (float) (parsePriceINF * searchOneway.getInfantCount());
//				String depAirportCode = mainObjMTravels.getJSONObject("data-" + i).get("dep_city_code").toString();
//				String arrAirportCode = mainObjMTravels.getJSONObject("data-" + i).get("arr_city_code").toString();
//				int stopNumber = Integer.parseInt(mainObjMTravels.getJSONObject("data-" + i).get("no_of_stop").toString());
//				String[] arrayDepDate = mainObjMTravels.getJSONObject("data-" + i).get("onward_date").toString().split("-");
//				String[] arrayArrDate = mainObjMTravels.getJSONObject("data-" + i).get("arr_date").toString().split("-");
//				String[] arrayDepTime = mainObjMTravels.getJSONObject("data-" + i).get("dep_time").toString().split(":");
//				String[] arrayArrTime = mainObjMTravels.getJSONObject("data-" + i).get("arr_time").toString().split(":");
//				int totalDepinMin = (Integer.parseInt(arrayDepTime[0]) * 60)  + Integer.parseInt(arrayDepTime[1]);
//				int totalArrinMin = 0;
//				if (Integer.parseInt(arrayDepDate[2]) < Integer.parseInt(arrayArrDate[2])) {
//					totalArrinMin = ((Integer.parseInt(arrayArrTime[0]) + 24 ) * 60 )  + Integer.parseInt(arrayArrTime[1]);
//				} else {
//					totalArrinMin = (Integer.parseInt(arrayArrTime[0]) * 60)  + Integer.parseInt(arrayArrTime[1]);
//				}
//				int duration = totalArrinMin - totalDepinMin;
//				String airlineName = mainObjMTravels.getJSONObject("data-" + i).get("airline_name").toString();
//				float depTimeFloat = 0;
//				float arrTimeFloat = 0;
//				String resultIndex = mainObjMTravels.getJSONObject("data-" + i).get("id").toString();
//				String airlineRemark = "";
//				String mode = "MasterTravels";
//				String depTerminal = mainObjMTravels.getJSONObject("data-" + i).get("dep_terminal_no").toString();
//				String arrTerminal = mainObjMTravels.getJSONObject("data-" + i).get("arr_terminal_no").toString();
//				String craftType = mainObjMTravels.getJSONObject("data-" + i).get("static").toString();
//				double totalPayablePrice = Double.parseDouble(mainObjMTravels.getJSONObject("data-" + i).get("total_payable_price").toString());
//				
//				float grandTotal = (float) totalPayablePrice;
//
//				Brand brand =  brandRepo.getBrandByName(airlineName);
//				if ( brand == null ) {
//					brandImage = "/images/no-image.png";
//				} else {
//					brandImage = brand.getPhotosImagePath();
//				}
//				
//				System.out.println("total: " + mainObjMTravels.getJSONObject("data-" + i).get("total_payable_price").toString());
//				
//				System.out.println("Grand total: " + grandTotal);
//
//		        logService.generateLog("Seats" + noOfSeatAvailable);
//
//				ProductDetail productDetail = new ProductDetail(i + 2000, "waiting...", noOfSeatAvailable, noOfSeatAvailable, flightNumber, date, 
//			    		stringDepTime, stringArrTime, (float) intTotalAdultChildPrice, intTotalInfantPrice, 0, 0, depAirportCode, arrAirportCode, true, true, stopNumber, duration, 
//			    		airlineName, depTimeFloat, arrTimeFloat, bookingTokenId, resultIndex, airlineRemark, mode, "1", depTerminal, arrTerminal, 15, 7, "", "", null, craftType, brandImage, "" + grandTotal);
//				
//			
//				System.out.println(productDetail);
//
//				allFlights.add(productDetail);
//			}
//			
//		} catch (Exception e) {
//
//			e.printStackTrace();			
//		
//		}
//
//		
//        
//        //Ease2fly ......
//        
//		StringBuilder responseBodySearchEase2fly;
//
//		//Ease2fly response.......
//        
//        try {
//        	responseBodySearchEase2fly = onlineFlightService.apiEase2flySearch(searchOneway.getOrigin(), searchOneway.getDestination(), searchOneway.getAdultCount(), searchOneway.getChildCount(), searchOneway.getInfantCount(), date);
//
//            JSONObject jsonObjSearchEase2fly = new JSONObject(responseBodySearchEase2fly.toString());
//            System.out.println(jsonObjSearchEase2fly);
//            logService.generateLog(jsonObjSearchEase2fly.toString());
//            
//			JSONArray jsonArraysEase2fly = jsonObjSearchEase2fly.getJSONArray("result");
//			JSONObject mainObjEase2fly = new JSONObject();
//			String refreshToken = jsonObjSearchEase2fly.get("refresh_token").toString();
//			String brandImage = "";
//
//			for (int i = 0; i < jsonArraysEase2fly.length(); i++) {
//				mainObjEase2fly.put("result-" + i, jsonArraysEase2fly.getJSONObject(i));
//				String noOfSeatAvailable = mainObjEase2fly.getJSONObject("result-" + i).get("seat").toString();
//
//		        logService.generateLog("Seats" + noOfSeatAvailable);
//		        
//				String flightNumber = mainObjEase2fly.getJSONObject("result-" + i).get("flight_no").toString();
//				String stringDepTime[] = mainObjEase2fly.getJSONObject("result-" + i).get("departure_time").toString().split(" ");
//				String stringArrTime[] = mainObjEase2fly.getJSONObject("result-" + i).get("arrival_time").toString().split(" ");
//				double parsePriceADT = Double.parseDouble(mainObjEase2fly.getJSONObject("result-" + i).get("total_fare").toString());
//				double parsePriceINF = Double.parseDouble(mainObjEase2fly.getJSONObject("result-" + i).get("infant_charge").toString());
//				float intTotalInfantPrice = (float) (parsePriceINF * searchOneway.getInfantCount());
//				float intTotalAdultChildPrice = (float) (parsePriceADT) - intTotalInfantPrice;
//				String depAirportCode = mainObjEase2fly.getJSONObject("result-" + i).get("origin").toString();
//				String arrAirportCode = mainObjEase2fly.getJSONObject("result-" + i).get("destination").toString();
//				String pnr = mainObjEase2fly.getJSONObject("result-" + i).get("pnr").toString();
//				
//				String[] arrayDepDate = mainObjEase2fly.getJSONObject("result-" + i).get("departure_date").toString().split("-");
//				String[] arrayArrDate = mainObjEase2fly.getJSONObject("result-" + i).get("arrival_date").toString().split("-");
//				String[] arrayDepTime = stringDepTime[0].split(":");
//				String[] arrayArrTime = stringArrTime[0].split(":");
//				int totalDepinMin = (Integer.parseInt(arrayDepTime[0]) * 60)  + Integer.parseInt(arrayDepTime[1]);
//				int totalArrinMin = 0;
//				if (Integer.parseInt(arrayDepDate[2]) < Integer.parseInt(arrayArrDate[2])) {
//					totalArrinMin = ((Integer.parseInt(arrayArrTime[0]) + 24 ) * 60 )  + Integer.parseInt(arrayArrTime[1]);
//				} else {
//					totalArrinMin = (Integer.parseInt(arrayArrTime[0]) * 60)  + Integer.parseInt(arrayArrTime[1]);
//				}
//				int duration = totalArrinMin - totalDepinMin;
//				String airlineName = mainObjEase2fly.getJSONObject("result-" + i).get("airline_name").toString();
//				float depTimeFloat = 0;
//				float arrTimeFloat = 0;
//				String resultIndex = mainObjEase2fly.getJSONObject("result-" + i).get("id").toString();
//				String airlineRemark = "";
//				String mode = "Ease2fly";
//				String depTerminal = "T1";
//				String arrTerminal = "T1";
//				String craftType = mainObjEase2fly.getJSONObject("result-" + i).get("d_owner").toString();
//				double totalPayablePrice = Double.parseDouble(mainObjEase2fly.getJSONObject("result-" + i).get("total_fare").toString());
//				
//				float grandTotal = (float) totalPayablePrice;
//
//				Brand brand =  brandRepo.getBrandByName(airlineName);
//				if ( brand == null ) {
//					brandImage = "/images/no-image.png";
//				} else {
//					brandImage = brand.getPhotosImagePath();
//				}
//
//				System.out.println("total: " + mainObjEase2fly.getJSONObject("result-" + i).get("total_fare").toString());
//				
//				System.out.println("Grand total: " + grandTotal);
//
//		        logService.generateLog("Seats" + noOfSeatAvailable);
//
//				ProductDetail productDetail = new ProductDetail(i + 3000, pnr, noOfSeatAvailable, noOfSeatAvailable, flightNumber, date, 
//			    		stringDepTime[0], stringArrTime[0], (float) intTotalAdultChildPrice, intTotalInfantPrice, 0, 0, depAirportCode, arrAirportCode, true, true, 0, duration, 
//			    		airlineName, depTimeFloat, arrTimeFloat, refreshToken, resultIndex, airlineRemark, mode, "1", depTerminal, arrTerminal, 15, 7, "", "", null, craftType, brandImage, "" + grandTotal);
//				
//			
//				System.out.println(productDetail);
//
//				allFlights.add(productDetail);
//
//			}
//			
//		} catch (Exception e) {
//			e.printStackTrace();	
//		}
			
		
		

        try {
        	
        	StringBuilder responseBodySearch = onlineFlightService.apiOnlineSearchMod(searchOneway.getOrigin(), searchOneway.getDestination(), searchOneway.getAdultCount(), searchOneway.getChildCount(), searchOneway.getInfantCount(), date, "1");
		
	        
			//TBO response.......
	        
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
				
				System.out.println(jsonArrays.length());
				
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
					double adultPrice = 0, childPrice = 0, infantPrice = 0, adultTax = 0, childTax = 0, infantTax = 0;
					double intTotalAdultChildPrice = 0;
					
					for (int j = 0; j < jsonArrayFareBreakdown.getJSONArray(i).length(); j++) {
						jsonObjectAdult.put("FareAdult-" + j, jsonArrayFareBreakdown.getJSONArray(i).getJSONObject(j));
						String priceOnline = jsonObjectAdult.getJSONObject("FareAdult-" + j).get("BaseFare").toString();
						String taxOnline = jsonObjectAdult.getJSONObject("FareAdult-" + j).get("Tax").toString();
						String passengerTypeOnline = jsonObjectAdult.getJSONObject("FareAdult-" + j).get("PassengerType").toString();
						if (passengerTypeOnline.equals("3")) {
							infantPrice = Double.parseDouble(priceOnline) / searchOneway.getInfantCount();
							infantTax = Double.parseDouble(taxOnline) / searchOneway.getInfantCount();
						} else if (passengerTypeOnline.equals("2")) {
							childPrice = Double.parseDouble(priceOnline) / searchOneway.getChildCount();
							childTax = Double.parseDouble(taxOnline) / searchOneway.getChildCount();
						} else {
							adultPrice = Double.parseDouble(priceOnline) / searchOneway.getAdultCount();
							adultTax = Double.parseDouble(taxOnline) / searchOneway.getAdultCount();
						}
					}
					
					if (childPrice != 0) {
						intTotalAdultChildPrice = ((adultPrice + childPrice) / 2) + ((adultTax + childTax) / 2);
					} else {
						intTotalAdultChildPrice = adultPrice + adultTax;
					}

					Integer intTotalInfantPrice = (int) (infantPrice + infantTax);
					Integer duration = 0;
					if (mainObjSegment.getJSONArray("Segment-" + i).length() > 1) {
						duration = Integer.parseInt(mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(mainObjSegment.getJSONArray("Segment-" + i).length() - 1)
								.get("AccumulatedDuration").toString());
					} else {
						duration = Integer.parseInt(mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(0).get("Duration").toString());
					}
					
					
					
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
					
					String brandImage = "";
					Brand brand =  brandRepo.getBrandByName(airlineName);
					if ( brand == null ) {
						brandImage = "/images/no-image.png";
					} else {
						brandImage = brand.getPhotosImagePath();
					}
					 
					ProductDetail productDetail = new ProductDetail(i + 1, "waiting...", noOfSeatAvailable, noOfSeatAvailable, flightNumber, date, 
				    		stringDepTime, stringArrTime, (float) intTotalAdultChildPrice, intTotalInfantPrice, 0, 0, depAirportCode, arrAirportCode, true, true, stopNumber, duration, 
				    		airlineName, depTimeFloat, arrTimeFloat, pController.traceId, resultIndex, airlineRemark, mode, "1", depTerminal, arrTerminal, 15, 7, "", "", null, craftType, 
				    		brandImage, null);
					
					List<Stop> stops = productDetail.getStops();
					for (int j = 0; j < mainObjSegment.getJSONArray("Segment-" + i).length(); j++) {
						String totalTime = "0";
						String groundTime = mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(j).get("GroundTime").toString();
						String dura1 = mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(j).get("Duration").toString();
						try {
							totalTime = mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(j).get("AccumulatedDuration").toString();
							groundTime = mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(j).get("GroundTime").toString();
							} catch (Exception e) {
								totalTime = "0";
								groundTime = "0";
							}
						String org = mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(j).getJSONObject("Origin").getJSONObject("Airport").get("AirportCode").toString();
						String des = mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(j).getJSONObject("Destination").getJSONObject("Airport").get("AirportCode").toString();
						String arrT =  mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(j).getJSONObject("Origin").get("DepTime").toString();
						String depT =  mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(j).getJSONObject("Destination").get("ArrTime").toString();
						String flightNu = mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(j).getJSONObject("Airline").get("AirlineCode").toString() 
								+ "-" + mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(j).getJSONObject("Airline").get("FlightNumber").toString();
						String bran = mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(j).getJSONObject("Airline").get("AirlineName").toString();
					
						Stop stop = new Stop(org, des, arrT, depT, totalTime, groundTime, dura1, flightNu, bran, productDetail);
						
						stops.add(stop);
						
					}
					allFlights.add(productDetail);
				}
				
//				JSONObject jsonObjTicketResponseError = jsonObjSearch.getJSONObject("Response").getJSONObject("Error");
				
			} catch (JSONException e) {
//				JSONObject jsonObjTicketResponseError = jsonObjSearch.getJSONObject("Response").getJSONObject("Error");
			}
	        
		} catch (Exception e) {
			e.printStackTrace();
		} 
        
        if (searchOneway.isFilter) {
    		allFlights = filterFlights(allFlights, searchOneway.airline, searchOneway.minTime, searchOneway.maxTime, searchOneway.maxStops, searchOneway.minPrice, searchOneway.maxPrice);
        	allFlights = sortFlight(searchOneway.sort, allFlights);
        } else {
        	allFlights = sortFlight(searchOneway.sort, allFlights);
		}
        
		List<ProductDetail> paginatedFlights = new ArrayList<>();
        int fromIndex = searchOneway.getPage() * searchOneway.getSize();
        int toIndex = Math.min(fromIndex + searchOneway.getSize(), allFlights.size());

        for (int i = fromIndex; i < toIndex; i++) {
            paginatedFlights.add(allFlights.get(i));
        }
    	
        return paginatedFlights;
	}
	
	public List<FlightMap> searchFlightReturnAPI(SearchOnewayOfflineReturnRequest searchReturn, Date date, Date returnDate) throws IllegalArgumentException, Exception {
		List<FlightMap> allFlightMaps = new ArrayList<>();
		searchReturn.getPage();
		List<ProductDetail> allFlightOneways = searchOffline(searchReturn.getOrigin(), searchReturn.getDestination(), date);
		List<ProductDetail> allFlightReturns = searchOffline(searchReturn.getDestination(), searchReturn.getOrigin(), returnDate);
        
		for (int i = 0; i < allFlightOneways.size(); i++) {
			FlightMap flightMap = new FlightMap();
			flightMap.setFlightIdOne(allFlightOneways.get(i).getId());
			
			if (allFlightReturns.size() != 0) {
				try {
					flightMap.setFlightIdTwo(allFlightReturns.get(i).getId());
					allFlightMaps.add(flightMap);
					
				} catch (Exception e) {
					e.printStackTrace();
					// TODO: handle exception
				}
			}
		}
		
		try {
			// Create URL object with the API end-point
	        StringBuilder responseBodySearch = onlineFlightService.apiOnlineSearchModReturn(searchReturn.getOrigin(), searchReturn.getDestination(), 
	        		searchReturn.getAdultCount(), searchReturn.getChildCount(), searchReturn.getInfantCount(), date, returnDate);
			
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
	    					infantPrice = Integer.parseInt(priceOnline) / searchReturn.getInfantCount();
	    					infantTax = Integer.parseInt(taxOnline) / searchReturn.getInfantCount();
	    				} else if (passengerTypeOnline.equals("2")) {
	    					childPrice = Integer.parseInt(priceOnline) / searchReturn.getChildCount();
	    					childTax = Integer.parseInt(taxOnline) / searchReturn.getChildCount();
	    				} else {
	    					adultPrice = Integer.parseInt(priceOnline) / searchReturn.getAdultCount();
	    					adultTax = Integer.parseInt(taxOnline) / searchReturn.getAdultCount();
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
	    			
	    			allFlightOneways.add(productDetail);
	    			
	    		}
				
			} catch (Exception e) {
				e.printStackTrace();
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
							infantPrice = Integer.parseInt(priceOnline) / searchReturn.getInfantCount();
							infantTax = Integer.parseInt(taxOnline) / searchReturn.getInfantCount();
						} else if (passengerTypeOnline.equals("2")) {
							childPrice = Integer.parseInt(priceOnline) / searchReturn.getChildCount();
							childTax = Integer.parseInt(taxOnline) / searchReturn.getChildCount();
						} else {
							adultPrice = Integer.parseInt(priceOnline) / searchReturn.getAdultCount();
							adultTax = Integer.parseInt(taxOnline) / searchReturn.getAdultCount();
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
					
					
					allFlightReturns.add(productDetailTwo);
					
					String resultStrTwo = productDetailTwo.getResultIndex();
					String[] arrayResultTwo = resultStrTwo.split("B");

					FlightMap flightMap = new FlightMap();
					
					for (ProductDetail productDetail2 : allFlightOneways) {
						String resultStr = productDetail2.getResultIndex();
						String[] arrayResult = resultStr.split("B");
						if (arrayResultTwo[1].equals(arrayResult[1])) {
							flightMap.setId(i);
							flightMap.setFlightIdOne(productDetailTwo.getId());
							flightMap.setFlightIdTwo(productDetail2.getId());
							allFlightMaps.add(flightMap);
						}
					}		
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
	        
		} catch (Exception e2) {
			
			e2.printStackTrace();
			// TODO: handle exception
		}

		return allFlightMaps;
	}

	private List<ProductDetail> sortFlight(String sortName, List<ProductDetail> listProduct) {
		
		if (sortName.equals("priceADT")) {
			listProduct.sort((p1, p2) -> {
		        float priceCompare = Float.compare(p1.getPriceADT(), p2.getPriceADT());
				return (int) priceCompare;
			});
		} else if (sortName.equals("duration")) {
			listProduct.sort((p1, p2) -> {
		        float duration = Float.compare(p1.getDuration(), p2.getDuration());
				return (int) duration;
			});
		} else if (sortName.equals("arrTimeInteger")) {
			listProduct.sort((p1, p2) -> {
		        float arrTime = Float.compare(p1.getArrTimeInteger(), p2.getArrTimeInteger());
				return (int) arrTime;
			});
		} else if (sortName.equals("depTimeInteger")) {
			listProduct.sort((p1, p2) -> {
		        float depTime = Float.compare(p1.getDepTimeInteger(), p2.getDepTimeInteger());
				return (int) depTime;
			});
		} else if (sortName.equals("brand")) {
			listProduct.sort(Comparator.comparing(ProductDetail::getBrand));
		}
		
		return listProduct;
	}

	public  List<ProductDetail> filterFlights(List<ProductDetail> flights, String airline, String minTime, String maxTime, Integer maxStops, Double minPrice, Double maxPrice) {
		
        System.out.println("33333333333333333333333333333333333333333333333333333333333333333333 " + airline + " " + minTime + " " + maxTime + " " + maxStops + " " + minPrice + " " + maxPrice);
        
        return flights.stream()
                .filter(flight -> {
                	if (airline == null || airline.equals("")) return flight.getBrand().equalsIgnoreCase(flight.getBrand());
					
                	return flight.getBrand().equalsIgnoreCase(airline);
                }) // Filter by airline
                .filter(flight -> {
                	if (minTime.equals("") || minTime == null) return Float.parseFloat(flight.getDepTime().split(":")[0] + "." + flight.getDepTime().split(":")[1]) >= 0.0 && Float.parseFloat(flight.getDepTime().split(":")[0] + "." + flight.getDepTime().split(":")[1]) <= 24.0;
                	
                	if (minTime.contains("01:00")) return Float.parseFloat(flight.getDepTime().split(":")[0] + "." + flight.getDepTime().split(":")[1]) >= 0.0 && Float.parseFloat(flight.getDepTime().split(":")[0] + "." + flight.getDepTime().split(":")[1]) < 8.0;
					
                	return Float.parseFloat(flight.getDepTime().split(":")[0] + "." + flight.getDepTime().split(":")[1]) >= Float.parseFloat(minTime.split(":")[0] + "." + minTime.split(":")[1]) && Float.parseFloat(flight.getDepTime().split(":")[0] + "." + flight.getDepTime().split(":")[1]) <= Float.parseFloat(maxTime.split(":")[0] + "." + maxTime.split(":")[1]);
                }) // Filter by time range
                .filter(flight -> {
                	if (maxStops == null) return flight.getStopNum() >= 0;
                	
                	if (maxStops == 3) return flight.getStopNum() >= 0;
					
                	return flight.getStopNum() <= maxStops;
                }) // Filter by stops
                .filter(flight -> (flight.getPriceADT() + flight.getPriceINF()) >= minPrice && (flight.getPriceADT() + flight.getPriceINF()) <= maxPrice) // Filter by price range
                .collect(Collectors.toList());
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
        private boolean isFilter;
        private String mode;
        private String sort;
        private Integer page;
        private Integer size;
        private String airline; 
        private String minTime; 
        private String maxTime; 
        private Integer maxStops; 
        private Double minPrice; 
        private Double maxPrice;
        
		public Integer getPage() {
			return page;
		}
		public void setPage(Integer page) {
			this.page = page;
		}
		public Integer getSize() {
			return size;
		}
		public void setSize(Integer size) {
			this.size = size;
		}
		public String getSort() {
			return sort;
		}
		public void setSort(String sort) {
			this.sort = sort;
		}
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
		public String getMode() {
			return mode;
		}
		public void setMode(String mode) {
			this.mode = mode;
		}
		public boolean isFilter() {
			return isFilter;
		}
		public void setFilter(boolean isFilter) {
			this.isFilter = isFilter;
		}
		public String getAirline() {
			return airline;
		}
		public void setAirline(String airline) {
			this.airline = airline;
		}
		public String getMinTime() {
			return minTime;
		}
		public void setMinTime(String minTime) {
			this.minTime = minTime;
		}
		public String getMaxTime() {
			return maxTime;
		}
		public void setMaxTime(String maxTime) {
			this.maxTime = maxTime;
		}
		public Integer getMaxStops() {
			return maxStops;
		}
		public void setMaxStops(Integer maxStops) {
			this.maxStops = maxStops;
		}
		public Double getMinPrice() {
			return minPrice;
		}
		public void setMinPrice(Double minPrice) {
			this.minPrice = minPrice;
		}
		public Double getMaxPrice() {
			return maxPrice;
		}
		public void setMaxPrice(Double maxPrice) {
			this.maxPrice = maxPrice;
		}
	
    }

    @SuppressWarnings("unused")
	private static class SearchOnewayOfflineReturnRequest {
    	private Integer adultCount;
        private Integer childCount;
        private Integer infantCount;
        private String journey_class;
        private String origin;
        private String destination;
        private String preferredDepartureTime;
        private String preferredReturnTime;
        private boolean isFilter;
        private String mode;
        private String sort;
        private Integer page;
        private Integer size;
        private String airline; 
        private String minTime; 
        private String maxTime; 
        private Integer maxStops; 
        private Double minPrice; 
        private Double maxPrice;
        
		public Integer getPage() {
			return page;
		}
		public void setPage(Integer page) {
			this.page = page;
		}
		public String getPreferredReturnTime() {
			return preferredReturnTime;
		}
		public void setPreferredReturnTime(String preferredReturnTime) {
			this.preferredReturnTime = preferredReturnTime;
		}
		public Integer getSize() {
			return size;
		}
		public void setSize(Integer size) {
			this.size = size;
		}
		public String getSort() {
			return sort;
		}
		public void setSort(String sort) {
			this.sort = sort;
		}
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
		public String getMode() {
			return mode;
		}
		public void setMode(String mode) {
			this.mode = mode;
		}
		public boolean isFilter() {
			return isFilter;
		}
		public void setFilter(boolean isFilter) {
			this.isFilter = isFilter;
		}
		public String getAirline() {
			return airline;
		}
		public void setAirline(String airline) {
			this.airline = airline;
		}
		public String getMinTime() {
			return minTime;
		}
		public void setMinTime(String minTime) {
			this.minTime = minTime;
		}
		public String getMaxTime() {
			return maxTime;
		}
		public void setMaxTime(String maxTime) {
			this.maxTime = maxTime;
		}
		public Integer getMaxStops() {
			return maxStops;
		}
		public void setMaxStops(Integer maxStops) {
			this.maxStops = maxStops;
		}
		public Double getMinPrice() {
			return minPrice;
		}
		public void setMinPrice(Double minPrice) {
			this.minPrice = minPrice;
		}
		public Double getMaxPrice() {
			return maxPrice;
		}
		public void setMaxPrice(Double maxPrice) {
			this.maxPrice = maxPrice;
		}
	
    }
    
    @SuppressWarnings("unused")
   	private static class CalendarFareRequest {
           private String journey_class;
           private String origin;
           private String destination;
           private String preferredDepartureTime;
           
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
   		
   	
       }
    
    
    public static class FlightResponse {
        public int ResponseStatus;
        public Error Error;
        public String TraceId;
        public String Origin;
        public String Destination;
        public List<List<Result>> Results;
    }

    public static class Error {
        public int ErrorCode;
        public String ErrorMessage;
    }

    public static class Result {
        public String ResultIndex;
        public int Source;
        public boolean IsLCC;
        public boolean IsRefundable;
        public String AirlineRemark;
        public Fare Fare;
        public List<FareBreakdown> FareBreakdown;
        public List<List<Segment>> Segments;
        public String LastTicketDate;
        public String TicketAdvisory;
        public List<FareRule> FareRules;
        public String AirlineCode;
        public String ValidatingAirline;
    }

    public static class Fare {
        public String Currency;
        public double BaseFare;
        public double Tax;
        public double YQTax;
        public double AdditionalTxnFeeOfrd;
        public double AdditionalTxnFeePub;
        public double OtherCharges;
        public List<ChargeBU> ChargeBU;
        public double Discount;
        public double PublishedFare;
        public double CommissionEarned;
        public double PLBEarned;
        public double IncentiveEarned;
        public double OfferedFare;
        public double TdsOnCommission;
        public double TdsOnPLB;
        public double TdsOnIncentive;
        public double ServiceFee;
    }

    public static class ChargeBU {
        public String key;
        public double value;
    }

    public static class FareBreakdown {
        public String Currency;
        public int PassengerType;
        public int PassengerCount;
        public double BaseFare;
        public double Tax;
        public double YQTax;
        public double AdditionalTxnFeeOfrd;
        public double AdditionalTxnFeePub;
    }

    public static class Segment {
        public int TripIndicator;
        public int SegmentIndicator;
        public Airline Airline;
        public AirportDetail Origin;
        public AirportDetail Destination;
        public int Duration;
        public int GroundTime;
        public int Mile;
        public boolean StopOver;
        public String StopPoint;
        public String StopPointArrivalTime;
        public String StopPointDepartureTime;
        public String Craft;
        public boolean IsETicketEligible;
        public String FlightStatus;
        public String Status;
        public String Baggage;
        public String CabinBaggage;
        public String Remark;
        public String CabinClass;
        public String NoOfSeatAvailable;
        
    }

    public static class Airline {
        public String AirlineCode;
        public String AirlineName;
        public String FlightNumber;
        public String FareClass;
        public String OperatingCarrier;
    }

    public static class AirportDetail {
        public Airport Airport;
        public String DepTime;
        public String ArrTime;
    }

    public static class Airport {
        public String AirportCode;
        public String AirportName;
        public String Terminal;
        public String CityCode;
        public String CityName;
        public String CountryCode;
        public String CountryName;
    }

    public static class FareRule {
        public String Origin;
        public String Destination;
        public String Airline;
        public String FareBasisCode;
        public String FareRuleDetail;
        public String FareRestriction;
    }
    
}
