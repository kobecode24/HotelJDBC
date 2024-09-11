package main.java.com.hotelreservation.pricing;

import main.java.com.hotelreservation.model.enums.RoomType;
import main.java.com.hotelreservation.util.DatabaseConnection;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class DynamicPricing implements PricingStrategy {

    private final Map<RoomType, Double> basePrices;
    private final Map<LocalDate, Double> seasonalMultipliers;
    private final Map<LocalDate, Map<String, Double>> eventPricing;
    private static final double WEEKEND_MULTIPLIER = 1.5;
    private static final double DEFAULT_OCCUPANCY_DISCOUNT = 0.1;

    public DynamicPricing() {
        this.basePrices = new EnumMap<>(RoomType.class);
        this.seasonalMultipliers = new HashMap<>();
        this.eventPricing = new HashMap<>();
        loadPricingData();
    }

    private void loadPricingData() {
        loadBasePrices();
        loadSeasonalMultipliers();
        loadEventPricing();
    }

    private void loadBasePrices() {
        String sql = "SELECT room_type, price FROM base_prices";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                RoomType roomType = RoomType.valueOf(rs.getString("room_type"));
                double price = rs.getDouble("price");
                basePrices.put(roomType, price);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading base prices", e);
        }
    }

    private void loadSeasonalMultipliers() {
        String sql = "SELECT start_date, end_date, multiplier FROM seasonal_pricing";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                LocalDate startDate = rs.getDate("start_date").toLocalDate();
                LocalDate endDate = rs.getDate("end_date").toLocalDate();
                double multiplier = rs.getDouble("multiplier");
                for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                    seasonalMultipliers.put(date, multiplier);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading seasonal multipliers", e);
        }
    }

    private void loadEventPricing() {
        String sql = "SELECT event_date, event_name, multiplier FROM event_pricing";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                LocalDate eventDate = rs.getDate("event_date").toLocalDate();
                String eventName = rs.getString("event_name");
                double multiplier = rs.getDouble("multiplier");
                eventPricing.computeIfAbsent(eventDate, k -> new HashMap<>()).put(eventName, multiplier);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading event pricing", e);
        }
    }

    @Override
    public double calculatePrice(LocalDate startDate, LocalDate endDate, RoomType roomType) {
        long nights = ChronoUnit.DAYS.between(startDate, endDate);
        double totalPrice = 0.0;

        for (int i = 0; i < nights; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            double dailyPrice = basePrices.get(roomType);

            if (isWeekend(currentDate)) {
                dailyPrice *= WEEKEND_MULTIPLIER;
            }

            dailyPrice *= getSeasonalMultiplier(currentDate);
            dailyPrice *= getEventMultiplier(currentDate);

            totalPrice += dailyPrice;
        }

        return totalPrice;
    }

    @Override
    public void updateBasePrice(RoomType roomType, double newPrice) {
        String sql = "UPDATE base_prices SET price = ? WHERE room_type = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newPrice);
            pstmt.setObject(2, roomType.name(), Types.OTHER);
            pstmt.executeUpdate();
            basePrices.put(roomType, newPrice);
        } catch (SQLException e) {
            throw new RuntimeException("Error updating base price", e);
        }
    }

    @Override
    public void setSeasonalMultiplier(LocalDate startDate, LocalDate endDate, double multiplier) {
        String sql = "INSERT INTO seasonal_pricing (start_date, end_date, multiplier) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));
            pstmt.setDouble(3, multiplier);
            pstmt.executeUpdate();
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                seasonalMultipliers.put(date, multiplier);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error setting seasonal multiplier", e);
        }
    }

    @Override
    public void setEventPricing(LocalDate date, String eventName, double multiplier) {
        String sql = "INSERT INTO event_pricing (event_date, event_name, multiplier) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(date));
            pstmt.setString(2, eventName);
            pstmt.setDouble(3, multiplier);
            pstmt.executeUpdate();
            eventPricing.computeIfAbsent(date, k -> new HashMap<>()).put(eventName, multiplier);
        } catch (SQLException e) {
            throw new RuntimeException("Error setting event pricing", e);
        }
    }

    @Override
    public double getOccupancyBasedDiscount(double occupancyRate) {
        if (occupancyRate < 0.5) {
            return DEFAULT_OCCUPANCY_DISCOUNT;
        }
        return 0;
    }

    private boolean isWeekend(LocalDate date) {
        int dayOfWeek = date.getDayOfWeek().getValue();
        return dayOfWeek == 6 || dayOfWeek == 7; // Saturday or Sunday
    }

    private double getSeasonalMultiplier(LocalDate date) {
        return seasonalMultipliers.getOrDefault(date, 1.0);
    }

    private double getEventMultiplier(LocalDate date) {
        Map<String, Double> events = eventPricing.get(date);
        if (events != null && !events.isEmpty()) {
            return events.values().stream().max(Double::compare).orElse(1.0);
        }
        return 1.0;
    }

    public void clearSeasonalPricing() {
        String sql = "DELETE FROM seasonal_pricing";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            seasonalMultipliers.clear();
        } catch (SQLException e) {
            throw new RuntimeException("Error clearing seasonal pricing", e);
        }
    }

    public void clearEventPricing() {
        String sql = "DELETE FROM event_pricing";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            eventPricing.clear();
        } catch (SQLException e) {
            throw new RuntimeException("Error clearing event pricing", e);
        }
    }

    public Map<RoomType, Double> getCurrentBasePrices() {
        return new EnumMap<>(basePrices);
    }

    public List<Map.Entry<LocalDate, Double>> getSeasonalPricingInfo() {
        List<Map.Entry<LocalDate, Double>> info = new ArrayList<>(seasonalMultipliers.entrySet());
        info.sort(Map.Entry.comparingByKey());
        return info;
    }

    public List<Map.Entry<LocalDate, Map<String, Double>>> getEventPricingInfo() {
        List<Map.Entry<LocalDate, Map<String, Double>>> info = new ArrayList<>(eventPricing.entrySet());
        info.sort(Map.Entry.comparingByKey());
        return info;
    }
}
