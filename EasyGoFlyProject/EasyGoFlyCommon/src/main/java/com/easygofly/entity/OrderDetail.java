package com.easygofly.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "order_details")
public class OrderDetail {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	private double flightCost;
	private float flightServiceCost;
	private float flightGSTCost;
	private double paymentTotal;
	private Date orderDate;
	
	@ManyToOne
	@JoinColumn(name = "product_detail_id")
	private ProductDetail productDetail;
	
	@ManyToOne
	@JoinColumn(name = "order_id")
	private Order order;
	
	
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
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public ProductDetail getProductDetail() {
		return productDetail;
	}
	public void setProductDetail(ProductDetail productDetail) {
		this.productDetail = productDetail;
	}
	public Order getOrder() {
		return order;
	}
	public void setOrder(Order order) {
		this.order = order;
	}
	
}
