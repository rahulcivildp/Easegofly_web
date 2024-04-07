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
@Table(name = "hotel_rooms")
public class HotelRoom {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(name = "room_type_code")
	private String roomTypeCode;
	
	@Column(name = "availability_type")
	private String availabilityType;
	
	@Column(name = "room_index")
	private Integer roomIndex;
	
	@Column(name = "room_status")
	private Integer roomStatus;
	
	@Column(name = "room_id")
	private Integer roomId;
	
	@Column(name = "child_count")
	private Integer childCount;

	@Column(name = "require_all_pax_details")
	private boolean requireAllPaxDetails;
	
	@Column(name = "room_description")
	private String roomDescription;
	
	@Column(name = "room_type_name")
	private String roomTypeName;
	
	@Column(name = "rate_plan_code")
	private String ratePlanCode;
	
	@Column(name = "rate_plan")
	private Integer ratePlan;
	
	@Column(name = "rate_plan_name")
	private String ratePlanName;
	
	@Column(name = "info_source")
	private String infoSource;
	
	@Column(name = "sequence_no")
	private String sequenceNo;

	@Column(name = "room_promotion")
	private String roomPromotion;
	
	@Column(name = "amenities")
	private String[] amenities;
	
	@Column(name = "amenity")
	private String[] amenity;
	
	@Column(name = "smoking_preference")
	private String smokingPreference;

	@Column(name = "bed_types")
	private String[] bedTypes;

	@Column(name = "hotel_supplements")
	private String[] hotelSupplements;
	
	@Column(name = "last_cancellation_date")
	private String lastCancellationDate;
	
	@OneToMany(mappedBy = "hotelRoom", cascade = CascadeType.ALL)
	private List<HotelCancelPolicy> hotelCancelPolicies = new ArrayList<>();
	
	@OneToMany(mappedBy = "hotelRoom", cascade = CascadeType.ALL)
	private List<RoomDayRate> roomDayRates = new ArrayList<>();
	
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
	
	@Column(name = "last_voucher_date")
	private String lastVoucherDate;
	
	@Column(name = "cancellation_policy")
	private String cancellationPolicy;

	@Column(name = "inclusion")
	private String[] inclusion;

	@Column(name = "is_passport_mandatory")
	private boolean isPassportMandatory;

	@Column(name = "is_PAN_mandatory")
	private boolean isPANMandatory;

	@ManyToOne
	@JoinColumn(name = "hotel_id")
	private Hotel hotel;
	
	
	public HotelRoom() {}

	public HotelRoom(String roomTypeCode, Integer roomIndex, Integer roomStatus, Integer roomId,
			boolean requireAllPaxDetails, String roomDescription, String roomTypeName, String ratePlanCode,
			Integer ratePlan, String ratePlanName, String infoSource, String sequenceNo, Integer childCount,
			String roomPromotion, String[] amenities, String[] amenity, String smokingPreference, String[] bedTypes,
			String[] hotelSupplements, String lastCancellationDate, List<HotelCancelPolicy> hotelCancelPolicies,
			double roomPrice, double tax, double extraGuestCharge, double childCharge, double discount, String availabilityType,
			double publishedPrice, double otherCharges, double offeredPrice, Integer publishedPriceRoundedOff,
			Integer offeredPriceRoundedOff, double agentCommission, double agentMarkUp, double serviceTax, double tds,
			String lastVoucherDate, String cancellationPolicy, String[] inclusion, boolean isPassportMandatory,
			boolean isPANMandatory, List<RoomDayRate> roomDayRates) {
		this.roomTypeCode = roomTypeCode;
		this.roomIndex = roomIndex;
		this.roomStatus = roomStatus;
		this.roomId = roomId;
		this.requireAllPaxDetails = requireAllPaxDetails;
		this.roomDescription = roomDescription;
		this.roomTypeName = roomTypeName;
		this.ratePlanCode = ratePlanCode;
		this.ratePlan = ratePlan;
		this.ratePlanName = ratePlanName;
		this.infoSource = infoSource;
		this.sequenceNo = sequenceNo;
		this.roomPromotion = roomPromotion;
		this.amenities = amenities;
		this.amenity = amenity;
		this.smokingPreference = smokingPreference;
		this.bedTypes = bedTypes;
		this.hotelSupplements = hotelSupplements;
		this.lastCancellationDate = lastCancellationDate;
		this.hotelCancelPolicies = hotelCancelPolicies;
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
		this.lastVoucherDate = lastVoucherDate;
		this.cancellationPolicy = cancellationPolicy;
		this.inclusion = inclusion;
		this.isPassportMandatory = isPassportMandatory;
		this.isPANMandatory = isPANMandatory;
		this.roomDayRates = roomDayRates;
		this.childCount = childCount;
		this.availabilityType = availabilityType;
	}

	public Integer getId() {
		return id;
	}

	public List<RoomDayRate> getRoomDayRates() {
		return roomDayRates;
	}

	public void setRoomDayRates(List<RoomDayRate> roomDayRates) {
		this.roomDayRates = roomDayRates;
	}
	
	public Integer getChildCount() {
		return childCount;
	}

	public void setChildCount(Integer childCount) {
		this.childCount = childCount;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}

	public String getRoomTypeCode() {
		return roomTypeCode;
	}

	public void setRoomTypeCode(String roomTypeCode) {
		this.roomTypeCode = roomTypeCode;
	}

	public Integer getRoomIndex() {
		return roomIndex;
	}

	public void setRoomIndex(Integer roomIndex) {
		this.roomIndex = roomIndex;
	}

	public String getAvailabilityType() {
		return availabilityType;
	}

	public void setAvailabilityType(String availabilityType) {
		this.availabilityType = availabilityType;
	}

	public Integer getRoomStatus() {
		return roomStatus;
	}

	public Integer getRoomId() {
		return roomId;
	}

	public void setRoomId(Integer roomId) {
		this.roomId = roomId;
	}

	public void setRoomStatus(Integer roomStatus) {
		this.roomStatus = roomStatus;
	}

	public boolean isRequireAllPaxDetails() {
		return requireAllPaxDetails;
	}

	public void setRequireAllPaxDetails(boolean requireAllPaxDetails) {
		this.requireAllPaxDetails = requireAllPaxDetails;
	}

	public String getRoomDescription() {
		return roomDescription;
	}

	public void setRoomDescription(String roomDescription) {
		this.roomDescription = roomDescription;
	}

	public String getRoomTypeName() {
		return roomTypeName;
	}

	public void setRoomTypeName(String roomTypeName) {
		this.roomTypeName = roomTypeName;
	}

	public String getRatePlanCode() {
		return ratePlanCode;
	}

	public void setRatePlanCode(String ratePlanCode) {
		this.ratePlanCode = ratePlanCode;
	}

	public Integer getRatePlan() {
		return ratePlan;
	}

	public void setRatePlan(Integer ratePlan) {
		this.ratePlan = ratePlan;
	}

	public String getRatePlanName() {
		return ratePlanName;
	}

	public void setRatePlanName(String ratePlanName) {
		this.ratePlanName = ratePlanName;
	}

	public String getInfoSource() {
		return infoSource;
	}

	public void setInfoSource(String infoSource) {
		this.infoSource = infoSource;
	}

	public String getSequenceNo() {
		return sequenceNo;
	}

	public void setSequenceNo(String sequenceNo) {
		this.sequenceNo = sequenceNo;
	}

	public String getRoomPromotion() {
		return roomPromotion;
	}

	public void setRoomPromotion(String roomPromotion) {
		this.roomPromotion = roomPromotion;
	}

	public String[] getAmenities() {
		return amenities;
	}

	public void setAmenities(String[] amenities) {
		this.amenities = amenities;
	}

	public String[] getAmenity() {
		return amenity;
	}

	public void setAmenity(String[] amenity) {
		this.amenity = amenity;
	}

	public String getSmokingPreference() {
		return smokingPreference;
	}

	public Hotel getHotel() {
		return hotel;
	}

	public void setHotel(Hotel hotel) {
		this.hotel = hotel;
	}

	public void setSmokingPreference(String smokingPreference) {
		this.smokingPreference = smokingPreference;
	}

	public String[] getBedTypes() {
		return bedTypes;
	}

	public void setBedTypes(String[] bedTypes) {
		this.bedTypes = bedTypes;
	}

	public String[] getHotelSupplements() {
		return hotelSupplements;
	}

	public void setHotelSupplements(String[] hotelSupplements) {
		this.hotelSupplements = hotelSupplements;
	}

	public String getLastCancellationDate() {
		return lastCancellationDate;
	}

	public void setLastCancellationDate(String lastCancellationDate) {
		this.lastCancellationDate = lastCancellationDate;
	}

	public List<HotelCancelPolicy> getHotelCancelPolicies() {
		return hotelCancelPolicies;
	}

	public void setHotelCancelPolicies(List<HotelCancelPolicy> hotelCancelPolicies) {
		this.hotelCancelPolicies = hotelCancelPolicies;
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

	public String getLastVoucherDate() {
		return lastVoucherDate;
	}

	public void setLastVoucherDate(String lastVoucherDate) {
		this.lastVoucherDate = lastVoucherDate;
	}

	public String getCancellationPolicy() {
		return cancellationPolicy;
	}

	public void setCancellationPolicy(String cancellationPolicy) {
		this.cancellationPolicy = cancellationPolicy;
	}

	public String[] getInclusion() {
		return inclusion;
	}

	public void setInclusion(String[] inclusion) {
		this.inclusion = inclusion;
	}

	public boolean isPassportMandatory() {
		return isPassportMandatory;
	}

	public void setPassportMandatory(boolean isPassportMandatory) {
		this.isPassportMandatory = isPassportMandatory;
	}

	public boolean isPANMandatory() {
		return isPANMandatory;
	}

	public void setPANMandatory(boolean isPANMandatory) {
		this.isPANMandatory = isPANMandatory;
	}
	
	
}
