package com.easygofly.site.bus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easygofly.site.LogService;

@Service
public class OnlineBusService {
	@Autowired private LogService logService;
	
	public String tokenId = "";
	public String traceId = "";
	public String resultIndex = "";


	public int apiAuthenticationBus(HttpURLConnection connection, StringBuilder responseBody)
			throws IOException {
		
        // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
        
        
        // Enable writing data to the connection
        connection.setDoOutput(true);

        // Create the request body
//        String requestBody = "{"
//        		+ "\"ClientId\": \"tboprod\", "
//        		+ "\"UserName\": \"CCUA927\", "
//        		+ "\"Password\": \"#API@Air&72\", "
//        		+ "\"EndUserIp\": \"89.116.231.35\""
//        		+ "}";
        
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
		return responseCode;
	}
	 
	public int apiOnlineSearchBus(HttpURLConnection connection, StringBuilder responseBody, String cityId1, String cityId2, Date date)
			throws IOException {
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); 
		String strDate = dateFormat.format(date);
		
        // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
  
        // Enable writing data to the connection
        connection.setDoOutput(true);
        
     // Create the request body
        String requestBody = "{\r\n"
        		+ "\"DateOfJourney\": \"" + strDate + "\", \r\n"
        		+ "\"DestinationId\": \"" + cityId1 + "\",\r\n"
        		+ "\"EndUserIp\": \"89.116.231.35\",\r\n"
        		+ "\"OriginId\": \"" + cityId2 + "\",\r\n"
        		+ "\"TokenId\": \"" + tokenId + "\",\r\n"
        		+ "\"PreferredCurrency\": \"INR\"\r\n"
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
		return responseCode;
	}

	public int apiOnlineBusSeatLayout(HttpURLConnection connection, StringBuilder responseBody, Integer resultIndex)
			throws IOException {
		
        // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
  
        // Enable writing data to the connection
        connection.setDoOutput(true);
        
     // Create the request body
        String requestBody = "{\r\n"
        		+ "  \"EndUserIp\": \"89.116.231.35\",\r\n"
        		+ "  \"ResultIndex\": " + resultIndex + ",\r\n"
        		+ "  \"TraceId\": \"" + traceId + "\",\r\n"
        		+ "  \"TokenId\": \"" + tokenId + "\"\r\n"
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
		return responseCode;
	}

	public int apiOnlineBusBoardingPoint(HttpURLConnection connection, StringBuilder responseBody, Integer resultIndex)
			throws IOException {
		
        // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
  
        // Enable writing data to the connection
        connection.setDoOutput(true);
        
     // Create the request body
        String requestBody = "{\r\n"
        		+ "  \"EndUserIp\": \"89.116.231.35\",\r\n"
        		+ "  \"ResultIndex\": " + resultIndex + ",\r\n"
        		+ "  \"TraceId\": \"" + traceId + "\",\r\n"
        		+ "  \"TokenId\": \"" + tokenId + "\"\r\n"
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
		return responseCode;
	}

}
