package com.hotel.controller;

import com.hotel.model.Hotel;
import com.hotel.service.HotelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HomeController.class)
public class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HotelService hotelService;

    @Test
    void home_ShouldReturnHomePage() throws Exception {
        when(hotelService.findAllHotels()).thenReturn(Collections.emptyList());
        
        mockMvc.perform(get("/"))
               .andExpect(status().isOk())
               .andExpect(view().name("index"))
               .andExpect(model().attributeExists("hotels"));
    }

    @Test
    void home_ShouldReturnHotelsInModel() throws Exception {
        Hotel hotel1 = new Hotel();
        hotel1.setName("Test Hotel 1");
        hotel1.setLocation("Test Location 1");
        
        Hotel hotel2 = new Hotel();
        hotel2.setName("Test Hotel 2");
        hotel2.setLocation("Test Location 2");
        
        when(hotelService.findAllHotels()).thenReturn(Arrays.asList(hotel1, hotel2));
        
        mockMvc.perform(get("/"))
               .andExpect(status().isOk())
               .andExpect(model().attribute("hotels", hasSize(2)));
    }

    @Test
    void home_ShouldHandleEmptyHotelList() throws Exception {
        when(hotelService.findAllHotels()).thenReturn(Collections.emptyList());
        
        mockMvc.perform(get("/"))
               .andExpect(status().isOk())
               .andExpect(model().attribute("hotels", empty()));
    }
}
