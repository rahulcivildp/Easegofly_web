package com.easygofly.site.hotel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easygofly.entity.Customer;
import com.easygofly.entity.Hotel;
import com.easygofly.entity.HotelGuest;
import com.easygofly.entity.HotelHistory;
import com.easygofly.entity.HotelOrder;
import com.easygofly.entity.HotelRoom;
import com.easygofly.entity.HotelSupplierCode;
import com.easygofly.entity.OrderStatus;
import com.easygofly.entity.Wallet;
import com.easygofly.site.wallet.WalletService;

@Service
public class HotelService {
	@Autowired private HotelHistoryRepository hotelHistoryRepo;
	@Autowired private HotelRepository hotelRepo;
	@Autowired private HotelGuestRepository guestRepo;
	@Autowired private HotelRoomRepository roomRepository;
	@Autowired private HotelOrderRepository orderRepository;
	@Autowired private HotelSupplierRepository hotelSupplierRepo;
	@Autowired private WalletService walletService;

	public HotelHistory saveHotelHistory(HotelHistory history, Customer customer) {
		HotelHistory newHistory = new HotelHistory(history.getCheckInDate(), history.getCheckOutDate(), history.getCountryCode(), history.getCityId(), history.getNoOfRooms(), history.getNoOfAdults(), 
						history.getNoOfChild(), history.getChildrenAge(), history.isNearBySearchAllowed(), customer);
		
		return hotelHistoryRepo.save(newHistory); 
	}
	
	public HotelHistory findByIdHistory(Integer id) {
		HotelHistory history = hotelHistoryRepo.findById(id).get();
		return history;
	}

	
	public Hotel saveHotel(Hotel hotel, Customer customer) {
		Hotel newHotel = hotel;
		newHotel.setCustomer(customer);
		
		Hotel savedHotel = hotelRepo.save(newHotel); 
		for (HotelSupplierCode hotelSupplierCode : savedHotel.getHotelSupplierCodes()) {
			hotelSupplierCode.setHotel(savedHotel);
			hotelSupplierRepo.save(hotelSupplierCode);
		}
		return savedHotel; 
	}
	
	public Hotel findByIdHotel(Integer id) {
		Hotel hotel = hotelRepo.findById(id).get();
		return hotel; 
	}
	
	
	public HotelGuest saveGuest(HotelGuest guest) {
		HotelGuest newGuest = guest;
		return guestRepo.save(newGuest); 
	}
	
	public HotelGuest findByIdGuest(Integer id) {
		HotelGuest guest = guestRepo.findById(id).get();
		return guest; 
	}
	
	public HotelRoom saveRoom(HotelRoom room) {
		HotelRoom newRoom = room;
		return roomRepository.save(newRoom); 
	}
	
	public HotelRoom findByIdRoom(Integer id) {
		HotelRoom room = roomRepository.findById(id).get();
		return room; 
	}

	public void deleteRoom(Integer id) {
		HotelRoom room = roomRepository.findById(id).get();
		roomRepository.deleteById(room.getId());
	}
	
	
	public HotelOrder saveOrder(HotelOrder hotelOrder) {
		return orderRepository.save(hotelOrder);
	}
	
	public HotelOrder findByIdOrder(Integer id) {
		return orderRepository.findById(id).get();
	}
	
	public HotelOrder updateOrder(Integer id, OrderStatus orderStatus, double price) {
		HotelOrder updateOrder = orderRepository.findById(id).get();
		updateOrder.setOrderStatus(orderStatus);
		updateOrder.setPrice(price);
		
		return orderRepository.save(updateOrder);
	}

	public HotelOrder updateOrderPrice(Integer id, double price) {
		HotelOrder updateOrder = orderRepository.findById(id).get();
		updateOrder.setPrice(price);
		
		return orderRepository.save(updateOrder);
	}
	
	public HotelOrder updateOrderStatus(Integer id, OrderStatus orderStatus) {
		HotelOrder updateOrder = orderRepository.findById(id).get();
		updateOrder.setOrderStatus(orderStatus);
		
		return orderRepository.save(updateOrder);
	}
	

	public Wallet hotelWalletPayOrder(Customer customer, HotelOrder order) {
		Date date = Calendar.getInstance().getTime();  
		DateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");  
		DateFormat dateFormat2 = new SimpleDateFormat("hhmmss");
		String strDate1 = dateFormat1.format(date);
		String strDate2 = dateFormat2.format(date);
		
		String orderString = "EGF" + strDate1 + "T" + strDate2 + "HO"+ order.getId();
		return walletService.updateWalletBalanceByHotelOrder(customer, order, orderString, "");
	}

	public Wallet walletPayHotelOrderCancel(Customer customer, HotelOrder order, OrderStatus orderStatus) {
		Date date = Calendar.getInstance().getTime();  
		DateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");  
		DateFormat dateFormat2 = new SimpleDateFormat("hhmmss");
		String strDate1 = dateFormat1.format(date);
		String strDate2 = dateFormat2.format(date);
		
		String orderString = "EGF" + strDate1 + "T" + strDate2 + "HO"+ order.getId();
		
		order.setOrderStatus(orderStatus);
		orderRepository.save(order);
		
		return walletService.cancelWalletBalanceByHotelOrder(customer, order, orderString, "");
	}
}
