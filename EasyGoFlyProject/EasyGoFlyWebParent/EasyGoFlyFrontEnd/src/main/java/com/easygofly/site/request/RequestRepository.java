package com.easygofly.site.request;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.easygofly.entity.Customer;
import com.easygofly.entity.Request;

public interface RequestRepository extends PagingAndSortingRepository<Request, Integer> {

	@Query("SELECT r FROM Request r WHERE r.customer= ?1 ")
	public Page<Request> findRequest(Customer customer, Pageable pageable); 
}
