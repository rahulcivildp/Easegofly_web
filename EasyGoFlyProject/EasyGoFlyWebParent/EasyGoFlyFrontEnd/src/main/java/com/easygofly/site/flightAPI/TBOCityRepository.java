package com.easygofly.site.flightAPI;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.easygofly.entity.TBOCity;

public interface TBOCityRepository extends CrudRepository<TBOCity, Integer> {

	@Query("SELECT t FROM TBOCity t WHERE t.destination = :city")
	public TBOCity getCityByCityId(String city);
	
}
