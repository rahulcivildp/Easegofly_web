package com.easygofly.site.checkout;

import java.util.Date;

public class CheckoutInfo {
	private double flightCost;
	private float flightServiceCost;
	private float flightGSTCost;
	private double paymentTotal;
	private Date orderDate;
	
	
	public double getFlightCost() {
		return flightCost;
	}
	public void setFlightCost(double flightCost) {
		this.flightCost = flightCost;
	}
	public float getFlightServiceCost() {
		return flightServiceCost;
	}
	public void setFlightServiceCost(float flightServiceCost) {
		this.flightServiceCost = flightServiceCost;
	}
	public float getFlightGSTCost() {
		return flightGSTCost;
	}
	public void setFlightGSTCost(float flightGSTCost) {
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
	
	
}
