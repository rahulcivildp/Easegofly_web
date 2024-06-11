package com.easygofly.entity;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "orders")
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(nullable = false, length = 256)
	private String name;
	
	@Column(name = "created_time")
	private Date createdTime;
	
	@Column(name = "first_name", length = 45, nullable = false)
	private String firstName;
	
	@Column(name = "last_name", length = 45, nullable = false)
	private String lastName;
	
	@Column(name = "phone_number", length = 15, nullable = false)
	private BigInteger phoneNumber;
	
	@Column(name = "contact_email", nullable = false)
	private String contactEmail;
	
	@Column(name = "address_line1", length = 64, nullable = false)
	private String addressLine1;
	
	@Column(name = "address_line2", length = 64)
	private String addressLine2;
	
	@Column(name = "city", length = 45, nullable = false)
	private String city;
	
	@Column(name = "state", length = 45, nullable = false)
	private String state;
	
	@Column(name = "postal_code", length = 10, nullable = false)
	private String postalCode;
	
	@Column(name = "country", length = 45, nullable = false)
	private String country;
	
	@Column(length = 256)
	private String transactionId;
	
	@Column(length = 256)
	private String transactionToken;
	
	@Column(name = "coupon_code")
	private String couponCode;
	
	private String status;
	
	@Column(name = "booking_id")
	private String bookingId;
	 
	private double price;
	
	private int cartId;
	
	@Column(nullable = false, name = "city_one")
	private String cityOne;
	
	@Column(nullable = false, name = "city_two")
	private String cityTwo;
	
	@Column(nullable = false, length = 3, name = "passenger_num")
	private Integer passengerNum;
	
	@Column(nullable = false, name = "journey_class")
	private String journeyClass;
	
	@Column(nullable = false, length = 3)
	private Integer adultNum;
	
	@Column(length = 3)
	private Integer childNum;
	
	@Column(length = 5)
	private Integer infantNum;
	
	@Column(nullable = false, name = "trip_type")
	private String tripType;
	
	@Enumerated(EnumType.STRING)
	private PaymentMethod paymentMethod;
	
	@Enumerated(EnumType.STRING)
	private OrderStatus orderStatus;

	@ManyToOne
	@JoinColumn(name = "customer_id")
	private Customer customer;
	
	@OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_detail_id")
    private ProductDetail productDetail;
	
	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
	private List<TravellerDetail> travellerDetails = new ArrayList<>();
	
	@Column(name = "device", length = 100)
	private String device;

	@Column(name = "device_description", length = 5000)
	private String deviceDescription;

	@Column(name = "device_type", length = 5000)
	private String deviceType;

	@ManyToOne
	@JoinColumn(name = "total_transaction_id")
	private TotalTransaction totalTransaction;
	
	
	public Order() {}

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

	public int getCartId() {
		return cartId;
	}

	public void setCartId(int cartId) {
		this.cartId = cartId;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
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

	public Integer getPassengerNum() {
		return passengerNum;
	}

	public void setPassengerNum(Integer passengerNum) {
		this.passengerNum = passengerNum;
	}

	public String getJourneyClass() {
		return journeyClass;
	}

	public void setJourneyClass(String journeyClass) {
		this.journeyClass = journeyClass;
	}

	public TotalTransaction getTotalTransaction() {
		return totalTransaction;
	}

	public void setTotalTransaction(TotalTransaction totalTransaction) {
		this.totalTransaction = totalTransaction;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public String getDeviceDescription() {
		return deviceDescription;
	}

	public void setDeviceDescription(String deviceDescription) {
		this.deviceDescription = deviceDescription;
	}

	public Integer getAdultNum() {
		return adultNum;
	}

	public void setAdultNum(Integer adultNum) {
		this.adultNum = adultNum;
	}

	public String getBookingId() {
		return bookingId;
	}

	public void setBookingId(String bookingId) {
		this.bookingId = bookingId;
	}

	public Integer getChildNum() {
		return childNum;
	}

	public void setChildNum(Integer childNum) {
		this.childNum = childNum;
	}

	public Integer getInfantNum() {
		return infantNum;
	}

	public void setInfantNum(Integer infantNum) {
		this.infantNum = infantNum;
	}

	public List<TravellerDetail> getTravellerDetails() {
		return travellerDetails;
	}

	public void setTravellerDetails(List<TravellerDetail> travellerDetails) {
		this.travellerDetails = travellerDetails;
	}

	public String getTripType() {
		return tripType;
	}

	public void setTripType(String tripType) {
		this.tripType = tripType;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getTransactionToken() {
		return transactionToken;
	}

	public void setTransactionToken(String transactionToken) {
		this.transactionToken = transactionToken;
	}

	public String getCouponCode() {
		return couponCode;
	}

	public void setCouponCode(String couponCode) {
		this.couponCode = couponCode;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public ProductDetail getProductDetail() {
		return productDetail;
	}

	public void setProductDetail(ProductDetail productDetail) {
		this.productDetail = productDetail;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public BigInteger getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(BigInteger phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getAddressLine1() {
		return addressLine1;
	}

	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	public String getAddressLine2() {
		return addressLine2;
	}

	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(PaymentMethod paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public OrderStatus getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(OrderStatus orderStatus) {
		this.orderStatus = orderStatus;
	}

	@Override
	public String toString() {
		return "Order [id=" + id + ", name=" + name + ", createdTime=" + createdTime + ", firstName=" + firstName
				+ ", lastName=" + lastName + ", phoneNumber=" + phoneNumber + ", addressLine1=" + addressLine1
				+ ", addressLine2=" + addressLine2 + ", city=" + city + ", state=" + state + ", postalCode="
				+ postalCode + ", country=" + country + ", transactionId=" + transactionId + ", transactionToken="
				+ transactionToken + ", status=" + status + ", price=" + price + ", cityOne=" + cityOne + ", cityTwo="
				+ cityTwo + ", passengerNum=" + passengerNum + ", journeyClass=" + journeyClass + ", adultNum="
				+ adultNum + ", childNum=" + childNum + ", infantNum=" + infantNum + ", tripType=" + tripType
				+ ", paymentMethod=" + paymentMethod + ", orderStatus=" + orderStatus + ", customer=" + customer
				+ ", productDetail=" + productDetail + "]";
	}

	
	
	
	
}
