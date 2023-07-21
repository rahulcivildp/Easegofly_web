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

import javax.persistence.EntityManager;

import org.json.JSONArray;
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
import com.easygofly.entity.Customer;
import com.easygofly.entity.Product;
import com.easygofly.entity.ProductDetail;
import com.easygofly.entity.SearchHistory;
import com.easygofly.entity.exception.ProductNotFoundException;
import com.easygofly.site.customer.CustomerService;
import com.easygofly.site.flight.CityRepository;
import com.easygofly.site.flight.ProductDetailService;
import com.easygofly.site.flight.ProductDetailsController;
import com.easygofly.site.flight.ProductDetailsRepository;
import com.easygofly.site.flight.ProductSaveHelper;
import com.easygofly.site.flightAPI.OnlineFlightService;
import com.easygofly.site.security.EasyGoFlyCustomerDetails;
import com.easygofly.site.security.oauth.CustomerOAuth2User;

@Controller
public class SearchHistoryController {

	@Autowired private SearchHistoryService searchService;
	@Autowired private CustomerService customerService;
	@Autowired private ProductDetailService productService;
	@Autowired private SearchHistoryRepository searchRepo;
	@Autowired private ProductDetailsRepository productRepo;
	@Autowired private CityRepository cityRepo;
	@Autowired private OnlineFlightService onlineFlightService;
	@Autowired private EntityManager enityManager;
	@Autowired private ProductDetailsController productDetailsController;
	
	private String searchURL = "";
	List<ProductDetail> listProductDetails;
	
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
		
		SearchHistory search = searchRepo.findById(id).get();

		System.out.println("Date: " + search.getDate());
		
		searchSort(search.getCityOne(), search.getCityTwo(), sortName, model, search.getDate());
		
		searchFilter(search.getCityOne(), search.getCityTwo(), brands, model, search.getDate(), stops, totalPrice, search.getInfantNum(), activeTime);

		List<Product> getProductBrand = productRepo.findProductByCity(search.getCityOne(), search.getCityTwo(), Sort.by("name").ascending());
		
		Iterable<City> cities = cityRepo.findAll();
		
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
		
		return "flight/search-result";
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
		 
	    Date date = new SimpleDateFormat("yyyy-MM-dd").parse(strDate);
	    
		System.out.println("no User Date: " + date + " - "+ sortName);

		searchSort(cityOne, cityTwo, sortName, model, date);
		
		searchFilter(cityOne, cityTwo, brands, model, date, stops, totalPrice, infantNum, activeTime);

		List<Product> getProductBrand = productRepo.findProductByCity(cityOne, cityTwo, Sort.by("name").ascending());
		
		Integer passengerNum = adultNum + childNum + infantNum;
		Iterable<City> cities = cityRepo.findAll();
		
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
		
		return "flight/search-result-noUser";
		
	}

	private Integer searchFlightAPI(String cityOne, String cityTwo, Integer adultNum, Integer childNum, Integer infantNum,
			String sortName, Model model, Date date) throws MalformedURLException, IOException {
		// Create URL object with the API end-point
        URL urlSearch = new URL("http://api.tektravels.com/BookingEngineService_Air/AirService.svc/rest/Search");

        // Open a connection
        HttpURLConnection connectionSearch = (HttpURLConnection) urlSearch.openConnection();
        
        StringBuilder responseBodySearch = new StringBuilder();
        
        int responseCode = onlineFlightService.apiOnlineMod(connectionSearch, responseBodySearch, cityOne, cityTwo, adultNum, childNum, infantNum, date);
        
        productDetailsController.listProductDetailsOnline = new ArrayList<ProductDetail>(); 
        Product productOnline = enityManager.find(Product.class, 15);
        ProductDetail[] productDetail = new ProductDetail[200];
        
        JSONObject jsonObjSearch = new JSONObject(responseBodySearch.toString());
        JSONArray jsonArrays = jsonObjSearch.getJSONObject("Response").getJSONArray("Results").getJSONArray(0);
        JSONArray jsonObjSegment = new JSONArray();
		JSONObject mainObj = new JSONObject();
		JSONObject mainObjSegment = new JSONObject();
		JSONObject mainObjOrigin = new JSONObject();
		JSONObject mainObjDestination = new JSONObject();
		JSONObject mainObjAirline = new JSONObject();
		JSONObject mainObjFare = new JSONObject();
       
		String traceId = jsonObjSearch.getJSONObject("Response").get("TraceId").toString();
		System.out.println(onlineFlightService.traceId);
        
		for (int i = 0; i < jsonArrays.length(); i++) {

	        mainObj.put("Result-" + i, jsonArrays.getJSONObject(i));
	        jsonObjSegment.put(mainObj.getJSONObject("Result-" + i).getJSONArray("Segments").getJSONArray(0));
	        mainObjSegment.put("Segment-" + i, mainObj.getJSONObject("Result-" + i).getJSONArray("Segments").getJSONArray(0));
	        mainObjFare.put("Fare-" + i, mainObj.getJSONObject("Result-" + i).getJSONObject("Fare"));
	        mainObjOrigin.put("Origin-" + i, mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(0).getJSONObject("Origin"));
	        mainObjDestination.put("Destination-" + i, mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(0).getJSONObject("Destination"));
	        mainObjAirline.put("Airline-" + i, mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(0).getJSONObject("Airline"));
	        
	        String depAirportCode = mainObjOrigin.getJSONObject("Origin-" + i).getJSONObject("Airport").get("AirportCode").toString();
	        String depAirportName = mainObjOrigin.getJSONObject("Origin-" + i).getJSONObject("Airport").get("AirportName").toString();
	        String depTerminal = mainObjOrigin.getJSONObject("Origin-" + i).getJSONObject("Airport").get("Terminal").toString();
	        
	        String arrAirportCode = mainObjDestination.getJSONObject("Destination-" + i).getJSONObject("Airport").get("AirportCode").toString();
	        String arrAirportName = mainObjDestination.getJSONObject("Destination-" + i).getJSONObject("Airport").get("AirportName").toString();
	        String arrTerminal = mainObjDestination.getJSONObject("Destination-" + i).getJSONObject("Airport").get("Terminal").toString();
	        
	        String airlineName = mainObjAirline.getJSONObject("Airline-" + i).get("AirlineName").toString();
	        String fareClass = mainObjAirline.getJSONObject("Airline-" + i).get("FareClass").toString();
	        String flightNumber = mainObjAirline.getJSONObject("Airline-" + i).get("AirlineCode").toString() + "-" + mainObjAirline.getJSONObject("Airline-" + i).get("FlightNumber").toString();
	        
	        String depTime = mainObjOrigin.getJSONObject("Origin-" + i).get("DepTime").toString();
	        String[] departureTimeParts = depTime.split("T");
			String[] departureTimeInnerParts = departureTimeParts[1].split(":");
			String stringDepTime = departureTimeInnerParts[0] + ":" + departureTimeInnerParts[1];
			String depTimeString = departureTimeInnerParts[0] + "." + departureTimeInnerParts[1].charAt(0);
			Float depTimeFloat = Float.parseFloat(depTimeString);
			
			String arrTime = mainObjDestination.getJSONObject("Destination-" + i).get("ArrTime").toString();
			String[] arrivalTimeParts = arrTime.split("T");
			String[] arrivalTimeInnerParts = arrivalTimeParts[1].split(":");
			String stringArrTime = arrivalTimeInnerParts[0] + ":" + arrivalTimeInnerParts[1];
			String arrTimeString = arrivalTimeInnerParts[0] + "." + arrivalTimeInnerParts[1].charAt(0);
			Float arrTimeFloat = Float.parseFloat(arrTimeString);
			
			Integer duration = Integer.parseInt(mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(0).get("Duration").toString());
			String flightStatus = mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(0).get("FlightStatus").toString();
			String noOfSeatAvailable = mainObjSegment.getJSONArray("Segment-" + i).getJSONObject(0).get("NoOfSeatAvailable").toString();
			
			String strTotalFare = mainObjFare.getJSONObject("Fare-" + i).get("PublishedFare").toString();
			Integer fareAdjustment = adultNum + childNum + infantNum;
			Double doubleFare = Double.parseDouble(strTotalFare);
			Integer intTotalFare = ((int) Math.round(doubleFare)) / fareAdjustment;
			
			String resultIndex = mainObj.getJSONObject("Result-" + i).get("ResultIndex").toString();
			String airlineRemark = mainObj.getJSONObject("Result-" + i).get("AirlineRemark").toString();
			
			String mode = "Online-data";
			
			productDetail[i] = new ProductDetail(200 + i, "waiting...", noOfSeatAvailable, noOfSeatAvailable, flightNumber, date, 
            		stringDepTime, stringArrTime, intTotalFare, 0, 0, 0, depAirportCode, arrAirportCode, true, true, 0, duration, airlineName, depTimeFloat, arrTimeFloat, traceId, resultIndex, airlineRemark, mode, productOnline);
			
			productDetailsController.listProductDetailsOnline.add(productDetail[i]);
	         
		}
		System.out.println(mainObj);
		System.out.println(responseCode);
		
		model.addAttribute("listProducts", productDetailsController.listProductDetailsOnline);
        model.addAttribute("responseCode", responseCode);
        
        // Close the connection
        connectionSearch.disconnect();
        
        return responseCode;
	}

	private void searchFilter(String cityOne, String cityTwo, String[] brands, Model model, Date date, Integer[] stops, Integer[] totalPrice, Integer infantNum, String[] activeTime) {
		String brand1 = "";
		String brand2 = "";
		Integer stop0 = null;
		Integer stop1 = null;
		Integer stop2 = null;
		Integer stop3 = null;
		Integer priceTotal = null;
		String[] combainedTime = null;

		if (brands.length == 2 && stops.length == 1 && totalPrice[1] == null && activeTime.length == 1) {
			brand1 = brands[1];
			listProductDetails = productService.findAllFlightsByBrand(cityOne, cityTwo, date, brand1, brand2);
			model.addAttribute("listProducts", listProductDetails);
		} else if (brands.length == 3 && stops.length == 1 && totalPrice[1] == null && activeTime.length == 1) {
			brand1 = brands[1];
			brand2 = brands[2];  
			listProductDetails = productService.findAllFlightsByBrand(cityOne, cityTwo, date, brand1, brand2);
			model.addAttribute("listProducts", listProductDetails);
		} else if (stops.length == 2 && brands.length == 1 && totalPrice[1] == null && activeTime.length == 1) {
			stop0 = stops[1];
			brand1 = "";
			brand2 = "";
			listProductDetails = productService.findAllFlightsByStop(cityOne, cityTwo, date, stop0, stop1, stop2, stop3);
			model.addAttribute("listProducts", listProductDetails);
		} else if (stops.length == 3 && brands.length == 1 && totalPrice[1] == null && activeTime.length == 1) {
			stop0 = stops[1];
			stop1 = stops[2];
			brand1 = "";
			brand2 = "";
			listProductDetails = productService.findAllFlightsByStop(cityOne, cityTwo, date, stop0, stop1, stop2, stop3);
			model.addAttribute("listProducts", listProductDetails);
		} else if (stops.length == 2 && brands.length == 2 && totalPrice[1] == null && activeTime.length == 1) {
			stop0 = stops[1];
			brand1 = brands[1];
			brand2 = "";
			listProductDetails = productService.findAllFlightsByBrandSort(cityOne, cityTwo, date, brand1, brand2, stop0, stop1, stop2, stop3);
			model.addAttribute("listProducts", listProductDetails);
		} else if (stops.length == 3 && brands.length == 2 && totalPrice[1] == null && activeTime.length == 1) {
			stop0 = stops[1];
			stop1 = stops[2];
			brand1 = brands[1];
			brand2 = "";
			listProductDetails = productService.findAllFlightsByBrandSort(cityOne, cityTwo, date, brand1, brand2, stop0, stop1, stop2, stop3);
			model.addAttribute("listProducts", listProductDetails);
		} else if (stops.length == 3 && brands.length == 3 && totalPrice[1] == null && activeTime.length == 1) {
			stop0 = stops[1];
			stop1 = stops[2];
			brand1 = brands[1];
			brand2 = brands[2];
			listProductDetails = productService.findAllFlightsByBrandSort(cityOne, cityTwo, date, brand1, brand2, stop0, stop1, stop2, stop3);
			model.addAttribute("listProducts", listProductDetails);
		} else if (stops.length == 2 && brands.length == 3 && totalPrice[1] == null && activeTime.length == 1) {
			stop0 = stops[1];
			stop1 = null;
			brand1 = brands[1];
			brand2 = brands[2];
			listProductDetails = productService.findAllFlightsByBrandSort(cityOne, cityTwo, date, brand1, brand2, stop0, stop1, stop2, stop3);
			model.addAttribute("listProducts", listProductDetails);
		} else if (totalPrice[1] != null && infantNum == 0 && stops.length == 1 && brands.length == 1 && activeTime.length == 1) {
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				listProductDetails = productService.findAllFlightsByTotalPriceADT(cityOne, cityTwo, date, priceTotal);
				model.addAttribute("listProducts", listProductDetails);
			}
		} else if (totalPrice[1] != null && infantNum == 0 && stops.length == 1 && brands.length == 2 && activeTime.length == 1) {
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				brand1 = brands[1];
				brand2 = "";
				listProductDetails = productService.findAllFlightsByBrandTotalPriceADT(cityOne,
						cityTwo, date, brand1, brand2, priceTotal);
				model.addAttribute("listProducts", listProductDetails);
			}
		} else if (totalPrice[1] != null && infantNum == 0 && stops.length == 2 && brands.length == 1 && activeTime.length == 1) {
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				stop0 = stops[1];
				listProductDetails = productService.findAllFlightsByStopNumTotalPriceADT(cityOne, cityTwo, date, priceTotal, stop0, stop1, stop2, stop3);
				model.addAttribute("listProducts", listProductDetails);
			}
		} else if (totalPrice[1] != null && infantNum == 0 && stops.length == 2 && brands.length == 2 && activeTime.length == 1) {
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				stop0 = stops[1];
				brand1 = brands[1];
				brand2 = "";
				listProductDetails = productService.findAllFlightsByBrandStopNumTotalPriceADT(cityOne, cityTwo, date, brand1, brand2, stop0, stop1, stop2, stop3, priceTotal);
				model.addAttribute("listProducts", listProductDetails);
			}
		} else if (totalPrice[1] != null && infantNum == 0 && stops.length == 3 && brands.length == 2 && activeTime.length == 1) {
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				stop0 = stops[1];
				stop1 = stops[2];
				brand1 = brands[1];
				brand2 = "";
				listProductDetails = productService.findAllFlightsByBrandStopNumTotalPriceADT(cityOne, cityTwo, date, brand1, brand2, stop0, stop1, stop2, stop3, priceTotal);
				model.addAttribute("listProducts", listProductDetails);
			}
		} else if (totalPrice[1] != null && infantNum == 0 && stops.length == 2 && brands.length == 3 && activeTime.length == 1) {
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				stop0 = stops[1];
				brand1 = brands[1];
				brand2 = brands[2];
				listProductDetails = productService.findAllFlightsByBrandStopNumTotalPriceADT(cityOne, cityTwo, date, brand1, brand2, stop0, stop1, stop2, stop3, priceTotal);
				model.addAttribute("listProducts", listProductDetails);
			}
		} else if (totalPrice[1] != null && infantNum == 0 && stops.length == 3 && brands.length == 3 && activeTime.length == 1) {
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				stop0 = stops[1];
				stop1 = stops[2];
				brand1 = brands[1];
				brand2 = brands[2];
				listProductDetails = productService.findAllFlightsByBrandStopNumTotalPriceADT(cityOne, cityTwo, date, brand1, brand2, stop0, stop1, stop2, stop3, priceTotal);
				model.addAttribute("listProducts", listProductDetails);
			}
		} else if (activeTime.length == 2 && stops.length == 1 && totalPrice[1] == null && brands.length == 1) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			listProductDetails = productService.findAllFlightsByTime(cityOne, cityTwo, date, convertedArrTime1, convertedArrTime2);
			model.addAttribute("listProducts", listProductDetails);
		} else if (activeTime.length == 2 && stops.length == 2 && totalPrice[1] == null && brands.length == 1) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			stop0 = stops[1];
			listProductDetails = productService.findAllFlightsByTimeStopNum(cityOne, cityTwo, date, stop0, stop1, stop2, stop3, convertedArrTime1, convertedArrTime2);
			model.addAttribute("listProducts", listProductDetails);
		} else if (activeTime.length == 2 && stops.length == 1 && totalPrice[1] == null && brands.length == 2) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			brand1 = brands[1];
			listProductDetails = productService.findAllFlightsByTimeBrand(cityOne, cityTwo, date, brand1, brand2, convertedArrTime1, convertedArrTime2);
			model.addAttribute("listProducts", listProductDetails);
		} else if (activeTime.length == 2 && stops.length == 1 && totalPrice[1] != null && brands.length == 2 && infantNum == 0) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			brand1 = brands[1];
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				listProductDetails = productService.findAllFlightsByTimePriceADT(cityOne, cityTwo, date, priceTotal, convertedArrTime1, convertedArrTime2);
				model.addAttribute("listProducts", listProductDetails);
			}
		} else if (activeTime.length == 2 && stops.length == 1 && totalPrice[1] != null && brands.length == 2) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			brand1 = brands[1];
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				listProductDetails = productService.findAllFlightsByTimePrice(cityOne, cityTwo, date, priceTotal, convertedArrTime1, convertedArrTime2);
				model.addAttribute("listProducts", listProductDetails);
			}
		} else if (activeTime.length == 2 && stops.length == 2 && totalPrice[1] == null && brands.length == 2) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			brand1 = brands[1];
			stop0 = stops[1];
			listProductDetails = productService.findAllFlightsByTimeBrandStopNum(cityOne, cityTwo, date, brand1, brand2, convertedArrTime1, convertedArrTime2, stop0, stop1, stop2, stop3);
			model.addAttribute("listProducts", listProductDetails);
		} else if (activeTime.length == 2 && stops.length == 3 && totalPrice[1] == null  && brands.length == 2) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			brand1 = brands[1];
			stop0 = stops[1];
			stop1 = stops[2];
			listProductDetails = productService.findAllFlightsByTimeBrandStopNum(cityOne, cityTwo, date, brand1, brand2, convertedArrTime1, convertedArrTime2, stop0, stop1, stop2, stop3);
			model.addAttribute("listProducts", listProductDetails);
		} else if (activeTime.length == 2 && stops.length == 2 && totalPrice[1] == null && brands.length == 3) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			brand1 = brands[1];
			brand2 = brands[2];
			stop0 = stops[1];
			listProductDetails = productService.findAllFlightsByTimeBrandStopNum(cityOne, cityTwo, date, brand1, brand2, convertedArrTime1, convertedArrTime2, stop0, stop1, stop2, stop3);
			model.addAttribute("listProducts", listProductDetails);
		} else if (activeTime.length == 2 && stops.length == 3 && totalPrice[1] == null && brands.length == 3) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			brand1 = brands[1];
			brand2 = brands[2];
			stop0 = stops[1];
			stop1 = stops[2];
			listProductDetails = productService.findAllFlightsByTimeBrandStopNum(cityOne, cityTwo, date, brand1, brand2, convertedArrTime1, convertedArrTime2, stop0, stop1, stop2, stop3);
			model.addAttribute("listProducts", listProductDetails);

			
		}	 else if (activeTime.length == 2 && stops.length == 1 && totalPrice[1] != null && brands.length == 2 && infantNum == 0) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			brand1 = brands[1];
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				listProductDetails = productService.findAllFlightsByTimePriceADTBrand(cityOne, cityTwo, date, priceTotal, convertedArrTime1, convertedArrTime2, brand1, brand2);
				model.addAttribute("listProducts", listProductDetails);
			}
		} else if (activeTime.length == 2 && stops.length == 1 && totalPrice[1] != null && brands.length == 2) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			brand1 = brands[1];
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				listProductDetails = productService.findAllFlightsByTimePriceBrand(cityOne, cityTwo, date, priceTotal, convertedArrTime1, convertedArrTime2, brand1, brand2);
				model.addAttribute("listProducts", listProductDetails);
			}
		} else if (activeTime.length == 2 && stops.length == 2 && totalPrice[1] != null && brands.length == 1 && infantNum == 0) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			stop0 = stops[1];
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				listProductDetails = productService.findAllFlightsByTimeStopNumPriceADT(cityOne, cityTwo, date, priceTotal, stop0, stop1, stop2, stop3, convertedArrTime1, convertedArrTime2);
				model.addAttribute("listProducts", listProductDetails);
			}
		} else if (activeTime.length == 2 && stops.length == 2 && totalPrice[1] != null && brands.length == 1) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			stop0 = stops[1];
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				listProductDetails = productService.findAllFlightsByTimeStopNumPrice(cityOne, cityTwo, date, priceTotal, stop0, stop1, stop2, stop3, convertedArrTime1, convertedArrTime2);
				model.addAttribute("listProducts", listProductDetails);
			}
		} else if (activeTime.length == 2 && stops.length == 2 && totalPrice[1] != null && brands.length == 2 && infantNum == 0) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			stop0 = stops[1];
			brand1 = brands[1];
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				listProductDetails = productService.findAllFlightsByTimePriceADTBrandStopNum(cityOne, cityTwo, date, priceTotal, convertedArrTime1, convertedArrTime2, brand1, brand2, stop0, stop1, stop2, stop3);
				model.addAttribute("listProducts", listProductDetails);
				
			}
		} else if (activeTime.length == 2 && stops.length == 2 && totalPrice[1] != null && brands.length == 2) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			stop0 = stops[1];
			stop1 = stops[2];
			brand1 = brands[1];
			brand2 = brands[2];
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				listProductDetails = productService.findAllFlightsByTimePriceBrandStopNum(cityOne, cityTwo, date, priceTotal, convertedArrTime1, convertedArrTime2, brand1, brand2, stop0, stop1, stop2, stop3);
				model.addAttribute("listProducts", listProductDetails);
			}
		}  else if (activeTime.length == 2 && stops.length == 3 && totalPrice[1] != null && brands.length == 2 && infantNum == 0) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			stop0 = stops[1];
			stop1 = stops[2];
			brand1 = brands[1];
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				listProductDetails = productService.findAllFlightsByTimePriceADTBrandStopNum(cityOne, cityTwo, date, priceTotal, convertedArrTime1, convertedArrTime2, brand1, brand2, stop0, stop1, stop2, stop3);
				model.addAttribute("listProducts", listProductDetails);
			}
		} else if (activeTime.length == 2 && stops.length == 3 && totalPrice[1] != null && brands.length == 2) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			stop0 = stops[1];
			stop1 = stops[2];
			brand1 = brands[1];
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				listProductDetails = productService.findAllFlightsByTimePriceBrandStopNum(cityOne, cityTwo, date, priceTotal, convertedArrTime1, convertedArrTime2, brand1, brand2, stop0, stop1, stop2, stop3);
				model.addAttribute("listProducts", listProductDetails);
			}
		} else if (activeTime.length == 2 && stops.length == 2 && totalPrice[1] != null && brands.length == 3 && infantNum == 0) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			stop0 = stops[1];
			brand1 = brands[1];
			brand2 = brands[2];
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				listProductDetails = productService.findAllFlightsByTimePriceADTBrandStopNum(cityOne, cityTwo, date, priceTotal, convertedArrTime1, convertedArrTime2, brand1, brand2, stop0, stop1, stop2, stop3);
				model.addAttribute("listProducts", listProductDetails);
			}
		} else if (activeTime.length == 2 && stops.length == 2 && totalPrice[1] != null && brands.length == 3) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			stop0 = stops[1];
			brand1 = brands[1];
			brand2 = brands[2];
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				listProductDetails = productService.findAllFlightsByTimePriceBrandStopNum(cityOne, cityTwo, date, priceTotal, convertedArrTime1, convertedArrTime2, brand1, brand2, stop0, stop1, stop2, stop3);
				model.addAttribute("listProducts", listProductDetails);
			}
		}  else if (activeTime.length == 2 && stops.length == 3 && totalPrice[1] != null && brands.length == 3 && infantNum == 0) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			stop0 = stops[1];
			stop1 = stops[2];
			brand1 = brands[1];
			brand2 = brands[2];
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				listProductDetails = productService.findAllFlightsByTimePriceADTBrandStopNum(cityOne, cityTwo, date, priceTotal, convertedArrTime1, convertedArrTime2, brand1, brand2, stop0, stop1, stop2, stop3);
				model.addAttribute("listProducts", listProductDetails);
			}
		}  else if (activeTime.length == 2 && stops.length == 3 && totalPrice[1] != null && brands.length == 3) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			stop0 = stops[1];
			stop1 = stops[2];
			brand1 = brands[1];
			brand2 = brands[2];
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				listProductDetails = productService.findAllFlightsByTimePriceBrandStopNum(cityOne, cityTwo, date, priceTotal, convertedArrTime1, convertedArrTime2, brand1, brand2, stop0, stop1, stop2, stop3);
				model.addAttribute("listProducts", listProductDetails);
			}
		}
	}

	private void searchSort(String cityOne, String cityTwo, String sortName, Model model, Date date) {
		if (sortName.equals("pnr")) {
			listProductDetails = productService.listAllFlights(cityOne, cityTwo, date, Sort.by(sortName).ascending());
			model.addAttribute("listProducts", listProductDetails);
		} else if (sortName.equals("price")) {
			listProductDetails = productService.listAllFlightsByPrice(cityOne, cityTwo, date);
			model.addAttribute("listProducts", listProductDetails);
		} else if (sortName.equals("duration")) {
			listProductDetails = productService.listAllFlights(cityOne, cityTwo, date, Sort.by(sortName).ascending());
			model.addAttribute("listProducts", listProductDetails);
		} else if (sortName.equals("arrTime")) {
			listProductDetails = productService.listAllFlightsByArrival(cityOne, cityTwo, date);
			model.addAttribute("listProducts", listProductDetails);
		} else if (sortName.equals("depTime")) {
			listProductDetails = productService.listAllFlightsByDeparture(cityOne, cityTwo, date);
			model.addAttribute("listProducts", listProductDetails);
		} else if (sortName.equals("brand")) {
			listProductDetails = productService.listAllFlights(cityOne, cityTwo, date, Sort.by(sortName).ascending());
			model.addAttribute("listProducts", listProductDetails);
		}
	}
	
	@GetMapping("/flight_search_save")
	public String searchHistorySave(@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer,
			@AuthenticationPrincipal CustomerOAuth2User googleLogin, 
			@RequestParam(name = "cityOne", required = false) String cityOne, 
			@RequestParam(name = "cityTwo", required = false) String cityTwo, 
			@RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date date, 
			@RequestParam(name = "passengerNum", required = false) Integer passengerNum,
			@RequestParam(name = "journeyClass", required = false) String journeyClass,
			@RequestParam(name = "tripType", required = false) String tripType,
			@RequestParam(name = "adultNum", required = false) Integer adultNum,
			@RequestParam(name = "childNum", required = false) Integer childNum,
			@RequestParam(name = "infantNum", required = false) Integer infantNum,
			Model model) throws ProductNotFoundException {
		
			System.out.println("Last Value of Search: " + date);
		
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
				System.out.println("Last Value of Search: " + searchId);
				searchURL = "/flight_search_" + searchId +"_"+ sort +"_"+ brand +"_"+ stop +"_"+ arrayPrice +"_"+ activeTime;
				return "redirect:/loading_";
			} else if (googleLogin != null) {
				email = googleLogin.getEmail();
				customer = customerService.getByEmail(email);
				model.addAttribute("customer", customer);
				Integer searchId = saveHistoryPart(city1.getCode(), city2.getCode(), date, journeyClass, tripType, adultNum, childNum,
						infantNum, customer);
				System.out.println("Last Value of Search: " + searchId);
				searchURL = "/flight_search_" + searchId +"_"+ sort +"_"+ brand +"_"+ stop +"_"+ arrayPrice +"_"+ activeTime;
				return "redirect:/loading_";
			}else {
				searchURL = "/flight_search-noUser_"+ city1.getCode() +"_"+ city2.getCode() +"_"+ journeyClass +"_"+ tripType +"_"+ adultNum 
						+"_"+ childNum +"_"+ infantNum +"_"+ strDate +"_"+ sort +"_"+ brand +"_"+ stop +"_"+ arrayPrice +"_"+ activeTime;
				return "redirect:/loading_";
			}
            
	}

	private Integer saveHistoryPart(String cityOne, String cityTwo, Date date, String journeyClass, String tripType,
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

	@GetMapping("/loading_")
    public String performApiRequest(Model model) {
        model.addAttribute("searchURL", searchURL);
        return "loading/loading";
    }
	
}
