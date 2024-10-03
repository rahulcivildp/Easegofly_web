package com.easygofly.entity;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "traveller_details")
public class TravellerDetail {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(name = "salutation")
	private String salutation;
	
	@Column(name = "first_name", length = 45)
	private String firstName;
	
	@Column(name = "last_name", length = 45)
	private String lastName;
	
	@Column(name = "passenger_type", length = 45)
	private String paxType;
	
	@Column(name = "cabin_baggage")
	private Integer cabinBaggage;
	
	@Column(name = "baggage_weight")
	private Integer baggageWT;
	
	@Column(name = "traveler_count_serial")
	private Integer travelerCountSerial;
	
	private String meal;
	
	private String baggage;
	
	private String seat;
	
	private String basefare;
	
	private String tax;
	
	@Column(name = "passport_no")
	private String passportNo;
	
	@Column(name = "passport_expiry")
	@Temporal(TemporalType.DATE)
	private Date passportExpiry;
	
	@Column()
	@Temporal(TemporalType.DATE)
	private Date dob;
	
	@ManyToOne
	@JoinColumn(name = "product_detail_id")
	private ProductDetail productDetail;

	@ManyToOne
	@JoinColumn(name = "cart_item_id")
	private CartItem cartItem;
	
	@ManyToOne
	@JoinColumn(name = "order_id")
	private Order order;
	
	@OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "meal_id", referencedColumnName = "id")
    private MealsOnline mealOnline;
	
	@OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "baggage_id", referencedColumnName = "id")
    private BaggageOnline baggageOnline ;
	
	@OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "seat_id", referencedColumnName = "id")
    private SeatsOnline seatsOnline ;

	
	public TravellerDetail() {}

	public TravellerDetail(String salutation, String firstName, String lastName, Date dob, ProductDetail productDetail, CartItem cartItem) {
		this.salutation = salutation;
		this.firstName = firstName;
		this.lastName = lastName;
		this.dob = dob;
		this.productDetail = productDetail;
		this.cartItem = cartItem;
	}
	
	public TravellerDetail(String salutation, String firstName, String lastName, Date dob, ProductDetail productDetail, CartItem cartItem, String paxType, Integer baggageWT, Integer cabinBaggage) {
		this.salutation = salutation;
		this.firstName = firstName;
		this.lastName = lastName;
		this.dob = dob;
		this.productDetail = productDetail;
		this.cartItem = cartItem;
		this.paxType = paxType;
		this.baggageWT = baggageWT;
		this.cabinBaggage = cabinBaggage;
	}
	
	public TravellerDetail(String salutation, String firstName, String lastName, Date dob, ProductDetail productDetail, CartItem cartItem, String paxType, Integer baggageWT, 
			Integer cabinBaggage, Integer travelerCountSerial) {
		this.salutation = salutation;
		this.firstName = firstName;
		this.lastName = lastName;
		this.dob = dob;
		this.productDetail = productDetail;
		this.cartItem = cartItem;
		this.paxType = paxType;
		this.baggageWT = baggageWT;
		this.cabinBaggage = cabinBaggage;
		this.travelerCountSerial = travelerCountSerial;
	}
	
	public TravellerDetail(String salutation, String firstName, String lastName, String paxType, Integer cabinBaggage,
			Integer baggageWT, Integer travelerCountSerial, String passportNo, Date passportExpiry, Date dob,
			ProductDetail productDetail, CartItem cartItem) {
		this.salutation = salutation;
		this.firstName = firstName;
		this.lastName = lastName;
		this.paxType = paxType;
		this.cabinBaggage = cabinBaggage;
		this.baggageWT = baggageWT;
		this.travelerCountSerial = travelerCountSerial;
		this.passportNo = passportNo;
		this.passportExpiry = passportExpiry;
		this.dob = dob;
		this.productDetail = productDetail;
		this.cartItem = cartItem;
	}

	
	public TravellerDetail(String salutation, String firstName, String lastName, String paxType, Integer cabinBaggage,
			Integer baggageWT, String passportNo, Date passportExpiry, Date dob, ProductDetail productDetail,
			MealsOnline mealOnline, BaggageOnline baggageOnline, SeatsOnline seatsOnline) {
		this.salutation = salutation;
		this.firstName = firstName;
		this.lastName = lastName;
		this.paxType = paxType;
		this.cabinBaggage = cabinBaggage;
		this.baggageWT = baggageWT;
		this.passportNo = passportNo;
		this.passportExpiry = passportExpiry;
		this.dob = dob;
		this.productDetail = productDetail;
		this.mealOnline = mealOnline;
		this.baggageOnline = baggageOnline;
		this.seatsOnline = seatsOnline;
	}
	
	public TravellerDetail(String salutation, String firstName, String lastName, String paxType, Integer cabinBaggage,
			Integer baggageWT, String passportNo, Date passportExpiry, Date dob, Integer travelerCountSerial, ProductDetail productDetail) {
		this.salutation = salutation;
		this.firstName = firstName;
		this.lastName = lastName;
		this.paxType = paxType;
		this.cabinBaggage = cabinBaggage;
		this.baggageWT = baggageWT;
		this.passportNo = passportNo;
		this.passportExpiry = passportExpiry;
		this.dob = dob;
		this.travelerCountSerial = travelerCountSerial;
		this.productDetail = productDetail;
	}

	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}

	public String getSalutation() {
		return salutation;
	}

	public void setSalutation(String salutation) {
		this.salutation = salutation;
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

	public Date getDob() {
		return dob;
	}

	public void setDob(Date dob) {
		this.dob = dob;
	}

	public Integer getBaggageWT() {
		return baggageWT;
	}

	public void setBaggageWT(Integer baggageWT) {
		this.baggageWT = baggageWT;
	}

	public ProductDetail getProductDetail() {
		return productDetail;
	}

	public void setProductDetail(ProductDetail productDetail) {
		this.productDetail = productDetail;
	}

	public Integer getCabinBaggage() {
		return cabinBaggage;
	}

	public void setCabinBaggage(Integer cabinBaggage) {
		this.cabinBaggage = cabinBaggage;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public CartItem getCartItem() {
		return cartItem;
	}

	public void setCartItem(CartItem cartItem) {
		this.cartItem = cartItem;
	}
	
	public String getMeal() {
		return meal;
	}

	public void setMeal(String meal) {
		this.meal = meal;
	}

	public String getBaggage() {
		return baggage;
	}

	public void setBaggage(String baggage) {
		this.baggage = baggage;
	}

	public String getSeat() {
		return seat;
	}

	public void setSeat(String seat) {
		this.seat = seat;
	}

	public String getPaxType() {
		return paxType;
	}

	public void setPaxType(String paxType) {
		this.paxType = paxType;
	}

	public MealsOnline getMealOnline() {
		return mealOnline;
	}

	public void setMealOnline(MealsOnline mealOnline) {
		this.mealOnline = mealOnline;
	}

	public String getBasefare() {
		return basefare;
	}

	public void setBasefare(String basefare) {
		this.basefare = basefare;
	}

	public String getTax() {
		return tax;
	}

	public void setTax(String tax) {
		this.tax = tax;
	}

	public String getPassportNo() {
		return passportNo;
	}

	public void setPassportNo(String passportNo) {
		this.passportNo = passportNo;
	}

	public Date getPassportExpiry() {
		return passportExpiry;
	}

	public void setPassportExpiry(Date passportExpiry) {
		this.passportExpiry = passportExpiry;
	}

	public BaggageOnline getBaggageOnline() {
		return baggageOnline;
	}

	public void setBaggageOnline(BaggageOnline baggageOnline) {
		this.baggageOnline = baggageOnline;
	}

	public SeatsOnline getSeatsOnline() {
		return seatsOnline;
	}

	public void setSeatsOnline(SeatsOnline seatsOnline) {
		this.seatsOnline = seatsOnline;
	}

	public Integer getTravelerCountSerial() {
		return travelerCountSerial;
	}

	public void setTravelerCountSerial(Integer travelerCountSerial) {
		this.travelerCountSerial = travelerCountSerial;
	}

	public void addMeal(String name, String price, String code, String quantity) {
		this.mealOnline = new MealsOnline(name, price, code, quantity, this);
	}

	public void addBaggage(String price, String code, String weight) {
		this.baggageOnline = new BaggageOnline(price, code, weight, this);
	}

	public void addSeat(String price, Integer compartment, Integer availablityType, Integer deck, String rowNo,
			String code, Integer seatType, String seatNo, String craftType) {
		this.seatsOnline = new SeatsOnline(price, compartment, availablityType, deck, rowNo, code, seatType, seatNo, craftType, this);
	}
	
	@Override
	public String toString() {
		return "TravellerDetail [salutation=" + salutation + ", firstName=" + firstName + ", lastName=" + lastName
				+ ", dob=" + dob + ", productDetail=" + productDetail + ", cartItem=" + cartItem + ", order=" + order
				+ "]";
	}

}
