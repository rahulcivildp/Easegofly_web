package com.easygofly.entity;


public class BusBoardingPointDetails {

	private Integer id;
	
	private Integer cityPointIndex;
	
	private String cityPointLocation;
	
	private String cityPointName;
	
	private String cityPointTime;
	
	private String CityPointLandmark;
	
	private String CityPointContactNumber;
	
	private String CityPointAddress;

	
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
	
	
	
	
}
