package com.easygofly.api.bus;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.easygofly.entity.TBObusCity;

public interface BusCityRepository extends CrudRepository<TBObusCity, Integer> {

	@Query("SELECT t FROM TBObusCity t WHERE t.cityName = :city")
	public TBObusCity getCityByCityName(String city);
	
	@Query("SELECT t FROM TBObusCity t WHERE t.cityId = :cityId")
	public TBObusCity getCityByCityId(Integer cityId);
}
