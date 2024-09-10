package main.java.com.hotelreservation.dao;

import main.java.com.hotelreservation.model.Hotel;
import java.util.List;
import java.util.Optional;

public interface HotelDAO {
    Optional<Hotel> findById(int id);
    List<Hotel> findAll();
    void save(Hotel hotel);
    void update(Hotel hotel);
    void delete(int id);
    List<Hotel> findByName(String name);
    Optional<Hotel> findByAddress(String address);
}
