package com.easygofly.site.request;

import java.util.Date;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.easygofly.entity.Customer;
import com.easygofly.entity.Request;
import com.easygofly.site.customer.CustomerRepository;

@Service
@Transactional
public class RequestService {
	
	public static final int REQUEST_PER_PAGE = 8;
	
	@Autowired private RequestRepository requestRepo;
	@Autowired private CustomerRepository customerRepo;
	
	public Request saveRequest(Request request, Customer customer, Date createdTime) {
		System.out.println("Request ID: " + request.getId());
		Customer userInDB = customerRepo.findById(customer.getId()).get();
		
		request.setCustomer(userInDB);
		request.setCreatedTime(createdTime);
		
		return requestRepo.save(request);
	} 

	public Request saveConversation(Request request) {
		Request saveRequest = requestRepo.findById(request.getId()).get();
		
		saveRequest.setConversations(request.getConversations());
		
		return requestRepo.save(saveRequest);
	}
	
	public Page<Request> listByPage(int pageNum, String sortField, String sortDir, Customer customer) {
		Sort sort = Sort.by(sortField);
		
		sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();
		
		Pageable pageable = PageRequest.of(pageNum - 1, REQUEST_PER_PAGE, sort);
		
		return requestRepo.findRequest(customer, pageable);
	}
}
