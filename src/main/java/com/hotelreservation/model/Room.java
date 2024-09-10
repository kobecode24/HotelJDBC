package main.java.com.hotelreservation.model;


import main.java.com.hotelreservation.model.enums.RoomType;

public class Room {
    int roomId;
    RoomType roomType;
    boolean isAvailable;
    private int hotelId;

    public Room(int roomId, RoomType roomType, Boolean isAvailable, int hotelId) {
        this.roomId = roomId;
        this.roomType = roomType;
        this.isAvailable = isAvailable;
        this.hotelId = hotelId;
    }


    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public int getHotelId() {
        return hotelId;
    }

    public void setHotelId(int hotelId) {
        this.hotelId = hotelId;
    }

    public Boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(Boolean available) {
        isAvailable = available;
    }


    @Override
    public String toString() {
        return "Room{" +
                "roomId=" + roomId +
                ", roomType=" + roomType +
                ", isAvailable=" + isAvailable +
                '}';
    }
}
