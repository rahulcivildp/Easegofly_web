package com.easygofly.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "wallets")
public class Wallet {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false)
	private Integer id;
	
	private Integer balance;
	
	@Column(name = "temp_value")
	private Integer tempValue;
	
	@OneToOne(mappedBy = "wallet")
	private Customer customer;
	
	@OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL)
	private List<RechargeHistory> rechargeHistories = new ArrayList<>();
	
	
	public Wallet() {}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getBalance() {
		return balance;
	}

	public void setBalance(Integer balance) {
		this.balance = balance;
	}

	public Integer getTempValue() {
		return tempValue;
	}

	public void setTempValue(Integer tempValue) {
		this.tempValue = tempValue;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public List<RechargeHistory> getRechargeHistories() {
		return rechargeHistories;
	}

	public void setRechargeHistories(List<RechargeHistory> rechargeHistories) {
		this.rechargeHistories = rechargeHistories;
	}
	
	
}
