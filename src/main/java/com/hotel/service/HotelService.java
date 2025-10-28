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

    public List<Hotel> searchHotelsByName(String name) {
        return hotelRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Hotel> getHotelsByLocation(String location) {
        return hotelRepository.findByLocationContainingIgnoreCase(location);
    }

    public List<Hotel> getHotelsByRating(Double minRating) {
        return hotelRepository.findByRatingGreaterThanEqual(minRating);
    }

    public List<Hotel> getTopRatedHotels() {
        return hotelRepository.findTop10ByOrderByRatingDesc();
    }

    // Add this missing method - returns Optional<Hotel>
    public Optional<Hotel> findHotelById(Long id) {
        return hotelRepository.findById(id);
    }

    // Alternative method that returns Hotel directly (if you prefer)
    public Hotel getHotelById(Long id) {
        return hotelRepository.findById(id).orElse(null);
    }

    public long getTotalHotels() {
        return hotelRepository.count();
    }
}