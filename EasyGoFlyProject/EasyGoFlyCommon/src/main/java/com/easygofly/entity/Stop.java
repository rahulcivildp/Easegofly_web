package com.easygofly.entity;
 
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "stops")
public class Stop {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(nullable = false, name = "city_name")
	private String cityName;
	
	@Column(nullable = false)
	private String depTime;
	
	@Column(nullable = false)
	private String arrTime;
	
	@Column(nullable = false)
	private String totalTime;
	
	@Column(nullable = false)
	private String goundTime;

	private String duration;
	
	@ManyToOne
	@JoinColumn(name = "product_detail_id")
	@JsonBackReference
	private ProductDetail productDetail;

	private String cityNameDep;
	
	private String flightNu;
	
	private String brand;
	
	
	public Stop() {}

	public Stop(String cityName, String depTime, String arrTime, String totalTime,
			ProductDetail productDetail) {
		this.cityName = cityName;
		this.depTime = depTime;
		this.arrTime = arrTime;
		this.totalTime = totalTime;
		this.productDetail = productDetail;
	}

	public Stop(int id, String cityOne, String depTime, String arrTime, String totalTime,
			ProductDetail productDetail) {
		this.id = id;
		this.cityName = cityOne;
		this.depTime = depTime;
		this.arrTime = arrTime;
		this.totalTime = totalTime;
		this.productDetail = productDetail;
	}
	
	

	public Stop(String cityName, String cityNameDep, String depTime, String arrTime, String totalTime, String goundTime, String duration, String flightNu, String brand,
			ProductDetail productDetail) {
		this.cityName = cityName;
		this.depTime = depTime;
		this.arrTime = arrTime;
		this.totalTime = totalTime;
		this.goundTime = goundTime;
		this.productDetail = productDetail;
		this.cityNameDep = cityNameDep;
		this.flightNu = flightNu;
		this.brand = brand;
		this.duration = duration;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
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

	public String getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(String totalTime) {
		this.totalTime = totalTime;
	}

	public ProductDetail getProductDetail() {
		return productDetail;
	}

	public void setProductDetail(ProductDetail productDetail) {
		this.productDetail = productDetail;
	}

	
	public String getGoundTime() {
		return goundTime;
	}

	public void setGoundTime(String goundTime) {
		this.goundTime = goundTime;
	}

	public String getCityNameDep() {
		return cityNameDep;
	}

	public void setCityNameDep(String cityNameDep) {
		this.cityNameDep = cityNameDep;
	}

	public String getFlightNu() {
		return flightNu;
	}

	public void setFlightNu(String flightNu) {
		this.flightNu = flightNu;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}
	
}
