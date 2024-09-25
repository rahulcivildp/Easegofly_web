package com.easygofly.api.flight;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.easygofly.entity.ProductDetail;

public interface ProductDetailCrudRepository extends CrudRepository<ProductDetail, Integer> {
	
	@Query("SELECT p FROM ProductDetail p WHERE p.cityOne = :cityOne AND p.cityTwo = :cityTwo AND p.journeyClass = :journeyClass AND p.traceId = :traceId AND p.mode = :mode AND p.date = :date")
	public List<ProductDetail> findFlights(String cityOne, String cityTwo, String journeyClass, String traceId, String mode, Date date, Sort ascending);
	
	
}
