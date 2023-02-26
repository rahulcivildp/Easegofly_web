package com.easygofly.admin.product;

import java.util.Date;

import com.easygofly.entity.Product;
import com.easygofly.entity.ProductDetail;


public class ProductSaveHelper {

	public static void setProductDetails(Product products, String pnr, String totalSeats, String uploadSeats, String flightNum, Date date,
			String depTime, String arrTime, float priceADT, float priceINF, float markupADT, float markupINF, String cityOne, String cityTwo, 
			boolean inStock, boolean enabled,  int stopNum, Integer duration, String brand) {
		if(pnr == null || flightNum == null) return;
		
			String pnr1 = pnr;
			String flightNum1 = flightNum;
			String totalSeats1 = totalSeats;
			String uploadSeats1 = uploadSeats;
			Date date1 = date;
			String depTime1 = depTime;
			String arrTime1 = arrTime;
			float priceADT1 = priceADT;
			float priceINF1 = priceINF;
			float markupADT1 = markupADT;
			float markupINF1 = markupINF;
			String cityOne1 = cityOne;
			String cityTwo1 = cityTwo;
			boolean inStock1 = inStock; 
			boolean enabled1 = enabled;
			
			String replaceArr = arrTime1.replace(":", ".");
			String replaceDep = depTime1.replace(":", ".");
		
			products.addDetail(pnr1, totalSeats1, uploadSeats1, flightNum1, date1, depTime1, arrTime1, priceADT1, priceINF1, 
					markupADT1, markupINF1, cityOne1, cityTwo1, inStock1, enabled1, stopNum, duration, brand, Float.parseFloat(replaceDep), Float.parseFloat(replaceArr));
			
	}
	
	public static void setStopDetails(String[] cityNames, String[] depTimes, String[] arrTimes, String[] totalTimes, ProductDetail productDetail) {
		if(cityNames == null || cityNames.length == 0) return;
		
		for (int i = 0; i < cityNames.length; i++) {
			String cityName = cityNames[i];
			String depTime = depTimes[i];
			String arrTime = arrTimes[i];
			String totalTime = totalTimes[i];
			
			if (!cityName.isEmpty()) {
				productDetail.addStopDetails(cityName, depTime, arrTime, totalTime);
			}
		}
	}

	public static void editStopDetails(Integer[] ids, String[] cityNames, String[] depTimes, String[] arrTimes, String[] totalTimes, ProductDetail productDetail) {
		if(ids == null || ids.length == 0) return;
		
		for (int i = 0; i < cityNames.length; i++) {
			Integer id = ids[i];
			String cityName = cityNames[i];
			String depTime = depTimes[i];
			String arrTime = arrTimes[i];
			String totalTime = totalTimes[i];
			
			if (!cityName.isEmpty()) {
				productDetail.addStopDetails(id, cityName, depTime, arrTime, totalTime);
			}
		}
	}
	
	public static String getRedirectURLtoAffectedUser(Product products) {
		String name = products.getName();
		return "redirect:/products/page/1?sortField=id&sortDir=asc&keyword=" + name;
	}
}
