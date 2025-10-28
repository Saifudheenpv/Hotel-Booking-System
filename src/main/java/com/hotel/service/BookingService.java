package com.hotel.service;

import com.hotel.model.Booking;
import com.hotel.model.Room;
import com.hotel.model.User;
import com.hotel.repository.BookingRepository;
import com.hotel.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private RoomRepository roomRepository;

    public Booking createBooking(User user, Room room, LocalDate checkInDate, 
                                LocalDate checkOutDate, Integer guests, String specialRequests) {
        
        // Validate dates
        if (checkInDate == null || checkOutDate == null) {
            throw new RuntimeException("Check-in and check-out dates are required");
        }
        
        if (checkInDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("Check-in date cannot be in the past");
        }
        
        if (checkOutDate.isBefore(checkInDate) || checkOutDate.isEqual(checkInDate)) {
            throw new RuntimeException("Check-out date must be after check-in date");
        }
        
        // Validate guests
        if (guests == null || guests <= 0) {
            throw new RuntimeException("Number of guests must be at least 1");
        }
        
        if (guests > room.getCapacity()) {
            throw new RuntimeException("Number of guests exceeds room capacity");
        }
        
        // Check if room is available for the selected dates using the date-based query
        List<Room> availableRooms = roomRepository.findAvailableRoomsByHotelAndDates(
            room.getHotel().getId(), checkInDate, checkOutDate);
        
        boolean roomAvailable = availableRooms.stream()
            .anyMatch(availableRoom -> availableRoom.getId().equals(room.getId()));
        
        if (!roomAvailable) {
            throw new RuntimeException("Room is not available for the selected dates");
        }

        // Calculate total price
        long days = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        BigDecimal totalPrice = room.getPrice().multiply(BigDecimal.valueOf(days));
        
        // Create booking - REMOVED the setCreatedAt line as it's handled in constructor
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRoom(room);
        booking.setCheckInDate(checkInDate);
        booking.setCheckOutDate(checkOutDate);
        booking.setGuests(guests);
        booking.setTotalPrice(totalPrice);
        booking.setSpecialRequests(specialRequests);
        booking.setStatus("CONFIRMED");
        // createdAt is automatically set in the constructor
        
        // Save booking
        return bookingRepository.save(booking);
    }

    public List<Booking> getUserBookings(Long userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    public Booking updateBooking(Booking booking) {
        return bookingRepository.save(booking);
    }

    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        // Only allow cancellation for confirmed bookings
        if (!"CONFIRMED".equals(booking.getStatus())) {
            throw new RuntimeException("Only confirmed bookings can be cancelled");
        }
        
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);
    }
    
    public List<Booking> getBookingsByRoom(Room room) {
        return bookingRepository.findByRoomId(room.getId());
    }
    
    public List<Booking> getActiveBookings() {
        return bookingRepository.findByStatus("CONFIRMED");
    }
}