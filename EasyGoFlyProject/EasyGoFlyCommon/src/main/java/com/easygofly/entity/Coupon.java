package com.easygofly.entity;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "coupons")
public class Coupon {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(nullable = false, name = "coupon_code")
	private String couponCode;
	
	@Column(nullable = false, name = "coupon_amount")
	private Integer couponAmount;

	
	
	public Coupon() {}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCouponCode() {
		return couponCode;
	}

	public void setCouponCode(String couponCode) {
		this.couponCode = couponCode;
	}

	public Integer getCouponAmount() {
		return couponAmount;
	}

	public void setCouponAmount(Integer couponAmount) {
		this.couponAmount = couponAmount;
	}

	@Override
	public String toString() {
		return "Coupon [id=" + id + ", couponCode=" + couponCode + ", couponAmount=" + couponAmount + "]";
	}
	

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		
		Coupon cou = (Coupon) obj;
		return id == cou.id && Objects.equals(couponCode, cou.couponCode) && couponAmount == cou.couponAmount;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return Objects.hash(id, couponCode, couponAmount);
	}
}
