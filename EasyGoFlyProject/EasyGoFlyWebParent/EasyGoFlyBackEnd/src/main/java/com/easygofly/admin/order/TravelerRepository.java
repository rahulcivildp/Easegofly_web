package com.easygofly.admin.order;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.easygofly.entity.CartItem;
import com.easygofly.entity.ProductDetail;
import com.easygofly.entity.TravellerDetail;

public interface TravelerRepository extends CrudRepository<TravellerDetail, Integer> {

	@Query("SELECT t FROM TravellerDetail t WHERE t.cartItem = ?1 AND t.productDetail = ?2")
	public List<TravellerDetail> findTravellers(CartItem cartItem, ProductDetail productDetail);
}
