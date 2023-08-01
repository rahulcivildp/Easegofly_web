package com.easygofly.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.criteria.CriteriaBuilder.In;

@Entity
@Table(name = "traveller_details")
public class TravellerDetail {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(name = "salutation")
	private String salutation;
	
	@Column(name = "first_name", length = 45)
	private String firstName;
	
	@Column(name = "last_name", length = 45)
	private String lastName;
	
	@Column(name = "passenger_type", length = 45)
	private String paxType;
	
	@Column(name = "cabin_baggage")
	private Integer cabinBaggage;
	
	@Column(name = "baggage_weight")
	private Integer baggageWT;
	
	private String meal;
	
	private String baggage;
	
	private String seat;
	
	@Column()
	@Temporal(TemporalType.DATE)
	private Date dob;
	
	@ManyToOne
	@JoinColumn(name = "product_detail_id")
	private ProductDetail productDetail;

	@ManyToOne
	@JoinColumn(name = "cart_item_id")
	private CartItem cartItem;
	
	@ManyToOne
	@JoinColumn(name = "order_id")
	private Order order;

	
	public TravellerDetail() {}

	public TravellerDetail(String salutation, String firstName, String lastName, Date dob, ProductDetail productDetail, CartItem cartItem) {
		this.salutation = salutation;
		this.firstName = firstName;
		this.lastName = lastName;
		this.dob = dob;
		this.productDetail = productDetail;
		this.cartItem = cartItem;
	}
	
	public TravellerDetail(String salutation, String firstName, String lastName, Date dob, ProductDetail productDetail, CartItem cartItem, String paxType, Integer baggageWT, Integer cabinBaggage) {
		this.salutation = salutation;
		this.firstName = firstName;
		this.lastName = lastName;
		this.dob = dob;
		this.productDetail = productDetail;
		this.cartItem = cartItem;
		this.paxType = paxType;
		this.baggageWT = baggageWT;
		this.cabinBaggage = cabinBaggage;
	}

	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}

	public String getSalutation() {
		return salutation;
	}

	public void setSalutation(String salutation) {
		this.salutation = salutation;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Date getDob() {
		return dob;
	}

	public void setDob(Date dob) {
		this.dob = dob;
	}

	public Integer getBaggageWT() {
		return baggageWT;
	}

	public void setBaggageWT(Integer baggageWT) {
		this.baggageWT = baggageWT;
	}

	public ProductDetail getProductDetail() {
		return productDetail;
	}

	public void setProductDetail(ProductDetail productDetail) {
		this.productDetail = productDetail;
	}

	public Integer getCabinBaggage() {
		return cabinBaggage;
	}

	public void setCabinBaggage(Integer cabinBaggage) {
		this.cabinBaggage = cabinBaggage;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public CartItem getCartItem() {
		return cartItem;
	}

	public void setCartItem(CartItem cartItem) {
		this.cartItem = cartItem;
	}
	
	public String getMeal() {
		return meal;
	}

	public void setMeal(String meal) {
		this.meal = meal;
	}

	public String getBaggage() {
		return baggage;
	}

	public void setBaggage(String baggage) {
		this.baggage = baggage;
	}

	public String getSeat() {
		return seat;
	}

	public void setSeat(String seat) {
		this.seat = seat;
	}

	public String getPaxType() {
		return paxType;
	}

	public void setPaxType(String paxType) {
		this.paxType = paxType;
	}

	@Override
	public String toString() {
		return "TravellerDetail [salutation=" + salutation + ", firstName=" + firstName + ", lastName=" + lastName
				+ ", dob=" + dob + ", productDetail=" + productDetail + ", cartItem=" + cartItem + ", order=" + order
				+ "]";
	}

}
