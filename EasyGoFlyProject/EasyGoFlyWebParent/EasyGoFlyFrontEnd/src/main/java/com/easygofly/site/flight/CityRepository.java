package com.easygofly.site.flight;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.easygofly.entity.City;
import com.easygofly.entity.Country;

public interface CityRepository extends CrudRepository<City, Integer> {

	@Query("SELECT c FROM City c WHERE c.cityName = ?1")
	public City getCityByName(String cityName); 
	
	@Query("SELECT c FROM City c WHERE c.code = ?1")
	public City getCityByCode(String code); 
	
	@Query("SELECT c FROM City c WHERE c.country = ?1")
	public List<City> getCityByCountry(Country country);
}
