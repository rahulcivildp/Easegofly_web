package com.easygofly.site.flight;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.easygofly.entity.ProductDetail;

public interface FlightRepository extends CrudRepository<ProductDetail, Integer> {
	
	@Query("SELECT p FROM ProductDetail p WHERE p.id = :id AND p.mode = :mode")
	public ProductDetail findProductDetailByIdMode(Integer id, String mode);
	
	@Query("SELECT p FROM ProductDetail p WHERE p.cityOne = :cityOne AND p.cityTwo = :cityTwo")
	public List<ProductDetail> findFlightByCity(String cityOne, String cityTwo, Sort ascending);
	
	@Query("SELECT p FROM ProductDetail p WHERE p.cityOne = :cityOne AND p.cityTwo = :cityTwo AND p.brand = :brand")
	public List<ProductDetail> findFlightByCityAndBrand(String cityOne, String cityTwo, String brand, Sort ascending);

	@Query("SELECT p FROM ProductDetail p WHERE p.date = :date AND p.cityOne = :cityOne AND p.cityTwo = :cityTwo")
	public List<ProductDetail> findFlightByDateAndCity(Date date, String cityOne, String cityTwo, Sort ascending);
	
	@Query("SELECT p FROM ProductDetail p WHERE p.date = :date AND p.cityOne = :cityOne AND p.cityTwo = :cityTwo ORDER BY (priceADT + priceINF + markupADT + markupINF) + 0 ASC")
	public List<ProductDetail> findFlightByDateAndCityPrice(Date date, String cityOne, String cityTwo);
	
	@Query("SELECT p FROM ProductDetail p WHERE p.date = :date AND p.cityOne = :cityOne AND p.cityTwo = :cityTwo ORDER BY p.arrTime + 0 ASC")
	public List<ProductDetail> findFlightByDateAndCityArrival(Date date, String cityOne, String cityTwo);
	
	@Query("SELECT p FROM ProductDetail p WHERE p.date = :date AND p.cityOne = :cityOne AND p.cityTwo = :cityTwo ORDER BY p.depTime + 0 ASC")
	public List<ProductDetail> findFlightByDateAndCityDeparture(Date date, String cityOne, String cityTwo);
	
	@Query("SELECT p FROM ProductDetail p WHERE p.date = :date AND p.cityOne = :cityOne AND p.cityTwo = :cityTwo ORDER BY p.stopNum + 0 ASC")
	public List<ProductDetail> findFlightByDateAndCityStop(Date date, String cityOne, String cityTwo);
	
	@Query("SELECT p FROM ProductDetail p WHERE p.date = :date AND p.cityOne = :cityOne AND p.cityTwo = :cityTwo AND "
			+ "( p.brand = :brand1 OR p.brand = :brand2) ORDER BY brand ASC") 
	public List<ProductDetail> findFlightByDateAndCityBrand(Date date, String cityOne, String cityTwo, String brand1, String brand2);
	
	@Query("SELECT p FROM ProductDetail p WHERE p.date = :date AND p.cityOne = :cityOne AND p.cityTwo = :cityTwo AND "
			+ "( p.brand = :brand1 OR p.brand = :brand2) AND (p.stopNum = :stop0 OR p.stopNum = :stop1 OR p.stopNum = :stop2 OR p.stopNum = :stop3 )") 
	public List<ProductDetail> findFlightByDateAndCityBrandStopNum(Date date, String cityOne, String cityTwo, String brand1, String brand2, Integer stop0, Integer stop1, Integer stop2, Integer stop3, Sort ascending);
	
	@Query("SELECT p FROM ProductDetail p WHERE p.date = :date AND p.cityOne = :cityOne AND p.cityTwo = :cityTwo AND (p.stopNum = :stop0 OR p.stopNum = :stop1 OR p.stopNum = :stop2 OR p.stopNum = :stop3) "
			+ "ORDER BY stopNum ASC")
	public List<ProductDetail> findFlightByDateAndCityStopNumber(Date date, String cityOne, String cityTwo, Integer stop0, Integer stop1, Integer stop2, Integer stop3);
	
	@Query("SELECT p FROM ProductDetail p WHERE p.date = :date AND p.cityOne = :cityOne AND p.cityTwo = :cityTwo AND (priceADT + priceINF + markupADT + markupINF) = :totalPrice "
			+ "ORDER BY (priceADT + priceINF + markupADT + markupINF) + 0 ASC") 
	public List<ProductDetail> findFlightByDateAndCitySelectedPrice(Date date, String cityOne, String cityTwo, Float totalPrice);
	
	@Query("SELECT p FROM ProductDetail p WHERE p.date = :date AND p.cityOne = :cityOne AND p.cityTwo = :cityTwo AND (priceADT + markupADT) <= :totalPrice ORDER BY (priceADT + markupADT ) + 0 ASC") 
	public List<ProductDetail> findFlightByDateAndCitySelectedPriceADT(Date date, String cityOne, String cityTwo, Float totalPrice);
	
	@Query("SELECT p FROM ProductDetail p WHERE p.date = :date AND p.cityOne = :cityOne AND p.cityTwo = :cityTwo AND "
			+ "( p.brand = :brand1 OR p.brand = :brand2) AND (priceADT + markupADT) <= :totalPrice ORDER BY (priceADT + markupADT ) + 0 ASC") 
	public List<ProductDetail> findFlightByDateAndCityBrandPriceADT(Date date, String cityOne, String cityTwo, String brand1, String brand2, Float totalPrice);
	
	@Query("SELECT p FROM ProductDetail p WHERE p.date = :date AND p.cityOne = :cityOne AND p.cityTwo = :cityTwo AND "
			+ "( p.brand = :brand1 OR p.brand = :brand2) AND (priceADT + priceINF + markupADT + markupINF) <= :totalPrice ORDER BY (priceADT + priceINF + markupADT + markupINF) + 0 ASC") 
	public List<ProductDetail> findFlightByDateAndCityBrandPrice(Date date, String cityOne, String cityTwo, String brand1, String brand2, Float totalPrice);
	
	@Query("SELECT p FROM ProductDetail p WHERE p.date = :date AND p.cityOne = :cityOne AND p.cityTwo = :cityTwo AND "
			+ "(p.stopNum = :stop0 OR p.stopNum = :stop1 OR p.stopNum = :stop2 OR p.stopNum = :stop3) AND (priceADT + markupADT) <= :totalPrice ORDER BY (priceADT  + markupADT) + 0 ASC") 
	public List<ProductDetail> findFlightByDateAndCityStopNumPriceADT(Date date, String cityOne, String cityTwo, Float totalPrice, Integer stop0, Integer stop1, Integer stop2, Integer stop3);
	
	@Query("SELECT p FROM ProductDetail p WHERE p.date = :date AND p.cityOne = :cityOne AND p.cityTwo = :cityTwo AND "
			+ "(p.stopNum = :stop0 OR p.stopNum = :stop1 OR p.stopNum = :stop2 OR p.stopNum = :stop3) AND (priceADT + priceINF + markupADT + markupINF) <= :totalPrice ORDER BY (priceADT + priceINF + markupADT + markupINF) + 0 ASC") 
	public List<ProductDetail> findFlightByDateAndCityStopNumPrice(Date date, String cityOne, String cityTwo, Float totalPrice, Integer stop0, Integer stop1, Integer stop2, Integer stop3);
	
	@Query("SELECT p FROM ProductDetail p WHERE p.date = :date AND p.cityOne = :cityOne AND p.cityTwo = :cityTwo AND "
			+ "( p.brand = :brand1 OR p.brand = :brand2) AND (priceADT + markupADT) <= :totalPrice AND (p.stopNum = :stop0 OR p.stopNum = :stop1 OR p.stopNum = :stop2 OR p.stopNum = :stop3) "
			+ "ORDER BY (priceADT + markupADT ) + 0 ASC") 
	public List<ProductDetail> findFlightByDateAndCityBrandPriceADTStopNum(Date date, String cityOne, String cityTwo, String brand1, String brand2, Float totalPrice, Integer stop0, Integer stop1, Integer stop2, Integer stop3);

	@Query("SELECT p FROM ProductDetail p WHERE p.date = :date AND p.cityOne = :cityOne AND p.cityTwo = :cityTwo AND "
			+ "( p.brand = :brand1 OR p.brand = :brand2) AND (priceADT + priceINF + markupADT + markupINF) <= :totalPrice AND (p.stopNum = :stop0 OR p.stopNum = :stop1 OR p.stopNum = :stop2 OR p.stopNum = :stop3) "
			+ "ORDER BY (priceADT + priceINF + markupADT + markupINF) + 0 ASC") 
	public List<ProductDetail> findFlightByDateAndCityBrandPriceStopNum(Date date, String cityOne, String cityTwo, String brand1, String brand2, Float totalPrice, Integer stop0, Integer stop1, Integer stop2, Integer stop3);

	@Query("SELECT p FROM ProductDetail p WHERE p.date = :date AND p.cityOne = :cityOne AND p.cityTwo = :cityTwo AND "
			+ "depTimeInteger >= :depTimeMinimum AND depTimeInteger <= :depTimeMaximum ORDER BY (priceADT + priceINF + markupADT + markupINF) + 0 ASC")
	public List<ProductDetail> findFlightByDateAndCityTime(Date date, String cityOne, String cityTwo, float depTimeMinimum, float depTimeMaximum);
	
	@Query("SELECT p FROM ProductDetail p WHERE p.date = :date AND p.cityOne = :cityOne AND p.cityTwo = :cityTwo AND ( p.brand = :brand1 OR p.brand = :brand2) AND "
			+ "depTimeInteger >= :depTimeMinimum AND depTimeInteger <= :depTimeMaximum ORDER BY (priceADT + priceINF + markupADT + markupINF) + 0 ASC")
	public List<ProductDetail> findFlightByDateAndCityTimeBrand(Date date, String cityOne, String cityTwo, float depTimeMinimum, float depTimeMaximum, String brand1, String brand2);
	
	@Query("SELECT p FROM ProductDetail p WHERE p.date = :date AND p.cityOne = :cityOne AND p.cityTwo = :cityTwo AND (p.stopNum = :stop0 OR p.stopNum = :stop1 OR p.stopNum = :stop2 OR p.stopNum = :stop3) AND "
			+ "depTimeInteger >= :depTimeMinimum AND depTimeInteger <= :depTimeMaximum ORDER BY (priceADT + priceINF + markupADT + markupINF) + 0 ASC")
	public List<ProductDetail> findFlightByDateAndCityTimeStopNum(Date date, String cityOne, String cityTwo, float depTimeMinimum, float depTimeMaximum, Integer stop0, Integer stop1, Integer stop2, Integer stop3);
	
	@Query("SELECT p FROM ProductDetail p WHERE p.date = :date AND p.cityOne = :cityOne AND p.cityTwo = :cityTwo AND (priceADT + markupADT) <= :totalPrice AND "
			+ "depTimeInteger >= :depTimeMinimum AND depTimeInteger <= :depTimeMaximum ORDER BY (priceADT + markupADT) + 0 ASC")
	public List<ProductDetail> findFlightByDateAndCityTimePriceADT(Date date, String cityOne, String cityTwo, float depTimeMinimum, float depTimeMaximum, Float totalPrice);
	
	@Query("SELECT p FROM ProductDetail p WHERE p.date = :date AND p.cityOne = :cityOne AND p.cityTwo = :cityTwo AND (priceADT + priceINF + markupADT + markupINF) <= :totalPrice AND "
			+ "depTimeInteger >= :depTimeMinimum AND depTimeInteger <= :depTimeMaximum ORDER BY (priceADT + priceINF + markupADT + markupINF) + 0 ASC")
	public List<ProductDetail> findFlightByDateAndCityTimePrice(Date date, String cityOne, String cityTwo, float depTimeMinimum, float depTimeMaximum, Float totalPrice);

	@Query("SELECT p FROM ProductDetail p WHERE p.date = :date AND p.cityOne = :cityOne AND p.cityTwo = :cityTwo AND (priceADT + markupADT) <= :totalPrice AND "
			+ "( p.brand = :brand1 OR p.brand = :brand2) AND depTimeInteger >= :depTimeMinimum AND depTimeInteger <= :depTimeMaximum ORDER BY (priceADT + markupADT) + 0 ASC")
	public List<ProductDetail> findFlightByDateAndCityTimePriceADTBrand(Date date, String cityOne, String cityTwo, float depTimeMinimum, float depTimeMaximum, Float totalPrice, String brand1, String brand2);

	@Query("SELECT p FROM ProductDetail p WHERE p.date = :date AND p.cityOne = :cityOne AND p.cityTwo = :cityTwo AND (priceADT + priceINF + markupADT + markupINF) <= :totalPrice AND "
			+ "( p.brand = :brand1 OR p.brand = :brand2) AND depTimeInteger >= :depTimeMinimum AND depTimeInteger <= :depTimeMaximum ORDER BY (priceADT + priceINF + markupADT + markupINF) + 0 ASC")
	public List<ProductDetail> findFlightByDateAndCityTimePriceBrand(Date date, String cityOne, String cityTwo, float depTimeMinimum, float depTimeMaximum, Float totalPrice, String brand1, String brand2);

	@Query("SELECT p FROM ProductDetail p WHERE p.date = :date AND p.cityOne = :cityOne AND p.cityTwo = :cityTwo AND (priceADT + markupADT) <= :totalPrice AND "
			+ "(p.stopNum = :stop0 OR p.stopNum = :stop1 OR p.stopNum = :stop2 OR p.stopNum = :stop3) AND depTimeInteger >= :depTimeMinimum AND "
			+ "depTimeInteger <= :depTimeMaximum ORDER BY (priceADT + markupADT) + 0 ASC")
	public List<ProductDetail> findFlightByDateAndCityTimePriceADTStop(Date date, String cityOne, String cityTwo, float depTimeMinimum, float depTimeMaximum, Float totalPrice, 
			Integer stop0, Integer stop1, Integer stop2, Integer stop3);
	
	@Query("SELECT p FROM ProductDetail p WHERE p.date = :date AND p.cityOne = :cityOne AND p.cityTwo = :cityTwo AND (priceADT + priceINF + markupADT + markupINF) <= :totalPrice AND "
			+ "(p.stopNum = :stop0 OR p.stopNum = :stop1 OR p.stopNum = :stop2 OR p.stopNum = :stop3) AND depTimeInteger >= :depTimeMinimum AND "
			+ "depTimeInteger <= :depTimeMaximum ORDER BY (priceADT + priceINF + markupADT + markupINF) + 0 ASC")
	public List<ProductDetail> findFlightByDateAndCityTimePriceStop(Date date, String cityOne, String cityTwo, float depTimeMinimum, float depTimeMaximum, Float totalPrice, 
			Integer stop0, Integer stop1, Integer stop2, Integer stop3);
	
	@Query("SELECT p FROM ProductDetail p WHERE p.date = :date AND p.cityOne = :cityOne AND p.cityTwo = :cityTwo AND "
			+ "(p.stopNum = :stop0 OR p.stopNum = :stop1 OR p.stopNum = :stop2 OR p.stopNum = :stop3) AND ( p.brand = :brand1 OR p.brand = :brand2) AND depTimeInteger >= :depTimeMinimum AND "
			+ "depTimeInteger <= :depTimeMaximum ORDER BY (priceADT + markupADT) + 0 ASC")
	public List<ProductDetail> findFlightByDateAndCityTimeBrandStop(Date date, String cityOne, String cityTwo, float depTimeMinimum, float depTimeMaximum, String brand1, String brand2, 
			Integer stop0, Integer stop1, Integer stop2, Integer stop3);
	
	@Query("SELECT p FROM ProductDetail p WHERE p.date = :date AND p.cityOne = :cityOne AND p.cityTwo = :cityTwo AND (priceADT + markupADT) <= :totalPrice AND"
			+ "(p.stopNum = :stop0 OR p.stopNum = :stop1 OR p.stopNum = :stop2 OR p.stopNum = :stop3) AND ( p.brand = :brand1 OR p.brand = :brand2) AND depTimeInteger >= :depTimeMinimum AND "
			+ "depTimeInteger <= :depTimeMaximum ORDER BY (priceADT + markupADT) + 0 ASC")
	public List<ProductDetail> findFlightByDateAndCityTimeBrandStopPriceADT(Date date, String cityOne, String cityTwo, float depTimeMinimum, float depTimeMaximum, String brand1, String brand2, Float totalPrice, 
			Integer stop0, Integer stop1, Integer stop2, Integer stop3);

	@Query("SELECT p FROM ProductDetail p WHERE p.date = :date AND p.cityOne = :cityOne AND p.cityTwo = :cityTwo AND (priceADT + priceINF + markupADT + markupINF) <= :totalPrice AND"
			+ "(p.stopNum = :stop0 OR p.stopNum = :stop1 OR p.stopNum = :stop2 OR p.stopNum = :stop3) AND ( p.brand = :brand1 OR p.brand = :brand2) AND depTimeInteger >= :depTimeMinimum AND "
			+ "depTimeInteger <= :depTimeMaximum ORDER BY (priceADT + priceINF + markupADT + markupINF) + 0 ASC")
	public List<ProductDetail> findFlightByDateAndCityTimeBrandStopPrice(Date date, String cityOne, String cityTwo, float depTimeMinimum, float depTimeMaximum, String brand1, String brand2, Float totalPrice, 
			Integer stop0, Integer stop1, Integer stop2, Integer stop3);
} 
