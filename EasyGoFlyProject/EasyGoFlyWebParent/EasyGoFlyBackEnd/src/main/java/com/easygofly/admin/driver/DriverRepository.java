package com.easygofly.admin.driver;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.easygofly.entity.Driver;

public interface DriverRepository extends PagingAndSortingRepository<Driver, Integer> {
	
	public Long countById(Integer id);
	
	@Query("SELECT d FROM Driver d WHERE CONCAT(d.id, ' ', d.name, ' ', d.experience, ' ', d.rating) LIKE %?1%")
	public Page<Driver> findDriver(String keyword, Pageable pageable); 

}
