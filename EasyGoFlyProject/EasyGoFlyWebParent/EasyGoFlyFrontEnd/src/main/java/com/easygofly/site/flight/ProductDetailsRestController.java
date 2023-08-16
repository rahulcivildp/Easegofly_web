package com.easygofly.site.flight;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.easygofly.entity.BaggageOnline;
import com.easygofly.entity.Brand;
import com.easygofly.entity.CartItem;
import com.easygofly.entity.Coupon;
import com.easygofly.entity.MealsOnline;
import com.easygofly.entity.ProductDetail;
import com.easygofly.entity.SeatsOnline;
import com.easygofly.entity.TravellerDetail;
import com.easygofly.site.order.CouponRepository;
import com.easygofly.site.shoppingCart.CartItemRepository;

@RestController
public class ProductDetailsRestController {
	
	@Autowired private CouponRepository couponRepo;
	@Autowired private BrandRepositoy brandRepo;
	@Autowired private ProductDetailsController productDetailsController;
	@Autowired private TravellerRepository travellerRepo;
	@Autowired private MealRepository mealRepo;
	@Autowired private BaggageRepository baggageRepo;
	@Autowired private SeatRepository seatRepo;
	@Autowired private ProductDetailCrudRepository productDetailsRepo;
	@Autowired private CartItemRepository cartItemRepo;
	@Autowired private ProductDetailService productDetailService;
	
	@PostMapping("/flight_order_check_coupon")
	public Coupon checkCoupon(@RequestBody Coupon coupon, RedirectAttributes redirectAttributes) throws IOException {
		Coupon coupon2 = couponRepo.findByCouponCode(coupon.getCouponCode());
		System.out.println(coupon2.toString());
		if (coupon2.equals(null)) {
			return null;
		} else {
			return coupon2;
		}
	}
	
	@GetMapping("/find_brand_{name}")
	public String findBrand(@PathVariable(name = "name") String name) {
		Brand brand = brandRepo.getBrandByName(name);
		if ( brand == null ) {
			return "/images/no-image.png";
		}
		String st = brand.getPhotosImagePath();
		return st;
	}

	
	@GetMapping("/find_baggage_{code}")
	public String findBaggage(@PathVariable(name = "code") String code) {
		List<BaggageOnline> baggageOnlineList = productDetailsController.baggageOnlineList;
		String price = "";
		for (BaggageOnline baggageOnline : baggageOnlineList) {
			if (baggageOnline.getCode().equals(code)) {
				price = baggageOnline.getPrice();
			}
		}
		System.out.println(price);
		return price;
	}
	
	@GetMapping("/find_meal_{code}")
	public String findMeal(@PathVariable(name = "code") String code) {
		List<MealsOnline> mealList = productDetailsController.mealsOnlineList;
		String price = "";
		for (MealsOnline mealsOnline : mealList) {
			if (mealsOnline.getCode().equals(code)) {
				price = mealsOnline.getPrice();
			}
		}
		return price;
	}
	
	@GetMapping("/find_seat_{id}")
	public String findSeat(@PathVariable(name = "id") Integer id) {
		List<SeatsOnline> seatsOnlineList = productDetailsController.seatsOnlineList;
		String price = "";
		for (SeatsOnline seatsOnline : seatsOnlineList) {
			if (seatsOnline.getId() == id ) {
				price = seatsOnline.getPrice();
			}
		}
		return price;
	}
	
	@PostMapping("/save_meal")
	public void saveMeal(@Param("id") Integer id, @Param("code") String code) {
		
		TravellerDetail travellerDetail = travellerRepo.findById(id).get();
		List<MealsOnline> mealsOnlines = productDetailsController.mealsOnlineList;
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
	
	@PostMapping("/save_baggage")
	public void saveBaggage(@Param("id") Integer id, @Param("code") String code) {
		
		TravellerDetail travellerDetail = travellerRepo.findById(id).get();
		List<BaggageOnline> baggageOnlineList = productDetailsController.baggageOnlineList;
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
	
	@PostMapping("/save_seat")
	public void saveSeat(@Param("id") Integer id, @Param("seatId") Integer seatId) {
		
		TravellerDetail travellerDetail = travellerRepo.findById(id).get();
		List<SeatsOnline> seatsOnlineList = productDetailsController.seatsOnlineList;
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
	
	@PostMapping("/save_every_components")
	public void saveEveryComponents(@Param("id") Integer id, @Param("seatId") Integer seatId, @Param("mealCode") String mealCode, @Param("baggageCode") String baggageCode) {
		
		TravellerDetail travellerDetail = travellerRepo.findById(id).get();
		mealBaggageSeatMethod(seatId, mealCode, baggageCode, travellerDetail);
	}
	

	@PostMapping("/save_every_return_components")
	public void saveEveryReturnComponents(@Param("travelerIdOne") Integer travelerIdOne, @Param("travelerIdTwo") Integer travelerIdTwo, @Param("seatId") Integer seatId, 
			@Param("mealCode") String mealCode, @Param("baggageCode") String baggageCode, @Param("seatIdTwo") Integer seatIdTwo, @Param("mealCodeTwo") String mealCodeTwo, 
			@Param("baggageCodeTwo") String baggageCodeTwo) {
		TravellerDetail travellerDetail = travellerRepo.findById(travelerIdOne).get();
		TravellerDetail travellerDetailTwo = travellerRepo.findById(travelerIdTwo).get();

		mealBaggageSeatMethod(seatId, mealCode, baggageCode, travellerDetail);
		mealBaggageSeatMethod(seatIdTwo, mealCodeTwo, baggageCodeTwo, travellerDetailTwo);
	}

	private void mealBaggageSeatMethod(Integer seatId, String mealCode, String baggageCode,
			TravellerDetail travellerDetail) {
		List<MealsOnline> mealsOnlines = productDetailsController.mealsOnlineList;
		for (MealsOnline mealsOnline : mealsOnlines) {
			if (mealsOnline.getCode().equals(mealCode) || mealsOnline.getCode() == mealCode) {
				if (travellerDetail.getMeal() == null ) {
					travellerDetail.addMeal(mealsOnline.getName(), mealsOnline.getPrice(), mealsOnline.getCode(), mealsOnline.getQuantity());
					travellerRepo.save(travellerDetail);
				} 
			} 
		}
		
		List<BaggageOnline> baggageOnlineList = productDetailsController.baggageOnlineList;
		for (BaggageOnline baggageOnline : baggageOnlineList) {
			if (baggageOnline.getCode().equals(baggageCode) || baggageOnline.getCode() == baggageCode) {
				if (travellerDetail.getBaggageOnline() == null) {
					BaggageOnline baggage = new BaggageOnline(baggageOnline.getPrice(), baggageOnline.getCode(), baggageOnline.getWeight(), travellerDetail);
					travellerDetail.setBaggageOnline(baggage);
					travellerRepo.save(travellerDetail);
				}
			} 
		}
		
		List<SeatsOnline> seatsOnlineList = productDetailsController.seatsOnlineList;
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
