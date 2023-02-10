package com.easygofly.admin.request;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.easygofly.entity.Request;
import com.easygofly.entity.exception.UserNotFoundException;

@Service
public class RequestService {
	
	public static final int REQUEST_PER_PAGE = 6;
	
	@Autowired private RequestRepository requestRepo;
	
	public List<Request> listRequests() {
		return requestRepo.findAllByOrderByCreatedTimeAsc();
	}
	
	public Page<Request> listByPage(int pageNum, String sortField, String sortDir, String keyword) {
		Sort sort = Sort.by(sortField);
		
		sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();
		
		Pageable pageable = PageRequest.of(pageNum - 1, REQUEST_PER_PAGE, sort);
		
		if(keyword != null) {
			return requestRepo.findRequest(keyword, pageable);
		}
		return requestRepo.findAll(pageable);
	}
	
	public void deleteRequest(Integer id) throws UserNotFoundException {
		Long count = requestRepo.countById(id);
		if(count == null || count == 0) {
			throw new UserNotFoundException("Could not find any request with ID: " + id);
		}
		
		requestRepo.deleteById(id);
	}
}
