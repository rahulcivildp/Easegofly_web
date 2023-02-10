package com.easygofly.site.flight;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.easygofly.entity.ProductDetail;

public interface FlightRepository extends CrudRepository<ProductDetail, Integer> {

	@Query("SELECT p FROM ProductDetail p WHERE p.date = :date AND p.cityOne = :cityOne AND p.cityTwo = :cityTwo")
	public List<ProductDetail> findFlightByDateAndCity(Date date, String cityOne, String cityTwo, Sort ascending);
}
