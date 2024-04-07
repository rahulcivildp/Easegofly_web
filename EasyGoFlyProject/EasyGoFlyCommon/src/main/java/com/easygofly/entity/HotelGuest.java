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
@Table(name = "hotel_guests")
public class HotelGuest {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(name = "title")
	private String title;
	
	@Column(name = "first_name")
	private String firstName;
	
	@Column(name = "last_name")
	private String lastName;
	
	@Column(name = "phone_no")
	private String phoneNo;
	
	@Column(name = "email")
	private String email;
	
	@Column(name = "pax_type")
	private Integer paxType;
	
	@Column(name = "age")
	private Integer age;

	@Column(name = "lead_passenger")
	private boolean leadPassenger;
	
	@Column(name = "passport_no")
	private String passportNo;
	
	@Column(name = "passport_exp_date")
	private String passportExpDate;
	
	@Column(name = "passport_issue_date")
	private String passportIssueDate;
	
	@Column(name = "pan")
	private String pan;

	@ManyToOne
	@JoinColumn(name = "hotel_id")
	private Hotel hotel;
	

	public HotelGuest() {}

	public HotelGuest(String title, String firstName, String lastName, String phoneNo, String email, Integer paxType,
			Integer age, boolean leadPassenger, String passportNo, String passportExpDate, String passportIssueDate,
			String pan) {
		super();
		this.title = title;
		this.firstName = firstName;
		this.lastName = lastName;
		this.phoneNo = phoneNo;
		this.email = email;
		this.paxType = paxType;
		this.age = age;
		this.leadPassenger = leadPassenger;
		this.passportNo = passportNo;
		this.passportExpDate = passportExpDate;
		this.passportIssueDate = passportIssueDate;
		this.pan = pan;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Hotel getHotel() {
		return hotel;
	}

	public void setHotel(Hotel hotel) {
		this.hotel = hotel;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Integer getPaxType() {
		return paxType;
	}

	public void setPaxType(Integer paxType) {
		this.paxType = paxType;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public boolean isLeadPassenger() {
		return leadPassenger;
	}

	public void setLeadPassenger(boolean leadPassenger) {
		this.leadPassenger = leadPassenger;
	}

	public String getPassportNo() {
		return passportNo;
	}

	public void setPassportNo(String passportNo) {
		this.passportNo = passportNo;
	}

	public String getPassportExpDate() {
		return passportExpDate;
	}

	public void setPassportExpDate(String passportExpDate) {
		this.passportExpDate = passportExpDate;
	}

	public String getPassportIssueDate() {
		return passportIssueDate;
	}

	public void setPassportIssueDate(String passportIssueDate) {
		this.passportIssueDate = passportIssueDate;
	}

	public String getPan() {
		return pan;
	}

	public void setPan(String pan) {
		this.pan = pan;
	}
	

	
}
