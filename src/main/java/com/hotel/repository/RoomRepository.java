package com.hotel.repository;

import com.hotel.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByHotelIdAndAvailableTrue(Long hotelId);
    
    @Query("SELECT r FROM Room r WHERE r.hotel.id = :hotelId AND r.available = true " +
           "AND r.id NOT IN (SELECT b.room.id FROM Booking b WHERE " +
           "b.status != 'CANCELLED' AND " +
           "((b.checkInDate <= :checkOutDate AND b.checkOutDate >= :checkInDate)))")
    List<Room> findAvailableRoomsByHotelAndDates(@Param("hotelId") Long hotelId,
                                                @Param("checkInDate") LocalDate checkInDate,
                                                @Param("checkOutDate") LocalDate checkOutDate);
    
    List<Room> findByTypeContainingIgnoreCase(String type);
    
    @Query("SELECT r FROM Room r WHERE r.price BETWEEN :minPrice AND :maxPrice")
    List<Room> findByPriceBetween(@Param("minPrice") BigDecimal minPrice, 
                                 @Param("maxPrice") BigDecimal maxPrice);
    
    List<Room> findByHotelId(Long hotelId);
    
    List<Room> findByAvailableTrue();
}