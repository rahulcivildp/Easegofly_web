package com.easygofly.admin.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.easygofly.entity.Order;

public interface OrderRepository extends PagingAndSortingRepository<Order, Integer> {

	@Query("SELECT o FROM Order o WHERE CONCAT(o.id, ' ', o.name, ' ', o.firstName, ' ', o.lastName, ' ', o.createdTime) LIKE %?1%")
	public Page<Order> findOrder(String keyword, Pageable pageable);  
}
