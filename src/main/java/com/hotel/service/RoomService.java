package com.hotel.service;

import com.hotel.model.Room;
import com.hotel.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class RoomService {
    
    @Autowired
    private RoomRepository roomRepository;

    public List<Room> getAvailableRoomsByHotelAndDates(Long hotelId, LocalDate checkIn, LocalDate checkOut) {
        if (checkIn != null && checkOut != null) {
            return roomRepository.findAvailableRoomsByHotelAndDates(hotelId, checkIn, checkOut);
        } else {
            return roomRepository.findByHotelIdAndAvailableTrue(hotelId);
        }
    }

    public Room getRoomById(Long id) {
        return roomRepository.findById(id).orElse(null);
    }

    public List<Room> getFilteredRooms(Long hotelId, String checkIn, String checkOut, 
                                      String roomType, Double maxPrice) {
        List<Room> rooms;
        
        // Handle date-based availability
        if (checkIn != null && checkOut != null && !checkIn.isEmpty() && !checkOut.isEmpty()) {
            try {
                LocalDate checkInDate = LocalDate.parse(checkIn);
                LocalDate checkOutDate = LocalDate.parse(checkOut);
                rooms = getAvailableRoomsByHotelAndDates(hotelId, checkInDate, checkOutDate);
            } catch (Exception e) {
                // If date parsing fails, fall back to all available rooms
                rooms = roomRepository.findByHotelIdAndAvailableTrue(hotelId);
            }
        } else {
            // No dates provided, show all available rooms
            rooms = roomRepository.findByHotelIdAndAvailableTrue(hotelId);
        }
        
        // Apply additional filters
        if (roomType != null && !roomType.isEmpty()) {
            rooms = rooms.stream()
                .filter(room -> room.getType().equalsIgnoreCase(roomType))
                .toList();
        }
        
        if (maxPrice != null) {
            rooms = rooms.stream()
                .filter(room -> room.getPrice().doubleValue() <= maxPrice)
                .toList();
        }
        
        return rooms;
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