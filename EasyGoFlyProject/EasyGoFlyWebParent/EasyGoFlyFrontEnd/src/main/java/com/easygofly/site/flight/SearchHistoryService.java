package com.easygofly.site.flight;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easygofly.entity.CartItem;
import com.easygofly.entity.Customer;
import com.easygofly.entity.SearchHistory;
import com.easygofly.site.customer.CustomerRepository;

@Service
@Transactional
public class SearchHistoryService {
	
	@Autowired private CustomerRepository customerRepo;
	@Autowired private SearchHistoryRepository searchRepo;
	@Autowired private EntityManager entityManager;
	@Autowired private SearchHistoryRepository hisrepo;
	
	public Customer saveSearchHistory(Customer customer) {
		Customer userLoggedin = customerRepo.findById(customer.getId()).get();
		
		userLoggedin.setSearchHistory(customer.getSearchHistory());
		
		return customerRepo.save(userLoggedin);
	}
	
	public SearchHistory setCustomerSearchHistory(Customer customer, SearchHistory search) {
		SearchHistory searchHistory = searchRepo.findById(search.getId()).get();
		
		searchHistory.setCustomer(customer);
		
		return searchRepo.save(searchHistory);
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
		for (int i=1;i<count;i++) {
			if (count >= 4) {
				searchRepo.deleteSearchByCustomer(id, cust);
				count = count - 1;
				id++;
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
	
	public Customer getByPhone(String phone) {
		return customerRepo.getCustomerByPhone(phone);
	}
	
	public SearchHistory setSearchHistoryWithouLogin(String cityOne, String cityTwo, Integer passengerNum, String journeyClass, Integer adultNum, Integer childNum, Integer infantNum,
			String tripType, Date date ) {
		if(cityOne == null || cityTwo == null) return null;
		
		Integer passengerNum1 = passengerNum; 
		String journeyClass1 = journeyClass; 
		Integer adultNum1 = adultNum; 
		Integer childNum1 = childNum; 
		Integer infantNum1 = infantNum;
		String tripType1 = tripType; 
		Date date1 = date;
		String cityOne1 = cityOne;
		String cityTwo1 = cityTwo;
		
		SearchHistory history = new SearchHistory(cityOne1, cityTwo1, passengerNum1, journeyClass1, adultNum1, childNum1, infantNum1, tripType1, date1);
		
		return hisrepo.save(history);
	}
	
}
