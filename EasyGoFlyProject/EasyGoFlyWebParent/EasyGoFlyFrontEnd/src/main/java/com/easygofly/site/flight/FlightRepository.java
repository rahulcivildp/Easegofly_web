package com.easygofly.site.flight;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.easygofly.entity.ProductDetail;

public interface FlightRepository extends CrudRepository<ProductDetail, Integer> {

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
			+ "( p.brand = :brand1 OR p.brand = :brand2)") 
	public List<ProductDetail> findFlightByDateAndCityBrand(Date date, String cityOne, String cityTwo, String brand1, String brand2, Sort ascending);
	
	@Query("SELECT p FROM ProductDetail p WHERE p.date = :date AND p.cityOne = :cityOne AND p.cityTwo = :cityTwo AND "
			+ "( p.brand = :brand1 OR p.brand = :brand2) AND (p.stopNum = :stop0 OR p.stopNum = :stop1 OR p.stopNum = :stop2 OR p.stopNum = :stop3 )") 
	public List<ProductDetail> findFlightByDateAndCityBrandSort(Date date, String cityOne, String cityTwo, String brand1, String brand2, Integer stop0, Integer stop1, Integer stop2, Integer stop3, Sort ascending);
	
	@Query("SELECT p FROM ProductDetail p WHERE p.date = :date AND p.cityOne = :cityOne AND p.cityTwo = :cityTwo AND (p.stopNum = :stop0 OR p.stopNum = :stop1 OR p.stopNum = :stop2 OR p.stopNum = :stop3 )")
	public List<ProductDetail> findFlightByDateAndCityStopNumber(Date date, String cityOne, String cityTwo, Integer stop0, Integer stop1, Integer stop2, Integer stop3, Sort ascending);
} 
