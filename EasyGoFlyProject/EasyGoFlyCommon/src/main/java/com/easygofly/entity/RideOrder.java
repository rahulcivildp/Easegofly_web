package com.easygofly.entity;

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

@Entity
@Table(name = "ride_orders")
public class RideOrder {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String date;
	
	@Column(name = "Order_Name", unique = true)
	private String orderName;
	
	private OrderStatus status;
	
	@Column(name = "Total_Amount")
	private Double totalAmount;

	@Column(name = "Base_Fare")
    private Double baseFare;
    
	private Double taxes;
    
    private Double discount;
    
    private Double convenience;

	@Column(name = "Pax_Num")
    private Integer paxNum;

	@Column(name = "Cab_Num")
    private Integer cabNum;
	
    private String phone;
	
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "driver_id", referencedColumnName = "id")
    private Driver driverId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "historyId", referencedColumnName = "id")
    private RideHistory historyId;

	@ManyToOne
	@JoinColumn(name = "customer_id")
	private Customer customer;
	
	@ManyToOne
	@JoinColumn(name = "total_transaction_id")
	private TotalTransaction totalTransaction;
	

    public RideOrder() {}

	public RideOrder(String date, String orderName, OrderStatus status, Double totalAmount, Double baseFare,
			Double taxes, Double discount, Double convenience, Integer paxNum, Integer cabNum,
			String phone, Driver driverId) {
		this.date = date;
		this.orderName = orderName;
		this.status = status;
		this.totalAmount = totalAmount;
		this.baseFare = baseFare;
		this.taxes = taxes;
		this.discount = discount;
		this.convenience = convenience;
		this.paxNum = paxNum;
		this.cabNum = cabNum;
		this.phone = phone;
		this.driverId = driverId;
	}

	// Getters and Setters
    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }
    
	public RideHistory getHistoryId() {
		return historyId;
	}

	public void setHistoryId(RideHistory historyId) {
		this.historyId = historyId;
	}

	public Double getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(Double baseFare) {
        this.baseFare = baseFare;
    }

    public Double getTaxes() {
        return taxes;
    }

    public void setTaxes(Double taxes) {
        this.taxes = taxes;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Double getConvenience() {
        return convenience;
    }

    public void setConvenience(Double convenience) {
        this.convenience = convenience;
    }

    public Integer getPaxNum() {
        return paxNum;
    }

    public void setPaxNum(Integer paxNum) {
        this.paxNum = paxNum;
    }

    public Integer getCabNum() {
        return cabNum;
    }

    public void setCabNum(Integer cabNum) {
        this.cabNum = cabNum;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Driver getDriverId() {
		return driverId;
	}

	public void setDriverId(Driver driverId) {
		this.driverId = driverId;
	}
	
	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getOrderName() {
		return orderName;
	}

	public void setOrderName(String orderName) {
		this.orderName = orderName;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	
	public TotalTransaction getTotalTransaction() {
		return totalTransaction;
	}

	public void setTotalTransaction(TotalTransaction totalTransaction) {
		this.totalTransaction = totalTransaction;
	}

	
	
}
