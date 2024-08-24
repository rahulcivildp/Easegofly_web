package com.easygofly.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "search_history")
public class SearchHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(nullable = false, name = "city_one")
	private String cityOne;
	
	@Column(nullable = false, name = "city_two")
	private String cityTwo;
	
	@Column(nullable = false, length = 3, name = "passenger_num")
	private Integer passengerNum;
	
	@Column(nullable = false, name = "journey_class")
	private String journeyClass;
	
	@Column(nullable = false, length = 3)
	private Integer adultNum;
	
	@Column(length = 3)
	private Integer childNum;
	
	@Column(length = 5)
	private Integer infantNum;
	
	@Column()
	private Integer cart_id;
	
	@Column(nullable = false, name = "trip_type")
	private String tripType;
	
	@Column(nullable = false)
	@Temporal(TemporalType.DATE)
	private Date date;
	
	@Column(name = "return_date")
	@Temporal(TemporalType.DATE)
	private Date returnDate;
	
	@ManyToOne
	@JoinColumn(name = "customer_id")
	private Customer customer;
	
	@ManyToOne
	@JoinColumn(name = "cart_item_id")
	private CartItem cartItem;
	 
	public SearchHistory() {}

	public SearchHistory(String cityOne, String cityTwo, Integer passengerNum, String journeyClass, Integer adultNum,
			Integer childNum, Integer infantNum, String tripType, Date date, Customer customer) {
		this.cityOne = cityOne;
		this.cityTwo = cityTwo;
		this.passengerNum = passengerNum;
		this.journeyClass = journeyClass;
		this.adultNum = adultNum;
		this.childNum = childNum;
		this.infantNum = infantNum;
		this.tripType = tripType;
		this.date = date;
		this.customer = customer;
	}
	
	public SearchHistory(String cityOne, String cityTwo, Integer passengerNum, String journeyClass, Integer adultNum,
			Integer childNum, Integer infantNum, String tripType, Date date) {
		this.cityOne = cityOne;
		this.cityTwo = cityTwo;
		this.passengerNum = passengerNum;
		this.journeyClass = journeyClass;
		this.adultNum = adultNum;
		this.childNum = childNum;
		this.infantNum = infantNum;
		this.tripType = tripType;
		this.date = date;
	}
	
	public SearchHistory(String cityOne, String cityTwo, Integer passengerNum, String journeyClass, Integer adultNum,
			Integer childNum, Integer infantNum, String tripType, Date date, Date returnDate, Customer customer) {
		this.cityOne = cityOne;
		this.cityTwo = cityTwo;
		this.passengerNum = passengerNum;
		this.journeyClass = journeyClass;
		this.adultNum = adultNum;
		this.childNum = childNum;
		this.infantNum = infantNum;
		this.tripType = tripType;
		this.date = date;
		this.returnDate = returnDate;
		this.customer = customer;
	}

	public SearchHistory(Integer id, String cityOne, String cityTwo, Integer passengerNum, String journeyClass,
			Integer adultNum, Integer childNum, Integer infantNum, String tripType, Date date, Customer customer) {
		this.id = id;
		this.cityOne = cityOne;
		this.cityTwo = cityTwo;
		this.passengerNum = passengerNum;
		this.journeyClass = journeyClass;
		this.adultNum = adultNum;
		this.childNum = childNum;
		this.infantNum = infantNum;
		this.tripType = tripType;
		this.date = date;
		this.customer = customer;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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

	public Integer getPassengerNum() {
		return passengerNum;
	}

	public void setPassengerNum(Integer passengerNum) {
		this.passengerNum = passengerNum;
	}

	public String getJourneyClass() {
		return journeyClass;
	}

	public void setJourneyClass(String journeyClass) {
		this.journeyClass = journeyClass;
	}

	public Integer getAdultNum() {
		return adultNum;
	}

	public Integer getCart_id() {
		return cart_id;
	}

	public void setCart_id(Integer cart_id) {
		this.cart_id = cart_id;
	}

	public CartItem getCartItem() {
		return cartItem;
	}

	public void setCartItem(CartItem cartItem) {
		this.cartItem = cartItem;
	}

	public void setAdultNum(Integer adultNum) {
		this.adultNum = adultNum;
	}

	public Integer getChildNum() {
		return childNum;
	}

	public void setChildNum(Integer childNum) {
		this.childNum = childNum;
	}

	public Integer getInfantNum() {
		return infantNum;
	}

	public void setInfantNum(Integer infantNum) {
		this.infantNum = infantNum;
	}

	public String getTripType() {
		return tripType;
	}

	public void setTripType(String tripType) {
		this.tripType = tripType;
	}

	public void setCityTwo(String cityTwo) {
		this.cityTwo = cityTwo;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public Date getReturnDate() {
		return returnDate;
	}

	public void setReturnDate(Date returnDate) {
		this.returnDate = returnDate;
	}

	@Override
	public String toString() {
		return "SearchHistory [id=" + id + ", cityOne=" + cityOne + ", cityTwo=" + cityTwo + ", passengerNum="
				+ passengerNum + ", journeyClass=" + journeyClass + ", adultNum=" + adultNum + ", childNum=" + childNum
				+ ", infantNum=" + infantNum + ", tripType=" + tripType + ", date=" + date + ", customer=" + customer
				+ "]";
	}

}
