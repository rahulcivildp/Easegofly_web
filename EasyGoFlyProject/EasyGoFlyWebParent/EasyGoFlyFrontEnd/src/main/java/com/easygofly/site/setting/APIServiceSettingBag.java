package com.easygofly.site.setting;

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
	
	public String getDefaultURL() {
		return super.getValue("TBO_API_DEFAULT_URL");
	}
	
	public String getHotelURL() {
		return super.getValue("TBO_API_HOTEL_URL");
	}
	
	public String getBusURL() {
		return super.getValue("TBO_API_BUS_URL");
	}

	public String getAirIqUsername() {
		return super.getValue("AIRIQ_API_USERNAME");
	}
	
	public String getAirIqPassword() {
		return super.getValue("AIRIQ_API_PASSWORD");
	}
	
	public String getAirIqApiKey() {
		return super.getValue("AIRIQ_API_KEY");
	}
	
	public String getMTravelsUserIp() {
		return super.getValue("MTRAVELS_API_USER_IP");
	}
	
	public String getMTravelsToken() {
		return super.getValue("MTRAVELS_FINAL_TOKEN");
	}
	
	public String getMTravelsAPIKey() {
		return super.getValue("MTRAVELS_API_KEY");
	}
	
	public String getEase2flyUser() {
		return super.getValue("EASE2FLY_USERNAME");
	}
	
	public String getEase2flyPassword() {
		return super.getValue("EASE2FLY_PASSWORD");
	}
	
	public String getEase2flyAPIKey() {
		return super.getValue("EASE2FLY_API_KEY");
	}


}
