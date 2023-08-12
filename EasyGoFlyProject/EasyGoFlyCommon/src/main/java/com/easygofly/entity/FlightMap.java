package com.easygofly.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "flight_maps")
public class FlightMap {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	private Integer flightIdOne;
	
	private Integer flightIdTwo;
	

	public FlightMap() {}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getFlightIdOne() {
		return flightIdOne;
	}

	public void setFlightIdOne(Integer flightIdOne) {
		this.flightIdOne = flightIdOne;
	}

	public Integer getFlightIdTwo() {
		return flightIdTwo;
	}

	public void setFlightIdTwo(Integer flightIdTwo) {
		this.flightIdTwo = flightIdTwo;
	}
	
}
