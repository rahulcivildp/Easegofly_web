package com.easygofly.site.setting;

import java.util.List;

import com.easygofly.entity.Setting;
import com.easygofly.entity.SettingBag;

public class EmailSettingBag extends SettingBag{

	public EmailSettingBag(List<Setting> listSettings) {
		super(listSettings);
	}

	public String getHost() {
		return super.getValue("MAIL_HOST");
	}
	
	public String getPort() {
		return super.getValue("MAIL_PORT");
	}
	
	public String getUsername() {
		return super.getValue("MAIL_USERNAME");
	}

	public String getPassword() {
		return super.getValue("MAIL_PASSWORD");
	}
	
	public String getSmtpAuth() {
		return super.getValue("SMTP_AUTH");
	}

	public String getSmtpSecured() {
		return super.getValue("SMTP_SECURED");
	}
	
	public String getFromAddress() {
		return super.getValue("MAIL_FROM");
	}

	public String getSenderName() {
		return super.getValue("MAIL_SENDER_NAME");
	}
	
	public String getCunstomerVerifySubject() {
		return super.getValue("CUSTOMER_VERIFY_SUBJECT");
	}
	
	public String getCunstomerVerifyContent() {
		return super.getValue("CUSTOMER_VERIFY_CONTENT");
	}
	
	public String getFlightSuccessContent() {
		return super.getValue("ORDER_CONFIRMATION_CONTENT");
	}
	
	public String getFlightSuccessSubject() {
		return super.getValue("ORDER_CONFIRMATION_SUBJECT");
	}
	
	public String getHotelSuccessContent() {
		return super.getValue("HOTEL_SUCCESS_CONTENT");
	}
	
	public String getHotelSuccessSubject() {
		return super.getValue("HOTEL_SUCCESS_SUBJECT");
	}
	
	public String getBusSuccessContent() {
		return super.getValue("BUS_SUCCESS_CONTENT");
	}
	
	public String getBusSuccessSubject() {
		return super.getValue("BUS_SUCCESS_SUBJECT");
	}
	
	public String getHolidaySuccessContent() {
		return super.getValue("HOLIDAY_SUCCESS_CONTENT");
	}
	
	public String getHolidaySuccessSubject() {
		return super.getValue("HOLIDAY_SUCCESS_SUBJECT");
	}
	
	public String getWalletRechargeContent() {
		return super.getValue("WALLET_RECHARGE_CONTENT");
	}
	
	public String getWalletRechargeSubject() {
		return super.getValue("WALLET_RECHARGE_SUBJECT");
	}
}
