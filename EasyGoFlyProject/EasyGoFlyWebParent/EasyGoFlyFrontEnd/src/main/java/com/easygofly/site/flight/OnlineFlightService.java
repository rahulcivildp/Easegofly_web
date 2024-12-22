package com.easygofly.site.flight;

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

import com.easygofly.site.LogService;
import com.easygofly.site.setting.APIServiceSettingBag;
import com.easygofly.site.setting.APITokenSettingBag;
import com.easygofly.site.setting.SettingService;

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
	
	public StringBuilder apiOnlineSearchMod(String cityOne, String cityTwo, Integer adultNum, Integer childNum, Integer infantNum, Date date)
			throws IOException {

        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
		APITokenSettingBag apiTokenSettingBag = settingService.getAPITokenSettings();
		
		// Create URL object with the API end-point
        URL url = new URL(apiServiceSettingBag.getDefaultURL() + "/AirService.svc/rest/Search");

        // Open a connection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        StringBuilder responseBody = new StringBuilder();
        
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
	
	public StringBuilder apiOnlineSearchModReturn(String cityOne, String cityTwo, Integer adultNum, Integer childNum, Integer infantNum, Date date, Date returnDate)
			throws IOException {

        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
		APITokenSettingBag apiTokenSettingBag = settingService.getAPITokenSettings();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); 
		String strDate = dateFormat.format(date);
		String strReturnDate = dateFormat.format(returnDate);
		
		// Create URL object with the API end-point
        URL url = new URL(apiServiceSettingBag.getDefaultURL() + "/AirService.svc/rest/Search");

        // Open a connection
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
	
	public StringBuilder apiOnlineFareruleQuoteSSR(String traceId, String resultIndex, String lastUrl)
			throws IOException {

        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
		APITokenSettingBag apiTokenSettingBag = settingService.getAPITokenSettings();
    	
		// Create URL object with the API end-point
        URL url = new URL(apiServiceSettingBag.getDefaultURL() + lastUrl);

        // Open a connection
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
	
	public StringBuilder apiOnlineTicket(String traceId, String resultIndex, String arrayTraveler)
			throws IOException {

        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
		APITokenSettingBag apiTokenSettingBag = settingService.getAPITokenSettings();
		
		// Create URL object with the API end-point
        URL url = new URL(apiServiceSettingBag.getDefaultURL() + "/AirService.svc/rest/Ticket");

        // Open a connection
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

	public StringBuilder apiOnlineBookingNonLCC(String traceId, String resultIndex, String arrayTraveler)
			throws IOException {
        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
		APITokenSettingBag apiTokenSettingBag = settingService.getAPITokenSettings();
		
		// Create URL object with the API end-point
        URL url = new URL(apiServiceSettingBag.getDefaultURL() + "/AirService.svc/rest/Book");

        // Open a connection
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
	
	public StringBuilder apiOnlineTicketNonLcc(String traceId, String pnr, Integer bookingId) throws IOException {
        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
		APITokenSettingBag apiTokenSettingBag = settingService.getAPITokenSettings();
		
		// Create URL object with the API end-point
        URL url = new URL(apiServiceSettingBag.getDefaultURL() + "/AirService.svc/rest/Ticket");

        // Open a connection
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
	
	public StringBuilder apiOnlineTicketNonLccPassport(String traceId, String pnr, Integer bookingId, String arrayPassportList) throws IOException {
        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
		APITokenSettingBag apiTokenSettingBag = settingService.getAPITokenSettings();
		
		// Create URL object with the API end-point
        URL url = new URL(apiServiceSettingBag.getDefaultURL() + "/AirService.svc/rest/Ticket");

        // Open a connection
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
	
	public StringBuilder apiOnlineGetBookingDetails(String traceId, String pnr, String bookingId)
			throws IOException {
        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
		APITokenSettingBag apiTokenSettingBag = settingService.getAPITokenSettings();
		
		// Create URL object with the API end-point
        URL url = new URL(apiServiceSettingBag.getDefaultURL() + "/AirService.svc/rest/GetBookingDetails");

        // Open a connection
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
}
