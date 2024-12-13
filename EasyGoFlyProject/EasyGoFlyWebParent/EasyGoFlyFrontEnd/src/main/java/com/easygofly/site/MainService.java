package com.easygofly.site;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easygofly.entity.LeastFare;
import com.easygofly.entity.ProductDetail;
import com.easygofly.entity.WebDetails;
import com.easygofly.site.flight.FlightRepository;
import com.easygofly.site.setting.web.WebSettingService;

@Service
public class MainService {
	@Autowired private WebSettingService webSettingService;
	@Autowired private FlightRepository flightRepo;

	public List<LeastFare> listLeastFare() {
		List<WebDetails> webDetails = webSettingService.listAllSettings();
		List<LeastFare> leastFares = new ArrayList<>();
		for (WebDetails detail : webDetails) {
			if (detail.getKey().equals("PRICE_1_LINK")) {
				ProductDetail flight = flightFareDetails(detail);
				if (flight != null ) {
					leastFares.add(new LeastFare(1, detail.getValue(), flight));
				}
				
			} else if (detail.getKey().equals("PRICE_2_LINK")) {
				ProductDetail flight = flightFareDetails(detail);
				if (flight != null ) {
					leastFares.add(new LeastFare(2, detail.getValue(), flight));
				}
				
			} else if (detail.getKey().equals("PRICE_3_LINK")) {
				ProductDetail flight = flightFareDetails(detail);
				if (flight != null ) {
					leastFares.add(new LeastFare(3, detail.getValue(), flight));
				}
				
			} else if (detail.getKey().equals("PRICE_4_LINK")) {
				ProductDetail flight = flightFareDetails(detail);
				if (flight != null ) {
					leastFares.add(new LeastFare(4, detail.getValue(), flight));
				}
				
			} else if (detail.getKey().equals("PRICE_5_LINK")) {
				ProductDetail flight = flightFareDetails(detail);
				if (flight != null ) {
					leastFares.add(new LeastFare(5, detail.getValue(), flight));
				}
				
			} else if (detail.getKey().equals("PRICE_6_LINK")) {
				ProductDetail flight = flightFareDetails(detail);
				if (flight != null ) {
					leastFares.add(new LeastFare(6, detail.getValue(), flight));
				}
			}
		}
		return leastFares;
	}

	private ProductDetail flightFareDetails(WebDetails detail) {
		String priceLink1 = detail.getValue();
		String[] parts = priceLink1.split("_");
		ProductDetail flight = new ProductDetail();
		if (parts.length != 0) {
			for (int i = 0; i < parts.length; i++) {
				if (i == 4) {
					Integer convInteger = Integer.parseInt(parts[i]);
					try {
						flight = flightRepo.findById(convInteger).get();
						
					} catch (Exception e) {
						e.printStackTrace();
						flight = null;
					}
				}
			}
		} 

		return flight;
	}
}
