package com.easygofly.admin.product;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.easygofly.entity.ProductDetail;

public interface ProductDetailRepository extends PagingAndSortingRepository<ProductDetail, Integer> {

	public Long countById(Integer id);

}
