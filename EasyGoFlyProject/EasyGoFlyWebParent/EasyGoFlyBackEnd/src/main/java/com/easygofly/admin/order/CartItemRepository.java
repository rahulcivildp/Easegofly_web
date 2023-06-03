package com.easygofly.admin.order;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.easygofly.entity.CartItem;
import com.easygofly.entity.Customer;

public interface CartItemRepository extends CrudRepository<CartItem, Integer> {
	
	@Query("SELECT c FROM CartItem c WHERE c.customer = ?1")
	public List<CartItem> findByCustomer(Customer customer); 

}
