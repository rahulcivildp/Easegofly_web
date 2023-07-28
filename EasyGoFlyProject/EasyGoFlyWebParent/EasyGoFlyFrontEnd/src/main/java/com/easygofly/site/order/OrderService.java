package com.easygofly.site.order;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.easygofly.entity.CartItem;
import com.easygofly.entity.Country;
import com.easygofly.entity.Customer;
import com.easygofly.entity.Order;
import com.easygofly.entity.OrderStatus;
import com.easygofly.entity.PaymentMethod;
import com.easygofly.entity.ProductDetail;
import com.easygofly.entity.SearchHistory;
import com.easygofly.entity.TravellerDetail;
import com.easygofly.entity.exception.UserNotFoundException;
import com.easygofly.site.checkout.CheckoutInfo;
import com.easygofly.site.flight.TravellerRepository;

@Service
public class OrderService {
	
	public static final int ORDER_PER_PAGE = 6;
	
	@Autowired OrderRepository orderRepo;
	@Autowired TravellerRepository travellerRepo;

	public Order createOrder(Customer customer, CartItem cartItem, ProductDetail productDetail, PaymentMethod paymentMethod, CheckoutInfo checkoutInfo, SearchHistory searchHistory, String orderName, List<TravellerDetail> travellerDetails) {
		Order newOrder = new Order();
		newOrder.setCreatedTime(new Date());
		newOrder.setOrderStatus(OrderStatus.NEW);
		newOrder.setCustomer(customer);
		newOrder.setProductDetail(productDetail);
		newOrder.setPrice(checkoutInfo.getPaymentTotal());
		newOrder.setPaymentMethod(paymentMethod);
		newOrder.setName(orderName);

		newOrder.setAddressLine1(customer.getAddressLine1());
		newOrder.setAddressLine2(customer.getAddressLine2());
		newOrder.setCity(customer.getCity());
		
		Country country = customer.getCountry();
		newOrder.setCountry(country.getName());
		
		newOrder.setPostalCode(customer.getPostalCode());
		newOrder.setState(customer.getState());
		newOrder.setFirstName(customer.getFirstName());
		newOrder.setLastName(customer.getLastName());
		newOrder.setPhoneNumber(cartItem.getPhoneNum());
		newOrder.setAdultNum(searchHistory.getAdultNum());
		newOrder.setChildNum(searchHistory.getChildNum());
		newOrder.setInfantNum(searchHistory.getInfantNum());
		newOrder.setCityOne(searchHistory.getCityOne());
		newOrder.setCityTwo(searchHistory.getCityTwo());
		newOrder.setJourneyClass(searchHistory.getJourneyClass());
		newOrder.setPassengerNum(searchHistory.getPassengerNum());
		newOrder.setTripType(searchHistory.getTripType());
		newOrder.setCartId(cartItem.getId());
		newOrder.setContactEmail(cartItem.getEmail());
		newOrder.setTravellerDetails(travellerDetails);
		
		String transaction_id = "UIGIK&*^HJAS585789";
		String transaction_token = "ashdjgh3284270&^%@#*&)asahj31";
		
		newOrder.setTransactionId(transaction_id);
		newOrder.setTransactionToken(transaction_token);
		
		return orderRepo.save(newOrder);
	}
	
	public Order createOrderOnline(Customer customer, CartItem cartItem, ProductDetail productDetail, PaymentMethod paymentMethod, String price, SearchHistory searchHistory, String orderName, List<TravellerDetail> travellerDetails) {
		Order newOrder = new Order();
		newOrder.setCreatedTime(new Date());
		newOrder.setOrderStatus(OrderStatus.NEW);
		newOrder.setCustomer(customer);
		newOrder.setProductDetail(productDetail);
		newOrder.setPrice(Double.parseDouble(price));
		newOrder.setPaymentMethod(paymentMethod);
		newOrder.setName(orderName);

		newOrder.setAddressLine1(customer.getAddressLine1());
		newOrder.setAddressLine2(customer.getAddressLine2());
		newOrder.setCity(customer.getCity());
		
		Country country = customer.getCountry();
		newOrder.setCountry(country.getName());
		
		newOrder.setPostalCode(customer.getPostalCode());
		newOrder.setState(customer.getState());
		newOrder.setFirstName(customer.getFirstName());
		newOrder.setLastName(customer.getLastName());
		newOrder.setPhoneNumber(cartItem.getPhoneNum());
		newOrder.setAdultNum(searchHistory.getAdultNum());
		newOrder.setChildNum(searchHistory.getChildNum());
		newOrder.setInfantNum(searchHistory.getInfantNum());
		newOrder.setCityOne(searchHistory.getCityOne());
		newOrder.setCityTwo(searchHistory.getCityTwo());
		newOrder.setJourneyClass(searchHistory.getJourneyClass());
		newOrder.setPassengerNum(searchHistory.getPassengerNum());
		newOrder.setTripType(searchHistory.getTripType());
		newOrder.setCartId(cartItem.getId());
		newOrder.setContactEmail(cartItem.getEmail());
		newOrder.setTravellerDetails(travellerDetails);
		
		String transaction_id = "UIGIK&*^HJAS585789";
		String transaction_token = "ashdjgh3284270&^%@#*&)asahj31";
		
		newOrder.setTransactionId(transaction_id);
		newOrder.setTransactionToken(transaction_token);
		
		return orderRepo.save(newOrder);
	}
	
	public Order updateOrder(Order order, OrderStatus orderStatus) {
		order.setOrderStatus(orderStatus);
		return orderRepo.save(order);
	}
	
	public Order updateOrderPrice(Order order, CheckoutInfo checkoutInfo) {
		Order savedOrder = orderRepo.findById(order.getId()).get();
		
		savedOrder.setPrice(checkoutInfo.getPaymentTotal());
		return orderRepo.save(savedOrder);
	}
	
	public Order updateOrderPriceOnline(Order order, String price) {
		Order savedOrder = orderRepo.findById(order.getId()).get();
		
		savedOrder.setPrice(Double.parseDouble(price));
		return orderRepo.save(savedOrder);
	}
	
	public Order addCouponCode(Order order, String couponCode) {
		Order savedOrder = orderRepo.findById(order.getId()).get();
		
		savedOrder.setCouponCode(couponCode);
		return orderRepo.save(savedOrder);
	}
	
	public Order deleteCouponCode(Order order) {
		Order savedOrder = orderRepo.findById(order.getId()).get();
		
		savedOrder.setCouponCode(null);
		return orderRepo.save(savedOrder);
	}
	
	public Order updateTotalPassenger(Order order, Integer totalSeat) {
		ProductDetail productDetail = order.getProductDetail();
		productDetail.setTotalSeats(totalSeat.toString());
		return orderRepo.save(order);
	}
	
	public void deleteOrder(Integer id) throws UserNotFoundException {
		Long count = orderRepo.countById(id);
		if(count == null || count == 0) {
			throw new UserNotFoundException("Could not find any Cart Item with ID: " + id);
		}
		
		orderRepo.deleteById(id);
	}

	public List<Order> listAll() {
		return (List<Order>) orderRepo.findAll(Sort.by("firstName").ascending());
	}
	
	public Page<Order> listByPageOrder(Customer customer, int pageNum, String sortField, String sortDir) {
		Sort sort = Sort.by(sortField);
		
		sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();
		
		Pageable pageable = PageRequest.of(pageNum - 1, ORDER_PER_PAGE, sort);
		
		return orderRepo.findByCustomer(customer, pageable);
	}
	
	public TravellerDetail updateTravelersOrderId(Integer travelerId, Order order) {
		TravellerDetail travellerDetail = travellerRepo.findById(travelerId).get();
		travellerDetail.setOrder(order);
		return travellerRepo.save(travellerDetail);
	}
	
}
