package com.easygofly.entity;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "hotel_orders")
public class HotelOrder {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(nullable = false, length = 256)
	private String name;
	
	private double price;
	
	@Column(name = "created_time")
	private Date createdTime;

	@Enumerated(EnumType.STRING)
	@Column(name = "order_status")
	private OrderStatus orderStatus;

	@ManyToOne
	@JoinColumn(name = "customer_id")
	private Customer customer;
	
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "hotel_history_id", referencedColumnName = "id")
	private HotelHistory hotelHistory;
	
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "hotel_id", referencedColumnName = "id")
	private Hotel hotel;

	
	
	public HotelOrder() {}

	public HotelOrder(String name, Date createdTime, OrderStatus orderStatus, Customer customer,
			HotelHistory hotelHistory, Hotel hotel) {
		this.name = name;
		this.createdTime = createdTime;
		this.orderStatus = orderStatus;
		this.customer = customer;
		this.hotelHistory = hotelHistory;
		this.hotel = hotel;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	public OrderStatus getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(OrderStatus orderStatus) {
		this.orderStatus = orderStatus;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public HotelHistory getHotelHistory() {
		return hotelHistory;
	}

	public void setHotelHistory(HotelHistory hotelHistory) {
		this.hotelHistory = hotelHistory;
	}

	public Hotel getHotel() {
		return hotel;
	}

	public void setHotel(Hotel hotel) {
		this.hotel = hotel;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}
	
	
}
