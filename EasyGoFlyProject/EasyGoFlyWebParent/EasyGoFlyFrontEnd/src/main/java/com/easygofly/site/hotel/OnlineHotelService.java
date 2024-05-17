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
	

	public int apiAuthenticationHotel(HttpURLConnection connection, StringBuilder responseBody)
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
	 
	public int apiOnlineSearchHotel(HttpURLConnection connection, StringBuilder responseBody, String cityId, String noOfNights, String noOfRooms, String countryId, Date date, String roomGuests)
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
        		+ "  \"NoOfNights\": \"" + noOfNights + "\",\r\n"
        		+ "  \"CountryCode\": \"" + countryId + "\",\r\n"
        		+ "  \"CityId\": \"" + cityId + "\",\r\n"
        		+ "  \"ResultCount\": null,\r\n"
        		+ "  \"IsTBOMapped\": \"true\",\r\n"
        		+ "  \"PreferredCurrency\": \"INR\",\r\n"
        		+ "  \"GuestNationality\": \"IN\",\r\n"
        		+ "  \"NoOfRooms\": \"" + noOfRooms + "\",\r\n"
        		+ "  \"RoomGuests\": " + roomGuests + ",\r\n"
        		+ "  \"MaxRating\": 5,\r\n"
        		+ "  \"MinRating\": 0,\r\n"
        		+ "  \"ReviewScore\": null,\r\n"
        		+ "  \"IsNearBySearchAllowed\": false,\r\n"
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

	public int apiOnlineHotelInfo(HttpURLConnection connection, StringBuilder responseBody, String resultIndex, String hotelCode, String categoryId)
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
        		+ "  \"TraceId\": \"" + traceId + "\",\r\n"
                + "  \"CategoryId\": \"" + categoryId + "\"\r\n"
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
