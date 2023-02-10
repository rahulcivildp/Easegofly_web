package com.easygofly.admin.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.easygofly.entity.Product;
import com.easygofly.entity.User;

public interface ProductRepository extends PagingAndSortingRepository<Product, Integer> {

	@Query("SELECT u FROM Product u WHERE CONCAT("
			+ " u.id, ' ',"
			+ " u.name, ' ',"
			+ " u.alias, ' ',"
			+ " u.cost, ' ',"
			+ " u.price)"
			+ " LIKE %?1%")
	public Page<Product> findProduct(String keyword, Pageable pageable); 
	
	@Query("UPDATE Product u SET u.enabled = ?2 WHERE u.id = ?1")
	@Modifying
	public void updateEnableStatus(Integer id, boolean enabled);

	public Long countById(Integer id);

	@Query("SELECT u FROM Product u WHERE u.name = :name")
	public Product getProductByName(String name);
	
	@Query("SELECT p FROM Product p WHERE p.categories.id = ?1 OR p.categories.allParentIDs LIKE %?2%")
	public Page<Product> findAllInCategory(Integer categoryId, String categoryIdMatch, Pageable pageable); 
	
	@Query("SELECT p FROM Product p WHERE "
			+ "(p.categories.id = ?1 OR "
			+ "p.categories.allParentIDs LIKE %?2%) AND "
			+ "(p.name LIKE %?3% OR "
			+ "p.remarks LIKE %?3% OR "
			+ "p.brands.name LIKE %?3% OR "
			+ "p.categories.name LIKE %?3%)")
	public Page<Product> searchInCategory(Integer categoryId, String categoryIdMatch, String keyword, Pageable pageable); 
	
	@Query("SELECT p FROM Product p WHERE p.user = ?1")
	public Page<Product> findAllByUser(User user, Pageable pageable);
}
