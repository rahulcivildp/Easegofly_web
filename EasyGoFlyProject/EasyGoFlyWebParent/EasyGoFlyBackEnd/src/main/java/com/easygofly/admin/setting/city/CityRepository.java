package com.easygofly.admin.setting.city;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.easygofly.entity.City;

public interface CityRepository extends CrudRepository<City, Integer> {
	
	public List<City> findAllByOrderByNameAsc();
	
	@Query("SELECT c FROM City c WHERE c.code = :code")
	public City findCityByCode(String code); 
}
