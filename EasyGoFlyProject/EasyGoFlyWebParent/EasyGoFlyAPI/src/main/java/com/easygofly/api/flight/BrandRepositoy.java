package com.easygofly.api.flight;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.easygofly.entity.Brand;

public interface BrandRepositoy extends CrudRepository<Brand, Integer> {
	
	@Query("SELECT u FROM Brand u WHERE u.name = :name")
	public Brand getBrandByName(@Param("name")String name); 
	
}
