package com.hotel.repository;

import com.hotel.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByHotelId(Long hotelId);
    List<Room> findByHotelIdAndAvailableTrue(Long hotelId);
    List<Room> findByTypeContainingIgnoreCase(String type);
    List<Room> findByPriceBetween(java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice);
    
    // Date-based availability query
    @Query("SELECT r FROM Room r WHERE r.hotel.id = :hotelId AND r.available = true " +
           "AND r.id NOT IN (" +
           "SELECT b.room.id FROM Booking b WHERE " +
           "b.status = 'CONFIRMED' AND " +
           "((b.checkInDate < :checkOut AND b.checkOutDate > :checkIn)))")
    List<Room> findAvailableRoomsByHotelAndDates(@Param("hotelId") Long hotelId,
                                                @Param("checkIn") LocalDate checkIn,
                                                @Param("checkOut") LocalDate checkOut);
}