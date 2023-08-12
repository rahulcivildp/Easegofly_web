package com.easygofly.entity;

import java.beans.Transient;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "product_details")
public class ProductDetail {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(length = 25)
	private String pnr;
	
	@Column(nullable = false, length = 5)
	private String totalSeats;
	
	@Column(nullable = false, length = 5)
	private String uploadSeats;
	
	@Column(nullable = false, length = 50)
	private String flightNum;
	
	@Column(nullable = false)
	@Temporal(TemporalType.DATE)
	private Date date;
	
	@Column(nullable = false)
	private String depTime;
	
	@Column(nullable = false)
	private String arrTime;
	
	@Column(nullable = false)
	private float depTimeInteger;
	
	@Column(nullable = false)
	private float arrTimeInteger;
	
	@Column(nullable = false, length = 20)
	private float priceADT;
	
	@Column(nullable = false, length = 20)
	private float priceINF;
	
	@Column(length = 10)
	private float markupADT;
	
	@Column(length = 10)
	private float markupINF;
	
	@Column(nullable = false, length = 5, name = "city_one")
	private String cityOne;
	
	@Column(nullable = false, length = 5, name = "city_two")
	private String cityTwo;
	
	@Column(name = "journey_class")
	private String journeyClass;

	@Column(name = "terminal_dep")
	private String terminalDep;

	@Column(name = "terminal_arr")
	private String terminalArr;

	@Column(name = "baggage")
	private Integer baggage;

	@Column(name = "cabin_baggage")
	private Integer cabinBaggage;
	
	private Integer duration;
	
	private String brand;
	
	private int stopNum;
	
	private boolean enabled;
	
	private boolean inStock;
	
	private String traceId;
	
	private String resultIndex;
	
	private String airlineRemarks;
	
	private String mode;
	
	@ManyToOne
	@JoinColumn(name = "product_id")
	private Product product;
	
	@OneToMany(mappedBy = "productDetail", cascade = CascadeType.ALL)
	private List<Stop> stops = new ArrayList<>();
	
	@OneToMany(mappedBy = "productDetail", cascade = CascadeType.ALL)
	private List<CartItem> cartItems = new ArrayList<>();
	
	@OneToMany(mappedBy = "productDetail", cascade = CascadeType.ALL)
	private List<TravellerDetail> travellerDetails = new ArrayList<>();
	
	@OneToOne(fetch = FetchType.LAZY,
            cascade =  CascadeType.ALL,
            mappedBy = "productDetail")
    private Order order;
	
	
	public ProductDetail() {}

	public ProductDetail(int id) {
		this.id = id;
	}

	public ProductDetail(int id, String pnr, String totalSeats, String uploadSeats, String flightNum, Date date, String depTime,
			String arrTime, float priceADT, float priceINF, float markupADT, float markupINF, String cityOne,
			String cityTwo, boolean inStock, boolean enabled, int stopNum, Integer duration, String brand, float depTimeInteger, 
			float arrTimeInteger, Product product) {
		this.id = id;
		this.pnr = pnr;
		this.totalSeats = totalSeats;
		this.uploadSeats = uploadSeats;
		this.flightNum = flightNum;
		this.date = date;
		this.depTime = depTime;
		this.arrTime = arrTime;
		this.priceADT = priceADT;
		this.priceINF = priceINF;
		this.markupADT = markupADT;
		this.markupINF = markupINF;
		this.cityOne = cityOne;
		this.cityTwo = cityTwo;
		this.enabled = enabled;
		this.inStock = inStock;
		this.product = product;
		this.stopNum = stopNum;
		this.duration = duration;
		this.brand = brand;
		this.arrTimeInteger = arrTimeInteger;
		this.depTimeInteger = depTimeInteger;
	}
	
	public ProductDetail(int id, String pnr, String totalSeats, String uploadSeats, String flightNum, Date date, String depTime,
			String arrTime, float priceADT, float priceINF, float markupADT, float markupINF, String cityOne,
			String cityTwo, boolean inStock, boolean enabled, int stopNum, Integer duration, String brand, float depTimeInteger, 
			float arrTimeInteger, String traceId, String resultIndex, String airlineRemarks, String mode, String journeyClass, 
			String terminalDep, String terminalArr, Integer baggage, Integer cabinBaggage, Product product) {
		this.id = id;
		this.pnr = pnr;
		this.totalSeats = totalSeats;
		this.uploadSeats = uploadSeats;
		this.flightNum = flightNum;
		this.date = date;
		this.depTime = depTime;
		this.arrTime = arrTime;
		this.priceADT = priceADT;
		this.priceINF = priceINF;
		this.markupADT = markupADT;
		this.markupINF = markupINF;
		this.cityOne = cityOne;
		this.cityTwo = cityTwo;
		this.enabled = enabled;
		this.inStock = inStock;
		this.product = product;
		this.stopNum = stopNum;
		this.duration = duration;
		this.brand = brand;
		this.arrTimeInteger = arrTimeInteger;
		this.depTimeInteger = depTimeInteger;
		this.traceId = traceId;
		this.resultIndex = resultIndex;
		this.airlineRemarks = airlineRemarks;
		this.mode = mode;
		this.journeyClass = journeyClass;
		this.terminalDep = terminalDep;
		this.terminalArr = terminalArr;
		this.baggage = baggage;
		this.cabinBaggage = cabinBaggage;
	}
	
	public ProductDetail(String pnr, String totalSeats, String uploadSeats, String flightNum, Date date, String depTime,
			String arrTime, float priceADT, float priceINF, float markupADT, float markupINF, String cityOne,
			String cityTwo, boolean inStock, boolean enabled, int stopNum, Integer duration, String brand, float depTimeInteger, 
			float arrTimeInteger, String mode, String journeyClass, String terminalDep, String terminalArr, Integer baggage, 
			Integer cabinBaggage, String traceId, Product product) {
		this.pnr = pnr;
		this.totalSeats = totalSeats;
		this.uploadSeats = uploadSeats;
		this.flightNum = flightNum;
		this.date = date;
		this.depTime = depTime;
		this.arrTime = arrTime;
		this.priceADT = priceADT;
		this.priceINF = priceINF;
		this.markupADT = markupADT;
		this.markupINF = markupINF;
		this.cityOne = cityOne;
		this.cityTwo = cityTwo;
		this.enabled = enabled;
		this.inStock = inStock;
		this.product = product;
		this.stopNum = stopNum;
		this.duration = duration;
		this.brand = brand;
		this.arrTimeInteger = arrTimeInteger;
		this.depTimeInteger = depTimeInteger;
		this.mode = mode;
		this.journeyClass = journeyClass;
		this.terminalDep = terminalDep;
		this.terminalArr = terminalArr;
		this.baggage = baggage;
		this.cabinBaggage = cabinBaggage;
		this.traceId = traceId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPnr() {
		return pnr;
	}

	public void setPnr(String pnr) {
		this.pnr = pnr;
	}

	public String getTotalSeats() {
		return totalSeats;
	}

	public void setTotalSeats(String totalSeats) {
		this.totalSeats = totalSeats;
	}

	public String getUploadSeats() {
		return uploadSeats;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	public void setUploadSeats(String uploadSeats) {
		this.uploadSeats = uploadSeats;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public float getDepTimeInteger() {
		return depTimeInteger;
	}

	public void setDepTimeInteger(Integer depTimeInteger) {
		this.depTimeInteger = depTimeInteger;
	}

	public float getArrTimeInteger() {
		return arrTimeInteger;
	}

	public void setArrTimeInteger(Integer arrTimeInteger) {
		this.arrTimeInteger = arrTimeInteger;
	}

	public int getStopNum() {
		return stopNum;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public void setStopNum(int stopNum) {
		this.stopNum = stopNum;
	}

	public List<Stop> getStops() {
		return stops;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public void setStops(List<Stop> stops) {
		this.stops = stops;
	}

	public Integer getBaggage() {
		return baggage;
	}

	public void setBaggage(Integer baggage) {
		this.baggage = baggage;
	}

	public Integer getCabinBaggage() {
		return cabinBaggage;
	}

	public void setCabinBaggage(Integer cabinBaggage) {
		this.cabinBaggage = cabinBaggage;
	}

	public String getJourneyClass() {
		return journeyClass;
	}

	public void setJourneyClass(String journeyClass) {
		this.journeyClass = journeyClass;
	}

	public String getTerminalDep() {
		return terminalDep;
	}

	public void setTerminalDep(String terminalDep) {
		this.terminalDep = terminalDep;
	}

	public String getTerminalArr() {
		return terminalArr;
	}

	public void setTerminalArr(String terminalArr) {
		this.terminalArr = terminalArr;
	}

	public String getCityOne() {
		return cityOne;
	}

	public void setCityOne(String cityOne) {
		this.cityOne = cityOne;
	}

	public String getCityTwo() {
		return cityTwo;
	}

	public void setCityTwo(String cityTwo) {
		this.cityTwo = cityTwo;
	}

	public String getFlightNum() {
		return flightNum;
	}
	
	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public void setFlightNum(String flightNum) {
		this.flightNum = flightNum;
	}

	public Date getDate() {
		return date;
	} 

	public void setDate(Date date) {
		this.date = date;
	}

	public boolean isInStock() {
		return inStock;
	}

	public void setInStock(boolean inStock) {
		this.inStock = inStock;
	}

	public String getDepTime() {
		return depTime;
	}

	public void setDepTime(String depTime) {
		this.depTime = depTime;
	}

	public String getArrTime() {
		return arrTime;
	}

	public void setArrTime(String arrTime) {
		this.arrTime = arrTime;
	}

	public float getPriceADT() {
		return priceADT;
	}

	public void setPriceADT(float priceADT) {
		this.priceADT = priceADT;
	}

	public float getPriceINF() {
		return priceINF;
	}

	public void setPriceINF(float priceINF) {
		this.priceINF = priceINF;
	}

	public float getMarkupADT() {
		return markupADT;
	}

	public void setMarkupADT(float markupADT) {
		this.markupADT = markupADT;
	}

	public float getMarkupINF() {
		return markupINF;
	}

	public void setMarkupINF(float markupINF) {
		this.markupINF = markupINF;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}
	
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

	public String getAirlineRemarks() {
		return airlineRemarks;
	}

	public void setAirlineRemarks(String airlineRemarks) {
		this.airlineRemarks = airlineRemarks;
	}

	public void setDepTimeInteger(float depTimeInteger) {
		this.depTimeInteger = depTimeInteger;
	}

	public void setArrTimeInteger(float arrTimeInteger) {
		this.arrTimeInteger = arrTimeInteger;
	}

	public List<CartItem> getCartItems() {
		return cartItems;
	}

	public void setCartItems(List<CartItem> cartItems) {
		this.cartItems = cartItems;
	}

	public List<TravellerDetail> getTravellerDetails() {
		return travellerDetails;
	}

	public void setTravellerDetails(List<TravellerDetail> travellerDetails) {
		this.travellerDetails = travellerDetails;
	}

	public void addBooking(Customer customer) {
		this.cartItems.add(new CartItem(customer, this));
	}
	
	public void addContactBooking(Customer customer, int quantity, double totalPrice, String email, BigInteger phoneNum) {
		this.cartItems.add(new CartItem(customer, this, quantity, totalPrice, email, phoneNum));
	}
	
	public void addTravellerDetails(String salutation, String firstName, String lastName, Date dob, CartItem cartItem) {
		this.travellerDetails.add(new TravellerDetail(salutation, firstName, lastName, dob, this, cartItem));
	}
	
	public void addTravellerDetails(String salutation, String firstName, String lastName, Date dob, CartItem cartItem, String paxType, Integer baggageWT, Integer cabinBaggage) {
		this.travellerDetails.add(new TravellerDetail(salutation, firstName, lastName, dob, this, cartItem, paxType, baggageWT, cabinBaggage));
	}
	
	public void addStopDetails(String cityName, String depTime, String arrTime, String totalTime) {
		this.stops.add(new Stop(cityName, depTime, arrTime, totalTime, this));
	}
	
	public void addStopDetails(int id, String cityName, String depTime, String arrTime, String totalTime) {
		this.stops.add(new Stop(id, cityName, depTime, arrTime, totalTime, this));
	}
	
	@Transient
	public String getDestinationName() {
		return cityOne + " " + cityTwo;
	}

	@Override
	public String toString() {
		return "ProductDetail [id=" + id + ", pnr=" + pnr + ", totalSeats=" + totalSeats + ", uploadSeats="
				+ uploadSeats + ", flightNum=" + flightNum + ", date=" + date + ", depTime=" + depTime + ", arrTime="
				+ arrTime + ", depTimeInteger=" + depTimeInteger + ", arrTimeInteger=" + arrTimeInteger + ", priceADT="
				+ priceADT + ", priceINF=" + priceINF + ", markupADT=" + markupADT + ", markupINF=" + markupINF
				+ ", cityOne=" + cityOne + ", cityTwo=" + cityTwo + ", journeyClass=" + journeyClass + ", terminalDep="
				+ terminalDep + ", terminalArr=" + terminalArr + ", baggage=" + baggage + ", cabinBaggage="
				+ cabinBaggage + ", duration=" + duration + ", brand=" + brand + ", stopNum=" + stopNum + ", enabled="
				+ enabled + ", inStock=" + inStock + ", traceId=" + traceId + ", resultIndex=" + resultIndex
				+ ", airlineRemarks=" + airlineRemarks + ", mode=" + mode + ", product=" + product + ", stops=" + stops
				+ ", cartItems=" + cartItems + ", travellerDetails=" + travellerDetails + ", order=" + order + "]";
	}

	
}
