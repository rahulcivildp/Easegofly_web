package com.easygofly.admin.driver;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.easygofly.admin.FileUploadUtil;
import com.easygofly.entity.Cab;
import com.easygofly.entity.Driver;
import com.easygofly.entity.exception.UserNotFoundException;

@Service
public class DriverService {
	@Autowired private DriverRepository driverRepo;
	@Autowired private CabRepository cabRepo;
	
	public static final int DRIVER_PER_PAGE = 9;
	

	public Page<Driver> listByPage(int pageNum, String sortField, String sortDir, String keyword) {
		Sort sort = Sort.by(sortField);
		
		sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();
		
		Pageable pageable = PageRequest.of(pageNum - 1, DRIVER_PER_PAGE, sort);
		
		if(keyword != null) {
			return driverRepo.findDriver(keyword, pageable);
		}
		return driverRepo.findAll(pageable);
	}
	

	public Driver saveDriver(Driver driver) {
		return driverRepo.save(driver);
	}

	public Driver updateDriver(Integer driverId, Integer cabId, Map<String, String> formData, MultipartFile multipartFileDriver, MultipartFile multipartFileCab) throws IOException {
		Driver driver = driverRepo.findById(driverId).get();
		Cab cab = cabRepo.findById(cabId).get();
		double latitude = formData.get("latitude") != null && !formData.get("latitude").isEmpty() ? Double.parseDouble(formData.get("latitude")) : 0.0;
		double longitude = formData.get("longitude") != null && !formData.get("longitude").isEmpty() ? Double.parseDouble(formData.get("longitude")) : 0.0;
		
		cab.setName(formData.get("cabName"));
		cab.setType(formData.get("type"));
		cab.setSeating(Integer.parseInt(formData.get("seating")));
		cab.setBookingFare(Double.parseDouble(formData.get("bookingFare")));
		cab.setFuelType(formData.get("fuelType"));
		cab.setColor(formData.get("color"));
		cab.setMaxSpeed(Integer.parseInt(formData.get("maxSpeed")));
		cab.setAirConditioning(formData.get("airConditioning"));
		cab.setWifi(formData.get("wifi"));
		cab.setLicense(formData.get("license"));
		cab.setFeatures(formData.get("features"));
		cabRepo.save(cab);
		

		driver.setName(formData.get("driverName"));
		driver.setRating(Double.parseDouble(formData.get("rating")));
		driver.setExperience(Integer.parseInt(formData.get("experience")));
		driver.setLocation(formData.get("location"));
		driver.setContact(formData.get("contact"));
		driver.setAddress(formData.get("address"));
		driver.setCoveringDistance(Integer.parseInt(formData.get("coveringDistance")));
		driver.setLatitude(latitude);
		driver.setLongitude(longitude);
				
		multipartMethodCab(multipartFileCab, cab);
		
		return multipartMethodDriver(multipartFileDriver, driver);
	}

	public Driver findById(Integer id) {
		return driverRepo.findById(id).get();
	}
	

	public Driver multipartMethodDriver(MultipartFile multipartFile, Driver driver) throws IOException {
		if (!multipartFile.isEmpty() ) {
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
			driver.setPhotos(fileName);
			Driver savedDriver = driverRepo.save(driver);
			String uploadDir = "../driver-photos/" + savedDriver.getId();
			
			FileUploadUtil.cleanDir(uploadDir);
			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
			
			return savedDriver;
		} else {
			if (driver.getPhotos() == null) {
				driver.setPhotos(null);
			} else if(driver.getPhotos().isEmpty()) {
				driver.setPhotos(null);
			}
			
			return driverRepo.save(driver);
		}
	}
	

	public void multipartMethodCab(MultipartFile multipartFile, Cab cab) throws IOException {
		if (!multipartFile.isEmpty() ) {
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
			cab.setPhotos(fileName);
			Cab savedDriver = cabRepo.save(cab);
			String uploadDir = "../cab-photos/" + savedDriver.getId();
			
			FileUploadUtil.cleanDir(uploadDir);
			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
		} else {
			if (cab.getPhotos() == null) {
				cab.setPhotos(null);
			} else if(cab.getPhotos().isEmpty()) {
				cab.setPhotos(null);
			}
			
			cabRepo.save(cab);
		}
	}
	
	public void deleteDriver(Integer id) throws UserNotFoundException {
		Long count = driverRepo.countById(id);
		if(count == null || count == 0) {
			throw new UserNotFoundException("Could not find any driver with ID: " + id);
		}
		
		driverRepo.deleteById(id);
	}

}
