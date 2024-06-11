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
@Table(name = "transactions")
public class Transaction {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(nullable = false)
	private String orderId;
	
	@Column()
	private String paymentMode;
	
	@Column()
	private String bank;
	
	@Column()
	private String paymentMethod;
	
	@Column(nullable = false)
	private String responseDescription;
	
	@Column(nullable = false)
	private String pgTransId;
	
	@Column(nullable = false)
	private String pgTransTime;
	
	@Column(nullable = false)
	private String amount;
	
	@Column()
	private String doRedirect;
	
	@Column(nullable = false)
	private String responseCode;
	
	@ManyToOne
	@JoinColumn(name = "customer_id")
	private Customer customer;

	
	
	
	public Transaction() {}

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

	public String getPaymentMode() {
		return paymentMode;
	}

	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
	}

	public String getBank() {
		return bank;
	}

	public void setBank(String bank) {
		this.bank = bank;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public String getResponseDescription() {
		return responseDescription;
	}

	public void setResponseDescription(String responseDescription) {
		this.responseDescription = responseDescription;
	}

	public String getPgTransId() {
		return pgTransId;
	}

	public void setPgTransId(String pgTransId) {
		this.pgTransId = pgTransId;
	}

	public String getPgTransTime() {
		return pgTransTime;
	}

	public void setPgTransTime(String pgTransTime) {
		this.pgTransTime = pgTransTime;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public String getDoRedirect() {
		return doRedirect;
	}

	public void setDoRedirect(String doRedirect) {
		this.doRedirect = doRedirect;
	}
	
	
	
}
