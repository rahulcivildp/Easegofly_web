package com.easygofly.site.holidays;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easygofly.site.LogService;
import com.easygofly.site.setting.APIServiceSettingBag;
import com.easygofly.site.setting.SettingService;

@Service
public class OnlineHolidayService {
	@Autowired private LogService logService;
	@Autowired private SettingService settingService;

	public String tokenId = "";
	public String traceId = "";
	public String resultIndex = "";

	public int apiAuthentication(StringBuilder responseBody)
			throws IOException {
    	// Create URL object with the API end-point
        URL url = new URL("https://api.travelboutiqueonline.com/SharedAPI/SharedData.svc/rest/Authenticate");
        
    	// Create URL object with the API end-point
//        URL url = new URL("http://api.tektravels.com/SharedServices/SharedData.svc/rest/Authenticate");

        // Open a connection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
        // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
        
        
        // Enable writing data to the connection
        connection.setDoOutput(true);
        ;
        // Create the request body
        //Test Credentials
      String requestBody = "{"
      		+ "\"ClientId\": \"ApiIntegrationNew\", "
      		+ "\"UserName\": \"aladdin\", "
      		+ "\"Password\": \"aladdin@1234\", "
      		+ "\"EndUserIp\": \"89.116.231.35\""
      		+ "}";
        
        System.out.println(requestBody);
        logService.generateLog(requestBody);
		// Write the request body to the connection's output stream
		OutputStream outputStream = connection.getOutputStream();
		outputStream.write(requestBody.getBytes());
		outputStream.flush();
		outputStream.close();

		// Get the response
		int responseCode = connection.getResponseCode();

		// Read the response body
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String line;
		while ((line = bufferedReader.readLine()) != null) {
		    responseBody.append(line);
		}
		bufferedReader.close();
        connection.disconnect();
        
		return responseCode;
	}
	
	
	public StringBuilder apiHolidaySearch( Integer cityId, String countryCode, String fromDate, String toDate, Integer adultCount, Integer childCount, Integer[] childAge) throws Exception {
        // Create String response body;.
		StringBuilder responseBody = new StringBuilder();
		
        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
		
    	// Create URL object with the API end-point
        URL url = new URL("http://api.tektravels.com/BookingEngineService_SightSeeing/SightseeingService.svc/rest/search/");

        // Open a connection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
        // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
        
        
        // Enable writing data to the connection
        connection.setDoOutput(true);
        ;
        // Create the request body
        String requestBody = "{\r\n"
        		+ "  \"CityId\": \"" + cityId + "\",\r\n"
        		+ "  \"CountryCode\": \"" + countryCode + "\",\r\n"
        		+ "  \"FromDate\": \"" + fromDate + "\",\r\n"
        		+ "  \"ToDate\": \"" + toDate +"\",\r\n"
        		+ "  \"AdultCount\": " + adultCount + ",\r\n"
        		+ "  \"ChildCount\": " + childCount + ",\r\n"
        		+ "  \"ChildAge\": " + childAge + ",\r\n"
        		+ "  \"PreferredLanguage\": 0,\r\n"
        		+ "  \"PreferredCurrency\": \"INR\",\r\n"
        		+ "  \"IsBaseCurrencyRequired\": false,\r\n"
        		+ "  \"EndUserIp\": \"" + apiServiceSettingBag.getUserIP() + "\",\r\n"
        		+ "  \"TokenId\": \"" + tokenId + "\",\r\n"
        		+ "  \"KeyWord\": \"\"\r\n"
        		+ "}";
        
        System.out.println(requestBody);
        logService.generateLog(requestBody);
		// Write the request body to the connection's output stream
		OutputStream outputStream = connection.getOutputStream();
		outputStream.write(requestBody.getBytes());
		outputStream.flush();
		outputStream.close();

		// Get the response
		int responseCode = connection.getResponseCode();
		
		System.out.println(" Response Code: " + responseCode);

		// Read the response body
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String line;
		while ((line = bufferedReader.readLine()) != null) {
		    responseBody.append(line);
		}
		bufferedReader.close();
        connection.disconnect();
        
		return responseBody;
		
	}
	
	
	public StringBuilder apiHolidayAvailabilty( Integer resultIndex) throws Exception {
        // Create String response body;.
		StringBuilder responseBody = new StringBuilder();
		
        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
		
    	// Create URL object with the API end-point
        URL url = new URL("http://api.tektravels.com/BookingEngineService_SightSeeing/SightseeingService.svc/rest/GetAvailability/");

        // Open a connection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
        // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
        
        
        // Enable writing data to the connection
        connection.setDoOutput(true);

        // Create the request body
           String requestBody = "{"
           		+ "\"EndUserIp\": \"" + apiServiceSettingBag.getUserIP() + "\", "
           		+ "\"TokenId\": \"" + tokenId + "\", "
           		+ "\"TraceId\": \"" + traceId + "\", "
           		+ "\"ResultIndex\": " + resultIndex + ""
           		+ "}";
        
        System.out.println(requestBody);
        logService.generateLog(requestBody);
		// Write the request body to the connection's output stream
		OutputStream outputStream = connection.getOutputStream();
		outputStream.write(requestBody.getBytes());
		outputStream.flush();
		outputStream.close();

		// Get the response
		int responseCode = connection.getResponseCode();
		
		System.out.println(" Response Code: " + responseCode);

		// Read the response body
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String line;
		while ((line = bufferedReader.readLine()) != null) {
		    responseBody.append(line);
		}
		bufferedReader.close();
        connection.disconnect();
        
		return responseBody;
		
	}
	
	
	public StringBuilder apiHolidayBlock( Integer resultIndex) throws Exception {
        // Create String response body;.
		StringBuilder responseBody = new StringBuilder();
		
        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
		
    	// Create URL object with the API end-point
        URL url = new URL("https://api.tektravels.com/BookingEngineService_SightSeeingBook/SightseeingService.svc/rest/Block/");

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
           		+ "    \"AgeBands\": [\r\n"
           		+ "        {\r\n"
           		+ "            \"AgeBandIndex\": 1,\r\n"
           		+ "            \"BandDescription\": \"Adult\",\r\n"
           		+ "            \"BandQuantity\": 1,\r\n"
           		+ "            \"IsAgeRequired\": false,\r\n"
           		+ "            \"MaximumCount\": 0,\r\n"
           		+ "            \"MinimumCount\": 0\r\n"
           		+ "        }\r\n"
           		+ "    ],\r\n"
           		+ "    \"TourIndex\": 1,\r\n"
           		+ "    \"ResultIndex\": " + resultIndex + ",\r\n"
           		+ "    \"EndUserIp\": \"" + apiServiceSettingBag.getUserIP() + "\",\r\n"
           		+ "    \"TraceId\": \"" + traceId + "\",\r\n"
           		+ "    \"TokenId\": \"" + tokenId + "\"\r\n"
           		+ "}";
        
        System.out.println(requestBody);
        logService.generateLog(requestBody);
		// Write the request body to the connection's output stream
		OutputStream outputStream = connection.getOutputStream();
		outputStream.write(requestBody.getBytes());
		outputStream.flush();
		outputStream.close();

		// Get the response
		int responseCode = connection.getResponseCode();
		
		System.out.println(" Response Code: " + responseCode);

		// Read the response body
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String line;
		while ((line = bufferedReader.readLine()) != null) {
		    responseBody.append(line);
		}
		bufferedReader.close();
        connection.disconnect();
        
		return responseBody;
		
	}

}
