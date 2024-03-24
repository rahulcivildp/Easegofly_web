package com.easygofly.site.hotel;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
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
import com.easygofly.entity.Hotel;
import com.easygofly.entity.HotelHistory;
import com.easygofly.entity.TBOCity;
import com.easygofly.entity.TourAttraction;
import com.easygofly.site.LogService;
import com.easygofly.site.customer.CustomerService;
import com.easygofly.site.flightAPI.TBOCityRepository;
import com.easygofly.site.security.EasegoflyPhoneCustomerDetails;

@Controller
public class HotelController {
	@Autowired private TBOCityRepository tboRepo;
	@Autowired private HotelService hotelService;
	@Autowired private OnlineHotelService onlineHotelService ;
	@Autowired private CustomerService customerService;
	@Autowired private LogService logService;

	private String searchURL = "";
	
	List<Hotel> hotels = new ArrayList<Hotel>();
	
	@GetMapping("/hotel")
	public String viewHotelPage(Model model) {
		cityFinder(model);
		
		hotelService.authenticationHotel(model);
		
		return "hotel/hotel";
	}
	

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
			@PathVariable(name = "noOfRooms") String noOfRooms) throws Exception {

		TBOCity city = tboRepo.getCityByCityId(hotelCity);
		cityFinder(model);
		hotels = new ArrayList<Hotel>();
		
	    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	    Date checkIn = dateFormat.parse(checkInDate);
		
		Integer noNights = noOfNightsMethod(checkInDate, checkOutDate);
		
		Integer[] arrChildrenAge = null; 
				
		// Create URL object with the API end-point
        URL urlSearch = new URL("http://api.tektravels.com/BookingEngineService_Hotel/hotelservice.svc/rest/GetHotelResult/");

        // Open a connection
        HttpURLConnection connectionSearch = (HttpURLConnection) urlSearch.openConnection();
       
        StringBuilder responseBodySearch = new StringBuilder();
       
		onlineHotelService.apiOnlineSearchHotel(connectionSearch, responseBodySearch, city.getCityId().toString(), noNights.toString(), noOfRooms, city.getCountryCode(), 
				Integer.parseInt(noOfAdults), Integer.parseInt(noOfChildren), checkIn, arrChildrenAge);
		

        JSONObject jsonObjSearch = new JSONObject(responseBodySearch.toString());
        System.out.println(jsonObjSearch);
        logService.generateLog(jsonObjSearch.toString());
        try {
			JSONArray jsonArrays = jsonObjSearch.getJSONObject("HotelSearchResult").getJSONArray("HotelResults");
			JSONObject mainObj = new JSONObject();
			
			onlineHotelService.traceId = jsonObjSearch.getJSONObject("HotelSearchResult").get("TraceId").toString();
			
			for (int i = 0; i < jsonArrays.length(); i++) {
			    mainObj.put("Hotel-" + i, jsonArrays.getJSONObject(i));
			    
			    String hotelCode = mainObj.getJSONObject("Hotel-" + i).get("HotelCode").toString();
			    Integer resultIndex = Integer.parseInt(mainObj.getJSONObject("Hotel-" + i).get("ResultIndex").toString());
			    String hotelName = mainObj.getJSONObject("Hotel-" + i).get("HotelName").toString();
			    String hotelCategory = mainObj.getJSONObject("Hotel-" + i).get("HotelCategory").toString();
			    Integer starRating = Integer.parseInt(mainObj.getJSONObject("Hotel-" + i).get("StarRating").toString());
			    String hotelDescription = mainObj.getJSONObject("Hotel-" + i).get("HotelDescription").toString();
			    String hotelPromotion = mainObj.getJSONObject("Hotel-" + i).get("HotelPromotion").toString();
			    String hotelPolicy = mainObj.getJSONObject("Hotel-" + i).get("HotelPolicy").toString();
			    String hotelPicture = mainObj.getJSONObject("Hotel-" + i).get("HotelPicture").toString();
			    String hotelAddress = mainObj.getJSONObject("Hotel-" + i).get("HotelAddress").toString();
			    String hotelContactNo = mainObj.getJSONObject("Hotel-" + i).get("HotelContactNo").toString();
			    String hotelMap = mainObj.getJSONObject("Hotel-" + i).get("HotelMap").toString();
			    String latitude = mainObj.getJSONObject("Hotel-" + i).get("Latitude").toString();
			    String longitude = mainObj.getJSONObject("Hotel-" + i).get("Longitude").toString();
			    String hotelLocation = mainObj.getJSONObject("Hotel-" + i).get("HotelLocation").toString();
			    double roomPrice = Double.parseDouble(mainObj.getJSONObject("Hotel-" + i).getJSONObject("Price").get("RoomPrice").toString());
			    double tax = Double.parseDouble(mainObj.getJSONObject("Hotel-" + i).getJSONObject("Price").get("Tax").toString());
			    double extraGuestCharge = Double.parseDouble(mainObj.getJSONObject("Hotel-" + i).getJSONObject("Price").get("ExtraGuestCharge").toString());
			    double childCharge = Double.parseDouble(mainObj.getJSONObject("Hotel-" + i).getJSONObject("Price").get("ChildCharge").toString());
			    double discount = Double.parseDouble(mainObj.getJSONObject("Hotel-" + i).getJSONObject("Price").get("Discount").toString());
			    double publishedPrice = Double.parseDouble(mainObj.getJSONObject("Hotel-" + i).getJSONObject("Price").get("PublishedPrice").toString());
			    double otherCharges = Double.parseDouble(mainObj.getJSONObject("Hotel-" + i).getJSONObject("Price").get("OtherCharges").toString());
			    double offeredPrice = Double.parseDouble(mainObj.getJSONObject("Hotel-" + i).getJSONObject("Price").get("OfferedPrice").toString());
			    Integer publishedPriceRoundedOff = Integer.parseInt(mainObj.getJSONObject("Hotel-" + i).getJSONObject("Price").get("PublishedPriceRoundedOff").toString());
			    Integer offeredPriceRoundedOff = Integer.parseInt(mainObj.getJSONObject("Hotel-" + i).getJSONObject("Price").get("OfferedPriceRoundedOff").toString());
			    double agentCommission = Double.parseDouble(mainObj.getJSONObject("Hotel-" + i).getJSONObject("Price").get("AgentCommission").toString());
			    double agentMarkUp = Double.parseDouble(mainObj.getJSONObject("Hotel-" + i).getJSONObject("Price").get("AgentMarkUp").toString());
			    double serviceTax = Double.parseDouble(mainObj.getJSONObject("Hotel-" + i).getJSONObject("Price").get("ServiceTax").toString());
			    double tds = Double.parseDouble(mainObj.getJSONObject("Hotel-" + i).getJSONObject("Price").get("TDS").toString());
			    
			    Hotel newHotel = new Hotel(hotelCode, resultIndex, hotelName, hotelCategory, starRating, hotelDescription, hotelPromotion, hotelPolicy, hotelPicture, hotelAddress, 
			    		hotelContactNo, hotelMap, latitude, longitude, hotelLocation, roomPrice, tax, extraGuestCharge, childCharge, discount, publishedPrice, otherCharges, offeredPrice, 
			    		publishedPriceRoundedOff, offeredPriceRoundedOff, agentCommission, agentMarkUp, serviceTax, tds);
			    
			    hotels.add(newHotel);
			}
			
		} catch (Exception e) {
//			JSONObject jsonObj = jsonObjSearch.getJSONObject("HotelSearchResult").getJSONObject("Error");
//			String errorCode = jsonObj.get("ErrorCode").toString();
//			String errorMessage = jsonObj.get("ErrorMessage").toString();
			
			e.printStackTrace();
		}
        
        Integer totalGuests = Integer.parseInt(noOfAdults) + Integer.parseInt(noOfChildren);
        
		model.addAttribute("hotelCity", hotelCity);
		model.addAttribute("checkIn", checkIn);
		model.addAttribute("checkInDate", checkInDate);
		model.addAttribute("checkOutDate", checkOutDate);
		model.addAttribute("totalGuests", totalGuests);
		model.addAttribute("noOfAdults", noOfAdults);
		model.addAttribute("noOfChildren", noOfChildren);
		model.addAttribute("noOfRooms", noOfRooms);
		model.addAttribute("noNights", noNights);
		model.addAttribute("hotelList", hotels);
		return "hotel/search/hotel-search-result";
	}

	private Integer noOfNightsMethod(String checkInDate, String checkOutDate) {
		String[] arrCheckIn = checkInDate.split("-");
		String[] arrCheckOut = checkOutDate.split("-");
		
		Integer totalDaysCheckIn = Integer.parseInt(arrCheckIn[2]);
		Integer totalDaysCheckOut = Integer.parseInt(arrCheckOut[2]);
		
		for (int i = 0; i < (Integer.parseInt(arrCheckIn[1]) - 1); i++) {
			totalDaysCheckIn = totalDaysCheckIn + YearMonth.of(Integer.parseInt(arrCheckIn[0]), Integer.parseInt(arrCheckIn[1])).lengthOfMonth();
		}
		
		for (int i = 0; i < (Integer.parseInt(arrCheckIn[1]) - 1); i++) {
			totalDaysCheckOut = totalDaysCheckOut + YearMonth.of(Integer.parseInt(arrCheckOut[0]), Integer.parseInt(arrCheckOut[1])).lengthOfMonth();
		}
		
		Integer noNights = totalDaysCheckOut - totalDaysCheckIn;
		
		return noNights;
	}
	
	@GetMapping("/hotel_loading...")
    public String performApiRequest(Model model) {
        model.addAttribute("searchURL", searchURL);
        return "loading/loading";
    }
	
	@GetMapping("/hotel/booking_{hotelCity}_{checkInDate}_{checkOutDate}_{noOfAdults}_{noOfChildren}_{noOfRooms}_{resultIndex}_{hotelCode}")
	public String hotelBooking(Model model, 
			@PathVariable(name = "hotelCity") String hotelCity,
			@PathVariable(name = "checkInDate") String checkInDate,
			@PathVariable(name = "checkOutDate") String checkOutDate,
			@PathVariable(name = "noOfAdults") String noOfAdults,
			@PathVariable(name = "noOfChildren") String noOfChildren,
			@PathVariable(name = "noOfRooms") String noOfRooms,
			@PathVariable(name = "resultIndex") String resultIndex,
			@PathVariable(name = "hotelCode") String hotelCode) throws Exception {

	    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	    Date checkIn = dateFormat.parse(checkInDate);
	    
		// Create URL object with the API end-point
        URL urlHotelInfo = new URL("http://api.tektravels.com/BookingEngineService_Hotel/hotelservice.svc/rest/GetHotelInfo");

        // Open a connection
        HttpURLConnection connectionHotelInfo = (HttpURLConnection) urlHotelInfo.openConnection();
       
        StringBuilder responseBodyHotelInfo = new StringBuilder();
        
        onlineHotelService.apiOnlineHotelInfo(connectionHotelInfo, responseBodyHotelInfo, resultIndex, hotelCode);
	
        JSONObject jsonObjSearch = new JSONObject(responseBodyHotelInfo.toString());
        System.out.println(jsonObjSearch);
        logService.generateLog(jsonObjSearch.toString());
        
        try {
        	JSONObject jsonObj = jsonObjSearch.getJSONObject("HotelInfoResult").getJSONObject("HotelDetails");

			List<TourAttraction> tours = new ArrayList<TourAttraction>();
			List<String> facilities = new ArrayList<String>();
			List<String> imageHo = new ArrayList<String>();
			
			try {
				JSONArray jsonArrtr = jsonObj.getJSONArray("Attractions");
				for (int i = 0; i < jsonArrtr.length(); i++) {
					String key = jsonArrtr.getJSONObject(i).get("Key").toString();
					String value = jsonArrtr.getJSONObject(i).get("Value").toString();
					
					TourAttraction tour = new TourAttraction();
					tour.setKey(key);
					tour.setValue(value); 
					tours.add(tour);
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			

			try {
				JSONArray jsonFacil = jsonObj.getJSONArray("HotelFacilities");
				for (int i = 0; i < jsonFacil.length(); i++) {
					String facil = jsonFacil.getString(i);
					
					facilities.add(facil);
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			
			try {
				JSONArray jsonImg = jsonObj.getJSONArray("Images");
				for (int i = 0; i < jsonImg.length(); i++) {
					String img = jsonImg.getString(i);
					
					imageHo.add(img);
				}
			} catch (Exception e) {
				// TODO: handle exception
			}

			model.addAttribute("hotelImages", imageHo);
			model.addAttribute("facilities", facilities);
			model.addAttribute("tours", tours);
			model.addAttribute("hotelCode", jsonObj.get("HotelCode").toString());
			model.addAttribute("hotelName", jsonObj.get("HotelName").toString());
			model.addAttribute("starRating", jsonObj.get("StarRating").toString());
			model.addAttribute("description", jsonObj.get("Description").toString());
			model.addAttribute("hotelURL", jsonObj.get("HotelURL").toString());
			model.addAttribute("hotelPolicy", jsonObj.get("HotelPolicy").toString());
			model.addAttribute("specialInstructions", jsonObj.get("SpecialInstructions").toString());
			model.addAttribute("address", jsonObj.get("Address").toString());
			model.addAttribute("countryName", jsonObj.get("CountryName").toString());
			model.addAttribute("pinCode", jsonObj.get("PinCode").toString());
			model.addAttribute("hotelContactNo", jsonObj.get("HotelContactNo").toString());
			model.addAttribute("faxNumber", jsonObj.get("FaxNumber").toString());
			model.addAttribute("email", jsonObj.get("Email").toString());
			model.addAttribute("latitude", jsonObj.get("Latitude").toString());
			model.addAttribute("longitude", jsonObj.get("Longitude").toString());
			model.addAttribute("roomData", jsonObj.get("RoomData").toString());
			model.addAttribute("roomFacilities", jsonObj.get("RoomFacilities").toString());
			model.addAttribute("services", jsonObj.get("Services").toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
        Integer totalGuests = Integer.parseInt(noOfAdults) + Integer.parseInt(noOfChildren);
		Integer noNights = noOfNightsMethod(checkInDate, checkOutDate);
        
		model.addAttribute("hotelCity", hotelCity);
		model.addAttribute("checkIn", checkIn);
		model.addAttribute("checkInDate", checkInDate);
		model.addAttribute("checkOutDate", checkOutDate);
		model.addAttribute("totalGuests", totalGuests);
		model.addAttribute("noOfAdults", noOfAdults);
		model.addAttribute("noOfChildren", noOfChildren);
		model.addAttribute("noOfRooms", noOfRooms);
		model.addAttribute("noNights", noNights);
        
		return "hotel/booking/hotel-booking";
	}
	
	@PostMapping("/hotel/saveHotel")
	public String saveHotel(@RequestParam(name = "resultIndex", required = false) String resultIndex,
			@RequestParam(name = "hotelCode", required = false) String hotelCode,
			@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer,
			@RequestParam(name = "hotelCity", required = false) String hotelCity, 
			@RequestParam(name = "checkInDate", required = false) String checkInDate, 
			@RequestParam(name = "checkOutDate", required = false) String checkOutDate, 
			@RequestParam(name = "noOfAdults", required = false) Integer noOfAdults,
			@RequestParam(name = "noOfChildren", required = false) Integer noOfChildren,
			@RequestParam(name = "noOfRooms", required = false) Integer noOfRooms) throws Exception {
	    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	    Date checkIn = dateFormat.parse(checkInDate);
	    Date checkOut = dateFormat.parse(checkOutDate);
		TBOCity city = tboRepo.getCityByCityId(hotelCity);

		String email = loggedCustomer.getUsername();
		Customer customer = customerService.getByPhone(email);
		HotelHistory history = new HotelHistory();
		history.setCheckInDate(checkIn);
		history.setCheckOutDate(checkOut);
		history.setChildrenAge(null);
		history.setCityId(city.getCityId().toString());
		history.setCountryCode(city.getCountryCode());
		history.setNearBySearchAllowed(false);
		history.setNoOfAdults(noOfAdults.toString());
		history.setNoOfChild(noOfChildren.toString());
		history.setNoOfRooms(noOfRooms.toString());
		history.setCustomer(customer);
		
		hotelService.saveHotelHistory(history, customer); 
		return "redirect:/hotel/booking_" + hotelCity + "_" + checkInDate + "_" + checkOutDate + "_" + noOfAdults + "_" + noOfChildren + "_" + noOfRooms + "_" + resultIndex + "_" + hotelCode;
	}
}
