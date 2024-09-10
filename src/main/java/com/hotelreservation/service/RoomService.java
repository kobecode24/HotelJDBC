package main.java.com.hotelreservation.service;

import main.java.com.hotelreservation.model.Room;
import main.java.com.hotelreservation.model.enums.RoomType;
import main.java.com.hotelreservation.repository.RoomRepository;

import java.util.List;
import java.util.Optional;

public class RoomService {
    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public Room createRoom(Room room) {
        roomRepository.save(room);
        return room;
    }

    public Optional<Room> getRoomById(int id) {
        return roomRepository.findById(id);
    }

    public void updateRoom(Room room) {
        roomRepository.update(room);
    }

    public void deleteRoom(int id) {
        roomRepository.delete(id);
    }

    public List<Room> findRoomsByType(RoomType type) {
        return roomRepository.findByType(type);
    }

    public List<Room> findAvailableRooms() {
        return roomRepository.findAvailableRooms();
    }

    public List<Room> findRoomsByHotelId(int hotelId) {
        return roomRepository.findByHotelId(hotelId);
    }
}
