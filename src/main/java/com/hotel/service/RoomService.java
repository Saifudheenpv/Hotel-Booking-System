package com.hotel.service;

import com.hotel.model.Room;
import com.hotel.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class RoomService {
    
    @Autowired
    private RoomRepository roomRepository;

    public List<Room> getAvailableRoomsByHotel(Long hotelId) {
        return roomRepository.findByHotelIdAndAvailableTrue(hotelId);
    }

    public List<Room> getAvailableRoomsByDates(Long hotelId, LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            return getAvailableRoomsByHotel(hotelId);
        }
        return roomRepository.findAvailableRoomsByHotelAndDates(hotelId, checkIn, checkOut);
    }

    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id);
    }

    public Room saveRoom(Room room) {
        return roomRepository.save(room);
    }

    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public List<Room> getRoomsByType(String type) {
        return roomRepository.findByTypeContainingIgnoreCase(type);
    }

    public List<Room> getRoomsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return roomRepository.findByPriceBetween(minPrice, maxPrice);
    }
    
    public List<Room> getRoomsByHotelId(Long hotelId) {
        return roomRepository.findByHotelId(hotelId);
    }
}