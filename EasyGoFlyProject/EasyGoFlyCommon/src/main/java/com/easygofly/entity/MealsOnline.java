package com.easygofly.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "meals")
public class MealsOnline {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false)
	private Integer id;
	
	private String name;
	
	private String price;
	
	private String code;
	
	private String quantity;

	@OneToOne(mappedBy = "mealOnline")
	private TravellerDetail travellerDetail;
	
	public MealsOnline() {}

	public MealsOnline(Integer id, String name, String price, String code, String quantity) {
		this.id = id;
		this.name = name;
		this.price = price;
		this.code = code;
		this.quantity = quantity;
	}

	public MealsOnline(String name, String price, String code, String quantity,
			TravellerDetail travellerDetail) {
		this.name = name;
		this.price = price;
		this.code = code;
		this.quantity = quantity;
		this.travellerDetail = travellerDetail;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	public TravellerDetail getTravellerDetail() {
		return travellerDetail;
	}

	public void setTravellerDetail(TravellerDetail travellerDetail) {
		this.travellerDetail = travellerDetail;
	}

	@Override
	public String toString() {
		return "MealsOnline [id=" + id + ", price=" + price + ", code=" + code + ", quantity="
				+ quantity + "]";
	}
	
}
