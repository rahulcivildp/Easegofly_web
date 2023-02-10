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
}
