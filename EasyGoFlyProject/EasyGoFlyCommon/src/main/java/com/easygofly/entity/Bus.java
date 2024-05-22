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
@Table(name = "buses")
public class Bus {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "result_index")
	private Integer resultIndex;
	
	@Column(name = "available_seats")
	private Integer availableSeats;

	@Column(name = "arrival_time")
	private String arrivalTime;

	@Column(name = "departure_time")
	private String departureTime;

	@Column(name = "route_id")
	private String routeId;

	@Column(name = "bus_type")
	private String busType;

	@Column(name = "service_name")
	private String serviceName;

	@Column(name = "travel_name")
	private String travelName;

	@Column(name = "currency_code")
	private String currencyCode;

	@Column(name = "id_proof_required")
	private boolean idProofRequired;

	@Column(name = "is_drop_point_mandatory")
	private boolean isDropPointMandatory;

	@Column(name = "live_tracking_vailable")
	private boolean liveTrackingAvailable;

	@Column(name = "m_ticket_enabled")
	private boolean mTicketEnabled;

	@Column(name = "partial_cancellation_allowed")
	private boolean partialCancellationAllowed;

	@Column(name = "max_seats_per_ticket")
	private Integer maxSeatsPerTicket;

	@Column(name = "operator_id")
	private Integer operatorId;

	@Column(nullable = true)
	private double tax;
	
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
	
	@Column(name = "base_price")
	private double basePrice;

	@Column(nullable = true)
	private double tds;
	
	@Column(name = "cgst_amount")
	private double cGSTAmount;
	
	@Column(name = "cgst_rate")
	private double cGSTRate;
	
	@Column(name = "cess_amount")
	private double cessAmount;
	
	@Column(name = "cess_rate")
	private double cessRate;
	
	@Column(name = "igst_amount")
	private double iGSTAmount;
	
	@Column(name = "igst_rate")
	private double iGSTRate;
	
	@Column(name = "sgst_amount")
	private double sGSTAmount;
	
	@Column(name = "sgst_rate")
	private double sGSTRate;
	
	@Column(name = "taxable_amount")
	private double taxableAmount;
	
	@ManyToOne
	@JoinColumn(name = "customer_id")
	private Customer customer;

	@OneToMany(mappedBy = "bus", cascade = CascadeType.ALL)
	private List<BusCancelPolicy> busCancelPolicies = new ArrayList<>();

	@OneToMany(mappedBy = "bus", cascade = CascadeType.ALL)
	private List<BusPointDetails> boardingPointsDetails = new ArrayList<>();

	@OneToMany(mappedBy = "bus", cascade = CascadeType.ALL)
	private List<BusPointDetails> droppingPointsDetails = new ArrayList<>();

	@OneToMany(mappedBy = "bus", cascade = CascadeType.ALL)
	private List<BusPassenger> busPassengers = new ArrayList<>();
	
	@OneToMany(mappedBy = "bus", cascade = CascadeType.ALL)
	private List<BusSeat> busSeats = new ArrayList<>();
	
	
	
	public Bus() {}
	
	public Bus(Integer resultIndex, String arrivalTime, String departureTime, String routeId, String busType,
			String serviceName, String travelName, String currencyCode, boolean idProofRequired,
			boolean isDropPointMandatory, boolean liveTrackingAvailable, boolean mTicketEnabled,
			boolean partialCancellationAllowed, Integer maxSeatsPerTicket, Integer operatorId, double tax,
			double discount, double publishedPrice, double otherCharges, double offeredPrice,
			Integer publishedPriceRoundedOff, Integer offeredPriceRoundedOff, double agentCommission,
			double agentMarkUp, double basePrice, double tds, double cGSTAmount, double cGSTRate, double cessAmount,
			double cessRate, double iGSTAmount, double iGSTRate, double sGSTAmount, double sGSTRate,
			double taxableAmount, Integer availableSeats, Customer customer, List<BusCancelPolicy> busCancelPolicies,
			List<BusPointDetails> boardingPointsDetails, List<BusPointDetails> droppingPointsDetails) {
		super();
		this.resultIndex = resultIndex;
		this.arrivalTime = arrivalTime;
		this.departureTime = departureTime;
		this.routeId = routeId;
		this.busType = busType;
		this.serviceName = serviceName;
		this.travelName = travelName;
		this.currencyCode = currencyCode;
		this.idProofRequired = idProofRequired;
		this.isDropPointMandatory = isDropPointMandatory;
		this.liveTrackingAvailable = liveTrackingAvailable;
		this.mTicketEnabled = mTicketEnabled;
		this.partialCancellationAllowed = partialCancellationAllowed;
		this.maxSeatsPerTicket = maxSeatsPerTicket;
		this.operatorId = operatorId;
		this.tax = tax;
		this.discount = discount;
		this.publishedPrice = publishedPrice;
		this.otherCharges = otherCharges;
		this.offeredPrice = offeredPrice;
		this.publishedPriceRoundedOff = publishedPriceRoundedOff;
		this.offeredPriceRoundedOff = offeredPriceRoundedOff;
		this.agentCommission = agentCommission;
		this.agentMarkUp = agentMarkUp;
		this.basePrice = basePrice;
		this.tds = tds;
		this.cGSTAmount = cGSTAmount;
		this.cGSTRate = cGSTRate;
		this.cessAmount = cessAmount;
		this.cessRate = cessRate;
		this.iGSTAmount = iGSTAmount;
		this.iGSTRate = iGSTRate;
		this.sGSTAmount = sGSTAmount;
		this.sGSTRate = sGSTRate;
		this.taxableAmount = taxableAmount;
		this.customer = customer;
		this.busCancelPolicies = busCancelPolicies;
		this.boardingPointsDetails = boardingPointsDetails;
		this.droppingPointsDetails = droppingPointsDetails;
		this.availableSeats = availableSeats;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getAvailableSeats() {
		return availableSeats;
	}

	public void setAvailableSeats(Integer availableSeats) {
		this.availableSeats = availableSeats;
	}

	public Integer getResultIndex() {
		return resultIndex;
	}

	public void setResultIndex(Integer resultIndex) {
		this.resultIndex = resultIndex;
	}

	public List<BusCancelPolicy> getBusCancelPolicies() {
		return busCancelPolicies;
	}

	public void setBusCancelPolicies(List<BusCancelPolicy> busCancelPolicies) {
		this.busCancelPolicies = busCancelPolicies;
	}
 
	public List<BusPointDetails> getBoardingPointsDetails() {
		return boardingPointsDetails;
	}

	public void setBoardingPointsDetails(List<BusPointDetails> boardingPointsDetails) {
		this.boardingPointsDetails = boardingPointsDetails;
	}

	public List<BusPointDetails> getDroppingPointsDetails() {
		return droppingPointsDetails;
	}

	public void setDroppingPointsDetails(List<BusPointDetails> droppingPointsDetails) {
		this.droppingPointsDetails = droppingPointsDetails;
	}

	public String getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(String arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public String getDepartureTime() {
		return departureTime;
	}

	public void setDepartureTime(String departureTime) {
		this.departureTime = departureTime;
	}

	public String getRouteId() {
		return routeId;
	}

	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}

	public String getBusType() {
		return busType;
	}

	public void setBusType(String busType) {
		this.busType = busType;
	}

	public List<BusSeat> getBusSeats() {
		return busSeats;
	}

	public void setBusSeats(List<BusSeat> busSeats) {
		this.busSeats = busSeats;
	}

	public List<BusPassenger> getBusPassengers() {
		return busPassengers;
	}

	public void setBusPassengers(List<BusPassenger> busPassengers) {
		this.busPassengers = busPassengers;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getTravelName() {
		return travelName;
	}

	public void setTravelName(String travelName) {
		this.travelName = travelName;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public boolean isIdProofRequired() {
		return idProofRequired;
	}

	public void setIdProofRequired(boolean idProofRequired) {
		this.idProofRequired = idProofRequired;
	}

	public boolean isDropPointMandatory() {
		return isDropPointMandatory;
	}

	public void setDropPointMandatory(boolean isDropPointMandatory) {
		this.isDropPointMandatory = isDropPointMandatory;
	}

	public boolean isLiveTrackingAvailable() {
		return liveTrackingAvailable;
	}

	public void setLiveTrackingAvailable(boolean liveTrackingAvailable) {
		this.liveTrackingAvailable = liveTrackingAvailable;
	}

	public boolean ismTicketEnabled() {
		return mTicketEnabled;
	}

	public void setmTicketEnabled(boolean mTicketEnabled) {
		this.mTicketEnabled = mTicketEnabled;
	}

	public boolean isPartialCancellationAllowed() {
		return partialCancellationAllowed;
	}

	public void setPartialCancellationAllowed(boolean partialCancellationAllowed) {
		this.partialCancellationAllowed = partialCancellationAllowed;
	}

	public Integer getMaxSeatsPerTicket() {
		return maxSeatsPerTicket;
	}

	public void setMaxSeatsPerTicket(Integer maxSeatsPerTicket) {
		this.maxSeatsPerTicket = maxSeatsPerTicket;
	}

	public Integer getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(Integer operatorId) {
		this.operatorId = operatorId;
	}

	public double getTax() {
		return tax;
	}

	public void setTax(double tax) {
		this.tax = tax;
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

	public double getBasePrice() {
		return basePrice;
	}

	public void setBasePrice(double basePrice) {
		this.basePrice = basePrice;
	}

	public double getTds() {
		return tds;
	}

	public void setTds(double tds) {
		this.tds = tds;
	}

	public double getcGSTAmount() {
		return cGSTAmount;
	}

	public void setcGSTAmount(double cGSTAmount) {
		this.cGSTAmount = cGSTAmount;
	}

	public double getcGSTRate() {
		return cGSTRate;
	}

	public void setcGSTRate(double cGSTRate) {
		this.cGSTRate = cGSTRate;
	}

	public double getCessAmount() {
		return cessAmount;
	}

	public void setCessAmount(double cessAmount) {
		this.cessAmount = cessAmount;
	}

	public double getCessRate() {
		return cessRate;
	}

	public void setCessRate(double cessRate) {
		this.cessRate = cessRate;
	}

	public double getiGSTAmount() {
		return iGSTAmount;
	}

	public void setiGSTAmount(double iGSTAmount) {
		this.iGSTAmount = iGSTAmount;
	}

	public double getiGSTRate() {
		return iGSTRate;
	}

	public void setiGSTRate(double iGSTRate) {
		this.iGSTRate = iGSTRate;
	}

	public double getsGSTAmount() {
		return sGSTAmount;
	}

	public void setsGSTAmount(double sGSTAmount) {
		this.sGSTAmount = sGSTAmount;
	}

	public double getsGSTRate() {
		return sGSTRate;
	}

	public void setsGSTRate(double sGSTRate) {
		this.sGSTRate = sGSTRate;
	}

	public double getTaxableAmount() {
		return taxableAmount;
	}

	public void setTaxableAmount(double taxableAmount) {
		this.taxableAmount = taxableAmount;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
	
	public void addSeats(String columnNo, String rowNo, Integer height, Integer width, Integer seatType, String seatName,
			Integer seatIndex, double seatFare, boolean isLadiesSeat, boolean isMalesSeat, boolean seatStatus,
			String currencyCode, double tax, double discount, double basePrice, double publishedPrice,
			double otherCharges, double offeredPrice, Integer publishedPriceRoundedOff, Integer offeredPriceRoundedOff,
			double agentCommission, double agentMarkUp, double tds, double cGSTAmount, double cGSTRate,
			double cessAmount, double cessRate, double iGSTAmount, double iGSTRate, double sGSTAmount, double sGSTRate,
			double taxableAmount) {
		this.busSeats.add(new BusSeat(columnNo, rowNo, height, width, seatType, seatName,
				 seatIndex, seatFare, isLadiesSeat, isMalesSeat, seatStatus,
				 currencyCode, tax, discount, basePrice, publishedPrice,
				 otherCharges, offeredPrice, publishedPriceRoundedOff, offeredPriceRoundedOff,
				 agentCommission, agentMarkUp, tds, cGSTAmount, cGSTRate,
				 cessAmount, cessRate, iGSTAmount, iGSTRate, sGSTAmount, sGSTRate,
				 taxableAmount, this));
	}
	
	public void addPax(String title, String firstName, String lastName, String phoneNo, String email, String idNumber,
			String idType, Integer gender, Integer age, Integer seatId, boolean leadPassenger, String address) {
		this.busPassengers.add(new BusPassenger(title, firstName, lastName, phoneNo, email, idNumber,
				idType, gender, age, seatId, leadPassenger, address, this));
	}
	
}
