package com.easygofly.site.flight;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easygofly.entity.TravellerDetail;

@Service
public class TravelerService {

	@Autowired TravellerRepository travellerRepo;
	
	public void updateBasefareTax(TravellerDetail travellerDetail, String basefare, String tax) {
		travellerDetail.setBasefare(basefare);
		travellerDetail.setTax(tax);
		
		travellerRepo.save(travellerDetail);
	}
	
}
