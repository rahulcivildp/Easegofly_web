package com.easygofly.city;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;

import com.easygofly.entity.City;
import com.easygofly.entity.Country;
import com.easygofly.entity.TBOCity;
import com.easygofly.entity.TBObusCity;
import com.easygofly.site.EasyGoFlyFrontEndApplication;
import com.easygofly.site.bus.BusCityRepository;
import com.easygofly.site.flight.CityRepository;
import com.easygofly.site.flight.TBOCityRepository;
import com.easygofly.site.setting.CountryRepository;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
@ContextConfiguration(classes = EasyGoFlyFrontEndApplication.class)
public class CityRepositoryTest {

	@Autowired private CityRepository cityRepo;
	@Autowired private CountryRepository countryRepo ;
	@Autowired private TBOCityRepository tboRepo ;
	@Autowired private BusCityRepository busCityRepo;
	
	@Test
	public void testUpdateCity() {
		Integer id = 1;
		City city = cityRepo.findById(id).get();
		
		city.setCityName("Kolkata");
		
		cityRepo.save(city);
	}
	
	@Test
	public void testUpdateCities() {
		Integer id = 54;
		City city1 = cityRepo.findById(id).get(); city1.setCityName("Haryana");
		City city2 = cityRepo.findById(id+1).get(); city2.setCityName("Dharamshala");
		City city3 = cityRepo.findById(id+2).get(); city3.setCityName("Manali");
		City city4 = cityRepo.findById(id+3).get(); city4.setCityName("Shimla");
		City city5 = cityRepo.findById(id+4).get(); city5.setCityName("Jammu");
		City city6 = cityRepo.findById(id+5).get(); city6.setCityName("Bokaro");
		City city7 = cityRepo.findById(id+6).get(); city7.setCityName("Jamshedpur");
		City city8 = cityRepo.findById(id+7).get(); city8.setCityName("Ranchi");
		City city9 = cityRepo.findById(id+8).get(); city9.setCityName("Karnataka");
		City city10 = cityRepo.findById(id+9).get(); city10.setCityName("Belgaum, Karnataka.");
		City city11 = cityRepo.findById(id+10).get(); city11.setCityName("Mysore, Karnataka.");
		City city12 = cityRepo.findById(id+11).get(); city12.setCityName("Kalaburagi, Karnataka.");
		City city13 = cityRepo.findById(id+12).get(); city13.setCityName("Dharwad, Karnataka.");
		City city14 = cityRepo.findById(id+13).get(); city14.setCityName("Lakshadweep");
		City city15 = cityRepo.findById(id+14).get(); city15.setCityName("Khajuraho");
		City city16 = cityRepo.findById(id+15).get(); city16.setCityName("Satna");
		City city17 = cityRepo.findById(id+16).get(); city17.setCityName("Jabalpur");
		City city18 = cityRepo.findById(id+17).get(); city18.setCityName("Bhopal");
		City city19 = cityRepo.findById(id+18).get(); city19.setCityName("Gwalior");
		City city20 = cityRepo.findById(id+19).get(); city20.setCityName("Indore");
		City city21 = cityRepo.findById(id+20).get(); city21.setCityName("Shirdi");
		City city22 = cityRepo.findById(id+21).get(); city22.setCityName("Nasik");
		City city23 = cityRepo.findById(id+22).get(); city23.setCityName("Nanded");
		City city24 = cityRepo.findById(id+23).get(); city24.setCityName("Kolhapur");
		City city25 = cityRepo.findById(id+24).get(); city25.setCityName("Aurangabad");
		City city26 = cityRepo.findById(id+25).get(); city26.setCityName("Jalgaon");
		City city27 = cityRepo.findById(id+26).get(); city27.setCityName("Shillong");
		City city28 = cityRepo.findById(id+27).get(); city28.setCityName("Aizawl");
		City city29 = cityRepo.findById(id+28).get(); city29.setCityName("Dimapur");
		City city30 = cityRepo.findById(id+29).get(); city30.setCityName("Jaisalmer");
		City city31 = cityRepo.findById(id+30).get(); city31.setCityName("Jeypore");
		City city32 = cityRepo.findById(id+31).get(); city32.setCityName("Puducherry");
		City city33 = cityRepo.findById(id+32).get(); city33.setCityName("Pueblo");
		City city34 = cityRepo.findById(id+33).get(); city34.setCityName("Pathankot");
		City city35 = cityRepo.findById(id+34).get(); city35.setCityName("Sahnewal");
		City city36 = cityRepo.findById(id+35).get(); city36.setCityName("Adampur");
		City city37 = cityRepo.findById(id+36).get(); city37.setCityName("Bikaner");
		City city38 = cityRepo.findById(id+37).get(); city38.setCityName("Kota");
		City city39 = cityRepo.findById(id+38).get(); city39.setCityName("Ajmer");
		City city40 = cityRepo.findById(id+39).get(); city40.setCityName("Jodhpur");
		City city41 = cityRepo.findById(id+40).get(); city41.setCityName("Udaipur");
		City city42 = cityRepo.findById(id+41).get(); city42.setCityName("Jaisalmer");
		City city43 = cityRepo.findById(id+42).get(); city43.setCityName("Pakyong");
		City city44 = cityRepo.findById(id+43).get(); city44.setCityName("Tuticorin");
		City city45 = cityRepo.findById(id+44).get(); city45.setCityName("Salem");
		City city46 = cityRepo.findById(id+45).get(); city46.setCityName("Agartala");
		City city47 = cityRepo.findById(id+46).get(); city47.setCityName("Kanpur");
		City city48 = cityRepo.findById(id+47).get(); city48.setCityName("Ghaziabad");
		City city49 = cityRepo.findById(id+48).get(); city49.setCityName("Agra");
		City city50 = cityRepo.findById(id+49).get(); city50.setCityName("Gorakhpur");
		City city51 = cityRepo.findById(id+50).get(); city51.setCityName("Allahabad");
		City city52 = cityRepo.findById(id+51).get(); city52.setCityName("Dehradun");
		City city53 = cityRepo.findById(id+52).get(); city53.setCityName("Kumaon");
		City city54 = cityRepo.findById(id+53).get(); city54.setCityName("Pantnagar");
		City city55 = cityRepo.findById(id+54).get(); city55.setCityName("Durgapur");
		City city56 = cityRepo.findById(id+55).get(); city56.setCityName("Cooch Behar");
		
		cityRepo.saveAll(List.of(city1, city2, city3, city4, city5, city6, city7, city8, city9, city10, city11, city12, city13, city14, city15, city16, city17, city18, city19, city20, city21, city22, city23, city24, city25, 
				city26, city27, city28, city29, city30, city31, city32, city33, city34, city35, city36, city37, city38, city39, city40, city41, city42, city43, city44, city45, city46, city47, city48, city49, city50, city51, city52,
				city53, city54, city55, city56));
	}
	
	@Test
	public void testSetCountry() {
		Country country = countryRepo.findById(106).get();
		Iterable<City> cities = cityRepo.findAll();
		for (City city : cities) {
			city.setCountry(country);
		}
	}

	@Test
	public void testSaveTBOCity() {
		
		readDataFromCustomSeparator("G:\\Web Project\\EasyGoFly\\EasyGoFlyProject\\EasyGoFlyWebParent\\xml-data\\NewCityListHotel.csv");
		
	}
	
	public void readDataFromCustomSeparator(String file) { 
        try { 
            // Create object of filereader 
            // class with csv file as parameter. 
            FileReader filereader = new FileReader(file); 
  
            // create csvParser object with 
            // custom separator semi-colon 
            CSVParser parser = new CSVParserBuilder().withSeparator(';').build(); 
  
            // create csvReader object with 
            // parameter filereader and parser 
            CSVReader csvReader = new CSVReaderBuilder(filereader) 
                                      .withCSVParser(parser) 
                                      .build(); 
  
            // Read all data at once 
            List<String[]> allData = csvReader.readAll(); 
            
            //Create TBO City list
            List<TBOCity> cities = new ArrayList<TBOCity>();
  
            // print Data 
            for (String[] row : allData) { 
                for (String cell : row) { 
                	TBOCity city = new TBOCity();
                	String[] cellSplit = cell.split(",");
                	city.setCityId(Integer.parseInt(cellSplit[0]));
                	city.setDestination(cellSplit[1]);
                	city.setStateProvince(cellSplit[2]);
                	city.setStateProvinceCode(cellSplit[3]);
                	city.setCountry(cellSplit[4]);
                	city.setCountryCode(cellSplit[5]);
                	
                	
//                    System.out.print(city.getCityId() + " - " + city.getDestination() + " - " + city.getStateProvince() + " - " + city.getStateProvinceCode() + " - " + city.getCountry() 
//                    + " - " + city.getCountryCode() + "\t"); 
                    
                    cities.add(city);
                } 
            } 
            
            System.out.println(cities.size());
            tboRepo.saveAll(cities);
        } 
        catch (Exception e) { 
            e.printStackTrace(); 
        } 
    } 
	



}
