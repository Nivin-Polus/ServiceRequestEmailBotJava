package com.servicerequest.emailbot.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Repository
public class DbService {
    
    @Autowired
    private DataSource dataSource;

    public Connection getDbConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public Map<String, Object> getCategoriesFromDb() {
        Map<String, Object> categories = new HashMap<>();
        try (Connection conn = getDbConnection();
             Statement stmt = conn.createStatement()) {
            
            // Try to query categories table, but handle if it doesn't exist (H2 development mode)
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM categories")) {
                while (rs.next()) {
                    categories.put(rs.getString("name"), rs.getString("description"));
                }
            } catch (SQLException e) {
                // Table doesn't exist or other SQL error - return default categories
                System.out.println("Categories table not found, using default categories");
            }
        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
        }
        
        // Always ensure we have default categories
        if (categories.isEmpty()) {
            categories.put("IT Support", "Information Technology support requests");
            categories.put("HR", "Human Resources requests");
            categories.put("Facilities", "Facilities and maintenance requests");
            categories.put("Finance", "Financial and accounting requests");
            categories.put("General", "General inquiries and requests");
        }
        
        return categories;
    }

    public Integer getCategoryCode(String categoryName) {
        Map<String, Integer> categoryCodeMap = new HashMap<>();
        categoryCodeMap.put("IT Support", 61);
        categoryCodeMap.put("Hardware Issue", 61);
        categoryCodeMap.put("Software Issue", 62);
        categoryCodeMap.put("Network Issue", 63);
        categoryCodeMap.put("HR", 64);
        categoryCodeMap.put("Facilities", 65);
        categoryCodeMap.put("Finance", 66);
        categoryCodeMap.put("General", 67);
        
        return categoryCodeMap.getOrDefault(categoryName, 61); // Default to IT Support
    }
    
    public Integer getTypeCode(String typeName, Integer categoryCode) {
        Map<String, Integer> typeCodeMap = new HashMap<>();
        typeCodeMap.put("Hardware Issue", 108);
        typeCodeMap.put("Software Issue", 109);
        typeCodeMap.put("Network Issue", 110);
        typeCodeMap.put("Access Request", 111);
        typeCodeMap.put("Password Reset", 112);
        typeCodeMap.put("Email Issue", 113);
        typeCodeMap.put("Printer Issue", 114);
        typeCodeMap.put("General Request", 115);
        
        return typeCodeMap.getOrDefault(typeName, 108); // Default to Hardware Issue
    }

    // Thread mappings are now handled by LocalStorageService
    // This service is now READ-ONLY for database operations
}
