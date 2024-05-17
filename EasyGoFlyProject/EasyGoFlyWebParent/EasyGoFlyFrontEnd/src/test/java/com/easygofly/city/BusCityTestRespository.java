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

import com.easygofly.entity.TBObusCity;
import com.easygofly.site.EasyGoFlyFrontEndApplication;
import com.easygofly.site.bus.BusCityRepository;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
@ContextConfiguration(classes = EasyGoFlyFrontEndApplication.class)
public class BusCityTestRespository {
	@Autowired private BusCityRepository busCityRepo;

	@Test
	public void testSaveTBObusCity() {
		
		readDataFromCustomSeparatorBusCity("G:\\Web Project\\EasyGoFly\\EasyGoFlyProject\\EasyGoFlyWebParent\\EasyGoFlyFrontEnd\\bus-city\\Test.csv");
		
	}

	
	public void readDataFromCustomSeparatorBusCity(String file) { 
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
            List<TBObusCity> cities = new ArrayList<TBObusCity>();
  
            // print Data 
            for (String[] row : allData) { 
                for (String cell : row) { 
                	TBObusCity city = new TBObusCity();
                	String[] cellSplit = cell.split(",");
                	city.setCityId(Integer.parseInt(cellSplit[0]));
                	if (cellSplit.length == 3 ) {
						if (cellSplit[1].charAt(0) == '"') {
							StringBuilder builder = new StringBuilder(cellSplit[1]);
							builder.deleteCharAt(0);
							cellSplit[1] = builder.toString();
						}
                		String destina = cellSplit[1] +", "+ cellSplit[2];
                    	city.setCityName(destina);
					} else {
                    	city.setCityName(cellSplit[1]);
					}
                	
	                System.out.println(city.getCityId() + " " + city.getCityName());
                	
                    cities.add(city);
                } 
            } 
            
            System.out.println(cities.size());
            busCityRepo.saveAll(cities);
        } 
        catch (Exception e) { 
            e.printStackTrace(); 
        } 
    } 
	
	@Test
	public void testCSV() {
		writeFromJSONtoCSV();
	}
	
	public void writeFromJSONtoCSV() {
		// Class data members 
        String jsonString; 
        JSONObject jsonObject; 
  
        // Try block to check for exceptions 
        try { 
  
            // Step 1: Reading the contents of the JSON file 
            // using readAllBytes() method and 
            // storing the result in a string 
            jsonString = new String( 
                Files.readAllBytes(Paths.get("file.json"))); 
  
            // Step 2: Construct a JSONObject using above 
            // string 
            jsonObject = new JSONObject(jsonString); 
  
            // Step 3: Fetching the JSON Array test 
            // from the JSON Object 
            JSONArray docs 
                = jsonObject.getJSONArray("BusCities"); 
  
            // Step 4: Create a new CSV file using 
            //  the package java.io.File 
            File file = new File("G:\\Web Project\\Test.csv"); 
  
            // Step 5: Produce a comma delimited text from 
            // the JSONArray of JSONObjects 
            // and write the string to the newly created CSV 
            // file 
  
            String csvString = CDL.toString(docs); 
            FileUtils.writeStringToFile(file, csvString); 
        } 
  
        // Catch block to handle exceptions 
        catch (Exception e) { 
  
            // Display exceptions on console with line 
            // number using printStackTrace() method 
            e.printStackTrace(); 
        } 
	}

}
