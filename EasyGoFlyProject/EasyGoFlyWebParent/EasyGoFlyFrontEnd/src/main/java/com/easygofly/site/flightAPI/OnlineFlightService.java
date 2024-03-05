package com.easygofly.site.flightAPI;

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
public class OnlineFlightService {
	@Autowired private LogService logService;
	
	public String tokenId = "";
	public String traceId = "";
	public String resultIndex = "";
	public String airlineRemark = "";
	public String tokenAirIQ = "";

	public int apiAuthentication(HttpURLConnection connection, StringBuilder responseBody)
			throws IOException {
		
        // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
        
        
        // Enable writing data to the connection
        connection.setDoOutput(true);

        // Create the request body
        String requestBody = "{"
        		+ "\"ClientId\": \"tboprod\", "
        		+ "\"UserName\": \"CCUA927\", "
        		+ "\"Password\": \"#API@Air&72\", "
        		+ "\"EndUserIp\": \"89.116.231.35\""
        		+ "}";
        
        //Test Credentials
//      String requestBody = "{"
//      		+ "\"ClientId\": \"ApiIntegrationNew\", "
//      		+ "\"UserName\": \"aladdin\", "
//      		+ "\"Password\": \"aladdin@1234\", "
//      		+ "\"EndUserIp\": \"89.116.231.35\""
//      		+ "}";
        
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
	
	public int apiOnlineSearchMod(HttpURLConnection connection, StringBuilder responseBody, String cityOne, String cityTwo, Integer adultNum, Integer childNum, Integer infantNum, Date date)
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
        String requestBody = "{"
        		+ "\"EndUserIp\": \"89.116.231.35\", "
        		+ "\"TokenId\": \"" + tokenId + "\", "
        		+ "\"AdultCount\": \"" + adultNum + "\", "
        		+ "\"ChildCount\": \"" + childNum + "\", "
        		+ "\"InfantCount\": \"" + infantNum + "\", "
        		+ "\"DirectFlight\": \"false\", "
        		+ "\"OneStopFlight\": \"false\", "
        		+ "\"JourneyType\": \"1\", "
        		+ "\"PreferredAirlines\": null, "
        		+ "\"Segments\": [{"
        			+ "\"Origin\": \"" + cityOne + "\", "
        			+ "\"Destination\": \"" + cityTwo + "\", "
        			+ "\"FlightCabinClass\": \"1\", "
        			+ "\"PreferredDepartureTime\": \"" + strDate + "T00: 00: 00\", "
        			+ "\"PreferredArrivalTime\": \"" + strDate + "T00: 00: 00\""
        			+ "}],"
        		+ "\"Sources\": null"
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
	
	public int apiOnlineSearchModReturn(HttpURLConnection connection, StringBuilder responseBody, String cityOne, String cityTwo, Integer adultNum, Integer childNum, Integer infantNum, Date date, Date returnDate)
			throws IOException {
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); 
		String strDate = dateFormat.format(date);
		String strReturnDate = dateFormat.format(returnDate);
		
        // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
        
        
        // Enable writing data to the connection
        connection.setDoOutput(true);
        
     // Create the request body
        String requestBody = "{"
        		+ "\"EndUserIp\": \"89.116.231.35\", "
        		+ "\"TokenId\": \"" + tokenId + "\", "
        		+ "\"AdultCount\": \"" + adultNum + "\", "
        		+ "\"ChildCount\": \"" + childNum + "\", "
        		+ "\"InfantCount\": \"" + infantNum + "\", "
        		+ "\"DirectFlight\": \"false\", "
        		+ "\"OneStopFlight\": \"false\", "
        		+ "\"JourneyType\": \"2\", "
        		+ "\"PreferredAirlines\": null, "
        		+ "\"Segments\": [{"
        			+ "\"Origin\": \"" + cityOne + "\", "
        			+ "\"Destination\": \"" + cityTwo + "\", "
        			+ "\"FlightCabinClass\": \"1\", "
        			+ "\"PreferredDepartureTime\": \"" + strDate + "T00: 00: 00\", "
        			+ "\"PreferredArrivalTime\": \"" + strDate + "T00: 00: 00\""
        			+ "},"
        			+ "{"
        			+ "\"Origin\": \"" + cityTwo + "\","
        			+ "\"Destination\": \"" + cityOne + "\",\r\n"
        			+ "        \"FlightCabinClass\": \"1\","
        			+ "\"PreferredDepartureTime\": \"" + strReturnDate + "T00: 00: 00\","
        			+ "\"PreferredArrivalTime\": \"" + strReturnDate + "T00: 00: 00\""
        			+ "}],"
        		+ "\"Sources\": null"
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
	
	public int apiOnlineFarerule_quote(HttpURLConnection connection, StringBuilder responseBody, String traceId, String resultIndex)
			throws IOException {
		
        // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
        
        
        // Enable writing data to the connection
        connection.setDoOutput(true);
        
     // Create the request body
        String requestBody = "{"
        		+ "\"EndUserIp\": \"89.116.231.35\", "
        		+ "\"TokenId\": \"" + tokenId + "\", "
        		+ "\"TraceId\": \"" + traceId + "\", "
        		+ "\"ResultIndex\": \"" + resultIndex + "\""
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
	
	public int apiOnlineTicket(HttpURLConnection connection, StringBuilder responseBody, String traceId, String resultIndex, String arrayTraveler)
			throws IOException {
		
        // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
        
        
        // Enable writing data to the connection
        connection.setDoOutput(true);
        
        // Create the request body
        String requestBody = "{\r\n"
        		+ "	\"PreferredCurrency\": null,\r\n"
        		+ "	\"ResultIndex\": \"" + resultIndex + "\",\r\n"
        		+ "	\"AgentReferenceNo\": \"sonam1234567890\",\r\n"
        		+ "	\"Passengers\": " + arrayTraveler + "	,\r\n"
        		+ "	\"EndUserIp\": \"89.116.231.35\",\r\n"
        		+ "	\"TokenId\": \"" + tokenId + "\",\r\n"
        		+ "	\"TraceId\": \"" + traceId + "\"\r\n"
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

	public int apiOnlineBookingNonLCC(HttpURLConnection connection, StringBuilder responseBody, String traceId, String resultIndex, String arrayTraveler)
			throws IOException {
        // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
        
        
        // Enable writing data to the connection
        connection.setDoOutput(true);
        
        // Create the request body
        String requestBody = "{\r\n"
        		+ "	\"ResultIndex\": \"" + resultIndex + "\",\r\n"
        		+ "	\"Passengers\": " + arrayTraveler + "	,\r\n"
        		+ "	\"EndUserIp\": \"89.116.231.35\",\r\n"
        		+ "	\"TokenId\": \"" + tokenId + "\",\r\n"
        		+ "	\"TraceId\": \"" + traceId + "\"\r\n"
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
	
	public int apiOnlineTicketNonLcc(HttpURLConnection connection, StringBuilder responseBody, String traceId, String pnr, Integer bookingId) throws IOException {
		// Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
        
        
        // Enable writing data to the connection
        connection.setDoOutput(true);
        
        // Create the request body
        String requestBody = "{\r\n"
        		+ "	\"EndUserIp\": \"89.116.231.35\",\r\n"
        		+ "	\"TokenId\": \"" + tokenId + "\",\r\n"
        		+ "	\"TraceId\": \"" + traceId + "\",\r\n"
                + "	\"PNR\": \"" + pnr + "\",\r\n"
                + "	\"BookingId\": " + bookingId + "\r\n"
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
	
	public int apiOnlineTicketNonLccPassport(HttpURLConnection connection, StringBuilder responseBody, String traceId, String pnr, Integer bookingId, String arrayPassportList) throws IOException {
		// Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
        
        
        // Enable writing data to the connection
        connection.setDoOutput(true);
        
        // Create the request body
        String requestBody = "{\r\n"
        		+ "	\"EndUserIp\": \"89.116.231.35\",\r\n"
        		+ "	\"TokenId\": \"" + tokenId + "\",\r\n"
        		+ "	\"TraceId\": \"" + traceId + "\",\r\n"
                + "\"Passport\": " + arrayPassportList + ",\r\n"
                + "	\"PNR\": \"" + pnr + "\",\r\n"
                + "	\"BookingId\": " + bookingId + "\r\n"
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
	
	public int apiOnlineSSR(HttpURLConnection connection, StringBuilder responseBody, String traceId, String resultIndex, String arrayTraveler)
			throws IOException {
		
        // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
        
        
        // Enable writing data to the connection
        connection.setDoOutput(true);
        
        // Create the request body
        String requestBody = "{"
        		+ "\"EndUserIp\": \"89.116.231.35\", "
        		+ "\"TokenId\": \"" + tokenId + "\", "
        		+ "\"TraceId\": \"" + traceId + "\", "
        		+ "\"ResultIndex\": \"" + resultIndex + "\""
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
	
	public int apiOnlineGetBookingDetails(HttpURLConnection connection, StringBuilder responseBody, String traceId, String pnr, String bookingId)
			throws IOException {
		
        // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
        
        
        // Enable writing data to the connection
        connection.setDoOutput(true);
        
        // Create the request body
        String requestBody = "{\r\n"
        		+ "	\"EndUserIp\": \"89.116.231.35\",\r\n"
        		+ "	\"TokenId\": \"" + tokenId + "\",\r\n"
        		+ "	\"PNR\": \"" + pnr + "\",\r\n"
        		+ "	\"BookingId\": \"" + bookingId + "\"\r\n"
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


	public int apiAirIQAuthentication(HttpURLConnection connection, StringBuilder responseBody) throws IOException {
		  // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
        
        connection.setRequestProperty("api-key", "NTMzNDUwMDpBSVJJUSBURVNUIEFQSToxODkxOTMwMDM1OTk2OlFRYjhLVjNFMW9UV05RY1NWL0Vtcm9UYXFKTSs5dkZvaHo0RzM4WWhwTDhsamNqR3pPN1dJSHhVQ2pCSzNRcW0=");
        
        // Enable writing data to the connection
        connection.setDoOutput(true);

        // Create the request body
//        String requestBody = "{"
//        		+ "\"ClientId\": \"tboprod\", "
//        		+ "\"UserName\": \"CCUA927\", "
//        		+ "\"Password\": \"#API@Air&72\", "
//        		+ "\"EndUserIp\": \"89.116.231.35\""
//        		+ "}";
        

      String requestBody = "{"
      		+ "\"Username\": \"9555202202\", "
      		+ "\"Password\": \"9800830000@testapi\", "
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

	public int apiAirIQSearch(HttpURLConnection connection, StringBuilder responseBody, String auth, String cityOne, String cityTwo, Integer adultNum, Integer childNum, Integer infantNum, Date date) throws IOException {
		  // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
        
        connection.setRequestProperty("api-key", "NTMzNDUwMDpBSVJJUSBURVNUIEFQSToxODkxOTMwMDM1OTk2OlFRYjhLVjNFMW9UV05RY1NWL0Vtcm9UYXFKTSs5dkZvaHo0RzM4WWhwTDhsamNqR3pPN1dJSHhVQ2pCSzNRcW0=");
        
        connection.setRequestProperty("Authorization", auth);
        
        // Enable writing data to the connection
        connection.setDoOutput(true);
        
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd"); 
		String strDate = dateFormat.format(date);

        String requestBody = "{"
      		+ "\"origin\":\"" + cityOne + "\","
      		+ "\"destination\":\"" + cityTwo + "\","
      		+ "\"departure_date\":\"" + strDate + "\","
      		+ "\"adult\":\"" + adultNum + "\","
      		+ "\"child\":\"" + childNum + "\","
      		+ "\"infant\":\"" + infantNum + "\","
      		+ "\"airline_code\":\"\""
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
