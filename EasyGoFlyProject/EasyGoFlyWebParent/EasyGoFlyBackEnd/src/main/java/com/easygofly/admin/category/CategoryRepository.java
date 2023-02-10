package com.easygofly.admin.category;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.easygofly.entity.Category;



@Repository
public interface CategoryRepository extends PagingAndSortingRepository<Category, Integer>{

	@Query("SELECT u FROM Category u WHERE CONCAT(u.id, ' ', u.name, ' ', u.alias) LIKE %?1%")
	public Page<Category> findCategory(String keyword, Pageable pageable);

	@Query("UPDATE Category u SET u.enabled = ?2 WHERE u.id = ?1")
	@Modifying
	public void updateEnableStatus(Integer id, boolean enabled);

	public Long countById(Integer id);
	
	@Query("SELECT u FROM Category u WHERE u.name = :name")
	public Category getUserByName(@Param("name") String name);
	
	@Query("SELECT u FROM Category u WHERE u.parent is NULL")
	public List<Category> findRootCategories();

}
