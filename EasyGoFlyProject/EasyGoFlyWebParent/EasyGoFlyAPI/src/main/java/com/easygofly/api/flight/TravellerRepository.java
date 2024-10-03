package com.easygofly.api.flight;

import java.util.Date;
import java.util.List;


import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.easygofly.entity.CartItem;
import com.easygofly.entity.Order;
import com.easygofly.entity.ProductDetail;
import com.easygofly.entity.TravellerDetail;

public interface TravellerRepository extends CrudRepository<TravellerDetail, Integer> {

	@Query("SELECT t FROM TravellerDetail t WHERE t.firstName = :firstName AND t.lastName = :lastName AND t.dob = :dob")
	public List<TravellerDetail> findTravellerByNameAndDate(String firstName, String lastName, Date dob, Sort ascending);
	
	@Query("SELECT t FROM TravellerDetail t WHERE t.productDetail = :productDetail AND t.cartItem = :cartItem")
	public List<TravellerDetail> findTravellerByCurtItemAndProductDetail(ProductDetail productDetail, CartItem cartItem);
	
	@Query("SELECT t FROM TravellerDetail t WHERE t.productDetail = :productDetail AND t.order = :order")
	public List<TravellerDetail> findTravellerByProductDetailAndOrder(ProductDetail productDetail, Order order);

	@Query("SELECT t FROM TravellerDetail t WHERE t.productDetail = :productDetail AND t.order = :order AND t.travelerCountSerial = :travelerCountSerial")
	public List<TravellerDetail> findTravellerByProductDetailOrderAndCount(ProductDetail productDetail, Order order, Integer travelerCountSerial);

	@Query("SELECT t FROM TravellerDetail t WHERE t.productDetail = :productDetail AND t.travelerCountSerial = :travelerCountSerial")
	public List<TravellerDetail> findTravellerFlightCount(ProductDetail productDetail, Integer travelerCountSerial);
}
