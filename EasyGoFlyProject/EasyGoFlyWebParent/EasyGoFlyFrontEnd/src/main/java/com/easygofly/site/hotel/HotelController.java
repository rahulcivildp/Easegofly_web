package com.easygofly.site.hotel;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.rowset.serial.SerialBlob;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.easygofly.entity.Customer;
import com.easygofly.entity.Hotel;
import com.easygofly.entity.HotelCancelPolicy;
import com.easygofly.entity.HotelGuest;
import com.easygofly.entity.HotelHistory;
import com.easygofly.entity.HotelOrder;
import com.easygofly.entity.HotelRoom;
import com.easygofly.entity.OrderStatus;
import com.easygofly.entity.RoomDayRate;
import com.easygofly.entity.TBOCity;
import com.easygofly.entity.TBOHotel;
import com.easygofly.entity.TourAttraction;
import com.easygofly.entity.Wallet;
import com.easygofly.site.LogService;
import com.easygofly.site.customer.CustomerService;
import com.easygofly.site.flight.TBOCityRepository;
import com.easygofly.site.flight.order.TransactionService;
import com.easygofly.site.hotel.HotelController.HotelSearchResponse.HotelResult;
import com.easygofly.site.security.EasegoflyPhoneCustomerDetails;
import com.easygofly.site.security.oauth.CustomerOAuth2User;
import com.easygofly.site.setting.PaymentSettingBag;
import com.easygofly.site.setting.SettingService;
import com.easygofly.site.wallet.TotalTransactionService;
import com.easygofly.site.zaakpay.ChecksumGenerator;
import com.easygofly.site.zaakpay.Transaction;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class HotelController {
	@Autowired
	private TBOCityRepository tboRepo;
	@Autowired
	private HotelService hotelService;
	@Autowired
	private OnlineHotelService onlineHotelService;
	@Autowired
	private CustomerService customerService;
	@Autowired
	private LogService logService;
	@Autowired
	TransactionService transactionService;
	@Autowired
	TotalTransactionService totalTransactionService;
	@Autowired
	private SettingService settingService;

	private String searchURL = "";
	private String bookingURL = "";
	private String orderURL = "";

	TBOHotel hotel = new TBOHotel();
	List<HotelRoom> hotelRooms = new ArrayList<HotelRoom>();
	List<HotelGuest> hotelGuests = new ArrayList<HotelGuest>();

	private String[] parameter = new String[20];
	private String checksum;
	private Boolean verifiedChecksum;
	private String[] responseParameters;

	private String bookingIdMain = "";

	HotelHistory history = new HotelHistory();
	HotelSearchAndTBO hotelSearchAndTBO = new HotelSearchAndTBO();
	List<HotelSearchAndTBO> hotelSearchAndTBOList = new ArrayList<HotelSearchAndTBO>();

	@GetMapping("/hotel")
	public String viewHotelPage(Model model) {
		cityFinder(model);

		return "hotel/hotel";
	}

	@PostMapping("/hotel/saveSearchHotel")
	public String saveSearchHotel(@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer,
			@AuthenticationPrincipal CustomerOAuth2User oauthCustomer,
			@RequestParam(name = "hotelCode", required = false) String hotelCode,
			@RequestParam(name = "hotelCity", required = false) String hotelCity,
			@RequestParam(name = "checkInDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date checkInDate,
			@RequestParam(name = "checkOutDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date checkOutDate,
			@RequestParam(name = "noOfAdults", required = false) Integer noOfAdults,
			@RequestParam(name = "noOfChildren", required = false) Integer noOfChildren,
			@RequestParam(name = "noOfAdultsTwo", required = false) Integer noOfAdultsTwo,
			@RequestParam(name = "noOfChildrenTwo", required = false) Integer noOfChildrenTwo,
			@RequestParam(name = "noOfRooms", required = false) Integer noOfRooms,
			@RequestParam(name = "noOfRoomsTwo", required = false) Integer noOfRoomsTwo) {
		if (noOfRoomsTwo == null) {
			noOfRoomsTwo = 0;
		}
		HotelHistory newHistory = new HotelHistory();

		if (loggedCustomer != null) {
			String email = loggedCustomer.getUsername();
			Customer customer = customerService.getByPhone(email);

			history = new HotelHistory();
			newHistory = saveHistoryPojo(hotelCode, hotelCity, checkInDate, checkOutDate, noOfAdults, noOfChildren,
					noOfAdultsTwo, noOfChildrenTwo, noOfRooms, noOfRoomsTwo, customer);

			history = hotelService.saveHotelHistory(newHistory, customer);

		} else if (oauthCustomer != null) {
			String email = oauthCustomer.getEmail();
			Customer customer = customerService.getByEmail(email);

			history = new HotelHistory();
			newHistory = saveHistoryPojo(hotelCode, hotelCity, checkInDate, checkOutDate, noOfAdults, noOfChildren,
					noOfAdultsTwo, noOfChildrenTwo, noOfRooms, noOfRoomsTwo, customer);
			history = hotelService.saveHotelHistory(newHistory, customer);

		} else {

			history = new HotelHistory();
			newHistory = saveHistoryPojo(hotelCode, hotelCity, checkInDate, checkOutDate, noOfAdults, noOfChildren,
					noOfAdultsTwo, noOfChildrenTwo, noOfRooms, noOfRoomsTwo, null);
			history = newHistory;
		}

		searchURL = "/hotel/search_";

		return "redirect:/hotel_loading...";
	}

	private HotelHistory saveHistoryPojo(String hotelCode, String hotelCity, Date checkInDate, Date checkOutDate,
			Integer noOfAdults, Integer noOfChildren, Integer noOfAdultsTwo, Integer noOfChildrenTwo, Integer noOfRooms,
			Integer noOfRoomsTwo, Customer customer) {

		HotelHistory newHistory = new HotelHistory();

		if (noOfRoomsTwo != 0) {
			newHistory.setNoOfRooms(noOfRoomsTwo.toString());
		} else {
			newHistory.setNoOfRooms(noOfRooms.toString());
		}
		if (noOfAdultsTwo == null) {
			noOfAdultsTwo = 0;
		}
		if (noOfChildrenTwo == null) {
			noOfChildrenTwo = 0;
		}

		newHistory.setNoOfAdults(noOfAdults.toString());
		newHistory.setNoOfAdultsTwo(noOfAdultsTwo.toString());
		newHistory.setNoOfChild(noOfChildren.toString());
		newHistory.setNoOfChildTwo(noOfChildrenTwo.toString());
		newHistory.setCheckInDate(checkInDate);
		newHistory.setCheckOutDate(checkOutDate);
		newHistory.setChildrenAge(null);
		newHistory.setCityId(hotelCode);
		newHistory.setCityName(hotelCity);
		newHistory.setCountryCode("IN");
		newHistory.setNearBySearchAllowed(false);
		newHistory.setCustomer(customer);

		return newHistory;
	}

	@GetMapping("/hotel/search_")
	public String viewHotelSearchResult(Model model) throws Exception {

		hotelSearchAndTBOList = new ArrayList<HotelSearchAndTBO>();
		
		long noNights = noOfNightsMethod(history.getCheckInDate(), history.getCheckOutDate());

		Integer noOfCh = Integer.parseInt(history.getNoOfChild());
		Integer noOfroom = Integer.parseInt(history.getNoOfRooms());
		Integer noOfadult = Integer.parseInt(history.getNoOfAdults());
		Integer noOfCh2 = Integer.parseInt(history.getNoOfChildTwo());
		Integer noOfadult2 = Integer.parseInt(history.getNoOfAdultsTwo());

		Integer totalGuests = noOfadult + noOfCh + noOfadult2 + noOfCh2;
		Integer adultCount = noOfadult + noOfadult2;
		Integer childCount = noOfCh + noOfCh2;

		StringBuilder responseBody = onlineHotelService.apiOnlineSearchHotel(history.getCityId());
		ObjectMapper objectMapper = new ObjectMapper();
		HotelResponse resp = objectMapper.readValue(responseBody.toString(), HotelResponse.class);

		String[] hotelCodes = new String[100];
		Integer index = 0;
		String hotelCodesresult = "";

		if (resp.hotels != null) {
			for (Hoteltbo h : resp.hotels) {
				hotelCodes[index] = h.hotelCode;
				index++;
			}

			hotelCodesresult = Arrays.stream(hotelCodes).filter(code -> code != null && !code.isEmpty())
					.collect(Collectors.joining(","));
		} else {
			hotelCodesresult = "No Hotels Found";
		}
		System.out.println(hotelCodesresult);

		List<String> arrRoomGuest = new ArrayList<String>();

		for (int i = 0; i < noOfroom; i++) {

			if (i == 0) {
				List<Integer> arrChildrenAge = new ArrayList<Integer>();
				String arrayChildAge = null;

				for (int j = 0; j < noOfCh; j++) {
					arrChildrenAge.add(11);
				}
				
				if (noOfCh > 0) {
					arrayChildAge = arrChildrenAge.stream().map(val -> String.valueOf(val))
							.collect(Collectors.joining(",", "[", "]"));
				} else {
					arrayChildAge = null;
				}

				String roomGuest = "{\r\n" + "      \"Adults\": " + noOfadult + ",\r\n" + "      \"Children\": "
						+ noOfCh + ",\r\n" + "      \"ChildrenAges\": " + arrayChildAge + "\r\n" + "    }";

				arrRoomGuest.add(roomGuest);
			} else {
				List<Integer> arrChildrenAge = new ArrayList<Integer>();
				String arrayChildAge = null;
				
				for (int j = 0; j < noOfCh2; j++) {
					arrChildrenAge.add(11);
				}
				if (noOfCh2 > 0) {
					arrayChildAge = arrChildrenAge.stream().map(val -> String.valueOf(val))
							.collect(Collectors.joining(",", "[", "]"));
				} else {
					arrayChildAge = null;
				}
				 

				String roomGuest = "{\r\n" + "      \"Adults\": " + noOfadult2 + ",\r\n" + "      \"Children\": "
						+ noOfCh2 + ",\r\n" + "      \"ChildrenAges\": " + arrayChildAge + "\r\n" + "    }";

				arrRoomGuest.add(roomGuest);
			}

		}

		String arrayRoomGuest = arrRoomGuest.stream().map(val -> String.valueOf(val))
				.collect(Collectors.joining(",", "[", "]"));
				
		StringBuilder responseBodySearch = onlineHotelService.apiOnlineSearchHotelInfos(history.getCheckInDate(),
				history.getCheckOutDate(), hotelCodesresult, arrayRoomGuest, noOfroom);
		ObjectMapper objectMapper2 = new ObjectMapper();
		
		HotelSearchResponse hotelSearchResponse = objectMapper2.readValue(responseBodySearch.toString(), HotelSearchResponse.class);
		hotelSearchResponse.hotelResult.forEach(ht -> {
			resp.hotels.forEach(rh -> {
				if (ht.hotelCode.equals(rh.hotelCode)) {
					hotelSearchAndTBOList.add(new HotelSearchAndTBO(rh, ht));
				}
			});
		});

		model.addAttribute("hotelCity", history.getCityName());
//		model.addAttribute("checkIn", checkIn);
		model.addAttribute("checkInDate", history.getCheckInDate());
		model.addAttribute("checkOutDate", history.getCheckOutDate());
		model.addAttribute("totalGuests", totalGuests);
		model.addAttribute("noOfAdults", adultCount);
		model.addAttribute("noOfChildren", childCount);
		model.addAttribute("noOfRooms", history.getNoOfRooms());
		model.addAttribute("noNights", noNights);
		model.addAttribute("hotelList", hotelSearchAndTBOList);

		return "hotel/search/hotel-search-result";
	}
	
	@GetMapping("/hotel_loading...")
	public String performApiRequest(Model model) {
		model.addAttribute("searchURL", searchURL);
		return "loading/loading";
	}

	//**************************** pojo **********************************************//
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class HotelResponse {

		@JsonProperty("Status")
		public Status status;

		@JsonProperty("Hotels")
		public List<Hoteltbo> hotels;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Status {
		@JsonProperty("Code")
		public int code;

		@JsonProperty("Description")
		public String description;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Hoteltbo {
		@JsonProperty("HotelCode")
		public String hotelCode;

		@JsonProperty("HotelName")
		public String hotelName;

		@JsonProperty("HotelRating")
		public String hotelRating;

		@JsonProperty("Address")
		public String address;

		@JsonProperty("Attractions")
		public List<String> attractions;

		@JsonProperty("CountryName")
		public String countryName;

		@JsonProperty("CountryCode")
		public String countryCode;

		@JsonProperty("Description")
		public String description;

		@JsonProperty("FaxNumber")
		public String faxNumber;

		@JsonProperty("HotelFacilities")
		public List<String> hotelFacilities;

		@JsonProperty("Map")
		public String map;

		@JsonProperty("PhoneNumber")
		public String phoneNumber;

		@JsonProperty("PinCode")
		public String pinCode;

		@JsonProperty("HotelWebsiteUrl")
		public String hotelWebsiteUrl;

		@JsonProperty("CityName")
		public String cityName;
	}

	public static class HotelSearchResponse {

	    @JsonProperty("Status")
	    public Status status;

	    @JsonProperty("HotelResult")
	    @JsonIgnoreProperties(ignoreUnknown = true)
	    public List<HotelResult> hotelResult;
	    
	    public HotelSearchResponse() {}

		public static class Status {
	        @JsonProperty("Code")
	        public int code;

	        @JsonProperty("Description")
	        public String description;

			public Status() {}
	    }

	    @JsonIgnoreProperties(ignoreUnknown = true)
	    public static class HotelResult {
	        @JsonProperty("HotelCode")
	        public String hotelCode;

	        @JsonProperty("Currency")
	        public String currency;

	        @JsonProperty("Rooms")
	        public List<Room> rooms;

			public HotelResult() {}
	    }

	    @JsonIgnoreProperties(ignoreUnknown = true)
	    public static class Room {
	        @JsonProperty("Name")
	        public List<String> name;

	        @JsonProperty("BookingCode")
	        public String bookingCode;

	        @JsonProperty("Inclusion")
	        public String inclusion;

	        @JsonProperty("DayRates")
	        public List<List<DayRate>> dayRates;

	        @JsonProperty("TotalFare")
	        public double totalFare;

	        @JsonProperty("TotalTax")
	        public double totalTax;

	        @JsonProperty("RoomPromotion")
	        public List<String> roomPromotion;

	        @JsonProperty("CancelPolicies")
	        public List<CancelPolicy> cancelPolicies;

	        @JsonProperty("MealType")
	        public String mealType;

	        @JsonProperty("IsRefundable")
	        public boolean isRefundable;

	        @JsonProperty("WithTransfers")
	        public boolean withTransfers;

	        @JsonProperty("BeddingGroup")
	        public String beddingGroup;

	        @JsonProperty("Supplements")
		    @JsonIgnoreProperties(ignoreUnknown = true)
	        public List<List<Supplement>> supplementList;

			public Room() {}
	    }

	    @JsonIgnoreProperties(ignoreUnknown = true)
	    public static class Supplement {
	        @JsonProperty("Index")
	        public Integer index;
	        
	        @JsonProperty("Type")
	        public String type;
	        
	        @JsonProperty("Description")
	        public String Description;
	        
	        @JsonProperty("Price")
	        public double Price;
	        
	        @JsonProperty("Currency")
	        public String Currency;

			public Supplement() {}
	    }
	    

	    @JsonIgnoreProperties(ignoreUnknown = true)
	    public static class DayRate {
	        @JsonProperty("BasePrice")
	        public double basePrice;

			public DayRate() {}
	    }

	    @JsonIgnoreProperties(ignoreUnknown = true)
	    public static class CancelPolicy {
	        @JsonProperty("FromDate")
	        public String fromDate;

	        @JsonProperty("ChargeType")
	        public String chargeType;

	        @JsonProperty("CancellationCharge")
	        public double cancellationCharge;

			public CancelPolicy() {}
	    }
	}

	public static class HotelSearchAndTBO {
		public Hoteltbo hoteltbo;
		
		public HotelSearchResponse.HotelResult hotelResults;
		
		public HotelSearchAndTBO() {}

		public HotelSearchAndTBO(Hoteltbo hoteltbo, HotelResult hotelResults) {
			this.hoteltbo = hoteltbo;
			this.hotelResults = hotelResults;
		}
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class HotelPreBookResponse {

	    @JsonProperty("Status")
	    private Status status;

	    @JsonProperty("HotelResult")
	    private List<HotelResult> hotelResult;

	    @JsonProperty("ValidationInfo")
	    private ValidationInfo validationInfo;

	    // Getters and Setters

	    @JsonIgnoreProperties(ignoreUnknown = true)
	    public static class Status {
	        @JsonProperty("Code")
	        private int code;

	        @JsonProperty("Description")
	        private String description;

	        // Getters and Setters
	    }

	    @JsonIgnoreProperties(ignoreUnknown = true)
	    public static class HotelResult {
	        @JsonProperty("HotelCode")
	        private String hotelCode;

	        @JsonProperty("Currency")
	        private String currency;

	        @JsonProperty("Rooms")
	        private List<Room> rooms;

	        @JsonProperty("RateConditions")
	        private List<String> rateConditions;

	        // Getters and Setters
	    }

	    @JsonIgnoreProperties(ignoreUnknown = true)
	    public static class Room {
	        @JsonProperty("Name")
	        private List<String> name;

	        @JsonProperty("BookingCode")
	        private String bookingCode;

	        @JsonProperty("Inclusion")
	        private String inclusion;

	        @JsonProperty("DayRates")
	        private List<List<DayRate>> dayRates;

	        @JsonProperty("TotalFare")
	        private double totalFare;

	        @JsonProperty("TotalTax")
	        private double totalTax;

	        @JsonProperty("NetAmount")
	        private double netAmount;

	        @JsonProperty("NetTax")
	        private double netTax;

	        @JsonProperty("CancelPolicies")
	        private List<CancelPolicy> cancelPolicies;

	        @JsonProperty("MealType")
	        private String mealType;

	        @JsonProperty("IsRefundable")
	        private boolean isRefundable;

	        @JsonProperty("Supplements")
	        private List<List<Supplement>> supplements;

	        @JsonProperty("WithTransfers")
	        private boolean withTransfers;

	        @JsonProperty("Amenities")
	        private List<String> amenities;

	        @JsonProperty("LastCancellationDeadline")
	        private String lastCancellationDeadline;

	        @JsonProperty("PriceBreakUp")
	        private List<PriceBreakUp> priceBreakUp;

	        // Getters and Setters
	    }

	    @JsonIgnoreProperties(ignoreUnknown = true)
	    public static class DayRate {
	        @JsonProperty("BasePrice")
	        private double basePrice;

	        // Getters and Setters
	    }

	    @JsonIgnoreProperties(ignoreUnknown = true)
	    public static class CancelPolicy {
	        @JsonProperty("FromDate")
	        private String fromDate;

	        @JsonProperty("ChargeType")
	        private String chargeType;

	        @JsonProperty("CancellationCharge")
	        private double cancellationCharge;

	        // Getters and Setters
	    }

	    @JsonIgnoreProperties(ignoreUnknown = true)
	    public static class Supplement {
	        @JsonProperty("Index")
	        private int index;

	        @JsonProperty("Type")
	        private String type;

	        @JsonProperty("Description")
	        private String description;

	        @JsonProperty("Price")
	        private double price;

	        @JsonProperty("Currency")
	        private String currency;

	        // Getters and Setters
	    }

	    @JsonIgnoreProperties(ignoreUnknown = true)
	    public static class PriceBreakUp {
	        @JsonProperty("RoomRate")
	        private double roomRate;

	        @JsonProperty("RoomTax")
	        private double roomTax;

	        @JsonProperty("AgentCommission")
	        private double agentCommission;

	        @JsonProperty("TaxBreakup")
	        private List<TaxBreakup> taxBreakup;

	        // Getters and Setters
	    }

	    @JsonIgnoreProperties(ignoreUnknown = true)
	    public static class TaxBreakup {
	        @JsonProperty("TaxType")
	        private String taxType;

	        @JsonProperty("TaxableAmount")
	        private double taxableAmount;

	        @JsonProperty("TaxPercentage")
	        private double taxPercentage;

	        @JsonProperty("TaxAmount")
	        private double taxAmount;

	        // Getters and Setters
	    }

	    @JsonIgnoreProperties(ignoreUnknown = true)
	    public static class ValidationInfo {
	        @JsonProperty("PanMandatory")
	        private boolean panMandatory;

	        @JsonProperty("PassportMandatory")
	        private boolean passportMandatory;

	        @JsonProperty("CorporateBookingAllowed")
	        private boolean corporateBookingAllowed;

	        @JsonProperty("PanCountRequired")
	        private int panCountRequired;

	        @JsonProperty("SamePaxNameAllowed")
	        private boolean samePaxNameAllowed;

	        @JsonProperty("SpaceAllowed")
	        private boolean spaceAllowed;

	        @JsonProperty("SpecialCharAllowed")
	        private boolean specialCharAllowed;

	        @JsonProperty("PaxNameMinLength")
	        private int paxNameMinLength;

	        @JsonProperty("PaxNameMaxLength")
	        private int paxNameMaxLength;

	        @JsonProperty("CharLimit")
	        private boolean charLimit;

	        @JsonProperty("PackageFare")
	        private boolean packageFare;

	        @JsonProperty("PackageDetailsMandatory")
	        private boolean packageDetailsMandatory;

	        @JsonProperty("DepartureDetailsMandatory")
	        private boolean departureDetailsMandatory;

	        @JsonProperty("GSTAllowed")
	        private boolean gstAllowed;

	        // Getters and Setters
	    }
	}

	
	//*************************************************************************************************//
	

	 @GetMapping("/hotel/booking_")
	public String hotelBooking(Model model, 
			@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer, @AuthenticationPrincipal CustomerOAuth2User oauthCustomer) throws Exception {

        Customer customer = new Customer();
		Integer noOfCh = Integer.parseInt(history.getNoOfChild());
		Integer noOfroom = Integer.parseInt(history.getNoOfRooms());
		Integer noOfadult = Integer.parseInt(history.getNoOfAdults());
		Integer noOfCh2 = Integer.parseInt(history.getNoOfChildTwo());
		Integer noOfadult2 = Integer.parseInt(history.getNoOfAdultsTwo());

		Integer totalGuests = noOfadult + noOfCh + noOfadult2 + noOfCh2;
		Integer adultCount = noOfadult + noOfadult2;
		Integer childCount = noOfCh + noOfCh2;
        
        if (loggedCustomer != null) {
        	customer = customerService.getByPhone(loggedCustomer.getUsername());
		} else if (oauthCustomer !=  null) {
        	customer = customerService.getByEmail(oauthCustomer.getEmail());
			
		}		
        
        long noNights = noOfNightsMethod(history.getCheckInDate(), history.getCheckOutDate());
        

		StringBuilder responseBodySearch = onlineHotelService.apiOnlineHotelPreBook(hotelSearchAndTBO.hotelResults.rooms.get(0).bookingCode);
		ObjectMapper objectMapper2 = new ObjectMapper();
		
		HotelPreBookResponse hotelPreBookResponse = objectMapper2.readValue(responseBodySearch.toString(), HotelPreBookResponse.class);
		
		hotel = new TBOHotel();
				
		TBOHotel newHotel = new TBOHotel(hotelSearchAndTBO.hotelResults.hotelCode, hotelSearchAndTBO.hotelResults.rooms.get(0).bookingCode, hotelSearchAndTBO.hoteltbo.hotelName, hotelPreBookResponse.hotelResult.get(0).rooms.get(0).mealType, hotelSearchAndTBO.hoteltbo.hotelRating, new SerialBlob(hotelSearchAndTBO.hoteltbo.description.getBytes()), new SerialBlob(new ObjectMapper().writeValueAsString(hotelSearchAndTBO.hoteltbo.attractions).getBytes()), hotelSearchAndTBO.hoteltbo.address, hotelSearchAndTBO.hoteltbo.phoneNumber, hotelSearchAndTBO.hoteltbo.map, hotelSearchAndTBO.hoteltbo.map.split("|")[1], hotelSearchAndTBO.hoteltbo.map.split("|")[0], hotelSearchAndTBO.hoteltbo.pinCode, hotelPreBookResponse.hotelResult.get(0).rooms.get(0).lastCancellationDeadline, hotelPreBookResponse.hotelResult.get(0).rooms.get(0).totalFare, hotelPreBookResponse.hotelResult.get(0).rooms.get(0).netAmount, hotelPreBookResponse.hotelResult.get(0).rooms.get(0).totalTax, hotelPreBookResponse.hotelResult.get(0).rooms.get(0).netTax, customer);
						
		if (noOfroom == 2) {
			for (int i = 0; i < 2; i++) {
				newHotel.addTboRooms(hotelPreBookResponse.hotelResult.get(0).rooms.get(0).name.get(i), hotelPreBookResponse.hotelResult.get(0).rooms.get(0).priceBreakUp.get(i).roomRate, hotelPreBookResponse.hotelResult.get(0).rooms.get(0).priceBreakUp.get(i).roomTax, hotelPreBookResponse.hotelResult.get(0).rooms.get(0).priceBreakUp.get(i).agentCommission, hotelPreBookResponse.hotelResult.get(0).rooms.get(0).priceBreakUp.get(i).taxBreakup.get(0).taxableAmount, hotelPreBookResponse.hotelResult.get(0).rooms.get(0).cancelPolicies.get(i).fromDate, hotelPreBookResponse.hotelResult.get(0).rooms.get(0).cancelPolicies.get(i).chargeType, (int) Math.round(hotelPreBookResponse.hotelResult.get(0).rooms.get(0).cancelPolicies.get(i).cancellationCharge));
			}
		} else {
			newHotel.addTboRooms(hotelPreBookResponse.hotelResult.get(0).rooms.get(0).name.get(0), hotelPreBookResponse.hotelResult.get(0).rooms.get(0).priceBreakUp.get(0).roomRate, hotelPreBookResponse.hotelResult.get(0).rooms.get(0).priceBreakUp.get(0).roomTax, hotelPreBookResponse.hotelResult.get(0).rooms.get(0).priceBreakUp.get(0).agentCommission, hotelPreBookResponse.hotelResult.get(0).rooms.get(0).priceBreakUp.get(0).taxBreakup.get(0).taxableAmount, hotelPreBookResponse.hotelResult.get(0).rooms.get(0).cancelPolicies.get(0).fromDate, hotelPreBookResponse.hotelResult.get(0).rooms.get(0).cancelPolicies.get(0).chargeType, (int) Math.round(hotelPreBookResponse.hotelResult.get(0).rooms.get(0).cancelPolicies.get(0).cancellationCharge));
		}
	
		hotel = hotelService.tboHotelSave(newHotel);
        
    	model.addAttribute("hotelCity", history.getCityName());
//		model.addAttribute("checkIn", checkIn);
		model.addAttribute("checkInDate", history.getCheckInDate());
		model.addAttribute("checkOutDate", history.getCheckOutDate());
		model.addAttribute("totalGuests", totalGuests);
		model.addAttribute("noOfAdults", adultCount);
		model.addAttribute("noOfChildren", childCount);
		model.addAttribute("noOfRooms", history.getNoOfRooms());
		model.addAttribute("noNights", noNights);
		model.addAttribute("hotelHistory", history);
		model.addAttribute("hotelGuests", hotelGuests);
		model.addAttribute("cust_id", customer.getId());
		model.addAttribute("hotelSearchAndTBO", hotelSearchAndTBO);
		model.addAttribute("hotelPreBookResponse", hotelPreBookResponse);
        
		return "hotel/booking/hotel-booking";
	}

	@PostMapping("/hotel/saveHotel")
	public String saveHotel(@RequestParam(name = "bookingCode", required = false) String bookingCode, @AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer,
			@AuthenticationPrincipal CustomerOAuth2User oauthCustomer) throws Exception, InterruptedException {
		Customer customer = new Customer();
		AtomicReference<HotelSearchAndTBO> hotelSearchAndTBOref = new AtomicReference<>(null);
		
		if (loggedCustomer != null) {
			customer = customerService.getByPhone(loggedCustomer.getUsername());
		} else if (oauthCustomer != null) {
			customer = customerService.getByEmail(oauthCustomer.getEmail());
		}

		if (history != null) {
			history = hotelService.saveHotelHistory(history, customer);
		} 
		
		hotelSearchAndTBOList.forEach(hts -> {
			hts.hotelResults.rooms.forEach(rm -> {
				if (rm.bookingCode.equals(bookingCode)) {
					hotelSearchAndTBOref.set(hts);
				}
			});
		});
		
		hotelSearchAndTBO = hotelSearchAndTBOref.get();

		bookingURL = "/hotel/booking_";

		return "redirect:/hotel_booking...";
	}

	@GetMapping("/hotel_booking...")
	public String performApiLoadBooking(Model model) {
		model.addAttribute("searchURL", bookingURL);
		return "loading/loading";
	}

	@PostMapping("/hotel/save_order")
	public String saveHotelOrder(@RequestParam(name = "hotel_id", required = false) Integer hotel_id,
			@RequestParam(name = "search_id", required = false) Integer search_id,
			@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer,
			@AuthenticationPrincipal CustomerOAuth2User oauthCustomer) {
		Hotel hotel = hotelService.findByIdHotel(hotel_id);
		List<HotelRoom> hotelRooms = hotel.getHotelRooms();
		List<HotelGuest> hotelGuests = hotel.getGuests();

		for (HotelRoom room : hotelRooms) {
			Integer count = 0;
			for (HotelGuest guest : hotelGuests) {
				if (guest.getHotelRoom().getId() == room.getId()) {
					if (guest.getPaxType() == 1) {
						count++;
						if (count == 1) {
							guest.setLeadPassenger(true);
							hotelService.saveGuest(guest);
						} else {
							guest.setLeadPassenger(false);
							hotelService.saveGuest(guest);
						}

						System.out.println("Guest lead: " + guest.isLeadPassenger());
					} else {
						guest.setLeadPassenger(false);
						hotelService.saveGuest(guest);
					}
				}
			}

			System.out.println("Total adult: " + count);
		}

//		Customer customer = customerService.getByPhone(loggedCustomer.getUsername());

		orderURL = "/hotel/order_" + hotel_id + "_" + search_id;

		return "redirect:/hotel_order_book...";
	}

	// @GetMapping("/hotel/order_{hotel_id}_{search_id}")
//	public String hotelOrder(Model model,
//			@PathVariable(name = "hotel_id") Integer hotel_id,
//			@PathVariable(name = "search_id") Integer search_id,
//			@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer, @AuthenticationPrincipal CustomerOAuth2User oauthCustomer, 
//			HttpServletRequest request, RedirectAttributes redirectAttributes) throws MalformedURLException, IOException {
//		
//
//        Customer customer = new Customer();
//        
//        if (loggedCustomer != null) {
//        	customer = customerService.getByPhone(loggedCustomer.getUsername());
//		} else if (oauthCustomer !=  null) {
//        	customer = customerService.getByEmail(oauthCustomer.getEmail());
//			
//		}
//        
//    	PaymentSettingBag paymentSettingBag = settingService.getPaymentSettings();
//		
//		Hotel hotel = hotelService.findByIdHotel(hotel_id);
//	    HotelHistory hotelHistory = hotelService.findByIdHistory(search_id);
//		TBOCity city = tboRepo.getCityByCityId(Integer.parseInt(hotelHistory.getCityId()));
//	    Date createdDate = new Date();
//	    
//	    String hotelOrdername = hotelHistory.getCheckInDate() + "-" + hotelHistory.getCheckOutDate() + ":(" + city.getDestination() + "):" + createdDate;
//	    
//	    HotelOrder hotelOrder = new HotelOrder(hotelOrdername, createdDate, OrderStatus.NEW, customer, hotelHistory, hotel);
//		
//	    HotelOrder savedOrder= hotelService.saveOrder(hotelOrder);
//
//	    String categoryId = "";
//		try {
//			categoryId = hotel.getHotelSupplierCodes().stream().findFirst().get().getCategoryId();
//		} catch (Exception e) {
//			categoryId = "";
//		}
//		hotelBlockMethod(model, hotel.getResultIndex().toString(), hotel.getHotelCode(), hotel.getHotelName(), hotelHistory.getNoOfRooms(), hotel, categoryId);
//
//		String[] checkInArr = hotelHistory.getCheckInDate().toString().split(" ");
//		String[] checkOutArr = hotelHistory.getCheckOutDate().toString().split(" ");
//		
//		Integer noOfNights = noOfNightsMethod(checkInArr[0], checkOutArr[0]);
//		Integer totalGuests = Integer.parseInt(hotelHistory.getNoOfAdults()) + Integer.parseInt(hotelHistory.getNoOfChild());
//		
//		Wallet wallet = customer.getWallet();
//		Double doubleAmount = (double) (wallet.getBalance() / 100);
//		model.addAttribute("balance", doubleAmount);
//		
//		/* ------ ZAAKPAY -------- */ /**/
//		Date date = Calendar.getInstance().getTime();  
//	    DateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");  
//	    DateFormat dateFormat2 = new SimpleDateFormat("hhmmss");
//	    String strDate1 = dateFormat1.format(date);
//	    String strDate2 = dateFormat2.format(date);
//		
//	    Integer totalPrice = 0;
//	    
//	    for (HotelRoom room : hotel.getHotelRooms()) {
//	    	totalPrice = totalPrice + room.getPublishedPriceRoundedOff();
//		}
//	    
//		String orderString = "EGF" + strDate1 + "T" + strDate2 + "HO" + savedOrder.getId();
//		Integer intAmount = (int) (totalPrice * 100);
//		String amount = "" + intAmount;
//		//String amount = "100";
//
//		//Cookie cookie = request.getCookies().get("JSESSIONID");
//		//String value = cookie.getValue();
//		
//		for (Cookie cookie : request.getCookies()) {
//			if(cookie.getName().equals("JSESSIONID")) {
//				String value = cookie.getValue();
//				model.addAttribute("JSESSIONID", value);
//			}
//		}
//		
//		
//		Transaction transaction = new Transaction();
//		
//		try {
//			ZaakpayApiRequestParameters processPayment = transaction.processPaymentHotel(orderString, amount, paymentSettingBag);
//			
//			model.addAttribute("entrySet", processPayment.getRequestParameters().entrySet());
//			model.addAttribute("requestUrl", processPayment.getRequestUrl());
//			model.addAttribute("checksum", processPayment.getChecksum());
//			
//		} catch (Exception e) {
//		}
//		
//		/* ******************************************************************************** */
//		
//		model.addAttribute("hotelHistory", hotelHistory);
//		model.addAttribute("hotel", hotel);
//		model.addAttribute("noOfNights", noOfNights);
//		model.addAttribute("totalGuests", totalGuests);
//		model.addAttribute("checkIn", hotelHistory.getCheckInDate());
//		model.addAttribute("hotelCity", city.getDestination());
//		model.addAttribute("totalPrice", totalPrice);
//		model.addAttribute("savedOrder", savedOrder);
//		
//        
//		return "hotel/order/hotel-order";
//	}

	@GetMapping("/hotel_order_book...")
	public String performApiLoadBook(Model model) {
		model.addAttribute("searchURL", orderURL);
		return "loading/loading";
	}

	@PostMapping("/hotel/previousSearchPage")
	public String previousPageSearch(@RequestParam(name = "history_id", required = false) Integer history_id) {

		HotelHistory newHistory = hotelService.findByIdHistory(history_id);
		TBOCity city = tboRepo.getCityByCityId(Integer.parseInt(newHistory.getCityId()));
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		searchURL = "/hotel/search_" + city.getDestination() + "_" + dateFormat.format(newHistory.getCheckInDate())
				+ "_" + dateFormat.format(newHistory.getCheckOutDate()) + "_" + newHistory.getNoOfAdults() + "_"
				+ newHistory.getNoOfChild() + "_" + newHistory.getNoOfRooms();

		return "redirect:/hotel_loading...";
	}

	@PostMapping("/hotel/order/wallet_check")
	public String hotelWalletPayment(@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer,
			@AuthenticationPrincipal CustomerOAuth2User oauthCustomer,
			@RequestParam(name = "order_id") Integer order_id, @RequestParam(name = "hotel_id") Integer hotel_id) {

		Customer customer = new Customer();

		if (loggedCustomer != null) {
			customer = customerService.getByPhone(loggedCustomer.getUsername());
		} else if (oauthCustomer != null) {
			customer = customerService.getByEmail(oauthCustomer.getEmail());

		}

		Hotel hotel = hotelService.findByIdHotel(hotel_id);
		HotelOrder hotelOrder = hotelService.findByIdOrder(order_id);
		Integer totalPrice = 0;

		for (HotelRoom room : hotel.getHotelRooms()) {
			totalPrice = totalPrice + room.getPublishedPriceRoundedOff();
		}
		HotelOrder updatedOrder = hotelService.updateOrderPrice(order_id, totalPrice);

		Wallet wallet = hotelService.hotelWalletPayOrder(customer, hotelOrder);

		if (wallet != null) {
			updatedOrder = hotelService.updateOrderStatus(hotelOrder.getId(), OrderStatus.SUCCESSFULL);
		} else {
			updatedOrder = hotelService.updateOrderStatus(hotelOrder.getId(), OrderStatus.FAILED);
		}

		return "redirect:/hotel/order/wallet_response_" + updatedOrder.getId();
	}

	@GetMapping("/hotel/order/wallet_response_{order_id}")
	public String showHotelWalletPayment(@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer,
			@AuthenticationPrincipal CustomerOAuth2User oauthCustomer, Model model,
			@PathVariable(name = "order_id") Integer order_id) throws MalformedURLException, IOException {

		Customer customer = new Customer();

		if (loggedCustomer != null) {
			customer = customerService.getByPhone(loggedCustomer.getUsername());
		} else if (oauthCustomer != null) {
			customer = customerService.getByEmail(oauthCustomer.getEmail());

		}

		HotelOrder order = hotelService.findByIdOrder(order_id);
		model.addAttribute("orderId", order.getId());
		model.addAttribute("order", order);

//	    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//	    String checkIn = dateFormat.format(order.getHotelHistory().getCheckInDate());
//	    String checkOut = dateFormat.format(order.getHotelHistory().getCheckOutDate());

		// Integer noOfNights = noOfNightsMethod(checkIn, checkOut);
		String rommCOunt = "" + order.getHotel().getHotelRooms().size();

		String[] hasErrorArr = new String[2];
		String categoryId = "";
		try {
			categoryId = order.getHotel().getHotelSupplierCodes().stream().findFirst().get().getCategoryId();
		} catch (Exception e) {
			categoryId = "";
		}
		hasErrorArr = hotelBookMethod(model, order.getHotel().getResultIndex().toString(),
				order.getHotel().getHotelCode(), order.getHotel().getHotelName(), rommCOunt, order.getHotel(),
				categoryId);

		hotelGetBookingDetails(bookingIdMain);

		if (hasErrorArr[0].equals("0")) {
			model.addAttribute("paymentSuccess", "Successfull");
		} else if (hasErrorArr[0].equals("5")) {
			model.addAttribute("paymentCancelled", hasErrorArr[0]);
		} else {
			hotelService.walletPayHotelOrderCancel(customer, order, OrderStatus.FAILED);
			model.addAttribute("paymentCancelled", hasErrorArr[1]);
		}

		model.addAttribute("amount", order.getPrice());

		return "wallet/hotel/response";
	}

	@CrossOrigin(origins = { "https://easegofly.com/" })
	@RequestMapping(value = "/zaakpay/hotel/response", method = { RequestMethod.POST })
	public String zaakpayHotelResponse(HttpServletRequest request, HttpServletResponse response,
			@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer,
			@AuthenticationPrincipal CustomerOAuth2User oauthCustomer,
			@RequestParam(name = "search_id") Integer search_id) throws Exception {

		PaymentSettingBag paymentSettingBag = settingService.getPaymentSettings();
		Transaction transaction = new Transaction();
		ChecksumGenerator checksumGenerator = new ChecksumGenerator();
		String checksumString = "";
		Integer n = 0;
		for (String param : transaction.getResponseParameters()) {
			checksumString = checksumString + param + "=" + request.getParameter(param);
			checksumString = checksumString + "&";
			// This will create the checksum string against every parameter.
			parameter[n] = request.getParameter(param);
			n += 1;
		}

		Boolean verifyChecksum = checksumGenerator.verifyChecksum(paymentSettingBag.getSecretKey(), checksumString,
				request.getParameter("checksum"));
		verifiedChecksum = verifyChecksum;
		checksum = request.getParameter("checksum");
		responseParameters = transaction.getResponseParameters();

		String orderParam = parameter[8];
		String[] parts = orderParam.split("HO");
		String part2 = parts[1]; // 034556
		Integer convert = Integer.parseInt(part2);
		HotelOrder order = hotelService.findByIdOrder(convert);

		if (parameter[12].equals("Customer cancelled transaction. Transaction has failed")) {
			hotelService.updateOrderStatus(order.getId(), OrderStatus.CANCELLED);
		} else if (parameter[12]
				.equals("Unfortunately the transaction has failed.Please try again. Transaction has failed")) {
			hotelService.updateOrderStatus(order.getId(), OrderStatus.FAILED);
		} else if (parameter[12].equals("Unfortunately the transaction has failed.Please try again.")) {
			hotelService.updateOrderStatus(order.getId(), OrderStatus.FAILED);
		} else if (parameter[12].equals("The transaction was completed successfully.")
				|| parameter[12].equals("Transaction has been settled.")) {
			hotelService.updateOrderStatus(order.getId(), OrderStatus.SUCCESSFULL);
		}

		return "redirect:/zaakpay/response";
	}

//	@CrossOrigin(origins = {"https://easegofly.com/"})
//	@RequestMapping(value = "/zaakpay/hotel/response",
//			method = {RequestMethod.GET})
//	public String zaakpayHotelResponseSe (Model model, 
//			@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer, @AuthenticationPrincipal CustomerOAuth2User oauthCustomer) throws Exception {
//		
//
//        Customer customer = new Customer();
//        
//        if (loggedCustomer != null) {
//        	customer = customerService.getByPhone(loggedCustomer.getUsername());
//		} else if (oauthCustomer !=  null) {
//        	customer = customerService.getByEmail(oauthCustomer.getEmail());
//			
//		}
//        
//		model.addAttribute("customer", customer);
//		
//		System.out.println(parameter);
//	    com.easygofly.entity.Transaction selfTrans = transactionService.createTransaction(customer, parameter);
//	    totalTransactionService.createTotalTransaction(customer, Double.parseDouble(selfTrans.getAmount()), false, true, null, null, null, selfTrans.getId(), OrderStatus.NEW);
//
//		String orderParam = parameter[8];
//		model.addAttribute("orderId", orderParam);
//		String[] parts = orderParam.split("HO");
//		String part2 = parts[1]; // 034556
//		Integer convert = Integer.parseInt(part2);
//		HotelOrder order= hotelService.findByIdOrder(convert);
//		model.addAttribute("order", order);
//		  
//	    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//	    String checkIn = dateFormat.format(order.getHotelHistory().getCheckInDate());
//	    String checkOut = dateFormat.format(order.getHotelHistory().getCheckOutDate());
//	    
//		Integer noOfNights = noOfNightsMethod(checkIn, checkOut);
//		
//		model.addAttribute("orderId", order.getId());
//		
//		String[] hasErrorArr = new String[2];	
//		String categoryId = "";
//		try {
//			categoryId = order.getHotel().getHotelSupplierCodes().stream().findFirst().get().getCategoryId();
//		} catch (Exception e) {
//			categoryId = "";
//		}
//		hasErrorArr = hotelBookMethod(model, order.getHotel().getResultIndex().toString(), order.getHotel().getHotelCode(), order.getHotel().getHotelName(), noOfNights.toString(), order.getHotel(), categoryId);
//		
//		hotelGetBookingDetails(bookingIdMain);
//		
//		if (parameter[9].contains("Not Found") && parameter[10].contains("unknown") ) {
//			model.addAttribute("paymentCancelled", parameter[12]);
//		} else if (parameter[12].contains("Unfortunately the transaction has failed.Please try again. Transaction has failed")) {
//			model.addAttribute("paymentCancelled", parameter[12]);
//		} else if (parameter[12].contains("Unfortunately the transaction has failed.Please try again.")) {
//			model.addAttribute("paymentCancelled", parameter[12]);
//		} else if (parameter[12].equals("") || parameter[12] == null || parameter[9] == null) {
//			model.addAttribute("paymentCancelled", parameter[12]);
//		} else if (parameter[12].equals("Your Bank has declined this transaction please Retry this payment with another pay method.")) {
//			model.addAttribute("paymentCancelled", parameter[12]);
//		} else if (parameter[12].contains("Your Bank has declined this transaction please Retry this payment with another pay method.")) {
//			model.addAttribute("paymentCancelled", parameter[12]);
//		} else if (parameter[11].contains("1017")) {
//			model.addAttribute("paymentCancelled", parameter[12]);
//		} else if (parameter[12].contains("The transaction was completed successfully.") || parameter[12].contains("Transaction has been settled.")) {
//			
//			if (hasErrorArr[0].equals("0")) {
//				model.addAttribute("paymentSuccess", OrderStatus.SUCCESSFULL);
//			}  else if (hasErrorArr[0].equals("5")) {
//				model.addAttribute("paymentCancelled", hasErrorArr[0]);
//			} else {
//				hotelService.walletPayHotelOrderCancel(customer, order, OrderStatus.FAILED);
//				hotelService.updateOrderStatus(order.getId(), OrderStatus.FAILED);
//				model.addAttribute("paymentCancelled", hasErrorArr[1]);
//			}
//		}
//        
//		Double amount = Double.parseDouble(parameter[0])/100;
//		
//		model.addAttribute("amount", amount);
//		model.addAttribute("checksum", checksum);
//		model.addAttribute("verifyChecksum", verifiedChecksum);
//		model.addAttribute("responseParameters", responseParameters);
//		
//		return "zaakpay/hotel/response";
//		
//		
//	}

	// private methods

	private void hotelGetBookingDetails(String bookingId) throws MalformedURLException, IOException {

		// Create URL object with the API end-point
		URL urlHotelGetBooking = new URL(
				"http://api.tektravels.com/BookingEngineService_Hotel/HotelService.svc/rest/GetBookingDetail");

		// Open a connection
		HttpURLConnection connectionHotelGetBooking = (HttpURLConnection) urlHotelGetBooking.openConnection();

		StringBuilder responseBodyHotelGetBooking = new StringBuilder();

		onlineHotelService.apiOnlineHotelGetBookingDetails(connectionHotelGetBooking, responseBodyHotelGetBooking,
				bookingId);

		JSONObject jsonObjBlock = new JSONObject(responseBodyHotelGetBooking.toString());
		System.out.println(jsonObjBlock);
		logService.generateLog(jsonObjBlock.toString());
	}

	private void hotelBlockMethod(Model model, String resultIndex, String hotelCode, String hotelName, String noOfRooms,
			Hotel hotel, String categoryId) throws MalformedURLException, IOException {

		List<String> strRooms = new ArrayList<String>();

		for (HotelRoom room : hotel.getHotelRooms()) {
			String hotelRoomDetail = "    {\r\n" + "      \"RoomIndex\": \"" + room.getRoomIndex() + "\",\r\n"
					+ "      \"RoomTypeCode\": \"" + room.getRoomTypeCode() + "\",\r\n" + "      \"RoomTypeName\": \""
					+ room.getRoomTypeName() + "\",\r\n" + "      \"RatePlanCode\": \"" + room.getRatePlanCode()
					+ "\",\r\n" + "      \"BedTypeCode\": null,\r\n" + "      \"SmokingPreference\": 0,\r\n"
					+ "      \"Supplements\": null,\r\n" + "      \"Price\": {\r\n"
					+ "        \"CurrencyCode\": \"INR\",\r\n" + "        \"RoomPrice\": \"" + room.getRoomPrice()
					+ "\",\r\n" + "        \"Tax\": \"" + room.getTax() + "\",\r\n" + "        \"ServiceTax\": \""
					+ room.getServiceTax() + "\",\r\n" + "        \"ExtraGuestCharge\": \"" + room.getExtraGuestCharge()
					+ "\",\r\n" + "        \"ChildCharge\": \"" + room.getChildCharge() + "\",\r\n"
					+ "        \"OtherCharges\": \"" + room.getOtherCharges() + "\",\r\n" + "        \"Discount\": \""
					+ room.getDiscount() + "\",\r\n" + "        \"PublishedPrice\": \"" + room.getPublishedPrice()
					+ "\",\r\n" + "        \"PublishedPriceRoundedOff\": \"" + room.getPublishedPriceRoundedOff()
					+ "\",\r\n" + "        \"OfferedPrice\": \"" + room.getOfferedPrice() + "\",\r\n"
					+ "        \"OfferedPriceRoundedOff\": \"" + room.getOfferedPriceRoundedOff() + "\",\r\n"
					+ "        \"AgentCommission\": \"" + room.getAgentCommission() + "\",\r\n"
					+ "        \"AgentMarkUp\": \"" + room.getAgentMarkUp() + "\",\r\n" + "        \"TDS\": \""
					+ room.getTds() + "\"\r\n" + "      }\r\n" + "    }\r\n";

			strRooms.add(hotelRoomDetail);
		}

		String arrayRoom = strRooms.stream().map(val -> String.valueOf(val)).collect(Collectors.joining(",", "[", "]"));

		// Create URL object with the API end-point
		URL urlHotelBlock = new URL(
				"http://api.tektravels.com/BookingEngineService_Hotel/hotelservice.svc/rest/BlockRoom");

		// Open a connection
		HttpURLConnection connectionHotelBlock = (HttpURLConnection) urlHotelBlock.openConnection();

		StringBuilder responseBodyHotelBlock = new StringBuilder();

		onlineHotelService.apiOnlineHotelBlockRoom(connectionHotelBlock, responseBodyHotelBlock, resultIndex, hotelCode,
				hotelName, noOfRooms, arrayRoom, categoryId);

		JSONObject jsonObjBlock = new JSONObject(responseBodyHotelBlock.toString());
		System.out.println(jsonObjBlock);
		logService.generateLog(jsonObjBlock.toString());

		List<HotelRoom> rooms = new ArrayList<HotelRoom>();

		try {
			JSONObject jsonObj = jsonObjBlock.getJSONObject("BlockRoomResult");

			String starRating = jsonObj.get("StarRating").toString();
			String latitude = jsonObj.get("Latitude").toString();
			String longitude = jsonObj.get("Longitude").toString();
			String addressLine1 = jsonObj.get("AddressLine1").toString();
			String addressLine2 = jsonObj.get("AddressLine2").toString();
			String responseStatus = jsonObj.get("ResponseStatus").toString();
			String blockHotelName = jsonObj.get("HotelName").toString();
			String hotelPolicyDetail = jsonObj.get("HotelPolicyDetail").toString();
			String availabilityType = "";
			String hotelNorms = "";
			try {
				availabilityType = jsonObj.get("AvailabilityType").toString();
				hotelNorms = jsonObj.get("HotelNorms").toString();
			} catch (Exception e) {
				// TODO: handle exception
			}

			JSONArray jsonArrRoom = jsonObj.getJSONArray("HotelRoomsDetails");

			for (int i = 0; i < jsonArrRoom.length(); i++) {
				String sequenceNo = jsonArrRoom.getJSONObject(i).get("SequenceNo").toString();
				Integer roomStatus = Integer.parseInt(jsonArrRoom.getJSONObject(i).get("RoomStatus").toString());
				String roomDescription = jsonArrRoom.getJSONObject(i).get("RoomDescription").toString();
				String lastCancellationDate = jsonArrRoom.getJSONObject(i).get("LastCancellationDate").toString();
				String infoSource = jsonArrRoom.getJSONObject(i).get("InfoSource").toString();
				Integer roomId = Integer.parseInt(jsonArrRoom.getJSONObject(i).get("RoomId").toString());
				Integer roomIndex = Integer.parseInt(jsonArrRoom.getJSONObject(i).get("RoomIndex").toString());
				Integer ratePlan = Integer.parseInt(jsonArrRoom.getJSONObject(i).get("RatePlan").toString());
				String roomTypeName = jsonArrRoom.getJSONObject(i).get("RoomTypeName").toString();
				String roomPromotion = jsonArrRoom.getJSONObject(i).get("RoomPromotion").toString();
				String cancellationPolicy = jsonArrRoom.getJSONObject(i).get("CancellationPolicy").toString();
				String ratePlanCode = jsonArrRoom.getJSONObject(i).get("RatePlanCode").toString();
				String roomTypeCode = jsonArrRoom.getJSONObject(i).get("RoomTypeCode").toString();
				String smokingPreference = jsonArrRoom.getJSONObject(i).get("SmokingPreference").toString();
				String lastVoucherDate = jsonArrRoom.getJSONObject(i).get("LastVoucherDate").toString();
				Integer childCount = Integer.parseInt(jsonArrRoom.getJSONObject(i).get("ChildCount").toString());
				String roomAvailabilityType = jsonArrRoom.getJSONObject(i).get("AvailabilityType").toString();
				String ratePlanName = jsonArrRoom.getJSONObject(i).get("RatePlanName").toString();
				double roomPrice = Double
						.parseDouble(jsonArrRoom.getJSONObject(i).getJSONObject("Price").get("RoomPrice").toString());
				double tax = Double
						.parseDouble(jsonArrRoom.getJSONObject(i).getJSONObject("Price").get("Tax").toString());
				double extraGuestCharge = Double.parseDouble(
						jsonArrRoom.getJSONObject(i).getJSONObject("Price").get("ExtraGuestCharge").toString());
				double childCharge = Double
						.parseDouble(jsonArrRoom.getJSONObject(i).getJSONObject("Price").get("ChildCharge").toString());
				double discount = Double
						.parseDouble(jsonArrRoom.getJSONObject(i).getJSONObject("Price").get("Discount").toString());
				double publishedPrice = Double.parseDouble(
						jsonArrRoom.getJSONObject(i).getJSONObject("Price").get("PublishedPrice").toString());
				double otherCharges = Double.parseDouble(
						jsonArrRoom.getJSONObject(i).getJSONObject("Price").get("OtherCharges").toString());
				double offeredPrice = Double.parseDouble(
						jsonArrRoom.getJSONObject(i).getJSONObject("Price").get("OfferedPrice").toString());
				Integer publishedPriceRoundedOff = Integer.parseInt(
						jsonArrRoom.getJSONObject(i).getJSONObject("Price").get("PublishedPriceRoundedOff").toString());
				Integer offeredPriceRoundedOff = Integer.parseInt(
						jsonArrRoom.getJSONObject(i).getJSONObject("Price").get("OfferedPriceRoundedOff").toString());
				double agentCommission = Double.parseDouble(
						jsonArrRoom.getJSONObject(i).getJSONObject("Price").get("AgentCommission").toString());
				double agentMarkUp = Double
						.parseDouble(jsonArrRoom.getJSONObject(i).getJSONObject("Price").get("AgentMarkUp").toString());
				double serviceTax = Double
						.parseDouble(jsonArrRoom.getJSONObject(i).getJSONObject("Price").get("ServiceTax").toString());
				double tds = Double
						.parseDouble(jsonArrRoom.getJSONObject(i).getJSONObject("Price").get("TDS").toString());
				boolean requireAllPaxDetails = Boolean
						.parseBoolean(jsonArrRoom.getJSONObject(i).get("RequireAllPaxDetails").toString());
				boolean isPassportMandatory = Boolean
						.parseBoolean(jsonArrRoom.getJSONObject(i).get("IsPassportMandatory").toString());
				boolean isPANMandatory = Boolean
						.parseBoolean(jsonArrRoom.getJSONObject(i).get("IsPANMandatory").toString());
				String[] amenities = {};
				String[] amenity = {};
				String[] hotelSupplements = {};
				String[] inclusion = {};
				String[] bedTypes = {};
				List<HotelCancelPolicy> cancelPList = new ArrayList<>();
				List<RoomDayRate> rateList = new ArrayList<>();

				try {
					JSONArray jsonFacil = jsonArrRoom.getJSONObject(i).getJSONArray("Amenities");
					amenities = new String[jsonFacil.length()];
					for (int j = 0; j < jsonFacil.length(); j++) {
						String facil = jsonFacil.getString(j);
						amenities[j] = facil;
					}
				} catch (Exception e) {
				}

				try {
					JSONArray jsonFacil = jsonArrRoom.getJSONObject(i).getJSONArray("Amenity");
					amenity = new String[jsonFacil.length()];
					for (int j = 0; j < jsonFacil.length(); j++) {
						String facil = jsonFacil.getString(j);
						amenity[j] = facil;
					}
				} catch (Exception e) {
				}

				try {
					JSONArray jsonFacil = jsonArrRoom.getJSONObject(i).getJSONArray("HotelSupplements");
					hotelSupplements = new String[jsonFacil.length()];
					for (int j = 0; j < jsonFacil.length(); j++) {
						String facil = jsonFacil.getString(j);
						hotelSupplements[j] = facil;
					}
				} catch (Exception e) {
				}

				try {
					JSONArray jsonFacil = jsonArrRoom.getJSONObject(i).getJSONArray("Inclusion");
					inclusion = new String[jsonFacil.length()];
					for (int j = 0; j < jsonFacil.length(); j++) {
						String facil = jsonFacil.getString(j);
						inclusion[j] = facil;
					}
				} catch (Exception e) {
				}

				try {
					JSONArray jsonFacil = jsonArrRoom.getJSONObject(i).getJSONArray("CancellationPolicies");
					JSONObject cancelObj = new JSONObject();

					for (int j = 0; j < jsonFacil.length(); j++) {
						cancelObj.put("Cancel-" + j, jsonFacil.getJSONObject(j));
						Integer charge = Integer
								.parseInt(cancelObj.getJSONObject("Cancel-" + j).get("Charge").toString());
						Integer chargeType = Integer
								.parseInt(cancelObj.getJSONObject("Cancel-" + j).get("ChargeType").toString());
						String currency = cancelObj.getJSONObject("Cancel-" + j).get("Currency").toString();
						String fromDate = cancelObj.getJSONObject("Cancel-" + j).get("FromDate").toString();
						String toDate = cancelObj.getJSONObject("Cancel-" + j).get("ToDate").toString();
						HotelCancelPolicy cancelPolicy = new HotelCancelPolicy(charge, chargeType, currency, fromDate,
								toDate);

						cancelPList.add(cancelPolicy);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				try {
					JSONArray jsonFacil = jsonArrRoom.getJSONObject(i).getJSONArray("DayRates");
					JSONObject rateObj = new JSONObject();

					for (int j = 0; j < jsonFacil.length(); j++) {
						rateObj.put("Rate-" + j, jsonFacil.getJSONObject(j));
						double amount = Double.parseDouble(rateObj.getJSONObject("Rate-" + j).get("Amount").toString());
						String date = rateObj.getJSONObject("Rate-" + j).get("Date").toString();

						RoomDayRate dayRate = new RoomDayRate(date, amount);

						rateList.add(dayRate);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				HotelRoom room = new HotelRoom(roomTypeCode, roomIndex, roomStatus, roomId, requireAllPaxDetails,
						roomDescription, roomTypeName, ratePlanCode, ratePlan, ratePlanName, infoSource, sequenceNo,
						childCount, roomPromotion, amenities, amenity, smokingPreference, bedTypes, hotelSupplements,
						lastCancellationDate, cancelPList, roomPrice, tax, extraGuestCharge, childCharge, discount,
						roomAvailabilityType, publishedPrice, otherCharges, offeredPrice, publishedPriceRoundedOff,
						offeredPriceRoundedOff, agentCommission, agentMarkUp, serviceTax, tds, lastVoucherDate,
						cancellationPolicy, inclusion, isPassportMandatory, isPANMandatory, rateList);

				for (HotelRoom savedRoom : hotel.getHotelRooms()) {
					if (savedRoom.getRoomIndex() == room.getRoomIndex()) {
						savedRoom = room;
						hotelService.saveRoom(savedRoom);
					}
				}

				rooms.add(room);
			}

			model.addAttribute("roomList", rooms);
			model.addAttribute("starRating", starRating);
			model.addAttribute("latitude", latitude);
			model.addAttribute("longitude", longitude);
			model.addAttribute("addressLine1", addressLine1);
			model.addAttribute("addressLine2", addressLine2);
			model.addAttribute("responseStatus", responseStatus);
			model.addAttribute("availabilityType", availabilityType);
			model.addAttribute("hotelName", blockHotelName);
			model.addAttribute("hotelNorms", hotelNorms);
			model.addAttribute("hotelPolicyDetail", hotelPolicyDetail);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String[] hotelBookMethod(Model model, String resultIndex, String hotelCode, String hotelName,
			String noOfRooms, Hotel hotel, String categoryId) throws MalformedURLException, IOException {

		List<String> strRooms = new ArrayList<String>();
		String[] hasErrorArr = new String[2];

		for (HotelRoom room : hotel.getHotelRooms()) {
			List<String> strGuests = new ArrayList<String>();
			String paxDetails = "";
			for (HotelGuest guest : hotel.getGuests()) {
				if (guest.getHotelRoom().getId() == room.getId()) {
					paxDetails = "{\r\n" + "\"Title\": \"" + guest.getTitle() + "\",\r\n" + "\"FirstName\": \""
							+ guest.getFirstName() + "\",\r\n" + "\"Middlename\": null,\r\n" + "\"LastName\": \""
							+ guest.getLastName() + "\",\r\n" + "\"Phoneno\": \"" + guest.getPhoneNo() + "\",\r\n"
							+ "\"Email\": \"" + guest.getEmail() + "\",\r\n" + "\"PaxType\": " + guest.getPaxType()
							+ ",\r\n" + "\"LeadPassenger\": " + guest.isLeadPassenger() + ",\r\n" + "\"Age\": "
							+ guest.getAge() + ",\r\n" + "\"PassportNo\": null,\r\n"
							+ "\"PassportIssueDate\": \"0001-01-01T00: 00: 00\",\r\n"
							+ "\"PassportExpDate\": \"0001-01-01T00: 00: 00\",\r\n" + "\"PAN\": \"" + guest.getPan()
							+ "\"\r\n" + "}\r\n";

					strGuests.add(paxDetails);
				}
			}

			String arrayGuest = strGuests.stream().map(val -> String.valueOf(val))
					.collect(Collectors.joining(",", "[", "]"));

			String hotelRoomDetail = "    {\r\n" + "      \"RoomIndex\": \"" + room.getRoomIndex() + "\",\r\n"
					+ "      \"RoomTypeCode\": \"" + room.getRoomTypeCode() + "\",\r\n" + "      \"RoomTypeName\": \""
					+ room.getRoomTypeName() + "\",\r\n" + "      \"RatePlanCode\": \"" + room.getRatePlanCode()
					+ "\",\r\n" + "      \"BedTypeCode\": null,\r\n" + "      \"SmokingPreference\": 0,\r\n"
					+ "      \"Supplements\": null,\r\n" + "      \"Price\": {\r\n"
					+ "        \"CurrencyCode\": \"INR\",\r\n" + "        \"RoomPrice\": \"" + room.getRoomPrice()
					+ "\",\r\n" + "        \"Tax\": \"" + room.getTax() + "\",\r\n" + "        \"ServiceTax\": \""
					+ room.getServiceTax() + "\",\r\n" + "        \"ExtraGuestCharge\": \"" + room.getExtraGuestCharge()
					+ "\",\r\n" + "        \"ChildCharge\": \"" + room.getChildCharge() + "\",\r\n"
					+ "        \"OtherCharges\": \"" + room.getOtherCharges() + "\",\r\n" + "        \"Discount\": \""
					+ room.getDiscount() + "\",\r\n" + "        \"PublishedPrice\": \"" + room.getPublishedPrice()
					+ "\",\r\n" + "        \"PublishedPriceRoundedOff\": \"" + room.getPublishedPriceRoundedOff()
					+ "\",\r\n" + "        \"OfferedPrice\": \"" + room.getOfferedPrice() + "\",\r\n"
					+ "        \"OfferedPriceRoundedOff\": \"" + room.getOfferedPriceRoundedOff() + "\",\r\n"
					+ "        \"AgentCommission\": \"" + room.getAgentCommission() + "\",\r\n"
					+ "        \"AgentMarkUp\": \"" + room.getAgentMarkUp() + "\",\r\n" + "        \"TDS\": \""
					+ room.getTds() + "\"\r\n" + "      },\r\n" + "      \"HotelPassenger\": " + arrayGuest + "\r\n"
					+ "    }\r\n";

			strRooms.add(hotelRoomDetail);
		}

		String arrayRoom = strRooms.stream().map(val -> String.valueOf(val)).collect(Collectors.joining(",", "[", "]"));

		// Create URL object with the API end-point
		URL urlHotelBook = new URL("http://api.tektravels.com/BookingEngineService_Hotel/hotelservice.svc/rest/Book");

		// Open a connection
		HttpURLConnection connectionHotelBook = (HttpURLConnection) urlHotelBook.openConnection();

		StringBuilder responseBodyHotelBook = new StringBuilder();

		onlineHotelService.apiOnlineHotelBook(connectionHotelBook, responseBodyHotelBook, resultIndex, hotelCode,
				hotelName, noOfRooms, arrayRoom, categoryId);

		JSONObject jsonObjBook = new JSONObject(responseBodyHotelBook.toString());
		System.out.println(jsonObjBook);
		logService.generateLog(jsonObjBook.toString());

		List<HotelRoom> rooms = new ArrayList<HotelRoom>();

		try {
			JSONObject jsonObj = jsonObjBook.getJSONObject("BookResult");

			String hotelBookingStatus = jsonObj.get("HotelBookingStatus").toString();
			String confirmationNo = jsonObj.get("ConfirmationNo").toString();
			String bookingRefNo = jsonObj.get("BookingRefNo").toString();
			String bookingId = jsonObj.get("BookingId").toString();
			String isPriceChanged = jsonObj.get("IsPriceChanged").toString();
			String isCancellationPolicyChanged = jsonObj.get("IsCancellationPolicyChanged").toString();
			bookingIdMain = bookingId;

			model.addAttribute("hotelBookingStatus", hotelBookingStatus);
			model.addAttribute("confirmationNo", confirmationNo);
			model.addAttribute("bookingRefNo", bookingRefNo);
			model.addAttribute("bookingId", bookingId);
			model.addAttribute("isPriceChanged", isPriceChanged);
			model.addAttribute("isCancellationPolicyChanged", isCancellationPolicyChanged);

			if (Boolean.parseBoolean(isPriceChanged) == true) {
				try {
					JSONArray jsonArr = jsonObj.getJSONArray("HotelRoomsDetails");
					for (int i = 0; i < jsonArr.length(); i++) {
						Integer roomIndex = Integer.parseInt(jsonArr.getJSONObject(i).get("RoomIndex").toString());
						String roomTypeCode = jsonArr.getJSONObject(i).get("RoomTypeCode").toString();
						String roomTypeName = jsonArr.getJSONObject(i).get("RoomTypeName").toString();
						String roomPromotion = jsonArr.getJSONObject(i).get("RoomPromotion").toString();
						String ratePlanCode = jsonArr.getJSONObject(i).get("RatePlanCode").toString();
						String ratePlanName = jsonArr.getJSONObject(i).get("RatePlanName").toString();
						String infoSource = jsonArr.getJSONObject(i).get("InfoSource").toString();
						String sequenceNo = jsonArr.getJSONObject(i).get("SequenceNo").toString();
						String smokingPreference = jsonArr.getJSONObject(i).get("SmokingPreference").toString();
						String supplierSpecificData = jsonArr.getJSONObject(i).get("SupplierSpecificData").toString();
						String lastCancellationDate = jsonArr.getJSONObject(i).get("LastCancellationDate").toString();
						String cancellationPolicy = jsonArr.getJSONObject(i).get("CancellationPolicy").toString();
						double roomPrice = Double.parseDouble(
								jsonArr.getJSONObject(i).getJSONObject("Price").get("RoomPrice").toString());
						double tax = Double
								.parseDouble(jsonArr.getJSONObject(i).getJSONObject("Price").get("Tax").toString());
						double extraGuestCharge = Double.parseDouble(
								jsonArr.getJSONObject(i).getJSONObject("Price").get("ExtraGuestCharge").toString());
						double childCharge = Double.parseDouble(
								jsonArr.getJSONObject(i).getJSONObject("Price").get("ChildCharge").toString());
						double discount = Double.parseDouble(
								jsonArr.getJSONObject(i).getJSONObject("Price").get("Discount").toString());
						double publishedPrice = Double.parseDouble(
								jsonArr.getJSONObject(i).getJSONObject("Price").get("PublishedPrice").toString());
						double otherCharges = Double.parseDouble(
								jsonArr.getJSONObject(i).getJSONObject("Price").get("OtherCharges").toString());
						double offeredPrice = Double.parseDouble(
								jsonArr.getJSONObject(i).getJSONObject("Price").get("OfferedPrice").toString());
						Integer publishedPriceRoundedOff = Integer.parseInt(jsonArr.getJSONObject(i)
								.getJSONObject("Price").get("PublishedPriceRoundedOff").toString());
						Integer offeredPriceRoundedOff = Integer.parseInt(jsonArr.getJSONObject(i)
								.getJSONObject("Price").get("OfferedPriceRoundedOff").toString());
						double agentCommission = Double.parseDouble(
								jsonArr.getJSONObject(i).getJSONObject("Price").get("AgentCommission").toString());
						double agentMarkUp = Double.parseDouble(
								jsonArr.getJSONObject(i).getJSONObject("Price").get("AgentMarkUp").toString());
						double tds = Double
								.parseDouble(jsonArr.getJSONObject(i).getJSONObject("Price").get("TDS").toString());
						String[] amenities = {};
						String[] hotelSupplements = {};
						String[] bedTypes = {};
						List<HotelCancelPolicy> cancelPList = new ArrayList<>();
						List<RoomDayRate> rateList = new ArrayList<>();

						try {
							JSONArray jsonFacil = jsonArr.getJSONObject(i).getJSONArray("Amenities");
							amenities = new String[jsonFacil.length()];
							for (int j = 0; j < jsonFacil.length(); j++) {
								String facil = jsonFacil.getString(j);
								amenities[j] = facil;
							}
						} catch (Exception e) {
						}

						try {
							JSONArray jsonFacil = jsonArr.getJSONObject(i).getJSONArray("BedTypes");
							bedTypes = new String[jsonFacil.length()];
							for (int j = 0; j < jsonFacil.length(); j++) {
								String facil = jsonFacil.getString(j);
								bedTypes[j] = facil;
							}
						} catch (Exception e) {
						}

						try {
							JSONArray jsonFacil = jsonArr.getJSONObject(i).getJSONArray("Supplements");
							hotelSupplements = new String[jsonFacil.length()];
							for (int j = 0; j < jsonFacil.length(); j++) {
								String facil = jsonFacil.getString(j);
								hotelSupplements[j] = facil;
							}
						} catch (Exception e) {
						}

						try {
							JSONArray jsonFacil = jsonArr.getJSONObject(i).getJSONArray("CancellationPolicies");
							JSONObject cancelObj = new JSONObject();

							for (int j = 0; j < jsonFacil.length(); j++) {
								cancelObj.put("Cancel-" + j, jsonFacil.getJSONObject(j));
								Integer charge = Integer
										.parseInt(cancelObj.getJSONObject("Cancel-" + j).get("Charge").toString());
								Integer chargeType = Integer
										.parseInt(cancelObj.getJSONObject("Cancel-" + j).get("ChargeType").toString());
								String currency = cancelObj.getJSONObject("Cancel-" + j).get("Currency").toString();
								String fromDate = cancelObj.getJSONObject("Cancel-" + j).get("FromDate").toString();
								String toDate = cancelObj.getJSONObject("Cancel-" + j).get("ToDate").toString();
								HotelCancelPolicy cancelPolicy = new HotelCancelPolicy(charge, chargeType, currency,
										fromDate, toDate);

								cancelPList.add(cancelPolicy);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

						try {
							JSONArray jsonFacil = jsonArr.getJSONObject(i).getJSONArray("DayRates");
							JSONObject rateObj = new JSONObject();

							for (int j = 0; j < jsonFacil.length(); j++) {
								rateObj.put("Rate-" + j, jsonFacil.getJSONObject(j));
								double amount = Double
										.parseDouble(rateObj.getJSONObject("Rate-" + j).get("Amount").toString());
								String date = rateObj.getJSONObject("Rate-" + j).get("Date").toString();

								RoomDayRate dayRate = new RoomDayRate(date, amount);

								rateList.add(dayRate);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

						HotelRoom room = new HotelRoom(roomTypeCode, roomIndex, 0, roomIndex, false,
								supplierSpecificData, roomTypeName, ratePlanCode, 0, ratePlanName, infoSource,
								sequenceNo, 0, roomPromotion, amenities, null, smokingPreference, bedTypes,
								hotelSupplements, lastCancellationDate, cancelPList, roomPrice, tax, extraGuestCharge,
								childCharge, discount, hotelBookingStatus, publishedPrice, otherCharges, offeredPrice,
								publishedPriceRoundedOff, offeredPriceRoundedOff, agentCommission, agentMarkUp, 0, tds,
								"none", cancellationPolicy, null, false, false, rateList);

						rooms.add(room);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			model.addAttribute("roomList", rooms);

			JSONObject jsonObjTicketResponseError = jsonObj.getJSONObject("Error");
			hasErrorArr[0] = jsonObjTicketResponseError.get("ErrorCode").toString();
			hasErrorArr[1] = jsonObjTicketResponseError.get("ErrorMessage").toString();

		} catch (Exception e) {
			JSONObject jsonObjTicketResponseError = jsonObjBook.getJSONObject("BookResult").getJSONObject("Error");
			hasErrorArr[0] = jsonObjTicketResponseError.get("ErrorCode").toString();
			hasErrorArr[1] = jsonObjTicketResponseError.get("ErrorMessage").toString();

			e.printStackTrace();
		}

		return hasErrorArr;
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

	private long noOfNightsMethod(Date checkInDate, Date checkOutDate) {
		// Calculate the difference in milliseconds
		long diffInMillis = checkOutDate.getTime() - checkInDate.getTime();

		// Convert milliseconds to days
		long daysBetween = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);

		return daysBetween;
	}

	private void hotelInfoMethod(Model model, String resultIndex, String hotelCode, String categoryId)
			throws MalformedURLException, IOException {
		// Create URL object with the API end-point
		URL urlHotelInfo = new URL(
				"http://api.tektravels.com/BookingEngineService_Hotel/hotelservice.svc/rest/GetHotelInfo");

		// Open a connection
		HttpURLConnection connectionHotelInfo = (HttpURLConnection) urlHotelInfo.openConnection();

		StringBuilder responseBodyHotelInfo = new StringBuilder();

//		onlineHotelService.apiOnlineHotelInfo(connectionHotelInfo, responseBodyHotelInfo, resultIndex, hotelCode,
//				categoryId);

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

			try {
				model.addAttribute("hotelCode", jsonObj.get("HotelCode").toString());
			} catch (Exception e) {
				// TODO: handle exception
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void hotelRoomsMethod(Model model, String resultIndex, String hotelCode)
			throws MalformedURLException, IOException {
		// Create URL object with the API end-point
		URL urlHotelRoom = new URL(
				"http://api.tektravels.com/BookingEngineService_Hotel/hotelservice.svc/rest/GetHotelRoom");

		// Open a connection
		HttpURLConnection connectionHotelRoom = (HttpURLConnection) urlHotelRoom.openConnection();

		StringBuilder responseBodyHotelRoom = new StringBuilder();

		onlineHotelService.apiOnlineHotelRoom(connectionHotelRoom, responseBodyHotelRoom, resultIndex, hotelCode);

		JSONObject jsonObjSearch = new JSONObject(responseBodyHotelRoom.toString());
		System.out.println(jsonObjSearch);
		logService.generateLog(jsonObjSearch.toString());

		try {
			JSONArray jsonArr = jsonObjSearch.getJSONObject("GetHotelRoomResult").getJSONArray("HotelRoomsDetails");
			JSONObject hotelRoomsObj = new JSONObject();

			for (int i = 0; i < jsonArr.length(); i++) {
				hotelRoomsObj.put("RoomDetail-" + i, jsonArr.getJSONObject(i));

				String availabilityType = hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("AvailabilityType")
						.toString();
				Integer childCount = Integer
						.parseInt(hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("ChildCount").toString());
				boolean requireAllPaxDetails = Boolean.parseBoolean(
						hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("RequireAllPaxDetails").toString());
				boolean isPassportMandatory = Boolean.parseBoolean(
						hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("IsPassportMandatory").toString());
				boolean isPANMandatory = Boolean
						.parseBoolean(hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("IsPANMandatory").toString());
				Integer roomId = Integer
						.parseInt(hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("RoomId").toString());
				Integer roomStatus = Integer
						.parseInt(hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("RoomStatus").toString());
				Integer roomIndex = Integer
						.parseInt(hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("RoomIndex").toString());
				String roomTypeCode = hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("RoomTypeCode").toString();
				String roomDescription = hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("RoomDescription")
						.toString();
				String roomTypeName = hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("RoomTypeName").toString();
				String ratePlanCode = hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("RatePlanCode").toString();
				Integer ratePlan = Integer
						.parseInt(hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("RatePlan").toString());
				String ratePlanName = hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("RatePlanName").toString();
				String infoSource = hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("InfoSource").toString();
				String sequenceNo = hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("SequenceNo").toString();
				double roomPrice = Double.parseDouble(hotelRoomsObj.getJSONObject("RoomDetail-" + i)
						.getJSONObject("Price").get("RoomPrice").toString());
				double tax = Double.parseDouble(
						hotelRoomsObj.getJSONObject("RoomDetail-" + i).getJSONObject("Price").get("Tax").toString());
				double extraGuestCharge = Double.parseDouble(hotelRoomsObj.getJSONObject("RoomDetail-" + i)
						.getJSONObject("Price").get("ExtraGuestCharge").toString());
				double childCharge = Double.parseDouble(hotelRoomsObj.getJSONObject("RoomDetail-" + i)
						.getJSONObject("Price").get("ChildCharge").toString());
				double discount = Double.parseDouble(hotelRoomsObj.getJSONObject("RoomDetail-" + i)
						.getJSONObject("Price").get("Discount").toString());
				double publishedPrice = Double.parseDouble(hotelRoomsObj.getJSONObject("RoomDetail-" + i)
						.getJSONObject("Price").get("PublishedPrice").toString());
				double otherCharges = Double.parseDouble(hotelRoomsObj.getJSONObject("RoomDetail-" + i)
						.getJSONObject("Price").get("OtherCharges").toString());
				double offeredPrice = Double.parseDouble(hotelRoomsObj.getJSONObject("RoomDetail-" + i)
						.getJSONObject("Price").get("OfferedPrice").toString());
				Integer publishedPriceRoundedOff = Integer.parseInt(hotelRoomsObj.getJSONObject("RoomDetail-" + i)
						.getJSONObject("Price").get("PublishedPriceRoundedOff").toString());
				Integer offeredPriceRoundedOff = Integer.parseInt(hotelRoomsObj.getJSONObject("RoomDetail-" + i)
						.getJSONObject("Price").get("OfferedPriceRoundedOff").toString());
				double agentCommission = Double.parseDouble(hotelRoomsObj.getJSONObject("RoomDetail-" + i)
						.getJSONObject("Price").get("AgentCommission").toString());
				double agentMarkUp = Double.parseDouble(hotelRoomsObj.getJSONObject("RoomDetail-" + i)
						.getJSONObject("Price").get("AgentMarkUp").toString());
				double serviceTax = Double.parseDouble(hotelRoomsObj.getJSONObject("RoomDetail-" + i)
						.getJSONObject("Price").get("ServiceTax").toString());
				double tds = Double.parseDouble(
						hotelRoomsObj.getJSONObject("RoomDetail-" + i).getJSONObject("Price").get("TDS").toString());
				String roomPromotion = hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("RoomPromotion").toString();
				String smokingPreference = hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("SmokingPreference")
						.toString();
				String lastVoucherDate = hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("LastVoucherDate")
						.toString();
				String cancellationPolicy = hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("CancellationPolicy")
						.toString();
				String lastCancellationDate = hotelRoomsObj.getJSONObject("RoomDetail-" + i).get("LastCancellationDate")
						.toString();
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
					JSONArray jsonFacil = hotelRoomsObj.getJSONObject("RoomDetail-" + i)
							.getJSONArray("HotelSupplements");
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
					JSONArray jsonFacil = hotelRoomsObj.getJSONObject("RoomDetail-" + i)
							.getJSONArray("CancellationPolicies");
					JSONObject cancelObj = new JSONObject();

					for (int j = 0; j < jsonFacil.length(); j++) {
						cancelObj.put("Cancel-" + j, jsonFacil.getJSONObject(j));
						Integer charge = Integer
								.parseInt(cancelObj.getJSONObject("Cancel-" + j).get("Charge").toString());
						Integer chargeType = Integer
								.parseInt(cancelObj.getJSONObject("Cancel-" + j).get("ChargeType").toString());
						String currency = cancelObj.getJSONObject("Cancel-" + j).get("Currency").toString();
						String fromDate = cancelObj.getJSONObject("Cancel-" + j).get("FromDate").toString();
						String toDate = cancelObj.getJSONObject("Cancel-" + j).get("ToDate").toString();
						HotelCancelPolicy cancelPolicy = new HotelCancelPolicy(charge, chargeType, currency, fromDate,
								toDate);

//    	        		System.out.println("Cancel Policy: " + charge + " ... " + fromDate);

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

//    	        		System.out.println("Day Rate: " + amount + " ... " + date);

						RoomDayRate dayRate = new RoomDayRate(date, amount);

						rateList.add(dayRate);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				HotelRoom room = new HotelRoom(roomTypeCode, roomIndex, roomStatus, roomId, requireAllPaxDetails,
						roomDescription, roomTypeName, ratePlanCode, ratePlan, ratePlanName, infoSource, sequenceNo,
						childCount, roomPromotion, amenities, amenity, smokingPreference, bedTypes, hotelSupplements,
						lastCancellationDate, cancelPList, roomPrice, tax, extraGuestCharge, childCharge, discount,
						availabilityType, publishedPrice, otherCharges, offeredPrice, publishedPriceRoundedOff,
						offeredPriceRoundedOff, agentCommission, agentMarkUp, serviceTax, tds, lastVoucherDate,
						cancellationPolicy, inclusion, isPassportMandatory, isPANMandatory, rateList);

				hotelRooms.add(room);
			}

			model.addAttribute("hotelRoomList", hotelRooms);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
