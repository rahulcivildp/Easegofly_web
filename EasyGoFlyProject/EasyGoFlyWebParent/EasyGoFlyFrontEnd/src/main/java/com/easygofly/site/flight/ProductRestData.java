package com.easygofly.site.flight;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easygofly.entity.BaggageOnline;
import com.easygofly.entity.CheckoutInfo;
import com.easygofly.entity.MealsOnline;
import com.easygofly.entity.SeatsOnline;
import com.easygofly.entity.TravellerDetail;
import com.easygofly.site.checkout.CheckOutRepository;

@Service
public class ProductRestData {
	@Autowired private TravellerRepository travellerRepo;
	@Autowired private MealRepository mealRepo;
	@Autowired private BaggageRepository baggageRepo;
	@Autowired private SeatRepository seatRepo;
	@Autowired private CheckOutRepository checkOutRepo;

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

	public void mealsMethod(Integer id, String code, List<MealsOnline> meals, Integer checkout_id) {
		CheckoutInfo checkoutInfo = checkOutRepo.findById(checkout_id).get();
		TravellerDetail travellerDetail =  travellerRepo.findById(id).get();
		List<MealsOnline> mealsOnlines = meals;
		
		for (MealsOnline mealsOnline : mealsOnlines) {
			if (mealsOnline.getCode().equals(code) || mealsOnline.getCode() == code) {
				if (travellerDetail.getMealOnline() != null ) {
					MealsOnline meal = travellerDetail.getMealOnline();
					
					double calculateMeal = checkoutUpdate(checkoutInfo.getMeal(), mealsOnline.getPrice(), meal.getPrice());
					checkoutInfo.setMeal(calculateMeal);
					checkOutRepo.save(checkoutInfo);
					
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

	public void baggageMethod(Integer id, String code, List<BaggageOnline> baggageList, Integer checkout_id) {
		CheckoutInfo checkoutInfo = checkOutRepo.findById(checkout_id).get();
		TravellerDetail travellerDetail =  travellerRepo.findById(id).get();
		List<BaggageOnline> baggageOnlineList = baggageList;
		
		for (BaggageOnline baggageOnline : baggageOnlineList) {
			if (baggageOnline.getCode().equals(code) || baggageOnline.getCode() == code) {
				if (travellerDetail.getBaggageOnline() != null) {
					BaggageOnline baggage = travellerDetail.getBaggageOnline();
					
					double calculateBaggage = checkoutUpdate(checkoutInfo.getBaggage(), baggageOnline.getPrice(), baggage.getPrice());
					checkoutInfo.setBaggage(calculateBaggage);
					checkOutRepo.save(checkoutInfo);
					
					baggage.setPrice(baggageOnline.getPrice());
					baggage.setCode(baggageOnline.getCode());
					baggage.setWeight(baggageOnline.getWeight());
					baggage.setTravellerDetail(travellerDetail);
					
					baggageRepo.save(baggage);
				} 
			}
		}
	}

	public void seatMethod(Integer id, String code, List<SeatsOnline> seatsList, Integer checkout_id) {
		CheckoutInfo checkoutInfo = checkOutRepo.findById(checkout_id).get();
		TravellerDetail travellerDetail =  travellerRepo.findById(id).get();
		List<SeatsOnline> seatsOnlineList = seatsList;
		
		for (SeatsOnline seatsOnline : seatsOnlineList) {
			if (seatsOnline.getCode().equals(code) || seatsOnline.getCode() == code) {
				if (travellerDetail.getSeatsOnline() != null) {
					SeatsOnline seat = travellerDetail.getSeatsOnline();
					
					double calculateSeat = checkoutUpdate(checkoutInfo.getSeat(), seatsOnline.getPrice(), seat.getPrice());
					checkoutInfo.setSeat(calculateSeat);
					checkOutRepo.save(checkoutInfo);
					
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

	public void mealBaggageSeatMethod(TravellerDetail travellerDetail, List<MealsOnline> mealsList, List<BaggageOnline> baggageList, List<SeatsOnline>  seatsList, Integer checkout_id) {
		CheckoutInfo checkoutInfo = checkOutRepo.findById(checkout_id).get();
		List<MealsOnline> mealsOnlines = mealsList;
		for (MealsOnline mealsOnline : mealsOnlines) {
			if (mealsOnline.getCode().equals("NoMeal") || mealsOnline.getCode() == "NoMeal") {
				if (travellerDetail.getMealOnline() == null ) {
					travellerDetail.addMeal(mealsOnline.getName(), mealsOnline.getPrice(), mealsOnline.getCode(), mealsOnline.getQuantity());
					travellerRepo.save(travellerDetail);
					
					double calculateMeal = checkoutUpdate(checkoutInfo.getMeal(), mealsOnline.getPrice(), travellerDetail.getMealOnline().getPrice());
					checkoutInfo.setMeal(calculateMeal);
				} else {
					MealsOnline meal = travellerDetail.getMealOnline();
					travellerDetail.setMealOnline(mealsOnline);
					travellerRepo.save(travellerDetail);
					
					double calculateMeal = checkoutUpdate(checkoutInfo.getMeal(), mealsOnline.getPrice(), meal.getPrice());
					checkoutInfo.setMeal(calculateMeal);
				}
			} 
		}
		
		List<BaggageOnline> baggageOnlineList = baggageList;
		for (BaggageOnline baggageOnline : baggageOnlineList) {
			if (baggageOnline.getCode().equals("NoBaggage") || baggageOnline.getCode() == "NoBaggage") {
				if (travellerDetail.getBaggageOnline() == null) {
					BaggageOnline baggage = new BaggageOnline(baggageOnline.getPrice(), baggageOnline.getCode(), baggageOnline.getWeight(), travellerDetail);
					travellerDetail.setBaggageOnline(baggage);
					travellerRepo.save(travellerDetail);
					
					double calculateBaggage = checkoutUpdate(checkoutInfo.getBaggage(), baggageOnline.getPrice(), travellerDetail.getBaggageOnline().getPrice());
					checkoutInfo.setBaggage(calculateBaggage);
				} else {
					BaggageOnline baggage = travellerDetail.getBaggageOnline();
					travellerDetail.setBaggageOnline(baggageOnline);
					travellerRepo.save(travellerDetail);
					
					double calculateBaggage = checkoutUpdate(checkoutInfo.getBaggage(), baggageOnline.getPrice(), baggage.getPrice());
					checkoutInfo.setBaggage(calculateBaggage);
				}
			} 
		}
		
		List<SeatsOnline> seatsOnlineList = seatsList;
		for (SeatsOnline seatsOnline : seatsOnlineList) {
			if (seatsOnline.getCode().equals("NoSeat") || seatsOnline.getCode() == "NoSeat") {
				if (travellerDetail.getSeatsOnline() == null) {
					SeatsOnline seat = new SeatsOnline(seatsOnline.getPrice(), seatsOnline.getCompartment(), seatsOnline.getAvailablityType(), seatsOnline.getDeck(), 
							seatsOnline.getRowNo(), seatsOnline.getCode(), seatsOnline.getSeatType(), seatsOnline.getSeatNo(), seatsOnline.getCraftType(), travellerDetail);
					travellerDetail.setSeatsOnline(seat);
					travellerRepo.save(travellerDetail);
					
					double calculateSeat = checkoutUpdate(checkoutInfo.getSeat(), seatsOnline.getPrice(), seat.getPrice());
					checkoutInfo.setSeat(calculateSeat);
				}  else {
					SeatsOnline seat = travellerDetail.getSeatsOnline();
					travellerDetail.setSeatsOnline(seatsOnline);
					travellerRepo.save(travellerDetail);
					
					double calculateSeat = checkoutUpdate(checkoutInfo.getSeat(), seatsOnline.getPrice(), seat.getPrice());
					checkoutInfo.setSeat(calculateSeat);
				}
			} 
		}
		checkOutRepo.save(checkoutInfo);
	}

	public String checkoutMentod(Integer checkout_id) {
		CheckoutInfo checkout = checkOutRepo.findById(checkout_id).get();
		
		double flightCost = checkout.getFlightCost();
		double flightServiceCost = checkout.getFlightServiceCost();
		double flightGSTCost = checkout.getFlightGSTCost();
		double meal = checkout.getMeal();
		double seat = checkout.getSeat();
		double baggage = checkout.getBaggage();
		double paymentTotal = flightCost + flightServiceCost + flightGSTCost + meal + seat + baggage;
		
		checkout.setPaymentTotal(paymentTotal);
		checkOutRepo.save(checkout);
		
		String response = "{\r\n"
				+ "      \"flightCost\": " + flightCost + ",\r\n"
				+ "      \"flightServiceCost\": " + flightServiceCost + ",\r\n"
				+ "      \"flightGSTCost\": " + flightGSTCost + ",\r\n"
				+ "      \"meal\": " + meal + ",\r\n"
				+ "      \"seat\": " + seat + ",\r\n"
				+ "      \"baggage\": " + baggage + ",\r\n"
				+ "      \"paymentTotal\": " + paymentTotal + "\r\n"
				+ "    }";
		
		return response;
	}

	public String checkoutReturnMentod(Integer checkoutOne_id, Integer checkoutTwo_id) {
		CheckoutInfo checkoutInfo1 = checkOutRepo.findById(checkoutOne_id).get();
		CheckoutInfo checkoutInfo2 = checkOutRepo.findById(checkoutTwo_id).get();
		
		double flightCost = checkoutInfo1.getFlightCost() + checkoutInfo2.getFlightCost();
		double flightServiceCost = checkoutInfo1.getFlightServiceCost() + checkoutInfo2.getFlightServiceCost();
		double flightGSTCost = checkoutInfo1.getFlightGSTCost() + checkoutInfo2.getFlightGSTCost();
		double meal = checkoutInfo1.getMeal() + checkoutInfo2.getMeal();
		double seat = checkoutInfo1.getSeat() + checkoutInfo2.getSeat();
		double baggage = checkoutInfo1.getBaggage() + checkoutInfo2.getBaggage();
		double paymentTotal = flightCost + flightServiceCost + flightGSTCost + meal + seat + baggage;
		double paymentOne = checkoutInfo1.getFlightCost() + checkoutInfo1.getFlightServiceCost() + checkoutInfo1.getFlightGSTCost() + checkoutInfo1.getMeal() + checkoutInfo1.getBaggage() + checkoutInfo1.getSeat();
		double paymentTwo = checkoutInfo2.getFlightCost() + checkoutInfo2.getFlightServiceCost() + checkoutInfo2.getFlightGSTCost() + checkoutInfo2.getMeal() + checkoutInfo2.getBaggage() + checkoutInfo2.getSeat();
		
		checkoutInfo1.setPaymentTotal(paymentOne);
		checkOutRepo.save(checkoutInfo1);
		checkoutInfo2.setPaymentTotal(paymentTwo);
		checkOutRepo.save(checkoutInfo2);
		
		String response = "{\r\n"
				+ "      \"flightCost\": " + flightCost + ",\r\n"
				+ "      \"flightServiceCost\": " + flightServiceCost + ",\r\n"
				+ "      \"flightGSTCost\": " + flightGSTCost + ",\r\n"
				+ "      \"meal\": " + meal + ",\r\n"
				+ "      \"seat\": " + seat + ",\r\n"
				+ "      \"baggage\": " + baggage + ",\r\n"
				+ "      \"paymentTotal\": " + paymentTotal + "\r\n"
				+ "    }";
		
		return response;
	}

	private double checkoutUpdate(double checkoutInfoPrice, String OnlinePrice, String price) {
		double calculateMeal;
		if (checkoutInfoPrice > 0) {
			calculateMeal = (checkoutInfoPrice - Double.parseDouble(price)) + Double.parseDouble(OnlinePrice);
		} else {
			calculateMeal = checkoutInfoPrice + Double.parseDouble(OnlinePrice);
		}
		return calculateMeal;
	}

}