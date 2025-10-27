package com.hotel.controller;

import com.hotel.model.Hotel;
import com.hotel.service.HotelService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Controller
public class HomeController {
    
    @Autowired
    private HotelService hotelService;

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        List<Hotel> hotels = hotelService.getAllHotels();
        model.addAttribute("hotels", hotels);
        model.addAttribute("user", session.getAttribute("user"));
        return "index";
    }

    @GetMapping("/search")
    public String searchHotels(@RequestParam String location, Model model, HttpSession session) {
        List<Hotel> hotels = hotelService.searchHotelsByLocation(location);
        model.addAttribute("hotels", hotels);
        model.addAttribute("searchLocation", location);
        model.addAttribute("user", session.getAttribute("user"));
        return "index";
    }
}