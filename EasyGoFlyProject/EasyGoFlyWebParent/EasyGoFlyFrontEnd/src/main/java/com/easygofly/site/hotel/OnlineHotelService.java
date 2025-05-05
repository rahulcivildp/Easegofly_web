package com.easygofly.site.hotel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.easygofly.site.LogService;
import com.easygofly.site.setting.APIServiceSettingBag;
import com.easygofly.site.setting.SettingService;

@Service
public class OnlineHotelService {
	@Autowired private LogService logService;
	@Autowired private SettingService settingService;
	
	public String tokenId = "";
	public String traceId = "";
	public String resultIndex = "";

	public StringBuilder apiOnlineCityByCountry()
			throws IOException, InterruptedException {

        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
		

		// Create an HttpClient instance
        HttpClient client = HttpClient.newHttpClient();
        
        // Basic Authentication - Encode username:password
        String username = "TBOStaticAPITest";
        String password = "Tbo@11530818";
        String encodedAuth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        
        StringBuilder responseBody = new StringBuilder();

        // Create the request body
           String requestBody = "{\r\n"
           		+ "  \"CountryCode\": \"IN\"\r\n"
           		+ "}";
           
        // Create an HttpRequest instance for POST
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiServiceSettingBag.getHotelHolidayURL() + "/CityList"))
                .header("Authorization", "Basic " + encodedAuth)
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(requestBody))
                .build();
       

        System.out.println(requestBody);
        logService.generateLog(requestBody);

        // Send the request and handle the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse the JSON response
	    responseBody.append(response.body());
	    
        System.out.println(responseBody.toString());
        logService.generateLog(responseBody.toString());
		return responseBody;
	}
	
	public StringBuilder apiOnlineSearchHotel(String cityId)
			throws IOException, InterruptedException {

        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();

		// Create an HttpClient instance
        HttpClient client = HttpClient.newHttpClient();
        
        // Basic Authentication - Encode username:password
        String username = "TBOStaticAPITest";
        String password = "Tbo@11530818";
        String encodedAuth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        
        StringBuilder responseBody = new StringBuilder();

        // Create the request body
           String requestBody = "{\r\n"
           		+ "  \"CityCode\": \"" + cityId + "\",\r\n"
           		+ "  \"IsDetailedResponse\": \"true\"\r\n"
           		+ "}";
           
        // Create an HttpRequest instance for POST
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiServiceSettingBag.getHotelHolidayURL() + "/TBOHotelCodeList"))
                .header("Authorization", "Basic " + encodedAuth)
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(requestBody))
                .build();
       
  

        System.out.println(requestBody);
        logService.generateLog(requestBody);

        // Send the request and handle the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse the JSON response
	    responseBody.append(response.body());
	    
        System.out.println(responseBody.toString());
        logService.generateLog(responseBody.toString());
		return responseBody;
	}

	public StringBuilder apiOnlineSearchHotelInfos(Date checkInDate, Date checkOutDate, String hotelCodesresult,
			String arrayRoomGuest, Integer noOfroom)
			throws IOException, InterruptedException {
		SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
		
        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();

		// Create an HttpClient instance
        HttpClient client = HttpClient.newHttpClient();
        
        // Basic Authentication - Encode username:password
        String username = apiServiceSettingBag.getUsername();
        String password = apiServiceSettingBag.getPassword();
        String encodedAuth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        
        StringBuilder responseBody = new StringBuilder();

        // Create the request body
           String requestBody = "{\r\n"
           		+ "	\"CheckIn\": \"" + outputFormat.format(checkInDate) + "\",\r\n"
           		+ "	\"CheckOut\": \"" + outputFormat.format(checkOutDate) + "\",\r\n"
           		+ "	\"HotelCodes\": \"" + hotelCodesresult + "\",\r\n"
           		+ "	\"GuestNationality\": \"IN\",\r\n"
           		+ "	\"PaxRooms\": " + arrayRoomGuest + ",\r\n"
           		+ "	\"ResponseTime\": 23.0,\r\n"
           		+ "	\"IsDetailedResponse\": true,\r\n"
           		+ "	\"Filters\": {\r\n"
           		+ "		\"Refundable\": false,\r\n"
           		+ "		\"NoOfRooms\": " + noOfroom + ",\r\n"
           		+ "		\"MealType\": 0,\r\n"
           		+ "		\"OrderBy\": 0,\r\n"
           		+ "		\"StarRating\": 0,\r\n"
           		+ "		\"HotelName\": null\r\n"
           		+ "	}\r\n"
           		+ "}";
           
        // Create an HttpRequest instance for POST
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiServiceSettingBag.getHotelURL() + "/Search"))
                .header("Authorization", "Basic " + encodedAuth)
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(requestBody))
                .build();
       
        System.out.println(requestBody);
        logService.generateLog(requestBody);

        // Send the request and handle the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse the JSON response
	    responseBody.append(response.body());
	    
        System.out.println(responseBody.toString());
        logService.generateLog(responseBody.toString());
		return responseBody;
	}
	
	public StringBuilder apiOnlineHotelPreBook(String bookingCode)
			throws IOException, InterruptedException {
		
        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();

		// Create an HttpClient instance
        HttpClient client = HttpClient.newHttpClient();
        
        // Basic Authentication - Encode username:password
        String username = apiServiceSettingBag.getUsername();
        String password = apiServiceSettingBag.getPassword();
        String encodedAuth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        
        StringBuilder responseBody = new StringBuilder();

        // Create the request body
           String requestBody = "{\r\n"
           		+ "	\"BookingCode\": \"" + bookingCode + "\",\r\n"
           		+ "	\"PaymentMode\": \"Limit\"\r\n"
           		+ "}";
           
        // Create an HttpRequest instance for POST
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiServiceSettingBag.getHotelURL() + "/Search"))
                .header("Authorization", "Basic " + encodedAuth)
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(requestBody))
                .build();
       
        System.out.println(requestBody);
        logService.generateLog(requestBody);

        // Send the request and handle the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse the JSON response
	    responseBody.append(response.body());
	    
        System.out.println(responseBody.toString());
        logService.generateLog(responseBody.toString());
		return responseBody;
	}

	public int apiOnlineHotelRoom(HttpURLConnection connection, StringBuilder responseBody, String resultIndex, String hotelCode)
			throws IOException {
		
        // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
  
        // Enable writing data to the connection
        connection.setDoOutput(true);
        
     // Create the request body
        String requestBody = "{\r\n"
        		+ "  \"ResultIndex\": \"" + resultIndex + "\",\r\n"
        		+ "  \"HotelCode\": \"" + hotelCode + "\",\r\n"
        		+ "  \"EndUserIp\": \"89.116.231.35\",\r\n"
        		+ "  \"TokenId\": \"" + tokenId + "\",\r\n"
        		+ "  \"TraceId\": \"" + traceId + "\"\r\n"
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

	public int apiOnlineHotelBlockRoom(HttpURLConnection connection, StringBuilder responseBody, String resultIndex, String hotelCode, String hotelName, String noOfRooms, 
			String arrayRoom, String categoryId)
			throws IOException {
		
        // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
  
        // Enable writing data to the connection
        connection.setDoOutput(true);
        
        
     // Create the request body
        String requestBody = "{\r\n"
        		+ "  \"ResultIndex\": \"" + resultIndex + "\",\r\n"
        		+ "  \"HotelCode\": \""+ hotelCode +"\",\r\n"
        		+ "  \"HotelName\": \"" + hotelName + "\",\r\n"
        		+ "  \"GuestNationality\": \"IN\",\r\n"
        		+ "  \"NoOfRooms\": \"" + noOfRooms + "\",\r\n"
        		+ "  \"ClientReferenceNo\": \"0\",\r\n"
        		+ "  \"IsVoucherBooking\": \"true\",\r\n"
        		+ "  \"CategoryId\": \"" + categoryId + "\",\r\n"
        		+ "  \"HotelRoomsDetails\": " + arrayRoom + "  ,\r\n"
        		+ "  \"EndUserIp\": \"89.116.231.35\",\r\n"
        		+ "  \"TokenId\": \"" + tokenId + "\",\r\n"
        		+ "  \"TraceId\": \"" + traceId + "\"\r\n"
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

	public int apiOnlineHotelBook(HttpURLConnection connection, StringBuilder responseBody, String resultIndex, String hotelCode, String hotelName, String noOfRooms, 
			String arrayRoom, String categoryId)
			throws IOException {
		
        // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
  
        // Enable writing data to the connection
        connection.setDoOutput(true);
        
        
     // Create the request body
        String requestBody = "{\r\n"
        		+ "  \"ResultIndex\": \"" + resultIndex + "\",\r\n"
        		+ "  \"HotelCode\": \""+ hotelCode +"\",\r\n"
        		+ "  \"HotelName\": \"" + hotelName + "\",\r\n"
        		+ "  \"GuestNationality\": \"IN\",\r\n"
        		+ "  \"NoOfRooms\": \"" + noOfRooms + "\",\r\n"
        		+ "  \"ClientReferenceNo\": \"0\",\r\n"
        		+ "  \"IsVoucherBooking\": \"true\",\r\n"
        		+ "  \"CategoryId\": \"" + categoryId + "\",\r\n"
        		+ "  \"HotelRoomsDetails\": " + arrayRoom + "  ,\r\n"
        		+ "  \"EndUserIp\": \"89.116.231.35\",\r\n"
        		+ "  \"TokenId\": \"" + tokenId + "\",\r\n"
        		+ "  \"TraceId\": \"" + traceId + "\"\r\n"
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

	public int apiOnlineHotelGetBookingDetails(HttpURLConnection connection, StringBuilder responseBody, String bookingId)
			throws IOException {
		
        // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
  
        // Enable writing data to the connection
        connection.setDoOutput(true);
        
        
     // Create the request body
        String requestBody = "{\r\n"
        		+ "  \"BookingId\": \"" + bookingId + "\",\r\n"
        		+ "  \"EndUserIp\": \"89.116.231.35\",\r\n"
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

	public int apiSendChangeRequestHotel(HttpURLConnection connection, StringBuilder responseBody, Integer bookingId, String tokenId)
			throws IOException {
		
        // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
        
        
        // Enable writing data to the connection
        connection.setDoOutput(true);
        
        // Request body
        String requestBody = "{\r\n"
      		+ "  “BookingMode”: 5,\r\n"
      		+ "  \"RequestType\": 4,\r\n"
      		+ "  \"Remarks\": \"sds\",\r\n"
      		+ "  \"BookingId\": " + bookingId + ",\r\n"
      		+ "  \"EndUserIp\": \"89.116.231.35\",\r\n"
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

	public int apiChangeRequestStatusHotel(HttpURLConnection connection, StringBuilder responseBody, Integer changeId, String tokenId)
			throws IOException {
		
        // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
        
        
        // Enable writing data to the connection
        connection.setDoOutput(true);
        
        // Request body
        String requestBody = "{\r\n"
        		+ "  “BookingMode”: 5,\r\n"
        		+ "  \"ChangeRequestId\": " + changeId + ",\r\n"
        		+ "  \"EndUserIp\": \"89.116.231.35\",\r\n"
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
