package com.hotel.repository;

import com.hotel.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    
    // Find hotels by location (case-insensitive)
    List<Hotel> findByLocationContainingIgnoreCase(String location);
    
    // Find hotels by minimum rating
    List<Hotel> findByRatingGreaterThanEqual(Double minRating);
    
    // Find hotels by name (case-insensitive)
    List<Hotel> findByNameContainingIgnoreCase(String name);
    
    // Find top 10 hotels by rating
    List<Hotel> findTop10ByOrderByRatingDesc();
    
    // Advanced search combining multiple fields
    @Query("SELECT h FROM Hotel h WHERE " +
           "LOWER(h.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(h.location) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(h.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(h.amenities) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Hotel> comprehensiveSearch(@Param("query") String query);
}