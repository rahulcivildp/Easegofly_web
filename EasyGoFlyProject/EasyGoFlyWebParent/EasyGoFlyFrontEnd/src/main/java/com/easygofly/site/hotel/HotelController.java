package com.easygofly.site.hotel;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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
import com.easygofly.entity.HotelCancelPolicy;
import com.easygofly.entity.HotelHistory;
import com.easygofly.entity.HotelRoom;
import com.easygofly.entity.RoomDayRate;
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
	private String bookingURL = "";
	
	List<Hotel> hotels = new ArrayList<Hotel>();
	List<HotelRoom> hotelRooms = new ArrayList<HotelRoom>();
	
	HotelHistory history = new HotelHistory();
	
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
			HotelHistory newHistory = new HotelHistory();

			newHistory = new HotelHistory();
			newHistory.setCheckInDate(checkInDate);
			newHistory.setCheckOutDate(checkOutDate);
			newHistory.setChildrenAge(null);
			newHistory.setCityId(city.getCityId().toString());
			newHistory.setCountryCode(city.getCountryCode());
			newHistory.setNearBySearchAllowed(false);
			newHistory.setNoOfAdults(noOfAdults.toString());
			newHistory.setNoOfChild(noOfChildren.toString());
			newHistory.setNoOfRooms(noOfRooms.toString());
			newHistory.setCustomer(customer);
			
			history = hotelService.saveHotelHistory(newHistory, customer);
			
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
	
	@GetMapping("/hotel/booking_{hotelCity}_{checkInDate}_{checkOutDate}_{noOfAdults}_{noOfChildren}_{noOfRooms}_{resultIndex}_{hotelCode}_{history_id}")
	public String hotelBooking(Model model, 
			@PathVariable(name = "hotelCity") String hotelCity,
			@PathVariable(name = "checkInDate") String checkInDate,
			@PathVariable(name = "checkOutDate") String checkOutDate,
			@PathVariable(name = "noOfAdults") String noOfAdults,
			@PathVariable(name = "noOfChildren") String noOfChildren,
			@PathVariable(name = "noOfRooms") String noOfRooms,
			@PathVariable(name = "resultIndex") String resultIndex,
			@PathVariable(name = "history_id") Integer history_id,
			@PathVariable(name = "hotelCode") String hotelCode) throws Exception {

	    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	    Date checkIn = dateFormat.parse(checkInDate);
	    
	    HotelHistory hotelHistory = hotelService.findById(history_id);
	    
	    for (Hotel hotel : hotels) {
			if (hotel.getResultIndex() == Integer.parseInt(resultIndex)) {
				
			}
		}
	    
		hotelInfoMethod(model, resultIndex, hotelCode);
		hotelRoomsMethod(model, resultIndex, hotelCode);
		
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
		model.addAttribute("hotelHistory", hotelHistory);
        
		return "hotel/booking/hotel-booking";
	}


	private void hotelInfoMethod(Model model, String resultIndex, String hotelCode)
			throws MalformedURLException, IOException {
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
	}

	private void hotelRoomsMethod(Model model, String resultIndex, String hotelCode)
			throws MalformedURLException, IOException {
		// Create URL object with the API end-point
        URL urlHotelRoom = new URL("http://api.tektravels.com/BookingEngineService_Hotel/hotelservice.svc/rest/GetHotelRoom");

        // Open a connection
        HttpURLConnection connectionHotelRoom = (HttpURLConnection) urlHotelRoom.openConnection();
       
        StringBuilder responseBodyHotelRoom = new StringBuilder();
        
        onlineHotelService.apiOnlineHotelInfo(connectionHotelRoom, responseBodyHotelRoom, resultIndex, hotelCode);
	
        JSONObject jsonObjSearch = new JSONObject(responseBodyHotelRoom.toString());
        System.out.println(jsonObjSearch);
        logService.generateLog(jsonObjSearch.toString());
        
        try {
        	JSONArray jsonArr = jsonObjSearch.getJSONObject("GetHotelRoomResult").getJSONArray("HotelRoomsDetails");
        	JSONObject hotelRoomsObj = new JSONObject();
        	
        	for (int i = 0; i < jsonArr.length(); i++) {
        		hotelRoomsObj.put("RoomDetail-" + i, jsonArr.getJSONObject(i));
        		
        		String availabilityType = hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("AvailabilityType").toString();
        		Integer childCount = Integer.parseInt(hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("ChildCount").toString());
        		boolean requireAllPaxDetails = Boolean.parseBoolean(hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("RequireAllPaxDetails").toString());
        		boolean isPassportMandatory = Boolean.parseBoolean(hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("IsPassportMandatory").toString());
        		boolean isPANMandatory = Boolean.parseBoolean(hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("IsPANMandatory").toString());
        		Integer roomId = Integer.parseInt(hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("RoomId").toString());
        		Integer roomStatus = Integer.parseInt(hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("RoomStatus").toString());
        		Integer roomIndex = Integer.parseInt(hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("RoomIndex").toString());
        		String roomTypeCode = hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("RoomTypeCode").toString();
        		String roomDescription = hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("RoomDescription").toString();
        		String roomTypeName = hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("RoomTypeName").toString();
        		String ratePlanCode = hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("RatePlanCode").toString();
        		Integer ratePlan = Integer.parseInt(hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("RatePlan").toString());
        		String ratePlanName = hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("RatePlanName").toString();
        		String infoSource = hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("InfoSource").toString();
        		String sequenceNo = hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("SequenceNo").toString();
			    double roomPrice = Double.parseDouble(hotelRoomsObj.getJSONObject("RoomDetail-" + i).getJSONObject("Price").get("RoomPrice").toString());
			    double tax = Double.parseDouble(hotelRoomsObj.getJSONObject("RoomDetail-" + i).getJSONObject("Price").get("Tax").toString());
			    double extraGuestCharge = Double.parseDouble(hotelRoomsObj.getJSONObject("RoomDetail-" + i).getJSONObject("Price").get("ExtraGuestCharge").toString());
			    double childCharge = Double.parseDouble(hotelRoomsObj.getJSONObject("RoomDetail-" + i).getJSONObject("Price").get("ChildCharge").toString());
			    double discount = Double.parseDouble(hotelRoomsObj.getJSONObject("RoomDetail-" + i).getJSONObject("Price").get("Discount").toString());
			    double publishedPrice = Double.parseDouble(hotelRoomsObj.getJSONObject("RoomDetail-" + i).getJSONObject("Price").get("PublishedPrice").toString());
			    double otherCharges = Double.parseDouble(hotelRoomsObj.getJSONObject("RoomDetail-" + i).getJSONObject("Price").get("OtherCharges").toString());
			    double offeredPrice = Double.parseDouble(hotelRoomsObj.getJSONObject("RoomDetail-" + i).getJSONObject("Price").get("OfferedPrice").toString());
			    Integer publishedPriceRoundedOff = Integer.parseInt(hotelRoomsObj.getJSONObject("RoomDetail-" + i).getJSONObject("Price").get("PublishedPriceRoundedOff").toString());
			    Integer offeredPriceRoundedOff = Integer.parseInt(hotelRoomsObj.getJSONObject("RoomDetail-" + i).getJSONObject("Price").get("OfferedPriceRoundedOff").toString());
			    double agentCommission = Double.parseDouble(hotelRoomsObj.getJSONObject("RoomDetail-" + i).getJSONObject("Price").get("AgentCommission").toString());
			    double agentMarkUp = Double.parseDouble(hotelRoomsObj.getJSONObject("RoomDetail-" + i).getJSONObject("Price").get("AgentMarkUp").toString());
			    double serviceTax = Double.parseDouble(hotelRoomsObj.getJSONObject("RoomDetail-" + i).getJSONObject("Price").get("ServiceTax").toString());
			    double tds = Double.parseDouble(hotelRoomsObj.getJSONObject("RoomDetail-" + i).getJSONObject("Price").get("TDS").toString());
        		String roomPromotion = hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("RoomPromotion").toString();
        		String smokingPreference = hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("SmokingPreference").toString();
        		String lastVoucherDate = hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("LastVoucherDate").toString();
        		String cancellationPolicy = hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("CancellationPolicy").toString();
        		String lastCancellationDate = hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("LastCancellationDate").toString();
        		String[] amenities = {};
        		String[] amenity = {};
        		String[] bedTypes = {};
        		String[] hotelSupplements = {};
        		String[] inclusion = {};
        		List<HotelCancelPolicy> cancelPList = new ArrayList<>();
        		List<RoomDayRate> rateList = new ArrayList<>();
        		
    			try {
    				JSONArray jsonFacil = hotelRoomsObj.getJSONObject("RoomDetail-" + i).getJSONArray("Amenities");
    				amenities = new String[jsonFacil.length()];
    				for (int j = 0; j < jsonFacil.length(); j++) {
    					String facil = jsonFacil.getString(j);
    					amenities[j] = facil;
    				}
    			} catch (Exception e) {
    			}
    			
    			try {
    				JSONArray jsonFacil = hotelRoomsObj.getJSONObject("RoomDetail-" + i).getJSONArray("Amenity");
    				amenity = new String[jsonFacil.length()];
    				for (int j = 0; j < jsonFacil.length(); j++) {
    					String facil = jsonFacil.getString(j);
    					amenity[j] = facil;
    				}
    			} catch (Exception e) {
    			}
    			
    			try {
    				JSONArray jsonFacil = hotelRoomsObj.getJSONObject("RoomDetail-" + i).getJSONArray("BedTypes");
    				bedTypes = new String[jsonFacil.length()];
    				for (int j = 0; j < jsonFacil.length(); j++) {
    					String facil = jsonFacil.getString(j);
    					bedTypes[j] = facil;
    				}
    			} catch (Exception e) {
    			}
    			
    			try {
    				JSONArray jsonFacil = hotelRoomsObj.getJSONObject("RoomDetail-" + i).getJSONArray("HotelSupplements");
    				hotelSupplements = new String[jsonFacil.length()];
    				for (int j = 0; j < jsonFacil.length(); j++) {
    					String facil = jsonFacil.getString(j);
    					hotelSupplements[j] = facil;
    				}
    			} catch (Exception e) {
    			}
    			
    			try {
    				JSONArray jsonFacil = hotelRoomsObj.getJSONObject("RoomDetail-" + i).getJSONArray("Inclusion");
    				inclusion = new String[jsonFacil.length()];
    				for (int j = 0; j < jsonFacil.length(); j++) {
    					String facil = jsonFacil.getString(j);
    					inclusion[j] = facil;
    				}
    			} catch (Exception e) {
    			}
    			
    			try {
    				JSONArray jsonFacil = hotelRoomsObj.getJSONObject("RoomDetail-" + i).getJSONArray("CancellationPolicies");
    	        	JSONObject cancelObj = new JSONObject();
    				
    				for (int j = 0; j < jsonFacil.length(); j++) {
    					cancelObj.put("Cancel-" + j, jsonFacil.getJSONObject(j));
    	        		Integer charge = Integer.parseInt(cancelObj.getJSONObject("Cancel-" + j).get("Charge").toString());
    	        		Integer chargeType = Integer.parseInt(cancelObj.getJSONObject("Cancel-" + j).get("ChargeType").toString());
    	        		String currency = cancelObj.getJSONObject("Cancel-" + j).get("Currency").toString();
    	        		String fromDate = cancelObj.getJSONObject("Cancel-" + j).get("FromDate").toString();
    	        		String toDate = cancelObj.getJSONObject("Cancel-" + j).get("ToDate").toString();
    					HotelCancelPolicy cancelPolicy = new HotelCancelPolicy(charge, chargeType, currency, fromDate, toDate);

    	        		System.out.println("Cancel Policy: " + charge + " ... " + fromDate);
    	        		
    					cancelPList.add(cancelPolicy);
    				}
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    			
    			try {
    				JSONArray jsonFacil = hotelRoomsObj.getJSONObject("RoomDetail-" + i).getJSONArray("DayRates");
    	        	JSONObject rateObj = new JSONObject();
    				
    				for (int j = 0; j < jsonFacil.length(); j++) {
    					rateObj.put("Rate-" + j, jsonFacil.getJSONObject(j));
    	        		double amount = Double.parseDouble(rateObj.getJSONObject("Rate-" + j).get("Amount").toString());
    	        		String date = rateObj.getJSONObject("Rate-" + j).get("Date").toString();
    	        		
    	        		System.out.println("Day Rate: " + amount + " ... " + date);
    	        		
    	        		RoomDayRate dayRate = new RoomDayRate(date, amount);
    					
    	        		rateList.add(dayRate);
    				}
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    			
        		
    			HotelRoom room = new HotelRoom(roomTypeCode, roomIndex, roomStatus, roomId, requireAllPaxDetails, roomDescription, roomTypeName, ratePlanCode, ratePlan, ratePlanName, infoSource, 
    					sequenceNo, childCount, roomPromotion, amenities, amenity, smokingPreference, bedTypes, hotelSupplements, lastCancellationDate, cancelPList, roomPrice, tax, extraGuestCharge, 
    					childCharge, discount, availabilityType, publishedPrice, otherCharges, offeredPrice, publishedPriceRoundedOff, offeredPriceRoundedOff, agentCommission, agentMarkUp, serviceTax, tds, 
    					lastVoucherDate, cancellationPolicy, inclusion, isPassportMandatory, isPANMandatory, rateList);
    			
        		hotelRooms.add(room);
			}
			

			model.addAttribute("hotelRoomList", hotelRooms);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		if (history == null) {
			HotelHistory newHistory = new HotelHistory();

			newHistory = new HotelHistory();
			newHistory.setCheckInDate(checkIn);
			newHistory.setCheckOutDate(checkOut);
			newHistory.setChildrenAge(null);
			newHistory.setCityId(city.getCityId().toString());
			newHistory.setCountryCode(city.getCountryCode());
			newHistory.setNearBySearchAllowed(false);
			newHistory.setNoOfAdults(noOfAdults.toString());
			newHistory.setNoOfChild(noOfChildren.toString());
			newHistory.setNoOfRooms(noOfRooms.toString());
			newHistory.setCustomer(customer);
			
			history = hotelService.saveHotelHistory(newHistory, customer);
		}

		bookingURL = "/hotel/booking_" + hotelCity + "_" + checkInDate + "_" + checkOutDate + "_" + noOfAdults + "_" + noOfChildren + "_" + noOfRooms + "_" + resultIndex + "_" + hotelCode + "_" + history.getId();
		
		return "redirect:/hotel_booking...";
	}

	@GetMapping("/hotel_booking...")
    public String performApiLoadBooking(Model model) {
        model.addAttribute("searchURL", bookingURL);
        return "loading/loading";
    }
}
