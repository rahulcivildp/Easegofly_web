package com.easygofly.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "bus_boarding_point_details")
public class BusBoardingPointDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "city_point_index")
	private Integer cityPointIndex;

	@Column(name = "city_point_location")
	private String cityPointLocation;

	@Column(name = "city_point_name")
	private String cityPointName;

	@Column(name = "city_point_time")
	private String cityPointTime;

	@Column(name = "city_point_landmark")
	private String CityPointLandmark;

	@Column(name = "city_point_contact_number")
	private String CityPointContactNumber;

	@Column(name = "city_point_address")
	private String CityPointAddress;
	
	@ManyToOne
	@JoinColumn(name = "bus_id")
	private Bus bus;

	
	public BusBoardingPointDetails() {}

	public BusBoardingPointDetails(Integer cityPointIndex, String cityPointLocation, String cityPointName,
			String cityPointTime, String cityPointLandmark, String cityPointContactNumber, String cityPointAddress) {
		this.cityPointIndex = cityPointIndex;
		this.cityPointLocation = cityPointLocation;
		this.cityPointName = cityPointName;
		this.cityPointTime = cityPointTime;
		CityPointLandmark = cityPointLandmark;
		CityPointContactNumber = cityPointContactNumber;
		CityPointAddress = cityPointAddress;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getCityPointIndex() {
		return cityPointIndex;
	}

	public void setCityPointIndex(Integer cityPointIndex) {
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

	public String getCityPointLandmark() {
		return CityPointLandmark;
	}

	public void setCityPointLandmark(String cityPointLandmark) {
		CityPointLandmark = cityPointLandmark;
	}

	public String getCityPointContactNumber() {
		return CityPointContactNumber;
	}

	public void setCityPointContactNumber(String cityPointContactNumber) {
		CityPointContactNumber = cityPointContactNumber;
	}

	public String getCityPointAddress() {
		return CityPointAddress;
	}

	public void setCityPointAddress(String cityPointAddress) {
		CityPointAddress = cityPointAddress;
	}

	
	public Bus getBus() {
		return bus;
	}

	public void setBus(Bus bus) {
		this.bus = bus;
	}
	
	
	
	
}
