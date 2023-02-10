package com.easygofly.site.flight;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.easygofly.entity.Product;

public interface ProductDetailsRepository extends PagingAndSortingRepository<Product, Integer> {

	@Query("SELECT p FROM Product p WHERE p.cityOne = :cityOne AND p.cityTwo = :cityTwo")
	public Product findProductByCity(String cityOne, String cityTwo);
	
	@Query("SELECT p FROM Product p WHERE p.cityOne = :cityOne AND p.cityTwo = :cityTwo")
	public List<Product> findProductByCity(String cityOne, String cityTwo, Sort ascending);
	

}
