package com.easygofly.entity;

import java.beans.Transient;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "slider_images")
public class SliderImage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(nullable = false)
	private String name;
	
	@ManyToOne
	@JoinColumn(name = "WebDetails_id")
	private WebDetails webDetails;
	
	
	
	public SliderImage() {}
	
	public SliderImage(Integer id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public SliderImage(String name, WebDetails webDetails) {
		this.name = name;
		this.webDetails = webDetails;
	}

	public SliderImage(Integer id, String name, WebDetails webDetails) {
		this.id = id;
		this.name = name;
		this.webDetails = webDetails;
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

	public WebDetails getWebDetails() {
		return webDetails;
	}

	public void setWebDetails(WebDetails webDetails) {
		this.webDetails = webDetails;
	}
	
	@Transient
	public String getSliderImagePath() {
		return "/slider-photos/" + webDetails.getKey() + "/extras/" + this.name;
	}
}
