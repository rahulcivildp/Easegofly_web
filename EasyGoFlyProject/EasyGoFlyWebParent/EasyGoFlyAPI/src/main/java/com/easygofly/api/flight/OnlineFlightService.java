package com.easygofly.api.flight;
 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easygofly.api.LogService;
import com.easygofly.api.setting.APIServiceSettingBag;
import com.easygofly.api.setting.APITokenSettingBag;
import com.easygofly.api.setting.SettingService;

@Service
public class OnlineFlightService {
	@Autowired private LogService logService;
	@Autowired private SettingService settingService;
	
	public String traceId = "";
	public String resultIndex = "";
	public String airlineRemark = "";
	public String tokenAirIQ = "";
	
	
	public StringBuilder apiAuthentication()
			throws IOException {

        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
        
    	// Create URL object with the API end-point
        URL url = new URL(apiServiceSettingBag.getAuthURL());
		
        // Open a connection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        StringBuilder responseBody = new StringBuilder();
        
        // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
        
        
        // Enable writing data to the connection
        connection.setDoOutput(true);
        ; 
        // Create the request body
        String requestBody = "{"
        		+ "\"ClientId\": \"" + apiServiceSettingBag.getClientId() + "\", "
        		+ "\"UserName\": \"" + apiServiceSettingBag.getUsername() + "\", "
        		+ "\"Password\": \"" + apiServiceSettingBag.getPassword() + "\", "
        		+ "\"EndUserIp\": \"" + apiServiceSettingBag.getUserIP() + "\""
        		+ "}";
        
        //Test Credentials
//      String requestBody = "{"
//      		+ "\"ClientId\": \"ApiIntegrationNew\", "
//      		+ "\"UserName\": \"aladdin\", "
//      		+ "\"Password\": \"Aladdin@1234\", "
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
		connection.getResponseCode();

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
	
	public StringBuilder apiOnlineCalendarFare(String cityOne, String cityTwo, Date date)
			throws IOException {

        URL urlSearch = new URL("https://tboapi.travelboutiqueonline.com/AirAPI_V10/AirService.svc/rest/GetCalendarFare");

        HttpURLConnection connection = (HttpURLConnection) urlSearch.openConnection();
        
		StringBuilder responseBody = new StringBuilder();
        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
		APITokenSettingBag apiTokenSettingBag = settingService.getAPITokenSettings();
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
        		+ "\"EndUserIp\": \"" + apiServiceSettingBag.getUserIP() + "\", "
        		//+ "\"TokenId\": \"" + apiTokenSettingBag.getFlightTokenNo() + "\", "
                + "\"TokenId\": \"" + apiTokenSettingBag.getFlightTokenNo() + "\", "
        		+ "\"JourneyType\": \"1\", "
        		+ "\"PreferredAirlines\": null, "
        		+ "\"Segments\": [{"
        			+ "\"Origin\": \"" + cityOne + "\", "
        			+ "\"Destination\": \"" + cityTwo + "\", "
        			+ "\"FlightCabinClass\": \"1\", "
        			+ "\"PreferredDepartureTime\": \"" + strDate + "T00: 00: 00\""
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
//		int responseCode = connection.getResponseCode();

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
	
	public StringBuilder apiOnlineSearchMod(String cityOne, String cityTwo, Integer adultNum, Integer childNum, Integer infantNum, Date date)
			throws IOException {
		
//        URL url = new URL("http://api.tektravels.com/BookingEngineService_Air/AirService.svc/rest/Search");

        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
		APITokenSettingBag apiTokenSettingBag = settingService.getAPITokenSettings();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); 
		String strDate = dateFormat.format(date);
		
		// Create URL object with the API end-point
        URL url = new URL(apiServiceSettingBag.getDefaultURL() + "/AirService.svc/rest/Search");
        
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
		StringBuilder responseBody = new StringBuilder();
		
        // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
        
        
        // Enable writing data to the connection
        connection.setDoOutput(true);
        
     // Create the request body
        String requestBody = "{"
        		+ "\"EndUserIp\": \"" + apiServiceSettingBag.getUserIP() + "\", "
        		//+ "\"TokenId\": \"" + apiTokenSettingBag.getFlightTokenNo() + "\", "
        		+ "\"TokenId\": \"" + apiTokenSettingBag.getFlightTokenNo() + "\", "
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
//		int responseCode = connection.getResponseCode();

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
	
	public int apiOnlineSearchModReturn(HttpURLConnection connection, StringBuilder responseBody, String cityOne, String cityTwo, Integer adultNum, Integer childNum, Integer infantNum, Date date, Date returnDate)
			throws IOException {

        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
		APITokenSettingBag apiTokenSettingBag = settingService.getAPITokenSettings();
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
        		+ "\"EndUserIp\": \"" + apiServiceSettingBag.getUserIP() + "\", "
        		+ "\"TokenId\": \"" + apiTokenSettingBag.getFlightTokenNo() + "\", "
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
        connection.disconnect();
        
		return responseCode;
	}
	
	public StringBuilder apiOnlineFarerule_quote(URL url, String traceId, String resultIndex)
			throws IOException {

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
		StringBuilder responseBody = new StringBuilder();

        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
		APITokenSettingBag apiTokenSettingBag = settingService.getAPITokenSettings();
        // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
        
        
        // Enable writing data to the connection
        connection.setDoOutput(true);
        
     // Create the request body
        String requestBody = "{"
        		+ "\"EndUserIp\": \"" + apiServiceSettingBag.getUserIP() + "\", "
        		//+ "\"TokenId\": \"" + apiTokenSettingBag.getFlightTokenNo() + "\", "
        		+ "\"TokenId\": \"" + apiTokenSettingBag.getFlightTokenNo() + "\", "
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
//		int responseCode = connection.getResponseCode();

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
	
	public StringBuilder apiOnlineTicket(String traceId, String resultIndex, String arrayTraveler)
			throws IOException {
        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
		APITokenSettingBag apiTokenSettingBag = settingService.getAPITokenSettings();
		
		// Create URL object with the API end-point
        URL url = new URL(apiServiceSettingBag.getDefaultURL() + "/AirService.svc/rest/Ticket");
//		URL url = new URL("http://api.tektravels.com/BookingEngineService_Air/AirService.svc/rest/Ticket");

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
		StringBuilder responseBody = new StringBuilder();
		
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
        		+ "	\"EndUserIp\": \"" + apiServiceSettingBag.getUserIP() + "\",\r\n"
        		//+ "\"TokenId\": \"" + apiTokenSettingBag.getFlightTokenNo() + "\", "
        		+ "	\"TokenId\": \"" + apiTokenSettingBag.getFlightTokenNo() + "\",\r\n"
        		+ "	\"TraceId\": \"" + traceId + "\"\r\n"
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
        
		return responseBody;
	}

	public StringBuilder apiOnlineBookingNonLCC(String traceId, String resultIndex, String arrayTraveler)
			throws IOException {
        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
		APITokenSettingBag apiTokenSettingBag = settingService.getAPITokenSettings();

		// Create URL object with the API end-point
        URL url = new URL(apiServiceSettingBag.getDefaultURL() + "/AirService.svc/rest/Book");
//        URL url = new URL("http://api.tektravels.com/BookingEngineService_Air/AirService.svc/rest/Book");
        
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
		StringBuilder responseBody = new StringBuilder();
        
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
        		+ "	\"EndUserIp\": \"" + apiServiceSettingBag.getUserIP() + "\",\r\n"
        		+ "	\"TokenId\": \"" + apiTokenSettingBag.getFlightTokenNo() + "\",\r\n"
        		+ "	\"TraceId\": \"" + traceId + "\"\r\n"
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
        
		return responseBody;
	}
	
	public StringBuilder apiOnlineTicketNonLcc(String traceId, String pnr, Integer bookingId) throws IOException {
        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
		APITokenSettingBag apiTokenSettingBag = settingService.getAPITokenSettings();

		// Create URL object with the API end-point
        URL url = new URL(apiServiceSettingBag.getDefaultURL() + "/AirService.svc/rest/Ticket");
//		URL url = new URL("http://api.tektravels.com/BookingEngineService_Air/AirService.svc/rest/Ticket");

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
		StringBuilder responseBody = new StringBuilder();
		
		// Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
        
        
        // Enable writing data to the connection
        connection.setDoOutput(true);
        
        // Create the request body
        String requestBody = "{\r\n"
        		+ "	\"EndUserIp\": \"" + apiServiceSettingBag.getUserIP() + "\",\r\n"
        		//+ "\"TokenId\": \"" + apiTokenSettingBag.getFlightTokenNo() + "\", "
        		+ "	\"TokenId\": \"" + apiTokenSettingBag.getFlightTokenNo() + "\",\r\n"
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
	
	public StringBuilder apiOnlineTicketNonLccPassport(String traceId, String pnr, Integer bookingId, String arrayPassportList) throws IOException {
        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
		APITokenSettingBag apiTokenSettingBag = settingService.getAPITokenSettings();

		// Create URL object with the API end-point
        URL url = new URL(apiServiceSettingBag.getDefaultURL() + "/AirService.svc/rest/Ticket");
//		URL url = new URL("http://api.tektravels.com/BookingEngineService_Air/AirService.svc/rest/Ticket");

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
		StringBuilder responseBody = new StringBuilder();
		
		// Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
        
        
        // Enable writing data to the connection
        connection.setDoOutput(true);
        
        // Create the request body
        String requestBody = "{\r\n"
        		+ "	\"EndUserIp\": \"" + apiServiceSettingBag.getUserIP() + "\",\r\n"
        		//+ "\"TokenId\": \"" + apiTokenSettingBag.getFlightTokenNo() + "\", "
        		+ "	\"TokenId\": \"" + apiTokenSettingBag.getFlightTokenNo() + "\",\r\n"
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
	
	public StringBuilder apiOnlineGetBookingDetails(String traceId, String pnr, String bookingId)
			throws IOException {
        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
		APITokenSettingBag apiTokenSettingBag = settingService.getAPITokenSettings();

		// Create URL object with the API end-point
        URL url = new URL(apiServiceSettingBag.getDefaultURL() + "/AirService.svc/rest/GetBookingDetails");
//		URL url = new URL("http://api.tektravels.com/BookingEngineService_Air/AirService.svc/rest/GetBookingDetails");

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
		StringBuilder responseBody = new StringBuilder();
		
		
        // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
        
        
        // Enable writing data to the connection
        connection.setDoOutput(true);
        
        // Create the request body
        String requestBody = "{\r\n"
        		+ "	\"EndUserIp\": \"" + apiServiceSettingBag.getUserIP() + "\",\r\n"
        		//+ "\"TokenId\": \"" + apiTokenSettingBag.getFlightTokenNo() + "\", "
        		+ "	\"TokenId\": \"" + apiTokenSettingBag.getFlightTokenNo() + "\",\r\n"
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

	public int apiAirIQTicket(HttpURLConnection connection, StringBuilder responseBody, String requestBody, String auth)
			throws IOException {
		
        // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
        
        connection.setRequestProperty("api-key", "NTMzNDUwMDpBSVJJUSBURVNUIEFQSToxODkxOTMwMDM1OTk2OlFRYjhLVjNFMW9UV05RY1NWL0Vtcm9UYXFKTSs5dkZvaHo0RzM4WWhwTDhsamNqR3pPN1dJSHhVQ2pCSzNRcW0=");
        
        connection.setRequestProperty("Authorization", auth);
        
        
        // Enable writing data to the connection
        connection.setDoOutput(true);
        
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

	public int apiAirIQTicketDetails(HttpURLConnection connection, StringBuilder responseBody, String auth)
			throws IOException {
		
        // Set the request method to GET
        connection.setRequestMethod("GET");
        
        // Set request headers (if required)
        
        connection.setRequestProperty("api-key", "NTMzNDUwMDpBSVJJUSBURVNUIEFQSToxODkxOTMwMDM1OTk2OlFRYjhLVjNFMW9UV05RY1NWL0Vtcm9UYXFKTSs5dkZvaHo0RzM4WWhwTDhsamNqR3pPN1dJSHhVQ2pCSzNRcW0=");
        
        connection.setRequestProperty("Authorization", auth);

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
