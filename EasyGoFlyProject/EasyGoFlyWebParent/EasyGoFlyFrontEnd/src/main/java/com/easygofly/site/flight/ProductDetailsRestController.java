package com.easygofly.site.flight;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.easygofly.entity.BaggageOnline;
import com.easygofly.entity.Brand;
import com.easygofly.entity.Coupon;
import com.easygofly.entity.MealsOnline;
import com.easygofly.entity.TravellerDetail;
import com.easygofly.site.order.CouponRepository;

@RestController
public class ProductDetailsRestController {
	
	@Autowired CouponRepository couponRepo;
	@Autowired BrandRepositoy brandRepo;
	@Autowired ProductDetailsController productDetailsController;
	@Autowired TravellerRepository travellerRepo ;

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

	
	@GetMapping("/find_baggage_{kg}")
	public String findBaggage(@PathVariable(name = "kg") String kg) {
		List<BaggageOnline> baggageOnlineList = productDetailsController.baggageOnlineList;
		String price = "";
		for (BaggageOnline baggageOnline : baggageOnlineList) {
			if (baggageOnline.getWeight().equals(kg)) {
				price = baggageOnline.getPrice();
			}
		}
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
	
	@PostMapping("/save_baggage/{id}/{code}")
	public String saveBaggage(@PathVariable(name = "id") Integer id, 
			@PathVariable(name = "code") String code) {
		
		TravellerDetail travellerDetail = travellerRepo.findById(id).get();
		List<BaggageOnline> baggageOnlineList = productDetailsController.baggageOnlineList;
		String resp = "FAILED";
		for (BaggageOnline baggageOnline : baggageOnlineList) {
			if (baggageOnline.getCode().equals(code) ) {
				travellerDetail.setBaggage(code + "|" + baggageOnline.getWeight() + "|" + baggageOnline.getPrice());
				travellerRepo.save(travellerDetail);
				String ok = "OK";
				resp = ok;
			} 
		}
		return resp;
	}
	
	@PostMapping("/save_meal/{id}/{code}")
	public String saveMeal(@PathVariable(name = "id") Integer id,
			@PathVariable(name = "code") String code) {
		
		TravellerDetail travellerDetail = travellerRepo.findById(id).get();
		List<MealsOnline> mealList = productDetailsController.mealsOnlineList;
		String resp = "FAILED";
		for (MealsOnline mealsOnline : mealList) {
			if (mealsOnline.getCode().equals(code) ) {
				travellerDetail.setMeal(code + "|" + mealsOnline.getQuantity() + "|" + mealsOnline.getPrice() + "|" + mealsOnline.getName());
				travellerRepo.save(travellerDetail);
				String ok = "OK";
				resp = ok;
			} 
		}
		return resp;
	}
}
