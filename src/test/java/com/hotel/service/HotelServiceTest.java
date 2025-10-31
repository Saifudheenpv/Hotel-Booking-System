package com.hotel.service;

import com.hotel.model.Hotel;
import com.hotel.repository.HotelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @InjectMocks
    private HotelService hotelService;

    private Hotel hotel1;
    private Hotel hotel2;

    @BeforeEach
    void setUp() {
        hotel1 = new Hotel();
        hotel1.setId(1L);
        hotel1.setName("Grand Plaza");
        hotel1.setLocation("New York");
        hotel1.setRating(4.5);
        hotel1.setStartingPrice(200.0);
        
        hotel2 = new Hotel();
        hotel2.setId(2L);
        hotel2.setName("Beach Resort");
        hotel2.setLocation("Miami");
        hotel2.setRating(4.2);
        hotel2.setStartingPrice(150.0);
    }

    @Test
    void findAllHotels_ShouldReturnAllHotels() {
        when(hotelRepository.findAll()).thenReturn(Arrays.asList(hotel1, hotel2));

        List<Hotel> hotels = hotelService.findAllHotels();

        assertThat(hotels).hasSize(2);
        assertThat(hotels.get(0).getName()).isEqualTo("Grand Plaza");
        assertThat(hotels.get(1).getName()).isEqualTo("Beach Resort");
        verify(hotelRepository, times(1)).findAll();
    }

    @Test
    void findAllHotels_ShouldReturnEmptyListWhenNoHotels() {
        when(hotelRepository.findAll()).thenReturn(Arrays.asList());

        List<Hotel> hotels = hotelService.findAllHotels();

        assertThat(hotels).isEmpty();
        verify(hotelRepository, times(1)).findAll();
    }

    @Test
    void findHotelById_WithValidId_ShouldReturnHotel() {
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel1));

        Optional<Hotel> foundHotel = hotelService.findHotelById(1L);

        assertThat(foundHotel).isPresent();
        assertThat(foundHotel.get().getName()).isEqualTo("Grand Plaza");
        verify(hotelRepository, times(1)).findById(1L);
    }

    @Test
    void findHotelById_WithInvalidId_ShouldReturnEmpty() {
        when(hotelRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Hotel> foundHotel = hotelService.findHotelById(99L);

        assertThat(foundHotel).isEmpty();
        verify(hotelRepository, times(1)).findById(99L);
    }

    @Test
    void saveHotel_ShouldSaveAndReturnHotel() {
        Hotel newHotel = new Hotel();
        newHotel.setName("New Hotel");
        newHotel.setLocation("Chicago");
        newHotel.setStartingPrice(180.0);
        
        when(hotelRepository.save(any(Hotel.class))).thenReturn(newHotel);

        Hotel savedHotel = hotelService.saveHotel(newHotel);

        assertThat(savedHotel.getName()).isEqualTo("New Hotel");
        assertThat(savedHotel.getLocation()).isEqualTo("Chicago");
        verify(hotelRepository, times(1)).save(newHotel);
    }

    @Test
    void deleteHotel_WithValidId_ShouldDeleteHotel() {
        doNothing().when(hotelRepository).deleteById(1L);

        hotelService.deleteHotel(1L);

        verify(hotelRepository, times(1)).deleteById(1L);
    }
}
