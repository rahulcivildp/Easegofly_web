package com.easygofly.search;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import com.easygofly.site.flight.SearchHistoryRepository;

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
	

	@Test
	public void testLog() {
		List<String> logs = new ArrayList<>();
		logs.add("9 7 50");
		logs.add("22 7 20");
		logs.add("33 7 50");
		logs.add("22 7 30");
		
		Integer threshold = 3;
		
		List<String> strThresold = new ArrayList<String>();
	    Map<String, Integer> mapStr = new TreeMap<>();
	    for (String log: logs) {
	        String[] logStr = log.split(" ");
			if (logStr[2].equals("0") || logStr[2].length() < 10) {
			
				List<String> listStrLog = new ArrayList<String>();
				listStrLog.add(logStr[0]);
				listStrLog.add(logStr[1]);
				
				for (String str : listStrLog) {
					if (!mapStr.containsKey(str)) {
						mapStr.put(str, 1);
					} else {
						mapStr.put(str, mapStr.get(str) + 1);
					}
				}
			}
	    }
	    
	    
	    for (Map.Entry<String, Integer> entry : mapStr.entrySet()) {
	        if(entry.getValue() >= threshold) {
	            strThresold.add(entry.getKey());
	        } 
	    }
	    
	    System.out.println(strThresold);
	    
	    System.out.println(mapStr);
		
	}
	

	@Test
	public void testGroup() {
		List<String> server = new ArrayList<>();
		server.add("3");
		server.add("3");
		
		List<Integer> replaceId  = new ArrayList<>();
		replaceId.add(3);
		replaceId.add(1);
		
		List<Integer> newId = new ArrayList<>();
		newId.add(1);
		newId.add(5);
		
		replaceId.forEach(id -> {
			if (server.contains(id.toString())) {
				int index = server.indexOf(id.toString());
				server.set(index, newId.get(index).toString());
			}
		});	
		
		System.out.println(server);
		
	}
}
