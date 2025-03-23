package com.easygofly.entity;

import java.beans.Transient;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "drivers")
public class Driver {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    private double rating;

    private int experience;

    private String location;

    private String contact;

    private String address;

    @Column(name = "covering_distance")
    private int coveringDistance;

    private double latitude;

    private double longitude;
    
	private String photos;
	

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "cab_id", referencedColumnName = "id")
    private Cab cab;

    
	public Driver() {}

	public Driver(String name, double rating, int experience, String location, String contact, String address,
			int coveringDistance, double latitude, double longitude, Cab cab) {
		this.name = name;
		this.rating = rating;
		this.experience = experience;
		this.location = location;
		this.contact = contact;
		this.address = address;
		this.coveringDistance = coveringDistance;
		this.latitude = latitude;
		this.longitude = longitude;
		this.cab = cab;
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

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

	public int getExperience() {
		return experience;
	}

	public void setExperience(int experience) {
		this.experience = experience;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getCoveringDistance() {
		return coveringDistance;
	}

	public void setCoveringDistance(int coveringDistance) {
		this.coveringDistance = coveringDistance;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public Cab getCab() {
		return cab;
	}

	public void setCab(Cab cab) {
		this.cab = cab;
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
		return "/driver-photos/" + this.id + "/" + this.photos;
	}
    
}
