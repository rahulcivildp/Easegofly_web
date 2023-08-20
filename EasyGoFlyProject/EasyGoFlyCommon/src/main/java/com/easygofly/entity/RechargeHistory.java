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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity 
@Table(name = "recharge_history")
public class RechargeHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(nullable = false, name = "recharge_amount")
	private Integer rechargeAmount;
	
	@Enumerated(EnumType.STRING)
	private RechargeHistoryStatus rechargeHistoryStatus; 
	
	@Column(nullable = false, name = "transaction")
	private String transaction;
	
	@Column(name = "zaakpay_transaction_id")
	private String zaakpaytransactionId;
	
	@Temporal(TemporalType.DATE)
	private Date date;
	
	@ManyToOne
	@JoinColumn(name = "wallet_id")
	private Wallet wallet;

	
	
	public RechargeHistory() {}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Integer getRechargeAmount() {
		return rechargeAmount;
	}

	public void setRechargeAmount(Integer rechargeAmount) {
		this.rechargeAmount = rechargeAmount;
	}

	public RechargeHistoryStatus getRechargeHistoryStatus() {
		return rechargeHistoryStatus;
	}

	public void setRechargeHistoryStatus(RechargeHistoryStatus rechargeHistoryStatus) {
		this.rechargeHistoryStatus = rechargeHistoryStatus;
	}

	public String getTransaction() {
		return transaction;
	}

	public void setTransaction(String transaction) {
		this.transaction = transaction;
	}

	public Wallet getWallet() {
		return wallet;
	}

	public void setWallet(Wallet wallet) {
		this.wallet = wallet;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getZaakpaytransactionId() {
		return zaakpaytransactionId;
	}

	public void setZaakpaytransactionId(String zaakpaytransactionId) {
		this.zaakpaytransactionId = zaakpaytransactionId;
	}
	
	
	
}
