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
@Table(name = "tbo_hotel_rooms")
public class TBORoom {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(name = "room_name")
	private String roomName;

	@Column(name = "room_price")
	private double roomPrice;

	private double tax;
	
	@Column(name = "agent_commission")
	private double agentCommission;
	
	@Column(name = "service_tax")
	private double serviceTax;
	
	@Column(name = "from_date")
	private String fromDate;
	
	@Column(name = "charge_type")
	private String chargeType;
	
	@Column(name = "cancellation_charge")
	private Integer cancellationCharge;
	
	@ManyToOne
	@JoinColumn(name = "tbo_hotel_id")
	private TBOHotel tboHotel;

	public TBORoom() {}

	public TBORoom(String roomName, double roomPrice, double tax, double agentCommission, double serviceTax,
			String fromDate, String chargeType, Integer cancellationCharge, TBOHotel tboHotel) {
		this.roomName = roomName;
		this.roomPrice = roomPrice;
		this.tax = tax;
		this.agentCommission = agentCommission;
		this.serviceTax = serviceTax;
		this.fromDate = fromDate;
		this.chargeType = chargeType;
		this.cancellationCharge = cancellationCharge;
		this.tboHotel = tboHotel;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public double getRoomPrice() {
		return roomPrice;
	}

	public void setRoomPrice(double roomPrice) {
		this.roomPrice = roomPrice;
	}

	public double getTax() {
		return tax;
	}

	public void setTax(double tax) {
		this.tax = tax;
	}

	public double getAgentCommission() {
		return agentCommission;
	}

	public void setAgentCommission(double agentCommission) {
		this.agentCommission = agentCommission;
	}

	public double getServiceTax() {
		return serviceTax;
	}

	public void setServiceTax(double serviceTax) {
		this.serviceTax = serviceTax;
	}

	public String getFromDate() {
		return fromDate;
	}

	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}

	public String getChargeType() {
		return chargeType;
	}

	public void setChargeType(String chargeType) {
		this.chargeType = chargeType;
	}

	public Integer getCancellationCharge() {
		return cancellationCharge;
	}

	public void setCancellationCharge(Integer cancellationCharge) {
		this.cancellationCharge = cancellationCharge;
	}

	public TBOHotel getTboHotel() {
		return tboHotel;
	}

	public void setTboHotel(TBOHotel tboHotel) {
		this.tboHotel = tboHotel;
	}

}
