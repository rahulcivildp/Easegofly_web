package com.easygofly.api.bus;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easygofly.api.wallet.WalletService;
import com.easygofly.entity.Bus;
import com.easygofly.entity.BusCancelPolicy;
import com.easygofly.entity.BusHistory;
import com.easygofly.entity.BusOrder;
import com.easygofly.entity.BusPassenger;
import com.easygofly.entity.BusPointDetails;
import com.easygofly.entity.BusSeat;
import com.easygofly.entity.Customer;
import com.easygofly.entity.OrderStatus;
import com.easygofly.entity.Wallet;

@Service
public class BusService {
	@Autowired private BusHistoryRepository busHistoryRepo;
	@Autowired private BusRepository busRepo;
	@Autowired private BusPointDetailRepository busPointDetailRepo;
	@Autowired private BusCancelPolicyRepository busCancelPolicyRepo;
	@Autowired private BusSeatRepository busSeatRepo;
	@Autowired private BusPaxRepository busPaxRepo;
	@Autowired private BusOrderRepository busOrderRepo;
	@Autowired private WalletService walletService;

	public BusHistory saveBusHistory(BusHistory history, Customer customer) {
		BusHistory newHistory = new BusHistory(history.getDeptDate(), history.getCityIdOne(), history.getCityIdTwo(), customer);
		
		return busHistoryRepo.save(newHistory); 
	}
	
	public BusHistory findByIdBusHistory(Integer id) {
		BusHistory savedHistory = busHistoryRepo.findById(id).get();
		return savedHistory; 
	}
	
	
	public Bus saveBus(Bus bus, Customer customer) {
		Bus newBus = bus;
		newBus.setCustomer(customer);
		
		Bus savedBus = busRepo.save(newBus); 
		
		for (BusPointDetails busPointDetails : savedBus.getPointsDetails()) {
			busPointDetails.setBus(savedBus);
			busPointDetailRepo.save(busPointDetails);
		}
		
		for (BusCancelPolicy busCancelPolicy : savedBus.getBusCancelPolicies()) {
			busCancelPolicy.setBus(savedBus);
			busCancelPolicyRepo.save(busCancelPolicy);
		}
		
		return savedBus; 
	}
	
	public Bus findByIdBus(Integer id) {
		Bus bus = busRepo.findById(id).get();
		return bus; 
	}
	

	public BusSeat findByIdSeat(Integer id) {
		BusSeat savedSeat = busSeatRepo.findById(id).get();
		return savedSeat; 
	}
	
	public void deleteSeat(Integer id) {
		BusSeat seat = busSeatRepo.findById(id).get();
		busSeatRepo.deleteById(seat.getId());
	}


	public BusPassenger findByIdPax(Integer id) {
		BusPassenger savedPax = busPaxRepo.findById(id).get();
		return savedPax; 
	}

	public BusPassenger savePax(BusPassenger pax) {
		BusPassenger newPax = pax;
		return busPaxRepo.save(newPax); 
	}

	public void deletePax(Integer id) {
		BusPassenger pax = busPaxRepo.findById(id).get();
		busPaxRepo.deleteById(pax.getId());
	}


	public BusOrder saveOrder(BusOrder busOrder, Bus bus, BusHistory history) {
		Bus newBus = busRepo.findById(bus.getId()).get();
		BusHistory newHis = busHistoryRepo.findById(history.getId()).get();
		
		busOrder.setBus(newBus);
		busOrder.setBusHistory(newHis);
		
		BusOrder newOrder = busOrderRepo.save(busOrder);
		
		newBus.getBusOrders().add(newOrder);
		busRepo.save(newBus);
		newHis.getBusOrders().add(newOrder);
		busHistoryRepo.save(newHis);
		
		return busOrderRepo.save(busOrder);
	}
	
	public BusOrder saveOrder(BusOrder busOrder) {
		BusOrder order = busOrderRepo.findById(busOrder.getId()).get();
		
		return busOrderRepo.save(order);
	}
	
	public BusOrder findByIdOrder(Integer id) {
		return busOrderRepo.findById(id).get();
	}
	
	public BusOrder updateOrderPrice(Integer id, double price) {
		BusOrder updateOrder = busOrderRepo.findById(id).get();
		updateOrder.setPrice(price);
		
		return busOrderRepo.save(updateOrder);
	}
	
	public BusOrder updateOrderStatus(Integer id, OrderStatus orderStatus) {
		BusOrder updateOrder = busOrderRepo.findById(id).get();
		updateOrder.setOrderStatus(orderStatus);
		
		return busOrderRepo.save(updateOrder);
	}

	
	public Wallet busWalletPayOrder(Customer customer, BusOrder order) {
		Date date = Calendar.getInstance().getTime();  
		DateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");  
		DateFormat dateFormat2 = new SimpleDateFormat("hhmmss");
		String strDate1 = dateFormat1.format(date);
		String strDate2 = dateFormat2.format(date);
		
		String orderString = "EGF" + strDate1 + "T" + strDate2 + "BU"+ order.getId();
		return walletService.updateWalletBalanceByBusOrder(customer, order, orderString, "");
	}

	public Wallet walletPayBusOrderCancel(Customer customer, BusOrder order, OrderStatus orderStatus) {
		Date date = Calendar.getInstance().getTime();  
		DateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");  
		DateFormat dateFormat2 = new SimpleDateFormat("hhmmss");
		String strDate1 = dateFormat1.format(date);
		String strDate2 = dateFormat2.format(date);
		
		String orderString = "EGF" + strDate1 + "T" + strDate2 + "BU"+ order.getId();
		
		order.setOrderStatus(orderStatus);
		busOrderRepo.save(order);
		
		return walletService.cancelWalletBalanceByBusOrder(customer, order, orderString, "");
	}
	
}
