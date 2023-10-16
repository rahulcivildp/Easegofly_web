package com.easygofly.site.flight;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easygofly.entity.BaggageOnline;
import com.easygofly.entity.MealsOnline;
import com.easygofly.entity.SeatsOnline;
import com.easygofly.entity.TravellerDetail;

@Service
public class ProductRestData {
	@Autowired private TravellerRepository travellerRepo;
	@Autowired private MealRepository mealRepo;
	@Autowired private BaggageRepository baggageRepo;
	@Autowired private SeatRepository seatRepo;

	public String findBaggageMethod(String code, List<BaggageOnline> baggageList) {
		List<BaggageOnline> baggageOnlineList = baggageList;
		String price = "";
		for (BaggageOnline baggageOnline : baggageOnlineList) {
			if (baggageOnline.getCode().equals(code)) {
				price = baggageOnline.getPrice();
			}
		}
		System.out.println(price);
		return price;
	}

	public String findMealMethod(String code, List<MealsOnline> meals) {
		List<MealsOnline> mealList = meals;
		String price = "";
		for (MealsOnline mealsOnline : mealList) {
			if (mealsOnline.getCode().equals(code)) {
				price = mealsOnline.getPrice();
			}
		}
		return price;
	}

	public String findSeatMethod(Integer id, List<SeatsOnline> seatsList) {
		List<SeatsOnline> seatsOnlineList = seatsList;
		String price = "";
		for (SeatsOnline seatsOnline : seatsOnlineList) {
			if (seatsOnline.getId() == id ) {
				price = seatsOnline.getPrice();
			}
		}
		return price;
	}

	public void mealsMethod(Integer id, String code, List<MealsOnline> meals) {
		TravellerDetail travellerDetail =  travellerRepo.findById(id).get();
		List<MealsOnline> mealsOnlines = meals;
		for (MealsOnline mealsOnline : mealsOnlines) {
			if (mealsOnline.getCode().equals(code) || mealsOnline.getCode() == code) {
				if (travellerDetail.getMeal() != null ) {
					MealsOnline meal = travellerDetail.getMealOnline();
					meal.setName(mealsOnline.getName());
					meal.setPrice(mealsOnline.getPrice());
					meal.setCode(mealsOnline.getCode());
					meal.setQuantity(mealsOnline.getQuantity());
					meal.setTravellerDetail(travellerDetail);
					
					 mealRepo.save(meal);
				} 
			} 
		}
	}

	public void baggageMethod(Integer id, String code, List<BaggageOnline> baggageList) {
		TravellerDetail travellerDetail =  travellerRepo.findById(id).get();
		List<BaggageOnline> baggageOnlineList = baggageList;
		for (BaggageOnline baggageOnline : baggageOnlineList) {
			if (baggageOnline.getCode().equals(code) || baggageOnline.getCode() == code) {
				if (travellerDetail.getBaggageOnline() != null) {
					BaggageOnline baggage = travellerDetail.getBaggageOnline();
					baggage.setPrice(baggageOnline.getPrice());
					baggage.setCode(baggageOnline.getCode());
					baggage.setWeight(baggageOnline.getWeight());
					baggage.setTravellerDetail(travellerDetail);
					
					baggageRepo.save(baggage);
				} 
			}
		}
	}

	public void seatMethod(Integer id, Integer seatId, List<SeatsOnline> seatsList) {
		TravellerDetail travellerDetail =  travellerRepo.findById(id).get();
		List<SeatsOnline> seatsOnlineList = seatsList;
		for (SeatsOnline seatsOnline : seatsOnlineList) {
			if (seatsOnline.getId() == seatId) {
				if (travellerDetail.getSeatsOnline() != null) {
					SeatsOnline seat = travellerDetail.getSeatsOnline();
					seat.setPrice(seatsOnline.getPrice());
					seat.setCompartment(seatsOnline.getCompartment());
					seat.setAvailablityType(seatsOnline.getAvailablityType());
					seat.setDeck(seatsOnline.getDeck());
					seat.setRowNo(seatsOnline.getRowNo());
					seat.setCode(seatsOnline.getCode());
					seat.setSeatType(seatsOnline.getSeatType());
					seat.setSeatNo(seatsOnline.getSeatNo());
					seat.setCraftType(seatsOnline.getCraftType());
					seat.setTravellerDetail(travellerDetail);
					
					 seatRepo.save(seat);
				} 
			}
		}
	}

	public void mealBaggageSeatMethod(Integer seatId, String mealCode, String baggageCode,
			TravellerDetail travellerDetail, List<MealsOnline> mealsList, List<BaggageOnline> baggageList, List<SeatsOnline>  seatsList) {
		List<MealsOnline> mealsOnlines = mealsList;
		for (MealsOnline mealsOnline : mealsOnlines) {
			if (mealsOnline.getCode().equals(mealCode) || mealsOnline.getCode() == mealCode) {
				if (travellerDetail.getMeal() == null ) {
					travellerDetail.addMeal(mealsOnline.getName(), mealsOnline.getPrice(), mealsOnline.getCode(), mealsOnline.getQuantity());
					 travellerRepo.save(travellerDetail);
				} 
			} 
		}
		
		List<BaggageOnline> baggageOnlineList = baggageList;
		for (BaggageOnline baggageOnline : baggageOnlineList) {
			if (baggageOnline.getCode().equals(baggageCode) || baggageOnline.getCode() == baggageCode) {
				if (travellerDetail.getBaggageOnline() == null) {
					BaggageOnline baggage = new BaggageOnline(baggageOnline.getPrice(), baggageOnline.getCode(), baggageOnline.getWeight(), travellerDetail);
					travellerDetail.setBaggageOnline(baggage);
					 travellerRepo.save(travellerDetail);
				}
			} 
		}
		
		List<SeatsOnline> seatsOnlineList = seatsList;
		for (SeatsOnline seatsOnline : seatsOnlineList) {
			if (seatsOnline.getId() == seatId) {
				if (travellerDetail.getSeatsOnline() == null) {
					SeatsOnline seat = new SeatsOnline(seatsOnline.getPrice(), seatsOnline.getCompartment(), seatsOnline.getAvailablityType(), seatsOnline.getDeck(), 
							seatsOnline.getRowNo(), seatsOnline.getCode(), seatsOnline.getSeatType(), seatsOnline.getSeatNo(), seatsOnline.getCraftType(), travellerDetail);
					travellerDetail.setSeatsOnline(seat);
					 travellerRepo.save(travellerDetail);
				} 
			} 
		}
	}
}