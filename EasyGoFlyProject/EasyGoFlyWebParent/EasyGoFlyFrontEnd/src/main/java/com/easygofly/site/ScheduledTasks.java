package com.easygofly.site;

import java.io.IOException;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.easygofly.entity.Setting;
import com.easygofly.site.flight.OnlineFlightService;
import com.easygofly.site.setting.SettingService;

@Component
public class ScheduledTasks {
	@Autowired private OnlineFlightService onlineFlightService;
	@Autowired private LogService logService;
	@Autowired private SettingService settingService ;
	
	@Scheduled(fixedRate = 43200000)
    public void taskFetchFlightToken() {
		try {
        	StringBuilder responseBody = new StringBuilder();
        	
            onlineFlightService.apiAuthentication(responseBody);
            
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
