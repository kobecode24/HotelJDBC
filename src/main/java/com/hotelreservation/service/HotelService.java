package main.java.com.hotelreservation.service;

import main.java.com.hotelreservation.model.Hotel;
import main.java.com.hotelreservation.repository.HotelRepository;

import java.util.List;
import java.util.Optional;

public class HotelService {
    private final HotelRepository hotelRepository;

    public HotelService(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    public Hotel createHotel(Hotel hotel) {
        hotelRepository.save(hotel);
        return hotel;
    }

    public Optional<Hotel> getHotelById(int id) {
        return hotelRepository.findById(id);
    }

    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    public void updateHotel(Hotel hotel) {
        hotelRepository.update(hotel);
    }

    public void deleteHotel(int id) {
        hotelRepository.delete(id);
    }

    public List<Hotel> findHotelsByName(String name) {
        return hotelRepository.findByName(name);
    }

    public Optional<Hotel> findHotelByAddress(String address) {
        return hotelRepository.findByAddress(address);
    }
}
