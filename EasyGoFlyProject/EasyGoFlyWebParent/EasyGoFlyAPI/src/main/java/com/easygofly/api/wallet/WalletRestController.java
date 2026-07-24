package com.easygofly.api.wallet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easygofly.entity.RechargeHistory;
import com.easygofly.entity.Wallet;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(
		name = "CRUD REST APIs for Customer Wallet", 
		description = "Operations related to customer wallet"
)
public class WalletRestController {
	@Autowired private WalletRepository walletRepo;

	@PostMapping("/api/wallet")
    public String showWallet(HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
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
	
	
	@PostMapping("/api/wallet/transaction")
    public String showWalletTransaction(HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
        response.setContentType("application/json");

        WalletRequest walletRequest = new ObjectMapper().readValue(request.getInputStream(), WalletRequest.class);
        Wallet existingWallet = walletRepo.findById(walletRequest.id).get();
        List<RechargeHistory> rechargeHistories = existingWallet.getRechargeHistories();
		List<String> rechargeList = new ArrayList<String>();
        
        double amount = existingWallet.getBalance() / 100;

		for (RechargeHistory recharge : rechargeHistories) {

	        String historyBody =  "{"
	        		+ "\"id\": " + recharge.getId() + ", "
	                + "\"rechargeHistoryStatus\": \"" + recharge.getRechargeHistoryStatus() + "\", "
	        		+ "\"rechargeAmount\": " + recharge.getRechargeAmount() + ", "
	        		+ "\"zaakpaytransactionId\": \"" + recharge.getZaakpaytransactionId() + "\", "
	    	        + "\"date\": \"" + recharge.getDate() + "\", "
	    	    	+ "\"transaction\": \"" + recharge.getTransaction() + "\""
	        		+ "}";
	        rechargeList.add(historyBody);
		}
		
       	String arrayHistoryList = rechargeList.stream().map(val -> String.valueOf(val)).collect(Collectors.joining(",", "[", "]"));
		
		String walletBody =  "{"
	        		+ "\"id\": " + existingWallet.getId() + ", "
	                + "\"balance\": " + existingWallet.getBalance() + ", "
	        		+ "\"temp_value\": " + existingWallet.getTempValue() + ""
	        		+ "}";
		
        String responseBody = "{"
        		+ "\"code\": 0, "
        		+ "\"msg\": \"Show Wallet Details.\", "
        		+ "\"wallet\": " + walletBody + ", "
        		+ "\"data\": " + arrayHistoryList + ", "
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
