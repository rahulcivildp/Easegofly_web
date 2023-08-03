package com.easygofly.entity;


import java.beans.Transient;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity 
@Table(name = "products")
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(nullable = false, length = 256, unique = true)
	private String name;
	
	@Column(nullable = false, length = 256, unique = true)
	private String alias;
	
	@Column(nullable = false, length = 5, name = "city_one")
	private String cityOne;
	
	@Column(nullable = false, length = 5, name = "city_two")
	private String cityTwo;
	
	@Column(nullable = false, length = 256, name = "journey_class")
	private String journeyClass;
	
	@Column(nullable = false, length = 25, name = "airline_code")
	private String airlineCode;
	
	@Column(nullable = false, length = 10, name = "duration")
	private int duration;
	
	@Column(nullable = false, length = 5, name = "origin_terminal")
	private String originTerminal;
	
	@Column(nullable = false, length = 5, name = "destination_terminal")
	private String destinationTerminal;
	
	@Column(length = 256, name = "remarks")
	private String remarks;
	
	@Column(name = "created_time")
	private Date createdTime;
	
	@Column(name = "updated_time")
	private Date updatedTime;
	
	private boolean enabled;
	
	@Column(name = "in_stock")
	private boolean inStock;

	@Column(name = "fund_state")
	private boolean fundState;
	
	@Column(name = "passport_ADT")
	private boolean passportADT;
	
	@Column(name = "passport_CHD")
	private boolean passportCHD;
	
	@Column(name = "passport_INF")
	private boolean passportINF;
	
	@Column(name = "dob_ADT")
	private boolean dob_ADT;
	
	@Column(name = "dob_CHD")
	private boolean dobCHD;
	
	@Column(name = "dob_INF")
	private boolean dobINF;
	
	private float cost;
	private float price;
	
	@Column(name = "discount_percentage")
	private float discountPercentage;

	private int stops;
	private int craft;
	
	@Column(nullable = false, length = 5)
	private int baggage;
	
	@Column(nullable = false, length = 5, name = "cabin_baggage")
	private int cabinBaggage;
	
	@ManyToOne()
	@JoinColumn(name = "category_id")
	private Category categories;
	
	@ManyToOne()
	@JoinColumn(name = "brand_id")
	private Brand brands;
	
	@ManyToOne()
	@JoinColumn(name = "user_id")
	private User user;
	
	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
	private List<ProductDetail> details = new ArrayList<>();
	
	
	public Product() {}
	
	public Product(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public int getCraft() {
		return craft;
	}

	public void setCraft(int craft) {
		this.craft = craft;
	}

	public String getJourneyClass() {
		return journeyClass;
	}

	public void setJourneyClass(String journeyClass) {
		this.journeyClass = journeyClass;
	}

	public String getAirlineCode() {
		return airlineCode;
	}

	public void setAirlineCode(String airlineCode) {
		this.airlineCode = airlineCode;
	}

	public boolean isFundState() {
		return fundState;
	}

	public void setFundState(boolean fundState) {
		this.fundState = fundState;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public String getOriginTerminal() {
		return originTerminal;
	}

	public void setOriginTerminal(String originTerminal) {
		this.originTerminal = originTerminal;
	}

	public String getDestinationTerminal() {
		return destinationTerminal;
	}

	public void setDestinationTerminal(String destinationTerminal) {
		this.destinationTerminal = destinationTerminal;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	public Date getUpdatedTime() {
		return updatedTime;
	}

	public void setUpdatedTime(Date updatedTime) {
		this.updatedTime = updatedTime;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isInStock() {
		return inStock;
	}

	public void setInStock(boolean inStock) {
		this.inStock = inStock;
	}

	public boolean isPassportADT() {
		return passportADT;
	}

	public void setPassportADT(boolean passportADT) {
		this.passportADT = passportADT;
	}

	public boolean isPassportCHD() {
		return passportCHD;
	}

	public void setPassportCHD(boolean passportCHD) {
		this.passportCHD = passportCHD;
	}

	public boolean isPassportINF() {
		return passportINF;
	}

	public void setPassportINF(boolean passportINF) {
		this.passportINF = passportINF;
	}

	public boolean isDob_ADT() {
		return dob_ADT;
	}

	public void setDob_ADT(boolean dob_ADT) {
		this.dob_ADT = dob_ADT;
	}

	public boolean isDobCHD() {
		return dobCHD;
	}

	public void setDobCHD(boolean dobCHD) {
		this.dobCHD = dobCHD;
	}

	public boolean isDobINF() {
		return dobINF;
	}

	public void setDobINF(boolean dobINF) {
		this.dobINF = dobINF;
	}

	public float getCost() {
		return cost;
	}

	public void setCost(float cost) {
		this.cost = cost;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public float getDiscountPercentage() {
		return discountPercentage;
	}

	public void setDiscountPercentage(float discountPercentage) {
		this.discountPercentage = discountPercentage;
	}

	public int getStops() {
		return stops;
	}

	public void setStops(int stops) {
		this.stops = stops;
	}

	public int getBaggage() {
		return baggage;
	}

	public void setBaggage(int baggage) {
		this.baggage = baggage;
	}

	public int getCabinBaggage() {
		return cabinBaggage;
	}

	public void setCabinBaggage(int cabinBaggage) {
		this.cabinBaggage = cabinBaggage;
	}

	public Category getCategories() {
		return categories;
	}

	public void setCategories(Category categories) {
		this.categories = categories;
	}

	public Brand getBrands() {
		return brands;
	}

	public void setBrands(Brand brands) {
		this.brands = brands;
	}

	public String getCityOne() {
		return cityOne;
	}

	public void setCityOne(String cityOne) {
		this.cityOne = cityOne;
	}

	public String getCityTwo() {
		return cityTwo;
	}

	public void setCityTwo(String cityTwo) {
		this.cityTwo = cityTwo;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public List<ProductDetail> getDetails() {
		return details;
	}

	public void setDetails(List<ProductDetail> details) {
		this.details = details;
	}

	
	public void addDetail(String pnr, String totalSeats, String uploadSeats, String flightNum, Date date, String depTime,
			String arrTime, float priceADT, float priceINF, float markupADT, float markupINF, String cityOne, String cityTwo, 
			boolean inStock, boolean enabled,  int stopNum, Integer duration, String brand, float depTimeInteger, float arrTimeInteger, 
			String mode, String journeyClass, String terminalDep, String terminalArr, Integer baggage, Integer cabinBaggage, String traceId) {
		this.details.add(new ProductDetail(pnr, totalSeats, uploadSeats, flightNum, date, depTime, arrTime, priceADT, priceINF, markupADT, 
				markupINF, cityOne, cityTwo, inStock, enabled, stopNum, duration, brand, depTimeInteger, arrTimeInteger, mode, journeyClass, 
				terminalDep, terminalArr, baggage, cabinBaggage, traceId, this));
	}
	
	public void addDetail(int id, String pnr, String totalSeats, String uploadSeats, String flightNum, Date date,
			String depTime, String arrTime, float priceADT, float priceINF, float markupADT, float markupINF, String cityOne, 
			String cityTwo, boolean inStock, boolean enabled,  int stopNum, Integer duration, String brand, float depTimeInteger, float arrTimeInteger) {
		this.details.add(new ProductDetail(id, pnr, totalSeats, uploadSeats, flightNum, date, depTime, arrTime, priceADT, priceINF, markupADT, markupINF, 
				cityOne, cityTwo, inStock, enabled, stopNum, duration, brand, depTimeInteger, arrTimeInteger, this));
	}
	
	@Override
	public String toString() {
		return "Product [id=" + id + ", name=" + name + "]";
	}
	
	
	@Transient
	public String getShortName() {
		if(name.length() > 70) {
			return name.substring(0, 70).concat("...");
		}
		return name;
	}
	
	@Transient
	public float getDiscountPrice() {
		if(discountPercentage > 0) {
			return price * ((100 - discountPercentage) / 100);
		}
		return this.price;
	}
	
}
