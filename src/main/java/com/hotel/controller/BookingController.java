package com.hotel.controller;

import com.hotel.model.Booking;
import com.hotel.model.Room;
import com.hotel.model.User;
import com.hotel.service.BookingService;
import com.hotel.service.HotelService;
import com.hotel.service.RoomService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Controller
public class BookingController {
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private HotelService hotelService;
    
    @Autowired
    private RoomService roomService;

    @GetMapping("/hotel/{id}/rooms")
    public String showHotelRooms(@PathVariable Long id, 
                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkIn,
                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkOut,
                                @RequestParam(required = false) String roomType,
                                @RequestParam(required = false) BigDecimal maxPrice,
                                Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        var hotelOpt = hotelService.getHotelById(id);
        if (hotelOpt.isEmpty()) {
            return "redirect:/";
        }

        model.addAttribute("user", user);
        model.addAttribute("hotel", hotelOpt.get());
        
        List<Room> availableRooms;
        if (checkIn != null && checkOut != null) {
            availableRooms = roomService.getAvailableRoomsByDates(id, checkIn, checkOut);
            model.addAttribute("checkIn", checkIn);
            model.addAttribute("checkOut", checkOut);
            
            // Calculate number of nights
            long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
            model.addAttribute("nights", nights);
        } else {
            availableRooms = roomService.getAvailableRoomsByHotel(id);
        }
        
        // Apply filters
        if (roomType != null && !roomType.isEmpty()) {
            availableRooms = availableRooms.stream()
                .filter(room -> room.getType().toLowerCase().contains(roomType.toLowerCase()))
                .toList();
        }
        
        if (maxPrice != null) {
            availableRooms = availableRooms.stream()
                .filter(room -> room.getPrice().compareTo(maxPrice) <= 0)
                .toList();
        }
        
        model.addAttribute("rooms", availableRooms);
        model.addAttribute("roomType", roomType);
        model.addAttribute("maxPrice", maxPrice);
        
        return "hotel-rooms";
    }

    @GetMapping("/room/{id}/details")
    public String showRoomDetails(@PathVariable Long id, 
                                 @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkIn,
                                 @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkOut,
                                 Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Room> roomOpt = roomService.getRoomById(id);
        if (roomOpt.isEmpty()) {
            return "redirect:/";
        }

        Room room = roomOpt.get();
        model.addAttribute("user", user);
        model.addAttribute("room", room);
        model.addAttribute("hotel", room.getHotel());
        model.addAttribute("checkIn", checkIn);
        model.addAttribute("checkOut", checkOut);

        if (checkIn != null && checkOut != null) {
            long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
            BigDecimal totalPrice = room.getPrice().multiply(BigDecimal.valueOf(nights));
            model.addAttribute("nights", nights);
            model.addAttribute("totalPrice", totalPrice);
        }

        return "room-details";
    }

    @PostMapping("/book-room")
    public String bookRoom(@RequestParam Long roomId,
                          @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkIn,
                          @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkOut,
                          @RequestParam Integer guests,
                          @RequestParam(required = false) String specialRequests,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Validate dates
        if (checkIn.isBefore(LocalDate.now()) || checkOut.isBefore(LocalDate.now()) || 
            checkOut.isBefore(checkIn) || checkIn.equals(checkOut)) {
            redirectAttributes.addFlashAttribute("error", "Invalid dates selected!");
            return "redirect:/my-bookings";
        }

        Optional<Room> room = roomService.getRoomById(roomId);
        if (room.isPresent() && room.get().getAvailable()) {
            try {
                Booking booking = bookingService.createBooking(user, room.get(), checkIn, checkOut, guests, specialRequests);
                redirectAttributes.addFlashAttribute("success", 
                    "Booking confirmed! Your Booking ID: " + booking.getId() + 
                    ". Total Amount: $" + booking.getTotalPrice());
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Booking failed: " + e.getMessage());
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Room not available for the selected dates!");
        }

        return "redirect:/my-bookings";
    }

    @GetMapping("/my-bookings")
    public String showMyBookings(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<Booking> bookings = bookingService.getUserBookings(user.getId());
        model.addAttribute("bookings", bookings);
        model.addAttribute("user", user);
        return "my-bookings";
    }

    @PostMapping("/cancel-booking")
    public String cancelBooking(@RequestParam Long bookingId, RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBooking(bookingId);
            redirectAttributes.addFlashAttribute("success", "Booking cancelled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error cancelling booking: " + e.getMessage());
        }
        return "redirect:/my-bookings";
    }

    @GetMapping("/booking-confirmation/{id}")
    public String showBookingConfirmation(@PathVariable Long id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Booking> booking = bookingService.getBookingById(id);
        if (booking.isPresent() && booking.get().getUser().getId().equals(user.getId())) {
            model.addAttribute("booking", booking.get());
            model.addAttribute("user", user);
            return "booking-confirmation";
        }

        return "redirect:/my-bookings";
    }
}