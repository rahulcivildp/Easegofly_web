package com.easygofly.site.flight;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.easygofly.entity.Brand;
import com.easygofly.entity.CartItem;
import com.easygofly.entity.Product;
import com.easygofly.entity.ProductDetail;
import com.easygofly.entity.TravellerDetail;

@Service
@Transactional
public class ProductDetailService {
	
	public static final int FLIGHT_PER_PAGE = 4;

	@Autowired private ProductDetailsRepository productRepo;
	
	@Autowired private BrandRepositoy brandRepo;
	
	@Autowired private FlightRepository flightRepo;
	
	@Autowired private TravellerRepository travellerRepo;
	
	public Product searchFlights(Integer id, String cityOne, String cityTwo) {
		Product productBycity = productRepo.findProductByCity(cityOne, cityTwo);
		boolean isNullValue = (id == null || id == 0);
		
		if (isNullValue) {
			return null;
		}
		return productBycity;
	}
	
	public List<Brand> listBrand() {
		return (List<Brand>) brandRepo.findAll();
	}
	
	public List<Product> listAllProducts(String cityOne, String cityTwo) {
		List<Product> products = productRepo.findProductByCity(cityOne, cityTwo, Sort.by("name").ascending());
		return products;
		
	}
	
	public List<ProductDetail> listAllFlights(String cityOne, String cityTwo, Date date) {
		
		List<ProductDetail> productDetails = flightRepo.findFlightByDateAndCity(date, cityOne, cityTwo, Sort.by("pnr").ascending());
		return productDetails;
	}
	
	public Page<Product> listByPage(int pageNum, String sortField, String sortDir, String keyword) {
		Sort sort = Sort.by(sortField);
		
		sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();
		
		Pageable pageable = PageRequest.of(pageNum - 1, FLIGHT_PER_PAGE, sort);
		
		return productRepo.findAll(pageable);
	}
	
	public ProductDetail saveCartItem(ProductDetail flight) {
		ProductDetail savedCartItem = flightRepo.findById(flight.getId()).get();
		
		savedCartItem.setCartItems(flight.getCartItems());
		
		return flightRepo.save(savedCartItem);
	}
	
	public ProductDetail saveFlightPassengerDetails(ProductDetail flight) {
		ProductDetail savedFlightPassengerDetails = flightRepo.findById(flight.getId()).get();
		
		savedFlightPassengerDetails.setTravellerDetails(flight.getTravellerDetails());
		
		return flightRepo.save(savedFlightPassengerDetails);
	}
	
	public List<TravellerDetail> findTraveller(ProductDetail productDetail, CartItem cartItem) {
		return travellerRepo.findTravellerByCustomerAndProductDetail(productDetail, cartItem);
	}
	
}
