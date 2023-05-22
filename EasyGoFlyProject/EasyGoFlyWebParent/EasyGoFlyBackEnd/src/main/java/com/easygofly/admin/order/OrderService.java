package com.easygofly.admin.order;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.easygofly.entity.Order;

@Service
@Transactional
public class OrderService {
	
	public static final int ORDER_PER_PAGE = 8;
	@Autowired private OrderRepository orderRepo;

	public List<Order> listAll() {
		return (List<Order>) orderRepo.findAll(Sort.by("name").ascending());
	}
	
	public Page<Order> listByPage(int pageNum, String sortField, String sortDir, String keyword) {
		Sort sort = Sort.by(sortField);
		
		sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();
		
		Pageable pageable = PageRequest.of(pageNum - 1, ORDER_PER_PAGE, sort);
		
		if(keyword != null) {
			return orderRepo.findOrder(keyword, pageable);
		}
		return orderRepo.findAll(pageable);
	}
}
