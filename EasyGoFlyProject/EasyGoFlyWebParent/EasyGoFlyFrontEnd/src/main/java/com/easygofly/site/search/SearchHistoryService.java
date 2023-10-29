package com.easygofly.site.search;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.easygofly.entity.CartItem;
import com.easygofly.entity.Customer;
import com.easygofly.entity.SearchHistory;
import com.easygofly.site.LogService;
import com.easygofly.site.customer.CustomerRepository;
import com.easygofly.site.flightAPI.OnlineFlightService;

@Service
@Transactional
public class SearchHistoryService {
	
	@Autowired private CustomerRepository customerRepo;
	@Autowired private SearchHistoryRepository searchRepo;
	@Autowired private EntityManager entityManager;
	@Autowired private OnlineFlightService onlineFlightService;
	@Autowired private LogService logService;
	
	public Customer saveSearchHistory(Customer customer) {
		Customer userLoggedin = customerRepo.findById(customer.getId()).get();
		
		userLoggedin.setSearchHistory(customer.getSearchHistory());
		
		return customerRepo.save(userLoggedin);
	}
	
	public SearchHistory updateSearchHistory(SearchHistory search, CartItem item) {
		SearchHistory searchHistory = searchRepo.findById(search.getId()).get();
		
		searchHistory.setCartItem(item);
		
		return searchRepo.save(searchHistory);
	}
	
	public SearchHistory updateSearchHistoryCart(SearchHistory search, CartItem item) {
		SearchHistory searchHistory = searchRepo.findById(search.getId()).get();
		
		searchHistory.setCart_id(item.getId());
		
		return searchRepo.save(searchHistory);
	}
	
	public void deleteSearchResult(Customer customer) {
		Customer userLoggedin = customerRepo.findById(customer.getId()).get();
		Customer cust = entityManager.find(Customer.class, userLoggedin.getId());
		
		Iterable<SearchHistory> search = cust.getSearchHistory();
		SearchHistory firstHistory = search.iterator().next();
		Integer id = firstHistory.getId();
		Integer count = extracted(search);
		System.out.println(count);
		for (int i=1;i<count;i++) {
			if (count >= 4) {
				System.out.println(firstHistory.getId());
				searchRepo.deleteSearchByCustomer(id, cust);
				count = count - 1;
				id++;
				System.out.println(count);
			}
		}
	}
	
	private Integer extracted(Iterable<SearchHistory> data) {
		int counter = 0;
		for (@SuppressWarnings("unused") Object i : data) {
		    counter++;
		}
		
		return counter;
	}
	
	public Customer getByEmail(String email) {
		return customerRepo.getCustomerByEmail(email);
	}
	
	public void authenticationFlight(Model model) {
		try {
        	
        	// Create URL object with the API end-point
            URL url = new URL("http://api.tektravels.com/SharedServices/SharedData.svc/rest/Authenticate");

            // Open a connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
        	StringBuilder responseBody = new StringBuilder();
        	
            int authCode = onlineFlightService.apiAuthentication(connection, responseBody);
            
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
            
            onlineFlightService.tokenId = (String) jsonObj.get("TokenId");
            System.out.println(jsonObj);
            logService.generateLog(jsonObj.toString());
            
            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
}
