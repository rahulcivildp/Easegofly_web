package com.easygofly.site.bus;

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
public class OnlineBusService {
	@Autowired private LogService logService;
	@Autowired private SettingService settingService;
	
	public String traceId = "";
	public String resultIndex = "";


	public int apiOnlineSearchBus(StringBuilder responseBody, String cityId1, String cityId2, Date date)
			throws IOException {

        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
		APITokenSettingBag apiTokenSettingBag = settingService.getAPITokenSettings();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); 
		String strDate = dateFormat.format(date);

		// Create URL object with the API end-point
//        URL urlSearch = new URL("https://api.travelboutiqueonline.com/BusAPI_V10/BusService.svc/rest/Search/");

        URL url = new URL(apiServiceSettingBag.getBusURL() + "/rest/Search");

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
        		+ "\"DateOfJourney\": \"" + strDate + "\", \r\n"
        		+ "\"DestinationId\": \"" + cityId1 + "\",\r\n"
        		+ "\"EndUserIp\": \"" + apiServiceSettingBag.getUserIP() + "\",\r\n"
        		+ "\"OriginId\": \"" + cityId2 + "\",\r\n"
        		+ "\"TokenId\": \"" + apiTokenSettingBag.getFlightTokenNo() + "\",\r\n"
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
        connection.disconnect();
        
		return responseCode;
	}

	public int apiOnlineBusSeatLayout(StringBuilder responseBody, Integer resultIndex)
			throws IOException {

        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
		APITokenSettingBag apiTokenSettingBag = settingService.getAPITokenSettings();
		// Create URL object with the API end-point
        URL urlBusSeatLayout = new URL("https://api.travelboutiqueonline.com/BusAPI_V10/BusService.svc/rest/GetBusSeatLayOut/");

//        URL urlBusSeatLayout = new URL("http://api.tektravels.com/BookingEngineService_Bus/Busservice.svc/rest/GetBusSeatLayOut");

        // Open a connection
        HttpURLConnection connection = (HttpURLConnection) urlBusSeatLayout.openConnection();
        // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
  
        // Enable writing data to the connection
        connection.setDoOutput(true);
        
     // Create the request body
        String requestBody = "{\r\n"
        		+ "  \"EndUserIp\": \"" + apiServiceSettingBag.getUserIP() + "\",\r\n"
        		+ "  \"ResultIndex\": " + resultIndex + ",\r\n"
        		+ "  \"TraceId\": \"" + traceId + "\",\r\n"
        		+ "  \"TokenId\": \"" + apiTokenSettingBag.getFlightTokenNo() + "\"\r\n"
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

	public int apiOnlineBusBoardingPoint(StringBuilder responseBody, Integer resultIndex)
			throws IOException {

        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
		APITokenSettingBag apiTokenSettingBag = settingService.getAPITokenSettings();
		// Create URL object with the API end-point
        URL urlBusPointDetail = new URL("https://api.travelboutiqueonline.com/BusAPI_V10/BusService.svc/rest/GetBoardingPointDetails/");

//        URL urlBusPointDetail = new URL("http://api.tektravels.com/BookingEngineService_Bus/Busservice.svc/rest/GetBoardingPointDetails");

        // Open a connection
        HttpURLConnection connection = (HttpURLConnection) urlBusPointDetail.openConnection();
        
        // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
  
        // Enable writing data to the connection
        connection.setDoOutput(true);
        
     // Create the request body
        String requestBody = "{\r\n"
        		+ "  \"EndUserIp\": \"" + apiServiceSettingBag.getUserIP() + "\",\r\n"
        		+ "  \"ResultIndex\": " + resultIndex + ",\r\n"
        		+ "  \"TraceId\": \"" + traceId + "\",\r\n"
        		+ "  \"TokenId\": \"" + apiTokenSettingBag.getFlightTokenNo() + "\"\r\n"
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

	public int apiOnlineBusBlock(StringBuilder responseBody, String paxs, Integer resultIndex, Integer boarding, Integer dropping)
			throws IOException {

        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
		APITokenSettingBag apiTokenSettingBag = settingService.getAPITokenSettings();
		// Create URL object with the API end-point
        URL urlBusBlock = new URL("https://api.travelboutiqueonline.com/BusAPI_V10/BusService.svc/rest/Block/");

//        URL urlBusBlock = new URL("http://api.tektravels.com/BookingEngineService_Bus/Busservice.svc/rest/Block/");

        // Open a connection
        HttpURLConnection connection = (HttpURLConnection) urlBusBlock.openConnection();
        
        // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
  
        // Enable writing data to the connection
        connection.setDoOutput(true);
        
     // Create the request body
        String requestBody = "{\r\n"
        		+ "  \"EndUserIp\": \"" + apiServiceSettingBag.getUserIP() + "\",\r\n"
        		+ "  \"ResultIndex\": \"" + resultIndex + "\",\r\n"
        		+ "  \"TraceId\": \"" + traceId + "\",\r\n"
        		+ "  \"TokenId\": \"" + apiTokenSettingBag.getFlightTokenNo() + "\",\r\n"
        		+ "  \"BoardingPointId\": " + boarding + ",\r\n"
        		+ "  \"DroppingPointId\": " + dropping + ","
                + "  \"Passenger\": " + paxs + "\r\n"
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

	public int apiOnlineBusBook(StringBuilder responseBody, String paxs, Integer resultIndex, Integer boarding, Integer dropping)
			throws IOException {

        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
		APITokenSettingBag apiTokenSettingBag = settingService.getAPITokenSettings();
		// Create URL object with the API end-point
        URL urlBusBook = new URL("https://api.travelboutiqueonline.com/BusAPI_V10/BusService.svc/rest/Book/");

//        URL urlBusBook = new URL("http://api.tektravels.com/BookingEngineService_Bus/Busservice.svc/rest/Book/");
        
        // Open a connection
        HttpURLConnection connection = (HttpURLConnection) urlBusBook.openConnection();
        
        // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
  
        // Enable writing data to the connection
        connection.setDoOutput(true);
        
     // Create the request body
        String requestBody = "{\r\n"
        		+ "  \"EndUserIp\": \"" + apiServiceSettingBag.getUserIP() + "\",\r\n"
        		+ "  \"ResultIndex\": \"" + resultIndex + "\",\r\n"
        		+ "  \"TraceId\": \"" + traceId + "\",\r\n"
        		+ "  \"TokenId\": \"" + apiTokenSettingBag.getFlightTokenNo() + "\",\r\n"
        		+ "  \"BoardingPointId\": " + boarding + ",\r\n"
        		+ "  \"DroppingPointId\": " + dropping + ","
                + "  \"Passenger\": " + paxs + "\r\n"
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

	public int apiOnlineBusBookingDetails(StringBuilder responseBody, Integer busId)
			throws IOException {

        // Get API details from Settings.
		APIServiceSettingBag apiServiceSettingBag = settingService.getAPIServiceSettings();
		APITokenSettingBag apiTokenSettingBag = settingService.getAPITokenSettings();
		// Create URL object with the API end-point
        URL urlBusBookingDetails = new URL("https://api.travelboutiqueonline.com/BusAPI_V10/BusService.svc/rest/GetBookingDetail/");
        
//        URL urlBusBookingDetails = new URL("http://api.tektravels.com/BookingEngineService_Bus/Busservice.svc/rest/GetBookingDetail/");

        // Open a connection
        HttpURLConnection connection = (HttpURLConnection) urlBusBookingDetails.openConnection();
        
        // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
  
        // Enable writing data to the connection
        connection.setDoOutput(true);
        
     // Create the request body
        String requestBody = "{\r\n"
        		+ "  \"EndUserIp\": \"" + apiServiceSettingBag.getUserIP() + "\",\r\n"
        		+ "  \"TraceId\": \"" + traceId + "\",\r\n"
        		+ "  \"TokenId\": \"" + apiTokenSettingBag.getFlightTokenNo() + "\",\r\n"
        		+ "  \"BusId\": " + busId + ",\r\n"
        		+ "  \"IsBaseCurrencyRequired\": false,"
                + "  \"SeatId\": 0\r\n"
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


}
