package com.easygofly.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "bus_orders")
public class BusOrder {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(nullable = false, length = 256)
	private String name;
	
	@Column(name = "bus_booking_status")
	private String busBookingStatus;
	
	@Column(name = "invoice_amount")
	private String invoiceAmount;
	
	@Column(name = "invoice_number")
	private String invoiceNumber;
	
	@Column(name = "tbo_book_bus_id")
	private Integer tboBookBusId;
	
	@Column(name = "ticket_no")
	private String ticketNo;
	
	@Column(name = "travel_operator_pnr")
	private String travelOperatorPNR;
	
	private double price;
	
	@Column(name = "created_time")
	private Date createdTime;

	@Enumerated(EnumType.STRING)
	@Column(name = "order_status")
	private OrderStatus orderStatus;

	@ManyToOne
	@JoinColumn(name = "customer_id")
	private Customer customer;

	@ManyToOne
	@JoinColumn(name = "bus_history_id")
	private BusHistory busHistory;

	@ManyToOne
	@JoinColumn(name = "bus_id")
	private Bus bus;

	@ManyToOne
	@JoinColumn(name = "total_transaction_id")
	private TotalTransaction totalTransaction;
	
	
	public BusOrder() {}

	public BusOrder(String name, double price, Date createdTime, OrderStatus orderStatus, Customer customer,
			BusHistory busHistory, Bus bus) {
		this.name = name;
		this.price = price;
		this.createdTime = createdTime;
		this.orderStatus = orderStatus;
		this.customer = customer;
		this.busHistory = busHistory;
		this.bus = bus;
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

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
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

	public BusHistory getBusHistory() {
		return busHistory;
	}

	public void setBusHistory(BusHistory busHistory) {
		this.busHistory = busHistory;
	}

	public Bus getBus() {
		return bus;
	}

	public void setBus(Bus bus) {
		this.bus = bus;
	}

	public String getBusBookingStatus() {
		return busBookingStatus;
	}

	public void setBusBookingStatus(String busBookingStatus) {
		this.busBookingStatus = busBookingStatus;
	}

	public String getInvoiceAmount() {
		return invoiceAmount;
	}

	public void setInvoiceAmount(String invoiceAmount) {
		this.invoiceAmount = invoiceAmount;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public Integer getTboBookBusId() {
		return tboBookBusId;
	}

	public void setTboBookBusId(Integer tboBookBusId) {
		this.tboBookBusId = tboBookBusId;
	}

	public String getTicketNo() {
		return ticketNo;
	}

	public void setTicketNo(String ticketNo) {
		this.ticketNo = ticketNo;
	}

	public String getTravelOperatorPNR() {
		return travelOperatorPNR;
	}

	public void setTravelOperatorPNR(String travelOperatorPNR) {
		this.travelOperatorPNR = travelOperatorPNR;
	}

	
	public TotalTransaction getTotalTransaction() {
		return totalTransaction;
	}

	public void setTotalTransaction(TotalTransaction totalTransaction) {
		this.totalTransaction = totalTransaction;
	}
	
	
	

}
