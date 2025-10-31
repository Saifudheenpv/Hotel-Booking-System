-- Create database if not exists
CREATE DATABASE IF NOT EXISTS hotel_booking_db;

-- Use the database
USE hotel_booking_db;

-- Create user and grant privileges (optional - for production)
-- CREATE USER IF NOT EXISTS 'hotel_user'@'%' IDENTIFIED BY 'hotel_password';
-- GRANT ALL PRIVILEGES ON hotel_booking_db.* TO 'hotel_user'@'%';
-- FLUSH PRIVILEGES;