package com.easygofly.api;


import java.util.HashMap;
import java.util.Map;

import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.easygofly.entity.CommonSet;
import com.easygofly.entity.CommonSetBag;



@RestController
public class MainRestController {
	
//	@PostMapping("/api/login")
//	public ResponseEntity<Map<String, Object>> viewLoginPage(@RequestHeader HttpHeaders headers, @Param("email")String email, @Param("pass")String pass) {
//	    if (headers.containsKey(HttpHeaders.AUTHORIZATION)) {
//	        String authorizationHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);
//	        if (authorizationHeader.startsWith("Basic ")) {
//	            HashMap<String, Object> map = new HashMap<String, Object>();
//	            map.put("name", "test1");
//	            map.put("sex", "male");
//	            map.put("address", "1324");
//	            map.put("old", "123");
//	            
//	          return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK); 
//	        }
//	      }
//	      return new ResponseEntity<Map<String, Object>>(headers, HttpStatus.UNAUTHORIZED);
//	}
	
}
