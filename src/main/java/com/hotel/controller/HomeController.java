package com.hotel.controller;

import com.hotel.model.Hotel;
import com.hotel.repository.HotelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private HotelRepository hotelRepository;

    @GetMapping("/")
    public String home(Model model,
                      @RequestParam(required = false) String location,
                      @RequestParam(required = false) Double minRating,
                      @RequestParam(required = false) String search) {
        
        List<Hotel> hotels;
        
        if (location != null && !location.trim().isEmpty()) {
            hotels = hotelRepository.findByLocationContainingIgnoreCase(location);
        } else if (minRating != null) {
            hotels = hotelRepository.findByRatingGreaterThanEqual(minRating);
        } else if (search != null && !search.trim().isEmpty()) {
            hotels = hotelRepository.findByNameContainingIgnoreCase(search);
        } else {
            hotels = hotelRepository.findAll();
        }
        
        model.addAttribute("hotels", hotels);
        model.addAttribute("totalHotels", hotelRepository.count());
        model.addAttribute("searchQuery", search);
        model.addAttribute("location", location);
        model.addAttribute("minRating", minRating);
        
        return "index";
    }

    @GetMapping("/search")
    public String searchHotels(@RequestParam String query, Model model) {
        List<Hotel> hotels = hotelRepository.findByNameContainingIgnoreCase(query);
        model.addAttribute("hotels", hotels);
        model.addAttribute("searchQuery", query);
        model.addAttribute("totalHotels", hotels.size());
        return "index";
    }

    @GetMapping("/hotels")
    public String browseHotels(Model model,
                              @RequestParam(required = false) String location,
                              @RequestParam(required = false) Double minRating,
                              @RequestParam(required = false) String search) {
        
        List<Hotel> hotels;
        
        if (location != null && !location.trim().isEmpty()) {
            hotels = hotelRepository.findByLocationContainingIgnoreCase(location);
        } else if (minRating != null) {
            hotels = hotelRepository.findByRatingGreaterThanEqual(minRating);
        } else if (search != null && !search.trim().isEmpty()) {
            hotels = hotelRepository.findByNameContainingIgnoreCase(search);
        } else {
            hotels = hotelRepository.findAll();
        }
        
        model.addAttribute("hotels", hotels);
        model.addAttribute("totalHotels", hotelRepository.count());
        model.addAttribute("searchQuery", search);
        model.addAttribute("location", location);
        model.addAttribute("minRating", minRating);
        
        return "hotels";
    }
}