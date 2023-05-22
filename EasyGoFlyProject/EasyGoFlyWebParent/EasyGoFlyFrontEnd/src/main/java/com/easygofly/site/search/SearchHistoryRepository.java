package com.easygofly.site.search;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.easygofly.entity.CartItem;
import com.easygofly.entity.Customer;
import com.easygofly.entity.SearchHistory;

public interface SearchHistoryRepository extends CrudRepository<SearchHistory, Integer> {
	
	@Query("SELECT s FROM SearchHistory s ORDER BY s.date DESC")
	public SearchHistory getSearchResultByDate();
	
	@Modifying
	@Query("DELETE FROM SearchHistory s WHERE s.id = :id AND s.customer = :cust")
	public void deleteSearchByCustomer(Integer id, Customer cust);
	
	public List<SearchHistory> findByCustomer(Customer customer);
	
	public List<SearchHistory> findByCartItem(CartItem cartItem);
	
	@Query("SELECT s FROM SearchHistory s WHERE s.cart_id = :cart_id")
	public SearchHistory findByCart_id(Integer cart_id);
}
