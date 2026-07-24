package com.easygofly.api.bus;

import java.io.IOException;
import java.net.MalformedURLException;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easygofly.api.LogService;
import com.easygofly.api.customer.CustomerRepository;
import com.easygofly.api.setting.PaymentSettingBag;
import com.easygofly.api.setting.SettingService;
import com.easygofly.entity.Bus;
import com.easygofly.entity.BusBoardingPointDetails;
import com.easygofly.entity.BusCancelPolicy;
import com.easygofly.entity.BusDroppingPointDetail;
import com.easygofly.entity.BusHistory;
import com.easygofly.entity.BusOrder;
import com.easygofly.entity.BusPassenger;
import com.easygofly.entity.BusPointDetails;
import com.easygofly.entity.BusSeat;
import com.easygofly.entity.Customer;
import com.easygofly.entity.OrderStatus;
import com.easygofly.entity.TBObusCity;
import com.easygofly.entity.Wallet;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.tags.Tag;
 
@RestController
@Tag(
		name = "CRUD REST APIs for Bus Booking", 
		description = "Operations related to bus booking"
)
public class BusRestController {

	@Autowired private BusCityRepository busCityRepo;
	@Autowired private BusService busService;
	@Autowired private CustomerRepository customerRepository;
	@Autowired private OnlineBusService onlineBusService;
	@Autowired private LogService logService;
	@Autowired private BusRepository busRepo;
	@Autowired private BusOrderRepository busOrderRepo;
	@Autowired private BusSaveHelper busSaveHelper;
	@Autowired private SettingService settingService;
	
	
	@GetMapping("/bus/city-list")
	public String getCityList(HttpServletResponse response) {
		response.setContentType("application/json");
    	Iterable<TBObusCity> cities = busCityRepo.findAll();
    	List<String> strCity = new ArrayList<>();
    	
    	for (TBObusCity city : cities) {
    		
			String cityBody = "{\r\n" 
							+ "  \"id\": " + city.getId() + ",\r\n" 
							+ "  \"cityId\": " + city.getCityId() + ",\r\n" 
							+ "  \"cityName\": \"" + sanitize(city.getCityName()) + "\"\r\n" 
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

	@PostMapping("/bus/search")
	public String saveSearchBus(HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException {

		SearchBusRequest searchBusRequest = new ObjectMapper().readValue(request.getInputStream(), SearchBusRequest.class);
		Customer customer = customerRepository.findById(searchBusRequest.cust_id).get();
		
		TBObusCity cityOne = busCityRepo.getCityByCityName(searchBusRequest.origin);
		TBObusCity cityTwo = busCityRepo.getCityByCityName(searchBusRequest.destination);
	    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	    Date startDate = dateFormat.parse(searchBusRequest.date);
		StringBuilder responseBody = new StringBuilder();
		@SuppressWarnings("unused")
		String errorCode = "";
		@SuppressWarnings("unused")
		String errorMessage = "";
		BusHistory history = new BusHistory();
		List<Bus> buses = new ArrayList<>();
		
	    if (customer != null) {	
			BusHistory newHistory = new BusHistory();

			newHistory.setDeptDate(startDate);
			newHistory.setCityIdOne(cityOne.getCityId().toString());
			newHistory.setCityIdTwo(cityTwo.getCityId().toString());
			newHistory.setCustomer(customer);
			
			history = busService.saveBusHistory(newHistory, customer);
			
			responseBody = onlineBusService.apiOnlineSearchBus(cityTwo.getCityId().toString(), cityOne.getCityId().toString(), startDate);
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
				String busType = mainObj.getJSONObject("Bus-" + i).get("BusType").toString().replace("\\", "-");
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
        
        String strHistory = "{\r\n"
        		                + "  \"id\": " + history.getId() + ",\r\n"
        		                + "  \"deptDate\": \"" + history.getDeptDate() + "\",\r\n"
        		                + "  \"cityIdOne\": \"" + history.getCityIdOne() + "\",\r\n"
        		                + "  \"cityIdTwo\": \"" + history.getCityIdTwo() + "\"\r\n"
        		                + "}";
        
       	String arrayBusList = strBuses.stream().map(val -> String.valueOf(val)).collect(Collectors.joining(",", "[", "]"));

        String responseBodyStr = "{"
        		+ "\"code\": 0, "
        		+ "\"msg\": \"List of Bus and Search Result.\", "
        		+ "\"history\": " + strHistory + ", "
        		+ "\"data\": " + arrayBusList + ""
        		+ "}";

		return responseBodyStr;
		
	}

	@PostMapping("/bus/booking")
	public String busBooking(HttpServletRequest request, HttpServletResponse response) throws Exception {

		BusBookingRequest busBookingRequest = new ObjectMapper().readValue(request.getInputStream(), BusBookingRequest.class);
		
		Customer customer = customerRepository.findById(busBookingRequest.customerId).get();
		Bus existingBus = new Bus();
		List<BusCancelPolicy> busCancelPolicies = new ArrayList<>();
		List<BusPointDetails> busBoardingPointDetails = new ArrayList<>();
		
		existingBus.setResultIndex(busBookingRequest.resultIndex);
		existingBus.setArrivalTime(busBookingRequest.arrivalTime);
		existingBus.setDepartureTime(busBookingRequest.departureTime);
		existingBus.setRouteId(busBookingRequest.routeId);
		existingBus.setBusType(busBookingRequest.busType);
		existingBus.setServiceName(busBookingRequest.serviceName);
		existingBus.setTravelName(busBookingRequest.travelName);
		existingBus.setCurrencyCode(busBookingRequest.currencyCode);
		existingBus.setIdProofRequired(busBookingRequest.idProofRequired);
		existingBus.setDropPointMandatory(busBookingRequest.isDropPointMandatory);
		existingBus.setLiveTrackingAvailable(busBookingRequest.liveTrackingAvailable);
		existingBus.setmTicketEnabled(busBookingRequest.mTicketEnabled);
		existingBus.setPartialCancellationAllowed(busBookingRequest.partialCancellationAllowed);
		existingBus.setMaxSeatsPerTicket(busBookingRequest.maxSeatsPerTicket);
		existingBus.setOperatorId(busBookingRequest.operatorId);
		existingBus.setTax(busBookingRequest.tax);
		existingBus.setDiscount(busBookingRequest.discount);
		existingBus.setPublishedPrice(busBookingRequest.publishedPrice);
		existingBus.setOtherCharges(busBookingRequest.otherCharges);
		existingBus.setOfferedPrice(busBookingRequest.offeredPrice);
		existingBus.setPublishedPriceRoundedOff((int) busBookingRequest.publishedPriceRoundedOff);
		existingBus.setOfferedPriceRoundedOff((int) busBookingRequest.offeredPriceRoundedOff);
		existingBus.setAgentCommission(busBookingRequest.agentCommission);
		existingBus.setAgentMarkUp(busBookingRequest.agentMarkUp);
		existingBus.setBasePrice(busBookingRequest.basePrice);
		existingBus.setTds(busBookingRequest.tds);
		existingBus.setcGSTAmount(busBookingRequest.cGSTAmount);
		existingBus.setcGSTRate(busBookingRequest.cGSTRate);
		existingBus.setCessAmount(busBookingRequest.cessAmount);
		existingBus.setCessRate(busBookingRequest.cessRate);
		existingBus.setiGSTAmount(busBookingRequest.iGSTAmount);
		existingBus.setiGSTRate(busBookingRequest.iGSTRate);
		existingBus.setsGSTAmount(busBookingRequest.sGSTAmount);
		existingBus.setsGSTRate(busBookingRequest.sGSTRate);
		existingBus.setTaxableAmount(busBookingRequest.taxableAmount);
		existingBus.setAvailableSeats(busBookingRequest.availableSeats);
		
		
		for (int i = 0; i < busBookingRequest.busCancelPolicies.size(); i++) {
            BusCancelPolicy newBusCancelPolicy = new BusCancelPolicy();
            newBusCancelPolicy.setCancellationCharge(busBookingRequest.busCancelPolicies.get(i).getCancellationCharge());
            newBusCancelPolicy.setCancellationChargeType(Integer.parseInt(busBookingRequest.busCancelPolicies.get(i).getCancellationChargeType()));
            newBusCancelPolicy.setPolicyString(busBookingRequest.busCancelPolicies.get(i).getPolicyString());
            newBusCancelPolicy.setTimeBeforeDept(busBookingRequest.busCancelPolicies.get(i).getTimeBeforeDept());
            newBusCancelPolicy.setFromDate(busBookingRequest.busCancelPolicies.get(i).getFromDate());
            newBusCancelPolicy.setToDate(busBookingRequest.busCancelPolicies.get(i).getToDate());
			
            busCancelPolicies.add(newBusCancelPolicy);
		}
		
		for (int i = 0; i < busBookingRequest.pointsDetails.size(); i++) {
			BusPointDetails newBusPointDetails = new BusPointDetails();
			newBusPointDetails.setCityPointIndex(busBookingRequest.pointsDetails.get(i).getCityPointIndex());
			newBusPointDetails.setCityPointLocation(busBookingRequest.pointsDetails.get(i).getCityPointLocation());
			newBusPointDetails.setCityPointName(busBookingRequest.pointsDetails.get(i).getCityPointName());
			newBusPointDetails.setCityPointTime(busBookingRequest.pointsDetails.get(i).getCityPointTime());
			newBusPointDetails.setPointType(busBookingRequest.pointsDetails.get(i).getPointType());

			busBoardingPointDetails.add(newBusPointDetails);
		}
		
		existingBus.setBusCancelPolicies(busCancelPolicies);
		existingBus.setPointsDetails(busBoardingPointDetails);
		
		Bus savedBus = busService.saveBus(existingBus, customer);

		System.out.println(savedBus);
		
	    String strBusSeat = busSeatLayout(busBookingRequest.resultIndex);
	    String strBusBoarding = busBusBoardingPoint(busBookingRequest.resultIndex, savedBus);
	    
	    List<String> cancels = new ArrayList<>();
    	List<String> points = new ArrayList<>();
    	
    	for (BusCancelPolicy busCancelPolicy : savedBus.getBusCancelPolicies()) {
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
    	
    	for (BusPointDetails busPointDetails : savedBus.getPointsDetails()) {
			String point = "{\r\n"
					+ "  \"cityPointIndex\": " + busPointDetails.getCityPointIndex() + ",\r\n"
					+ "  \"cityPointLocation\": \"" + busPointDetails.getCityPointLocation() + "\",\r\n"
					+ "  \"cityPointName\": \"" + busPointDetails.getCityPointName() + "\",\r\n"
					+ "  \"cityPointTime\": \"" + busPointDetails.getCityPointTime() + "\",\r\n"
					+ "  \"pointType\": \"" + busPointDetails.getPointType() + "\"\r\n"
					+ "}";
			
			points.add(point);
		}
    	
       	String arrayCancelList = cancels.stream().map(val -> String.valueOf(val)).collect(Collectors.joining(",", "[", "]"));
       	String arrayPointList = points.stream().map(val -> String.valueOf(val)).collect(Collectors.joining(",", "[", "]"));
    	
		String busBody = "{\r\n"
				+ "  \"id\": " + savedBus.getId() + ",\r\n"
				+ "  \"resultIndex\": " + savedBus.getResultIndex() + ",\r\n"
				+ "  \"arrivalTime\": \"" + savedBus.getArrivalTime() + "\",\r\n"
				+ "  \"departureTime\": \"" + savedBus.getDepartureTime() + "\",\r\n"
				+ "  \"routeId\": \"" + savedBus.getRouteId() + "\",\r\n"
				+ "  \"busType\": \"" + savedBus.getBusType() + "\",\r\n"
				+ "  \"serviceName\": \"" + savedBus.getServiceName() + "\",\r\n"
				+ "  \"travelName\": \"" + savedBus.getTravelName() + "\",\r\n"
				+ "  \"currencyCode\": \"" + savedBus.getCurrencyCode() + "\",\r\n"
				+ "  \"idProofRequired\": " + savedBus.isIdProofRequired() + ",\r\n"
				+ "  \"isDropPointMandatory\": " + savedBus.isDropPointMandatory() + ",\r\n"
				+ "  \"liveTrackingAvailable\": " + savedBus.isLiveTrackingAvailable() + ",\r\n"
				+ "  \"mTicketEnabled\": " + savedBus.ismTicketEnabled() + ",\r\n"
				+ "  \"partialCancellationAllowed\": " + savedBus.isPartialCancellationAllowed() + ",\r\n"
				+ "  \"maxSeatsPerTicket\": " + savedBus.getMaxSeatsPerTicket() +",\r\n"
				+ "  \"operatorId\": " + savedBus.getOperatorId() + ",\r\n"
				+ "  \"tax\": " + savedBus.getTax() + ",\r\n"
				+ "  \"discount\": " + savedBus.getDiscount() + ",\r\n"
				+ "  \"publishedPrice\": " + savedBus.getPublishedPrice() + ",\r\n"
				+ "  \"otherCharges\": " + savedBus.getOtherCharges() + ",\r\n"
				+ "  \"offeredPrice\": " + savedBus.getOfferedPrice() + ",\r\n"
				+ "  \"publishedPriceRoundedOff\": " + savedBus.getPublishedPriceRoundedOff() + ",\r\n"
				+ "  \"offeredPriceRoundedOff\": " + savedBus.getOfferedPriceRoundedOff() + ",\r\n"
				+ "  \"agentCommission\": " + savedBus.getAgentCommission() + ",\r\n"
				+ "  \"agentMarkUp\": " + savedBus.getAgentMarkUp() + ",\r\n"
				+ "  \"basePrice\": " + savedBus.getBasePrice() + ",\r\n"
				+ "  \"tds\": " + savedBus.getTds() + ",\r\n"
				+ "  \"cGSTAmount\": " + savedBus.getcGSTAmount() + ",\r\n"
				+ "  \"cGSTRate\": " + savedBus.getcGSTRate() + ",\r\n"
				+ "  \"cessAmount\": " + savedBus.getCessAmount() + ",\r\n"
				+ "  \"cessRate\": " + savedBus.getCessRate() + ",\r\n"
				+ "  \"iGSTAmount\": " + savedBus.getiGSTAmount() + ",\r\n"
				+ "  \"iGSTRate\": " + savedBus.getiGSTRate() + ",\r\n"
				+ "  \"sGSTAmount\": " + savedBus.getsGSTAmount() + ",\r\n"
				+ "  \"sGSTRate\": " + savedBus.getsGSTRate() + ",\r\n"
				+ "  \"taxableAmount\": " + savedBus.getTaxableAmount() + ",\r\n"
				+ "  \"availableSeats\": " + savedBus.getAvailableSeats() + ",\r\n"
				+ "  \"busCancelPolicies\": " + arrayCancelList + ",\r\n"
				+ "  \"pointsDetails\": " + arrayPointList + "\r\n"
				+ "}";
		
		String responseBodyStr = "{" 
				+ "\"code\": 0, " 
				+ "\"msg\": \"List of Bus and Search Result.\", " 
				+ "\"bus\": " + busBody + ", " 
				+ "\"busSeatLayout\": " + strBusSeat + ", " 
				+ "\"boardingPoints\": " + strBusBoarding + "" 
				+ "}";
		
		return responseBodyStr;
	}
	
	@PostMapping("/bus/save_bus_pax_seat")
	public String savePaxSeat(HttpServletRequest request, HttpServletResponse response) throws StreamReadException, DatabindException, IOException {
		SeatAndPassengerRequest seatAndPassengerRequest = new ObjectMapper().readValue(request.getInputStream(), SeatAndPassengerRequest.class);
		
		Bus savedBus = busService.findByIdBus(seatAndPassengerRequest.busId);
		Customer customer = customerRepository.findById(seatAndPassengerRequest.custId).get();
		
		seatAndPassengerRequest.seats.forEach(s -> {
			Integer[] count = {0};
			
		    BusSeat newSeat = new BusSeat();
	        newSeat.setSeatName(s.seatName);
	        newSeat.setSeatFare(s.seatFare);
	        newSeat.setSeatType(s.seatType);
	        newSeat.setSeatIndex(Integer.parseInt(s.seatIndex));
	        newSeat.setHeight(s.height);
	        newSeat.setWidth(s.width);
	        newSeat.setLadiesSeat(s.isLadiesSeat);
	        newSeat.setMalesSeat(s.isMalesSeat);
	        newSeat.setSeatStatus(s.seatStatus);
	        newSeat.setRowNo(s.rowNo);
	        newSeat.setColumnNo(s.columnNo);
	        newSeat.setBus(savedBus);
	        newSeat.setBasePrice(s.basePrice);
	        newSeat.setPublishedPrice(s.publishedPrice);
	        newSeat.setOfferedPrice(s.offeredPrice);
	        newSeat.setPublishedPriceRoundedOff(s.publishedPriceRoundedOff);
	        newSeat.setOfferedPriceRoundedOff(s.offeredPriceRoundedOff);
	        newSeat.setAgentCommission(s.agentCommission);
	        newSeat.setAgentMarkUp(s.agentMarkUp);
	        newSeat.setTds(s.tds);
	        newSeat.setcGSTAmount(s.cGSTAmount);
	        newSeat.setcGSTRate(s.cGSTRate);
	        newSeat.setCessAmount(s.cessAmount);
	        newSeat.setCessRate(s.cessRate);
	        newSeat.setiGSTAmount(s.iGSTAmount);
	        newSeat.setiGSTRate(s.iGSTRate);
	        newSeat.setsGSTAmount(s.sGSTAmount);
	        newSeat.setsGSTRate(s.sGSTRate);
	        newSeat.setTaxableAmount(s.taxableAmount);
	        newSeat.setTax(s.tax);
	        newSeat.setDiscount(s.discount);
	        newSeat.setOtherCharges(s.otherCharges);
	        
	        Bus bus = busSaveHelper.setSeats(newSeat, savedBus);
			
	        Bus busSaved = busService.saveBus(bus, customer);
	        
	        BusSeat savedSeat = busSaved.getBusSeats().get(busSaved.getBusSeats().size() - 1);
	        
	        seatAndPassengerRequest.passengers.forEach(p -> {
	        	if(savedSeat.getSeatIndex() == p.seatId) {
	        		Bus busSavedPax = busSaveHelper.setPax(savedBus, p.title, p.fName, p.lName, p.phoneNo, p.email, p.pan, "Pan no", p.gender, p.age, savedSeat.getId(), false, p.address);

	        		BusPassenger pax = busSavedPax.getBusPassengers().get(busSavedPax.getBusPassengers().size() - 1);
	        		
	        		count[0]++;
	        		
					if(count[0] == 1) {
						pax.setLeadPassenger(true);
						busService.savePax(pax);
					} else {
						pax.setLeadPassenger(false);
						busService.savePax(pax);
					}
					
	        		busService.saveBus(busSavedPax, customer);
	        		System.out.println(busSavedPax.getTravelName());
	        	}
	        });
		});
		
		String str = "{\r\n"
				+ "  \"status\": \"Success\"\r\n"
				+ "}";
		
		return str;
	}

	@PostMapping("/bus/order/wallet_check")
	public String busWalletPayment(HttpServletRequest request, HttpServletResponse response) throws StreamReadException, DatabindException, IOException {

		BusOrderRequest busOrderRequest = new ObjectMapper().readValue(request.getInputStream(), BusOrderRequest.class);

		Customer customer = customerRepository.findById(busOrderRequest.custId).get();
//    	PaymentSettingBag paymentSettingBag = settingService.getPaymentSettings();
		Bus bus = busService.findByIdBus(busOrderRequest.busId);
	    BusHistory busHistory = busService.findByIdBusHistory(busOrderRequest.searchId);
		TBObusCity cityOne = busCityRepo.getCityByCityId(Integer.parseInt(busHistory.getCityIdOne()));
		TBObusCity cityTwo = busCityRepo.getCityByCityId(Integer.parseInt(busHistory.getCityIdTwo()));
	    
	    Date createdDate = new Date();
	    
	    String busOrdername = busHistory.getDeptDate() + ":(" + cityOne.getCityName() + "-" + cityTwo.getCityName() + "):" + createdDate;
	    
	    BusOrder busOrder = new BusOrder(busOrdername, 0, createdDate, OrderStatus.NEW, customer, busHistory, bus);
	    BusOrder savedOrder= busService.saveOrder(busOrder, bus, busHistory);
		
		double totalPrice = 0;
	    
	    for (BusSeat seat : bus.getBusSeats()) {
	    	totalPrice = totalPrice + seat.getPublishedPriceRoundedOff();
		}
	    BusOrder updatedOrder = busService.updateOrderPrice(savedOrder.getId(), totalPrice);
	    
		Wallet wallet = busService.busWalletPayOrder(customer, updatedOrder);
		updatedOrder = (wallet != null) ? busService.updateOrderStatus(busOrder.getId(), OrderStatus.SUCCESSFULL) : busService.updateOrderStatus(busOrder.getId(), OrderStatus.FAILED);

		String urlName = (updatedOrder.getOrderStatus() == OrderStatus.SUCCESSFULL) ? busBlockMethod(bus) : "Failed";
	    String hasArr = (updatedOrder.getOrderStatus() == OrderStatus.SUCCESSFULL) ? busBookkMethodAndBookingDetails(updatedOrder) : "Failed";
		
	    String rsp = "{"  + 
				"\"code\": 0, " + "\"msg\": \"List of Bus and Order Result.\", " + "\"bus\": "
				+ updatedOrder.getId() + ", " + "\"block\": " + urlName + ", " + "\"book\": " + hasArr
				+ "}";
	    
	    System.out.println(rsp);
	    
		return rsp;
	}

	@PostMapping("/bus/order/save_zaak")
	public String busZaakOrderSave(HttpServletRequest request, HttpServletResponse response) throws StreamReadException, DatabindException, IOException {

		BusOrderRequest busOrderRequest = new ObjectMapper().readValue(request.getInputStream(), BusOrderRequest.class);

		Customer customer = customerRepository.findById(busOrderRequest.custId).get();
//    	PaymentSettingBag paymentSettingBag = settingService.getPaymentSettings();
		Bus bus = busService.findByIdBus(busOrderRequest.busId);
	    BusHistory busHistory = busService.findByIdBusHistory(busOrderRequest.searchId);
		TBObusCity cityOne = busCityRepo.getCityByCityId(Integer.parseInt(busHistory.getCityIdOne()));
		TBObusCity cityTwo = busCityRepo.getCityByCityId(Integer.parseInt(busHistory.getCityIdTwo()));
	    
	    Date createdDate = new Date();
	    
	    String busOrdername = busHistory.getDeptDate() + ":(" + cityOne.getCityName() + "-" + cityTwo.getCityName() + "):" + createdDate;
	    
	    BusOrder busOrder = new BusOrder(busOrdername, 0, createdDate, OrderStatus.NEW, customer, busHistory, bus);
	    BusOrder savedOrder= busService.saveOrder(busOrder, bus, busHistory);
		
		double totalPrice = 0;
	    
	    for (BusSeat seat : bus.getBusSeats()) {
	    	totalPrice = totalPrice + seat.getPublishedPriceRoundedOff();
		}
	    BusOrder updatedOrder = busService.updateOrderPrice(savedOrder.getId(), totalPrice);
		
	    String rsp = "{"  + 
				"\"code\": 0, " 
	    		+ "\"msg\": \"new saved Order.\", " 
				+ "\"order\": " + updatedOrder.getId() + " "
				+ "}";
	    
	    System.out.println(rsp);
	    
		return rsp;
	}
	
	@PostMapping("/bus/order/zaak_check")
	public String busZaakPayment(HttpServletRequest request, HttpServletResponse response) throws StreamReadException, DatabindException, IOException {

		BusOrderSaveRequest busOrderRequest = new ObjectMapper().readValue(request.getInputStream(), BusOrderSaveRequest.class);

		Bus bus = busService.findByIdBus(busOrderRequest.busId);
	    BusOrder busOrder = busOrderRepo.findById(busOrderRequest.orderId).get();
	    
		busOrder = busService.updateOrderStatus(busOrder.getId(), OrderStatus.SUCCESSFULL);

		String urlName = (busOrder.getOrderStatus() == OrderStatus.SUCCESSFULL) ? busBlockMethod(bus) : "Failed";
	    String hasArr = (busOrder.getOrderStatus() == OrderStatus.SUCCESSFULL) ? busBookkMethodAndBookingDetails(busOrder) : "Failed";
		
	    String rsp = "{"  + 
				"\"code\": 0, " + "\"msg\": \"List of Bus and Order Result.\", " + "\"bus\": "
				+ busOrder.getId() + ", " + "\"block\": " + urlName + ", " + "\"book\": " + hasArr
				+ "}";
	    
	    System.out.println(rsp);
	    
		return rsp;
	}
	
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
//
//	@PostMapping("/delete_bus_seat")
//	public void deleteSeat(@Param("seat_id") Integer seat_id) {
//		busService.deleteSeat(seat_id);
//	}

	
	
	// Private methods
	
	private String busSeatLayout(Integer resultIndex) throws IOException {

        StringBuilder responseBodyBusSeatLayout = onlineBusService.apiOnlineBusSeatLayout(resultIndex);;
        
        JSONObject jsonObjSeatLayout = new JSONObject(responseBodyBusSeatLayout.toString());
        System.out.println(jsonObjSeatLayout);
        logService.generateLog(jsonObjSeatLayout.toString());
        
        List<BusSeat> seatList = new ArrayList<BusSeat>();
        List<String> seatListIds = new ArrayList<String>();
        
        try {
        	JSONObject jsonObjSeat = jsonObjSeatLayout.getJSONObject("GetBusSeatLayOutResult").getJSONObject("SeatLayoutDetails");
        	JSONArray jsonArraySeatDetails = jsonObjSeat.getJSONObject("SeatLayout").getJSONArray("SeatDetails");
			JSONObject mainObj = new JSONObject();
        	
        	String availableSeats = jsonObjSeat.get("AvailableSeats").toString();
//        	String hTMLLayout = jsonObjSeat.get("HTMLLayout").toString();
        	Integer noOfColumns = Integer.parseInt(jsonObjSeat.getJSONObject("SeatLayout").get("NoOfColumns").toString());
        	Integer noOfRows = Integer.parseInt(jsonObjSeat.getJSONObject("SeatLayout").get("NoOfRows").toString());
        	
        	for (int i = 0; i < jsonArraySeatDetails.length(); i++) {
        		for (int j = 0; j < jsonArraySeatDetails.getJSONArray(i).length(); j++) {
        			mainObj.put("Seat-" + i + "-" + j, jsonArraySeatDetails.getJSONArray(i).getJSONObject(j));
        			
        			String columnNo = mainObj.getJSONObject("Seat-" + i + "-" + j).get("ColumnNo").toString();
    			    String rowNo = mainObj.getJSONObject("Seat-" + i + "-" + j).get("RowNo").toString();
    			    String seatName = mainObj.getJSONObject("Seat-" + i + "-" + j).get("SeatName").toString();
    	        	Integer height = Integer.parseInt(mainObj.getJSONObject("Seat-" + i + "-" + j).get("Height").toString());
    	        	Integer seatIndex = Integer.parseInt(mainObj.getJSONObject("Seat-" + i + "-" + j).get("SeatIndex").toString());
    	        	Integer seatType = Integer.parseInt(mainObj.getJSONObject("Seat-" + i + "-" + j).get("SeatType").toString());
    	        	Integer width = Integer.parseInt(mainObj.getJSONObject("Seat-" + i + "-" + j).get("Width").toString());
    	        	boolean isLadiesSeat = Boolean.parseBoolean(mainObj.getJSONObject("Seat-" + i + "-" + j).get("IsLadiesSeat").toString());
    	        	boolean isMalesSeat = Boolean.parseBoolean(mainObj.getJSONObject("Seat-" + i + "-" + j).get("IsMalesSeat").toString());
    	        	boolean seatStatus = Boolean.parseBoolean(mainObj.getJSONObject("Seat-" + i + "-" + j).get("SeatStatus").toString());
    	        	double seatFare = Double.parseDouble(mainObj.getJSONObject("Seat-" + i + "-" + j).get("SeatFare").toString());
    	        	double BasePrice = Double.parseDouble(mainObj.getJSONObject("Seat-" + i + "-" + j).getJSONObject("Price").get("BasePrice").toString());
    			    double tax = Double.parseDouble(mainObj.getJSONObject("Seat-" + i + "-" + j).getJSONObject("Price").get("Tax").toString());
    			    double discount = Double.parseDouble(mainObj.getJSONObject("Seat-" + i + "-" + j).getJSONObject("Price").get("Discount").toString());
    			    double publishedPrice = Double.parseDouble(mainObj.getJSONObject("Seat-" + i + "-" + j).getJSONObject("Price").get("PublishedPrice").toString());
    			    double otherCharges = Double.parseDouble(mainObj.getJSONObject("Seat-" + i + "-" + j).getJSONObject("Price").get("OtherCharges").toString());
    			    double offeredPrice = Double.parseDouble(mainObj.getJSONObject("Seat-" + i + "-" + j).getJSONObject("Price").get("OfferedPrice").toString());
    			    Integer publishedPriceRoundedOff = Integer.parseInt(mainObj.getJSONObject("Seat-" + i + "-" + j).getJSONObject("Price").get("PublishedPriceRoundedOff").toString());
    			    Integer offeredPriceRoundedOff = Integer.parseInt(mainObj.getJSONObject("Seat-" + i + "-" + j).getJSONObject("Price").get("OfferedPriceRoundedOff").toString());
    			    double agentCommission = Double.parseDouble(mainObj.getJSONObject("Seat-" + i + "-" + j).getJSONObject("Price").get("AgentCommission").toString());
    			    double agentMarkUp = Double.parseDouble(mainObj.getJSONObject("Seat-" + i + "-" + j).getJSONObject("Price").get("AgentMarkUp").toString());
    			    double tds = Double.parseDouble(mainObj.getJSONObject("Seat-" + i + "-" + j).getJSONObject("Price").get("TDS").toString());
    			    double cGSTAmount = Double.parseDouble(mainObj.getJSONObject("Seat-" + i + "-" + j).getJSONObject("Price").getJSONObject("GST").get("CGSTAmount").toString());
    			    double cGSTRate = Double.parseDouble(mainObj.getJSONObject("Seat-" + i + "-" + j).getJSONObject("Price").getJSONObject("GST").get("CGSTRate").toString());
    			    double cessAmount = Double.parseDouble(mainObj.getJSONObject("Seat-" + i + "-" + j).getJSONObject("Price").getJSONObject("GST").get("CessAmount").toString());
    			    double cessRate = Double.parseDouble(mainObj.getJSONObject("Seat-" + i + "-" + j).getJSONObject("Price").getJSONObject("GST").get("CessRate").toString());
    			    double iGSTAmount = Double.parseDouble(mainObj.getJSONObject("Seat-" + i + "-" + j).getJSONObject("Price").getJSONObject("GST").get("IGSTAmount").toString());
    			    double iGSTRate = Double.parseDouble(mainObj.getJSONObject("Seat-" + i + "-" + j).getJSONObject("Price").getJSONObject("GST").get("IGSTRate").toString());
    			    double sGSTAmount = Double.parseDouble(mainObj.getJSONObject("Seat-" + i + "-" + j).getJSONObject("Price").getJSONObject("GST").get("SGSTAmount").toString());
    			    double sGSTRate = Double.parseDouble(mainObj.getJSONObject("Seat-" + i + "-" + j).getJSONObject("Price").getJSONObject("GST").get("SGSTRate").toString());
    			    double taxableAmount = Double.parseDouble(mainObj.getJSONObject("Seat-" + i + "-" + j).getJSONObject("Price").getJSONObject("GST").get("TaxableAmount").toString());
    			    
    			    BusSeat newSeat = new BusSeat(columnNo, rowNo, height, width, seatType, seatName, seatIndex, seatFare, isLadiesSeat, isMalesSeat, seatStatus, 
    					   "INR", tax, discount, BasePrice, publishedPrice, otherCharges, offeredPrice, publishedPriceRoundedOff, offeredPriceRoundedOff, 
    					   agentCommission, agentMarkUp, tds, cGSTAmount, cGSTRate, cessAmount, cessRate, iGSTAmount, iGSTRate, sGSTAmount, sGSTRate, taxableAmount);
    			    
    			    seatList.add(newSeat);
    			    seatListIds.add(newSeat.getSeatIndex().toString());
					
				}
//			    mainObj.put("Seat-" + i, jsonArraySeatDetails.getJSONArray(0).getJSONObject(i));
			}

			List<String> seatListStr = new ArrayList<String>();
			
			for (BusSeat busSeat : seatList) {
				String strbusSeat = "{\r\n" 
						+ "  \"columnNo\": \"" + busSeat.getColumnNo() + "\",\r\n"
						+ "  \"rowNo\": \"" + busSeat.getRowNo() + "\",\r\n" 
						+ "  \"height\": " + busSeat.getHeight() + ",\r\n" 
						+ "  \"width\": " + busSeat.getWidth() + ",\r\n" 
						+ "  \"seatType\": " + busSeat.getSeatType() + ",\r\n" 
						+ "  \"seatName\": \"" + busSeat.getSeatName() + "\",\r\n"
						+ "  \"seatIndex\": " + busSeat.getSeatIndex() + ",\r\n" 
						+ "  \"seatFare\": " + busSeat.getSeatFare() + ",\r\n" 
						+ "  \"isLadiesSeat\": " + busSeat.isLadiesSeat() + ",\r\n"
						+ "  \"isMalesSeat\": " + busSeat.isMalesSeat() + ",\r\n" 
						+ "  \"price\": " + busSeat.getPublishedPriceRoundedOff() + ",\r\n" 
						+ "  \"basePrice\": " + busSeat.getBasePrice() + ",\r\n"
						+ "  \"publishedPrice\": " + busSeat.getPublishedPrice() + ",\r\n"
						+ "  \"offeredPrice\": " + busSeat.getOfferedPrice() + ",\r\n"
						+ "  \"publishedPriceRoundedOff\": " + busSeat.getPublishedPriceRoundedOff() + ",\r\n"
						+ "  \"offeredPriceRoundedOff\": " + busSeat.getOfferedPriceRoundedOff() + ",\r\n"
						+ "  \"agentCommission\": " + busSeat.getAgentCommission() + ",\r\n"
						+ "  \"agentMarkUp\": " + busSeat.getAgentMarkUp() + ",\r\n"
						+ "  \"tds\": " + busSeat.getTds() + ",\r\n"
						+ "  \"cGSTAmount\": " + busSeat.getcGSTAmount() + ",\r\n"
						+ "  \"cGSTRate\": " + busSeat.getcGSTRate() + ",\r\n"
						+ "  \"cessAmount\": " + busSeat.getCessAmount() + ",\r\n"
						+ "  \"cessRate\": " + busSeat.getCessRate() + ",\r\n"
						+ "  \"iGSTAmount\": " + busSeat.getiGSTAmount() + ",\r\n"
						+ "  \"iGSTRate\": " + busSeat.getiGSTRate() + ",\r\n"
						+ "  \"sGSTAmount\": " + busSeat.getsGSTAmount() + ",\r\n"
						+ "  \"sGSTRate\": " + busSeat.getsGSTRate() + ",\r\n"
						+ "  \"taxableAmount\": " + busSeat.getTaxableAmount() + ",\r\n"
						+ "  \"tax\": " + busSeat.getTax() + ",\r\n"
						+ "  \"discount\": " + busSeat.getDiscount() + ",\r\n"
						+ "  \"otherCharges\": " + busSeat.getOtherCharges() + ",\r\n"
						+ "  \"seatStatus\": " + busSeat.isSeatStatus() + "\r\n" 
						+ "}";
				
				seatListStr.add(strbusSeat);
			}
			
			String arraySeatList = seatListStr.stream().map(val -> String.valueOf(val)).collect(Collectors.joining(",", "[", "]"));
			
			return "{\r\n" 
					+ "  \"seatList\": " + arraySeatList + ",\r\n"
					+ "  \"availableSeats\": " + availableSeats + ",\r\n"
//					+ "  \"hTMLLayout\": \"" + hTMLLayout + "\",\r\n" 
					+ "  \"noOfColumns\": " + noOfColumns + ",\r\n"
					+ "  \"noOfRows\": " + noOfRows + "\r\n" 
					+ "}";
			
        	
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			
			return "";
		}
	}

	private String busBusBoardingPoint(Integer resultIndex, Bus bus) throws IOException {

        StringBuilder responseBodyBusPointDetail = onlineBusService.apiOnlineBusBoardingPoint(resultIndex);;
        
        JSONObject jsonObjPointDetail = new JSONObject(responseBodyBusPointDetail.toString());
        System.out.println(jsonObjPointDetail);
        logService.generateLog(jsonObjPointDetail.toString());
        
        try {
        	JSONObject jsonObjPoint = jsonObjPointDetail.getJSONObject("GetBusRouteDetailResult");
        	JSONArray jsonArrayBoarding = jsonObjPoint.getJSONArray("BoardingPointsDetails");
        	JSONArray jsonArrayDropping = jsonObjPoint.getJSONArray("DroppingPointsDetails");
			JSONObject mainObjBoarding = new JSONObject();
			JSONObject mainObjDropping = new JSONObject();

			List<BusDroppingPointDetail> droppingPointDetails = new ArrayList<BusDroppingPointDetail>();
			List<BusBoardingPointDetails> boardingPointDetails = new ArrayList<BusBoardingPointDetails>();
        	
        	for (int i = 0; i < jsonArrayBoarding.length(); i++) {
        		mainObjBoarding.put("Point-" + i, jsonArrayBoarding.getJSONObject(i));

			    String CityPointAddress = mainObjBoarding.getJSONObject("Point-" + i).get("CityPointAddress").toString();
			    String CityPointContactNumber = mainObjBoarding.getJSONObject("Point-" + i).get("CityPointContactNumber").toString();
			    Integer CityPointIndex = Integer.parseInt(mainObjBoarding.getJSONObject("Point-" + i).get("CityPointIndex").toString());
			    String CityPointLandmark = mainObjBoarding.getJSONObject("Point-" + i).get("CityPointLandmark").toString();
			    String CityPointLocation = mainObjBoarding.getJSONObject("Point-" + i).get("CityPointLocation").toString();
			    String CityPointName = mainObjBoarding.getJSONObject("Point-" + i).get("CityPointName").toString();
			    String CityPointTime = mainObjBoarding.getJSONObject("Point-" + i).get("CityPointTime").toString();
			    
			    BusBoardingPointDetails newPoint = new BusBoardingPointDetails(CityPointIndex, CityPointLocation, CityPointName, CityPointTime, CityPointLandmark, CityPointContactNumber, CityPointAddress);
			    newPoint.setBus(bus);
			    boardingPointDetails.add(newPoint);
			}
        	
        	for (int i = 0; i < jsonArrayDropping.length(); i++) {
        		mainObjDropping.put("Point-" + i, jsonArrayDropping.getJSONObject(i));
        		
			    Integer CityPointIndex = Integer.parseInt(mainObjDropping.getJSONObject("Point-" + i).get("CityPointIndex").toString());
			    String CityPointLocation = mainObjDropping.getJSONObject("Point-" + i).get("CityPointLocation").toString();
			    String CityPointName = mainObjDropping.getJSONObject("Point-" + i).get("CityPointName").toString();
			    String CityPointTime = mainObjDropping.getJSONObject("Point-" + i).get("CityPointTime").toString();
			    
			    BusDroppingPointDetail newPoint =  new BusDroppingPointDetail(CityPointIndex, CityPointLocation, CityPointName, CityPointTime);
			    newPoint.setBus(bus);
			    droppingPointDetails.add(newPoint);
			}
        	
        	bus.setBusBoardingPointDetails(boardingPointDetails);
        	bus.setBusDroppingPointDetails(droppingPointDetails);
        	
        	busRepo.save(bus);
			List<String> strBoardingPointDetails = new ArrayList<String>();
			List<String> strDroppingPointDetails = new ArrayList<String>();
			
			for (BusBoardingPointDetails busBoardingPointDetails : boardingPointDetails) {
				String safeAddress = busBoardingPointDetails.getCityPointAddress()
					    .replace("\\", "\\\\")
					    .replace("\"", "\\\"")
					    .replace("\n", "\\n")
					    .replace("\r", "\\r")
					    .replace("\t", "\\t");
				
				String str = "{\r\n" 
						+ "  \"cityPointIndex\": " + busBoardingPointDetails.getCityPointIndex() + ",\r\n"
						+ "  \"cityPointLocation\": \"" + busBoardingPointDetails.getCityPointLocation() + "\",\r\n"
						+ "  \"cityPointName\": \"" + busBoardingPointDetails.getCityPointName() + "\",\r\n"
						+ "  \"cityPointTime\": \"" + busBoardingPointDetails.getCityPointTime() + "\",\r\n"
						+ "  \"landmark\": \"" + busBoardingPointDetails.getCityPointLandmark() + "\",\r\n" 
						+ "  \"contactNumber\": \"" + busBoardingPointDetails.getCityPointContactNumber() + "\",\r\n"
						+ "  \"address\": \"" + safeAddress + "\"\r\n"
						+ "}";
				
				strBoardingPointDetails.add(str);
			}
			
			String arrayBoardingPointList = strBoardingPointDetails.stream().map(val -> String.valueOf(val)).collect(Collectors.joining(",", "[", "]"));
			
			for (BusDroppingPointDetail busDroppingPointDetail : droppingPointDetails) {
				String str = "{\r\n" 
						+ "  \"cityPointIndex\": " + busDroppingPointDetail.getCityPointIndex() + ",\r\n"
						+ "  \"cityPointLocation\": \"" + busDroppingPointDetail.getCityPointLocation() + "\",\r\n"
						+ "  \"cityPointName\": \"" + busDroppingPointDetail.getCityPointName() + "\",\r\n"
						+ "  \"cityPointTime\": \"" + busDroppingPointDetail.getCityPointTime() + "\"\r\n"
						+ "}";
				
				strDroppingPointDetails.add(str);
			}
			
			String arrayDroppingPointList = strDroppingPointDetails.stream().map(val -> String.valueOf(val)).collect(Collectors.joining(",", "[", "]"));
			
			return "{\r\n" 
				+ "  \"boardingPointDetails\": " + arrayBoardingPointList + ",\r\n"
				+ "  \"droppingPointDetails\": " + arrayDroppingPointList + "\r\n" 
				+ "}";
        	
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return "";
		}
	}

	private String busBlockMethod(Bus bus) 
			throws MalformedURLException, IOException {
		
		List<String> strPaxs = new ArrayList<String>();
		
		Integer count = 0;
        
        for (BusPassenger pax : bus.getBusPassengers()) {
        	BusSeat seat = busService.findByIdSeat(pax.getSeatId());
        	count++;
        	boolean isLead = false;
        	
        	if (pax.getAge() >= 18) {
				if (count == 1) {
					isLead = true;
				} else {
					isLead = false;
				}
			} else {
				isLead = false;
			}
        	
			String busPaxDetail = "    {\r\n"
	        		+ "      \"LeadPassenger\": " + isLead + ",\r\n"
	        		+ "      \"PassengerId\": 0,\r\n"
	        		+ "      \"Title\": \"" + pax.getTitle() + "\",\r\n"
	        		+ "      \"Address\": \"" + pax.getAddress() + "\",\r\n"
	        		+ "      \"Age\": " + pax.getAge() + ",\r\n"
	        		+ "      \"Email\": \"" + pax.getEmail() + "\",\r\n"
	        		+ "      \"FirstName\": \"" + pax.getFirstName() + "\",\r\n"
	    	        + "      \"Gender\": " + pax.getGender() + ",\r\n"
	    	    	+ "      \"IdNumber\": null,\r\n"
	    	    	+ "      \"IdType\": null,\r\n"
	    	    	+ "      \"LastName\": \"" + pax.getLastName() + "\",\r\n"
	    	    	+ "      \"Phoneno\": \"" + pax.getPhoneNo() + "\",\r\n"
	        		+ "      \"Seat\": {\r\n"
	        		+ "        \"ColumnNo\": \"" + seat.getColumnNo() + "\",\r\n"
	        		+ "        \"Height\": " + seat.getHeight() + ",\r\n"
	        		+ "        \"IsLadiesSeat\": " + seat.isLadiesSeat() + ",\r\n"
	        		+ "        \"IsMalesSeat\": " + seat.isMalesSeat() + ",\r\n"
	        		+ "        \"IsUpper\": false,\r\n"
	        		+ "        \"RowNo\": \"" + seat.getRowNo() + "\",\r\n"
	        		+ "        \"SeatIndex\": \"" + seat.getSeatIndex() + "\",\r\n"
	        		+ "        \"SeatName\": \"" + seat.getSeatName() + "\",\r\n"
	        		+ "        \"SeatStatus\": " + seat.isSeatStatus() + ",\r\n"
	        		+ "        \"SeatType\": " + seat.getSeatType() + ",\r\n"
	        		+ "        \"Width\": " + seat.getWidth() + ",\r\n"
	        		+ "        \"Price\": {\r\n"
	        		+ "          \"CurrencyCode\": \"INR\",\r\n"
	        		+ "          \"BasePrice\": " + seat.getBasePrice() + ",\r\n"
	        		+ "          \"Tax\": " + seat.getTax() + ",\r\n"
	        		+ "          \"OtherCharges\": " + seat.getOtherCharges() + ",\r\n"
	        		+ "          \"Discount\": " + seat.getDiscount() + ",\r\n"
	        		+ "          \"PublishedPrice\": " + seat.getPublishedPrice() + ",\r\n"
	        		+ "          \"PublishedPriceRoundedOff\": " + seat.getPublishedPriceRoundedOff() + ",\r\n"
	        		+ "          \"OfferedPrice\": " + seat.getOfferedPrice() + ",\r\n"
	        		+ "          \"OfferedPriceRoundedOff\": " + seat.getOfferedPriceRoundedOff() + ",\r\n"
	        		+ "          \"AgentCommission\": " + seat.getAgentCommission() + ",\r\n"
	        		+ "          \"AgentMarkUp\": " + seat.getAgentMarkUp() + ",\r\n"
	        		+ "          \"TDS\": " + seat.getTds() + ",\r\n"
	        		+ "          \"GST\": {\r\n"
	        		+ "            \"CGSTAmount\": " + seat.getcGSTAmount() + ",\r\n"
	        		+ "            \"CGSTRate\": " + seat.getcGSTRate() + ",\r\n"
	        		+ "            \"CessAmount\": " + seat.getCessAmount() + ",\r\n"
	        		+ "            \"CessRate\": " + seat.getCessRate() + ",\r\n"
	        		+ "            \"IGSTAmount\": " + seat.getiGSTAmount() + ",\r\n"
	        		+ "            \"IGSTRate\": " + seat.getiGSTRate() + ",\r\n"
	        		+ "            \"SGSTAmount\": " + seat.getsGSTAmount() + ",\r\n"
	        		+ "            \"SGSTRate\": " + seat.getsGSTRate() + ",\r\n"
	        		+ "            \"TaxableAmount\": " + seat.getTaxableAmount() + "\r\n"
	        		+ "          }\r\n"
	        		+ "        }\r\n"
	        		+ "      }\r\n"
	        		+ "    }\r\n";
			
			strPaxs.add(busPaxDetail);
			
			pax.setLeadPassenger(isLead);
			busService.savePax(pax);
		}
       	
       	String arraySeat = strPaxs.stream().map(val -> String.valueOf(val)).collect(Collectors.joining(",", "[", "]"));
		
        StringBuilder responseBodyBusBlock = onlineBusService.apiOnlineBusBlock(arraySeat, bus.getResultIndex(), bus.getBusBoardingPointDetails().get(0).getCityPointIndex(), 
        		bus.getBusDroppingPointDetails().get(0).getCityPointIndex());;
        
        JSONObject jsonObjBlock = new JSONObject(responseBodyBusBlock.toString());
        System.out.println(jsonObjBlock);
        logService.generateLog(jsonObjBlock.toString());
        
        try {
        	return responseBodyBusBlock.toString();
    		
		} catch (Exception e) {
			
			e.printStackTrace();
			return 	"{\r\n"
					+ "	\"Response\": {\r\n"
					+ "		\"ResponseStatus\": -1,\r\n"
					+ "		\"CurrentStatus\": -1,\r\n"
					+ "		\"Error\": {\r\n"
					+ "			\"ErrorCode\": -1,\r\n"
					+ "			\"ErrorMessage\": \"The order has been cancelled.\"\r\n"
					+ "		}\r\n"
					+ "	}\r\n"
					+ "}";
		}

	}

	private String busBookkMethodAndBookingDetails(BusOrder order) 
			throws MalformedURLException, IOException {
		
		List<String> strPaxs = new ArrayList<String>();
		Bus bus = order.getBus();
		Integer count = 0;
		
        
        for (BusPassenger pax : bus.getBusPassengers()) {
        	BusSeat seat = busService.findByIdSeat(pax.getSeatId());
        	count++;
        	boolean isLead = false;
        	
        	if (pax.getAge() >= 18) {
				if (count == 1) {
					isLead = true;
				} else {
					isLead = false;
				}
			} else {
				isLead = false;
			}
        	
			String busPaxDetail = "    {\r\n"
	        		+ "      \"LeadPassenger\": " + isLead + ",\r\n"
	        		+ "      \"PassengerId\": 0,\r\n"
	        		+ "      \"Title\": \"" + pax.getTitle() + "\",\r\n"
	        		+ "      \"Address\": \"" + pax.getAddress() + "\",\r\n"
	        		+ "      \"Age\": " + pax.getAge() + ",\r\n"
	        		+ "      \"Email\": \"" + pax.getEmail() + "\",\r\n"
	        		+ "      \"FirstName\": \"" + pax.getFirstName() + "\",\r\n"
	    	        + "      \"Gender\": " + pax.getGender() + ",\r\n"
	    	    	+ "      \"IdNumber\": null,\r\n"
	    	    	+ "      \"IdType\": null,\r\n"
	    	    	+ "      \"LastName\": \"" + pax.getLastName() + "\",\r\n"
	    	    	+ "      \"Phoneno\": \"" + pax.getPhoneNo() + "\",\r\n"
	        		+ "      \"Seat\": {\r\n"
	        		+ "        \"ColumnNo\": \"" + seat.getColumnNo() + "\",\r\n"
	        		+ "        \"Height\": " + seat.getHeight() + ",\r\n"
	        		+ "        \"IsLadiesSeat\": " + seat.isLadiesSeat() + ",\r\n"
	        		+ "        \"IsMalesSeat\": " + seat.isMalesSeat() + ",\r\n"
	        		+ "        \"IsUpper\": false,\r\n"
	        		+ "        \"RowNo\": \"" + seat.getRowNo() + "\",\r\n"
	        		+ "        \"SeatIndex\": \"" + seat.getSeatIndex() + "\",\r\n"
	        		+ "        \"SeatName\": \"" + seat.getSeatName() + "\",\r\n"
	        		+ "        \"SeatStatus\": " + seat.isSeatStatus() + ",\r\n"
	        		+ "        \"SeatType\": " + seat.getSeatType() + ",\r\n"
	        		+ "        \"Width\": " + seat.getWidth() + ",\r\n"
	        		+ "        \"Price\": {\r\n"
	        		+ "          \"CurrencyCode\": \"INR\",\r\n"
	        		+ "          \"BasePrice\": " + seat.getBasePrice() + ",\r\n"
	        		+ "          \"Tax\": " + seat.getTax() + ",\r\n"
	        		+ "          \"OtherCharges\": " + seat.getOtherCharges() + ",\r\n"
	        		+ "          \"Discount\": " + seat.getDiscount() + ",\r\n"
	        		+ "          \"PublishedPrice\": " + seat.getPublishedPrice() + ",\r\n"
	        		+ "          \"PublishedPriceRoundedOff\": " + seat.getPublishedPriceRoundedOff() + ",\r\n"
	        		+ "          \"OfferedPrice\": " + seat.getOfferedPrice() + ",\r\n"
	        		+ "          \"OfferedPriceRoundedOff\": " + seat.getOfferedPriceRoundedOff() + ",\r\n"
	        		+ "          \"AgentCommission\": " + seat.getAgentCommission() + ",\r\n"
	        		+ "          \"AgentMarkUp\": " + seat.getAgentMarkUp() + ",\r\n"
	        		+ "          \"TDS\": " + seat.getTds() + ",\r\n"
	        		+ "          \"GST\": {\r\n"
	        		+ "            \"CGSTAmount\": " + seat.getcGSTAmount() + ",\r\n"
	        		+ "            \"CGSTRate\": " + seat.getcGSTRate() + ",\r\n"
	        		+ "            \"CessAmount\": " + seat.getCessAmount() + ",\r\n"
	        		+ "            \"CessRate\": " + seat.getCessRate() + ",\r\n"
	        		+ "            \"IGSTAmount\": " + seat.getiGSTAmount() + ",\r\n"
	        		+ "            \"IGSTRate\": " + seat.getiGSTRate() + ",\r\n"
	        		+ "            \"SGSTAmount\": " + seat.getsGSTAmount() + ",\r\n"
	        		+ "            \"SGSTRate\": " + seat.getsGSTRate() + ",\r\n"
	        		+ "            \"TaxableAmount\": " + seat.getTaxableAmount() + "\r\n"
	        		+ "          }\r\n"
	        		+ "        }\r\n"
	        		+ "      }\r\n"
	        		+ "    }\r\n";
			
			strPaxs.add(busPaxDetail);
		}
       	
       	String arraySeat = strPaxs.stream().map(val -> String.valueOf(val)).collect(Collectors.joining(",", "[", "]"));
       
        StringBuilder responseBodyBusBook = onlineBusService.apiOnlineBusBook(arraySeat, bus.getResultIndex(), bus.getBusBoardingPointDetails().get(0).getCityPointIndex(), 
        		bus.getBusDroppingPointDetails().get(0).getCityPointIndex());;
        
        JSONObject jsonObjBook = new JSONObject(responseBodyBusBook.toString());
        System.out.println(jsonObjBook);
        logService.generateLog(jsonObjBook.toString());
        
        try {
        	JSONObject jsonObj = jsonObjBook.getJSONObject("BookResult");
        	
        	String busBookingStatus = jsonObj.get("BusBookingStatus").toString();
        	double invoiceAmount = Double.parseDouble(jsonObj.get("InvoiceAmount").toString());
        	String invoiceNumber = jsonObj.get("InvoiceNumber").toString();
        	Integer busId = Integer.parseInt(jsonObj.get("BusId").toString());
        	String ticketNo = jsonObj.get("TicketNo").toString();
        	String travelOperatorPNR = jsonObj.get("TravelOperatorPNR").toString();
        	
        	order.setBusBookingStatus(busBookingStatus);
        	order.setInvoiceAmount("" + invoiceAmount);
        	order.setInvoiceNumber(invoiceNumber);
        	order.setTboBookBusId(busId);
        	order.setTicketNo(ticketNo);
        	order.setTravelOperatorPNR(travelOperatorPNR);
        	
        	busService.saveOrder(order);

    		
            StringBuilder responseBodyBusBookingDetails = onlineBusService.apiOnlineBusBookingDetails(busId);;

            JSONObject jsonObjBookingDetails = new JSONObject(responseBodyBusBookingDetails.toString());
            System.out.println(jsonObjBookingDetails);
            logService.generateLog(jsonObjBookingDetails.toString());
            try {
            	busService.updateOrderStatus(order.getId(), OrderStatus.SUCCESSFULL);

            	return responseBodyBusBookingDetails.toString();
				
			} catch (Exception e) {
				e.printStackTrace();
            	return responseBodyBusBookingDetails.toString();
        		
			}
    		
		} catch (Exception e) {
			e.printStackTrace();
			
			return 	"{\r\n"
			+ "	\"Response\": {\r\n"
			+ "		\"ResponseStatus\": -1,\r\n"
			+ "		\"CurrentStatus\": -1,\r\n"
			+ "		\"Error\": {\r\n"
			+ "			\"ErrorCode\": -1,\r\n"
			+ "			\"ErrorMessage\": \"The order has been cancelled.\"\r\n"
			+ "		}\r\n"
			+ "	}\r\n"
			+ "}";
		}
	}

	// Optional: remove tab and other control characters from cityName
	private String sanitize(String input) {
	    return input == null ? "" : input.replaceAll("[\\x00-\\x1F]", " ");
	}
	
	// POJO List
	
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

	public static class BusBookingRequest {

	    private Integer resultIndex;
	    private Integer historyId;
	    private Integer customerId;
	    private String cityIdOne;
	    private String cityIdTwo;
	    private String deptDate;
	    public String arrivalTime;
        public String departureTime;
        public String routeId;
        public String busType;
        public String serviceName;
        public String travelName;
        public String currencyCode;
        public boolean idProofRequired;
        public boolean isDropPointMandatory;
        public boolean liveTrackingAvailable;
        public boolean mTicketEnabled;
        public boolean partialCancellationAllowed;
        public int maxSeatsPerTicket;
        public int operatorId;
        public double tax;
        public double discount;
        public double publishedPrice;
        public double otherCharges;
        public double offeredPrice;
        public double publishedPriceRoundedOff;
        public double offeredPriceRoundedOff;
        public double agentCommission;
        public double agentMarkUp;
        public double basePrice;
        public double tds;
        public double cGSTAmount;
        public double cGSTRate;
        public double cessAmount;
        public double cessRate;
        public double iGSTAmount;
        public double iGSTRate;
        public double sGSTAmount;
        public double sGSTRate;
        public double taxableAmount;        
        public int availableSeats;
        public List<CancelPolicy> busCancelPolicies;
        public List<PointDetails> pointsDetails;

	    public BusBookingRequest() {}

	    public Integer getResultIndex() {
			return resultIndex;
		}

		public void setResultIndex(Integer resultIndex) {
			this.resultIndex = resultIndex;
		}

		public Integer getCustomerId() {
			return customerId;
		}

		public void setCustomerId(Integer customerId) {
			this.customerId = customerId;
		}

	    public Integer getHistoryId() {
	        return historyId;
	    }

	    public void setHistoryId(Integer historyId) {
	        this.historyId = historyId;
	    }

	    public String getCityIdOne() {
	        return cityIdOne;
	    }

	    public void setCityIdOne(String cityIdOne) {
	        this.cityIdOne = cityIdOne;
	    }

	    public String getCityIdTwo() {
	        return cityIdTwo;
	    }

	    public void setCityIdTwo(String cityIdTwo) {
	        this.cityIdTwo = cityIdTwo;
	    }

	    public String getDeptDate() {
	        return deptDate;
	    }

	    public void setDeptDate(String deptDate) {
	        this.deptDate = deptDate;
	    }

		public String getArrivalTime() {
			return arrivalTime;
		}

		public void setArrivalTime(String arrivalTime) {
			this.arrivalTime = arrivalTime;
		}

		public String getDepartureTime() {
			return departureTime;
		}

		public void setDepartureTime(String departureTime) {
			this.departureTime = departureTime;
		}

		public String getRouteId() {
			return routeId;
		}

		public void setRouteId(String routeId) {
			this.routeId = routeId;
		}

		public String getBusType() {
			return busType;
		}

		public void setBusType(String busType) {
			this.busType = busType;
		}

		public String getServiceName() {
			return serviceName;
		}

		public void setServiceName(String serviceName) {
			this.serviceName = serviceName;
		}

		public String getTravelName() {
			return travelName;
		}

		public void setTravelName(String travelName) {
			this.travelName = travelName;
		}

		public String getCurrencyCode() {
			return currencyCode;
		}

		public void setCurrencyCode(String currencyCode) {
			this.currencyCode = currencyCode;
		}

		public boolean isIdProofRequired() {
			return idProofRequired;
		}

		public void setIdProofRequired(boolean idProofRequired) {
			this.idProofRequired = idProofRequired;
		}

		public boolean isDropPointMandatory() {
			return isDropPointMandatory;
		}

		public void setDropPointMandatory(boolean isDropPointMandatory) {
			this.isDropPointMandatory = isDropPointMandatory;
		}

		public boolean isLiveTrackingAvailable() {
			return liveTrackingAvailable;
		}

		public void setLiveTrackingAvailable(boolean liveTrackingAvailable) {
			this.liveTrackingAvailable = liveTrackingAvailable;
		}

		public boolean ismTicketEnabled() {
			return mTicketEnabled;
		}

		public void setmTicketEnabled(boolean mTicketEnabled) {
			this.mTicketEnabled = mTicketEnabled;
		}

		public boolean isPartialCancellationAllowed() {
			return partialCancellationAllowed;
		}

		public void setPartialCancellationAllowed(boolean partialCancellationAllowed) {
			this.partialCancellationAllowed = partialCancellationAllowed;
		}

		public int getMaxSeatsPerTicket() {
			return maxSeatsPerTicket;
		}

		public void setMaxSeatsPerTicket(int maxSeatsPerTicket) {
			this.maxSeatsPerTicket = maxSeatsPerTicket;
		}

		public int getOperatorId() {
			return operatorId;
		}

		public void setOperatorId(int operatorId) {
			this.operatorId = operatorId;
		}

		public double getTax() {
			return tax;
		}

		public void setTax(double tax) {
			this.tax = tax;
		}

		public double getDiscount() {
			return discount;
		}

		public void setDiscount(double discount) {
			this.discount = discount;
		}

		public double getPublishedPrice() {
			return publishedPrice;
		}

		public void setPublishedPrice(double publishedPrice) {
			this.publishedPrice = publishedPrice;
		}

		public double getOtherCharges() {
			return otherCharges;
		}

		public void setOtherCharges(double otherCharges) {
			this.otherCharges = otherCharges;
		}

		public double getOfferedPrice() {
			return offeredPrice;
		}

		public void setOfferedPrice(double offeredPrice) {
			this.offeredPrice = offeredPrice;
		}

		public double getPublishedPriceRoundedOff() {
			return publishedPriceRoundedOff;
		}

		public void setPublishedPriceRoundedOff(double publishedPriceRoundedOff) {
			this.publishedPriceRoundedOff = publishedPriceRoundedOff;
		}

		public double getOfferedPriceRoundedOff() {
			return offeredPriceRoundedOff;
		}

		public void setOfferedPriceRoundedOff(double offeredPriceRoundedOff) {
			this.offeredPriceRoundedOff = offeredPriceRoundedOff;
		}

		public double getAgentCommission() {
			return agentCommission;
		}

		public void setAgentCommission(double agentCommission) {
			this.agentCommission = agentCommission;
		}

		public double getAgentMarkUp() {
			return agentMarkUp;
		}

		public void setAgentMarkUp(double agentMarkUp) {
			this.agentMarkUp = agentMarkUp;
		}

		public double getBasePrice() {
			return basePrice;
		}

		public void setBasePrice(double basePrice) {
			this.basePrice = basePrice;
		}

		public double getTds() {
			return tds;
		}

		public void setTds(double tds) {
			this.tds = tds;
		}

		public double getcGSTAmount() {
			return cGSTAmount;
		}

		public void setcGSTAmount(double cGSTAmount) {
			this.cGSTAmount = cGSTAmount;
		}

		public double getcGSTRate() {
			return cGSTRate;
		}

		public void setcGSTRate(double cGSTRate) {
			this.cGSTRate = cGSTRate;
		}

		public double getCessAmount() {
			return cessAmount;
		}

		public void setCessAmount(double cessAmount) {
			this.cessAmount = cessAmount;
		}

		public double getCessRate() {
			return cessRate;
		}

		public void setCessRate(double cessRate) {
			this.cessRate = cessRate;
		}

		public double getiGSTAmount() {
			return iGSTAmount;
		}

		public void setiGSTAmount(double iGSTAmount) {
			this.iGSTAmount = iGSTAmount;
		}

		public double getiGSTRate() {
			return iGSTRate;
		}

		public void setiGSTRate(double iGSTRate) {
			this.iGSTRate = iGSTRate;
		}

		public double getsGSTAmount() {
			return sGSTAmount;
		}

		public void setsGSTAmount(double sGSTAmount) {
			this.sGSTAmount = sGSTAmount;
		}

		public double getsGSTRate() {
			return sGSTRate;
		}

		public void setsGSTRate(double sGSTRate) {
			this.sGSTRate = sGSTRate;
		}

		public double getTaxableAmount() {
			return taxableAmount;
		}

		public void setTaxableAmount(double taxableAmount) {
			this.taxableAmount = taxableAmount;
		}

		public int getAvailableSeats() {
			return availableSeats;
		}

		public void setAvailableSeats(int availableSeats) {
			this.availableSeats = availableSeats;
		}

		public List<CancelPolicy> getBusCancelPolicies() {
			return busCancelPolicies;
		}

		public void setBusCancelPolicies(List<CancelPolicy> busCancelPolicies) {
			this.busCancelPolicies = busCancelPolicies;
		}

		public List<PointDetails> getPointsDetails() {
			return pointsDetails;
		}

		public void setPointsDetails(List<PointDetails> pointsDetails) {
			this.pointsDetails = pointsDetails;
		}

	    
	}
	
	public static class CancelPolicy {
        public double cancellationCharge;
        public String cancellationChargeType;
        public String policyString;
        public String timeBeforeDept;
        public String fromDate;
        public String toDate;
        
		public CancelPolicy() {}

		public double getCancellationCharge() {
			return cancellationCharge;
		}

		public void setCancellationCharge(double cancellationCharge) {
			this.cancellationCharge = cancellationCharge;
		}

		public String getCancellationChargeType() {
			return cancellationChargeType;
		}

		public void setCancellationChargeType(String cancellationChargeType) {
			this.cancellationChargeType = cancellationChargeType;
		}

		public String getPolicyString() {
			return policyString;
		}

		public void setPolicyString(String policyString) {
			this.policyString = policyString;
		}

		public String getTimeBeforeDept() {
			return timeBeforeDept;
		}

		public void setTimeBeforeDept(String timeBeforeDept) {
			this.timeBeforeDept = timeBeforeDept;
		}

		public String getFromDate() {
			return fromDate;
		}

		public void setFromDate(String fromDate) {
			this.fromDate = fromDate;
		}

		public String getToDate() {
			return toDate;
		}

		public void setToDate(String toDate) {
			this.toDate = toDate;
		}
        
        
    }

    public static class PointDetails {
        public int cityPointIndex;
        public String cityPointLocation;
        public String cityPointName;
        public String cityPointTime;
        public String pointType;
        
		public PointDetails() {}

		public int getCityPointIndex() {
			return cityPointIndex;
		}

		public void setCityPointIndex(int cityPointIndex) {
			this.cityPointIndex = cityPointIndex;
		}

		public String getCityPointLocation() {
			return cityPointLocation;
		}

		public void setCityPointLocation(String cityPointLocation) {
			this.cityPointLocation = cityPointLocation;
		}

		public String getCityPointName() {
			return cityPointName;
		}

		public void setCityPointName(String cityPointName) {
			this.cityPointName = cityPointName;
		}

		public String getCityPointTime() {
			return cityPointTime;
		}

		public void setCityPointTime(String cityPointTime) {
			this.cityPointTime = cityPointTime;
		}

		public String getPointType() {
			return pointType;
		}

		public void setPointType(String pointType) {
			this.pointType = pointType;
		}
        
        
    }

    public static class PassengerRequest {

        private String title;
        private String fName;
        private String lName;
        private String email;
        private String phoneNo;
        private Integer age;
        private Integer gender;
        private String pan;
        private Integer seatId;
        private String address;

        public PassengerRequest() {
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getfName() {
			return fName;
		}

		public void setfName(String fName) {
			this.fName = fName;
		}

		public String getlName() {
			return lName;
		}

		public void setlName(String lName) {
			this.lName = lName;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getPhoneNo() {
			return phoneNo;
		}

		public void setPhoneNo(String phoneNo) {
			this.phoneNo = phoneNo;
		}

		public Integer getAge() {
			return age;
		}

		public void setAge(Integer age) {
			this.age = age;
		}

		public Integer getGender() {
			return gender;
		}

		public void setGender(Integer gender) {
			this.gender = gender;
		}

		public String getPan() {
			return pan;
		}

		public void setPan(String pan) {
			this.pan = pan;
		}

		public Integer getSeatId() {
			return seatId;
		}

		public void setSeatId(Integer seatId) {
			this.seatId = seatId;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}


    }
    
    public static class SeatSelectionRequest {
        private String seatIndex;
        private String columnNo;
        private String rowNo;
        private int height;
        private int width;
        private int seatType;
        private String seatName;
        private double seatFare;
        private boolean isLadiesSeat;
        private boolean isMalesSeat;
        private boolean seatStatus;
        private double basePrice;
        private double publishedPrice;
        private double offeredPrice;
        private double publishedPriceRoundedOff;
        private double offeredPriceRoundedOff;
        private double agentCommission;
        private double agentMarkUp;
        private double tds;
        private double cGSTAmount;
        private double cGSTRate;
        private double cessAmount;
        private double cessRate;
        private double iGSTAmount;
        private double iGSTRate;
        private double sGSTAmount;
        private double sGSTRate;
        private double taxableAmount;
        private double tax;
        private double discount;
        private double otherCharges;
        
        // Constructor
        public SeatSelectionRequest() {
        }

        
		public double getBasePrice() {
			return basePrice;
		}


		public void setBasePrice(double basePrice) {
			this.basePrice = basePrice;
		}


		public double getPublishedPrice() {
			return publishedPrice;
		}


		public void setPublishedPrice(double publishedPrice) {
			this.publishedPrice = publishedPrice;
		}


		public double getOfferedPrice() {
			return offeredPrice;
		}


		public void setOfferedPrice(double offeredPrice) {
			this.offeredPrice = offeredPrice;
		}


		public double getPublishedPriceRoundedOff() {
			return publishedPriceRoundedOff;
		}


		public void setPublishedPriceRoundedOff(double publishedPriceRoundedOff) {
			this.publishedPriceRoundedOff = publishedPriceRoundedOff;
		}


		public double getOfferedPriceRoundedOff() {
			return offeredPriceRoundedOff;
		}


		public void setOfferedPriceRoundedOff(double offeredPriceRoundedOff) {
			this.offeredPriceRoundedOff = offeredPriceRoundedOff;
		}


		public double getAgentCommission() {
			return agentCommission;
		}


		public void setAgentCommission(double agentCommission) {
			this.agentCommission = agentCommission;
		}


		public double getAgentMarkUp() {
			return agentMarkUp;
		}


		public void setAgentMarkUp(double agentMarkUp) {
			this.agentMarkUp = agentMarkUp;
		}


		public double getTds() {
			return tds;
		}


		public void setTds(double tds) {
			this.tds = tds;
		}


		public double getcGSTAmount() {
			return cGSTAmount;
		}


		public void setcGSTAmount(double cGSTAmount) {
			this.cGSTAmount = cGSTAmount;
		}


		public double getcGSTRate() {
			return cGSTRate;
		}


		public void setcGSTRate(double cGSTRate) {
			this.cGSTRate = cGSTRate;
		}


		public double getCessAmount() {
			return cessAmount;
		}


		public void setCessAmount(double cessAmount) {
			this.cessAmount = cessAmount;
		}


		public double getCessRate() {
			return cessRate;
		}


		public void setCessRate(double cessRate) {
			this.cessRate = cessRate;
		}


		public double getiGSTAmount() {
			return iGSTAmount;
		}


		public void setiGSTAmount(double iGSTAmount) {
			this.iGSTAmount = iGSTAmount;
		}


		public double getiGSTRate() {
			return iGSTRate;
		}


		public void setiGSTRate(double iGSTRate) {
			this.iGSTRate = iGSTRate;
		}


		public double getsGSTAmount() {
			return sGSTAmount;
		}


		public void setsGSTAmount(double sGSTAmount) {
			this.sGSTAmount = sGSTAmount;
		}


		public double getsGSTRate() {
			return sGSTRate;
		}


		public void setsGSTRate(double sGSTRate) {
			this.sGSTRate = sGSTRate;
		}


		public double getTaxableAmount() {
			return taxableAmount;
		}


		public void setTaxableAmount(double taxableAmount) {
			this.taxableAmount = taxableAmount;
		}


		public double getTax() {
			return tax;
		}


		public void setTax(double tax) {
			this.tax = tax;
		}


		public double getDiscount() {
			return discount;
		}


		public void setDiscount(double discount) {
			this.discount = discount;
		}


		public double getOtherCharges() {
			return otherCharges;
		}


		public void setOtherCharges(double otherCharges) {
			this.otherCharges = otherCharges;
		}


		public String getSeatIndex() {
			return seatIndex;
		}

		public void setSeatIndex(String seatIndex) {
			this.seatIndex = seatIndex;
		}

		public String getColumnNo() {
			return columnNo;
		}

		public void setColumnNo(String columnNo) {
			this.columnNo = columnNo;
		}

		public String getRowNo() {
			return rowNo;
		}

		public void setRowNo(String rowNo) {
			this.rowNo = rowNo;
		}

		public int getHeight() {
			return height;
		}

		public void setHeight(int height) {
			this.height = height;
		}

		public int getWidth() {
			return width;
		}

		public void setWidth(int width) {
			this.width = width;
		}

		public int getSeatType() {
			return seatType;
		}

		public void setSeatType(int seatType) {
			this.seatType = seatType;
		}

		public String getSeatName() {
			return seatName;
		}

		public void setSeatName(String seatName) {
			this.seatName = seatName;
		}

		public double getSeatFare() {
			return seatFare;
		}

		public void setSeatFare(double seatFare) {
			this.seatFare = seatFare;
		}

		public boolean isLadiesSeat() {
			return isLadiesSeat;
		}

		public void setLadiesSeat(boolean isLadiesSeat) {
			this.isLadiesSeat = isLadiesSeat;
		}

		public boolean isMalesSeat() {
			return isMalesSeat;
		}

		public void setMalesSeat(boolean isMalesSeat) {
			this.isMalesSeat = isMalesSeat;
		}

		public boolean isSeatStatus() {
			return seatStatus;
		}

		public void setSeatStatus(boolean seatStatus) {
			this.seatStatus = seatStatus;
		}

    }

	public static class SeatAndPassengerRequest {
        private Integer busId;
        private Integer custId;
        private List<PassengerRequest> passengers;
        private List<SeatSelectionRequest> seats;
        
		public SeatAndPassengerRequest() {}

		public List<PassengerRequest> getPassengers() {
			return passengers;
		}

		public void setPassengers(List<PassengerRequest> passengers) {
			this.passengers = passengers;
		}

		public List<SeatSelectionRequest> getSeats() {
			return seats;
		}

		public void setSeats(List<SeatSelectionRequest> seats) {
			this.seats = seats;
		}

		public Integer getBusId() {
			return busId;
		}

		public void setBusId(Integer busId) {
			this.busId = busId;
		}

		public Integer getCustId() {
			return custId;
		}

		public void setCustId(Integer custId) {
			this.custId = custId;
		}
		
	}
    
	public static class BusOrderRequest {
		private Integer busId;
		private Integer custId;
		private Integer searchId;
		
		public BusOrderRequest() {}
		
		public Integer getBusId() {
			return busId;
		}
		public void setBusId(Integer busId) {
			this.busId = busId;
		}
		public Integer getCustId() {
			return custId;
		}
		public void setCustId(Integer custId) {
			this.custId = custId;
		}
		public Integer getSearchId() {
			return searchId;
		}
		public void setSearchId(Integer searchId) {
			this.searchId = searchId;
		}
	}
	
	public static class BusOrderSaveRequest {
		private Integer busId;
		private Integer orderId;
		
		public BusOrderSaveRequest() {}
		
		public Integer getBusId() {
			return busId;
		}
		public void setBusId(Integer busId) {
			this.busId = busId;
		}

		public Integer getOrderId() {
			return orderId;
		}

		public void setOrderId(Integer orderId) {
			this.orderId = orderId;
		}
		
	}

}
