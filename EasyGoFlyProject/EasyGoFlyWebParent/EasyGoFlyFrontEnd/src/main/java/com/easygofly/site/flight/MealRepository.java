package com.easygofly.site.flight;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.easygofly.entity.MealsOnline;
import com.easygofly.entity.TravellerDetail;

public interface MealRepository extends CrudRepository<MealsOnline, Integer> {

	@Query("SELECT m FROM MealsOnline m WHERE m.travellerDetail = ?1")
	public MealsOnline findByTravellerDetail(TravellerDetail travellerDetail);

	@Query("SELECT m FROM MealsOnline m WHERE m.code = ?1")
	public MealsOnline findByCode(String code);
}
