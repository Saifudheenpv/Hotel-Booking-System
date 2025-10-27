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
        
        // Calculate total price
        long days = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        BigDecimal totalPrice = room.getPrice().multiply(BigDecimal.valueOf(days));
        
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRoom(room);
        booking.setCheckInDate(checkInDate);
        booking.setCheckOutDate(checkOutDate);
        booking.setGuests(guests);
        booking.setTotalPrice(totalPrice);
        booking.setSpecialRequests(specialRequests);
        
        // Mark room as unavailable
        room.setAvailable(false);
        roomRepository.save(room);
        
        return bookingRepository.save(booking);
    }

    public List<Booking> getUserBookings(Long userId) {
        return bookingRepository.findByUserIdOrderByBookingDateDesc(userId);
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
        
        booking.setStatus("CANCELLED");
        
        // Mark room as available again
        Room room = booking.getRoom();
        room.setAvailable(true);
        roomRepository.save(room);
        
        bookingRepository.save(booking);
    }
}