package com.easygofly.admin.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.easygofly.entity.Customer;

public interface CustomerRepository extends PagingAndSortingRepository<Customer, Integer> {

	@Query("SELECT c FROM Customer c WHERE c.email = :email")
	public Customer getCustomerByEmail(@Param("email") String email);
	
	public Long countById(Integer id);
	
	@Query("SELECT c FROM Customer c WHERE CONCAT(c.id, ' ', c.email, ' ', c.firstName, ' ', c.lastName) LIKE %?1%")
	public Page<Customer> findUser(String keyword, Pageable pageable); 
	
	@Query("UPDATE Customer c SET c.enabled = ?2 WHERE c.id = ?1")
	@Modifying
	public void updateEnableStatus(Integer id, boolean enabled);
}
