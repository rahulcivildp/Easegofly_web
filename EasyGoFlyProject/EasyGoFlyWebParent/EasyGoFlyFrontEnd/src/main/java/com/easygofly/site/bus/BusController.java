package com.easygofly.site.bus;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
import com.easygofly.site.LogService;
import com.easygofly.site.customer.CustomerService;
import com.easygofly.site.security.EasegoflyPhoneCustomerDetails;
import com.easygofly.site.zaakpay.ChecksumGenerator;
import com.easygofly.site.zaakpay.Config;
import com.easygofly.site.zaakpay.Transaction;
import com.easygofly.site.zaakpay.ZaakpayApiRequestParameters;

@Controller
public class BusController {

	@Autowired private BusCityRepository busCityRepo;
	@Autowired private BusService busService;
	@Autowired private CustomerService customerService;
	@Autowired private OnlineBusService onlineBusService;
	@Autowired private LogService logService;

	private String searchURL = "";
	private String bookingURL = "";
	private String orderURL = "";
	
	BusHistory history = new BusHistory();
	List<Bus> buses = new ArrayList<>();
	List<BusSeat> seatList = new ArrayList<>();
	
	private String[] parameter = new String[20];
	private String checksum;
	private Boolean verifiedChecksum;
	private String[] responseParameters;
	
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
					// TODO: handle exception
//					e.printStackTrace();
				}

				Bus newBus = new Bus(resultIndex, arrivalTime, departureTime, routeId, busType, serviceName, travelName, "INR", idProofRequired, isDropPointMandatory, 
						liveTrackingAvailable, mTicketEnabled, partialCancellationAllowed, maxSeatsPerTicket, operatorId, tax, discount, publishedPrice, otherCharges, offeredPrice, 
						publishedPriceRoundedOff, offeredPriceRoundedOff, agentCommission, agentMarkUp, basePrice, tDS, cGSTAmount, cGSTRate, cessAmount, cessRate, iGSTAmount, iGSTRate, sGSTAmount, sGSTRate, 
						taxableAmount, availableSeats, null, cancellationPolicieList, pointList);
				
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
		model.addAttribute("deptDate", deptDate);
		model.addAttribute("checkIn", deptTime);
		model.addAttribute("busList", buses);
		
		return "bus/search/bus-search-result";
	}

	@PostMapping("/bus/saveBus")
	public String saveBus(@RequestParam(name = "resultIndex", required = false) String resultIndex,
			@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer,
			@RequestParam(name = "busDeparture", required = false) String busDeparture, 
			@RequestParam(name = "deptTime", required = false) String deptTime, 
			@RequestParam(name = "busDestination", required = false) String busDestination) throws Exception {
		
		TBObusCity cityOne = busCityRepo.getCityByCityName(busDeparture);
		TBObusCity cityTwo = busCityRepo.getCityByCityName(busDestination);
	    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	    Date deptDate = dateFormat.parse(deptTime);
		
		Customer customer = customerService.getByPhone(loggedCustomer.getUsername());	
		BusHistory newHistory = new BusHistory();
		
		if (history == null) {

			newHistory.setDeptDate(deptDate);
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
	    
		bookingURL = "/bus/booking_" + savedBus.getId() + "_" + history.getId() + "_" + busDeparture + "_" + busDestination + "_" + deptTime; 
		
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
	    busBusBoardingPoint(model, bus.getResultIndex(), bus);

		model.addAttribute("checkIn", checkIn);
		model.addAttribute("checkIn", checkIn);
		model.addAttribute("cityOne", cityOne);
		model.addAttribute("cityTwo", cityTwo);
		model.addAttribute("busHistory", busHistory);
		model.addAttribute("busId", busId);
		model.addAttribute("bus", bus);
		model.addAttribute("custId", customer.getId());
        
		return "bus/booking/bus-booking";
	}
	

	@PostMapping("/bus/save_order")
	public String saveBusOrder(@RequestParam(name = "bus_id", required = false) Integer bus_id, 
			@RequestParam(name = "search_id", required = false) Integer search_id, 
			@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer) {
		Bus bus = busService.findByIdBus(bus_id);
		List<BusSeat> busSeats = bus.getBusSeats();
		List<BusPassenger> busPaxs = bus.getBusPassengers();
		
		for (BusSeat seat : busSeats) {
			Integer count = 0;
			for (BusPassenger pax : busPaxs) {
				if(pax.getSeatId() == seat.getId()) {

					count++;
					if(count == 1) {
						pax.setLeadPassenger(true);
						busService.savePax(pax);
					} else {
						pax.setLeadPassenger(false);
						busService.savePax(pax);
					}
				}
			}
		}
	    
		
//		Customer customer = customerService.getByPhone(loggedCustomer.getUsername());
		
		orderURL = "/bus/order_" + bus_id + "_" + search_id;
		
		return "redirect:/bus_order_book...";
	}

	@GetMapping("/bus_order_book...")
    public String performApiLoadBusBook(Model model) {
        model.addAttribute("searchURL", orderURL);
        return "loading/loading";
    }

	@GetMapping("/bus/order_{bus_id}_{search_id}")
	public String busOrder(Model model,
			@PathVariable(name = "bus_id") Integer bus_id,
			@PathVariable(name = "search_id") Integer search_id,
			@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer, 
			HttpServletRequest request, RedirectAttributes redirectAttributes) throws MalformedURLException, IOException {
		
		Customer customer = customerService.getByPhone(loggedCustomer.getUsername()); 
		
		Bus bus = busService.findByIdBus(bus_id);
	    BusHistory busHistory = busService.findByIdBusHistory(search_id);
		TBObusCity cityOne = busCityRepo.getCityByCityId(Integer.parseInt(busHistory.getCityIdOne()));
		TBObusCity cityTwo = busCityRepo.getCityByCityId(Integer.parseInt(busHistory.getCityIdTwo()));
	    
	    Date createdDate = new Date();
	    
	    String busOrdername = busHistory.getDeptDate() + ":(" + cityOne.getCityName() + "-" + cityTwo.getCityName() + "):" + createdDate;
	    
	    BusOrder busOrder = new BusOrder(busOrdername, 0, createdDate, OrderStatus.NEW, customer, busHistory, bus);
		
	    BusOrder savedOrder= busService.saveOrder(busOrder, bus, busHistory);

	    String[] urlName = busBlockMethod(model, bus);
	    

		Integer totalGuests = bus.getBusPassengers().size();
		
		Wallet wallet = customer.getWallet();
		Double doubleAmount = (double) (wallet.getBalance() / 100);
		model.addAttribute("balance", doubleAmount);
		
		/* ------ ZAAKPAY -------- */ /**/
		Date date = Calendar.getInstance().getTime();  
	    DateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");  
	    DateFormat dateFormat2 = new SimpleDateFormat("hhmmss");
	    String strDate1 = dateFormat1.format(date);
	    String strDate2 = dateFormat2.format(date);
		
	    double totalPrice = 0;
	    
	    for (BusSeat seat : bus.getBusSeats()) {
	    	totalPrice = totalPrice + seat.getPublishedPriceRoundedOff();
		}
	    
		String orderString = "EGF" + strDate1 + "T" + strDate2 + "BUS" + savedOrder.getId();
		Integer intAmount = (int) (totalPrice * 100);
		String amount = "" + intAmount;
		//String amount = "100";

		//Cookie cookie = request.getCookies().get("JSESSIONID");
		//String value = cookie.getValue();
		
		for (Cookie cookie : request.getCookies()) {
			if(cookie.getName().equals("JSESSIONID")) {
				String value = cookie.getValue();
				model.addAttribute("JSESSIONID", value);
			}
		}
		
		
		Transaction transaction = new Transaction();
		
		try {
			ZaakpayApiRequestParameters processPayment = transaction.processPaymentBus(orderString, amount);
			
			model.addAttribute("entrySet", processPayment.getRequestParameters().entrySet());
			model.addAttribute("requestUrl", processPayment.getRequestUrl());
			model.addAttribute("checksum", processPayment.getChecksum());
			
		} catch (Exception e) {
		}
		
		/* ******************************************************************************** */
		
		

	    if (Integer.parseInt(urlName[0]) == 3) {
			return "redirect:/bus_booking...";
		} else if (Integer.parseInt(urlName[0]) == 2) {
			return "redirect:/bus_booking...";
		} 
	    
	    
		model.addAttribute("busHistory", busHistory);
		model.addAttribute("bus", bus);
		model.addAttribute("totalGuests", totalGuests);
		model.addAttribute("checkIn", busHistory.getDeptDate());
		model.addAttribute("cityOne", cityOne);
		model.addAttribute("cityTwo", cityTwo);
		model.addAttribute("totalPrice", totalPrice);
		model.addAttribute("savedOrder", savedOrder);
		
        
		return "bus/order/bus-order";
	}
	
	
	@PostMapping("/bus/order/wallet_check")
	public String busWalletPayment(@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer, 
			@RequestParam(name = "order_id") Integer order_id, 
			@RequestParam(name = "bus_id") Integer bus_id) {
		
		Customer customer = customerService.getByPhone(loggedCustomer.getUsername()); 
		Bus bus = busService.findByIdBus(bus_id);
		BusOrder busOrder = busService.findByIdOrder(order_id);
		double totalPrice = 0;
	    
	    for (BusSeat seat : bus.getBusSeats()) {
	    	totalPrice = totalPrice + seat.getPublishedPriceRoundedOff();
		}
	    BusOrder updatedOrder = busService.updateOrderPrice(order_id, totalPrice);
	    
		Wallet wallet = busService.busWalletPayOrder(customer, updatedOrder);
		
		if (wallet != null) {
			updatedOrder = busService.updateOrderStatus(busOrder.getId(), OrderStatus.SUCCESSFULL);
		} else {
			updatedOrder = busService.updateOrderStatus(busOrder.getId(), OrderStatus.FAILED);
		}
	
		return "redirect:/bus/order/wallet_response_" + updatedOrder.getId();
	}

	@GetMapping("/bus/order/wallet_response_{order_id}")
	public String showBusWalletPayment(@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer, Model model,
			@PathVariable(name = "order_id") Integer order_id) throws MalformedURLException, IOException, Exception {
		

		Customer customer = customerService.getByPhone(loggedCustomer.getUsername()); 

		BusOrder order = busService.findByIdOrder(order_id);
		TBObusCity cityOne = busCityRepo.getCityByCityId(Integer.parseInt(order.getBusHistory().getCityIdOne()));
		TBObusCity cityTwo = busCityRepo.getCityByCityId(Integer.parseInt(order.getBusHistory().getCityIdTwo()));
		BusBoardingPointDetails boardingPointDetails = order.getBus().getBusBoardingPointDetails().get(0);

	    String cityPointTime = boardingPointDetails.getCityPointTime();
	    Date date = new SimpleDateFormat("yyyy-MM-ddThh:mm:ss").parse(cityPointTime);
	    DateFormat dateFormat1 = new SimpleDateFormat("dd MMMM, yyyy");  
	    DateFormat dateFormat2 = new SimpleDateFormat("hh:mm");
	    String boardingDate = dateFormat1.format(date);
	    String boardingTime = dateFormat2.format(date);
	    
	    String cityPointName = boardingPointDetails.getCityPointName();
		

		model.addAttribute("orderId", order.getId());
		model.addAttribute("order", order);
		model.addAttribute("cityPointName", cityPointName);
		model.addAttribute("boardingDate", boardingDate);
		model.addAttribute("boardingTime", boardingTime);
		model.addAttribute("cityOne", cityOne);
		model.addAttribute("cityTwo", cityTwo);
		
		String[] hasErrorArr = new String[2];

		hasErrorArr = busBookkMethodAndBookingDetails(model, order);
		
		if (hasErrorArr[0].equals("0")) {
			model.addAttribute("paymentSuccess", "Successfull");
		} else if (hasErrorArr[0].equals("5")) {
			model.addAttribute("paymentCancelled", hasErrorArr[0]);
		} else {
			busService.walletPayBusOrderCancel(customer, order, OrderStatus.FAILED);
			model.addAttribute("paymentCancelled", hasErrorArr[1]);
		}
		
		model.addAttribute("amount", order.getPrice());
		
		return "wallet/bus/response";
	}
	
	
	
	@CrossOrigin(origins = {"https://easegofly.com/"})
	@RequestMapping(value = "/zaakpay/bus/response",
			method = {RequestMethod.POST})
	public String zaakpayHotelResponse (HttpServletRequest request, HttpServletResponse response,
			@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer, 
			@RequestParam(name = "search_id") Integer search_id) throws Exception {

//		Customer customer = customerService.getByPhone(loggedCustomer.getUsername()); 
		
		Transaction transaction = new Transaction();
	    ChecksumGenerator checksumGenerator = new ChecksumGenerator();
	    String checksumString = "" ;
	    Integer n= 0;
	    for (String param: transaction.getResponseParameters()) {
	        checksumString=checksumString+param+"="+request.getParameter(param);
	        checksumString=checksumString+"&";
	        //This will create the checksum string against every parameter.
	        parameter[n] = request.getParameter(param);
	        n+=1;
	    }
	    
	    Boolean verifyChecksum = checksumGenerator.verifyChecksum(Config.ZAAKPAY_SECRET_KEY,checksumString,request.getParameter("checksum")) ;
	    verifiedChecksum = verifyChecksum;
	    checksum = request.getParameter("checksum");
	    responseParameters = transaction.getResponseParameters();
		
	    String orderParam = parameter[8];
		String[] parts = orderParam.split("BU");
		String part2 = parts[1]; // 034556
		Integer convert = Integer.parseInt(part2);
		BusOrder order = busService.findByIdOrder(convert);

		if (parameter[12].equals("Customer cancelled transaction. Transaction has failed")) {
			busService.updateOrderStatus(order.getId(), OrderStatus.CANCELLED);
		} else if (parameter[12].equals("Unfortunately the transaction has failed.Please try again. Transaction has failed")) {
			busService.updateOrderStatus(order.getId(), OrderStatus.FAILED);
		} else if (parameter[12].equals("Unfortunately the transaction has failed.Please try again.")) {
			busService.updateOrderStatus(order.getId(), OrderStatus.FAILED);
		} else if (parameter[12].equals("The transaction was completed successfully.") || parameter[12].equals("Transaction has been settled.")) {
			busService.updateOrderStatus(order.getId(), OrderStatus.SUCCESSFULL);
		} 
		
		return "redirect:/zaakpay/response";
	}
	
	@CrossOrigin(origins = {"https://easegofly.com/"})
	@RequestMapping(value = "/zaakpay/bus/response",
			method = {RequestMethod.GET})
	public String zaakpayHotelResponseSe (Model model, 
			@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer) throws Exception {
		

		Customer customer = customerService.getByPhone(loggedCustomer.getUsername()); 

		String orderParam = parameter[8];
		model.addAttribute("orderId", orderParam);
		String[] parts = orderParam.split("BU");
		String part2 = parts[1]; // 034556
		Integer convert = Integer.parseInt(part2);
		BusOrder order = busService.findByIdOrder(convert);
		TBObusCity cityOne = busCityRepo.getCityByCityId(Integer.parseInt(order.getBusHistory().getCityIdOne()));
		TBObusCity cityTwo = busCityRepo.getCityByCityId(Integer.parseInt(order.getBusHistory().getCityIdTwo()));
		BusBoardingPointDetails boardingPointDetails = order.getBus().getBusBoardingPointDetails().get(0);

	    String cityPointTime = boardingPointDetails.getCityPointTime();
	    Date date = new SimpleDateFormat("yyyy-MM-ddThh:mm:ss").parse(cityPointTime);
	    DateFormat dateFormat1 = new SimpleDateFormat("dd MMMM, yyyy");  
	    DateFormat dateFormat2 = new SimpleDateFormat("hh:mm");
	    String boardingDate = dateFormat1.format(date);
	    String boardingTime = dateFormat2.format(date);
	    
	    String cityPointName = boardingPointDetails.getCityPointName();
		

		model.addAttribute("order", order);
		model.addAttribute("orderId", order.getId());
		model.addAttribute("cityPointName", cityPointName);
		model.addAttribute("boardingDate", boardingDate);
		model.addAttribute("boardingTime", boardingTime);
		model.addAttribute("cityOne", cityOne);
		model.addAttribute("cityTwo", cityTwo);
		
		String[] hasErrorArr = new String[2];	
		
		hasErrorArr = busBookkMethodAndBookingDetails(model, order);
		
		if (parameter[9].contains("Not Found") && parameter[10].contains("unknown") ) {
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].contains("Unfortunately the transaction has failed.Please try again. Transaction has failed")) {
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].contains("Unfortunately the transaction has failed.Please try again.")) {
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].equals("") || parameter[12] == null || parameter[9] == null) {
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].equals("Your Bank has declined this transaction please Retry this payment with another pay method.")) {
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].contains("Your Bank has declined this transaction please Retry this payment with another pay method.")) {
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[11].contains("1017")) {
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].contains("The transaction was completed successfully.") || parameter[12].contains("Transaction has been settled.")) {
			
			if (hasErrorArr[0].equals("0")) {
				model.addAttribute("paymentSuccess", OrderStatus.SUCCESSFULL);
			}  else if (hasErrorArr[0].equals("5")) {
				model.addAttribute("paymentCancelled", hasErrorArr[0]);
			} else {
				busService.walletPayBusOrderCancel(customer, order, OrderStatus.FAILED);
				busService.updateOrderStatus(order.getId(), OrderStatus.FAILED);
				model.addAttribute("paymentCancelled", hasErrorArr[1]);
			}
		}
        
		Double amount = Double.parseDouble(parameter[0])/100;
		
		model.addAttribute("amount", amount);
		model.addAttribute("checksum", checksum);
		model.addAttribute("verifyChecksum", verifiedChecksum);
		model.addAttribute("responseParameters", responseParameters);
		
		return "zaakpay/bus/response";
		
		
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
        List<String> seatListIds = new ArrayList<String>();
        
        try {
        	JSONObject jsonObjSeat = jsonObjSeatLayout.getJSONObject("GetBusSeatLayOutResult").getJSONObject("SeatLayoutDetails");
        	JSONArray jsonArraySeatDetails = jsonObjSeat.getJSONObject("SeatLayout").getJSONArray("SeatDetails");
			JSONObject mainObj = new JSONObject();
        	
        	String availableSeats = jsonObjSeat.get("AvailableSeats").toString();
        	String hTMLLayout = jsonObjSeat.get("HTMLLayout").toString();
        	Integer noOfColumns = Integer.parseInt(jsonObjSeat.getJSONObject("SeatLayout").get("NoOfColumns").toString());
        	Integer noOfRows = Integer.parseInt(jsonObjSeat.getJSONObject("SeatLayout").get("NoOfRows").toString());
        	
        	for (int i = 0; i < jsonArraySeatDetails.length(); i++) {
			    mainObj.put("Seat-" + i, jsonArraySeatDetails.getJSONArray(0).getJSONObject(i));
				
			    String columnNo = mainObj.getJSONObject("Seat-" + i).get("ColumnNo").toString();
			    String rowNo = mainObj.getJSONObject("Seat-" + i).get("RowNo").toString();
			    String seatName = mainObj.getJSONObject("Seat-" + i).get("SeatName").toString();
	        	Integer height = Integer.parseInt(mainObj.getJSONObject("Seat-" + i).get("Height").toString());
	        	Integer seatIndex = Integer.parseInt(mainObj.getJSONObject("Seat-" + i).get("SeatIndex").toString());
	        	Integer seatType = Integer.parseInt(mainObj.getJSONObject("Seat-" + i).get("SeatType").toString());
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
			    seatListIds.add(newSeat.getSeatIndex().toString());

			}

			model.addAttribute("seatListIds", seatListIds);
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

	private void busBusBoardingPoint(Model model, Integer resultIndex, Bus bus) throws IOException {

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
        		
			    Integer CityPointIndex = Integer.parseInt(mainObjBoarding.getJSONObject("Point-" + i).get("CityPointIndex").toString());
			    String CityPointLocation = mainObjBoarding.getJSONObject("Point-" + i).get("CityPointLocation").toString();
			    String CityPointName = mainObjBoarding.getJSONObject("Point-" + i).get("CityPointName").toString();
			    String CityPointTime = mainObjBoarding.getJSONObject("Point-" + i).get("CityPointTime").toString();
			    
			    BusDroppingPointDetail newPoint =  new BusDroppingPointDetail(CityPointIndex, CityPointLocation, CityPointName, CityPointTime);
			    newPoint.setBus(bus);
			    droppingPointDetails.add(newPoint);
			}
        	
        	bus.setBusBoardingPointDetails(boardingPointDetails);
        	bus.setBusDroppingPointDetails(droppingPointDetails);
        	
        	busService.saveBus(bus);

			model.addAttribute("busPointDetails", droppingPointDetails);
			model.addAttribute("boardingPointDetails", boardingPointDetails);
			
        	
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	private String[] busBlockMethod(Model model, Bus bus) 
			throws MalformedURLException, IOException {
		
		List<String> strPaxs = new ArrayList<String>();
		String[] hasErrorArr = new String[2];
		
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
		
		
		
		// Create URL object with the API end-point
        URL urlBusBlock = new URL("http://api.tektravels.com/BookingEngineService_Bus/Busservice.svc/rest/Block/");

        // Open a connection
        HttpURLConnection connectionBusBlock = (HttpURLConnection) urlBusBlock.openConnection();
       
        StringBuilder responseBodyBusBlock = new StringBuilder();
        
        onlineBusService.apiOnlineBusBlock(connectionBusBlock, responseBodyBusBlock, arraySeat, bus.getResultIndex(), bus.getBusBoardingPointDetails().get(0).getCityPointIndex(), 
        		bus.getBusDroppingPointDetails().get(0).getCityPointIndex());
	
        JSONObject jsonObjBlock = new JSONObject(responseBodyBusBlock.toString());
        System.out.println(jsonObjBlock);
        logService.generateLog(jsonObjBlock.toString());
        
        
        try {
        	JSONObject jsonObj = jsonObjBlock.getJSONObject("BlockResult");

			JSONObject jsonObjTicketResponseErrorBooking = jsonObj.getJSONObject("Error");
			hasErrorArr[0] = jsonObjTicketResponseErrorBooking.get("ErrorCode").toString();
			hasErrorArr[1] = jsonObjTicketResponseErrorBooking.get("ErrorMessage").toString();
        	
        	String arrivalTime = jsonObj.get("ArrivalTime").toString();
        	String busType = jsonObj.get("BusType").toString();
        	String departureTime = jsonObj.get("DepartureTime").toString();
        	String serviceName = jsonObj.get("ServiceName").toString();
        	String travelName = jsonObj.get("TravelName").toString();

    		model.addAttribute("arrivalTime", arrivalTime);
    		model.addAttribute("busType", busType);
    		model.addAttribute("departureTime", departureTime);
    		model.addAttribute("serviceName", serviceName);
    		model.addAttribute("travelName", travelName);
    		
		} catch (Exception e) {
			
					
        	JSONObject jsonObj = jsonObjBlock.getJSONObject("BlockResult");

			JSONObject jsonObjTicketResponseErrorBooking = jsonObj.getJSONObject("Error");
			hasErrorArr[0] = jsonObjTicketResponseErrorBooking.get("ErrorCode").toString();
			hasErrorArr[1] = jsonObjTicketResponseErrorBooking.get("ErrorMessage").toString();

        	Integer errorCode = Integer.parseInt(jsonObjTicketResponseErrorBooking.get("ErrorCode").toString());
        	String errorMessage = jsonObjTicketResponseErrorBooking.get("ErrorMessage").toString();

    		model.addAttribute("errorCode", errorCode);
    		model.addAttribute("errorMessage", errorMessage);
    		
			e.printStackTrace();
		}

		return hasErrorArr;
	}

	private String[] busBookkMethodAndBookingDetails(Model model, BusOrder order) 
			throws MalformedURLException, IOException {
		
		List<String> strPaxs = new ArrayList<String>();
		String[] hasErrorArr = new String[2];
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
		
		
		
		// Create URL object with the API end-point
        URL urlBusBook = new URL("http://api.tektravels.com/BookingEngineService_Bus/Busservice.svc/rest/Book/");

        // Open a connection
        HttpURLConnection connectionBusBook = (HttpURLConnection) urlBusBook.openConnection();
       
        StringBuilder responseBodyBusBook = new StringBuilder();
        
        onlineBusService.apiOnlineBusBlock(connectionBusBook, responseBodyBusBook, arraySeat, bus.getResultIndex(), bus.getBusBoardingPointDetails().get(0).getCityPointIndex(), 
        		bus.getBusDroppingPointDetails().get(0).getCityPointIndex());
	
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

			JSONObject jsonObjTicketResponseError = jsonObj.getJSONObject("Error");
			hasErrorArr[0] = jsonObjTicketResponseError.get("ErrorCode").toString();
			hasErrorArr[1] = jsonObjTicketResponseError.get("ErrorMessage").toString();
    		
    		// Create URL object with the API end-point
            URL urlBusBookingDetails = new URL("http://api.tektravels.com/BookingEngineService_Bus/Busservice.svc/rest/Book/");

            // Open a connection
            HttpURLConnection connectionBusBookingDetails = (HttpURLConnection) urlBusBookingDetails.openConnection();
           
            StringBuilder responseBodyBusBookingDetails = new StringBuilder();
            
            onlineBusService.apiOnlineBusBookingDetails(connectionBusBookingDetails, responseBodyBusBookingDetails, busId);
            
            JSONObject jsonObjBookingDetails = new JSONObject(responseBodyBusBookingDetails.toString());
            System.out.println(jsonObjBookingDetails);
            logService.generateLog(jsonObjBookingDetails.toString());
            try {
            	JSONObject jsonObjBooki = jsonObjBook.getJSONObject("GetBookingDetailResult");

            	Integer errorCode = Integer.parseInt(jsonObjBooki.getJSONObject("Error").get("ErrorCode").toString());
            	String errorMessage = jsonObjBooki.getJSONObject("Error").get("ErrorMessage").toString();

        		model.addAttribute("errorCode", errorCode);
        		model.addAttribute("errorMessage", errorMessage);
				
			} catch (Exception e) {
            	JSONObject jsonObjBooki = jsonObjBook.getJSONObject("GetBookingDetailResult");

    			JSONObject jsonObjTicketResponseErrorBooking = jsonObjBooki.getJSONObject("Error");
    			hasErrorArr[0] = jsonObjTicketResponseErrorBooking.get("ErrorCode").toString();
    			hasErrorArr[1] = jsonObjTicketResponseErrorBooking.get("ErrorMessage").toString();

            	Integer errorCode = Integer.parseInt(jsonObjBooki.getJSONObject("Error").get("ErrorCode").toString());
            	String errorMessage = jsonObjBooki.getJSONObject("Error").get("ErrorMessage").toString();

        		model.addAttribute("errorCode", errorCode);
        		model.addAttribute("errorMessage", errorMessage);
        		
				e.printStackTrace();
			}
    		
		} catch (Exception e) {

        	JSONObject jsonObj = jsonObjBook.getJSONObject("BookResult");

			JSONObject jsonObjTicketResponseError = jsonObj.getJSONObject("Error");
			hasErrorArr[0] = jsonObjTicketResponseError.get("ErrorCode").toString();
			hasErrorArr[1] = jsonObjTicketResponseError.get("ErrorMessage").toString();

        	Integer errorCode = Integer.parseInt(jsonObj.getJSONObject("Error").get("ErrorCode").toString());
        	String errorMessage = jsonObj.getJSONObject("Error").get("ErrorMessage").toString();
        	
    		model.addAttribute("errorCodeBook", errorCode);
    		model.addAttribute("errorMessageBook", errorMessage);
    		
			e.printStackTrace();
		}
        
        return hasErrorArr;
	}

}
