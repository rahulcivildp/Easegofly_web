package com.easygofly.admin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class LogService {
    
    public String readLogsFromFile() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader("logs/application.log"))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
    
    public void generateLog(String logMessage) {
        try {
            String logFilePath = "logs/application.log";

            // Open the log file in append mode
            BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath, true));

            // Write the log message to the file
            writer.write(logMessage);
            writer.newLine();

            // Close the file
            writer.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 
