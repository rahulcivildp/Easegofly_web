package com.easygofly.site.flightAPI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class OnlineFlightController {
	private String tokenId = "";
	
	@GetMapping("/authentication")
	public String onlineFlightAPI(Model model) {
        try {
        	
        	// Create URL object with the API end-point
            URL url = new URL("http://api.tektravels.com/SharedServices/SharedData.svc/rest/Authenticate");

            // Open a connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
        	StringBuilder responseBody = new StringBuilder();
        	
            int authCode = apiAuthentication(connection, responseBody);
            
            JSONObject jsonObj = new JSONObject(responseBody.toString());
            JSONObject jsonObjInnerError = jsonObj.getJSONObject("Error");
            JSONObject jsonObjInnerMember = jsonObj.getJSONObject("Member");
             
            model.addAttribute("authCode", authCode);
            model.addAttribute("responseBody", jsonObj);
            model.addAttribute("memberName", jsonObjInnerMember.get("FirstName") + " " + jsonObjInnerMember.get("LastName"));
            model.addAttribute("memberEmail", jsonObjInnerMember.get("Email"));
            model.addAttribute("memberId", jsonObjInnerMember.get("MemberId"));
            model.addAttribute("memberAgencyId", jsonObjInnerMember.get("AgencyId"));
            model.addAttribute("memberLoginName", jsonObjInnerMember.get("LoginName"));
            model.addAttribute("memberLoginDetails", jsonObjInnerMember.get("LoginDetails"));
            model.addAttribute("memberIsPrimaryAgent", jsonObjInnerMember.get("isPrimaryAgent"));
            model.addAttribute("errorCode", jsonObjInnerError.get("ErrorCode"));
            model.addAttribute("errorMessage", jsonObjInnerError.get("ErrorMessage"));
            
            System.out.println("Token ID: " + jsonObj.get("TokenId"));
            tokenId = (String) jsonObj.get("TokenId");
            // Print the response
//            System.out.println("Response Code: " + responseCode);
//            System.out.println("Response Body: " + responseBody.toString());

            // Close the connection
            connection.disconnect();
            
         // Create URL object with the API end-point
            URL urlSearch = new URL("http://api.tektravels.com/BookingEngineService_Air/AirService.svc/rest/Search");

            // Open a connection
            HttpURLConnection connectionSearch = (HttpURLConnection) urlSearch.openConnection();
            
            StringBuilder responseBodySearch = new StringBuilder();
            
            // Create the request body
            String requestBody = "{"
            		+ "\"EndUserIp\": \"49.37.50.177\", "
            		+ "\"TokenId\": \"" + tokenId + "\", "
            		+ "\"AdultCount\": \"1\", "
            		+ "\"ChildCount\": \"0\", "
            		+ "\"InfantCount\": \"0\", "
            		+ "\"DirectFlight\": \"true\", "
            		+ "\"OneStopFlight\": \"true\", "
            		+ "\"JourneyType\": \"1\", "
            		+ "\"PreferredAirlines\": null, "
            		+ "\"Segments\": [{"
            			+ "\"Origin\": \"CCU\", "
            			+ "\"Destination\": \"BLR\", "
            			+ "\"FlightCabinClass\": \"1\", "
            			+ "\"PreferredDepartureTime\": \"2023-08-20T00: 00: 00\", "
            			+ "\"PreferredArrivalTime\": \"2023-08-20T00: 00: 00\""
            			+ "}],"
            		+ "\"Sources\": null"
            		+ "}";
            
            int responseCode = apiOnlineMod(connectionSearch, responseBodySearch, requestBody);
            
            JSONObject jsonObjSearch = new JSONObject(responseBodySearch.toString());
            
            model.addAttribute("responseCodeSearch", responseCode);
            model.addAttribute("jsonObjSearch", jsonObjSearch);

            System.out.println("Response Body: " + responseBodySearch.toString());
            
            
         // Close the connection
            connectionSearch.disconnect();
            
            return "test/online_api";
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
		
    }
	
	public String getFlightDetails(Model model) {
		try {
			// Create URL object with the API end-point
            URL urlSearch = new URL("http://api.tektravels.com/BookingEngineService_Air/AirService.svc/rest/Search");

            // Open a connection
            HttpURLConnection connectionSearch = (HttpURLConnection) urlSearch.openConnection();
            
            StringBuilder responseBodySearch = new StringBuilder();
            
            // Create the request body
            String requestBody = "{"
            		+ "\"EndUserIp\": \"49.37.50.177\", "
            		+ "\"TokenId\": \"" + tokenId + "\", "
            		+ "\"AdultCount\": \"1\", "
            		+ "\"ChildCount\": \"0\", "
            		+ "\"InfantCount\": \"0\", "
            		+ "\"DirectFlight\": \"true\", "
            		+ "\"OneStopFlight\": \"true\", "
            		+ "\"JourneyType\": \"1\", "
            		+ "\"PreferredAirlines\": null, "
            		+ "\"Segments\": [{"
            			+ "\"Origin\": \"CCU\", "
            			+ "\"Destination\": \"BLR\", "
            			+ "\"FlightCabinClass\": \"1\", "
            			+ "\"PreferredDepartureTime\": \"2023-08-20T00: 00: 00\", "
            			+ "\"PreferredArrivalTime\": \"2023-08-20T00: 00: 00\""
            			+ "}],"
            		+ "\"Sources\": null"
            		+ "}";
            
            int responseCode = apiOnlineMod(connectionSearch, responseBodySearch, requestBody);
            
            JSONObject jsonObjSearch = new JSONObject(responseBodySearch.toString());
            
            model.addAttribute("responseCode", responseCode);

            System.out.println("Response Body: " + responseBodySearch.toString());
            
            
         // Close the connection
            connectionSearch.disconnect();
            return null;
            
		} catch (Exception e) {
			return null;// TODO: handle exception
		}
		
	}

	private int apiAuthentication(HttpURLConnection connection, StringBuilder responseBody)
			throws IOException {
		
        // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
        
        
        // Enable writing data to the connection
        connection.setDoOutput(true);

        // Create the request body
        String requestBody = "{"
        		+ "\"ClientId\": \"ApiIntegrationNew\", "
        		+ "\"UserName\": \"aladdin\", "
        		+ "\"Password\": \"aladdin@1234\", "
        		+ "\"EndUserIp\": \"49.37.50.177\""
        		+ "}";
        
        
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
	
	private int apiOnlineMod(HttpURLConnection connection, StringBuilder responseBody, String requestBody)
			throws IOException {
		
        // Set the request method to POST
        connection.setRequestMethod("POST");
        
        // Set request headers (if required)
        connection.setRequestProperty("Content-Type", "application/json");
        
        
        // Enable writing data to the connection
        connection.setDoOutput(true);
        
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

