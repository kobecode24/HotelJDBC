package main.java.com.hotelreservation.statistics;

import main.java.com.hotelreservation.model.Reservation;
import main.java.com.hotelreservation.model.Room;
import main.java.com.hotelreservation.model.enums.ReservationStatus;
import main.java.com.hotelreservation.model.enums.RoomType;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class Statistics {

    private List<Reservation> reservations;
    private List<Room> rooms;

    public Statistics(List<Reservation> reservations, List<Room> rooms) {
        this.reservations = reservations;
        this.rooms = rooms;
    }

    public double calculateOccupancy() {
        long totalRooms = rooms.size();
        long occupiedRooms = reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                .filter(this::isCurrentlyOccupied)
                .count();
        return (double) occupiedRooms / totalRooms;
    }

    public double calculateRevenue(LocalDate startDate, LocalDate endDate) {
        return reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                .filter(r -> isWithinDateRange(r, startDate, endDate))
                .mapToDouble(this::calculateReservationRevenue)
                .sum();
    }

    public int calculateCancellations(LocalDate startDate, LocalDate endDate) {
        return (int) reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CANCELLED)
                .filter(r -> isWithinDateRange(r, startDate, endDate))
                .count();
    }

    public String generateOccupancyReport() {
        StringBuilder report = new StringBuilder("Occupancy Report:\n");
        report.append(String.format("Current Occupancy Rate: %.2f%%\n", calculateOccupancy() * 100));

        Map<RoomType, Long> occupancyByType = rooms.stream()
                .collect(Collectors.groupingBy(Room::getRoomType, Collectors.counting()));

        for (RoomType type : RoomType.values()) {
            long totalRooms = occupancyByType.getOrDefault(type, 0L);
            long occupiedRooms = reservations.stream()
                    .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                    .filter(this::isCurrentlyOccupied)
                    .filter(r -> getRoomType(r) == type)
                    .count();
            double typeOccupancy = totalRooms > 0 ? (double) occupiedRooms / totalRooms : 0;
            report.append(String.format("%s Rooms: %.2f%% (%d/%d)\n", type, typeOccupancy * 100, occupiedRooms, totalRooms));
        }

        return report.toString();
    }

    public String generateRevenueReport(LocalDate startDate, LocalDate endDate) {
        StringBuilder report = new StringBuilder("Revenue Report:\n");
        report.append(String.format("Period: %s to %s\n", startDate, endDate));
        double totalRevenue = calculateRevenue(startDate, endDate);
        report.append(String.format("Total Revenue: $%.2f\n", totalRevenue));

        Map<RoomType, Double> revenueByType = reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                .filter(r -> isWithinDateRange(r, startDate, endDate))
                .collect(Collectors.groupingBy(this::getRoomType,
                        Collectors.summingDouble(this::calculateReservationRevenue)));

        for (RoomType type : RoomType.values()) {
            double typeRevenue = revenueByType.getOrDefault(type, 0.0);
            report.append(String.format("%s Rooms Revenue: $%.2f\n", type, typeRevenue));
        }

        return report.toString();
    }

    public String generateCancellationReport() {
        StringBuilder report = new StringBuilder("Cancellation Report:\n");
        int totalCancellations = calculateCancellations(LocalDate.now().minusMonths(1), LocalDate.now());
        report.append(String.format("Total Cancellations (Last 30 days): %d\n", totalCancellations));

        Map<RoomType, Long> cancellationsByType = reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CANCELLED)
                .filter(r -> isWithinDateRange(r, LocalDate.now().minusMonths(1), LocalDate.now()))
                .collect(Collectors.groupingBy(this::getRoomType, Collectors.counting()));

        for (RoomType type : RoomType.values()) {
            long typeCancellations = cancellationsByType.getOrDefault(type, 0L);
            report.append(String.format("%s Room Cancellations: %d\n", type, typeCancellations));
        }

        return report.toString();
    }

    private boolean isCurrentlyOccupied(Reservation reservation) {
        LocalDate now = LocalDate.now();
        return !now.isBefore(reservation.getStartDate()) && !now.isAfter(reservation.getEndDate());
    }

    private boolean isWithinDateRange(Reservation reservation, LocalDate startDate, LocalDate endDate) {
        return !reservation.getEndDate().isBefore(startDate) && !reservation.getStartDate().isAfter(endDate);
    }

    private double calculateReservationRevenue(Reservation reservation) {
        // This is a simplified calculation. In a real system, you'd use the actual pricing strategy.
        long nights = ChronoUnit.DAYS.between(reservation.getStartDate(), reservation.getEndDate());
        double basePrice = getBasePrice(getRoomType(reservation));
        return nights * basePrice;
    }

    private RoomType getRoomType(Reservation reservation) {
        // This method assumes you have a way to get the room type from a reservation.
        // You might need to adjust this based on your actual data model.
        return rooms.stream()
                .filter(r -> r.getRoomId() == reservation.getRoomId())
                .findFirst()
                .map(Room::getRoomType)
                .orElseThrow(() -> new IllegalStateException("Room not found for reservation"));
    }

    private double getBasePrice(RoomType roomType) {
        // This is a placeholder. In a real system, you'd get this from your pricing strategy.
        switch (roomType) {
            case SINGLE: return 100;
            case DOUBLE: return 150;
            case SUITE: return 250;
            default: throw new IllegalArgumentException("Unknown room type");
        }
    }

    public String generateRoomTypePerformanceReport(LocalDate startDate, LocalDate endDate) {
        StringBuilder report = new StringBuilder("Room Type Performance Report:\n");
        for (RoomType type : RoomType.values()) {
            double occupancy = calculateOccupancyByRoomType(type, startDate, endDate);
            double revenue = calculateRevenueByRoomType(type, startDate, endDate);
            report.append(String.format("%s Rooms:\n", type));
            report.append(String.format("  Occupancy: %.2f%%\n", occupancy * 100));
            report.append(String.format("  Revenue: $%.2f\n", revenue));
        }
        return report.toString();
    }
}