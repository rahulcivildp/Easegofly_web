package com.easygofly.api.bus;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.easygofly.api.LogService;
import com.easygofly.api.customer.CustomerRepository;
import com.easygofly.api.customer.CustomerService;
import com.easygofly.api.setting.SettingService;
import com.easygofly.api.wallet.TotalTransactionService;
import com.easygofly.api.wallet.TransactionService;
import com.easygofly.entity.Bus;
import com.easygofly.entity.BusCancelPolicy;
import com.easygofly.entity.BusHistory;
import com.easygofly.entity.BusPassenger;
import com.easygofly.entity.BusPointDetails;
import com.easygofly.entity.BusSeat;
import com.easygofly.entity.Customer;
import com.easygofly.entity.TBObusCity;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class BusRestController {

	@Autowired private BusCityRepository busCityRepo;
	@Autowired private BusService busService;
//	@Autowired private BusSaveHelper busSaveHelper;
	@Autowired private CustomerRepository customerRepository;
	@Autowired private CustomerService customerService;
	@Autowired private OnlineBusService onlineBusService;
	@Autowired private LogService logService;
	@Autowired private TransactionService transactionService;
	@Autowired private TotalTransactionService totalTransactionService;
	@Autowired private SettingService settingService;
	

	@PostMapping("/bus/search")
	public String saveSearchBus(HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException {

		SearchBusRequest fareRuleQuote = new ObjectMapper().readValue(request.getInputStream(), SearchBusRequest.class);
		Customer customer = customerRepository.findById(fareRuleQuote.cust_id).get();
		
		TBObusCity cityOne = busCityRepo.getCityByCityName(fareRuleQuote.origin);
		TBObusCity cityTwo = busCityRepo.getCityByCityName(fareRuleQuote.destination);
	    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	    Date startDate = dateFormat.parse(fareRuleQuote.date);
		StringBuilder responseBody = new StringBuilder();
		@SuppressWarnings("unused")
		String errorCode = "";
		@SuppressWarnings("unused")
		String errorMessage = "";
		List<Bus> buses = new ArrayList<>();
		
	    if (customer != null) {	
			BusHistory newHistory = new BusHistory();

			newHistory.setDeptDate(startDate);
			newHistory.setCityIdOne(cityOne.getCityId().toString());
			newHistory.setCityIdTwo(cityTwo.getCityId().toString());
			newHistory.setCustomer(customer);
			
			busService.saveBusHistory(newHistory, customer);
			
			responseBody = onlineBusService.apiOnlineSearchBus(cityOne.getCityId().toString(), cityTwo.getCityId().toString(), startDate);
		} 

        JSONObject jsonObjSearch = new JSONObject(responseBody.toString());
        System.out.println(jsonObjSearch);
        logService.generateLog(jsonObjSearch.toString());
        try {
			JSONArray jsonArrays = jsonObjSearch.getJSONObject("BusSearchResult").getJSONArray("BusResults");
			JSONObject mainObj = new JSONObject();
			
			onlineBusService.traceId = jsonObjSearch.getJSONObject("BusSearchResult").get("TraceId").toString();
			String destination = jsonObjSearch.getJSONObject("BusSearchResult").get("Destination").toString();
			String origin = jsonObjSearch.getJSONObject("BusSearchResult").get("Origin").toString();
			
			for (int i = 0; i < jsonArrays.length(); i++) {
			    mainObj.put("Bus-" + i, jsonArrays.getJSONObject(i));
			    
			    List<BusPointDetails> pointList = new ArrayList<BusPointDetails>();
			    List<BusCancelPolicy> cancellationPolicieList = new ArrayList<BusCancelPolicy>();
			    
				
				Integer resultIndex = Integer.parseInt(mainObj.getJSONObject("Bus-" + i).get("ResultIndex").toString());
				Integer availableSeats = Integer.parseInt(mainObj.getJSONObject("Bus-" + i).get("AvailableSeats").toString());
				Integer maxSeatsPerTicket = Integer.parseInt(mainObj.getJSONObject("Bus-" + i).get("MaxSeatsPerTicket").toString());
				Integer operatorId = Integer.parseInt(mainObj.getJSONObject("Bus-" + i).get("OperatorId").toString());
				String arrivalTime = mainObj.getJSONObject("Bus-" + i).get("ArrivalTime").toString();
				String departureTime = mainObj.getJSONObject("Bus-" + i).get("DepartureTime").toString();
				String routeId = mainObj.getJSONObject("Bus-" + i).get("RouteId").toString();
				String busType = mainObj.getJSONObject("Bus-" + i).get("BusType").toString();
				String serviceName = mainObj.getJSONObject("Bus-" + i).get("ServiceName").toString();
				String travelName = mainObj.getJSONObject("Bus-" + i).get("TravelName").toString();
				boolean idProofRequired = Boolean.parseBoolean(mainObj.getJSONObject("Bus-" + i).get("IdProofRequired").toString());
				boolean liveTrackingAvailable = Boolean.parseBoolean(mainObj.getJSONObject("Bus-" + i).get("LiveTrackingAvailable").toString());
				boolean isDropPointMandatory = Boolean.parseBoolean(mainObj.getJSONObject("Bus-" + i).get("IsDropPointMandatory").toString());
				boolean mTicketEnabled = Boolean.parseBoolean(mainObj.getJSONObject("Bus-" + i).get("MTicketEnabled").toString());
				boolean partialCancellationAllowed = Boolean.parseBoolean(mainObj.getJSONObject("Bus-" + i).get("PartialCancellationAllowed").toString());
				
				double basePrice = Double.parseDouble(mainObj.getJSONObject("Bus-" + i).getJSONObject("BusPrice").get("BasePrice").toString());
				double tax = Double.parseDouble(mainObj.getJSONObject("Bus-" + i).getJSONObject("BusPrice").get("Tax").toString());
				double otherCharges = Double.parseDouble(mainObj.getJSONObject("Bus-" + i).getJSONObject("BusPrice").get("OtherCharges").toString());
				double discount = Double.parseDouble(mainObj.getJSONObject("Bus-" + i).getJSONObject("BusPrice").get("Discount").toString());
				double publishedPrice = Double.parseDouble(mainObj.getJSONObject("Bus-" + i).getJSONObject("BusPrice").get("PublishedPrice").toString());
				Integer publishedPriceRoundedOff = Integer.parseInt(mainObj.getJSONObject("Bus-" + i).getJSONObject("BusPrice").get("PublishedPriceRoundedOff").toString());
				double offeredPrice = Double.parseDouble(mainObj.getJSONObject("Bus-" + i).getJSONObject("BusPrice").get("OfferedPrice").toString());
				Integer offeredPriceRoundedOff = Integer.parseInt(mainObj.getJSONObject("Bus-" + i).getJSONObject("BusPrice").get("OfferedPriceRoundedOff").toString());
				double agentCommission = Double.parseDouble(mainObj.getJSONObject("Bus-" + i).getJSONObject("BusPrice").get("AgentCommission").toString());
				double agentMarkUp = Double.parseDouble(mainObj.getJSONObject("Bus-" + i).getJSONObject("BusPrice").get("AgentMarkUp").toString());
				double tDS = Double.parseDouble(mainObj.getJSONObject("Bus-" + i).getJSONObject("BusPrice").get("TDS").toString());
				double cGSTAmount = Double.parseDouble(mainObj.getJSONObject("Bus-" + i).getJSONObject("BusPrice").getJSONObject("GST").get("CGSTAmount").toString());
				double cGSTRate = Double.parseDouble(mainObj.getJSONObject("Bus-" + i).getJSONObject("BusPrice").getJSONObject("GST").get("CGSTRate").toString());
				double cessAmount = Double.parseDouble(mainObj.getJSONObject("Bus-" + i).getJSONObject("BusPrice").getJSONObject("GST").get("CessAmount").toString());
				double cessRate = Double.parseDouble(mainObj.getJSONObject("Bus-" + i).getJSONObject("BusPrice").getJSONObject("GST").get("CessRate").toString());
				double iGSTAmount = Double.parseDouble(mainObj.getJSONObject("Bus-" + i).getJSONObject("BusPrice").getJSONObject("GST").get("IGSTAmount").toString());
				double iGSTRate = Double.parseDouble(mainObj.getJSONObject("Bus-" + i).getJSONObject("BusPrice").getJSONObject("GST").get("IGSTRate").toString());
				double sGSTAmount = Double.parseDouble(mainObj.getJSONObject("Bus-" + i).getJSONObject("BusPrice").getJSONObject("GST").get("SGSTAmount").toString());
				double sGSTRate = Double.parseDouble(mainObj.getJSONObject("Bus-" + i).getJSONObject("BusPrice").getJSONObject("GST").get("SGSTRate").toString());
				double taxableAmount = Double.parseDouble(mainObj.getJSONObject("Bus-" + i).getJSONObject("BusPrice").getJSONObject("GST").get("TaxableAmount").toString());
				
				try {
				    JSONArray jsonArrayBoardingPoints = mainObj.getJSONObject("Bus-" + i).getJSONArray("BoardingPointsDetails");
				    
					for (int j = 0; j < jsonArrayBoardingPoints.length(); j++) {

						Integer cityPointIndex = Integer.parseInt(jsonArrayBoardingPoints.getJSONObject(j).get("CityPointIndex").toString());
						String cityPointLocation = jsonArrayBoardingPoints.getJSONObject(j).get("CityPointLocation").toString();
						String cityPointName = jsonArrayBoardingPoints.getJSONObject(j).get("CityPointName").toString();
						String cityPointTime = jsonArrayBoardingPoints.getJSONObject(j).get("CityPointTime").toString();
						
						BusPointDetails newBording = new BusPointDetails(cityPointIndex, cityPointLocation, cityPointName, cityPointTime, "Boarding");
						pointList.add(newBording);
					}
					
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				try {
				    JSONArray jsonArrayDroppingPoints = mainObj.getJSONObject("Bus-" + i).getJSONArray("DroppingPointsDetails");
				    
					for (int j = 0; j < jsonArrayDroppingPoints.length(); j++) {
						
						Integer cityPointIndex = Integer.parseInt(jsonArrayDroppingPoints.getJSONObject(j).get("CityPointIndex").toString());
						String cityPointLocation = jsonArrayDroppingPoints.getJSONObject(j).get("CityPointLocation").toString();
						String cityPointName = jsonArrayDroppingPoints.getJSONObject(j).get("CityPointName").toString();
						String cityPointTime = jsonArrayDroppingPoints.getJSONObject(j).get("CityPointTime").toString();
						
						BusPointDetails newDropping = new BusPointDetails(cityPointIndex, cityPointLocation, cityPointName, cityPointTime, "Dropping");
						pointList.add(newDropping);
					
					} 
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				
				
				try {
				    JSONArray jsonArrayCancellationPolicies = mainObj.getJSONObject("Bus-" + i).getJSONArray("CancellationPolicies");
				    
					for (int j = 0; j < jsonArrayCancellationPolicies.length(); j++) {
						
						double cancellationCharge = Double.parseDouble(jsonArrayCancellationPolicies.getJSONObject(j).get("CancellationCharge").toString());
						Integer cancellationChargeType = Integer.parseInt(jsonArrayCancellationPolicies.getJSONObject(j).get("CancellationChargeType").toString());
						String policyString = jsonArrayCancellationPolicies.getJSONObject(j).get("PolicyString").toString();
						String timeBeforeDept = jsonArrayCancellationPolicies.getJSONObject(j).get("TimeBeforeDept").toString();
						String fromDate = jsonArrayCancellationPolicies.getJSONObject(j).get("FromDate").toString();
						String toDate = jsonArrayCancellationPolicies.getJSONObject(j).get("ToDate").toString();
						
						BusCancelPolicy newCancel = new BusCancelPolicy(cancellationCharge, cancellationChargeType, policyString, timeBeforeDept, fromDate, toDate);
						cancellationPolicieList.add(newCancel);
					}
					
				} catch (Exception e) {
			
					e.printStackTrace();
				}

				Bus newBus = new Bus(resultIndex, arrivalTime, departureTime, routeId, busType, serviceName, travelName, "INR", idProofRequired, isDropPointMandatory, 
						liveTrackingAvailable, mTicketEnabled, partialCancellationAllowed, maxSeatsPerTicket, operatorId, tax, discount, publishedPrice, otherCharges, offeredPrice, 
						publishedPriceRoundedOff, offeredPriceRoundedOff, agentCommission, agentMarkUp, basePrice, tDS, cGSTAmount, cGSTRate, cessAmount, cessRate, iGSTAmount, iGSTRate, sGSTAmount, sGSTRate, 
						taxableAmount, availableSeats, null, cancellationPolicieList, pointList);
				newBus.setTraceId(jsonObjSearch.getJSONObject("BusSearchResult").get("TraceId").toString());
				newBus.setOrigin(origin);
				newBus.setDestination(destination);
				
				buses.add(newBus);
			}
			
		} catch (Exception e) {
			JSONObject jsonObj = jsonObjSearch.getJSONObject("BusSearchResult").getJSONObject("Error");
			errorCode = jsonObj.get("ErrorCode").toString();
			errorMessage = jsonObj.get("ErrorMessage").toString();
			
			e.printStackTrace();
		}

    	List<String> strBuses = new ArrayList<>();
    	
        for (Bus bus : buses) {
        	List<String> cancels = new ArrayList<>();
        	List<String> points = new ArrayList<>();
        	
        	for (BusCancelPolicy busCancelPolicy : bus.getBusCancelPolicies()) {
				String cancel = "{\r\n"
						+ "  \"cancellationCharge\": " + busCancelPolicy.getCancellationCharge() + ",\r\n"
						+ "  \"cancellationChargeType\": " + busCancelPolicy.getCancellationChargeType() + ",\r\n"
						+ "  \"policyString\": \"" + busCancelPolicy.getPolicyString()+ "\",\r\n"
						+ "  \"timeBeforeDept\": \"" + busCancelPolicy.getTimeBeforeDept()+ "\",\r\n"
						+ "  \"fromDate\": \"" + busCancelPolicy.getFromDate() + "\",\r\n"
						+ "  \"toDate\": \"" + busCancelPolicy.getToDate() + "\"\r\n"
						+ "}";
				
				cancels.add(cancel);
			}
        	
        	for (BusPointDetails busPointDetails : bus.getPointsDetails()) {
				String point = "{\r\n"
						+ "  \"cityPointIndex\": " + busPointDetails.getCityPointIndex() + ",\r\n"
						+ "  \"cityPointLocation\": \"cityPointLocation\",\r\n"
						+ "  \"cityPointName\": \"cityPointName\",\r\n"
						+ "  \"cityPointTime\": \"cityPointTime\",\r\n"
						+ "  \"pointType\": \"pointType\"\r\n"
						+ "}";
				
				points.add(point);
			}
        	

           	String arrayCancelList = cancels.stream().map(val -> String.valueOf(val)).collect(Collectors.joining(",", "[", "]"));
           	String arrayPointList = points.stream().map(val -> String.valueOf(val)).collect(Collectors.joining(",", "[", "]"));
        	
			String busBody = "{\r\n"
					+ "  \"resultIndex\": " + bus.getResultIndex() + ",\r\n"
					+ "  \"arrivalTime\": \"" + bus.getArrivalTime() + "\",\r\n"
					+ "  \"departureTime\": \"" + bus.getDepartureTime() + "\",\r\n"
					+ "  \"routeId\": \"" + bus.getRouteId() + "\",\r\n"
					+ "  \"busType\": \"" + bus.getBusType() + "\",\r\n"
					+ "  \"serviceName\": \"" + bus.getServiceName() + "\",\r\n"
					+ "  \"travelName\": \"" + bus.getTravelName() + "\",\r\n"
					+ "  \"currencyCode\": \"" + bus.getCurrencyCode() + "\",\r\n"
					+ "  \"idProofRequired\": " + bus.isIdProofRequired() + ",\r\n"
					+ "  \"isDropPointMandatory\": " + bus.isDropPointMandatory() + ",\r\n"
					+ "  \"liveTrackingAvailable\": " + bus.isLiveTrackingAvailable() + ",\r\n"
					+ "  \"mTicketEnabled\": " + bus.ismTicketEnabled() + ",\r\n"
					+ "  \"partialCancellationAllowed\": " + bus.isPartialCancellationAllowed() + ",\r\n"
					+ "  \"maxSeatsPerTicket\": " + bus.getMaxSeatsPerTicket() +",\r\n"
					+ "  \"operatorId\": " + bus.getOperatorId() + ",\r\n"
					+ "  \"tax\": " + bus.getTax() + ",\r\n"
					+ "  \"discount\": " + bus.getDiscount() + ",\r\n"
					+ "  \"publishedPrice\": " + bus.getPublishedPrice() + ",\r\n"
					+ "  \"otherCharges\": " + bus.getOtherCharges() + ",\r\n"
					+ "  \"offeredPrice\": " + bus.getOfferedPrice() + ",\r\n"
					+ "  \"publishedPriceRoundedOff\": " + bus.getPublishedPriceRoundedOff() + ",\r\n"
					+ "  \"offeredPriceRoundedOff\": " + bus.getOfferedPriceRoundedOff() + ",\r\n"
					+ "  \"agentCommission\": " + bus.getAgentCommission() + ",\r\n"
					+ "  \"agentMarkUp\": " + bus.getAgentMarkUp() + ",\r\n"
					+ "  \"basePrice\": " + bus.getBasePrice() + ",\r\n"
					+ "  \"tds\": " + bus.getTds() + ",\r\n"
					+ "  \"cGSTAmount\": " + bus.getcGSTAmount() + ",\r\n"
					+ "  \"cGSTRate\": " + bus.getcGSTRate() + ",\r\n"
					+ "  \"cessAmount\": " + bus.getCessAmount() + ",\r\n"
					+ "  \"cessRate\": " + bus.getCessRate() + ",\r\n"
					+ "  \"iGSTAmount\": " + bus.getiGSTAmount() + ",\r\n"
					+ "  \"iGSTRate\": " + bus.getiGSTRate() + ",\r\n"
					+ "  \"sGSTAmount\": " + bus.getsGSTAmount() + ",\r\n"
					+ "  \"sGSTRate\": " + bus.getsGSTRate() + ",\r\n"
					+ "  \"taxableAmount\": " + bus.getTaxableAmount() + ",\r\n"
					+ "  \"customer\": " + customer.getId() + ",\r\n"
					+ "  \"availableSeats\": " + bus.getAvailableSeats() + ",\r\n"
					+ "  \"busCancelPolicies\": " + arrayCancelList + ",\r\n"
					+ "  \"pointsDetails\": " + arrayPointList + "\r\n"
					+ "}";
			
			strBuses.add(busBody);
		}
        
       	String arrayBusList = strBuses.stream().map(val -> String.valueOf(val)).collect(Collectors.joining(",", "[", "]"));

        String responseBodyStr = "{"
        		+ "\"code\": 0, "
        		+ "\"msg\": \"List of Bus Search Result.\", "
        		+ "\"data\": " + arrayBusList + ""
        		+ "}";

		return responseBodyStr;
		
	}

	
	@GetMapping("/bus/city-list")
	public String getCityList(HttpServletResponse response) {
		response.setContentType("application/json");
    	Iterable<TBObusCity> cities = busCityRepo.findAll();
    	List<String> strCity = new ArrayList<>();
    	
    	for (TBObusCity city : cities) {
    		
			String cityBody = "{\r\n" 
							+ "  \"id\": " + city.getId() + ",\r\n" 
							+ "  \"cityId\": " + city.getCityId() + ",\r\n" 
							+ "  \"cityName\": \"" + city.getCityName() + "\"\r\n" 
							+ "}";
			
			strCity.add(cityBody);
		}
    	
       	String arrayBusList = strCity.stream().map(val -> String.valueOf(val)).collect(Collectors.joining(",", "[", "]"));

        String responseBodyStr = "{"
        		+ "\"code\": 0, "
        		+ "\"msg\": \"List of City.\", "
        		+ "\"data\": " + arrayBusList + ""
        		+ "}";
        
		return responseBodyStr;
	}

	
	
//	@PostMapping("/save_bus_pax")
//	public void savePax(@Param("title") String title, @Param("fName") String fName, @Param("lName") String lName, @Param("email") String email, @Param("phoneNo") String phoneNo, 
//			@Param("age") Integer age, @Param("gender") Integer gender, @Param("pan") String pan, @Param("bus_id") Integer bus_id, @Param("cust_id") Integer cust_id, @Param("seat_id") Integer seat_id,
//			@Param("address") String address) {
//	    Bus savedBus = busService.findByIdBus(bus_id);
//		BusSeat savedSeat = busService.findByIdSeat(seat_id);
//	
//		Bus bus = busSaveHelper.setPax(savedBus, title, fName, lName, phoneNo, email, pan, "Pan no", gender, age, savedSeat.getId(), false, address);
//		Customer customer = customerRepository.findById(cust_id).get();
//		
//		System.out.println(bus.getTravelName());
//		busService.saveBus(bus, customer);
//	}
//	
//	@PostMapping("/modify_pax")
//	public void modifyPax(@Param("guest_id") Integer guest_id, @Param("title") String title, @Param("fName") String fName, @Param("lName") String lName, 
//			@Param("email") String email, @Param("phoneNo") String phoneNo, @Param("age") Integer age, @Param("pan") String pan, @Param("address") String address, 
//			@Param("gender") Integer gender, @Param("seat_id") Integer seat_id) {
//	    BusPassenger savedGuest = busService.findByIdPax(guest_id);
//		BusSeat savedSeat = busService.findByIdSeat(seat_id);
//	    savedGuest.setTitle(title);
//	    savedGuest.setFirstName(fName);
//	    savedGuest.setLastName(lName);
//	    savedGuest.setEmail(email);
//	    savedGuest.setPhoneNo(phoneNo);
//	    savedGuest.setAge(age);
//	    savedGuest.setIdNumber(pan);
//	    savedGuest.setAddress(address);
//	    savedGuest.setGender(gender);
//	    savedGuest.setSeatId(savedSeat.getId());
//	    
//	    BusPassenger saveGuest = busService.savePax(savedGuest);
//	    	System.out.println(saveGuest.getEmail());
//	}
//	
//	@PostMapping("/show_bus_pax")
//	public List<String> showPax(@Param("bus_id") Integer bus_id) {
//		List<String> stringList = new ArrayList<>();
//		Bus savedBus = busService.findByIdBus(bus_id);
//	
//		try {
//			for (BusPassenger newPax : savedBus.getBusPassengers()) {
//				try {
//					BusSeat savedSeat = busService.findByIdSeat(newPax.getSeatId());
//	
//					String str =  newPax.getId() + "-" + newPax.getTitle() + "-" + newPax.getFirstName() + "-" + newPax.getLastName() + "-" + newPax.getAge() + "-" + newPax.getIdNumber() 
//					+ "-" + newPax.getEmail() + "-" + newPax.getPhoneNo() + "-" + newPax.getGender() + "-" + newPax.getAddress() + "-" + savedSeat.getSeatName() + "-" + savedSeat.getId() 
//					+ "-" + savedSeat.getSeatFare();
//					
//					stringList.add(str);
//					
//				} catch (Exception e) {
//					newPax.setBus(null);
//					BusPassenger savedPax = busService.savePax(newPax);
//					busService.deletePax(savedPax.getId());
//				}
//			}
//			
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//		
//		
//		return stringList;
//	}
//
//	@PostMapping("/delete_bus_pax")
//	public void deletePax(@Param("pax_id") Integer pax_id) {
//		busService.deletePax(pax_id);
//	}
//	
//	@PostMapping("/show_bus_seat_rest")
//	public List<String> showSeat(@Param("bus_id") Integer bus_id) {
//		List<String> stringList = new ArrayList<>();
//		Bus savedBus = busService.findByIdBus(bus_id);
//	
//		for (BusSeat seat : savedBus.getBusSeats()) {
//
//			String str =  seat.getId() + "-" + seat.getSeatIndex() + "-" + seat.getSeatName() + "-" + seat.getSeatType() + "-" + seat.getSeatFare();
//			stringList.add(str);
//		}
//		
//		return stringList;
//	}
//
//	@PostMapping("/show_option_seats")
//	public List<String> showListOption(@Param("bus_id") Integer bus_id){
//		List<String> stringList = new ArrayList<>();
//		List<BusSeat> seatList = new ArrayList<BusSeat>();
//		Bus savedBus = busService.findByIdBus(bus_id);
//		seatList = savedBus.getBusSeats();
//		try {
//			for (BusSeat seat : savedBus.getBusSeats()) {
//				for (BusPassenger pax : savedBus.getBusPassengers()) {
//					if (pax.getSeatId() == seat.getId()) {
//						seatList.remove(seat);
//					}
//				}
//			}
//			
//			for (BusSeat busSeat : seatList) {
//				
//				String str =  busSeat.getId() + "-" + busSeat.getSeatName() + "-" + busSeat.getSeatFare();
//				stringList.add(str);
//			}
//			
//		} catch (Exception e) {
//			for (BusSeat busSeat : seatList) {
//				
//				String str =  busSeat.getId() + "-" + busSeat.getSeatName() + "-" + busSeat.getSeatFare();
//				stringList.add(str);
//			}
//		}
//		
//
//		return stringList;
//	}
//	
//	@PostMapping("/save_bus_seat")
//	public void saveSeat(@Param("seatIndex") String seatIndex, @Param("bus_id") Integer bus_id, @Param("cust_id") Integer cust_id) {
//		Bus savedBus = busService.findByIdBus(bus_id);
//	    BusSeat newSeat = new BusSeat();
//		Customer customer = customerRepository.findById(cust_id).get();
//	    
//	    for (BusSeat seat : busController.seatList) {
//			if (seat.getSeatIndex() == Integer.parseInt(seatIndex)) {
//				newSeat = seat;
//			}
//		}
//	    Bus bus = busSaveHelper.setSeats(newSeat, savedBus);
//		
//	    busService.saveBus(bus, customer);
//	}
//
//	@PostMapping("/delete_bus_seat")
//	public void deleteSeat(@Param("seat_id") Integer seat_id) {
//		busService.deleteSeat(seat_id);
//	}

	
	
	// Private methods

	private void cityFinder(Model model) {
		Iterable<TBObusCity> cities = busCityRepo.findAll();
		List<String> cityList = new ArrayList<String>();
		List<String> cityIds = new ArrayList<String>();
		Date date = new Date();
		
		for (TBObusCity city : cities) {
			cityList.add(city.getCityName());
			cityIds.add(city.getCityId().toString());
		}

		model.addAttribute("today", date);
		model.addAttribute("tboCities", cities);
		model.addAttribute("cityList", cityList);
		model.addAttribute("cityIds", cityIds);
	}
	
	

	// POJO 
	public static class SearchBusRequest {
		private Integer cust_id;
		private String date;
		private String origin;
		private String destination;
		
		public SearchBusRequest() {}
		
		public Integer getCust_id() {
			return cust_id;
		}
		public void setCust_id(Integer cust_id) {
			this.cust_id = cust_id;
		}
		public String getDate() {
			return date;
		}
		public void setDate(String date) {
			this.date = date;
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
		
		
	}

}
