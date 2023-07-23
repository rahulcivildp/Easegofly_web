package com.easygofly.entity;

public class BaggageOnline {

	private Integer id;
	
	private String price;
	
	private String code;
	
	private String weight;
	

	public BaggageOnline() {}
	
	public BaggageOnline(Integer id, String price, String code, String weight) {
		this.id = id;
		this.price = price;
		this.code = code;
		this.weight = weight;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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

	public String getWeight() {
		return weight;
	}

	public void setWeight(String weight) {
		this.weight = weight;
	}
	
	
}
