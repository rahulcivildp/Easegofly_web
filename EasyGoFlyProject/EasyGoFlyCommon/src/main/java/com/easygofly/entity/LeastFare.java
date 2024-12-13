package com.easygofly.entity;

public class LeastFare {
	private Integer id;
	
	private String url;
	
	private ProductDetail productDetail;

	
	public LeastFare() {}

	public LeastFare(String url, ProductDetail productDetail) {
		this.url = url;
		this.productDetail = productDetail;
	}

	public LeastFare(Integer id, String url, ProductDetail productDetail) {
		this.id = id;
		this.url = url;
		this.productDetail = productDetail;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public ProductDetail getProductDetail() {
		return productDetail;
	}

	public void setProductDetail(ProductDetail productDetail) {
		this.productDetail = productDetail;
	}
	
	
}
