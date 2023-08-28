package com.easygofly.site;

import java.io.BufferedReader;
import java.io.FileReader;
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
} 
