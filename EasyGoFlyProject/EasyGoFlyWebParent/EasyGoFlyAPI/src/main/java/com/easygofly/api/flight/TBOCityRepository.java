package com.easygofly.api.flight;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.easygofly.entity.TBOCity;

public interface TBOCityRepository extends CrudRepository<TBOCity, Integer> {

	@Query("SELECT t FROM TBOCity t WHERE t.destination = :city")
	public TBOCity getCityByCityName(String city);
	
	@Query("SELECT t FROM TBOCity t WHERE t.cityId = :cityId")
	public TBOCity getCityByCityId(Integer cityId);
	
}
