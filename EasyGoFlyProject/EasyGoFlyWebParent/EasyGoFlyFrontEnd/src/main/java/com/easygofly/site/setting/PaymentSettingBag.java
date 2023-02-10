package com.easygofly.site.setting;

import java.util.List;

import com.easygofly.entity.Setting;
import com.easygofly.entity.SettingBag;

public class PaymentSettingBag extends SettingBag{

	public PaymentSettingBag(List<Setting> listSettings) {
		super(listSettings);
	}

	public String getURL() {
		return super.getValue("PAYMENT_API_BASE_URL");
	}
	
	public String getKeyId() {
		return super.getValue("PAYMENT_CLIENT_ID");
	}
	
	public String getSecretKey() {
		return super.getValue("PAYMENT_CLIENT_SECRET_KEY");
	}
}
