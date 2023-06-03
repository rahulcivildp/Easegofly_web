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
	
	public List<ProductDetail> listAllFLightByCityAndBrand(String cityOne, String cityTwo, String brand, Sort sort) {
		List<ProductDetail> productDetails = flightRepo.findFlightByCityAndBrand(cityOne, cityTwo, brand, sort);
		return productDetails;
	}
	
	public List<ProductDetail> listAllFLightByCity(String cityOne, String cityTwo, Sort sort) {
		List<ProductDetail> productDetails = flightRepo.findFlightByCity(cityOne, cityTwo, sort);
		return productDetails;
	}
	public List<ProductDetail> listAllFlights(String cityOne, String cityTwo, Date date, Sort sort) {
		
		List<ProductDetail> productDetails = flightRepo.findFlightByDateAndCity(date, cityOne, cityTwo, sort);
		return productDetails;
	}
	
	public List<ProductDetail> listAllFlightsByPrice(String cityOne, String cityTwo, Date date) {
		
		List<ProductDetail> productDetails = flightRepo.findFlightByDateAndCityPrice(date, cityOne, cityTwo);
		return productDetails;
	}
	
	public List<ProductDetail> listAllFlightsByArrival(String cityOne, String cityTwo, Date date) {
		
		List<ProductDetail> productDetails = flightRepo.findFlightByDateAndCityArrival(date, cityOne, cityTwo);
		return productDetails;
	}
	
	public List<ProductDetail> listAllFlightsByDeparture(String cityOne, String cityTwo, Date date) {
		
		List<ProductDetail> productDetails = flightRepo.findFlightByDateAndCityDeparture(date, cityOne, cityTwo);
		return productDetails;
	}
	
	public List<ProductDetail> findAllFlightsByBrand(String cityOne, String cityTwo, Date date, String brand1, String brand2) {
		
		List<ProductDetail> productDetails = flightRepo.findFlightByDateAndCityBrand(date, cityOne, cityTwo, brand1, brand2);
		return productDetails;
	}
	
	public List<ProductDetail> findAllFlightsByBrandSort(String cityOne, String cityTwo, Date date, String brand1, String brand2, Integer stop0, Integer stop1,  Integer stop2,  Integer stop3) {
		
		List<ProductDetail> productDetails = flightRepo.findFlightByDateAndCityBrandStopNum(date, cityOne, cityTwo, brand1, brand2, stop0, stop1, stop2, stop3, Sort.by("pnr").ascending());
		return productDetails;
	}
	
	public List<ProductDetail> findAllFlightsByStop(String cityOne, String cityTwo, Date date, Integer stop0, Integer stop1,  Integer stop2,  Integer stop3) {
		
		List<ProductDetail> productDetails = flightRepo.findFlightByDateAndCityStopNumber(date, cityOne, cityTwo, stop0, stop1, stop2, stop3);
		return productDetails;
	}  
	 
	public List<ProductDetail> findAllFlightsByTotalPrice(String cityOne, String cityTwo, Date date, Integer totalPrice) {
		float priceTotal = (float) totalPrice;
		List<ProductDetail> productDetails = flightRepo.findFlightByDateAndCitySelectedPrice(date, cityOne, cityTwo, priceTotal);
		return productDetails;
	}
	
	public List<ProductDetail> findAllFlightsByTotalPriceADT(String cityOne, String cityTwo, Date date, Integer totalPrice) {
		float priceTotal = (float) totalPrice;
		List<ProductDetail> productDetails = flightRepo.findFlightByDateAndCitySelectedPriceADT(date, cityOne, cityTwo, priceTotal);
		return productDetails;
	}
	
	public List<ProductDetail> findAllFlightsByTime(String cityOne, String cityTwo, Date date, float depTimeMinimum, float depTimeMaximum) {
		List<ProductDetail> productDetails = flightRepo.findFlightByDateAndCityTime(date, cityOne, cityTwo, depTimeMinimum, depTimeMaximum);
		return productDetails;
	}
	
	public List<ProductDetail> findAllFlightsByBrandTotalPriceADT(String cityOne, String cityTwo, Date date, String brand1, String brand2, Integer totalPrice) {
		float priceTotal = (float) totalPrice;
		List<ProductDetail> productDetails = flightRepo.findFlightByDateAndCityBrandPriceADT(date, cityOne, cityTwo, brand1, brand2, priceTotal);
		return productDetails;
	}

	public List<ProductDetail> findAllFlightsByStopNumTotalPriceADT(String cityOne, String cityTwo, Date date, Integer totalPrice, Integer stop0, Integer stop1,  Integer stop2,  Integer stop3) {	
		float priceTotal = (float) totalPrice;
		List<ProductDetail> productDetails = flightRepo.findFlightByDateAndCityStopNumPriceADT(date, cityOne, cityTwo, priceTotal, stop0, stop1, stop2, stop3);
		return productDetails;
	}
	
	public List<ProductDetail> findAllFlightsByBrandTotalPrice(String cityOne, String cityTwo, Date date, String brand1, String brand2, Integer totalPrice) {
		float priceTotal = (float) totalPrice;
		List<ProductDetail> productDetails = flightRepo.findFlightByDateAndCityBrandPrice(date, cityOne, cityTwo, brand1, brand2, priceTotal);
		return productDetails;
	}

	public List<ProductDetail> findAllFlightsByStopNumTotalPrice(String cityOne, String cityTwo, Date date, Integer totalPrice, Integer stop0, Integer stop1,  Integer stop2,  Integer stop3) {
		float priceTotal = (float) totalPrice;
		List<ProductDetail> productDetails = flightRepo.findFlightByDateAndCityStopNumPrice(date, cityOne, cityTwo, priceTotal, stop0, stop1, stop2, stop3);
		return productDetails;
	}
	
	public List<ProductDetail> findAllFlightsByBrandStopNumTotalPriceADT(String cityOne, String cityTwo, Date date, String brand1, String brand2, Integer stop0, Integer stop1,  Integer stop2,  Integer stop3, Integer totalPrice) {
		float priceTotal = (float) totalPrice;
		List<ProductDetail> productDetails = flightRepo.findFlightByDateAndCityBrandPriceADTStopNum(date, cityOne, cityTwo, brand1, brand2, priceTotal, stop0, stop1, stop2, stop3);
		return productDetails;
	}
	
	public List<ProductDetail> findAllFlightsByBrandStopNumTotalPrice(String cityOne, String cityTwo, Date date, String brand1, String brand2, Integer stop0, Integer stop1,  Integer stop2,  Integer stop3, Integer totalPrice) {
		float priceTotal = (float) totalPrice;
		List<ProductDetail> productDetails = flightRepo.findFlightByDateAndCityBrandPriceStopNum(date, cityOne, cityTwo, brand1, brand2, priceTotal, stop0, stop1, stop2, stop3);
		return productDetails;
	}
	
	public List<ProductDetail> findAllFlightsByTimeBrand(String cityOne, String cityTwo, Date date, String brand1, String brand2, float depTimeMinimum, float depTimeMaximum) {
		List<ProductDetail> productDetails = flightRepo.findFlightByDateAndCityTimeBrand(date, cityOne, cityTwo, depTimeMinimum, depTimeMaximum, brand1, brand2);
		return productDetails;
	}

	public List<ProductDetail> findAllFlightsByTimeStopNum(String cityOne, String cityTwo, Date date, Integer stop0, Integer stop1,  Integer stop2,  Integer stop3, float depTimeMinimum, float depTimeMaximum) {
		List<ProductDetail> productDetails = flightRepo.findFlightByDateAndCityTimeStopNum(date, cityOne, cityTwo, depTimeMinimum, depTimeMaximum, stop0, stop1, stop2, stop3);
		return productDetails;
	}
	
	public List<ProductDetail> findAllFlightsByTimePriceADT(String cityOne, String cityTwo, Date date, Integer totalPrice, float depTimeMinimum, float depTimeMaximum) {
		float priceTotal = (float) totalPrice;
		List<ProductDetail> productDetails = flightRepo.findFlightByDateAndCityTimePriceADT(date, cityOne, cityTwo, depTimeMinimum, depTimeMaximum, priceTotal);
		return productDetails;
	}
	
	public List<ProductDetail> findAllFlightsByTimePrice(String cityOne, String cityTwo, Date date, Integer totalPrice, float depTimeMinimum, float depTimeMaximum) {
		float priceTotal = (float) totalPrice;
		List<ProductDetail> productDetails = flightRepo.findFlightByDateAndCityTimePrice(date, cityOne, cityTwo, depTimeMinimum, depTimeMaximum, priceTotal);
		return productDetails;
	}
	
	public List<ProductDetail> findAllFlightsByTimeBrandStopNum(String cityOne, String cityTwo, Date date, String brand1, String brand2, float depTimeMinimum, float depTimeMaximum, 
			Integer stop0, Integer stop1,  Integer stop2,  Integer stop3) {
		List<ProductDetail> productDetails = flightRepo.findFlightByDateAndCityTimeBrandStop(date, cityOne, cityTwo, depTimeMinimum, depTimeMaximum, brand1, brand2, stop0, stop1, stop2, stop3);
		return productDetails;
	}

	public List<ProductDetail> findAllFlightsByTimeStopNumPriceADT(String cityOne, String cityTwo, Date date, Integer totalPrice, Integer stop0, Integer stop1,  Integer stop2,  Integer stop3, 
			float depTimeMinimum, float depTimeMaximum) {
		float priceTotal = (float) totalPrice;
		List<ProductDetail> productDetails = flightRepo.findFlightByDateAndCityTimePriceADTStop(date, cityOne, cityTwo, depTimeMinimum, depTimeMaximum, priceTotal, stop0, stop1, stop2, stop3);
		return productDetails;
	}
	
	public List<ProductDetail> findAllFlightsByTimePriceADTBrand(String cityOne, String cityTwo, Date date, Integer totalPrice, float depTimeMinimum, float depTimeMaximum, String brand1, String brand2) {
		float priceTotal = (float) totalPrice;
		List<ProductDetail> productDetails = flightRepo.findFlightByDateAndCityTimePriceADTBrand(date, cityOne, cityTwo, depTimeMinimum, depTimeMaximum, priceTotal, brand1, brand2);
		return productDetails;
	}
	
	public List<ProductDetail> findAllFlightsByTimeStopNumPrice(String cityOne, String cityTwo, Date date, Integer totalPrice, Integer stop0, Integer stop1,  Integer stop2,  Integer stop3, 
			float depTimeMinimum, float depTimeMaximum) {
		float priceTotal = (float) totalPrice;
		List<ProductDetail> productDetails = flightRepo.findFlightByDateAndCityTimePriceStop(date, cityOne, cityTwo, depTimeMinimum, depTimeMaximum, priceTotal, stop0, stop1, stop2, stop3);
		return productDetails;
	}
	
	public List<ProductDetail> findAllFlightsByTimePriceBrand(String cityOne, String cityTwo, Date date, Integer totalPrice, float depTimeMinimum, float depTimeMaximum, String brand1, String brand2) {
		float priceTotal = (float) totalPrice;
		List<ProductDetail> productDetails = flightRepo.findFlightByDateAndCityTimePriceBrand(date, cityOne, cityTwo, depTimeMinimum, depTimeMaximum, priceTotal, brand1, brand2);
		return productDetails;
	}
	
	public List<ProductDetail> findAllFlightsByTimePriceBrandStopNum(String cityOne, String cityTwo, Date date, Integer totalPrice, float depTimeMinimum, float depTimeMaximum, String brand1, String brand2, 
			Integer stop0, Integer stop1,  Integer stop2,  Integer stop3) {
		float priceTotal = (float) totalPrice;
		List<ProductDetail> productDetails = flightRepo.findFlightByDateAndCityTimeBrandStopPrice(date, cityOne, cityTwo, depTimeMinimum, depTimeMaximum, brand1, brand2, priceTotal, stop0, stop1, stop2, stop3);
		return productDetails;
	}
	
	public List<ProductDetail> findAllFlightsByTimePriceADTBrandStopNum(String cityOne, String cityTwo, Date date, Integer totalPrice, float depTimeMinimum, float depTimeMaximum, String brand1, String brand2, 
			Integer stop0, Integer stop1,  Integer stop2,  Integer stop3) {
		float priceTotal = (float) totalPrice;
		List<ProductDetail> productDetails = flightRepo.findFlightByDateAndCityTimeBrandStopPriceADT(date, cityOne, cityTwo, depTimeMinimum, depTimeMaximum, brand1, brand2, priceTotal, stop0, stop1, stop2, stop3);
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
