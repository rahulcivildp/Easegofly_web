package com.easygofly.admin.setting.web;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.easygofly.entity.WebDetails;

@Component
public class WebSettingFilter implements Filter {
	
	@Autowired
	private WebSettingService service;
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletRequest servletRequest = (HttpServletRequest) request;
		String url = servletRequest.getRequestURI().toString();
		
		if (url.endsWith(".css") || url.endsWith(".js") || url.endsWith(".png") || url.endsWith(".jpg") || url.endsWith(".woff2")) {
			
			chain.doFilter(request, response);
			return;
		}
		
		List<WebDetails> generalSettings = service.getGeneralSetting();
		
		generalSettings.forEach(setting -> {
			//System.out.println(setting);
			request.setAttribute(setting.getKey(), setting.getValue());
		});
		
		chain.doFilter(request, response);
		

	}
}
