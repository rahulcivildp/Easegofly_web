package com.easygofly.api.setting;

import java.util.List;

import com.easygofly.entity.Setting;
import com.easygofly.entity.SettingBag;

public class APIServiceSettingBag extends SettingBag {

	public APIServiceSettingBag(List<Setting> listSettings) {
		super(listSettings);
	}

	public String getUsername() {
		return super.getValue("TBO_API_USERNAME");
	}
	
	public String getPassword() {
		return super.getValue("TBO_API_PASSWORD");
	}
	
	public String getClientId() {
		return super.getValue("TBO_API_CLIENTID");
	}
	
	public String getUserIP() {
		return super.getValue("TBO_API_USERIP");
	}

	public String getAuthURL() {
		return super.getValue("TBO_API_AUTH_URL");
	}

}
