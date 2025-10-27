package com.hotel.config;

import com.hotel.model.Hotel;
import com.hotel.model.Room;
import com.hotel.repository.HotelRepository;
import com.hotel.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Override
    public void run(String... args) throws Exception {
        // Only initialize if no hotels exist
        if (hotelRepository.count() == 0) {
            // Sample Hotel 1 - Grand Plaza Hotel
            Hotel hotel1 = new Hotel();
            hotel1.setName("Grand Plaza Hotel");
            hotel1.setLocation("New York");
            hotel1.setDescription("Luxury 5-star hotel in the heart of Manhattan with stunning city views and premium amenities. Experience world-class service and exceptional comfort.");
            hotel1.setRating(4.8);
            hotel1.setImageUrl("https://images.unsplash.com/photo-1564501049412-61c2a3083791?ixlib=rb-4.0.3&auto=format&fit=crop&w=2070&q=80");
            hotel1.setAmenities("Free WiFi,Swimming Pool,Spa,Fitness Center,Restaurant,Bar,24/7 Room Service,Concierge");
            hotelRepository.save(hotel1);

            // Rooms for Hotel 1
            Room room1 = new Room("101", "DELUXE", new BigDecimal("199.99"), hotel1);
            room1.setDescription("Spacious deluxe room with king-sized bed, city view, and modern amenities. Perfect for business travelers and couples.");
            room1.setCapacity(2);
            room1.setAmenities("Air Conditioning,Smart TV,Minibar,Free WiFi,Work Desk,Coffee Maker");
            room1.setImageUrl("https://images.unsplash.com/photo-1631049307264-da0ec9d70304?ixlib=rb-4.0.3&auto=format&fit=crop&w=2070&q=80");

            Room room2 = new Room("102", "SUITE", new BigDecimal("299.99"), hotel1);
            room2.setDescription("Luxury suite with separate living area, balcony, and panoramic city views. Includes premium bathroom amenities.");
            room2.setCapacity(3);
            room2.setAmenities("Air Conditioning,Smart TV,Minibar,Free WiFi,Balcony,Separate Living Area");
            room2.setImageUrl("https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?ixlib=rb-4.0.3&auto=format&fit=crop&w=2070&q=80");

            Room room3 = new Room("103", "EXECUTIVE", new BigDecimal("249.99"), hotel1);
            room3.setDescription("Executive room with enhanced workspace, premium bedding, and exclusive lounge access.");
            room3.setCapacity(2);
            room3.setAmenities("Air Conditioning,Smart TV,Minibar,Free WiFi,Executive Lounge Access");
            room3.setImageUrl("https://images.unsplash.com/photo-1611892440504-42a792e24d32?ixlib=rb-4.0.3&auto=format&fit=crop&w=2070&q=80");

            Room room4 = new Room("104", "PRESIDENTIAL", new BigDecimal("499.99"), hotel1);
            room4.setDescription("Ultra-luxurious presidential suite with private dining area, butler service, and breathtaking city views.");
            room4.setCapacity(4);
            room4.setAmenities("Air Conditioning,Multiple TVs,Minibar,Free WiFi,Private Dining,Butler Service");
            room4.setImageUrl("https://images.unsplash.com/photo-1590490360182-c33d57733427?ixlib=rb-4.0.3&auto=format&fit=crop&w=2070&q=80");

            roomRepository.saveAll(Arrays.asList(room1, room2, room3, room4));

            // Sample Hotel 2 - Beach Resort & Spa
            Hotel hotel2 = new Hotel();
            hotel2.setName("Beach Resort & Spa");
            hotel2.setLocation("Miami");
            hotel2.setDescription("Beautiful beachfront resort with private beach access, world-class spa facilities, and tropical gardens.");
            hotel2.setRating(4.6);
            hotel2.setImageUrl("https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?ixlib=rb-4.0.3&auto=format&fit=crop&w=2070&q=80");
            hotel2.setAmenities("Free WiFi,Swimming Pool,Private Beach,Spa,Fitness Center,Beach Bar,Water Sports");
            hotelRepository.save(hotel2);

            // Rooms for Hotel 2
            Room room5 = new Room("201", "SINGLE", new BigDecimal("129.99"), hotel2);
            room5.setDescription("Comfortable single room with ocean view, perfect for solo travelers seeking relaxation.");
            room5.setCapacity(1);
            room5.setAmenities("Air Conditioning,TV,Free WiFi,Ocean View,Balcony");
            room5.setImageUrl("https://images.unsplash.com/photo-1540518614846-7eded1028d50?ixlib=rb-4.0.3&auto=format&fit=crop&w=2070&q=80");

            Room room6 = new Room("202", "DOUBLE", new BigDecimal("179.99"), hotel2);
            room6.setDescription("Double room with two queen beds, balcony, and partial ocean view. Ideal for families.");
            room6.setCapacity(4);
            room6.setAmenities("Air Conditioning,TV,Free WiFi,Balcony,Ocean View");
            room6.setImageUrl("https://images.unsplash.com/photo-1590490360182-c33d57733427?ixlib=rb-4.0.3&auto=format&fit=crop&w=2070&q=80");

            Room room7 = new Room("203", "BEACHFRONT SUITE", new BigDecimal("349.99"), hotel2);
            room7.setDescription("Luxurious beachfront suite with direct beach access, private terrace, and premium amenities.");
            room7.setCapacity(3);
            room7.setAmenities("Air Conditioning,TV,Free WiFi,Private Terrace,Direct Beach Access");
            room7.setImageUrl("https://images.unsplash.com/photo-1566073771259-6a8506099945?ixlib=rb-4.0.3&auto=format&fit=crop&w=2070&q=80");

            roomRepository.saveAll(Arrays.asList(room5, room6, room7));

            // Sample Hotel 3 - Mountain View Lodge
            Hotel hotel3 = new Hotel();
            hotel3.setName("Mountain View Lodge");
            hotel3.setLocation("Switzerland");
            hotel3.setDescription("Cozy alpine lodge nestled in the Swiss Alps with breathtaking mountain views, ski access, and rustic charm.");
            hotel3.setRating(4.9);
            hotel3.setImageUrl("https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?ixlib=rb-4.0.3&auto=format&fit=crop&w=2070&q=80");
            hotel3.setAmenities("Free WiFi,Fireplace,Ski Storage,Restaurant,Bar,Spa,Ski Rental");
            hotelRepository.save(hotel3);

            // Rooms for Hotel 3
            Room room8 = new Room("301", "DOUBLE", new BigDecimal("159.99"), hotel3);
            room8.setDescription("Cozy double room with mountain view, fireplace, and traditional alpine decor.");
            room8.setCapacity(2);
            room8.setAmenities("Air Conditioning,TV,Fireplace,Free WiFi,Mountain View,Balcony");
            room8.setImageUrl("https://images.unsplash.com/photo-1566665797739-1674de7a421a?ixlib=rb-4.0.3&auto=format&fit=crop&w=2070&q=80");

            Room room9 = new Room("302", "SUITE", new BigDecimal("249.99"), hotel3);
            room9.setDescription("Luxury suite with private balcony, panoramic mountain views, and separate living area.");
            room9.setCapacity(4);
            room9.setAmenities("Air Conditioning,TV,Fireplace,Free WiFi,Balcony,Mountain View,Separate Living Area");
            room9.setImageUrl("https://images.unsplash.com/photo-1611892440504-42a792e24d32?ixlib=rb-4.0.3&auto=format&fit=crop&w=2070&q=80");

            Room room10 = new Room("303", "FAMILY ROOM", new BigDecimal("199.99"), hotel3);
            room10.setDescription("Spacious family room with bunk beds, mountain view, and extra space for children.");
            room10.setCapacity(5);
            room10.setAmenities("Air Conditioning,TV,Fireplace,Free WiFi,Mountain View,Bunk Beds");
            room10.setImageUrl("https://images.unsplash.com/photo-1586105251261-72a756497a11?ixlib=rb-4.0.3&auto=format&fit=crop&w=2070&q=80");

            roomRepository.saveAll(Arrays.asList(room8, room9, room10));

            // Sample Hotel 4 - City Central Hotel
            Hotel hotel4 = new Hotel();
            hotel4.setName("City Central Hotel");
            hotel4.setLocation("London");
            hotel4.setDescription("Modern hotel in the heart of London with easy access to major attractions, shopping, and business districts.");
            hotel4.setRating(4.4);
            hotel4.setImageUrl("https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?ixlib=rb-4.0.3&auto=format&fit=crop&w=2070&q=80");
            hotel4.setAmenities("Free WiFi,Fitness Center,Restaurant,Bar,Business Center,Concierge");
            hotelRepository.save(hotel4);

            // Rooms for Hotel 4
            Room room11 = new Room("401", "STANDARD", new BigDecimal("89.99"), hotel4);
            room11.setDescription("Comfortable standard room with all essential amenities for a pleasant stay.");
            room11.setCapacity(2);
            room11.setAmenities("Air Conditioning,TV,Free WiFi,Work Desk");
            room11.setImageUrl("https://images.unsplash.com/photo-1631049307264-da0ec9d70304?ixlib=rb-4.0.3&auto=format&fit=crop&w=2070&q=80");

            Room room12 = new Room("402", "SUPERIOR", new BigDecimal("129.99"), hotel4);
            room12.setDescription("Superior room with extra space, enhanced amenities, and city views.");
            room12.setCapacity(2);
            room12.setAmenities("Air Conditioning,Smart TV,Free WiFi,Work Desk,Coffee Maker");
            room12.setImageUrl("https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?ixlib=rb-4.0.3&auto=format&fit=crop&w=2070&q=80");

            roomRepository.saveAll(Arrays.asList(room11, room12));
        }
    }
}