package com.easygofly.site.bus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easygofly.entity.Bus;
import com.easygofly.entity.Customer;
import com.easygofly.entity.Hotel;
import com.easygofly.entity.HotelRoom;
import com.easygofly.site.customer.CustomerRepository;
import com.easygofly.site.hotel.HotelController;
import com.easygofly.site.hotel.HotelService;

@RestController
public class BusRestController {

	@Autowired private BusController busController;
	@Autowired private BusService busService;
	@Autowired private CustomerRepository customerRepository;

//	@PostMapping("/save_bus_pax")
//	public void saveGuest(@Param("title") String title, @Param("fName") String fName, @Param("lName") String lName, @Param("email") String email, @Param("phoneNo") String phoneNo, 
//			@Param("age") Integer age, @Param("pan") String pan, @Param("bus_id") Integer bus_id, @Param("cust_id") Integer cust_id, @Param("seat_id") Integer seat_id) {
//	    Bus savedBus = busService.findByIdBus(bus_id);
//		HotelRoom savedRoom = hotelService.findByIdRoom(room_id);
//		Integer intPaxType = 0;
//		if (age <= 12) {
//			intPaxType = 2;
//		} else {
//			intPaxType = 1;
//		}
//		Hotel hotel = saveHelper.setGuests(title, fName, lName, phoneNo, email, intPaxType, age, true, null, "0001-01-01T00: 00: 00", "0001-01-01T00: 00: 00", pan, savedRoom, savedHotel);
//		Customer customer = customerRepository.findById(cust_id).get();
//		
//		hotelService.saveHotel(hotel, customer);
//	}
}
