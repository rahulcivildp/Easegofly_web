package com.easygofly.site.setting;

import java.util.List;

import com.easygofly.entity.Setting;
import com.easygofly.entity.SettingBag;

public class APITokenSettingBag extends SettingBag{

	public APITokenSettingBag(List<Setting> listSettings) {
		super(listSettings);
	}

	public String getBusTokenNo() {
		return super.getValue("TBO_API_BUS_TOKEN");
	}

	public String getFlightTokenNo() {
		return super.getValue("TBO_API_FLIGHT_TOKEN");
	}

	public String getHolidayTokenNo() {
		return super.getValue("TBO_API_HOLIDAY_TOKEN");
	}

	public String getHotelTokenNo() {
		return super.getValue("TBO_API_HOTEL_TOKEN");
	}


}
