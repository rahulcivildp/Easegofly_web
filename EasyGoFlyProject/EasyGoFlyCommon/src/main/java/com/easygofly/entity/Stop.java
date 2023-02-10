package com.easygofly.entity;
 
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "stops")
public class Stop {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(nullable = false, name = "city_name")
	private String cityName;
	
	@Column(nullable = false)
	private String depTime;
	
	@Column(nullable = false)
	private String arrTime;
	
	@Column(nullable = false)
	private String totalTime;
	
	@ManyToOne
	@JoinColumn(name = "product_detail_id")
	private ProductDetail productDetail;

	
	
	public Stop() {}

	public Stop(String cityName, String depTime, String arrTime, String totalTime,
			ProductDetail productDetail) {
		this.cityName = cityName;
		this.depTime = depTime;
		this.arrTime = arrTime;
		this.totalTime = totalTime;
		this.productDetail = productDetail;
	}

	public Stop(int id, String cityOne, String depTime, String arrTime, String totalTime,
			ProductDetail productDetail) {
		this.id = id;
		this.cityName = cityOne;
		this.depTime = depTime;
		this.arrTime = arrTime;
		this.totalTime = totalTime;
		this.productDetail = productDetail;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public String getDepTime() {
		return depTime;
	}

	public void setDepTime(String depTime) {
		this.depTime = depTime;
	}

	public String getArrTime() {
		return arrTime;
	}

	public void setArrTime(String arrTime) {
		this.arrTime = arrTime;
	}

	public String getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(String totalTime) {
		this.totalTime = totalTime;
	}

	public ProductDetail getProductDetail() {
		return productDetail;
	}

	public void setProductDetail(ProductDetail productDetail) {
		this.productDetail = productDetail;
	}
	
}
