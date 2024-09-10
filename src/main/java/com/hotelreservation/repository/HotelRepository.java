package main.java.com.hotelreservation.repository;

import main.java.com.hotelreservation.dao.HotelDAO;
import main.java.com.hotelreservation.model.Hotel;
import main.java.com.hotelreservation.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HotelRepository implements HotelDAO {

    private Hotel mapResultSetToHotel(ResultSet rs) throws SQLException {
        return new Hotel(
                rs.getInt("hotel_id"),
                rs.getString("name"),
                rs.getString("address")
        );
    }

    @Override
    public Optional<Hotel> findById(int id) {
        String sql = "SELECT * FROM hotels WHERE hotel_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToHotel(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding hotel by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Hotel> findAll() {
        List<Hotel> hotels = new ArrayList<>();
        String sql = "SELECT * FROM hotels";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                hotels.add(mapResultSetToHotel(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all hotels", e);
        }
        return hotels;
    }

    @Override
    public void save(Hotel hotel) {
        String sql = "INSERT INTO hotels (name, address) VALUES (?, ?) RETURNING hotel_id";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hotel.getName());
            pstmt.setString(2, hotel.getAddress());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                hotel.setHotelId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving hotel", e);
        }
    }

    @Override
    public void update(Hotel hotel) {
        String sql = "UPDATE hotels SET name = ?, address = ? WHERE hotel_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hotel.getName());
            pstmt.setString(2, hotel.getAddress());
            pstmt.setInt(3, hotel.getHotelId());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Updating hotel failed, no rows affected.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating hotel", e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM hotels WHERE hotel_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Deleting hotel failed, no rows affected.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting hotel", e);
        }
    }

    @Override
    public List<Hotel> findByName(String name) {
        List<Hotel> hotels = new ArrayList<>();
        String sql = "SELECT * FROM hotels WHERE name LIKE ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + name + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                hotels.add(mapResultSetToHotel(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding hotels by name", e);
        }
        return hotels;
    }

    @Override
    public Optional<Hotel> findByAddress(String address) {
        String sql = "SELECT * FROM hotels WHERE address = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, address);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToHotel(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding hotel by address", e);
        }
        return Optional.empty();
    }
}
