package com.easygofly.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "total_transactions")
public class TotalTransaction {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(nullable = false)
	private String orderId;
	
	private OrderStatus orderStatus;

	private double amount;
	
	private Date createdTime;
	
	private boolean isWallet;
	
	private boolean isZaakPay;
	
	@OneToMany(mappedBy = "totalTransaction", cascade = CascadeType.ALL)
	private List<Order> flightOrders = new ArrayList<>();
	
	@OneToMany(mappedBy = "totalTransaction", cascade = CascadeType.ALL)
	private List<HotelOrder> hotelOrders = new ArrayList<>();
	
	@OneToMany(mappedBy = "totalTransaction", cascade = CascadeType.ALL)
	private List<BusOrder> busOrders = new ArrayList<>();
	
	@OneToMany(mappedBy = "totalTransaction", cascade = CascadeType.ALL)
	private List<RideOrder> rideOrders = new ArrayList<>();

	@ManyToOne
	@JoinColumn(name = "customer_id")
	private Customer customer;

	private Integer trnId;
	
	
	public TotalTransaction() {}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public Integer getTrnId() {
		return trnId;
	}

	public void setTrnId(Integer trnId) {
		this.trnId = trnId;
	}

	public OrderStatus getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(OrderStatus orderStatus) {
		this.orderStatus = orderStatus;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	public boolean isWallet() {
		return isWallet;
	}

	public void setWallet(boolean isWallet) {
		this.isWallet = isWallet;
	}

	public boolean isZaakPay() {
		return isZaakPay;
	}

	public void setZaakPay(boolean isZaakPay) {
		this.isZaakPay = isZaakPay;
	}

	public List<Order> getFlightOrders() {
		return flightOrders;
	}

	public void setFlightOrders(List<Order> flightOrders) {
		this.flightOrders = flightOrders;
	}

	public List<HotelOrder> getHotelOrders() {
		return hotelOrders;
	}

	public void setHotelOrders(List<HotelOrder> hotelOrders) {
		this.hotelOrders = hotelOrders;
	}

	public List<BusOrder> getBusOrders() {
		return busOrders;
	}

	public void setBusOrders(List<BusOrder> busOrders) {
		this.busOrders = busOrders;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	
	public List<RideOrder> getRideOrders() {
		return rideOrders;
	}

	public void setRideOrders(List<RideOrder> rideOrders) {
		this.rideOrders = rideOrders;
	}
	
	

	
}
