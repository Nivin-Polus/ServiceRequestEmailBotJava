package com.servicerequest.emailbot.service;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Service;
import java.sql.*;
import java.util.*;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Service
public class DbService {
    private static final Dotenv dotenv = Dotenv.load();
    private static final HikariDataSource dataSource;

    static {
        // DB config
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" 
                + dotenv.get("DB_HOST") + ":" 
                + dotenv.get("DB_PORT", "3306") + "/" 
                + dotenv.get("DB_NAME"));
        config.setUsername(dotenv.get("DB_USER"));
        config.setPassword(dotenv.get("DB_PASS"));
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(config);
    }

    public static Connection getDbConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static Map<String, Object> getCategoriesFromDb() {
        Map<String, Object> categories = new HashMap<>();
        try (Connection conn = getDbConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM categories")) {
            
            while (rs.next()) {
                categories.put(rs.getString("name"), rs.getString("description"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching categories: " + e.getMessage());
            // Return default categories
            categories.put("IT Support", "Information Technology support requests");
            categories.put("HR", "Human Resources requests");
            categories.put("Facilities", "Facilities and maintenance requests");
            categories.put("Finance", "Financial and accounting requests");
        }
        return categories;
    }

    // Thread mappings are now handled by LocalStorageService
    // This service is now READ-ONLY for database operations
}
