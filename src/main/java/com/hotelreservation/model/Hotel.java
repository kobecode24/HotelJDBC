package main.java.com.hotelreservation.model;


import main.java.com.hotelreservation.model.enums.RoomType;

import java.util.ArrayList;
import java.util.List;

public class Hotel {
    int hotelId;
    String name;
    String address;
    List<Room> rooms;

    public Hotel(int hotelId, String name, String address) {
        this.hotelId = hotelId;
        this.name = name;
        this.address = address;
        this.rooms = new ArrayList<>();
    }


    public int getHotelId() {
        return hotelId;
    }

    public void setHotelId(int hotelId) {
        this.hotelId = hotelId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    public void addRoom(Room room) {
        this.rooms.add(room);
    }

    /*public List<Room> getAvailableRooms(){
        return rooms.stream().filter(room -> room.isAvailable).toList();
    }
*/
    public List<Room> getAvailableRooms() {
        return rooms.stream().filter(Room::isAvailable).toList();
    }




    @Override
    public String toString() {
        return "Hotel{" +
                "hotelId=" + hotelId +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", rooms=" + rooms +
                '}';
    }
}