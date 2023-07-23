package com.easygofly.entity;

import javax.persistence.Column;

public class MealsOnline {

	private Integer id;
	
	@Column(unique = true)
	private String name;
	
	private String price;
	
	private String code;
	
	private String quantity;

	
	public MealsOnline() {}

	public MealsOnline(Integer id, String name, String price, String code, String quantity) {
		super();
		this.id = id;
		this.name = name;
		this.price = price;
		this.code = code;
		this.quantity = quantity;
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

	@Override
	public String toString() {
		return "MealsOnline [id=" + id + ", price=" + price + ", code=" + code + ", quantity="
				+ quantity + "]";
	}
	
}
