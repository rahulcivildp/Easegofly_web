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
@Table(name = "sightseeing_histories")
public class SightseeingHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false, name = "city_id")
	private Integer cityId; 

	@Column(nullable = false, name = "country_code")
	private String countryCode; 
	
	@Column(nullable = false, name = "from_date")
	private Date fromDate;
	
	@Column(nullable = false, name = "to_date")
	private Date toDate;

	@Column(nullable = false, name = "adult_count")
	private Integer adultCount; 

	@Column(name = "child_count")
	private Integer childCount;

	@Column(name = "children_age")
	private Integer[] childrenAge;

	@ManyToOne
	@JoinColumn(name = "customer_id")
	private Customer customer;

	
	
	public SightseeingHistory() {}

	public SightseeingHistory(Integer cityId, String countryCode, Date fromDate, Date toDate, Integer adultCount,
			Integer childCount, Integer[] childrenAge, Customer customer) {
		this.cityId = cityId;
		this.countryCode = countryCode;
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.adultCount = adultCount;
		this.childCount = childCount;
		this.childrenAge = childrenAge;
		this.customer = customer;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getCityId() {
		return cityId;
	}

	public void setCityId(Integer cityId) {
		this.cityId = cityId;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public Date getFromDate() {
		return fromDate;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public Date getToDate() {
		return toDate;
	}

	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	public Integer getAdultCount() {
		return adultCount;
	}

	public void setAdultCount(Integer adultCount) {
		this.adultCount = adultCount;
	}

	public Integer getChildCount() {
		return childCount;
	}

	public void setChildCount(Integer childCount) {
		this.childCount = childCount;
	}

	public Integer[] getChildrenAge() {
		return childrenAge;
	}

	public void setChildrenAge(Integer[] childrenAge) {
		this.childrenAge = childrenAge;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
	
	

	
}
