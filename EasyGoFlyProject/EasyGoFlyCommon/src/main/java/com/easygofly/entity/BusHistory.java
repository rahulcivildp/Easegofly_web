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
@Table(name = "bus_histories")
public class BusHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false, name = "dept_date", length = 40)
	private Date deptDate;

	@Column(nullable = false, name = "city_id_one", length = 40)
	private String cityIdOne;

	@Column(nullable = false, name = "city_id_two", length = 40)
	private String cityIdTwo;

	@ManyToOne
	@JoinColumn(name = "customer_id")
	private Customer customer;

	
	
	public BusHistory() {}

	public BusHistory(Date deptDate, String cityIdOne, String cityIdTwo, Customer customer) {
		this.deptDate = deptDate;
		this.cityIdOne = cityIdOne;
		this.cityIdTwo = cityIdTwo;
		this.customer = customer;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Date getDeptDate() {
		return deptDate;
	}

	public void setDeptDate(Date deptDate) {
		this.deptDate = deptDate;
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

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
}
