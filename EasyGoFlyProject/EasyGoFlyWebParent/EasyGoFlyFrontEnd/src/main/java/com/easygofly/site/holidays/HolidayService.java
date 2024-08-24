package com.easygofly.site.holidays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easygofly.entity.Customer;
import com.easygofly.entity.SightseeingHistory;

@Service
public class HolidayService {
	@Autowired private SightseeingHistoryRepository sHistoryRepo;

	public SightseeingHistory saveSightseeingHistory(SightseeingHistory history, Customer customer) {
		SightseeingHistory newHistory = new SightseeingHistory(history.getCityId(), history.getCountryCode(), history.getFromDate(), 
				history.getToDate(), history.getAdultCount(), history.getChildCount(), history.getChildrenAge(), customer);
		
		return sHistoryRepo.save(newHistory); 
	}
}
