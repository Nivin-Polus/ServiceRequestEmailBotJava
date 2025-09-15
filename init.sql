-- Database initialization script for Service Request Email Bot
-- This script creates the necessary tables for the application

CREATE DATABASE IF NOT EXISTS emailbot;
USE emailbot;

-- Create categories table
CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create types table
CREATE TABLE IF NOT EXISTS types (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    category_id INT,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Note: service_requests and email_threads tables removed
-- All tracking data is now stored locally in JSON files
-- This database is READ-ONLY for the application

-- Insert sample categories
INSERT INTO categories (code, name, description) VALUES
('HARDWARE', 'Hardware Issues', 'Hardware related service requests'),
('SOFTWARE', 'Software Issues', 'Software related service requests'),
('NETWORK', 'Network Issues', 'Network and connectivity issues'),
('ACCESS', 'Access Requests', 'Account and access related requests'),
('OTHER', 'Other', 'General or miscellaneous requests');

-- Insert sample types
INSERT INTO types (code, name, category_id, description) VALUES
('LAPTOP_ISSUE', 'Laptop Problem', 1, 'Laptop hardware issues'),
('PRINTER_ISSUE', 'Printer Problem', 1, 'Printer related issues'),
('APP_INSTALL', 'Application Installation', 2, 'Software installation requests'),
('APP_ISSUE', 'Application Issue', 2, 'Software application problems'),
('WIFI_ISSUE', 'WiFi Problem', 3, 'Wireless network connectivity issues'),
('VPN_ISSUE', 'VPN Problem', 3, 'VPN connection issues'),
('ACCOUNT_ACCESS', 'Account Access', 4, 'User account access requests'),
('PASSWORD_RESET', 'Password Reset', 4, 'Password reset requests'),
('GENERAL', 'General Request', 5, 'General service requests');

-- Create indexes for better performance on read-only tables
CREATE INDEX idx_categories_code ON categories(code);
CREATE INDEX idx_types_code ON types(code);
CREATE INDEX idx_types_category_id ON types(category_id);
