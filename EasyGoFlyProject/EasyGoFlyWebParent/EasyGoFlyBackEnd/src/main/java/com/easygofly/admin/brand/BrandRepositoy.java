package com.easygofly.admin.brand;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.easygofly.entity.Brand;

public interface BrandRepositoy extends PagingAndSortingRepository<Brand, Integer> {
	 
	@Query("SELECT u FROM Brand u WHERE CONCAT(u.id, ' ', u.name, ' ', u.logo) LIKE %?1%")
	public Page<Brand> findBrand(String keyword, Pageable pageable);

	@Query("UPDATE Brand u SET u.enabled = ?2 WHERE u.id = ?1")
	@Modifying
	public void updateEnableStatus(Integer id, boolean enabled);

	public Long countById(Integer id);
	@Query("SELECT u FROM Brand u WHERE u.name = :name")
	public Brand getBrandByName(@Param("name")String name); 
	
}
