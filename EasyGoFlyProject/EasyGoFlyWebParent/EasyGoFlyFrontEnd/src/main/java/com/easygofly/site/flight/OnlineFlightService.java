package com.easygofly.site.flight;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.HttpURLConnection;
import java.net.URI;
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

	//TBO //
	
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
	
	public StringBuilder apiOnlineCalendarFare(String cityOne, String cityTwo, Date date)
			throws IOException {
        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
		APITokenSettingBag apiTokenSettingBag = settingService.getAPITokenSettings();
		
		// Create URL object with the API end-point
        URL urlSearch = new URL(apiServiceSettingBag.getDefaultURL() + "/AirService.svc/rest/GetCalendarFare");

        HttpURLConnection connection = (HttpURLConnection) urlSearch.openConnection();
        
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
        		+ "\"JourneyType\": \"1\", "
        		+ "\"PreferredAirlines\": null, "
        		+ "\"AdultCount\": \"1\","
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

        logService.generateLog(responseBody.toString());
        System.out.println("Auth: " + responseBody.toString());
		return responseBody;
	}
	
	public StringBuilder apiOnlineSearchMod(String cityOne, String cityTwo, Integer adultNum, Integer childNum, Integer infantNum, Date date, String jouInteger)
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
        			+ "\"FlightCabinClass\": \"" + jouInteger + "\", "
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

	
	//Air IQ

	public StringBuilder apiAirIQAuthentication() throws IOException, Exception {
        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
		
	
		// Create an HttpClient instance
        HttpClient client = HttpClient.newHttpClient();

        StringBuilder responseBody = new StringBuilder();
        
        // Define the request body
		String requestBody = "{"
	      		+ "\"Username\": \"" + apiServiceSettingBag.getAirIqUsername() + "\", "
	      		+ "\"Password\": \"" + apiServiceSettingBag.getAirIqPassword() + "\""
	      		+ "}";

		System.out.println(requestBody);
		logService.generateLog(requestBody);
		
        // Create an HttpRequest instance for POST
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://omairiq.azurewebsites.net/login"))
                .header("Content-Type", "application/json")
                .header("api-key", apiServiceSettingBag.getAirIqApiKey())
                .POST(BodyPublishers.ofString(requestBody))
                .build();

        // Send the request and handle the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse the JSON response
	    responseBody.append(response.body());
		
		return responseBody;
	}

	public StringBuilder apiAirIQSearch(String cityOne, String cityTwo, Integer adultNum, Integer childNum, Integer infantNum, Date date) throws IOException, IllegalArgumentException, Exception {

        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
		APITokenSettingBag tokenSettingBag = settingService.getAPITokenSettings();
		
		// Create an HttpClient instance
        HttpClient client = HttpClient.newHttpClient();

        StringBuilder responseBody = new StringBuilder();
        
        // Define the request body
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

        // Create an HttpRequest instance for POST
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://omairiq.azurewebsites.net/search/"))
                .header("Content-Type", "application/json")
                .header("api-key", apiServiceSettingBag.getAirIqApiKey())
                .header("Authorization", tokenSettingBag.getAirIQTokenNo())
                .POST(BodyPublishers.ofString(requestBody))
                .build();

		
        // Send the request and handle the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse the JSON response
	    responseBody.append(response.body());
        
		return responseBody;
	}

	public StringBuilder apiAirIQTicket(String requestBody)
			throws IOException, Exception {	
        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
		APITokenSettingBag tokenSettingBag = settingService.getAPITokenSettings();
		
		// Create an HttpClient instance
        HttpClient client = HttpClient.newHttpClient();

        StringBuilder responseBody = new StringBuilder();

        // Create an HttpRequest instance for POST
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://omairiq.azurewebsites.net/book"))
                .header("Content-Type", "application/json")
                .header("api-key", apiServiceSettingBag.getAirIqApiKey())
                .header("Authorization", tokenSettingBag.getAirIQTokenNo())
                .POST(BodyPublishers.ofString(requestBody))
                .build();

		System.out.println(requestBody);
		logService.generateLog(requestBody);
		
        // Send the request and handle the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse the JSON response
	    responseBody.append(response.body());
		
		return responseBody;
	}

	public StringBuilder apiAirIQTicketDetails(String booking_id)
			throws IOException, Exception {
        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
		APITokenSettingBag tokenSettingBag = settingService.getAPITokenSettings();

		// Create an HttpClient instance
        HttpClient client = HttpClient.newHttpClient();

        StringBuilder responseBody = new StringBuilder();

        // Create an HttpRequest instance for POST
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://omairiq.azurewebsites.net/ticket?booking_id=" + booking_id))
                .header("Content-Type", "application/json")
                .header("api-key", apiServiceSettingBag.getAirIqApiKey())
                .header("Authorization", tokenSettingBag.getAirIQTokenNo())
                .GET()
                .build();
		
        // Send the request and handle the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse the JSON response
	    responseBody.append(response.body());
	    
		return responseBody;
	}

	
	//Master Travels
	
	public StringBuilder apiMasterSearch(String cityOne, String cityTwo, Integer adultNum, Integer childNum, Integer infantNum, Date date)
			throws IOException, Exception {
        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();

		// Create an HttpClient instance
        HttpClient client = HttpClient.newHttpClient();

        StringBuilder responseBody = new StringBuilder();
        
        // Define the request body
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); 
		String strDate = dateFormat.format(date);

        String requestBody =  "{"
          		+ "\"trip_type\": 0,"
          		+ "\"end_user_ip\": \"" + apiServiceSettingBag.getMTravelsUserIp() + "\","
          		+ "\"token\": \"" + apiServiceSettingBag.getMTravelsToken() + "\","
          		+ "\"dep_city_code\": \"" + cityOne + "\","
          		+ "\"arr_city_code\": \"" + cityTwo + "\","
          		+ "\"onward_date\": \"" + strDate + "\","
                + "\"return_date\": \"\","
          		+ "\"adult\": " + adultNum + ","
          		+ "\"children\": " + childNum + ","
          		+ "\"infant\": " + infantNum + ""
          		+ "}";
        
		System.out.println(requestBody);
		logService.generateLog(requestBody);

        // Create an HttpRequest instance for POST
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://devapi.fareboutique.com/v1/fbapi/search"))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(requestBody))
                .build();
		
        // Send the request and handle the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse the JSON response
	    responseBody.append(response.body());
	    
		return responseBody;
	}

	public StringBuilder apiMasterOnWards(String cityOne, String cityTwo)
			throws IOException, Exception {
        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();

		// Create an HttpClient instance
        HttpClient client = HttpClient.newHttpClient();

        StringBuilder responseBody = new StringBuilder();
        
        // Define the request body
        String requestBody =  "{"
          		+ "\"trip_type\": 0,"
          		+ "\"end_user_ip\": \"" + apiServiceSettingBag.getMTravelsUserIp() + "\","
          		+ "\"token\": \"" + apiServiceSettingBag.getMTravelsToken() + "\","
          		+ "\"dep_city_code\": " + cityOne + ","
          		+ "\"arr_city_code\": " + cityTwo + ""
          		+ "}";
        
		System.out.println(requestBody);
		logService.generateLog(requestBody);

        // Create an HttpRequest instance for POST
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://devapi.fareboutique.com/v1/fbapi/onward_date"))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(requestBody))
                .build();
		
        // Send the request and handle the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse the JSON response
	    responseBody.append(response.body());
	    
		return responseBody;
	}

	public StringBuilder apiMasterFareQuote(Integer resultIndex, String staticV, Integer adultChild, Integer infant, Date date)
			throws IOException, Exception {
        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();

		// Create an HttpClient instance
        HttpClient client = HttpClient.newHttpClient();

        StringBuilder responseBody = new StringBuilder();
        
        // Define the request body
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); 
		String strDate = dateFormat.format(date);

        String requestBody =  "{"
          		+ "\"id\": " + resultIndex + ","
          		+ "\"end_user_ip\": \"" + apiServiceSettingBag.getMTravelsUserIp() + "\","
          		+ "\"token\": \"" + apiServiceSettingBag.getMTravelsToken() + "\","
          		+ "\"adult_children\": " + adultChild + ","
          		+ "\"infant\": " + infant + ","
                + "\"static\": \"" + staticV + "\","
          		+ "\"onward_date\": \"" + strDate + "\""
          		+ "}";
        
		System.out.println(requestBody);
		logService.generateLog(requestBody);

        // Create an HttpRequest instance for POST
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://devapi.fareboutique.com/v1/fbapi/fare_quote"))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(requestBody))
                .build();
		
        // Send the request and handle the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse the JSON response
	    responseBody.append(response.body());
	    
		return responseBody;
	}

	public StringBuilder apiMasterBook(String cityOne, String cityTwo, Integer adultNum, Integer childNum, Integer infantNum, 
			Integer resultIndex, String cEmail, String cPhone, String cName, String bookingToken, String travelerDetails, Integer totalAmount, String staticV, Date date)
			throws IOException, Exception {
        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();

		// Create an HttpClient instance
        HttpClient client = HttpClient.newHttpClient();

        StringBuilder responseBody = new StringBuilder();
        
        // Define the request body
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); 
		String strDate = dateFormat.format(date);
		
		Integer totalSeat = adultNum + childNum + infantNum;

        String requestBody =  "{"
          		+ "\"id\": " + resultIndex + ","
          		+ "\"end_user_ip\": \"" + apiServiceSettingBag.getMTravelsUserIp() + "\","
          		+ "\"token\": \"" + apiServiceSettingBag.getMTravelsToken() + "\","
          		+ "\"onward_date\": \"" + strDate + "\","
                + "\"return_date\": \"\","
          		+ "\"adult\": " + adultNum + ","
          		+ "\"children\": " + childNum + ","
          		+ "\"infant\": " + infantNum + ","
          		+ "\"dep_city_code\": \"" + cityOne + "\","
          		+ "\"arr_city_code\": \"" + cityTwo + "\","
          		+ "\"total_book_seats\": " + totalSeat + ","
          		+ "\"contact_name\": \"" + cName + "\","
                + "\"contact_email\": \"" + cEmail + "\","
          		+ "\"contact_number\": \"" + cPhone + "\","
          		+ "\"flight_traveller_details\": " + travelerDetails + ","
          		+ "\"total_amount\": " + totalAmount + ","
          		+ "\"static\": \"" + staticV + "\","
                + "\"booking_token_id\": \"" + bookingToken + "\","
                + "\"partner_user_id\": \"0\""
          		+ "}";
        
		System.out.println(requestBody);
		logService.generateLog(requestBody);

        // Create an HttpRequest instance for POST
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://devapi.fareboutique.com/v1/fbapi/book"))
                .header("Content-Type", "application/json")
      		    .header("x-api-key", apiServiceSettingBag.getMTravelsAPIKey())
                .POST(BodyPublishers.ofString(requestBody))
                .build();
		
        // Send the request and handle the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse the JSON response
	    responseBody.append(response.body());
	    
		return responseBody;
	}

	public StringBuilder apiMasterBookingDetails(String refNo, String TranId, String staticV)
			throws IOException, Exception {
        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();

		// Create an HttpClient instance
        HttpClient client = HttpClient.newHttpClient();

        StringBuilder responseBody = new StringBuilder();
        
        // Define the request body
        String requestBody =  "{"
          		+ "\"reference_id\": \"" + refNo + "\","
          		+ "\"transaction_id\": \"" + TranId + "\","
                + "\"static\": \"" + staticV + "\","
          		+ "\"end_user_ip\": \"" + apiServiceSettingBag.getMTravelsUserIp() + "\","
          		+ "\"token\": \"" + apiServiceSettingBag.getMTravelsToken() + "\""
          		+ "}";
        
		System.out.println(requestBody);
		logService.generateLog(requestBody);

        // Create an HttpRequest instance for POST
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://devapi.fareboutique.com/v1/fbapi/booking_details"))
                .header("Content-Type", "application/json")
    		    .header("x-api-key", apiServiceSettingBag.getMTravelsAPIKey())
                .POST(BodyPublishers.ofString(requestBody))
                .build();
		
        // Send the request and handle the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse the JSON response
	    responseBody.append(response.body());
	    
		return responseBody;
	}
	
	
	
	
	//Ease2fly

	public StringBuilder apiEase2flyAuthentication() throws IOException, Exception {
        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
		
	
		// Create an HttpClient instance
        HttpClient client = HttpClient.newHttpClient();

        StringBuilder responseBody = new StringBuilder();
        
        // Define the request body
		String requestBody = "{"
	      		+ "\"email\": \"" + apiServiceSettingBag.getEase2flyUser() + "\", "
	      		+ "\"pwd\": \"" + apiServiceSettingBag.getEase2flyPassword() + "\", "
	    	    + "\"efly_api_key\": \"" + apiServiceSettingBag.getEase2flyAPIKey() + "\""
	      		+ "}";

		System.out.println(requestBody);
		logService.generateLog(requestBody);
		
        // Create an HttpRequest instance for POST
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://eflyapi.ease2fly.com/api/tp-api/login"))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(requestBody))
                .build();

        // Send the request and handle the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse the JSON response
	    responseBody.append(response.body());
		
		return responseBody;
	}

	public StringBuilder apiEase2flySearch(String cityOne, String cityTwo, Integer adultNum, Integer childNum, Integer infantNum, Date date)
			throws IOException, Exception {
        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
		APITokenSettingBag tokenSettingBag = settingService.getAPITokenSettings();

		// Create an HttpClient instance
        HttpClient client = HttpClient.newHttpClient();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); 
		String strDate = dateFormat.format(date);
		
        StringBuilder responseBody = new StringBuilder();
        
        //Create url
        String url = "https://eflyapi.ease2fly.com/api/tp-api/search-flights?origin=" + cityOne + "&destination=" + cityTwo + "&airline=&departuredate=" + strDate + "&adults=" 
        		+ adultNum + "&child=" + childNum + "&infant=" + infantNum;

		System.out.println(url);
		logService.generateLog(url);
        
        // Create an HttpRequest instance for POST
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", tokenSettingBag.getEase2flyTokenNo())
                .header("efly_api_key", apiServiceSettingBag.getEase2flyAPIKey())
                .GET()
                .build();
		
        // Send the request and handle the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse the JSON response
	    responseBody.append(response.body());
	    
		return responseBody;
	}

	public StringBuilder apiEase2flyBookingTicket(String adultInfo, String childInfo, String infantInfo, Integer adultNum, Integer childNum, Integer infantNum, Integer id, Integer fare, 
			String cPhone, String cEmail, String token)
			throws IOException, Exception {
        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();

		// Create an HttpClient instance
        HttpClient client = HttpClient.newHttpClient();

        StringBuilder responseBody = new StringBuilder();
        
        // Define the request body
        String bearerToken = "Bearer " + token;
        
        String requestBody =  "{"
          		+ "\"adult_info\": " + adultInfo + ","
          		+ "\"child_info\": " + childInfo + ","
                + "\"infant_info\": " + infantInfo + ","
          		+ "\"adults\": " + adultNum + ","
          		+ "\"child\": " + childNum + ","
                + "\"infant\": " + infantNum + ","
          		+ "\"sector_id\": " + id + ","
          		+ "\"fare\": " + fare + ","
                + "\"phone\": \"" + cPhone + "\","
          		+ "\"email\": \"" + cEmail + "\""
          		+ "}";
        
		System.out.println(requestBody);
		logService.generateLog(requestBody);

        // Create an HttpRequest instance for POST
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://eflyapi.ease2fly.com/api/tp-api/book-ticket"))
                .header("Content-Type", "application/json")
                .header("Authorization", bearerToken)
                .header("efly_api_key", apiServiceSettingBag.getEase2flyAPIKey())
                .POST(BodyPublishers.ofString(requestBody))
                .build();
		
        // Send the request and handle the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse the JSON response
	    responseBody.append(response.body());
	    
		return responseBody;
	}

	public StringBuilder apiEase2flyShowTicket(String bookingId, String token)
			throws IOException, Exception {
        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();

		// Create an HttpClient instance
        HttpClient client = HttpClient.newHttpClient();

        StringBuilder responseBody = new StringBuilder();
        
        // Define the request body
        String bearerToken = "Bearer " + token;

        //Create url
        String url = "https://eflyapi.ease2fly.com/api/tp-api/book-ticket" + bookingId;

		System.out.println(url);
		logService.generateLog(url);

        // Create an HttpRequest instance for POST
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", bearerToken)
                .header("efly_api_key", apiServiceSettingBag.getEase2flyAPIKey())
                .GET()
                .build();
		
        // Send the request and handle the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse the JSON response
	    responseBody.append(response.body());
	    
		return responseBody;
	}




}
