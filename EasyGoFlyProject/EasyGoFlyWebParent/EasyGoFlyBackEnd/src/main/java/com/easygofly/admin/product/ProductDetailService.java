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
}
