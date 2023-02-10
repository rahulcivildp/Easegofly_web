package com.easygofly.admin.product.stops;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.easygofly.entity.Stop;

public interface StopsRepository extends CrudRepository<Stop, Integer> {
	
	@Query("SELECT s FROM Stop s WHERE s.productDetail.id = ?1 ")
	public List<Stop> findByProductDetail(Integer flightId);
}
