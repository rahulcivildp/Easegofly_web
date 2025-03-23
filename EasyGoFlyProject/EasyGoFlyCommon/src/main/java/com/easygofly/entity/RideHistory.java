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
@Table(name = "ride_histories")
public class RideHistory {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "Pickup_Location")
	private String pickupLocation;
	
	@Column(name = "Dropoff_Location")
    private String dropoffLocation;
    private String date;
    private String time;
    private Double route;
    
	
	@ManyToOne
	@JoinColumn(name = "customer_id")
	private Customer customer;
	

    // Getters and Setters
    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getDropoffLocation() {
        return dropoffLocation;
    }

    public void setDropoffLocation(String dropoffLocation) {
        this.dropoffLocation = dropoffLocation;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Double getRoute() {
        return route;
    }

    public void setRoute(Double route) {
        this.route = route;
    }

    public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	// Constructor
 

    // Default constructor
    public RideHistory() {}

    public RideHistory(String pickupLocation, String dropoffLocation, String date, String time, Double route,
			Customer customer) {
		this.pickupLocation = pickupLocation;
		this.dropoffLocation = dropoffLocation;
		this.date = date;
		this.time = time;
		this.route = route;
		this.customer = customer;
	}

	// toString Method
    @Override
    public String toString() {
        return "RideDetails{" +
                "pickupLocation='" + pickupLocation + '\'' +
                ", dropoffLocation='" + dropoffLocation + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", route=" + route +
                '}';
    }
}
