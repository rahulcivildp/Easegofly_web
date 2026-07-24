package com.easygofly.api;

import java.io.IOException;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.easygofly.api.flight.OnlineFlightService;
import com.easygofly.api.setting.SettingService;
import com.easygofly.entity.Setting;
 
@Component
public class ScheduledTasks {
	@Autowired private OnlineFlightService onlineFlightService;
	@Autowired private LogService logService;
	@Autowired private SettingService settingService ;
	
	@Scheduled(fixedRate = 23200000)
    public void taskFetchFlightToken() {
		try {
        	StringBuilder responseBody = onlineFlightService.apiAuthentication();
            
            JSONObject jsonObj = new JSONObject(responseBody.toString());
             
            String tokenId = jsonObj.get("TokenId").toString();
            System.out.println(jsonObj);
            logService.generateLog(jsonObj.toString());
            
            Setting setting = settingService.findByKey("TBO_API_FLIGHT_TOKEN"); 
            setting.setValue(tokenId);
            settingService.saveSetting(setting);
 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
