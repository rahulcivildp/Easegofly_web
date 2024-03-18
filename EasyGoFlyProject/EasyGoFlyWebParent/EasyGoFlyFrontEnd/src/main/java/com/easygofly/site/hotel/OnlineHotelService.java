package com.easygofly.site.hotel;

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
public class OnlineHotelService {
	@Autowired private LogService logService;
	
	public String tokenId = "";
	public String traceId = "";
	public String resultIndex = "";
	

	public int apiOnlineSearchHotel(HttpURLConnection connection, StringBuilder responseBody, String cityOne, String cityTwo, Integer adultNum, Integer childNum, Integer infantNum, Date date)
			throws IOException {
		
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"); 
		String strDate = dateFormat.format(date);
		
        // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
        
        
        // Enable writing data to the connection
        connection.setDoOutput(true);
        
     // Create the request body
        String requestBody = "{\r\n"
        		+ "  \"CheckInDate\": \"" + strDate + "\",\r\n"
        		+ "  \"NoOfNights\": \"1\",\r\n"
        		+ "  \"CountryCode\": \"IN\",\r\n"
        		+ "  \"CityId\": \"130443\",\r\n"
        		+ "  \"ResultCount\": null,\r\n"
        		+ "  \"PreferredCurrency\": \"INR\",\r\n"
        		+ "  \"GuestNationality\": \"IN\",\r\n"
        		+ "  \"NoOfRooms\": \"1\",\r\n"
        		+ "  \"RoomGuests\": [\r\n"
        		+ "    {\r\n"
        		+ "      \"NoOfAdults\": 1,\r\n"
        		+ "      \"NoOfChild\": 0,\r\n"
        		+ "      \"ChildAge\": null\r\n"
        		+ "    }\r\n"
        		+ "  ],\r\n"
        		+ "  \"MaxRating\": 5,\r\n"
        		+ "  \"MinRating\": 0,\r\n"
        		+ "  \"ReviewScore\": null,\r\n"
        		+ "  \"IsNearBySearchAllowed\": false,\r\n"
        		+ "  \"EndUserIp\": \"89.116.231.35\",\r\n"
        		+ "  \"TokenId\": \"dd3d4c6e-21eb-43f4-aba5-e251563414cd\"\r\n"
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
