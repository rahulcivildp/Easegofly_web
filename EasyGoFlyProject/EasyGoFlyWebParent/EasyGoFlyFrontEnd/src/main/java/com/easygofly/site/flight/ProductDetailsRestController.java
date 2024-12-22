package com.easygofly.site.flight;

import java.io.IOException;
import java.util.ArrayList;
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
import com.easygofly.site.flight.order.CouponRepository;

@RestController
public class ProductDetailsRestController {
	
	@Autowired private ProductRestData restService;
	@Autowired private CouponRepository couponRepo;
	@Autowired private BrandRepositoy brandRepo;
	@Autowired private ProductDetailsController productDetailsController;
	@Autowired private ProductDetailsInternationController pInternationController;
	
	List<ProductDetail> testflights = new ArrayList<>();
	
	@PostMapping("/flight_order_check_coupon")
	public Coupon checkCoupon(@RequestBody Coupon coupon, RedirectAttributes redirectAttributes) throws IOException {
		Coupon coupon2 =  couponRepo.findByCouponCode(coupon.getCouponCode());
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

	@PostMapping("/show_checkout")
	public String showCheckoutInfo(@Param("checkout_id") Integer checkout_id) {
		return restService.checkoutMentod(checkout_id);
	}
	
	@PostMapping("/show_checkout_return")
	public String showCheckoutInfoReturn(@Param("checkoutOne_id") Integer checkoutOne_id, @Param("checkoutTwo_id") Integer checkoutTwo_id) {
		return restService.checkoutReturnMentod(checkoutOne_id,  checkoutTwo_id);
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
	public void saveMeal(@Param("id") Integer id, @Param("code") String code, @Param("checkout_id") Integer checkout_id, @Param("type") String type) {
		if (type.equals("oneway")) {
			restService.mealsMethod(id, code,  productDetailsController.mealsOnlineList, checkout_id);
		} else {
			restService.mealsMethod(id, code,  productDetailsController.mealsOnlineListReturn, checkout_id);
		}
	}
	
	@PostMapping("/save_meal_international")
	public void saveMealInter(@Param("id") Integer id, @Param("code") String code, @Param("checkout_id") Integer checkout_id) {
		
		restService.mealsMethod(id, code,  pInternationController.mealsOnlineList, checkout_id);
	}
	
	@PostMapping("/save_baggage")
	public void saveBaggage(@Param("id") Integer id, @Param("code") String code, @Param("checkout_id") Integer checkout_id, @Param("type") String type) {
		if (type.equals("oneway")) {
			restService.baggageMethod(id, code,  productDetailsController.baggageOnlineList, checkout_id);
		} else {
			restService.baggageMethod(id, code,  productDetailsController.baggageOnlineListReturn, checkout_id);
		}
	}
	
	@PostMapping("/save_baggage_international")
	public void saveBaggageInter(@Param("id") Integer id, @Param("code") String code, @Param("checkout_id") Integer checkout_id) {
		
		restService.baggageMethod(id, code,  pInternationController.baggageOnlineList, checkout_id);
	}
	
	@PostMapping("/save_seat")
	public void saveSeat(@Param("id") Integer id,  @Param("code") String code, @Param("checkout_id") Integer checkout_id, @Param("type") String type) {
		if (type.equals("oneway")) {
			restService.seatMethod(id, code,  productDetailsController.seatsOnlineList, checkout_id);
		} else {
			restService.seatMethod(id, code,  productDetailsController.seatsOnlineListReturn, checkout_id);
		}
	}
	
	@PostMapping("/save_seat_international")
	public void saveSeatInter(@Param("id") Integer id,  @Param("code") String code, @Param("checkout_id") Integer checkout_id) {
		
		restService.seatMethod(id, code,  pInternationController.seatsOnlineList, checkout_id);
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
