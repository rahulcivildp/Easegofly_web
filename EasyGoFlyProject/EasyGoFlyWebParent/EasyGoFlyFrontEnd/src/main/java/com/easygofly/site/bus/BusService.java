package com.easygofly.site.bus;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.easygofly.entity.Bus;
import com.easygofly.entity.BusCancelPolicy;
import com.easygofly.entity.BusHistory;
import com.easygofly.entity.BusPointDetails;
import com.easygofly.entity.Customer;
import com.easygofly.site.LogService;

@Service
public class BusService {
	@Autowired private OnlineBusService onlineBusService;
	@Autowired private LogService logService;
	@Autowired private BusHistoryRepository busHistoryRepo;
	@Autowired private BusRepository busRepo;
	@Autowired private BusPointDetailRepository busPointDetailRepo;
	@Autowired private BusCancelPolicyRepository busCancelPolicyRepo;

	public void authenticationBus(Model model) {
		try {
        	
        	// Create URL object with the API end-point
//            URL url = new URL("https://api.travelboutiqueonline.com/SharedAPI/SharedData.svc/rest/Authenticate");
            
        	// Create URL object with the API end-point
            URL url = new URL("http://api.tektravels.com/SharedServices/SharedData.svc/rest/Authenticate");

            // Open a connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
        	StringBuilder responseBody = new StringBuilder();
        	
            int authCode = onlineBusService.apiAuthenticationBus(connection, responseBody);
            
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
            
            onlineBusService.tokenId = (String) jsonObj.get("TokenId");
            System.out.println(jsonObj);
            logService.generateLog(jsonObj.toString());
            
            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	
	public BusHistory saveBusHistory(BusHistory history, Customer customer) {
		BusHistory newHistory = new BusHistory(history.getDeptDate(), history.getCityIdOne(), history.getCityIdTwo(), customer);
		
		return busHistoryRepo.save(newHistory); 
	}
	
	public BusHistory findByIdBusHistory(Integer id) {
		BusHistory savedHistory = busHistoryRepo.findById(id).get();
		return savedHistory; 
	}
	
	
	public Bus saveBus(Bus bus, Customer customer) {
		Bus newBus = bus;
		newBus.setCustomer(customer);
		
		Bus savedBus = busRepo.save(newBus); 
		
		for (BusPointDetails busPointDetails : savedBus.getBoardingPointsDetails()) {
			busPointDetails.setBus(savedBus);
			busPointDetailRepo.save(busPointDetails);
		}
		for (BusPointDetails busDropPointDetails : savedBus.getDroppingPointsDetails()) {
			busDropPointDetails.setBus(savedBus);
			busPointDetailRepo.save(busDropPointDetails);
		}
		for (BusCancelPolicy busCancelPolicy : savedBus.getBusCancelPolicies()) {
			busCancelPolicy.setBus(savedBus);
			busCancelPolicyRepo.save(busCancelPolicy);
		}
		
		return savedBus; 
	}
	
	public Bus findByIdBus(Integer id) {
		Bus bus = busRepo.findById(id).get();
		return bus; 
	}
	
}
