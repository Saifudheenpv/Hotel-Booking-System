package com.hotel.controller;

import com.hotel.model.Hotel;
import com.hotel.model.Room;
import com.hotel.service.HotelService;
import com.hotel.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
public class HotelController {

    @Autowired
    private HotelService hotelService;

    @Autowired
    private RoomService roomService;

    @GetMapping("/hotels/{id}/rooms")
    public String viewHotelRooms(@PathVariable Long id,
                                @RequestParam(required = false) String checkIn,
                                @RequestParam(required = false) String checkOut,
                                @RequestParam(required = false) String roomType,
                                @RequestParam(required = false) Double maxPrice,
                                Model model) {
        
        Hotel hotel = hotelService.getHotelById(id);
        
        if (hotel != null) {
            // Use the new filtering method that handles dates and availability
            List<Room> rooms = roomService.getFilteredRooms(id, checkIn, checkOut, roomType, maxPrice);
            
            // Calculate nights if dates are provided
            int nights = 0;
            LocalDate parsedCheckIn = null;
            LocalDate parsedCheckOut = null;
            
            if (checkIn != null && checkOut != null && !checkIn.isEmpty() && !checkOut.isEmpty()) {
                try {
                    parsedCheckIn = LocalDate.parse(checkIn);
                    parsedCheckOut = LocalDate.parse(checkOut);
                    nights = (int) ChronoUnit.DAYS.between(parsedCheckIn, parsedCheckOut);
                    nights = Math.max(nights, 1); // Ensure at least 1 night
                } catch (Exception e) {
                    // Handle date parsing errors
                    nights = 1;
                }
            }
            
            model.addAttribute("hotel", hotel);
            model.addAttribute("rooms", rooms);
            model.addAttribute("checkIn", checkIn);  // Keep as String for form values
            model.addAttribute("checkOut", checkOut); // Keep as String for form values
            model.addAttribute("parsedCheckIn", parsedCheckIn); // Add parsed dates if needed
            model.addAttribute("parsedCheckOut", parsedCheckOut); // Add parsed dates if needed
            model.addAttribute("roomType", roomType);
            model.addAttribute("maxPrice", maxPrice);
            model.addAttribute("nights", nights);
            
            return "hotel-rooms";
        } else {
            return "redirect:/?error=Hotel+not+found";
        }
    }
}