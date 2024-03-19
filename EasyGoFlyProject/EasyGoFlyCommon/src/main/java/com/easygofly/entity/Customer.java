package com.easygofly.entity;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "customers")
public class Customer {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(length = 128, nullable = false, unique = true)
	private String email;
	
	@Column(length = 64, nullable = false)
	private String password;
	
	@Column(name = "first_name", length = 45, nullable = false)
	private String firstName;
	
	@Column(name = "last_name", length = 45, nullable = false)
	private String lastName;
	
	@Column(name = "phone", length = 128, nullable = false, unique = true)
	private String phone;
	
	@Column(name = "address_line1", length = 64)
	private String addressLine1;
	
	@Column(name = "address_line2", length = 64)
	private String addressLine2;
	
	@Column(name = "city", length = 45)
	private String city;
	
	@Column(name = "state", length = 45)
	private String state;
	
	@Column(name = "postal_code", length = 10)
	private String postalCode;
	
	@Column(name = "verification_code", length = 64)
	private String verificationCode;
	
	@Column(name = "created_time")
	private Date createdTime;
	
	@Column(name = "gender", length = 10)
	private String gender;
	
	@Column(name = "balance")
	private Integer balance;
	
	@Column(length = 128)
	private String photos;
	
	private boolean enabled;
	
	@Column(name = "otp_requested_time")
	private Date otpRequestedTime;
	
	@ManyToOne
	@JoinColumn(name = "country_id")
	private Country country;
	
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
				name = "customer_roles",
				joinColumns = @JoinColumn(name = "customer_id"),
				inverseJoinColumns = @JoinColumn(name = "role_id")
			)
	private Set<Role> roles = new HashSet<>();
	
	@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
	private List<SearchHistory> searchHistory = new ArrayList<>();
	
	@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
	private List<Order> orders = new ArrayList<>();
	
	@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
	private List<Request> requests = new ArrayList<>();
	
	@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
	private List<Transaction> transactions = new ArrayList<>();
	
	@Column(name="reset_password_token", length = 30)
	private String resetPasswordToken;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "authentication_type", length = 10)
	private AuthenticationType authenticationType;
	
	@OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "wallet_id")
	private Wallet wallet;
	
	@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
	private List<HotelHistory> hotelHistory = new ArrayList<>();
	
	
	public Customer() {}
	
	public Customer(Integer id) {
		this.id = id;
	}

	public Customer(String email, String firstName, String lastName, String phoneNumber, String addressLine1,
			String addressLine2, String city, String state, String postalCode, Date createdTime, Country country,
			String photos) {
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.phone = phoneNumber;
		this.addressLine1 = addressLine1;
		this.addressLine2 = addressLine2;
		this.city = city;
		this.state = state;
		this.postalCode = postalCode;
		this.createdTime = createdTime;
		this.country = country;
		this.photos = photos;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
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

	public String getVerificationCode() {
		return verificationCode;
	}

	public void setVerificationCode(String verificationCode) {
		this.verificationCode = verificationCode;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public String getPhotos() {
		return photos;
	}

	public void setPhotos(String photos) {
		this.photos = photos;
	}

	public Date getOtpRequestedTime() {
		return otpRequestedTime;
	}

	public void setOtpRequestedTime(Date otpRequestedTime) {
		this.otpRequestedTime = otpRequestedTime;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Integer getBalance() {
		return balance;
	}

	public void setBalance(Integer balance) {
		this.balance = balance;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public void addRole(Role role) {
		this.roles.add(role);
	}
	
	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public List<SearchHistory> getSearchHistory() {
		return searchHistory;
	}

	public void setSearchHistory(List<SearchHistory> searchHistory) {
		this.searchHistory = searchHistory;
	}
	
	public List<Order> getOrders() {
		return orders;
	}

	public void setOrders(List<Order> orders) {
		this.orders = orders;
	}

	public String getResetPasswordToken() {
		return resetPasswordToken;
	}

	public List<Transaction> getTransactions() {
		return transactions;
	}

	public void setTransactions(List<Transaction> transactions) {
		this.transactions = transactions;
	}

	public void setResetPasswordToken(String resetPasswordToken) {
		this.resetPasswordToken = resetPasswordToken;
	}

	public AuthenticationType getAuthenticationType() {
		return authenticationType;
	}

	public void setAuthenticationType(AuthenticationType authenticationType) {
		this.authenticationType = authenticationType;
	}

	public List<HotelHistory> getHotelHistory() {
		return hotelHistory;
	}

	public void setHotelHistory(List<HotelHistory> hotelHistory) {
		this.hotelHistory = hotelHistory;
	}

	public Wallet getWallet() {
		return wallet;
	}

	public void setWallet(Wallet wallet) {
		this.wallet = wallet;
	}

	public List<Request> getRequests() {
		return requests;
	}

	public void setRequests(List<Request> requests) {
		this.requests = requests;
	}

	public void addSerchHistory(String cityOne, String cityTwo, Integer passengerNum, String journeyClass, Integer adultNum, Integer childNum, Integer infantNum,
			String tripType, Date date) {
		this.searchHistory.add(new SearchHistory( cityOne,  cityTwo, passengerNum, journeyClass, adultNum, childNum, infantNum, tripType, date, this ));
	}

	public void addSerchHistoryReturn(String cityOne, String cityTwo, Integer passengerNum, String journeyClass, Integer adultNum, Integer childNum, Integer infantNum,
			String tripType, Date date, Date returnDate) {
		this.searchHistory.add(new SearchHistory(cityOne, cityTwo, passengerNum, journeyClass, adultNum, childNum, infantNum, tripType, date, returnDate, this));
	}
	
	public void addSerchHistory(Integer id, String cityOne, String cityTwo, Integer passengerNum, String journeyClass, Integer adultNum, Integer childNum, Integer infantNum,
			String tripType, Date date) {
		this.searchHistory.add(new SearchHistory( id, cityOne, cityTwo, passengerNum, journeyClass, adultNum, childNum, infantNum, tripType, date, this ));
	}

	public boolean hasRole(String roleName) {
		Iterator<Role> iterator= roles.iterator();
		while (iterator.hasNext()) {
			Role role = iterator.next();
			if (role.getName().equals(roleName)) {
				return true;
			}
		}
		return false;
	}
	
	private static final long OTP_VALID_DURATION = 5 * 60 * 1000;

	@Transient
    public boolean isOTPRequired() {
        if (this.getVerificationCode() == null) {
            return false;
        }
         
        long currentTimeInMillis = System.currentTimeMillis();
        long otpRequestedTimeInMillis = this.otpRequestedTime.getTime();
         
        if (otpRequestedTimeInMillis + OTP_VALID_DURATION < currentTimeInMillis) {
            // OTP expires
            return false;
        }
         
        return true;
    }
	
	@Transient
	public String getFullName() {
		return firstName + " " + lastName;
	}

	@Transient
	public String getPhotosImagePath() {
		if(id == null || photos == null) {
			return "/images/user.png";
		}
		return "/customer-photos/" + this.id + "/" + this.photos;
	}
	
	@Transient
	public String getImageLink() {
		return "/customer-photos/" + this.id + "/" + this.photos;
	}
	
	@Override
	public String toString() {
		return "Customer [id=" + id + ", email=" + email + ", firstName=" + firstName + ", lastName=" + lastName
				+ ", phoneNumber=" + phone + ", addressLine1=" + addressLine1 + ", addressLine2=" + addressLine2
				+ ", city=" + city + ", state=" + state + ", postalCode=" + postalCode + ", createdTime=" + createdTime
				+ ", photos=" + photos + ", enabled=" + enabled + ", country=" + country + ", roles=" + roles + "]";
	}
	
	
}
