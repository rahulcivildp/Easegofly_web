package com.easygofly.site.hotel;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easygofly.entity.Customer;
import com.easygofly.entity.Hotel;
import com.easygofly.entity.HotelGuest;
import com.easygofly.entity.HotelRoom;
import com.easygofly.site.customer.CustomerRepository;

@RestController
public class HotelRestController {

	@Autowired private HotelController hotelController;
	@Autowired private HotelService hotelService;
	@Autowired private HotelSaveHelper saveHelper;
	@Autowired private CustomerRepository customerRepository;

	@PostMapping("/save_hotel_guest")
	public void saveGuest(@Param("title") String title, @Param("fName") String fName, @Param("lName") String lName, @Param("email") String email, @Param("phoneNo") String phoneNo, 
			@Param("age") Integer age, @Param("pan") String pan, @Param("hotel_id") Integer hotel_id, @Param("cust_id") Integer cust_id, @Param("room_id") Integer room_id) {
	    Hotel savedHotel = hotelService.findByIdHotel(hotel_id);
		HotelRoom savedRoom = hotelService.findByIdRoom(room_id);
		Integer intPaxType = 0;
		if (age <= 12) {
			intPaxType = 2;
		} else {
			intPaxType = 1;
		}
		Hotel hotel = saveHelper.setGuests(title, fName, lName, phoneNo, email, intPaxType, age, true, null, "0001-01-01T00: 00: 00", "0001-01-01T00: 00: 00", pan, savedRoom, savedHotel);
		Customer customer = customerRepository.findById(cust_id).get();
		
		hotelService.saveHotel(hotel, customer);
	}
	
	@PostMapping("/show_hotel_guest")
	public List<String> showHotelGuest(@Param("hotel_id") Integer hotel_id) {
		List<String> stringList = new ArrayList<>();
	    Hotel savedHotel = hotelService.findByIdHotel(hotel_id);
	
		for (HotelGuest newGuest : savedHotel.getGuests()) {
			HotelRoom savedRoom = hotelService.findByIdRoom(newGuest.getHotelRoom().getId());

			String str =  newGuest.getId() + "-" + newGuest.getTitle() + "-" + newGuest.getFirstName() + "-" + newGuest.getLastName() + "-" + newGuest.getAge() + "-" + newGuest.getPan() 
			+ "-" + newGuest.getEmail() + "-" + newGuest.getPhoneNo() + "-" + savedRoom.getRoomIndex() + "-" + savedRoom.getRoomDescription();
			
			stringList.add(str);
		}
		
		return stringList;
	}
	
	@PostMapping("/modify_guest")
	public void modifyHotelGuest(@Param("guest_id") Integer guest_id, @Param("title") String title, @Param("fName") String fName, @Param("lName") String lName, 
			@Param("email") String email, @Param("phoneNo") String phoneNo, @Param("age") Integer age, @Param("pan") String pan ) {
	    HotelGuest savedGuest = hotelService.findByIdGuest(guest_id);
	    savedGuest.setTitle(title);
	    savedGuest.setFirstName(fName);
	    savedGuest.setLastName(lName);
	    savedGuest.setEmail(email);
	    savedGuest.setPhoneNo(phoneNo);
	    savedGuest.setAge(age);
	    savedGuest.setPan(pan);
	    
	    	HotelGuest saveGuest = hotelService.saveGuest(savedGuest);
	    	System.out.println(saveGuest.getEmail());
	}
	
	@PostMapping("/save_hotel_room")
	public void saveRoom(@Param("roomIndex") String roomIndex, @Param("hotel_id") Integer hotel_id, @Param("cust_id") Integer cust_id) {
	    Hotel savedHotel = hotelService.findByIdHotel(hotel_id);
	    HotelRoom newRoom = new HotelRoom();
		Customer customer = customerRepository.findById(cust_id).get();
	    
	    for (HotelRoom room : hotelController.hotelRooms) {
			if (room.getRoomIndex() == Integer.parseInt(roomIndex)) {
				newRoom = room;
			}
		}
		Hotel hotel = saveHelper.setRooms(newRoom, savedHotel);
		
		hotelService.saveHotel(hotel, customer);
	}
	
	@PostMapping("/show_hotel_room_rest")
	public List<String> showHotelRoom(@Param("hotel_id") Integer hotel_id) {
		List<String> stringList = new ArrayList<>();
	    Hotel savedHotel = hotelService.findByIdHotel(hotel_id);
	
		for (HotelRoom room : savedHotel.getHotelRooms()) {

			String str =  room.getId() + "-" + room.getRoomIndex() + "-" + room.getRoomDescription() + "-" + room.getRatePlanName() + "-" + room.getAvailabilityType();
			stringList.add(str);
		}
		
		return stringList;
	}

	@PostMapping("/delete_hotel_room")
	public void deleteRoom(@Param("room_id") Integer room_id) {
	    hotelService.deleteRoom(room_id);
	}
}
