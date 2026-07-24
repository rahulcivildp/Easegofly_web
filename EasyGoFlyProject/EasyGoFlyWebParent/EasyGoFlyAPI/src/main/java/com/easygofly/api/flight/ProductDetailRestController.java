package com.easygofly.api.flight;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easygofly.api.setting.APIServiceSettingBag;
import com.easygofly.api.setting.SettingService;
import com.easygofly.entity.ProductDetail;
import com.easygofly.entity.TravellerDetail;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(
		name = "CRUD REST APIs for Flight Booking", 
		description = "Operations related to flight booking"
)
public class ProductDetailRestController {
	@Autowired
	private ProductDetailCrudRepository flightRepo;
	@Autowired
	private OnlineFlightService onlineFlightService;
	@Autowired
	private SettingService settingService;

	@PostMapping("/api/flight/tbo-search/fareRule")
	public String tbofareRule(HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
		response.setContentType("application/json");

		TBOfareRuleQuote fareRuleQuote = new ObjectMapper().readValue(request.getInputStream(), TBOfareRuleQuote.class);
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();

		URL url = new URL(apiServiceSettingBag.getDefaultURL() + "/AirService.svc/rest/FareRule");
//        URL url = new URL("http://api.tektravels.com/BookingEngineService_Air/AirService.svc/rest/FareRule");

		StringBuilder responseBody = onlineFlightService.apiOnlineFarerule_quote(url, fareRuleQuote.traceId,
				fareRuleQuote.resultIndex);

		return responseBody.toString();
	}

	@PostMapping("/api/flight/tbo-search/fareQuote")
	public String tbofareQuote(HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
		response.setContentType("application/json");

		TBOfareRuleQuote fareRuleQuote = new ObjectMapper().readValue(request.getInputStream(), TBOfareRuleQuote.class);
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();

		URL url = new URL(apiServiceSettingBag.getDefaultURL() + "/AirService.svc/rest/FareQuote");

//        URL url = new URL("http://api.tektravels.com/BookingEngineService_Air/AirService.svc/rest/FareQuote");

		StringBuilder responseBody = onlineFlightService.apiOnlineFarerule_quote(url, fareRuleQuote.traceId,
				fareRuleQuote.resultIndex);

		return responseBody.toString();
	}

	@PostMapping("/api/flight/tbo-search/ssr")
	public String tboSSR(HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
		response.setContentType("application/json");

		TBOfareRuleQuote fareRuleQuote = new ObjectMapper().readValue(request.getInputStream(), TBOfareRuleQuote.class);
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();

		URL url = new URL(apiServiceSettingBag.getDefaultURL() + "/AirService.svc/rest/SSR");
//        URL url = new URL("http://api.tektravels.com/BookingEngineService_Air/AirService.svc/rest/SSR");

		StringBuilder responseBody = onlineFlightService.apiOnlineFarerule_quote(url, fareRuleQuote.traceId,
				fareRuleQuote.resultIndex);

		return responseBody.toString();
	}

	@PostMapping("/api/flight/save_flight")
	public String flightDetailSave(HttpServletRequest request, HttpServletResponse response)
			throws IOException, Exception {
		response.setContentType("application/json");

		SaveFlight saveFlight = new ObjectMapper().readValue(request.getInputStream(), SaveFlight.class);

		Date origin = new SimpleDateFormat("yyyy-MM-dd").parse(saveFlight.date);

		ProductDetail existingFlight = flightRepo.getFlight(saveFlight.trace_id, saveFlight.result_index);
		ProductDetail savedFlight = new ProductDetail();
		;

		if (existingFlight == null) {
			ProductDetail flight = new ProductDetail();

			flight.setArrTime(saveFlight.arr_time);
			flight.setDepTime(saveFlight.dep_time);
			flight.setCityOne(saveFlight.city_one);
			flight.setCityTwo(saveFlight.city_two);
			flight.setDate(origin);
			flight.setPnr(saveFlight.pnr);
			flight.setTotalSeats(saveFlight.total_seats);
			flight.setFlightNum(saveFlight.flight_num);
			flight.setArrTimeInteger(saveFlight.arr_time_integer);
			flight.setDepTimeInteger(saveFlight.dep_time_integer);
			flight.setPriceADT(saveFlight.priceadt);
			flight.setPriceINF(saveFlight.priceinf);
			flight.setMarkupADT(saveFlight.markupadt);
			flight.setMarkupINF(saveFlight.markupinf);
			flight.setJourneyClass(saveFlight.journey_class);
			flight.setTerminalDep(saveFlight.terminal_dep);
			flight.setTerminalArr(saveFlight.terminal_arr);
			flight.setCabinBaggage(saveFlight.cabin_baggage);
			flight.setBaggage(saveFlight.baggage);
			flight.setCraftType(saveFlight.craftType);
			flight.setDuration(saveFlight.duration);
			flight.setBrand(saveFlight.brand);
			flight.setStopNum(saveFlight.stop_num);
			flight.setTraceId(saveFlight.trace_id);
			flight.setResultIndex(saveFlight.result_index);
			flight.setAirlineRemarks(saveFlight.airline_remarks);
			flight.setMode(saveFlight.mode);
			flight.setLcc(saveFlight.lcc);
			flight.setDevice(saveFlight.device);
			flight.setDeviceDescription(saveFlight.device_description);
			flight.setDeviceType(saveFlight.device_type);
			flight.setUploadSeats(saveFlight.upload_seats);
			flight.setEnabled(saveFlight.enabled);

			savedFlight = flightRepo.save(flight);
		} else {
			savedFlight = existingFlight;
		}

		String flightBody = "{" + "\"id\": " + savedFlight.getId() + "" + "}";

		String responseBody = "{" + "\"code\": 0, " + "\"msg\": \"Flight Entity Id.\", " + "\"data\": " + flightBody
				+ "" + "}";

		return responseBody;
	}

	@PostMapping("/api/flight/save_traveller")
	public String saveTravelerDetail(HttpServletRequest request, HttpServletResponse response)
			throws IOException, Exception {
		response.setContentType("application/json");

		SaveTravelerRequest saveTravelerRequest = new ObjectMapper().readValue(request.getInputStream(), SaveTravelerRequest.class);
		ProductDetail flight = flightRepo.findById(saveTravelerRequest.flight_id).get();
		flight.setTravellerDetails(new ArrayList<TravellerDetail>());
		List<String> strTravelerIdList = new ArrayList<String>();
		
		for (SaveTraveler pax : saveTravelerRequest.travelers) {
			
			Date passportExpiry = new SimpleDateFormat("yyyy-MM-dd").parse(pax.passport_expiry);
			Date dob = new SimpleDateFormat("yyyy-MM-dd").parse(pax.dob);
			Integer paxType = 0;
			if (pax.pax_type.equals("Adult")) {
				paxType = 1;
			} else if (pax.pax_type.equals("Child")) {
				paxType = 2;
			} else {
				paxType = 3;
			}
			
			TravellerDetail traveler = new TravellerDetail(pax.salutation, pax.first_name, pax.last_name,
					paxType.toString(), 7, 15, pax.passposrt_no, passportExpiry, dob, pax.id,
					flight);
			traveler.addMeal(pax.meal.description, Double.toString(pax.meal.price),
					pax.meal.code, pax.meal.quantity.toString());
			traveler.addBaggage(Double.toString(pax.baggage.price), pax.baggage.code,
					pax.baggage.weight.toString());
			if (pax.seat != null){
				traveler.addSeat(Double.toString(pax.seat.price), pax.seat.compartment,
					pax.seat.availablity_type, pax.seat.deck, pax.seat.row_no,
					pax.seat.code, pax.seat.seat_type, pax.seat.seat_no,
					pax.seat.craft_type);
			}
			
			flight.addTravellerDetails(traveler);
		}
		
		flight.setUploadSeats(saveTravelerRequest.total_pax.toString());
		
		ProductDetail savedFlight = flightRepo.save(flight);
		
		for (TravellerDetail travellerDetail : savedFlight.getTravellerDetails()) {

	        String cityBody =  "{"
	        		+ "\"id\": " + travellerDetail.getId() + ""
	        		+ "}";
	        
	        strTravelerIdList.add(cityBody);
		}
		
		String arrayTravelerIdList = strTravelerIdList.stream().map(val -> String.valueOf(val)).collect(Collectors.joining(",", "[", "]"));

		String responseBody = "{" + "\"code\": 0, " + "\"msg\": \"Traveler Details List Ids.\", " + "\"data\": "
				+ arrayTravelerIdList + "" + "}";

		return responseBody;
	}
	
	// Static POJO List

	@SuppressWarnings("unused")
	private static class SaveFlight {
		private String arr_time;
		private String dep_time;
		private String city_one;
		private String city_two;
		private String date;
		private String pnr;
		private String total_seats;
		private String flight_num;
		private float arr_time_integer;
		private float dep_time_integer;
		private float priceadt;
		private float priceinf;
		private float markupadt;
		private float markupinf;
		private String journey_class;
		private String terminal_dep;
		private String terminal_arr;
		private Integer cabin_baggage;
		private Integer baggage;
		private String craftType;
		private Integer duration;
		private String brand;
		private Integer stop_num;
		private String trace_id;
		private String result_index;
		private String airline_remarks;
		private String mode;
		private boolean lcc;
		private String device;
		private String device_description;
		private String device_type;
		private String arr_date;
		private String dep_date;
		private String upload_seats;
		private boolean enabled;

		public String getArr_time() {
			return arr_time;
		}

		public void setArr_time(String arr_time) {
			this.arr_time = arr_time;
		}

		public String getDep_time() {
			return dep_time;
		}

		public void setDep_time(String dep_time) {
			this.dep_time = dep_time;
		}

		public String getCity_one() {
			return city_one;
		}

		public void setCity_one(String city_one) {
			this.city_one = city_one;
		}

		public String getCity_two() {
			return city_two;
		}

		public void setCity_two(String city_two) {
			this.city_two = city_two;
		}

		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public String getPnr() {
			return pnr;
		}

		public void setPnr(String pnr) {
			this.pnr = pnr;
		}

		public String getTotal_seats() {
			return total_seats;
		}

		public void setTotal_seats(String total_seats) {
			this.total_seats = total_seats;
		}

		public String getFlight_num() {
			return flight_num;
		}

		public void setFlight_num(String flight_num) {
			this.flight_num = flight_num;
		}

		public float getArr_time_integer() {
			return arr_time_integer;
		}

		public void setArr_time_integer(float arr_time_integer) {
			this.arr_time_integer = arr_time_integer;
		}

		public float getDep_time_integer() {
			return dep_time_integer;
		}

		public void setDep_time_integer(float dep_time_integer) {
			this.dep_time_integer = dep_time_integer;
		}

		public float getPriceadt() {
			return priceadt;
		}

		public void setPriceadt(float priceadt) {
			this.priceadt = priceadt;
		}

		public float getPriceinf() {
			return priceinf;
		}

		public void setPriceinf(float priceinf) {
			this.priceinf = priceinf;
		}

		public float getMarkupadt() {
			return markupadt;
		}

		public void setMarkupadt(float markupadt) {
			this.markupadt = markupadt;
		}

		public float getMarkupinf() {
			return markupinf;
		}

		public void setMarkupinf(float markupinf) {
			this.markupinf = markupinf;
		}

		public String getJourney_class() {
			return journey_class;
		}

		public void setJourney_class(String journey_class) {
			this.journey_class = journey_class;
		}

		public String getTerminal_dep() {
			return terminal_dep;
		}

		public void setTerminal_dep(String terminal_dep) {
			this.terminal_dep = terminal_dep;
		}

		public String getTerminal_arr() {
			return terminal_arr;
		}

		public void setTerminal_arr(String terminal_arr) {
			this.terminal_arr = terminal_arr;
		}

		public Integer getCabin_baggage() {
			return cabin_baggage;
		}

		public void setCabin_baggage(Integer cabin_baggage) {
			this.cabin_baggage = cabin_baggage;
		}

		public Integer getBaggage() {
			return baggage;
		}

		public void setBaggage(Integer baggage) {
			this.baggage = baggage;
		}

		public String getCraftType() {
			return craftType;
		}

		public void setCraftType(String craftType) {
			this.craftType = craftType;
		}

		public Integer getDuration() {
			return duration;
		}

		public void setDuration(Integer duration) {
			this.duration = duration;
		}

		public String getBrand() {
			return brand;
		}

		public void setBrand(String brand) {
			this.brand = brand;
		}

		public Integer getStop_num() {
			return stop_num;
		}

		public void setStop_num(Integer stop_num) {
			this.stop_num = stop_num;
		}

		public String getTrace_id() {
			return trace_id;
		}

		public void setTrace_id(String trace_id) {
			this.trace_id = trace_id;
		}

		public String getResult_index() {
			return result_index;
		}

		public void setResult_index(String result_index) {
			this.result_index = result_index;
		}

		public String getAirline_remarks() {
			return airline_remarks;
		}

		public void setAirline_remarks(String airline_remarks) {
			this.airline_remarks = airline_remarks;
		}

		public String getMode() {
			return mode;
		}

		public void setMode(String mode) {
			this.mode = mode;
		}

		public boolean isLcc() {
			return lcc;
		}

		public void setLcc(boolean lcc) {
			this.lcc = lcc;
		}

		public String getDevice() {
			return device;
		}

		public void setDevice(String device) {
			this.device = device;
		}

		public String getDevice_description() {
			return device_description;
		}

		public void setDevice_description(String device_description) {
			this.device_description = device_description;
		}

		public String getDevice_type() {
			return device_type;
		}

		public void setDevice_type(String device_type) {
			this.device_type = device_type;
		}

		public String getArr_date() {
			return arr_date;
		}

		public void setArr_date(String arr_date) {
			this.arr_date = arr_date;
		}

		public String getDep_date() {
			return dep_date;
		}

		public void setDep_date(String dep_date) {
			this.dep_date = dep_date;
		}

		public String getUpload_seats() {
			return upload_seats;
		}

		public void setUpload_seats(String upload_seats) {
			this.upload_seats = upload_seats;
		}

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

	}

	@SuppressWarnings("unused")
	private static class SaveTravelerRequest {
		private List<SaveTraveler> travelers; 
		private Integer flight_id;
		private Integer total_pax;
		
		public SaveTravelerRequest() {}
		
		public List<SaveTraveler> getTravelers() {
			return travelers;
		}
		public void setTravelers(List<SaveTraveler> travelers) {
			this.travelers = travelers;
		}
		public Integer getFlight_id() {
			return flight_id;
		}
		public void setFlight_id(Integer flight_id) {
			this.flight_id = flight_id;
		}
		public Integer getTotal_pax() {
			return total_pax;
		}
		public void setTotal_pax(Integer total_pax) {
			this.total_pax = total_pax;
		}
		
		
	}
	
	@SuppressWarnings("unused")
	private static class SaveTraveler {
		private Integer id;
		private String salutation;
		private String first_name;
		private String last_name;
		private String pax_type;
		private String dob;
		private String email;
		private String passposrt_no;
		private String passport_expiry;
		private String phone;
		private SaveMeal meal;
		private SaveBaggage baggage;
		private SaveSeat seat;

		public SaveTraveler() {}

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getSalutation() {
			return salutation;
		}

		public void setSalutation(String salutation) {
			this.salutation = salutation;
		}

		public String getDob() {
			return dob;
		}

		public void setDob(String dob) {
			this.dob = dob;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getPhone() {
			return phone;
		}

		public void setPhone(String phone) {
			this.phone = phone;
		}

		public String getFirst_name() {
			return first_name;
		}

		public void setFirst_name(String first_name) {
			this.first_name = first_name;
		}

		public String getLast_name() {
			return last_name;
		}

		public void setLast_name(String last_name) {
			this.last_name = last_name;
		}

		public String getPax_type() {
			return pax_type;
		}

		public void setPax_type(String pax_type) {
			this.pax_type = pax_type;
		}

		public String getPassposrt_no() {
			return passposrt_no;
		}

		public void setPassposrt_no(String passposrt_no) {
			this.passposrt_no = passposrt_no;
		}

		public String getPassport_expiry() {
			return passport_expiry;
		}

		public void setPassport_expiry(String passport_expiry) {
			this.passport_expiry = passport_expiry;
		}

		public SaveMeal getMeal() {
			return meal;
		}

		public void setMeal(SaveMeal meal) {
			this.meal = meal;
		}

		public SaveBaggage getBaggage() {
			return baggage;
		}

		public void setBaggage(SaveBaggage baggage) {
			this.baggage = baggage;
		}

		public SaveSeat getSeat() {
			return seat;
		}

		public void setSeat(SaveSeat seat) {
			this.seat = seat;
		}

	}

	@SuppressWarnings("unused")
	private static class SaveMeal {
		private String airline_code;
		private String origin;
		private String destination;
		private String description;
		private double price;
		private String currency;
		private String flight_number;
		private String airline_description;
		private Integer quantity;
		private Integer way_type;
		private String code;

		public String getAirline_code() {
			return airline_code;
		}

		public void setAirline_code(String airline_code) {
			this.airline_code = airline_code;
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

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public double getPrice() {
			return price;
		}

		public void setPrice(double price) {
			this.price = price;
		}

		public String getCurrency() {
			return currency;
		}

		public void setCurrency(String currency) {
			this.currency = currency;
		}

		public String getFlight_number() {
			return flight_number;
		}

		public void setFlight_number(String flight_number) {
			this.flight_number = flight_number;
		}

		public String getAirline_description() {
			return airline_description;
		}

		public void setAirline_description(String airline_description) {
			this.airline_description = airline_description;
		}

		public Integer getQuantity() {
			return quantity;
		}

		public void setQuantity(Integer quantity) {
			this.quantity = quantity;
		}

		public Integer getWay_type() {
			return way_type;
		}

		public void setWay_type(Integer way_type) {
			this.way_type = way_type;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}
	}

	@SuppressWarnings("unused")
	private static class SaveBaggage {
		private String airline_code;
		private String origin;
		private String destination;
		private String description;
		private double price;
		private String flight_number;
		private Integer way_type;
		private String code;
		private Integer weight;

		public String getAirline_code() {
			return airline_code;
		}

		public void setAirline_code(String airline_code) {
			this.airline_code = airline_code;
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

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public double getPrice() {
			return price;
		}

		public void setPrice(double price) {
			this.price = price;
		}

		public String getFlight_number() {
			return flight_number;
		}

		public void setFlight_number(String flight_number) {
			this.flight_number = flight_number;
		}

		public Integer getWay_type() {
			return way_type;
		}

		public void setWay_type(Integer way_type) {
			this.way_type = way_type;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public Integer getWeight() {
			return weight;
		}

		public void setWeight(Integer weight) {
			this.weight = weight;
		}
	}

	@SuppressWarnings("unused")
	private static class SaveSeat {
		private String origin;
		private String destination;
		private String description;
		private Integer compartment;
		private double price;
		private Integer availablity_type;
		private Integer deck;
		private String row_no;
		private Integer seat_way_type;
		private String airline_code;
		private String code;
		private String flight_number;
		private Integer seat_type;
		private String seat_no;
		private String craft_type;

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

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public Integer getCompartment() {
			return compartment;
		}

		public void setCompartment(Integer compartment) {
			this.compartment = compartment;
		}

		public double getPrice() {
			return price;
		}

		public void setPrice(double price) {
			this.price = price;
		}

		public Integer getAvailablity_type() {
			return availablity_type;
		}

		public void setAvailablity_type(Integer availablity_type) {
			this.availablity_type = availablity_type;
		}

		public Integer getDeck() {
			return deck;
		}

		public void setDeck(Integer deck) {
			this.deck = deck;
		}

		public String getRow_no() {
			return row_no;
		}

		public void setRow_no(String row_no) {
			this.row_no = row_no;
		}

		public Integer getSeat_way_type() {
			return seat_way_type;
		}

		public void setSeat_way_type(Integer seat_way_type) {
			this.seat_way_type = seat_way_type;
		}

		public String getAirline_code() {
			return airline_code;
		}

		public void setAirline_code(String airline_code) {
			this.airline_code = airline_code;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getFlight_number() {
			return flight_number;
		}

		public void setFlight_number(String flight_number) {
			this.flight_number = flight_number;
		}

		public Integer getSeat_type() {
			return seat_type;
		}

		public void setSeat_type(Integer seat_type) {
			this.seat_type = seat_type;
		}

		public String getSeat_no() {
			return seat_no;
		}

		public void setSeat_no(String seat_no) {
			this.seat_no = seat_no;
		}

		public String getCraft_type() {
			return craft_type;
		}

		public void setCraft_type(String craft_type) {
			this.craft_type = craft_type;
		}

	}

	@SuppressWarnings("unused")
	private static class TBOfareRuleQuote {
		private String traceId;
		private String resultIndex;

		public String getTraceId() {
			return traceId;
		}

		public void setTraceId(String traceId) {
			this.traceId = traceId;
		}

		public String getResultIndex() {
			return resultIndex;
		}

		public void setResultIndex(String resultIndex) {
			this.resultIndex = resultIndex;
		}

	}

}
