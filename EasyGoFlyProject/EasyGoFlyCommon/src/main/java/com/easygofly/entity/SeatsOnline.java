package com.easygofly.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "seats")
public class SeatsOnline {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false)
	private Integer id;
	
	private String price;
	
	private Integer compartment;
	
	private Integer availablityType;
	
	private Integer deck;
	
	private String rowNo;
	
	private String code;
	
	private Integer seatType;
	
	private String seatNo;
	
	private String craftType;

	@OneToOne(mappedBy = "seatsOnline")
	private TravellerDetail travellerDetail;
	
	
	public SeatsOnline() {}

	public SeatsOnline(Integer id, String price, Integer compartment, Integer availablityType,
			Integer deck, String rowNo, String code, Integer seatType, String seatNo, String craftType) {
		this.id = id;
		this.price = price;
		this.compartment = compartment;
		this.availablityType = availablityType;
		this.deck = deck;
		this.rowNo = rowNo;
		this.code = code;
		this.seatType = seatType;
		this.seatNo = seatNo;
		this.craftType = craftType;
	}

	public SeatsOnline(String price, Integer compartment, Integer availablityType, Integer deck, String rowNo,
			String code, Integer seatType, String seatNo, String craftType, TravellerDetail travellerDetail) {
		this.price = price;
		this.compartment = compartment;
		this.availablityType = availablityType;
		this.deck = deck;
		this.rowNo = rowNo;
		this.code = code;
		this.seatType = seatType;
		this.seatNo = seatNo;
		this.craftType = craftType;
		this.travellerDetail = travellerDetail;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public Integer getCompartment() {
		return compartment;
	}

	public void setCompartment(Integer compartment) {
		this.compartment = compartment;
	}

	public Integer getAvailablityType() {
		return availablityType;
	}

	public void setAvailablityType(Integer availablityType) {
		this.availablityType = availablityType;
	}

	public Integer getDeck() {
		return deck;
	}

	public void setDeck(Integer deck) {
		this.deck = deck;
	}

	public String getRowNo() {
		return rowNo;
	}

	public void setRowNo(String rowNo) {
		this.rowNo = rowNo;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Integer getSeatType() {
		return seatType;
	}

	public void setSeatType(Integer seatType) {
		this.seatType = seatType;
	}

	public String getSeatNo() {
		return seatNo;
	}

	public void setSeatNo(String seatNo) {
		this.seatNo = seatNo;
	}

	public String getCraftType() {
		return craftType;
	}

	public void setCraftType(String craftType) {
		this.craftType = craftType;
	}

	public TravellerDetail getTravellerDetail() {
		return travellerDetail;
	}

	public void setTravellerDetail(TravellerDetail travellerDetail) {
		this.travellerDetail = travellerDetail;
	}
	
	
}
