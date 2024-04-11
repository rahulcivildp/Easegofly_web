package com.easygofly.entity;

import java.util.ArrayList;
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
@Table(name = "hotels")
public class Hotel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(name = "hotel_code", length = 40)
	private String hotelCode;
	
	@Column(name = "result_index")
	private Integer resultIndex;
	
	@Column(name = "hotel_name")
	private String hotelName;
	
	@Column(name = "hotel_category")
	private String hotelCategory;
	
	@Column(name = "star_rating")
	private Integer starRating;
	
	@Column(name = "hotel_description", length = 5000)
	private String hotelDescription;
	
	@Column(name = "hotel_promotion", length = 1000)
	private String hotelPromotion;
	
	@Column(name = "hotel_policy", length = 2000)
	private String hotelPolicy;
	
	@Column(name = "hotel_picture")
	private String hotelPicture;
	
	@Column(name = "hotel_address", length = 500)
	private String hotelAddress;
	
	@Column(name = "hotel_contact_no")
	private String hotelContactNo;
	
	@Column(name = "hotel_map")
	private String hotelMap;
	
	@Column(length = 40)
	private String latitude;
	
	@Column(length = 40)
	private String longitude;
	
	@Column(name = "hotel_location")
	private String hotelLocation;
	
	@Column(name = "room_price")
	private double roomPrice;

	@Column(nullable = true)
	private double tax;
	
	@Column(name = "extra_guest_charge")
	private double extraGuestCharge;
	
	@Column(name = "child_charge")
	private double childCharge;
	
	@Column(name = "discount")
	private double discount;
	
	@Column(name = "published_price")
	private double publishedPrice;
	
	@Column(name = "other_charges")
	private double otherCharges;
	
	@Column(name = "offered_price")
	private double offeredPrice;

	@Column(name = "published_price_rounded_off")
	private Integer publishedPriceRoundedOff;

	@Column(name = "offered_price_rounded_off")
	private Integer offeredPriceRoundedOff;
	
	@Column(name = "agent_commission")
	private double agentCommission;
	
	@Column(name = "agent_markUp")
	private double agentMarkUp;
	
	@Column(name = "service_tax")
	private double serviceTax;

	@Column(nullable = true)
	private double tds;
	
	@OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL)
	private List<HotelGuest> guests = new ArrayList<>();

	@OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL)
	private List<HotelRoom> hotelRooms = new ArrayList<>();

	@ManyToOne
	@JoinColumn(name = "customer_id")
	private Customer customer;
	
	
	public Hotel() {}
	
	public Hotel(String hotelCode, Integer resultIndex, String hotelName, String hotelCategory, Integer starRating,
			String hotelDescription, String hotelPromotion, String hotelPolicy, String hotelPicture,
			String hotelAddress, String hotelContactNo, String hotelMap, String latitude, String longitude,
			String hotelLocation, double roomPrice, double tax, double extraGuestCharge, double childCharge,
			double discount, double publishedPrice, double otherCharges, double offeredPrice,
			Integer publishedPriceRoundedOff, Integer offeredPriceRoundedOff, double agentCommission,
			double agentMarkUp, double serviceTax, double tds) {
		super();
		this.hotelCode = hotelCode;
		this.resultIndex = resultIndex;
		this.hotelName = hotelName;
		this.hotelCategory = hotelCategory;
		this.starRating = starRating;
		this.hotelDescription = hotelDescription;
		this.hotelPromotion = hotelPromotion;
		this.hotelPolicy = hotelPolicy;
		this.hotelPicture = hotelPicture;
		this.hotelAddress = hotelAddress;
		this.hotelContactNo = hotelContactNo;
		this.hotelMap = hotelMap;
		this.latitude = latitude;
		this.longitude = longitude;
		this.hotelLocation = hotelLocation;
		this.roomPrice = roomPrice;
		this.tax = tax;
		this.extraGuestCharge = extraGuestCharge;
		this.childCharge = childCharge;
		this.discount = discount;
		this.publishedPrice = publishedPrice;
		this.otherCharges = otherCharges;
		this.offeredPrice = offeredPrice;
		this.publishedPriceRoundedOff = publishedPriceRoundedOff;
		this.offeredPriceRoundedOff = offeredPriceRoundedOff;
		this.agentCommission = agentCommission;
		this.agentMarkUp = agentMarkUp;
		this.serviceTax = serviceTax;
		this.tds = tds;
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

	public Integer getResultIndex() {
		return resultIndex;
	}

	public void setResultIndex(Integer resultIndex) {
		this.resultIndex = resultIndex;
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

	public Integer getStarRating() {
		return starRating;
	}

	public void setStarRating(Integer starRating) {
		this.starRating = starRating;
	}

	public String getHotelDescription() {
		return hotelDescription;
	}

	public void setHotelDescription(String hotelDescription) {
		this.hotelDescription = hotelDescription;
	}

	public String getHotelPromotion() {
		return hotelPromotion;
	}

	public void setHotelPromotion(String hotelPromotion) {
		this.hotelPromotion = hotelPromotion;
	}

	public String getHotelPolicy() {
		return hotelPolicy;
	}

	public void setHotelPolicy(String hotelPolicy) {
		this.hotelPolicy = hotelPolicy;
	}

	public String getHotelPicture() {
		return hotelPicture;
	}

	public void setHotelPicture(String hotelPicture) {
		this.hotelPicture = hotelPicture;
	}

	public String getHotelAddress() {
		return hotelAddress;
	}

	public List<HotelRoom> getHotelRooms() {
		return hotelRooms;
	}

	public void setHotelRooms(List<HotelRoom> hotelRooms) {
		this.hotelRooms = hotelRooms;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
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

	public double getRoomPrice() {
		return roomPrice;
	}

	public void setRoomPrice(double roomPrice) {
		this.roomPrice = roomPrice;
	}

	public double getTax() {
		return tax;
	}

	public void setTax(double tax) {
		this.tax = tax;
	}

	public double getExtraGuestCharge() {
		return extraGuestCharge;
	}

	public void setExtraGuestCharge(double extraGuestCharge) {
		this.extraGuestCharge = extraGuestCharge;
	}

	public double getChildCharge() {
		return childCharge;
	}

	public void setChildCharge(double childCharge) {
		this.childCharge = childCharge;
	}

	public double getDiscount() {
		return discount;
	}

	public void setDiscount(double discount) {
		this.discount = discount;
	}

	public double getPublishedPrice() {
		return publishedPrice;
	}

	public void setPublishedPrice(double publishedPrice) {
		this.publishedPrice = publishedPrice;
	}

	public double getOtherCharges() {
		return otherCharges;
	}

	public void setOtherCharges(double otherCharges) {
		this.otherCharges = otherCharges;
	}

	public double getOfferedPrice() {
		return offeredPrice;
	}

	public void setOfferedPrice(double offeredPrice) {
		this.offeredPrice = offeredPrice;
	}

	public Integer getPublishedPriceRoundedOff() {
		return publishedPriceRoundedOff;
	}

	public void setPublishedPriceRoundedOff(Integer publishedPriceRoundedOff) {
		this.publishedPriceRoundedOff = publishedPriceRoundedOff;
	}

	public Integer getOfferedPriceRoundedOff() {
		return offeredPriceRoundedOff;
	}

	public void setOfferedPriceRoundedOff(Integer offeredPriceRoundedOff) {
		this.offeredPriceRoundedOff = offeredPriceRoundedOff;
	}

	public double getAgentCommission() {
		return agentCommission;
	}

	public void setAgentCommission(double agentCommission) {
		this.agentCommission = agentCommission;
	}

	public double getAgentMarkUp() {
		return agentMarkUp;
	}

	public void setAgentMarkUp(double agentMarkUp) {
		this.agentMarkUp = agentMarkUp;
	}

	public double getServiceTax() {
		return serviceTax;
	}

	public void setServiceTax(double serviceTax) {
		this.serviceTax = serviceTax;
	}

	public double getTds() {
		return tds;
	}

	public void setTds(double tds) {
		this.tds = tds;
	}

	public List<HotelGuest> getGuests() {
		return guests;
	}

	public void setGuests(List<HotelGuest> guests) {
		this.guests = guests;
	}

	public void addGuests(String title, String firstName, String lastName, String phoneNo, String email, Integer paxType,
			Integer age, boolean leadPassenger, String passportNo, String passportExpDate, String passportIssueDate,
			String pan, HotelRoom room) {
		this.guests.add(new HotelGuest(title, firstName, lastName, phoneNo, email, paxType, age, leadPassenger, passportNo, passportExpDate, passportIssueDate, pan, room, this));
	}

	public void addRooms(String roomTypeCode, Integer roomIndex, Integer roomStatus, Integer roomId,
			boolean requireAllPaxDetails, String roomDescription, String roomTypeName, String ratePlanCode,
			Integer ratePlan, String ratePlanName, String infoSource, String sequenceNo, Integer childCount,
			String roomPromotion, String[] amenities, String[] amenity, String smokingPreference, String[] bedTypes,
			String[] hotelSupplements, String lastCancellationDate, List<HotelCancelPolicy> hotelCancelPolicies,
			double roomPrice, double tax, double extraGuestCharge, double childCharge, double discount, String availabilityType,
			double publishedPrice, double otherCharges, double offeredPrice, Integer publishedPriceRoundedOff,
			Integer offeredPriceRoundedOff, double agentCommission, double agentMarkUp, double serviceTax, double tds,
			String lastVoucherDate, String cancellationPolicy, String[] inclusion, boolean isPassportMandatory,
			boolean isPANMandatory, List<RoomDayRate> roomDayRates) {
		this.hotelRooms.add(new HotelRoom(roomTypeCode, roomIndex, roomStatus, roomId,
				 requireAllPaxDetails, roomDescription, roomTypeName, ratePlanCode, ratePlan, ratePlanName, infoSource, sequenceNo, childCount,
				 roomPromotion, amenities, amenity, smokingPreference, bedTypes, hotelSupplements, lastCancellationDate, hotelCancelPolicies,
				 roomPrice, tax, extraGuestCharge, childCharge, discount, availabilityType, publishedPrice, otherCharges, offeredPrice, publishedPriceRoundedOff,
				 offeredPriceRoundedOff, agentCommission, agentMarkUp, serviceTax, tds,
				 lastVoucherDate, cancellationPolicy, inclusion, isPassportMandatory, isPANMandatory, roomDayRates, this));
	}
	
	
}
