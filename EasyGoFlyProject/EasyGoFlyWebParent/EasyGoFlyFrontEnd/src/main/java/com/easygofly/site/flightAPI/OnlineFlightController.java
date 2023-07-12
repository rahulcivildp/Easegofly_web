package com.easygofly.site.flightAPI;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class OnlineFlightController {
	@Autowired private OnlineFlightService service;
	
	
	private String tokenId = "";
	
	@GetMapping("/authentication")
	public String onlineFlightAPI(Model model) {
        try {
        	
        	// Create URL object with the API end-point
            URL url = new URL("http://api.tektravels.com/SharedServices/SharedData.svc/rest/Authenticate");

            // Open a connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
        	StringBuilder responseBody = new StringBuilder();
        	
            int authCode = service.apiAuthentication(connection, responseBody);
            
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

            return "test/online_api";
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
		
    }
	
//	@GetMapping("/loading_")
//    public String performApiRequest() {
//        // Perform your API logic here
//        // Redirect to the loading page
//        return "test/loading";
//    }
//	
//	@GetMapping("/api_results")
//	public String getFlightDetails(Model model) {
//        return "redirect:/loading_";
//	}
	
//	@GetMapping("/result")
//    public String performApiResult(Model model) {
//		
//		try {
//			// Create URL object with the API end-point
//            URL urlSearch = new URL("http://api.tektravels.com/BookingEngineService_Air/AirService.svc/rest/Search");
//
//            // Open a connection
//            HttpURLConnection connectionSearch = (HttpURLConnection) urlSearch.openConnection();
//            
//            StringBuilder responseBodySearch = new StringBuilder();
//            
//            // Create the request body
//            String requestBody = "{"
//            		+ "\"EndUserIp\": \"49.37.50.177\", "
//            		+ "\"TokenId\": \"" + tokenId + "\", "
//            		+ "\"AdultCount\": \"1\", "
//            		+ "\"ChildCount\": \"0\", "
//            		+ "\"InfantCount\": \"0\", "
//            		+ "\"DirectFlight\": \"true\", "
//            		+ "\"OneStopFlight\": \"true\", "
//            		+ "\"JourneyType\": \"1\", "
//            		+ "\"PreferredAirlines\": null, "
//            		+ "\"Segments\": [{"
//            			+ "\"Origin\": \"CCU\", "
//            			+ "\"Destination\": \"BLR\", "
//            			+ "\"FlightCabinClass\": \"1\", "
//            			+ "\"PreferredDepartureTime\": \"2023-08-20T00: 00: 00\", "
//            			+ "\"PreferredArrivalTime\": \"2023-08-20T00: 00: 00\""
//            			+ "}],"
//            		+ "\"Sources\": null"
//            		+ "}";
//            
//            int responseCode = service.apiOnlineMod(connectionSearch, responseBodySearch, requestBody);
//            
//            JSONObject jsonObjSearch = new JSONObject(responseBodySearch.toString());
//            
//            model.addAttribute("responseCode", responseCode);
//
//            System.out.println("Response Body: " + responseBodySearch.toString());
//            
//            System.out.println("connection.getReadTimeout() : " + connectionSearch.HTTP_GATEWAY_TIMEOUT);
//            
//            
//         // Close the connection
//            connectionSearch.disconnect();
//            return "test/result";
//            
//		} catch (Exception e) {
//			return null;// TODO: handle exception
//		}
//        // Perform your API logic here
//        // Redirect to the loading page
//    }

	

}

