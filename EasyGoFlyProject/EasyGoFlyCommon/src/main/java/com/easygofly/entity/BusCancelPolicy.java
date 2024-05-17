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
@Table(name = "bus_cancel_policies")
public class BusCancelPolicy {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(name = "cancellation_charge")
	private double cancellationCharge;
	
	@Column(name = "cancellation_charge_type")
	private Integer cancellationChargeType;
	
	@Column(name = "policy_string")
	private String policyString;
	
	@Column(name = "time_before_dept")
	private String timeBeforeDept;
	
	@Column(name = "from_date")
	private String fromDate;
	
	@Column(name = "to_date")
	private String toDate;
	
	@ManyToOne
	@JoinColumn(name = "bus_id")
	private Bus bus;

	
	public BusCancelPolicy() {}

	public BusCancelPolicy(double cancellationCharge, Integer cancellationChargeType, String policyString,
			String timeBeforeDept, String fromDate, String toDate) {
		this.cancellationCharge = cancellationCharge;
		this.cancellationChargeType = cancellationChargeType;
		this.policyString = policyString;
		this.timeBeforeDept = timeBeforeDept;
		this.fromDate = fromDate;
		this.toDate = toDate;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public double getCancellationCharge() {
		return cancellationCharge;
	}

	public void setCancellationCharge(double cancellationCharge) {
		this.cancellationCharge = cancellationCharge;
	}

	public Integer getCancellationChargeType() {
		return cancellationChargeType;
	}

	public void setCancellationChargeType(Integer cancellationChargeType) {
		this.cancellationChargeType = cancellationChargeType;
	}

	public String getPolicyString() {
		return policyString;
	}

	public void setPolicyString(String policyString) {
		this.policyString = policyString;
	}

	public String getTimeBeforeDept() {
		return timeBeforeDept;
	}

	public void setTimeBeforeDept(String timeBeforeDept) {
		this.timeBeforeDept = timeBeforeDept;
	}

	public String getFromDate() {
		return fromDate;
	}

	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}

	public String getToDate() {
		return toDate;
	}

	public void setToDate(String toDate) {
		this.toDate = toDate;
	}

	public Bus getBus() {
		return bus;
	}

	public void setBus(Bus bus) {
		this.bus = bus;
	}
	
	
	
}
