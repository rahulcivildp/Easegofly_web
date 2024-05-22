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
@Table(name = "bus_passengers")
public class BusPassenger {

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
	
	@Column(name = "id_number")
	private String idNumber;
	
	@Column(name = "id_type")
	private String idType;
	
	@Column(name = "gender")
	private Integer gender;
	
	@Column(name = "age")
	private Integer age;
	
	@Column(name = "seat_id")
	private Integer seatId;

	@Column(name = "lead_passenger")
	private boolean leadPassenger;
	
	@Column(name = "address", length = 500)
	private String address;
	
	@ManyToOne
	@JoinColumn(name = "bus_id")
	private Bus bus;

	
	public BusPassenger() {}

	public BusPassenger(String title, String firstName, String lastName, String phoneNo, String email, String idNumber,
			String idType, Integer gender, Integer age, Integer seatId, boolean leadPassenger, String address,
			Bus bus) {
		this.title = title;
		this.firstName = firstName;
		this.lastName = lastName;
		this.phoneNo = phoneNo;
		this.email = email;
		this.idNumber = idNumber;
		this.idType = idType;
		this.gender = gender;
		this.age = age;
		this.seatId = seatId;
		this.leadPassenger = leadPassenger;
		this.address = address;
		this.bus = bus;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getSeatId() {
		return seatId;
	}

	public void setSeatId(Integer seatId) {
		this.seatId = seatId;
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

	public String getIdNumber() {
		return idNumber;
	}

	public void setIdNumber(String idNumber) {
		this.idNumber = idNumber;
	}

	public String getIdType() {
		return idType;
	}

	public void setIdType(String idType) {
		this.idType = idType;
	}

	public Integer getGender() {
		return gender;
	}

	public void setGender(Integer gender) {
		this.gender = gender;
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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Bus getBus() {
		return bus;
	}

	public void setBus(Bus bus) {
		this.bus = bus;
	}
	
	
	
}
