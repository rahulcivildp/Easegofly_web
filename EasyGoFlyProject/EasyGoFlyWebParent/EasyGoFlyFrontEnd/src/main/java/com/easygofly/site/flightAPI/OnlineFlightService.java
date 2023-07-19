package com.easygofly.site.flightAPI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.stereotype.Service;

@Service
public class OnlineFlightService {
	@SuppressWarnings("unused")
	public String tokenId = "";
	public String traceId = "";
	public String resultIndex = "";
	public String airlineRemark = "";

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
	
	public int apiOnlineMod(HttpURLConnection connection, StringBuilder responseBody, String cityOne, String cityTwo, Integer adultNum, Integer childNum, Integer infantNum, Date date)
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
        		+ "\"EndUserIp\": \"49.37.50.177\", "
        		+ "\"TokenId\": \"" + tokenId + "\", "
        		+ "\"AdultCount\": \"" + adultNum + "\", "
        		+ "\"ChildCount\": \"" + childNum + "\", "
        		+ "\"InfantCount\": \"" + infantNum + "\", "
        		+ "\"DirectFlight\": \"true\", "
        		+ "\"OneStopFlight\": \"true\", "
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
        		+ "\"EndUserIp\": \"49.37.50.177\", "
        		+ "\"TokenId\": \"" + tokenId + "\", "
        		+ "\"TraceId\": \"" + traceId + "\", "
        		+ "\"ResultIndex\": \"" + resultIndex + "\""
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
	
	public int apiOnlineTicket(HttpURLConnection connection, StringBuilder responseBody, String traceId, String resultIndex)
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
        		+ "	\"Passengers\": [{\r\n"
        		+ "		\"Title\": \"Mr\",\r\n"
        		+ "		\"FirstName\": \"OIRNEGRPN\",\r\n"
        		+ "		\"LastName\": \"tbo\",\r\n"
        		+ "		\"PaxType\": 1,\r\n"
        		+ "		\"DateOfBirth\": \"1987-12-06T00:00:00\",\r\n"
        		+ "		\"Gender\": 1,\r\n"
        		+ "		\"PassportNo\": \"KJHHJKHKJH\",\r\n"
        		+ "		\"PassportExpiry\": \"2020-12-06T00:00:00\",\r\n"
        		+ "		\"AddressLine1\": \"123, Test\",\r\n"
        		+ "		\"AddressLine2\": \"\",\r\n"
        		+ "		\"Fare\": {\r\n"
        		+ "			\"BaseFare\": 5531.0,\r\n"
        		+ "			\"Tax\": 1042.0,\r\n"
        		+ "			\"YQTax\": 0.0,\r\n"
        		+ "			\"AdditionalTxnFeePub\": 0.0,\r\n"
        		+ "			\"AdditionalTxnFeeOfrd\": 0.0,\r\n"
        		+ "			\"OtherCharges\": 0.0\r\n"
        		+ "		},\r\n"
        		+ "		\"City\": \"Gurgaon\",\r\n"
        		+ "		\"CountryCode\": \"IN\",\r\n"
        		+ "		\"CountryName\": \"India\",      \r\n"
        		+ "                \"Nationality\": \"IN\",\r\n"
        		+ "		\"ContactNo\": \"9879879877\",\r\n"
        		+ "		\"Email\": \"harsh@tbtq.in\",\r\n"
        		+ "		\"IsLeadPax\": true,\r\n"
        		+ "		\"FFAirlineCode\": \"6E\",\r\n"
        		+ "		\"FFNumber\": \"123\",\r\n"
        		+ "	\"Baggage\":[\r\n"
        		+ "            {\r\n"
        		+ "                \"AirlineCode\": \"6E\",\r\n"
        		+ "                \"FlightNumber\": \"23\",\r\n"
        		+ "                \"WayType\": 2,\r\n"
        		+ "                \"Code\": \"No Baggage\",\r\n"
        		+ "                \"Description\": 2,\r\n"
        		+ "                \"Weight\": 0,\r\n"
        		+ "                \"Currency\": \"INR\",\r\n"
        		+ "                 \"Price\": 0,\r\n"
        		+ "                 \"Origin\": \"DEL\",\r\n"
        		+ "                \"Destination\": \"DXB\"\r\n"
        		+ "        }],\r\n"
        		+ "        \"MealDynamic\": [\r\n"
        		+ "        {\r\n"
        		+ "          \"AirlineCode\": \"6E\",\r\n"
        		+ "          \"FlightNumber\": \"23\",\r\n"
        		+ "          \"WayType\": 2,\r\n"
        		+ "          \"Code\": \"No Meal\",\r\n"
        		+ "          \"Description\": 2,\r\n"
        		+ "          \"AirlineDescription\": \"\",\r\n"
        		+ "          \"Quantity\": 0,\r\n"
        		+ "          \"Currency\": \"INR\",\r\n"
        		+ "          \"Price\": 0,\r\n"
        		+ "          \"Origin\": \"DEL\",\r\n"
        		+ "          \"Destination\": \"DXB\"\r\n"
        		+ "        }],\r\n"
        		+ "	\"SeatDynamic\": [\r\n"
        		+ "        {\r\n"
        		+ "	    \"AirlineCode\": \"6E\",\r\n"
        		+ "             \"FlightNumber\": \"2978\",\r\n"
        		+ "              \"CraftType\": \"A320-180\",\r\n"
        		+ "               \"Origin\": \"DEL\",\r\n"
        		+ "                \"Destination\": \"DXB\",\r\n"
        		+ "                \"AvailablityType\": 1,\r\n"
        		+ "                \"Description\": 2,\r\n"
        		+ "                \"Code\": \"2A\",\r\n"
        		+ "                \"RowNo\": \"2\",\r\n"
        		+ "                \"SeatNo\": \"A\",\r\n"
        		+ "                \"SeatType\": 1,\r\n"
        		+ "                \"SeatWayType\": 2,\r\n"
        		+ "                \"Compartment\": 1,\r\n"
        		+ "                \"Deck\": 1,\r\n"
        		+ "                \"Currency\": \"INR\",\r\n"
        		+ "                \"Price\": 300                                                                                                                                                                                                      \r\n"
        		+ "			\r\n"
        		+ "		}],\r\n"
        		+ "		\"GSTCompanyAddress\": \"\",\r\n"
        		+ "		\"GSTCompanyContactNumber\": \"\",\r\n"
        		+ "		\"GSTCompanyName\": \"\",\r\n"
        		+ "		\"GSTNumber\": \"\",\r\n"
        		+ "		\"GSTCompanyEmail\": \"\"\r\n"
        		+ "	}],\r\n"
        		+ "	\"EndUserIp\": \"192.168.11.58\",\r\n"
        		+ "	\"TokenId\": \"" + tokenId + "\",\r\n"
        		+ "	\"TraceId\": \"" + traceId + "\"\r\n"
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

}
