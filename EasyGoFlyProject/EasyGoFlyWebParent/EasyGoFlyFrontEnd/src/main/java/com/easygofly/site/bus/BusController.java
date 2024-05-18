package com.easygofly.site.bus;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
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

import com.easygofly.entity.Bus;
import com.easygofly.entity.BusBoardingPointDetails;
import com.easygofly.entity.BusCancelPolicy;
import com.easygofly.entity.BusHistory;
import com.easygofly.entity.BusPointDetails;
import com.easygofly.entity.BusSeat;
import com.easygofly.entity.Customer;
import com.easygofly.entity.TBObusCity;
import com.easygofly.site.LogService;
import com.easygofly.site.customer.CustomerService;
import com.easygofly.site.security.EasegoflyPhoneCustomerDetails;

@Controller
public class BusController {

	@Autowired private BusCityRepository busCityRepo;
	@Autowired private BusService busService;
	@Autowired private CustomerService customerService;
	@Autowired private OnlineBusService onlineBusService;
	@Autowired private LogService logService;

	private String searchURL = "";
	private String bookingURL = "";
//	private String orderURL = "";
	
	BusHistory history = new BusHistory();
	List<Bus> buses = new ArrayList<>();
	List<BusSeat> seatList = new ArrayList<>();
	
	@GetMapping("/bus")
	public String viewBusPage(Model model) {
		cityFinder(model);
		
		busService.authenticationBus(model);
		
		return "bus/bus";
	}
	
	@PostMapping("/bus/saveSearchBus")
	public String saveSearchBus(@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer,
			@RequestParam(name = "busDeparture", required = false) String busDeparture, 
			@RequestParam(name = "deptTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date deptTime, 
			@RequestParam(name = "busDestination", required = false) String busDestination) {

		TBObusCity cityOne = busCityRepo.getCityByCityName(busDeparture);
		TBObusCity cityTwo = busCityRepo.getCityByCityName(busDestination);
	    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	    
	    if (loggedCustomer != null) {
			String email = loggedCustomer.getUsername();
			Customer customer = customerService.getByPhone(email);	
			BusHistory newHistory = new BusHistory();

			newHistory.setDeptDate(deptTime);
			newHistory.setCityIdOne(cityOne.getCityId().toString());
			newHistory.setCityIdTwo(cityTwo.getCityId().toString());
			newHistory.setCustomer(customer);
			
			history = busService.saveBusHistory(newHistory, customer);
			
		} else {
			
		}
	    
	    searchURL = "/bus/search_" + busDeparture + "_" + busDestination + "_" + dateFormat.format(deptTime);
	    
		return "redirect:/bus_loading...";

	}

	@GetMapping("/bus_loading...")
    public String performApiRequest(Model model) {
        model.addAttribute("searchURL", searchURL);
        return "loading/loading";
    }

	@GetMapping("/bus/search_{cityIdOne}_{cityIdTwo}_{deptDate}")
	public String viewBusSearchResult(Model model,
			@PathVariable(name = "cityIdOne") String cityIdOne,
			@PathVariable(name = "cityIdTwo") String cityIdTwo,
			@PathVariable(name = "deptDate") String deptDate) throws Exception {

		TBObusCity cityTwo = busCityRepo.getCityByCityName(cityIdTwo);
		TBObusCity cityOne = busCityRepo.getCityByCityName(cityIdOne);
		
		cityFinder(model);
		buses = new ArrayList<Bus>();
		
	    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	    Date deptTime = dateFormat.parse(deptDate);
			
		// Create URL object with the API end-point
        URL urlSearch = new URL("http://api.tektravels.com/BookingEngineService_Bus/Busservice.svc/rest/Search");

        // Open a connection
        HttpURLConnection connectionSearch = (HttpURLConnection) urlSearch.openConnection();
       
        StringBuilder responseBodySearch = new StringBuilder();
       
        onlineBusService.apiOnlineSearchBus(connectionSearch, responseBodySearch, cityOne.getCityId().toString(), cityTwo.getCityId().toString(), deptTime);
		
        JSONObject jsonObjSearch = new JSONObject(responseBodySearch.toString());
        System.out.println(jsonObjSearch);
        logService.generateLog(jsonObjSearch.toString());
        try {
			JSONArray jsonArrays = jsonObjSearch.getJSONObject("BusSearchResult").getJSONArray("BusResults");
			JSONObject mainObj = new JSONObject();
			
			onlineBusService.traceId = jsonObjSearch.getJSONObject("BusSearchResult").get("TraceId").toString();
			String destination = jsonObjSearch.getJSONObject("BusSearchResult").get("Destination").toString();
			String origin = jsonObjSearch.getJSONObject("BusSearchResult").get("Origin").toString();
			Integer responseStatus =Integer.parseInt(jsonObjSearch.getJSONObject("BusSearchResult").get("ResponseStatus").toString());
			
			for (int i = 0; i < jsonArrays.length(); i++) {
			    mainObj.put("Bus-" + i, jsonArrays.getJSONObject(i));
			    
			    List<BusPointDetails> boardingPointList = new ArrayList<BusPointDetails>();
			    List<BusPointDetails> droppingPointList = new ArrayList<BusPointDetails>();
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
						System.out.println("jsonArrayBoardingPoints.length() " + jsonArrayBoardingPoints.getJSONObject(j).get("CityPointIndex").toString());

						Integer cityPointIndex = Integer.parseInt(jsonArrayBoardingPoints.getJSONObject(j).get("CityPointIndex").toString());
						String cityPointLocation = jsonArrayBoardingPoints.getJSONObject(j).get("CityPointLocation").toString();
						String cityPointName = jsonArrayBoardingPoints.getJSONObject(j).get("CityPointName").toString();
						String cityPointTime = jsonArrayBoardingPoints.getJSONObject(j).get("CityPointTime").toString();
						
						BusPointDetails newBording = new BusPointDetails(cityPointIndex, cityPointLocation, cityPointName, cityPointTime);
						boardingPointList.add(newBording);
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
						
						BusPointDetails newDropping = new BusPointDetails(cityPointIndex, cityPointLocation, cityPointName, cityPointTime);
						droppingPointList.add(newDropping);
					
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
					// TODO: handle exception
//					e.printStackTrace();
				}
				
				

				Bus newBus = new Bus(resultIndex, arrivalTime, departureTime, routeId, busType, serviceName, travelName, "INR", idProofRequired, isDropPointMandatory, 
						liveTrackingAvailable, mTicketEnabled, partialCancellationAllowed, maxSeatsPerTicket, operatorId, tax, discount, publishedPrice, otherCharges, offeredPrice, 
						publishedPriceRoundedOff, offeredPriceRoundedOff, agentCommission, agentMarkUp, basePrice, tDS, cGSTAmount, cGSTRate, cessAmount, cessRate, iGSTAmount, iGSTRate, sGSTAmount, sGSTRate, 
						taxableAmount, availableSeats, null, cancellationPolicieList, boardingPointList, droppingPointList);
				
				buses.add(newBus);
			}
			

			model.addAttribute("responseStatus", responseStatus);
			model.addAttribute("destination", destination);
			model.addAttribute("origin", origin);
			
			
		} catch (Exception e) {
//			JSONObject jsonObj = jsonObjSearch.getJSONObject("BusSearchResult").getJSONObject("Error");
//			String errorCode = jsonObj.get("ErrorCode").toString();
//			String errorMessage = jsonObj.get("ErrorMessage").toString();
			
			e.printStackTrace();
		}

		model.addAttribute("cityTwo", cityTwo);
		model.addAttribute("cityOne", cityOne);
		model.addAttribute("deptTime", deptTime);
		model.addAttribute("busList", buses);
		
		return "bus/search/bus-search-result";
	}

	@PostMapping("/hotel/saveBus")
	public String saveBus(@RequestParam(name = "resultIndex", required = false) String resultIndex,
			@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer,
			@RequestParam(name = "busDeparture", required = false) String busDeparture, 
			@RequestParam(name = "deptTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date deptTime, 
			@RequestParam(name = "busDestination", required = false) String busDestination) throws Exception {
		
		TBObusCity cityOne = busCityRepo.getCityByCityName(busDeparture);
		TBObusCity cityTwo = busCityRepo.getCityByCityName(busDestination);
	    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		Customer customer = customerService.getByPhone(loggedCustomer.getUsername());	
		BusHistory newHistory = new BusHistory();
		
		if (history == null) {

			newHistory.setDeptDate(deptTime);
			newHistory.setCityIdOne(cityOne.getCityId().toString());
			newHistory.setCityIdTwo(cityTwo.getCityId().toString());
			newHistory.setCustomer(customer);
			
			history = busService.saveBusHistory(newHistory, customer);
		}
	
		Bus savedBus = new Bus();

	    for (Bus bus : buses) {
			if (bus.getResultIndex() == Integer.parseInt(resultIndex)) {
				savedBus = busService.saveBus(bus, customer);
			}
		}
	    
		bookingURL = "/bus/booking_" + savedBus.getId() + "_" + history.getId() + "_" + busDeparture + "_" + busDestination + "_" + dateFormat.format(deptTime); 
		
		return "redirect:/bus_booking...";
	}

	@GetMapping("/bus_booking...")
    public String performApiLoadBooking(Model model) {
        model.addAttribute("searchURL", bookingURL);
        return "loading/loading";
    }

	@GetMapping("/bus/booking_{busId}_{history_id}_{cityIdOne}_{cityIdTwo}_{deptDate}")
	public String busBooking(Model model, 
			@PathVariable(name = "busId") Integer busId,
			@PathVariable(name = "history_id") Integer history_id,
			@PathVariable(name = "cityIdOne") String cityIdOne,
			@PathVariable(name = "cityIdTwo") String cityIdTwo,
			@PathVariable(name = "deptDate") String deptDate,
			@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer) throws Exception {

		Bus bus = busService.findByIdBus(busId);
		TBObusCity cityOne = busCityRepo.getCityByCityName(cityIdOne);
		TBObusCity cityTwo = busCityRepo.getCityByCityName(cityIdTwo);
        Customer customer = customerService.getByPhone(loggedCustomer.getUsername());
       
	    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	    Date checkIn = dateFormat.parse(deptDate);
  
	    BusHistory busHistory = busService.findByIdBusHistory(history_id);

	    busSeatLayout(model, bus.getResultIndex());
	    busBusBoardingPoint(model, bus.getResultIndex());

		model.addAttribute("checkIn", checkIn);
		model.addAttribute("cityOne", cityOne);
		model.addAttribute("cityTwo", cityTwo);
		model.addAttribute("busHistory", busHistory);
		model.addAttribute("busId", busId);
		model.addAttribute("bus", bus);
		model.addAttribute("custId", customer.getId());
        
		return "hotel/booking/hotel-booking";
	}
	
	
	
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

	private void busSeatLayout(Model model, Integer resultIndex) throws IOException {

		// Create URL object with the API end-point
        URL urlBusSeatLayout = new URL("http://api.tektravels.com/BookingEngineService_Bus/Busservice.svc/rest/GetBusSeatLayOut");

        // Open a connection
        HttpURLConnection connectionBusSeatLayout = (HttpURLConnection) urlBusSeatLayout.openConnection();
       
        StringBuilder responseBodyBusSeatLayout = new StringBuilder();
        
        onlineBusService.apiOnlineBusSeatLayout(connectionBusSeatLayout, responseBodyBusSeatLayout, resultIndex);

        JSONObject jsonObjSeatLayout = new JSONObject(responseBodyBusSeatLayout.toString());
        System.out.println(jsonObjSeatLayout);
        logService.generateLog(jsonObjSeatLayout.toString());
        
        seatList = new ArrayList<BusSeat>();
        
        try {
        	JSONObject jsonObjSeat = jsonObjSeatLayout.getJSONObject("GetBusSeatLayOutResult").getJSONObject("SeatLayoutDetails");
        	JSONArray jsonArraySeatDetails = jsonObjSeat.getJSONObject("SeatLayout").getJSONArray("SeatDetails");
			JSONObject mainObj = new JSONObject();
        	
        	String availableSeats = jsonObjSeat.get("AvailableSeats").toString();
        	String hTMLLayout = jsonObjSeat.get("HTMLLayout").toString();
        	Integer noOfColumns = Integer.parseInt(jsonObjSeat.getJSONObject("SeatLayout").get("NoOfColumns").toString());
        	Integer noOfRows = Integer.parseInt(jsonObjSeat.getJSONObject("SeatLayout").get("NoOfRows").toString());
        	
        	for (int i = 0; i < jsonArraySeatDetails.length(); i++) {
			    mainObj.put("Seat-" + i, jsonArraySeatDetails.getJSONObject(i));
				
			    String columnNo = mainObj.getJSONObject("Seat-" + i).get("ColumnNo").toString();
			    String rowNo = mainObj.getJSONObject("Seat-" + i).get("RowNo").toString();
			    String seatName = mainObj.getJSONObject("Seat-" + i).get("SeatName").toString();
	        	Integer height = Integer.parseInt(mainObj.getJSONObject("Seat-" + i).get("Height").toString());
	        	Integer seatIndex = Integer.parseInt(mainObj.getJSONObject("Seat-" + i).get("SeatIndex").toString());
	        	Integer seatType = Integer.parseInt(mainObj.getJSONObject("Seat-" + i).get("NoOfColumns").toString());
	        	Integer width = Integer.parseInt(mainObj.getJSONObject("Seat-" + i).get("Width").toString());
	        	boolean isLadiesSeat = Boolean.parseBoolean(mainObj.getJSONObject("Seat-" + i).get("IsLadiesSeat").toString());
	        	boolean isMalesSeat = Boolean.parseBoolean(mainObj.getJSONObject("Seat-" + i).get("IsMalesSeat").toString());
	        	boolean seatStatus = Boolean.parseBoolean(mainObj.getJSONObject("Seat-" + i).get("SeatStatus").toString());
	        	double seatFare = Double.parseDouble(mainObj.getJSONObject("Seat-" + i).get("SeatFare").toString());
	        	double BasePrice = Double.parseDouble(mainObj.getJSONObject("Seat-" + i).getJSONObject("Price").get("BasePrice").toString());
			    double tax = Double.parseDouble(mainObj.getJSONObject("Seat-" + i).getJSONObject("Price").get("Tax").toString());
			    double discount = Double.parseDouble(mainObj.getJSONObject("Seat-" + i).getJSONObject("Price").get("Discount").toString());
			    double publishedPrice = Double.parseDouble(mainObj.getJSONObject("Seat-" + i).getJSONObject("Price").get("PublishedPrice").toString());
			    double otherCharges = Double.parseDouble(mainObj.getJSONObject("Seat-" + i).getJSONObject("Price").get("OtherCharges").toString());
			    double offeredPrice = Double.parseDouble(mainObj.getJSONObject("Seat-" + i).getJSONObject("Price").get("OfferedPrice").toString());
			    Integer publishedPriceRoundedOff = Integer.parseInt(mainObj.getJSONObject("Seat-" + i).getJSONObject("Price").get("PublishedPriceRoundedOff").toString());
			    Integer offeredPriceRoundedOff = Integer.parseInt(mainObj.getJSONObject("Seat-" + i).getJSONObject("Price").get("OfferedPriceRoundedOff").toString());
			    double agentCommission = Double.parseDouble(mainObj.getJSONObject("Seat-" + i).getJSONObject("Price").get("AgentCommission").toString());
			    double agentMarkUp = Double.parseDouble(mainObj.getJSONObject("Seat-" + i).getJSONObject("Price").get("AgentMarkUp").toString());
			    double tds = Double.parseDouble(mainObj.getJSONObject("Seat-" + i).getJSONObject("Price").get("TDS").toString());
			    double cGSTAmount = Double.parseDouble(mainObj.getJSONObject("Seat-" + i).getJSONObject("Price").getJSONObject("GST").get("CGSTAmount").toString());
			    double cGSTRate = Double.parseDouble(mainObj.getJSONObject("Seat-" + i).getJSONObject("Price").getJSONObject("GST").get("CGSTRate").toString());
			    double cessAmount = Double.parseDouble(mainObj.getJSONObject("Seat-" + i).getJSONObject("Price").getJSONObject("GST").get("CessAmount").toString());
			    double cessRate = Double.parseDouble(mainObj.getJSONObject("Seat-" + i).getJSONObject("Price").getJSONObject("GST").get("CessRate").toString());
			    double iGSTAmount = Double.parseDouble(mainObj.getJSONObject("Seat-" + i).getJSONObject("Price").getJSONObject("GST").get("IGSTAmount").toString());
			    double iGSTRate = Double.parseDouble(mainObj.getJSONObject("Seat-" + i).getJSONObject("Price").getJSONObject("GST").get("IGSTRate").toString());
			    double sGSTAmount = Double.parseDouble(mainObj.getJSONObject("Seat-" + i).getJSONObject("Price").getJSONObject("GST").get("SGSTAmount").toString());
			    double sGSTRate = Double.parseDouble(mainObj.getJSONObject("Seat-" + i).getJSONObject("Price").getJSONObject("GST").get("SGSTRate").toString());
			    double taxableAmount = Double.parseDouble(mainObj.getJSONObject("Seat-" + i).getJSONObject("Price").getJSONObject("GST").get("TaxableAmount").toString());
			    
			    BusSeat newSeat = new BusSeat(columnNo, rowNo, height, width, seatType, seatName, seatIndex, seatFare, isLadiesSeat, isMalesSeat, seatStatus, 
					   "INR", tax, discount, BasePrice, publishedPrice, otherCharges, offeredPrice, publishedPriceRoundedOff, offeredPriceRoundedOff, 
					   agentCommission, agentMarkUp, tds, cGSTAmount, cGSTRate, cessAmount, cessRate, iGSTAmount, iGSTRate, sGSTAmount, sGSTRate, taxableAmount);
			    
			    seatList.add(newSeat);

			}

			model.addAttribute("seatList", seatList);
			model.addAttribute("availableSeats", availableSeats);
			model.addAttribute("hTMLLayout", hTMLLayout);
			model.addAttribute("noOfColumns", noOfColumns);
			model.addAttribute("noOfRows", noOfRows);
			
        	
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	private void busBusBoardingPoint(Model model, Integer resultIndex) throws IOException {

		// Create URL object with the API end-point
        URL urlBusPointDetail = new URL("http://api.tektravels.com/BookingEngineService_Bus/Busservice.svc/rest/GetBoardingPointDetails");

        // Open a connection
        HttpURLConnection connectionBusPointDetail = (HttpURLConnection) urlBusPointDetail.openConnection();
       
        StringBuilder responseBodyBusPointDetail = new StringBuilder();
        
        onlineBusService.apiOnlineBusSeatLayout(connectionBusPointDetail, responseBodyBusPointDetail, resultIndex);

        JSONObject jsonObjPointDetail = new JSONObject(responseBodyBusPointDetail.toString());
        System.out.println(jsonObjPointDetail);
        logService.generateLog(jsonObjPointDetail.toString());
        
        try {
        	JSONObject jsonObjPoint = jsonObjPointDetail.getJSONObject("GetBusRouteDetailResult");
        	JSONArray jsonArrayBoarding = jsonObjPoint.getJSONArray("BoardingPointsDetails");
        	JSONArray jsonArrayDropping = jsonObjPoint.getJSONArray("DroppingPointsDetails");
			JSONObject mainObjBoarding = new JSONObject();
			JSONObject mainObjDropping = new JSONObject();

			List<BusPointDetails> busPointDetails = new ArrayList<BusPointDetails>();
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
			    boardingPointDetails.add(newPoint);
			}
        	
        	for (int i = 0; i < jsonArrayDropping.length(); i++) {
        		mainObjDropping.put("Point-" + i, jsonArrayDropping.getJSONObject(i));
        		
			    Integer CityPointIndex = Integer.parseInt(mainObjBoarding.getJSONObject("Point-" + i).get("CityPointIndex").toString());
			    String CityPointLocation = mainObjBoarding.getJSONObject("Point-" + i).get("CityPointLocation").toString();
			    String CityPointName = mainObjBoarding.getJSONObject("Point-" + i).get("CityPointName").toString();
			    String CityPointTime = mainObjBoarding.getJSONObject("Point-" + i).get("CityPointTime").toString();
			    
			    BusPointDetails newPoint =  new BusPointDetails(CityPointIndex, CityPointLocation, CityPointName, CityPointTime);
			    busPointDetails.add(newPoint);
			}

			model.addAttribute("busPointDetails", busPointDetails);
			model.addAttribute("boardingPointDetails", boardingPointDetails);
			
        	
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

}
