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

@Entity
@Table(name = "hotel_histories")
public class HotelHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false, name = "check_in_date", length = 40)
	private Date checkInDate;

	@Column(name = "check_out_date", length = 40)
	private Date checkOutDate;

	@Column(nullable = false, name = "country_code", length = 100)
	private String countryCode;

	@Column(nullable = false, name = "city_id", length = 40)
	private String cityId;

	@Column(nullable = false, name = "no_of_rooms", length = 10)
	private String noOfRooms;

	@Column(nullable = false, name = "no_of_adults", length = 10)
	private String noOfAdults;

	@Column(nullable = false, name = "no_of_children", length = 10)
	private String noOfChild;

	@Column(name = "children_age", length = 20)
	private String childrenAge;

	@Column(nullable = false, name = "is_near_by_search_allowed", length = 20)
	private boolean isNearBySearchAllowed;

	@ManyToOne
	@JoinColumn(name = "customer_id")
	private Customer customer;
	
	
	
	public HotelHistory() {}

	public HotelHistory(Date checkInDate, Date checkOutDate, String countryCode, String cityId, String noOfRooms,
			String noOfAdults, String noOfChild, String childrenAge, boolean isNearBySearchAllowed, Customer customer) {
		super();
		this.checkInDate = checkInDate;
		this.checkOutDate = checkOutDate;
		this.countryCode = countryCode;
		this.cityId = cityId;
		this.noOfRooms = noOfRooms;
		this.noOfAdults = noOfAdults;
		this.noOfChild = noOfChild;
		this.childrenAge = childrenAge;
		this.isNearBySearchAllowed = isNearBySearchAllowed;
		this.customer = customer;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Date getCheckInDate() {
		return checkInDate;
	}

	public void setCheckInDate(Date checkInDate) {
		this.checkInDate = checkInDate;
	}

	public Date getCheckOutDate() {
		return checkOutDate;
	}

	public void setCheckOutDate(Date checkOutDate) {
		this.checkOutDate = checkOutDate;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getCityId() {
		return cityId;
	}

	public void setCityId(String cityId) {
		this.cityId = cityId;
	}

	public String getNoOfRooms() {
		return noOfRooms;
	}

	public void setNoOfRooms(String noOfRooms) {
		this.noOfRooms = noOfRooms;
	}

	public String getNoOfAdults() {
		return noOfAdults;
	}

	public void setNoOfAdults(String noOfAdults) {
		this.noOfAdults = noOfAdults;
	}

	public String getNoOfChild() {
		return noOfChild;
	}

	public void setNoOfChild(String noOfChild) {
		this.noOfChild = noOfChild;
	}

	public String getChildrenAge() {
		return childrenAge;
	}

	public void setChildrenAge(String childrenAge) {
		this.childrenAge = childrenAge;
	}

	public boolean isNearBySearchAllowed() {
		return isNearBySearchAllowed;
	}

	public void setNearBySearchAllowed(boolean isNearBySearchAllowed) {
		this.isNearBySearchAllowed = isNearBySearchAllowed;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
	
	
	
}
