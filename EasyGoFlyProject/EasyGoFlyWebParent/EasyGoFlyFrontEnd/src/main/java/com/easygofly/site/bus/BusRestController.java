package com.easygofly.site.bus;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easygofly.entity.Bus;
import com.easygofly.entity.BusPassenger;
import com.easygofly.entity.BusSeat;
import com.easygofly.entity.Customer;
import com.easygofly.site.customer.CustomerRepository;

@RestController
public class BusRestController {

	@Autowired private BusController busController;
	@Autowired private BusService busService;
	@Autowired private BusSaveHelper busSaveHelper;
	@Autowired private CustomerRepository customerRepository;

	@PostMapping("/save_bus_pax")
	public void savePax(@Param("title") String title, @Param("fName") String fName, @Param("lName") String lName, @Param("email") String email, @Param("phoneNo") String phoneNo, 
			@Param("age") Integer age, @Param("gender") Integer gender, @Param("pan") String pan, @Param("bus_id") Integer bus_id, @Param("cust_id") Integer cust_id, @Param("seat_id") Integer seat_id,
			@Param("address") String address) {
	    Bus savedBus = busService.findByIdBus(bus_id);
		BusSeat savedSeat = busService.findByIdSeat(seat_id);
	
		Bus bus = busSaveHelper.setPax(savedBus, title, fName, lName, phoneNo, email, pan, "Pan no", gender, age, savedSeat.getId(), false, address);
		Customer customer = customerRepository.findById(cust_id).get();
		
		System.out.println(bus.getTravelName());
		busService.saveBus(bus, customer);
	}
	
	@PostMapping("/modify_pax")
	public void modifyPax(@Param("guest_id") Integer guest_id, @Param("title") String title, @Param("fName") String fName, @Param("lName") String lName, 
			@Param("email") String email, @Param("phoneNo") String phoneNo, @Param("age") Integer age, @Param("pan") String pan, @Param("address") String address, 
			@Param("gender") Integer gender, @Param("seat_id") Integer seat_id) {
	    BusPassenger savedGuest = busService.findByIdPax(guest_id);
		BusSeat savedSeat = busService.findByIdSeat(seat_id);
	    savedGuest.setTitle(title);
	    savedGuest.setFirstName(fName);
	    savedGuest.setLastName(lName);
	    savedGuest.setEmail(email);
	    savedGuest.setPhoneNo(phoneNo);
	    savedGuest.setAge(age);
	    savedGuest.setIdNumber(pan);
	    savedGuest.setAddress(address);
	    savedGuest.setGender(gender);
	    savedGuest.setSeatId(savedSeat.getId());
	    
	    BusPassenger saveGuest = busService.savePax(savedGuest);
	    	System.out.println(saveGuest.getEmail());
	}
	
	@PostMapping("/show_bus_pax")
	public List<String> showPax(@Param("bus_id") Integer bus_id) {
		List<String> stringList = new ArrayList<>();
		Bus savedBus = busService.findByIdBus(bus_id);
	
		for (BusPassenger newPax : savedBus.getBusPassengers()) {
			try {
				BusSeat savedSeat = busService.findByIdSeat(newPax.getSeatId());

				String str =  newPax.getId() + "-" + newPax.getTitle() + "-" + newPax.getFirstName() + "-" + newPax.getLastName() + "-" + newPax.getAge() + "-" + newPax.getIdNumber() 
				+ "-" + newPax.getEmail() + "-" + newPax.getPhoneNo() + "-" + newPax.getGender() + "-" + newPax.getAddress() + "-" + savedSeat.getSeatName() + "-" + savedSeat.getId() 
				+ "-" + savedSeat.getSeatFare();
				
				stringList.add(str);
				
			} catch (Exception e) {
				busService.deletePax(newPax.getId());
			}
		}
		
		return stringList;
	}

	@PostMapping("/delete_bus_pax")
	public void deletePax(@Param("pax_id") Integer pax_id) {
		busService.deletePax(pax_id);
	}
	@PostMapping("/show_bus_seat_rest")
	public List<String> showSeat(@Param("bus_id") Integer bus_id) {
		List<String> stringList = new ArrayList<>();
		Bus savedBus = busService.findByIdBus(bus_id);
	
		for (BusSeat seat : savedBus.getBusSeats()) {

			String str =  seat.getId() + "-" + seat.getSeatIndex() + "-" + seat.getSeatName() + "-" + seat.getSeatType() + "-" + seat.getSeatFare();
			stringList.add(str);
		}
		
		return stringList;
	}

	@PostMapping("/show_option_seats")
	public List<String> showListOption(@Param("bus_id") Integer bus_id){
		List<String> stringList = new ArrayList<>();
		List<BusSeat> seatList = new ArrayList<BusSeat>();
		Bus savedBus = busService.findByIdBus(bus_id);
		seatList = savedBus.getBusSeats();
		try {
			for (BusSeat seat : savedBus.getBusSeats()) {
				for (BusPassenger pax : savedBus.getBusPassengers()) {
					if (pax.getSeatId() == seat.getId()) {
						seatList.remove(seat);
					}
				}
			}
			
			for (BusSeat busSeat : seatList) {
				
				String str =  busSeat.getId() + "-" + busSeat.getSeatName() + "-" + busSeat.getSeatFare();
				stringList.add(str);
			}
			
		} catch (Exception e) {
			for (BusSeat busSeat : seatList) {
				
				String str =  busSeat.getId() + "-" + busSeat.getSeatName() + "-" + busSeat.getSeatFare();
				stringList.add(str);
			}
		}
		

		return stringList;
	}
	
	@PostMapping("/save_bus_seat")
	public void saveSeat(@Param("seatIndex") String seatIndex, @Param("bus_id") Integer bus_id, @Param("cust_id") Integer cust_id) {
		Bus savedBus = busService.findByIdBus(bus_id);
	    BusSeat newSeat = new BusSeat();
		Customer customer = customerRepository.findById(cust_id).get();
	    
	    for (BusSeat seat : busController.seatList) {
			if (seat.getSeatIndex() == Integer.parseInt(seatIndex)) {
				newSeat = seat;
			}
		}
	    Bus bus = busSaveHelper.setSeats(newSeat, savedBus);
		
	    busService.saveBus(bus, customer);
	}

	@PostMapping("/delete_bus_seat")
	public void deleteSeat(@Param("seat_id") Integer seat_id) {
		busService.deleteSeat(seat_id);
	}
}
