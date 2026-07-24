package com.easygofly.admin.setting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.easygofly.admin.LogService;
import com.easygofly.entity.Setting;
import com.easygofly.entity.SettingCategory;
import com.easygofly.entity.TBObusCity;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

 
@Service
public class SettingService {
	
	@Autowired private SettingRepository settingRepo;
	@Autowired private LogService logService;
	@Autowired private TBObusCityRepository busCityRepo;
	
	public List<Setting> listAllSettings() {
		return (List<Setting>) settingRepo.findAll();
	}
	
	public GeneralSettingBag getGeneralSettingBag() {
		List<Setting> settings = new ArrayList<>();
		List<Setting> settingsGenaral = settingRepo.findByCategory(SettingCategory.GENERAL);
		List<Setting> settingsCurrency = settingRepo.findByCategory(SettingCategory.CURRENCY);
		List<Setting> settingsAPIService = settingRepo.findByCategory(SettingCategory.API_SERVICE);
		List<Setting> settingsAPIToken = settingRepo.findByCategory(SettingCategory.API_TOKEN);
		
		settings.addAll(settingsGenaral);
		settings.addAll(settingsCurrency);
		settings.addAll(settingsAPIService);
		settings.addAll(settingsAPIToken);
		
		return new GeneralSettingBag(settings);
		
	}
	
	public List<Setting> getGeneralSetting() {
		return settingRepo.findByTwoCategories(SettingCategory.GENERAL, SettingCategory.CURRENCY);
	}
	
	public void saveAll(Iterable<Setting> settings) {
		settingRepo.saveAll(settings);
	}
	
	public List<Setting> getPaymentSettings() {
		return settingRepo.findByCategory(SettingCategory.PAYMENT);
	}
	
	public List<Setting> getMailServerSettings() {
		return settingRepo.findByCategory(SettingCategory.MAIL_SERVER);
	}
	
	public List<Setting> getMailTemplateSettings() {
		return settingRepo.findByCategory(SettingCategory.MAIL_TEMPLATES);
	}
	
	public List<Setting> getAPIServiceSettings() {
		return settingRepo.findByCategory(SettingCategory.API_SERVICE);
	}
	
	public List<Setting> getAPITokenSettings() {
		return settingRepo.findByCategory(SettingCategory.API_TOKEN);
	}



	public RedirectAttributes apiOnlineCityList(RedirectAttributes red)
			throws IOException {

        // Get API details from Settings.
		GeneralSettingBag apiServiceSettingBag = getGeneralSettingBag();
        
		StringBuilder responseBody = new StringBuilder();
		
		System.out.println("Bus City Url: " + apiServiceSettingBag.getBusCity());

		// Create URL object with the API end-point
        URL url = new URL(apiServiceSettingBag.getBusCity());

        // Open a connection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
  
        // Enable writing data to the connection
        connection.setDoOutput(true);
        
     // Create the request body
        String requestBody = "{\r\n"
        		+ "\"TokenId\": \"" + apiServiceSettingBag.getTBOtoken() + "\",\r\n"
        		+ "\"IpAddress\": \"" + apiServiceSettingBag.getUserIP() + "\",\r\n"
        		+ "\"ClientId\": \"" + apiServiceSettingBag.getTBOclientID() + "\"\r\n"
        		+ "}";

        System.out.println(requestBody);
        logService.generateLog(requestBody);
		// Write the request body to the connection's output stream
		OutputStream outputStream = connection.getOutputStream();
		outputStream.write(requestBody.getBytes());
		outputStream.flush();
		outputStream.close();

		// Read the response body
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String line;
		while ((line = bufferedReader.readLine()) != null) {
		    responseBody.append(line);
		}
		bufferedReader.close();
        connection.disconnect();
        
        // Convert JSON to BusCityResponse
        ObjectMapper objectMapper = new ObjectMapper();
        try {
			BusCityResponse busCityResponse = objectMapper.readValue(responseBody.toString(), BusCityResponse.class);

			if (busCityResponse.BusCities != null) {
				busCityRepo.deleteAll();
				List<TBObusCity> busCities = new ArrayList<>();
				
				busCityResponse.BusCities.forEach(city -> {
					busCities.add(new TBObusCity(city.CityId, city.CityName));
				});
				
				Iterable<TBObusCity> iterableBusCity = busCities;
				
				busCityRepo.saveAll(iterableBusCity);
				
				return red.addAttribute("message", "Bus City addition was successful!");
			} else {
			    
				return red.addAttribute("message", "Bus City addition failed!");
				
			}
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return red.addAttribute("message", "Bus City addition failed!");
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return red.addAttribute("message", "Bus City addition failed!");
		}
	}
	

	//Static pojo
	public static class BusCityResponse {
	    @JsonProperty("TokenId")
	    private String TokenId;
	    
	    @JsonProperty("Status")
	    private Integer Status;
	    
	    @JsonProperty("Error")
	    private String Error;
	    
	    @JsonProperty("BusCities")
	    private List<BusCity> BusCities;
	    
		public BusCityResponse() {}

		public String getTokenId() {
			return TokenId;
		}

		public void setTokenId(String tokenId) {
			TokenId = tokenId;
		}

		public Integer getStatus() {
			return Status;
		}

		public void setStatus(Integer status) {
			Status = status;
		}

		public String getError() {
			return Error;
		}

		public void setError(String error) {
			Error = error;
		}

		public List<BusCity> getBusCities() {
			return BusCities;
		}

		public void setBusCities(List<BusCity> busCities) {
			BusCities = busCities;
		}
	}
	
	public static class BusCity {
	    @JsonProperty("CityId")
        private Integer CityId;
	    
	    @JsonProperty("CityName")
        private String CityName;
	    
	    
        
		public BusCity() {}

		public BusCity(Integer cityId, String cityName) {
			CityId = cityId;
			CityName = cityName;
		}
		
		public Integer getCityId() {
			return CityId;
		}
		public void setCityId(Integer cityId) {
			CityId = cityId;
		}
		public String getCityName() {
			return CityName;
		}
		public void setCityName(String cityName) {
			CityName = cityName;
		}
    }
	


}
