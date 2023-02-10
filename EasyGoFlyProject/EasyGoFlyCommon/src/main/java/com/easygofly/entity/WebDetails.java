package com.easygofly.entity;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "WebDetails")
public class WebDetails {
	@Id
	@Column(name = "`key`", nullable = false, length = 128)
	private String key;
	
	@Column(nullable = false, length = 10240)
	private String value;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 45)
	private WebSettingCategory category;

	@OneToMany(mappedBy = "webDetails", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<SliderImage> sliderImage = new HashSet<>();
	
	
	
	public WebDetails() {}
	
	public WebDetails(String key) {
		this.key = key;
	}
	
	public WebDetails(String key, String value, WebSettingCategory category) {
		this.key = key;
		this.value = value;
		this.category = category;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public WebSettingCategory getCategory() {
		return category;
	}

	public void setCategory(WebSettingCategory category) {
		this.category = category;
	}

	public Set<SliderImage> getSliderImage() {
		return sliderImage;
	}

	public void setSliderImage(Set<SliderImage> sliderImage) {
		this.sliderImage = sliderImage;
	}

	public void addSliderImages(String imageName) {
		this.sliderImage.add(new SliderImage(imageName, this));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(key);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WebDetails other = (WebDetails) obj;
		return Objects.equals(key, other.key);
	}

	@Override
	public String toString() {
		return "WebDetails [key=" + key + ", value=" + value + "]";
	}
	
	
}
