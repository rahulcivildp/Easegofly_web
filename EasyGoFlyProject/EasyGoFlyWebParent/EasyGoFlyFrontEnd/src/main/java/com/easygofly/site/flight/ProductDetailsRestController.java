package com.easygofly.site.flight;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.easygofly.entity.Brand;
import com.easygofly.entity.Coupon;
import com.easygofly.site.order.CouponRepository;

@RestController
public class ProductDetailsRestController {
	
	@Autowired CouponRepository couponRepo;
	@Autowired BrandRepositoy brandRepo;

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
}
