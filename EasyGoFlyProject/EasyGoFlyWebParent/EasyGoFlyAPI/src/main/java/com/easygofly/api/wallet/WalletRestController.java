package com.easygofly.api.wallet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easygofly.entity.Wallet;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class WalletRestController {
	@Autowired private WalletRepository walletRepo;

	@PostMapping("/api/wallet")
    public String flightHistorySave(HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
        response.setContentType("application/json");

        WalletRequest walletRequest = new ObjectMapper().readValue(request.getInputStream(), WalletRequest.class);
        Wallet existingWallet = walletRepo.findById(walletRequest.id).get();
        
        double amount = existingWallet.getBalance() / 100;

		String walletBody =  "{"
	        		+ "\"id\": " + existingWallet.getId() + ", "
	                + "\"balance\": " + existingWallet.getBalance() + ", "
	        		+ "\"temp_value\": " + existingWallet.getTempValue() + ""
	        		+ "}";
		
        String responseBody = "{"
        		+ "\"code\": 0, "
        		+ "\"msg\": \"Show Wallet Details.\", "
        		+ "\"data\": " + walletBody + ", "
        		+ "\"amount\": " + amount + ""
        		+ "}";

      return responseBody;
    }
	// Static POJO List
	
    private static class WalletRequest {
        private Integer id;

		@SuppressWarnings("unused")
		public Integer getId() {
			return id;
		}

		@SuppressWarnings("unused")
		public void setId(Integer id) {
			this.id = id;
		}
    }
    
}
