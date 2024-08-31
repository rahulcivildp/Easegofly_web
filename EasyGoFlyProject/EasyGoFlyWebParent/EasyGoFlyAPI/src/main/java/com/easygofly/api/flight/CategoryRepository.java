package com.easygofly.api.flight;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.easygofly.entity.Category;

public interface CategoryRepository extends CrudRepository<Category, Integer> {
	
	@Query("SELECT c FROM Category c WHERE c.name = :name")
	public Category getCategoryByName(@Param("name")String name); 
	

}
