package com.easygofly.entity;

import java.beans.Transient;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "brands")
public class Brand {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false)
	private Integer id;
	
	@Column(nullable = false, length = 45, unique = true)
	private String name;
	
	@Column(nullable = false, length = 128)
	private String logo;
	
	private boolean enabled;
	
	@ManyToMany
	@JoinTable(
			name = "brand_categories",
			joinColumns = @JoinColumn(name = "brand_id"),
			inverseJoinColumns = @JoinColumn(name = "category_id")
			)
	private Set<Category> categories = new HashSet<>();
	

	
	public Brand() {}
	
	public Brand(Integer id, String name, String logo) {
		this.id = id;
		this.name = name;
		this.logo = logo;
	}

	public Brand(String name, String logo, Set<Category> categories) {
		this.name = name;
		this.logo = logo;
		this.categories = categories;
	}

	
	public Brand(String name) {
		this.name = name;
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

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Set<Category> getCategories() {
		return categories;
	}

	public void setCategories(Set<Category> categories) {
		this.categories = categories;
	}
	
	public void addCategory(Category category) {
		this.categories.add(category);
	}

	@Override
	public String toString() {
		return "Brand [id=" + id + ", name=" + name + ", logo=" + logo + ", enabled=" + enabled + ", categories="
				+ categories + "]";
	}
	
	@Transient
	public String getPhotosImagePath() {
		if(id == null || logo == null) {
			return "/images/no-image.png";
		}
		return "/brand-logos/" + this.id + "/" + this.logo;
	}
	
}
