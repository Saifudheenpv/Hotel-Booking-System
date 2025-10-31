package com.hotel.config;

import com.hotel.model.Hotel;
import com.hotel.model.Room;
import com.hotel.repository.HotelRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private Environment env;

    @Override
    public void run(String... args) throws Exception {
        if (!shouldInitializeData()) {
            System.out.println("Skipping data initialization for test profile");
            return;
        }
        
        // Clear existing data in correct order (to avoid foreign key constraints)
        bookingRepository.deleteAll();
        roomRepository.deleteAll();
        hotelRepository.deleteAll();
        
        initializeHotelsAndRooms();
        System.out.println("Data initialization completed! Total hotels: " + hotelRepository.count());
    }

    private boolean shouldInitializeData() {
        // Check if we're in test profile
        String[] activeProfiles = env.getActiveProfiles();
        for (String profile : activeProfiles) {
            if ("test".equals(profile)) {
                return false; // Don't initialize data in tests
            }
        }
        return true;
    }

    private void initializeHotelsAndRooms() {
        // Luxury Hotels (10)
        List<Hotel> luxuryHotels = Arrays.asList(
            createHotel("The Ritz Carlton", "New York", 4.9, 
                "Iconic luxury hotel offering unparalleled service in Manhattan",
                "https://images.unsplash.com/photo-1564501049412-61c2a3083791",
                "Spa,Fine Dining,Concierge,Valet,Butler Service"),
                
            createHotel("Four Seasons Hotel", "Paris", 4.8,
                "Elegant hotel with stunning views of the Eiffel Tower",
                "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa",
                "Michelin Restaurant,Spa,Rooftop Pool,Luxury Suites"),
                
            createHotel("St. Regis", "Dubai", 4.9,
                "Ultra-luxury hotel in the heart of Downtown Dubai",
                "https://images.unsplash.com/photo-1512917774080-9991f1c4c750",
                "Infinity Pool,Private Beach,Butler,Helipad"),
                
            createHotel("Mandarin Oriental", "Tokyo", 4.8,
                "Sophisticated luxury with traditional Japanese hospitality",
                "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb",
                "Spa,Fine Dining,Tea Ceremony,Concierge"),
                
            createHotel("The Plaza", "New York", 4.7,
                "Historic luxury hotel overlooking Central Park",
                "https://images.unsplash.com/photo-1586375300773-8384e3e4916f",
                "Afternoon Tea,Piano Bar,Luxury Shopping"),
                
            createHotel("Burj Al Arab", "Dubai", 5.0,
                "World's most luxurious hotel shaped like a sail",
                "https://images.unsplash.com/photo-1513584684374-8bab748fbf90",
                "Private Beach,Helicopter Transfer,Gold Plated interiors"),
                
            createHotel("The Savoy", "London", 4.8,
                "Legendary luxury hotel on the Strand",
                "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4",
                "American Bar,Afternoon Tea,River Views"),
                
            createHotel("Hotel de Paris", "Monte Carlo", 4.9,
                "Belle Époque palace in the heart of Monaco",
                "https://images.unsplash.com/photo-1566073771259-6a8506099945",
                "Casino,Fine Dining,Luxury Spa"),
                
            createHotel("Aman Tokyo", "Tokyo", 4.9,
                "Urban sanctuary with traditional Japanese design",
                "https://images.unsplash.com/photo-1540483761890-a1f7be05d99f",
                "Spa,Japanese Garden,City Views"),
                
            createHotel("The Langham", "London", 4.7,
                "Historic luxury hotel with modern amenities",
                "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa",
                "Palm Court,Art Collection,Luxury Suites")
        );

        // Business Hotels (10)
        List<Hotel> businessHotels = Arrays.asList(
            createHotel("Hilton Times Square", "New York", 4.3,
                "Modern business hotel in the heart of Times Square",
                "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb",
                "Business Center,Meeting Rooms,Concierge"),
                
            createHotel("Marriott Marquis", "San Francisco", 4.4,
                "Contemporary hotel perfect for business travelers",
                "https://images.unsplash.com/photo-1566073771259-6a8506099945",
                "Executive Lounge,Fitness Center,Rooftop Bar"),
                
            createHotel("Sheraton Grand", "Chicago", 4.2,
                "Upscale hotel in downtown Chicago",
                "https://images.unsplash.com/photo-1513584684374-8bab748fbf90",
                "Conference Facilities,Pool,Spa"),
                
            createHotel("Westin", "Boston", 4.3,
                "Wellness-focused business hotel",
                "https://images.unsplash.com/photo-1586375300773-8384e3e4916f",
                "Heavenly Bed,Workout Gear Lending"),
                
            createHotel("Hyatt Regency", "Tokyo", 4.4,
                "Modern hotel in Shinjuku business district",
                "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb",
                "Business Center,Multiple Restaurants"),
                
            createHotel("InterContinental", "Sydney", 4.5,
                "Luxury business hotel with harbor views",
                "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4",
                "Harbor Views,Executive Club"),
                
            createHotel("Radisson Blu", "Berlin", 4.2,
                "Contemporary hotel near government district",
                "https://images.unsplash.com/photo-1564501049412-61c2a3083791",
                "Free WiFi,Meeting Rooms"),
                
            createHotel("Novotel", "Paris", 4.1,
                "Modern hotel perfect for business trips",
                "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa",
                "Business Center,Fitness Room"),
                
            createHotel("Crowne Plaza", "Singapore", 4.3,
                "Upscale hotel in financial district",
                "https://images.unsplash.com/photo-1512917774080-9991f1c4c750",
                "Pool,Executive Lounge"),
                
            createHotel("DoubleTree", "London", 4.2,
                "Comfortable business hotel with warm service",
                "https://images.unsplash.com/photo-1586375300773-8384e3e4916f",
                "Warm Cookie Welcome,Business Facilities")
        );

        // Beach Resorts (10)
        List<Hotel> beachResorts = Arrays.asList(
            createHotel("Four Seasons Resort", "Maldives", 4.9,
                "Overwater bungalows in paradise",
                "https://images.unsplash.com/photo-1540541338287-41700207dee6",
                "Private Beach,Overwater Villas,Spa"),
                
            createHotel("St. Regis Bora Bora", "Bora Bora", 5.0,
                "Luxury overwater villas with mountain views",
                "https://images.unsplash.com/photo-1571896349842-33c89424de2d",
                "Overwater Bungalows,Private Pool"),
                
            createHotel("Atlantis Paradise", "Bahamas", 4.6,
                "Massive resort with water park and marine habitat",
                "https://images.unsplash.com/photo-1566073771259-6a8506099945",
                "Water Park,Marine Habitat,Casino"),
                
            createHotel("Sandals Resort", "Jamaica", 4.7,
                "All-inclusive luxury couples resort",
                "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4",
                "All Inclusive,Private Beach"),
                
            createHotel("Amanpuri", "Phuket", 4.9,
                "Secluded luxury resort on private peninsula",
                "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa",
                "Private Beach,Pavilions,Spa"),
                
            createHotel("One&Only", "Mauritius", 4.8,
                "Luxury resort with pristine beaches",
                "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb",
                "Private Villas,Golf Course"),
                
            createHotel("Soneva Fushi", "Maldives", 4.9,
                "Eco-luxury resort with Robinson Crusoe vibe",
                "https://images.unsplash.com/photo-1513584684374-8bab748fbf90",
                "Eco Friendly,Private Residences"),
                
            createHotel("Six Senses", "Fiji", 4.8,
                "Sustainable luxury with stunning ocean views",
                "https://images.unsplash.com/photo-1586375300773-8384e3e4916f",
                "Sustainability Focus,Spa"),
                
            createHotel("Belmond", "Bali", 4.7,
                "Luxury resort in tropical paradise",
                "https://images.unsplash.com/photo-1564501049412-61c2a3083791",
                "Rice Terrace Views,Infinity Pool"),
                
            createHotel("Rosewood", "Phuket", 4.8,
                "Luxury beachfront resort with pavilions",
                "https://images.unsplash.com/photo-1512917774080-9991f1c4c750",
                "Beachfront,Pavilions,Spa")
        );

        // Boutique Hotels (10)
        List<Hotel> boutiqueHotels = Arrays.asList(
            createHotel("Ace Hotel", "New York", 4.4,
                "Trendy boutique hotel in Manhattan",
                "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb",
                "Artistic Design,Rooftop Bar"),
                
            createHotel("The Hoxton", "London", 4.5,
                "Stylish hotel with co-working spaces",
                "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa",
                "Co-working,Lively Lobby"),
                
            createHotel("Hotel Costes", "Paris", 4.6,
                "Chic boutique hotel with famous bar",
                "https://images.unsplash.com/photo-1566073771259-6a8506099945",
                "Famous Bar,Designer Interiors"),
                
            createHotel("The Standard", "Los Angeles", 4.4,
                "Modern boutique with rooftop pool",
                "https://images.unsplash.com/photo-1513584684374-8bab748fbf90",
                "Rooftop Pool,Modern Design"),
                
            createHotel("Casa Cook", "Rhodes", 4.6,
                "Boho-chic beach club hotel",
                "https://images.unsplash.com/photo-1586375300773-8384e3e4916f",
                "Beach Club,Boho Design"),
                
            createHotel("The Dean", "Dublin", 4.3,
                "Creative hotel with rooftop terrace",
                "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4",
                "Rooftop,Artistic"),
                
            createHotel("The Thief", "Oslo", 4.7,
                "Design hotel with art focus",
                "https://images.unsplash.com/photo-1564501049412-61c2a3083791",
                "Art Collection,Rooftop Sauna"),
                
            createHotel("Hotel Unique", "São Paulo", 4.5,
                "Architectural masterpiece hotel",
                "https://images.unsplash.com/photo-1512917774080-9991f1c4c750",
                "Unique Architecture,Rooftop Pool"),
                
            createHotel("The Dylan", "Amsterdam", 4.6,
                "Historic boutique on Keizersgracht canal",
                "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb",
                "Canal Views,Historic Building"),
                
            createHotel("Château Voltaire", "Paris", 4.7,
                "Intimate boutique near Palais Royal",
                "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa",
                "Intimate,Designer Rooms")
        );

        // Budget Hotels (10)
        List<Hotel> budgetHotels = Arrays.asList(
            createHotel("Ibis Styles", "Berlin", 3.9,
                "Colorful budget hotel with free breakfast",
                "https://images.unsplash.com/photo-1566073771259-6a8506099945",
                "Free Breakfast,Colorful Design"),
                
            createHotel("Premier Inn", "London", 4.0,
                "Reliable budget hotel chain",
                "https://images.unsplash.com/photo-1513584684374-8bab748fbf90",
                "Comfortable Beds,Good Value"),
                
            createHotel("Motel One", "Munich", 4.1,
                "Design budget hotels",
                "https://images.unsplash.com/photo-1586375300773-8384e3e4916f",
                "Design Interiors,Great Locations"),
                
            createHotel("Holiday Inn Express", "New York", 3.8,
                "Comfortable budget option",
                "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4",
                "Free Breakfast,Comfortable"),
                
            createHotel("Travelodge", "Manchester", 3.7,
                "Simple and affordable accommodation",
                "https://images.unsplash.com/photo-1564501049412-61c2a3083791",
                "Affordable,Central Locations"),
                
            createHotel("EasyHotel", "Barcelona", 3.6,
                "Basic no-frills budget option",
                "https://images.unsplash.com/photo-1512917774080-9991f1c4c750",
                "Budget Friendly,Central"),
                
            createHotel("Tune Hotels", "Kuala Lumpur", 3.8,
                "Pay-for-what-you-use concept",
                "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb",
                "Pay Per Service,Modern"),
                
            createHotel("Formule 1", "Paris", 3.5,
                "Ultra-budget modular rooms",
                "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa",
                "Budget,Modular Rooms"),
                
            createHotel("Red Roof Inn", "Chicago", 3.6,
                "Economy hotel chain",
                "https://images.unsplash.com/photo-1566073771259-6a8506099945",
                "Economy,Pet Friendly"),
                
            createHotel("Super 8", "Las Vegas", 3.5,
                "Classic American budget chain",
                "https://images.unsplash.com/photo-1513584684374-8bab748fbf90",
                "Budget,Swimming Pool")
        );

        // Combine all hotels properly
        List<Hotel> allHotels = new ArrayList<>();
        allHotels.addAll(luxuryHotels);
        allHotels.addAll(businessHotels);
        allHotels.addAll(beachResorts);
        allHotels.addAll(boutiqueHotels);
        allHotels.addAll(budgetHotels);
        
        // Save all hotels first
        hotelRepository.saveAll(allHotels);
        
        // Create rooms for all hotels
        allHotels.forEach(this::createRoomsForHotel);
        
        System.out.println("Initialized " + allHotels.size() + " hotels with rooms");
    }

    private Hotel createHotel(String name, String location, double rating, 
                            String description, String imageUrl, String amenities) {
        Hotel hotel = new Hotel();
        hotel.setName(name);
        hotel.setLocation(location);
        hotel.setRating(rating);
        hotel.setDescription(description);
        hotel.setImageUrl(imageUrl);
        hotel.setAmenities(amenities);
        
        // Add starting price based on hotel type/rating with whole numbers
        int minPrice, maxPrice;
        
        if (rating >= 4.5) {
            minPrice = 300;
            maxPrice = 500; // $300-500 for luxury
        } else if (rating >= 4.0) {
            minPrice = 150;
            maxPrice = 300; // $150-300 for premium
        } else if (rating >= 3.5) {
            minPrice = 80;
            maxPrice = 150; // $80-150 for standard
        } else {
            minPrice = 50;
            maxPrice = 80; // $50-80 for budget
        }
        
        // Generate whole number price
        int price = minPrice + (int)(Math.random() * (maxPrice - minPrice + 1));
        hotel.setStartingPrice((double) price);
        
        return hotel;
    }

    private void createRoomsForHotel(Hotel hotel) {
        List<Room> rooms = Arrays.asList(
            // Standard Rooms
            createRoom("101", "STANDARD", new BigDecimal("89.99"), true, hotel),
            createRoom("102", "STANDARD", new BigDecimal("89.99"), true, hotel),
            createRoom("103", "STANDARD", new BigDecimal("89.99"), true, hotel),
            
            // Deluxe Rooms
            createRoom("201", "DELUXE", new BigDecimal("149.99"), true, hotel),
            createRoom("202", "DELUXE", new BigDecimal("149.99"), true, hotel),
            
            // Suite Rooms
            createRoom("301", "SUITE", new BigDecimal("249.99"), true, hotel),
            createRoom("302", "SUITE", new BigDecimal("249.99"), true, hotel),
            
            // Executive Rooms (for business hotels)
            createRoom("401", "EXECUTIVE", new BigDecimal("199.99"), true, hotel),
            
            // Family Rooms
            createRoom("501", "FAMILY", new BigDecimal("179.99"), true, hotel),
            
            // Premium Rooms (for luxury hotels)
            createRoom("601", "PREMIUM", new BigDecimal("299.99"), true, hotel)
        );

        // Set room details based on hotel type
        rooms.forEach(room -> {
            room.setCapacity(getCapacityForRoomType(room.getType()));
            room.setAmenities(getAmenitiesForRoomType(room.getType()));
            room.setDescription(getDescriptionForRoomType(room.getType(), hotel.getName()));
            room.setImageUrl(getImageForRoomType(room.getType()));
        });

        roomRepository.saveAll(rooms);
    }

    // Helper method to create rooms with availability
    private Room createRoom(String roomNumber, String type, BigDecimal price, Boolean available, Hotel hotel) {
        Room room = new Room(roomNumber, type, price, hotel);
        room.setAvailable(available);
        return room;
    }

    private int getCapacityForRoomType(String type) {
        return switch (type) {
            case "STANDARD", "DELUXE", "EXECUTIVE" -> 2;
            case "SUITE", "PREMIUM" -> 3;
            case "FAMILY" -> 4;
            default -> 2;
        };
    }

    private String getAmenitiesForRoomType(String type) {
        return switch (type) {
            case "STANDARD" -> "Air Conditioning,TV,Free WiFi,Work Desk";
            case "DELUXE" -> "Air Conditioning,Smart TV,Free WiFi,Minibar,Coffee Maker";
            case "SUITE" -> "Air Conditioning,Smart TV,Free WiFi,Minibar,Coffee Maker,Separate Living Area";
            case "EXECUTIVE" -> "Air Conditioning,Smart TV,Free WiFi,Work Desk,Executive Lounge Access";
            case "FAMILY" -> "Air Conditioning,TV,Free WiFi,Extra Beds,Family Friendly";
            case "PREMIUM" -> "Air Conditioning,Smart TV,Free WiFi,Minibar,Coffee Maker,Balcony,Premium Toiletries";
            default -> "Air Conditioning,TV,Free WiFi";
        };
    }

    private String getDescriptionForRoomType(String type, String hotelName) {
        return switch (type) {
            case "STANDARD" -> "Comfortable standard room with all essential amenities at " + hotelName;
            case "DELUXE" -> "Spacious deluxe room with enhanced amenities and comfortable bedding at " + hotelName;
            case "SUITE" -> "Luxurious suite with separate living area and premium amenities at " + hotelName;
            case "EXECUTIVE" -> "Executive room designed for business travelers with enhanced workspace at " + hotelName;
            case "FAMILY" -> "Family-friendly room with extra space and comfortable bedding for all at " + hotelName;
            case "PREMIUM" -> "Premium room with exclusive amenities and special touches at " + hotelName;
            default -> "Comfortable accommodation at " + hotelName;
        };
    }

    private String getImageForRoomType(String type) {
        return switch (type) {
            case "STANDARD" -> "https://images.unsplash.com/photo-1631049307264-da0ec9d70304";
            case "DELUXE" -> "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b";
            case "SUITE" -> "https://images.unsplash.com/photo-1611892440504-42a792e24d32";
            case "EXECUTIVE" -> "https://images.unsplash.com/photo-1590490360182-c33d57733427";
            case "FAMILY" -> "https://images.unsplash.com/photo-1586105251261-72a756497a11";
            case "PREMIUM" -> "https://images.unsplash.com/photo-1566665797739-1674de7a421a";
            default -> "https://images.unsplash.com/photo-1631049307264-da0ec9d70304";
        };
    }
}
