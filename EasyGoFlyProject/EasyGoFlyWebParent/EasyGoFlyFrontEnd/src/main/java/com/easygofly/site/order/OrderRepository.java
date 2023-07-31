package com.easygofly.site.order;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.easygofly.entity.Customer;
import com.easygofly.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Integer> {

	@Query("SELECT o FROM Order o INNER JOIN ProductDetail p ON p.id = o.productDetail WHERE p.totalSeats >= o.passengerNum AND o.customer = ?1")
	public Page<Order> findByCustomer(Customer customer, Pageable pageable);
	
	@Query("SELECT o FROM Order o WHERE o.customer = ?1 ")
	public List<Order> findByCustomer(Customer customer);
	
	@Query("SELECT o FROM Order o WHERE o.cartId = ?1 ")
	public Order findByCartItemOrder(Integer cartId);
	
	public Long countById(Integer id); 
}
