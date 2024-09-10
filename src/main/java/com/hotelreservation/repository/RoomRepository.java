package main.java.com.hotelreservation.repository;

import main.java.com.hotelreservation.dao.RoomDAO;
import main.java.com.hotelreservation.model.Room;
import main.java.com.hotelreservation.model.enums.RoomType;
import main.java.com.hotelreservation.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomRepository implements RoomDAO {

    @Override
    public Optional<Room> findById(int id) {
        String sql = "SELECT * FROM rooms WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToRoom(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding room by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public void save(Room room) {
        String sql = "INSERT INTO rooms (room_type, is_available, hotel_id) VALUES (CAST(? AS room_type), ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, room.getRoomType().name());
            pstmt.setBoolean(2, room.isAvailable());
            pstmt.setInt(3, room.getHotelId());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating room failed, no rows affected.");
            }
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    room.setRoomId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating room failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving room", e);
        }
    }

    @Override
    public void update(Room room) {
        String sql = "UPDATE rooms SET room_type = ?, is_available = ? WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, room.getRoomType().name(), Types.OTHER);
            pstmt.setBoolean(2, room.isAvailable());
            pstmt.setInt(3, room.getRoomId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating room", e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM rooms WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting room", e);
        }
    }

    @Override
    public List<Room> findByType(RoomType type) {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms WHERE room_type = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, type.name(), Types.OTHER);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                rooms.add(mapResultSetToRoom(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding rooms by type", e);
        }
        return rooms;
    }

    @Override
    public List<Room> findAvailableRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms WHERE is_available = true";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rooms.add(mapResultSetToRoom(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding available rooms", e);
        }
        return rooms;
    }

    @Override
    public List<Room> findByHotelId(int hotelId) {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms WHERE hotel_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, hotelId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                rooms.add(mapResultSetToRoom(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding rooms by hotel ID", e);
        }
        return rooms;
    }

    private Room mapResultSetToRoom(ResultSet rs) throws SQLException {
        return new Room(
                rs.getInt("room_id"),
                RoomType.valueOf(rs.getString("room_type")),
                rs.getBoolean("is_available"),
                rs.getInt("hotel_id")
        );
    }
}
