package com.easygofly.admin.request;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.easygofly.entity.Request;

public interface RequestRepository extends PagingAndSortingRepository<Request, Integer> {

	public List<Request> findAllByOrderByCreatedTimeAsc();
	
	@Query("SELECT r FROM Request r WHERE CONCAT(r.id, ' ', r.subject, ' ', r.createdTime) LIKE %?1%")
	public Page<Request> findRequest(String keyword, Pageable pageable);
	
	public Long countById(Integer id);
}
