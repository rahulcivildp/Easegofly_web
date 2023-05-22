package com.easygofly.site.search;

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
	
	@Autowired
	private CustomerRepository customerRepo;
	
	@Autowired
	private SearchHistoryRepository searchRepo;
	
	@Autowired
	private EntityManager entityManager;
	
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
}
