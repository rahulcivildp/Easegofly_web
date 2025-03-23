package com.easygofly.entity;

import java.beans.Transient;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "cabs")
public class Cab {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    private String type;

    private int seating;

    @Column(name = "booking_fare")
    private double bookingFare;

    @Column(name = "fuel_type")
    private String fuelType;

    private String color;

    @Column(name = "max_speed")
    private int maxSpeed;

    @Column(name = "air_conditioning")
    private String airConditioning;

    private String wifi;

    private String license;

    private String features;

	private String photos;
    
    
    
	public Cab() {}

	public Cab(String name, String type, int seating, double bookingFare, String fuelType, String color, int maxSpeed,
			String airConditioning, String wifi, String license, String features) {
		this.name = name;
		this.type = type;
		this.seating = seating;
		this.bookingFare = bookingFare;
		this.fuelType = fuelType;
		this.color = color;
		this.maxSpeed = maxSpeed;
		this.airConditioning = airConditioning;
		this.wifi = wifi;
		this.license = license;
		this.features = features;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getSeating() {
		return seating;
	}

	public void setSeating(int seating) {
		this.seating = seating;
	}

	public double getBookingFare() {
		return bookingFare;
	}

	public void setBookingFare(double bookingFare) {
		this.bookingFare = bookingFare;
	}

	public String getFuelType() {
		return fuelType;
	}

	public void setFuelType(String fuelType) {
		this.fuelType = fuelType;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public int getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(int maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public String getAirConditioning() {
		return airConditioning;
	}

	public void setAirConditioning(String airConditioning) {
		this.airConditioning = airConditioning;
	}

	public String getWifi() {
		return wifi;
	}

	public void setWifi(String wifi) {
		this.wifi = wifi;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public String getFeatures() {
		return features;
	}

	public void setFeatures(String features) {
		this.features = features;
	}

	
	public String getPhotos() {
		return photos;
	}

	public void setPhotos(String photos) {
		this.photos = photos;
	}
    


	@Transient
	public String getPhotosImagePath() {
		if(id == null || photos == null) {
			return "/images/user.png";
		}
		return "/cab-photos/" + this.id + "/" + this.photos;
	}
    
}
