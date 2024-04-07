package com.easygofly.site.hotel;

import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HotelRestController {

	@PostMapping("/save_mhotel_guest")
	public void saveMeal(@Param("id") Integer id, @Param("code") String code) {
		
	}
}
