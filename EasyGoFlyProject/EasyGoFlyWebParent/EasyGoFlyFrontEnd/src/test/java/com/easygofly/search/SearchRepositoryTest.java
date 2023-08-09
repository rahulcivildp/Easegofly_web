package com.easygofly.search;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;

import com.easygofly.entity.CartItem;
import com.easygofly.entity.Customer;
import com.easygofly.entity.MealsOnline;
import com.easygofly.entity.ProductDetail;
import com.easygofly.entity.SearchHistory;
import com.easygofly.entity.TravellerDetail;
import com.easygofly.site.EasyGoFlyFrontEndApplication;
import com.easygofly.site.flight.MealRepository;
import com.easygofly.site.flight.TravellerRepository;
import com.easygofly.site.search.SearchHistoryRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
@ContextConfiguration(classes = EasyGoFlyFrontEndApplication.class)
public class SearchRepositoryTest {

	@Autowired private SearchHistoryRepository repo;
	@Autowired private TestEntityManager entityManager;
	@Autowired private MealRepository mealRepo;
	@Autowired private TravellerRepository travellerRepo;
	
	@Test
	public void createSearchHistory() {
		Customer customer = entityManager.find(Customer.class, 6);
		
		SearchHistory searchHistory = new SearchHistory();
		searchHistory.setCityOne("CCU");
		searchHistory.setCityTwo("BLR");
		searchHistory.setJourneyClass("Economy");
		searchHistory.setTripType("oneWay");
		searchHistory.setPassengerNum(8);
		searchHistory.setAdultNum(3);
		searchHistory.setChildNum(4);
		searchHistory.setInfantNum(1);
		searchHistory.setDate(new Date());
		searchHistory.setCustomer(customer);
		
		
		SearchHistory savedHistory = repo.save(searchHistory);
		
		assertThat(savedHistory).isNotNull();
		assertThat(savedHistory.getId()).isGreaterThan(0);
	}

	@Test
	public void deleteSearchResult() {
		Customer customer = entityManager.find(Customer.class, 6);
		
		Iterable<SearchHistory> search = customer.getSearchHistory();
		SearchHistory firstHistory = search.iterator().next();
		Integer id = firstHistory.getId();
		Integer count = extracted(search);
		System.out.println(count);
		for (int i=1;i<count;i++) {
			if (count >= 9) {
				System.out.println(firstHistory.getId());
				repo.deleteSearchByCustomer(id, customer);
				count = count - 1;
				id++;
				System.out.println(count);
			}
		}
	}
	

	@Test
	public void deleteSearchResultByCustomer() {
		Customer customer = entityManager.find(Customer.class, 6);
		int id = 30;
		repo.deleteSearchByCustomer(id, customer);
	}
	
	private Integer extracted(Iterable<SearchHistory> data) {
		int counter = 0;
		for (@SuppressWarnings("unused") Object i : data) {
		    counter++;
		}
		
		return counter;
	}
	
	@Test
	public void setTravelerMeal() {
		Date date = new Date();
		ProductDetail productDetail = entityManager.find(ProductDetail.class, 208);
		CartItem cartItem = entityManager.find(CartItem.class, 248);
		TravellerDetail travellerDetail = new TravellerDetail("Mr", "Tanmay", "Sarkar", date, productDetail, cartItem, "1", 15, 7);
//		MealsOnline mealsOnline = new MealsOnline("No Meal", "0", "NoMeal", "1", travellerDetail);
		travellerDetail.addMeal("No Meal", "0", "NoMeal", "0");
		
		TravellerDetail savedTraveler = travellerRepo.save(travellerDetail);
		MealsOnline neawMeal = savedTraveler.getMealOnline();
		System.out.println(neawMeal.getName());
		
	}
}
