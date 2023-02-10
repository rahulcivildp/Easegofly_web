package com.easygofly.site.shoppingCart;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.easygofly.entity.CartItem;
import com.easygofly.entity.Customer;
import com.easygofly.entity.ProductDetail;

public interface CartItemRepository extends PagingAndSortingRepository<CartItem, Integer> {

	public List<CartItem> findByCustomer(Customer customer); 
	
	@Query("SELECT c FROM CartItem c WHERE c.customer = :customer AND c.productDetail = :productDetail")
	public CartItem findByCustomerAndProductDetail(Customer customer, ProductDetail productDetail);
	
	@Modifying
	@Query("UPDATE CartItem c SET c.quantity = ?1, c.email = ?2, c.phoneNum = ?3 WHERE c.id = ?4")
	public void updateEmailAndPhone(Integer quantity,String email, Integer phoneNum, Integer id);
	
	@Modifying
	@Query("DELETE FROM CartItem c WHERE c.customer.id = ?1 AND c.productDetail.id = ?2")
	public void deleteCustomerAndProductDetail(Integer customerId, Integer productId);
	
	@Query("SELECT c FROM CartItem c WHERE CONCAT(c.email, ' ', c.customer, ' ', c.id) LIKE %?1%")
	public Page<CartItem> findBooking(String keyword, Pageable pageable); 
	
	@Query("SELECT c FROM CartItem c WHERE c.customer = :customer")
	public Page<CartItem> findByCustomer(Customer customer, Pageable pageable); 
	
	public Long countById(Integer id);
}
