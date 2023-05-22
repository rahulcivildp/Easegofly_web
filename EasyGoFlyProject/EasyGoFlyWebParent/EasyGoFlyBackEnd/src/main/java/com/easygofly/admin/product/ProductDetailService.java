package com.easygofly.admin.product;

import java.util.NoSuchElementException;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.easygofly.entity.ProductDetail;
import com.easygofly.entity.exception.CategoryNotFoundException;
import com.easygofly.entity.exception.ProductNotFoundException;

@Service
@Transactional
public class ProductDetailService {

	@Autowired private ProductDetailRepository productDetailRepo;
	
	public ProductDetail getFlightDetails(Integer id) throws ProductNotFoundException {
		try {
			return productDetailRepo.findById(id).get();
		} catch (NoSuchElementException e) {
			throw new ProductNotFoundException("Could not find any Flight with ID: " + id);
		}
	}
	
	public ProductDetail saveFlightDetails(ProductDetail productDetail) {
		ProductDetail stopInDB = productDetailRepo.findById(productDetail.getId()).get();
		
		stopInDB.setStops(productDetail.getStops());
		
		return productDetailRepo.save(stopInDB);
	}
	
	public void deleteProductDetail(Integer id) throws CategoryNotFoundException {
		Long count = productDetailRepo.countById(id);
		if(count == null || count == 0) {
			throw new CategoryNotFoundException("Could not find any user with ID: " + id);
		}
		
		productDetailRepo.deleteById(id);
	}
	
	public ProductDetail updateProductDetail(Integer flight_id, 
			String totalSeats, 
			String uploadSeats, 
			String flightNum, 
			Integer stops, 
			String depTime, 
			String arrTime, 
			Integer duration, 
			Float priceADT,
			Float priceINF,
			Float markupADT,
			Float markupINF,
			String pnr) {
		ProductDetail productDetail = productDetailRepo.findById(flight_id).get();
		productDetail.setPnr(pnr);
		productDetail.setTotalSeats(totalSeats);
		productDetail.setUploadSeats(uploadSeats);
		productDetail.setFlightNum(flightNum);
		productDetail.setStopNum(stops);
		productDetail.setDepTime(depTime);
		productDetail.setArrTime(arrTime);
		productDetail.setDuration(duration);
		productDetail.setPriceADT(priceADT);
		productDetail.setPriceINF(priceINF);
		productDetail.setMarkupADT(markupADT);
		productDetail.setMarkupINF(markupINF);
		return productDetailRepo.save(productDetail);
	}
	
}
