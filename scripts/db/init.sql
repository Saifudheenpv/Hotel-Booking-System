-- Initialize Hotel Booking Database
CREATE DATABASE IF NOT EXISTS hotel_booking_db;
USE hotel_booking_db;

-- Create tables will be handled by JPA auto-ddl
-- This script is for manual database setup if needed

-- Create user for application (optional)
CREATE USER IF NOT EXISTS 'hotel_user'@'%' IDENTIFIED BY 'hotel_password';
GRANT ALL PRIVILEGES ON hotel_booking_db.* TO 'hotel_user'@'%';
FLUSH PRIVILEGES;
