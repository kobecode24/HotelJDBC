package main.java.com.hotelreservation.dao;

import main.java.com.hotelreservation.model.Room;
import main.java.com.hotelreservation.model.enums.RoomType;

import java.util.List;
import java.util.Optional;

public interface RoomDAO {
    Optional<Room> findById(int id);

    List<Room> findAll();

    void save(Room room);
    void update(Room room);
    void delete(int id);
    List<Room> findByType(RoomType type);
    List<Room> findAvailableRooms();
    List<Room> findByHotelId(int hotelId);
}
