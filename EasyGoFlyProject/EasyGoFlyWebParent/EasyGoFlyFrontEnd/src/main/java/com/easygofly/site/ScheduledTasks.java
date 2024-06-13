package com.easygofly.site;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.easygofly.entity.Setting;
import com.easygofly.site.flightAPI.OnlineFlightService;
import com.easygofly.site.setting.SettingService;

@Component
public class ScheduledTasks {
	@Autowired private OnlineFlightService onlineFlightService;
	@Autowired private LogService logService;
	@Autowired private SettingService settingService ;
	
	@Scheduled(fixedRate = 2000000000)
    public void taskFetchFlightToken() {
try {
        	
        	// Create URL object with the API end-point
            URL url = new URL("https://api.travelboutiqueonline.com/SharedAPI/SharedData.svc/rest/Authenticate");
            
        	// Create URL object with the API end-point
//            URL url = new URL("http://api.tektravels.com/SharedServices/SharedData.svc/rest/Authenticate");

            // Open a connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
        	StringBuilder responseBody = new StringBuilder();
        	
            onlineFlightService.apiAuthentication(connection, responseBody);
            
            JSONObject jsonObj = new JSONObject(responseBody.toString());
            
            String tokenId = jsonObj.get("TokenId").toString();
            System.out.println(jsonObj);
            logService.generateLog(jsonObj.toString());
            
            Setting setting = settingService.findByKey("TBO_API_FLIGHT_TOKEN");
            setting.setValue(tokenId);
            settingService.saveSetting(setting);
            
            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
