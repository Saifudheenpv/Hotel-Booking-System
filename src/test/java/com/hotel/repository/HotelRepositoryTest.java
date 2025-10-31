package com.hotel.repository;

import com.hotel.model.Hotel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class HotelRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private HotelRepository hotelRepository;

    @Test
    void whenFindAll_thenReturnAllHotels() {
        Hotel hotel1 = new Hotel();
        hotel1.setName("Hotel A");
        hotel1.setLocation("Location A");
        hotel1.setStartingPrice(100.0);
        
        Hotel hotel2 = new Hotel();
        hotel2.setName("Hotel B");
        hotel2.setLocation("Location B");
        hotel2.setStartingPrice(150.0);
        
        entityManager.persist(hotel1);
        entityManager.persist(hotel2);
        entityManager.flush();

        List<Hotel> hotels = hotelRepository.findAll();

        assertThat(hotels).hasSize(2);
        assertThat(hotels).extracting(Hotel::getName)
                         .containsExactlyInAnyOrder("Hotel A", "Hotel B");
    }

    @Test
    void whenFindById_thenReturnHotel() {
        Hotel hotel = new Hotel();
        hotel.setName("Test Hotel");
        hotel.setLocation("Test Location");
        hotel.setStartingPrice(200.0);
        
        Hotel persistedHotel = entityManager.persist(hotel);
        entityManager.flush();

        Optional<Hotel> found = hotelRepository.findById(persistedHotel.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Hotel");
    }

    @Test
    void whenSaveHotel_thenHotelIsSaved() {
        Hotel hotel = new Hotel();
        hotel.setName("New Hotel");
        hotel.setLocation("New Location");
        hotel.setStartingPrice(180.0);

        Hotel saved = hotelRepository.save(hotel);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("New Hotel");
    }

    @Test
    void whenDeleteHotel_thenHotelIsRemoved() {
        Hotel hotel = new Hotel();
        hotel.setName("To Delete");
        hotel.setLocation("Location");
        hotel.setStartingPrice(100.0);
        
        Hotel persisted = entityManager.persist(hotel);
        entityManager.flush();

        hotelRepository.deleteById(persisted.getId());
        
        Optional<Hotel> found = hotelRepository.findById(persisted.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void whenFindByLocation_thenReturnHotelsInLocation() {
        Hotel hotel1 = new Hotel();
        hotel1.setName("Hotel NY");
        hotel1.setLocation("New York");
        hotel1.setStartingPrice(200.0);
        
        Hotel hotel2 = new Hotel();
        hotel2.setName("Another NY Hotel");
        hotel2.setLocation("New York");
        hotel2.setStartingPrice(250.0);
        
        Hotel hotel3 = new Hotel();
        hotel3.setName("Miami Hotel");
        hotel3.setLocation("Miami");
        hotel3.setStartingPrice(150.0);
        
        entityManager.persist(hotel1);
        entityManager.persist(hotel2);
        entityManager.persist(hotel3);
        entityManager.flush();

        List<Hotel> nyHotels = hotelRepository.findByLocation("New York");

        assertThat(nyHotels).hasSize(2);
        assertThat(nyHotels).extracting(Hotel::getLocation)
                           .containsOnly("New York");
    }
}
