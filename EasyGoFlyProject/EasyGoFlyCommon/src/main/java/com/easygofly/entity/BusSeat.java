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
@Table(name = "bus_seats")
public class BusSeat {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "column_no")
	private String columnNo;

	@Column(name = "row_no")
	private String rowNo;

	@Column(name = "height")
	private Integer height;
	
	@Column(name = "width")
	private Integer width;

	@Column(name = "seat_type")
	private Integer seatType;

	@Column(name = "seat_name")
	private String seatName;

	@Column(name = "seat_index")
	private Integer seatIndex;

	@Column(name = "seat_fare")
	private double seatFare;

	@Column(name = "is_ladies_seat")
	private boolean isLadiesSeat;

	@Column(name = "is_males_seat")
	private boolean isMalesSeat;

	@Column(name = "seat_status")
	private boolean seatStatus;

	@Column(name = "currency_code")
	private String currencyCode;

	@Column(nullable = true)
	private double tax;
	
	@Column(name = "discount")
	private double discount;
	
	@Column(name = "base_price")
	private double basePrice;
	
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
	@JoinColumn(name = "bus_id")
	private Bus bus;

	
	
	public BusSeat() {}

	public BusSeat(String columnNo, String rowNo, Integer height, Integer width, Integer seatType, String seatName,
			Integer seatIndex, double seatFare, boolean isLadiesSeat, boolean isMalesSeat, boolean seatStatus,
			String currencyCode, double tax, double discount, double basePrice, double publishedPrice,
			double otherCharges, double offeredPrice, Integer publishedPriceRoundedOff, Integer offeredPriceRoundedOff,
			double agentCommission, double agentMarkUp, double tds, double cGSTAmount, double cGSTRate,
			double cessAmount, double cessRate, double iGSTAmount, double iGSTRate, double sGSTAmount, double sGSTRate,
			double taxableAmount) {
		this.columnNo = columnNo;
		this.rowNo = rowNo;
		this.height = height;
		this.width = width;
		this.seatType = seatType;
		this.seatName = seatName;
		this.seatIndex = seatIndex;
		this.seatFare = seatFare;
		this.isLadiesSeat = isLadiesSeat;
		this.isMalesSeat = isMalesSeat;
		this.seatStatus = seatStatus;
		this.currencyCode = currencyCode;
		this.tax = tax;
		this.discount = discount;
		this.basePrice = basePrice;
		this.publishedPrice = publishedPrice;
		this.otherCharges = otherCharges;
		this.offeredPrice = offeredPrice;
		this.publishedPriceRoundedOff = publishedPriceRoundedOff;
		this.offeredPriceRoundedOff = offeredPriceRoundedOff;
		this.agentCommission = agentCommission;
		this.agentMarkUp = agentMarkUp;
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
	}
	
	public BusSeat(String columnNo, String rowNo, Integer height, Integer width, Integer seatType, String seatName,
			Integer seatIndex, double seatFare, boolean isLadiesSeat, boolean isMalesSeat, boolean seatStatus,
			String currencyCode, double tax, double discount, double basePrice, double publishedPrice,
			double otherCharges, double offeredPrice, Integer publishedPriceRoundedOff, Integer offeredPriceRoundedOff,
			double agentCommission, double agentMarkUp, double tds, double cGSTAmount, double cGSTRate,
			double cessAmount, double cessRate, double iGSTAmount, double iGSTRate, double sGSTAmount, double sGSTRate,
			double taxableAmount, Bus bus) {
		super();
		this.columnNo = columnNo;
		this.rowNo = rowNo;
		this.height = height;
		this.width = width;
		this.seatType = seatType;
		this.seatName = seatName;
		this.seatIndex = seatIndex;
		this.seatFare = seatFare;
		this.isLadiesSeat = isLadiesSeat;
		this.isMalesSeat = isMalesSeat;
		this.seatStatus = seatStatus;
		this.currencyCode = currencyCode;
		this.tax = tax;
		this.discount = discount;
		this.basePrice = basePrice;
		this.publishedPrice = publishedPrice;
		this.otherCharges = otherCharges;
		this.offeredPrice = offeredPrice;
		this.publishedPriceRoundedOff = publishedPriceRoundedOff;
		this.offeredPriceRoundedOff = offeredPriceRoundedOff;
		this.agentCommission = agentCommission;
		this.agentMarkUp = agentMarkUp;
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
		this.bus = bus;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getColumnNo() {
		return columnNo;
	}

	public void setColumnNo(String columnNo) {
		this.columnNo = columnNo;
	}

	public String getRowNo() {
		return rowNo;
	}

	public void setRowNo(String rowNo) {
		this.rowNo = rowNo;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public Integer getSeatType() {
		return seatType;
	}

	public void setSeatType(Integer seatType) {
		this.seatType = seatType;
	}

	public String getSeatName() {
		return seatName;
	}

	public void setSeatName(String seatName) {
		this.seatName = seatName;
	}

	public Integer getSeatIndex() {
		return seatIndex;
	}

	public void setSeatIndex(Integer seatIndex) {
		this.seatIndex = seatIndex;
	}

	public double getSeatFare() {
		return seatFare;
	}

	public void setSeatFare(double seatFare) {
		this.seatFare = seatFare;
	}

	public boolean isLadiesSeat() {
		return isLadiesSeat;
	}

	public void setLadiesSeat(boolean isLadiesSeat) {
		this.isLadiesSeat = isLadiesSeat;
	}

	public boolean isMalesSeat() {
		return isMalesSeat;
	}

	public void setMalesSeat(boolean isMalesSeat) {
		this.isMalesSeat = isMalesSeat;
	}

	public boolean isSeatStatus() {
		return seatStatus;
	}

	public void setSeatStatus(boolean seatStatus) {
		this.seatStatus = seatStatus;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
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

	public double getBasePrice() {
		return basePrice;
	}

	public void setBasePrice(double basePrice) {
		this.basePrice = basePrice;
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

	public Bus getBus() {
		return bus;
	}

	public void setBus(Bus bus) {
		this.bus = bus;
	}
	
}
