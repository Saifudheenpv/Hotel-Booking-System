package com.hotel.service;

import com.hotel.model.Hotel;
import com.hotel.repository.HotelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HotelService {
    
    @Autowired
    private HotelRepository hotelRepository;

    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    public Optional<Hotel> getHotelById(Long id) {
        return hotelRepository.findById(id);
    }

    public List<Hotel> searchHotelsByLocation(String location) {
        return hotelRepository.findByLocationContainingIgnoreCase(location);
    }

    public List<Hotel> getAvailableHotelsByLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            return hotelRepository.findAll();
        }
        return hotelRepository.findAvailableHotelsByLocation(location);
    }

    public Hotel saveHotel(Hotel hotel) {
        return hotelRepository.save(hotel);
    }

    public void deleteHotel(Long id) {
        hotelRepository.deleteById(id);
    }
}