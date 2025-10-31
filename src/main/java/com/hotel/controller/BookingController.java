package com.hotel.controller;

import com.hotel.model.Booking;
import com.hotel.model.Room;
import com.hotel.model.User;
import com.hotel.service.BookingService;
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
    private RoomService roomService;

    @GetMapping("/room/{id}/details")
    public String showRoomDetails(@PathVariable Long id, 
                                 @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkIn,
                                 @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkOut,
                                 Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Room room = roomService.getRoomById(id);
        if (room == null) {
            return "redirect:/";
        }

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
            redirectAttributes.addFlashAttribute("error", "Invalid dates selected! Please ensure check-out is after check-in.");
            return "redirect:/my-bookings";
        }

        Room room = roomService.getRoomById(roomId);
        if (room != null) {
            try {
                Booking booking = bookingService.createBooking(user, room, checkIn, checkOut, guests, specialRequests);
                redirectAttributes.addFlashAttribute("success", 
                    "Booking confirmed! Your Booking ID: " + booking.getId() + 
                    ". Total Amount: $" + booking.getTotalPrice() +
                    ". Check your email for confirmation details.");
                return "redirect:/booking-confirmation/" + booking.getId();
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Booking failed: " + e.getMessage());
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Room not found!");
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
    public String cancelBooking(@RequestParam Long bookingId, RedirectAttributes redirectAttributes, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            // Verify the booking belongs to the user
            Optional<Booking> bookingOpt = bookingService.getBookingById(bookingId);
            if (bookingOpt.isPresent() && bookingOpt.get().getUser().getId().equals(user.getId())) {
                bookingService.cancelBooking(bookingId);
                redirectAttributes.addFlashAttribute("success", "Booking cancelled successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Booking not found or access denied!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error cancelling booking: " + e.getMessage());
        }
        return "redirect:/my-bookings";
    }

    @GetMapping("/booking-confirmation/{id}")
    public String showBookingConfirmation(@PathVariable Long id, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Booking> bookingOpt = bookingService.getBookingById(id);
        if (bookingOpt.isPresent() && bookingOpt.get().getUser().getId().equals(user.getId())) {
            Booking booking = bookingOpt.get();
            
            // Calculate number of nights
            long numberOfNights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
            
            model.addAttribute("booking", booking);
            model.addAttribute("user", user);
            model.addAttribute("numberOfNights", numberOfNights);
            
            return "booking-confirmation";
        }

        redirectAttributes.addFlashAttribute("error", "Booking not found!");
        return "redirect:/my-bookings";
    }
}