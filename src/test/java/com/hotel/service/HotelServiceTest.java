package com.hotel.service;

import com.hotel.model.Hotel;
import com.hotel.repository.HotelRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class HotelServiceTest {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private HotelService hotelService;

    @Test
    void testGetAllHotels() {
        List<Hotel> hotels = hotelService.getAllHotels();
        assertNotNull(hotels);
        assertTrue(hotels.size() >= 0);
    }

    @Test
    void testFindHotelById() {
        Hotel hotel = new Hotel();
        hotel.setName("Test Hotel");
        hotel.setLocation("Test Location");
        hotel.setRating(4.5);
        hotel.setDescription("Test Description");
        
        Hotel savedHotel = hotelRepository.save(hotel);
        
        Hotel foundHotel = hotelService.getHotelById(savedHotel.getId());
        assertNotNull(foundHotel);
        assertEquals("Test Hotel", foundHotel.getName());
        
        hotelRepository.delete(savedHotel);
    }
}
