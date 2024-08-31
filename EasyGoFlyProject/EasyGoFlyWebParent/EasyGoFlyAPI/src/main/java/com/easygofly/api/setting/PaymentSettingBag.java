package com.easygofly.api.setting;

import java.util.List;

import com.easygofly.entity.Setting;
import com.easygofly.entity.SettingBag;

public class PaymentSettingBag extends SettingBag{

	public PaymentSettingBag(List<Setting> listSettings) {
		super(listSettings);
	}

	public String getAPIEnvironment() {
		return super.getValue("API_ENVIRONMENT");
	}
	
	public String getMarchentKey() {
		return super.getValue("ZAAKPAY_MERCHANT_IDENTIFIER");
	}
	
	public String getSecretKey() {
		return super.getValue("ZAAKPAY_SECRET_KEY");
	}
	
	public String getTransactionURL() {
		return super.getValue("TRANSACTION_API_URL");
	}
	
	public String getBuyerEmail() {
		return super.getValue("BUYEREMAIL");
	}
}
