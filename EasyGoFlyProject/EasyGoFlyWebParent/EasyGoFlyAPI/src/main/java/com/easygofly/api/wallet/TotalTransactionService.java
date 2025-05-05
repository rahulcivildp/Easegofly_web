package com.easygofly.api.wallet;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easygofly.entity.BusOrder;
import com.easygofly.entity.Customer;
import com.easygofly.entity.HotelOrder;
import com.easygofly.entity.Order;
import com.easygofly.entity.OrderStatus;
import com.easygofly.entity.RideOrder;
import com.easygofly.entity.TotalTransaction;

@Service
public class TotalTransactionService {


	@Autowired TotalTransactionRepository transactionRepo;
	
	public TotalTransaction createTotalTransaction(Customer customer, double amount, boolean isWallet, boolean isZaakPay, List<Order> flightOrders, List<HotelOrder> hotelOrders, 
			List<BusOrder> busOrders, Integer trnId, OrderStatus orderStatus) {
		System.out.println("...................................................1");
		TotalTransaction transaction = new TotalTransaction();
		Date date = new Date();
	    DateFormat dateFormat = new SimpleDateFormat("ddMMyyyyhhmm");
	    String formatedDate = dateFormat.format(date);

		System.out.println("...................................................2");
		String orderId = "TRN"+ formatedDate + "C"+customer.getId();
		
		transaction.setAmount(amount);
		transaction.setOrderId(orderId);
		transaction.setCreatedTime(date);
		transaction.setOrderStatus(orderStatus);
		transaction.setWallet(isWallet);
		transaction.setZaakPay(isZaakPay);
		transaction.setTrnId(trnId);
		transaction.setFlightOrders(flightOrders);
		transaction.setHotelOrders(hotelOrders);
		transaction.setBusOrders(busOrders);
		transaction.setCustomer(customer);

		System.out.println("...................................................3");
		return transactionRepo.save(transaction);
	}
	
	public TotalTransaction updateTotalTransactionStatus(TotalTransaction tr, OrderStatus orderStatus) {
		
		tr.setOrderStatus(orderStatus);
		
		return transactionRepo.save(tr);
	}
	
	public TotalTransaction createTotalTransactionRide(Customer customer, double amount, boolean isWallet, boolean isZaakPay,  
			List<RideOrder> rideOrders, Integer trnId, OrderStatus orderStatus) {
		TotalTransaction transaction = new TotalTransaction();
		Date date = new Date();
	    DateFormat dateFormat = new SimpleDateFormat("ddMMyyyyhhmm");
	    String formatedDate = dateFormat.format(date);

		String orderId = "TRN"+ formatedDate + "C"+customer.getId();
		
		transaction.setAmount(amount);
		transaction.setOrderId(orderId);
		transaction.setCreatedTime(date);
		transaction.setOrderStatus(orderStatus);
		transaction.setWallet(isWallet);
		transaction.setZaakPay(isZaakPay);
		transaction.setTrnId(trnId);
		transaction.setRideOrders(rideOrders);
		transaction.setCustomer(customer);

		return transactionRepo.save(transaction);
	}
}
