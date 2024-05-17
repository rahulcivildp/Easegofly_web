package com.easygofly.site.hotel;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.easygofly.entity.Customer;
import com.easygofly.entity.Hotel;
import com.easygofly.entity.HotelGuest;
import com.easygofly.entity.HotelHistory;
import com.easygofly.entity.HotelOrder;
import com.easygofly.entity.HotelRoom;
import com.easygofly.entity.HotelSupplierCode;
import com.easygofly.entity.OrderStatus;
import com.easygofly.entity.Wallet;
import com.easygofly.site.LogService;
import com.easygofly.site.wallet.WalletService;

@Service
public class HotelService {
	@Autowired private OnlineHotelService onlineHotelService;
	@Autowired private LogService logService;
	@Autowired private HotelHistoryRepository hotelHistoryRepo;
	@Autowired private HotelRepository hotelRepo;
	@Autowired private HotelGuestRepository guestRepo;
	@Autowired private HotelRoomRepository roomRepository;
	@Autowired private HotelOrderRepository orderRepository;
	@Autowired private HotelSupplierRepository hotelSupplierRepo;
	@Autowired private WalletService walletService;

	public void authenticationHotel(Model model) {
		try {
        	
        	// Create URL object with the API end-point
//            URL url = new URL("https://api.travelboutiqueonline.com/SharedAPI/SharedData.svc/rest/Authenticate");
            
        	// Create URL object with the API end-point
            URL url = new URL("http://api.tektravels.com/SharedServices/SharedData.svc/rest/Authenticate");

            // Open a connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
        	StringBuilder responseBody = new StringBuilder();
        	
            int authCode = onlineHotelService.apiAuthenticationHotel(connection, responseBody);
            
            JSONObject jsonObj = new JSONObject(responseBody.toString());
            JSONObject jsonObjInnerError = jsonObj.getJSONObject("Error");
            JSONObject jsonObjInnerMember = jsonObj.getJSONObject("Member");
             
            model.addAttribute("authCode", authCode);
            model.addAttribute("responseBody", jsonObj);
            model.addAttribute("memberName", jsonObjInnerMember.get("FirstName") + " " + jsonObjInnerMember.get("LastName"));
            model.addAttribute("memberEmail", jsonObjInnerMember.get("Email"));
            model.addAttribute("memberId", jsonObjInnerMember.get("MemberId"));
            model.addAttribute("memberAgencyId", jsonObjInnerMember.get("AgencyId"));
            model.addAttribute("memberLoginName", jsonObjInnerMember.get("LoginName"));
            model.addAttribute("memberLoginDetails", jsonObjInnerMember.get("LoginDetails"));
            model.addAttribute("memberIsPrimaryAgent", jsonObjInnerMember.get("isPrimaryAgent"));
            model.addAttribute("errorCode", jsonObjInnerError.get("ErrorCode"));
            model.addAttribute("errorMessage", jsonObjInnerError.get("ErrorMessage"));
            
            onlineHotelService.tokenId = (String) jsonObj.get("TokenId");
            System.out.println(jsonObj);
            logService.generateLog(jsonObj.toString());
            
            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
	}

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
