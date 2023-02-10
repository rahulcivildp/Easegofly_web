package com.easygofly.entity;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

@Entity
@Table(name = "countries")
public class Country {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(length = 45, nullable = false)
	private String name;
	
	@Column(length = 5, nullable = false)
	private String code;

	@OneToMany(mappedBy = "country", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("name asc")
	private Set<State> states;
	
	
	public Country() {}
	
	public Country(Integer id) {
		this.id = id;
	}
	
	public Country(String name) {
		this.name = name;
	}
	
	public Country(String name, String code) {
		this.name = name;
		this.code = code;
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

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}


	@Override
	public String toString() {
		return "Country [id=" + id + ", name=" + name + ", code=" + code + ", states=" + states + "]";
	}
	
	
}
