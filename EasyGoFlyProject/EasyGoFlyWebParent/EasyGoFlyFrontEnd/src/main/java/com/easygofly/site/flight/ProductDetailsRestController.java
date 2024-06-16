package com.easygofly.site.flight;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.easygofly.entity.Brand;
import com.easygofly.entity.Coupon;
import com.easygofly.entity.ProductDetail;
import com.easygofly.entity.TravellerDetail;
import com.easygofly.site.flight.order.CouponRepository;

@RestController
public class ProductDetailsRestController {
	
	@Autowired private ProductRestData restService;
	@Autowired private CouponRepository couponRepo;
	@Autowired private BrandRepositoy brandRepo;
	@Autowired private ProductDetailsController productDetailsController;
	@Autowired private ProductDetailsInternationController pInternationController;
	@Autowired private TravellerRepository travellerRepo;
	
	@PostMapping("/flight_order_check_coupon")
	public Coupon checkCoupon(@RequestBody Coupon coupon, RedirectAttributes redirectAttributes) throws IOException {
		Coupon coupon2 =  couponRepo.findByCouponCode(coupon.getCouponCode());
		System.out.println(coupon2.toString());
		if (coupon2.equals(null)) {
			return null;
		} else {
			return coupon2;
		}
	}
	
	@GetMapping("/find_brand_{name}")
	public String findBrand(@PathVariable(name = "name") String name) {
		Brand brand =  brandRepo.getBrandByName(name);
		if ( brand == null ) {
			return "/images/no-image.png";
		}
		String st = brand.getPhotosImagePath();
		return st;
	}

	
	@GetMapping("/find_baggage_{code}")
	public String findBaggage(@PathVariable(name = "code") String code) {
		return restService.findBaggageMethod(code,  productDetailsController.baggageOnlineList);
	}

	@GetMapping("/find_baggage_inter_{code}")
	public String findBaggageInter(@PathVariable(name = "code") String code) {
		return restService.findBaggageMethod(code,  pInternationController.baggageOnlineList);
	}
	
	@GetMapping("/find_meal_{code}")
	public String findMeal(@PathVariable(name = "code") String code) {
		return restService.findMealMethod(code,  productDetailsController.mealsOnlineList);
	}
	
	@GetMapping("/find_meal_inter_{code}")
	public String findMealInter(@PathVariable(name = "code") String code) {
		return restService.findMealMethod(code,  pInternationController.mealsOnlineList);
	}
	
	@GetMapping("/find_seat_{id}")
	public String findSeat(@PathVariable(name = "id") Integer id) {
		return restService.findSeatMethod(id,  productDetailsController.seatsOnlineList);
	}
	
	@GetMapping("/find_seat_inter_{id}")
	public String findSeatInter(@PathVariable(name = "id") Integer id) {
		return restService.findSeatMethod(id,  pInternationController.seatsOnlineList);
	}
	
	@PostMapping("/save_meal")
	public void saveMeal(@Param("id") Integer id, @Param("code") String code) {
		
		restService.mealsMethod(id, code,  productDetailsController.mealsOnlineList);
	}
	
	@PostMapping("/save_meal_international")
	public void saveMealInter(@Param("id") Integer id, @Param("code") String code) {
		
		restService.mealsMethod(id, code,  pInternationController.mealsOnlineList);
	}
	
	@PostMapping("/save_baggage")
	public void saveBaggage(@Param("id") Integer id, @Param("code") String code) {
		
		restService.baggageMethod(id, code,  productDetailsController.baggageOnlineList);
	}
	
	@PostMapping("/save_baggage_international")
	public void saveBaggageInter(@Param("id") Integer id, @Param("code") String code) {
		
		restService.baggageMethod(id, code,  pInternationController.baggageOnlineList);
	}
	
	@PostMapping("/save_seat")
	public void saveSeat(@Param("id") Integer id, @Param("seatId") Integer seatId) {
		
		restService.seatMethod(id, seatId,  productDetailsController.seatsOnlineList);
	}
	
	@PostMapping("/save_seat_international")
	public void saveSeatInter(@Param("id") Integer id, @Param("seatId") Integer seatId) {
		
		restService.seatMethod(id, seatId,  pInternationController.seatsOnlineList);
	}
	
	@PostMapping("/save_every_components")
	public void saveEveryComponents(@Param("id") Integer id, @Param("seatId") Integer seatId, @Param("mealCode") String mealCode, @Param("baggageCode") String baggageCode) {
		
		TravellerDetail travellerDetail =  travellerRepo.findById(id).get();
		restService.mealBaggageSeatMethod(seatId, mealCode, baggageCode, travellerDetail,  productDetailsController.mealsOnlineList,  productDetailsController.baggageOnlineList,  productDetailsController.seatsOnlineList);
	}
	
	@PostMapping("/save_every_return_components")
	public void saveEveryReturnComponents(@Param("travelerIdOne") Integer travelerIdOne, @Param("travelerIdTwo") Integer travelerIdTwo, @Param("seatId") Integer seatId, 
			@Param("mealCode") String mealCode, @Param("baggageCode") String baggageCode, @Param("seatIdTwo") Integer seatIdTwo, @Param("mealCodeTwo") String mealCodeTwo, 
			@Param("baggageCodeTwo") String baggageCodeTwo) {
		TravellerDetail travellerDetail =  travellerRepo.findById(travelerIdOne).get();
		TravellerDetail travellerDetailTwo =  travellerRepo.findById(travelerIdTwo).get();

		restService.mealBaggageSeatMethod(seatId, mealCode, baggageCode, travellerDetail,  productDetailsController.mealsOnlineList,  productDetailsController.baggageOnlineList,  productDetailsController.seatsOnlineList);
		restService.mealBaggageSeatMethod(seatIdTwo, mealCodeTwo, baggageCodeTwo, travellerDetailTwo,  productDetailsController.mealsOnlineList,  productDetailsController.baggageOnlineList,  productDetailsController.seatsOnlineList);
	}

	@PostMapping("/save_every_components_international")
	public void saveEveryComponentsInter(@Param("id") Integer id, @Param("seatId") Integer seatId, @Param("mealCode") String mealCode, @Param("baggageCode") String baggageCode) {
		
		TravellerDetail travellerDetail =  travellerRepo.findById(id).get();
		restService.mealBaggageSeatMethod(seatId, mealCode, baggageCode, travellerDetail,  pInternationController.mealsOnlineList,  pInternationController.baggageOnlineList,  pInternationController.seatsOnlineList);
	}

	@PostMapping("/save_every_return_components_international")
	public void saveEveryReturnComponentsInter(@Param("travelerIdOne") Integer travelerIdOne, @Param("travelerIdTwo") Integer travelerIdTwo, @Param("seatId") Integer seatId, 
			@Param("mealCode") String mealCode, @Param("baggageCode") String baggageCode, @Param("seatIdTwo") Integer seatIdTwo, @Param("mealCodeTwo") String mealCodeTwo, 
			@Param("baggageCodeTwo") String baggageCodeTwo) {
		TravellerDetail travellerDetail =  travellerRepo.findById(travelerIdOne).get();
		TravellerDetail travellerDetailTwo =  travellerRepo.findById(travelerIdTwo).get();

		restService.mealBaggageSeatMethod(seatId, mealCode, baggageCode, travellerDetail,  pInternationController.mealsOnlineList,  pInternationController.baggageOnlineList,  pInternationController.seatsOnlineList);
		restService.mealBaggageSeatMethod(seatIdTwo, mealCodeTwo, baggageCodeTwo, travellerDetailTwo,  pInternationController.mealsOnlineList,  pInternationController.baggageOnlineList,  pInternationController.seatsOnlineList);
	}

	// Sorting methods  

	@GetMapping("/sort_by_brand")
	public void sortByBrand() {
		List<ProductDetail> flights = productDetailsController.listProductDetails;
		
		// Sort the objects in ascending order by the element
        Collections.sort(flights, (o1, o2) -> o1.getBrand().compareTo(o2.getBrand()));

	}
	
	//Timer Method
	
	@GetMapping("/show_timer")
	public Integer timer(@Param("timer") Integer timer) {
		pInternationController.timeRemainingProOne = timer;
		
		return pInternationController.timeRemainingProOne;
	}
	
	@GetMapping("/show_timer_return")
	public Integer timerReturn(@Param("timer") Integer timer) {
		pInternationController.timeRemainingProOne = timer;
		
		return pInternationController.timeRemainingProOne;
	}
}
