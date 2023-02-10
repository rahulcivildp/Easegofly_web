package com.easygofly.site.shoppingCart;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.easygofly.entity.CartItem;
import com.easygofly.entity.Customer;
import com.easygofly.entity.TravellerDetail;
import com.easygofly.entity.exception.UserNotFoundException;
import com.easygofly.site.flight.TravellerRepository;

@Service
@Transactional
public class CartItemService {
	
	public static final int ITEM_PER_PAGE = 8;
	
	@Autowired private CartItemRepository cartRepo;
	
	@Autowired private TravellerRepository travelerRepo;
	
	
	public CartItem updateCartItem(CartItem cartItem, String email, BigInteger phoneNum, int quantity, boolean ordered) {
		CartItem updateCart = cartRepo.findById(cartItem.getId()).get();
		
		if(updateCart.getCustomer() != null && updateCart.getProductDetail() != null) {
			updateCart.setCustomer(updateCart.getCustomer());
			updateCart.setProductDetail(updateCart.getProductDetail());
			updateCart.setEmail(email);
			updateCart.setQuantity(quantity);
			updateCart.setPhoneNum(phoneNum);
			updateCart.setOrdered(ordered);
		}
		
		return cartRepo.save(updateCart);
	}
	
	public CartItem updateCartItemOrdered(CartItem cartItem) {
		CartItem updateCart = cartRepo.findById(cartItem.getId()).get();
		
		if(updateCart.getCustomer() != null && updateCart.getProductDetail() != null) {
			updateCart.setOrdered(true);
		}
		
		return cartRepo.save(updateCart);
	}
	
	public CartItem updateTotalPrice(CartItem cartItem, double totalPrice) {
		CartItem updateCart = cartRepo.findById(cartItem.getId()).get();
		if(updateCart.getCustomer() != null && updateCart.getProductDetail() != null) {
			updateCart.setTotalPrice(totalPrice);
		}
		
		return cartRepo.save(updateCart);
	}
	
	public TravellerDetail updateTraveler(TravellerDetail traveler, String salutation, String firstName, String lastName, Date date) {
		TravellerDetail updateTravelers = travelerRepo.findById(traveler.getId()).get();
		
		if(updateTravelers.getId() != null) {
			updateTravelers.setSalutation(salutation);
			updateTravelers.setFirstName(firstName);
			updateTravelers.setLastName(lastName);
			updateTravelers.setDob(date);
		}
		
		return travelerRepo.save(updateTravelers);
	}
	
	public List<CartItem> listAll() {
		return (List<CartItem>) cartRepo.findAll(Sort.by("firstName").ascending());
	}
	
	public Page<CartItem> listByPage(int pageNum, String sortField, String sortDir, String keyword, Customer customer) {
		Sort sort = Sort.by(sortField);
		
		sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();
		
		Pageable pageable = PageRequest.of(pageNum - 1, ITEM_PER_PAGE, sort);
		
		if(keyword != null) {
			return cartRepo.findBooking(keyword, pageable);
		}
		return cartRepo.findByCustomer(customer, pageable);
	}
	
	public void deleteCartItem(Integer id) throws UserNotFoundException {
		Long count = cartRepo.countById(id);
		if(count == null || count == 0) {
			throw new UserNotFoundException("Could not find any Cart Item with ID: " + id);
		}
		
		cartRepo.deleteById(id);
	}
}
