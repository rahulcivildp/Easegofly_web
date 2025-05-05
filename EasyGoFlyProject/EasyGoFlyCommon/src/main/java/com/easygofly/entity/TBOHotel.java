package com.easygofly.entity;

import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "tbo_hotels")
public class TBOHotel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(name = "hotel_code")
	private String hotelCode;
	
	@Column(unique = true, name = "booking_code")
	private String bookingCode;
	
	@Column(name = "hotel_name")
	private String hotelName;
	
	@Column(name = "hotel_category")
	private String hotelCategory;
	
	@Column(name = "star_rating")
	private String starRating;
	
	@Lob
	@Column(name = "hotel_description")
	private Blob hotelDescription;
	
	@Lob
	@Column(name = "hotel_promotion")
	private Blob hotelPromotion;
	
	@Column(name = "hotel_address", length = 3000)
	private String hotelAddress;
	
	@Column(name = "hotel_contact_no")
	private String hotelContactNo;
	
	@Column(name = "hotel_map")
	private String hotelMap;
	
	private String latitude;
	
	private String longitude;
	
	@Column(name = "hotel_location")
	private String hotelLocation;

	@Column(name = "last_cancellation_deadline")
	private String lastCancellationDeadline;

	@Column(name = "published_price")
	private double publishedPrice;
	
	@Column(name = "offered_price")
	private double offeredPrice;

	@Column(name = "published_price_rounded_off")
	private double publishedPriceRoundedOff;

	@Column(name = "offered_price_rounded_off")
	private double offeredPriceRoundedOff;

	@OneToMany(mappedBy = "tboHotel", cascade = CascadeType.ALL)
	private List<TBORoom> tboRooms = new ArrayList<>();

	@ManyToOne
	@JoinColumn(name = "customer_id")
	private Customer customer;

	
	public TBOHotel() {}

	public TBOHotel(String hotelCode, String bookingCode, String hotelName, String hotelCategory, String starRating,
			Blob hotelDescription, Blob hotelPromotion, String hotelAddress, String hotelContactNo, String hotelMap,
			String latitude, String longitude, String hotelLocation, String lastCancellationDeadline,
			double publishedPrice, double offeredPrice, double publishedPriceRoundedOff, double offeredPriceRoundedOff, Customer customer) {
		this.hotelCode = hotelCode;
		this.bookingCode = bookingCode;
		this.hotelName = hotelName;
		this.hotelCategory = hotelCategory;
		this.starRating = starRating;
		this.hotelDescription = hotelDescription;
		this.hotelPromotion = hotelPromotion;
		this.hotelAddress = hotelAddress;
		this.hotelContactNo = hotelContactNo;
		this.hotelMap = hotelMap;
		this.latitude = latitude;
		this.longitude = longitude;
		this.hotelLocation = hotelLocation;
		this.lastCancellationDeadline = lastCancellationDeadline;
		this.publishedPrice = publishedPrice;
		this.offeredPrice = offeredPrice;
		this.publishedPriceRoundedOff = publishedPriceRoundedOff;
		this.offeredPriceRoundedOff = offeredPriceRoundedOff;
		this.customer = customer;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getHotelCode() {
		return hotelCode;
	}

	public void setHotelCode(String hotelCode) {
		this.hotelCode = hotelCode;
	}

	public String getBookingCode() {
		return bookingCode;
	}

	public void setBookingCode(String bookingCode) {
		this.bookingCode = bookingCode;
	}

	public String getHotelName() {
		return hotelName;
	}

	public void setHotelName(String hotelName) {
		this.hotelName = hotelName;
	}

	public String getHotelCategory() {
		return hotelCategory;
	}

	public void setHotelCategory(String hotelCategory) {
		this.hotelCategory = hotelCategory;
	}

	public String getStarRating() {
		return starRating;
	}

	public void setStarRating(String starRating) {
		this.starRating = starRating;
	}

	public Blob getHotelDescription() {
		return hotelDescription;
	}

	public void setHotelDescription(Blob hotelDescription) {
		this.hotelDescription = hotelDescription;
	}

	public Blob getHotelPromotion() {
		return hotelPromotion;
	}

	public void setHotelPromotion(Blob hotelPromotion) {
		this.hotelPromotion = hotelPromotion;
	}

	public String getHotelAddress() {
		return hotelAddress;
	}

	public void setHotelAddress(String hotelAddress) {
		this.hotelAddress = hotelAddress;
	}

	public String getHotelContactNo() {
		return hotelContactNo;
	}

	public void setHotelContactNo(String hotelContactNo) {
		this.hotelContactNo = hotelContactNo;
	}

	public String getHotelMap() {
		return hotelMap;
	}

	public void setHotelMap(String hotelMap) {
		this.hotelMap = hotelMap;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getHotelLocation() {
		return hotelLocation;
	}

	public void setHotelLocation(String hotelLocation) {
		this.hotelLocation = hotelLocation;
	}

	public String getLastCancellationDeadline() {
		return lastCancellationDeadline;
	}

	public void setLastCancellationDeadline(String lastCancellationDeadline) {
		this.lastCancellationDeadline = lastCancellationDeadline;
	}

	public double getPublishedPrice() {
		return publishedPrice;
	}

	public void setPublishedPrice(double publishedPrice) {
		this.publishedPrice = publishedPrice;
	}

	public double getOfferedPrice() {
		return offeredPrice;
	}

	public void setOfferedPrice(double offeredPrice) {
		this.offeredPrice = offeredPrice;
	}

	public double getPublishedPriceRoundedOff() {
		return publishedPriceRoundedOff;
	}

	public void setPublishedPriceRoundedOff(double publishedPriceRoundedOff) {
		this.publishedPriceRoundedOff = publishedPriceRoundedOff;
	}

	public double getOfferedPriceRoundedOff() {
		return offeredPriceRoundedOff;
	}

	public void setOfferedPriceRoundedOff(double offeredPriceRoundedOff) {
		this.offeredPriceRoundedOff = offeredPriceRoundedOff;
	}

	public List<TBORoom> getTboRooms() {
		return tboRooms;
	}

	public void setTboRooms(List<TBORoom> tboRooms) {
		this.tboRooms = tboRooms;
	}

	public void addTboRooms(String roomName, double roomPrice, double tax, double agentCommission, double serviceTax,
			String fromDate, String chargeType, Integer cancellationCharge) {
		this.tboRooms.add(new TBORoom(roomName, roomPrice, tax, agentCommission, serviceTax, fromDate, chargeType, cancellationCharge, this));
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
}
