package com.easygofly.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "checkout_infos")
public class CheckoutInfo {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "flight_cost")
	private double flightCost;

	@Column(name = "flight_service_cost")
	private double flightServiceCost;
	
	@Column(name = "flight_gst_cost")
	private double flightGSTCost;
	
	@Column(name = "meal")
	private double meal;
	
	@Column(name = "baggage")
	private double baggage;
	
	@Column(name = "seat")
	private double seat;
	
	@Column(name = "payment_total")
	private double paymentTotal;

	@Column(name = "order_date")
	private Date orderDate;
	
	
	public CheckoutInfo() {}
	
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public double getFlightCost() {
		return flightCost;
	}
	public void setFlightCost(double flightCost) {
		this.flightCost = flightCost;
	}
	public double getFlightServiceCost() {
		return flightServiceCost;
	}
	public void setFlightServiceCost(double flightServiceCost) {
		this.flightServiceCost = flightServiceCost;
	}
	public double getFlightGSTCost() {
		return flightGSTCost;
	}
	public void setFlightGSTCost(double flightGSTCost) {
		this.flightGSTCost = flightGSTCost;
	}
	public double getPaymentTotal() {
		return paymentTotal;
	}
	public void setPaymentTotal(double paymentTotal) {
		this.paymentTotal = paymentTotal;
	}
	public Date getOrderDate() {
		return orderDate;
	}
	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}


	public double getMeal() {
		return meal;
	}


	public void setMeal(double meal) {
		this.meal = meal;
	}


	public double getBaggage() {
		return baggage;
	}


	public void setBaggage(double baggage) {
		this.baggage = baggage;
	}


	public double getSeat() {
		return seat;
	}


	public void setSeat(double seat) {
		this.seat = seat;
	}
	
}
