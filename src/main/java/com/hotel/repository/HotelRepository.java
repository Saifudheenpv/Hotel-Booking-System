package com.hotel.repository;

import com.hotel.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    List<Hotel> findByLocationContainingIgnoreCase(String location);
    
    @Query("SELECT h FROM Hotel h WHERE h.location LIKE %:location% AND h.id IN " +
           "(SELECT r.hotel.id FROM Room r WHERE r.available = true)")
    List<Hotel> findAvailableHotelsByLocation(@Param("location") String location);
}