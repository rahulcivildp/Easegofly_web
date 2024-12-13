package com.easygofly.site;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.easygofly.entity.LeastFare;


@Component
public class MainFilter implements Filter{

	@Autowired private MainService service;
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		List<LeastFare> leastFares = service.listLeastFare();
		
		request.setAttribute("leastFares", leastFares);
		
		chain.doFilter(request, response);
	}

}
