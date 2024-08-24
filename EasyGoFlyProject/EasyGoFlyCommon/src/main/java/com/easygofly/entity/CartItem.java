package com.easygofly.entity;

import java.math.BigInteger;
import java.util.ArrayList;
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
import javax.persistence.Transient;

@Entity
@Table(name = "cart_items")
public class CartItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@ManyToOne
	@JoinColumn(name = "customer_id")
	private Customer customer;
	
	@ManyToOne
	@JoinColumn(name = "product_detail_id")
	private ProductDetail productDetail;
	
	private int quantity;
	
	private float serviceCharge;
	
	private double totalPrice;
	
	private boolean ordered;
	
	@Column(unique = false)
	private String email;

	private String cartMode;
	
	private BigInteger phoneNum;
 
	@OneToMany(mappedBy = "cartItem", cascade = CascadeType.ALL)
	private List<TravellerDetail> travellerDetails = new ArrayList<>();
	
	@OneToMany(mappedBy = "cartItem", cascade = CascadeType.ALL)
	private List<SearchHistory> searchHistory = new ArrayList<>();
	
	
	public CartItem() {}
	
	
	public CartItem(Customer customer, ProductDetail productDetail) {
		this.customer = customer;
		this.productDetail = productDetail;
	}
	
	public CartItem(ProductDetail productDetail) {
		this.productDetail = productDetail;
	}

	public CartItem(Customer customer, ProductDetail productDetail, int quantity, double totalPrice, String email, BigInteger phoneNum) {
		this.customer = customer;
		this.productDetail = productDetail;
		this.quantity = quantity;
		this.email = email;
		this.phoneNum = phoneNum;
		this.totalPrice = totalPrice;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public boolean isOrdered() {
		return ordered;
	}

	public void setOrdered(boolean ordered) {
		this.ordered = ordered;
	}

	public ProductDetail getProductDetail() {
		return productDetail;
	}

	public void setProductDetail(ProductDetail productDetail) {
		this.productDetail = productDetail;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	
	public String getCartMode() {
		return cartMode;
	}


	public void setCartMode(String cartMode) {
		this.cartMode = cartMode;
	}


	public double getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(double totalPrice) {
		this.totalPrice = totalPrice;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public BigInteger getPhoneNum() {
		return phoneNum;
	}

	public void setPhoneNum(BigInteger phoneNum) {
		this.phoneNum = phoneNum;
	}
 
	public List<TravellerDetail> getTravellerDetails() {
		return travellerDetails;
	}

	public void setTravellerDetails(List<TravellerDetail> travellerDetails) {
		this.travellerDetails = travellerDetails;
	}

	public List<SearchHistory> getSearchHistory() {
		return searchHistory;
	}

	public void setSearchHistory(List<SearchHistory> searchHistory) {
		this.searchHistory = searchHistory;
	}

	public float getServiceCharge() {
		return serviceCharge;
	}

	public void setServiceCharge(float serviceCharge) {
		this.serviceCharge = serviceCharge;
	}


	@Transient
	public float getServiceCost() {
		return serviceCharge;
	}
	
	public void setServiceCost(float serviceCharge) {
		this.serviceCharge = serviceCharge;
	}


	@Override
	public String toString() {
		return "CartItem [id=" + id + ", customer=" + customer.getFullName() + ", productDetail=" + productDetail.getDestinationName() + ", quantity="
				+ quantity + "]";
	}
	
}
