package main.java.com.hotelreservation.statistics;

import main.java.com.hotelreservation.model.Reservation;
import main.java.com.hotelreservation.model.Room;
import main.java.com.hotelreservation.model.enums.ReservationStatus;
import main.java.com.hotelreservation.model.enums.RoomType;
import main.java.com.hotelreservation.pricing.DynamicPricing;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class Statistics {

    private List<Reservation> reservations;
    private List<Room> rooms;
    private DynamicPricing pricingStrategy;

    public Statistics(List<Reservation> reservations, List<Room> rooms, DynamicPricing pricingStrategy) {
        this.reservations = reservations;
        this.rooms = rooms;
        this.pricingStrategy = pricingStrategy;
    }

    public double calculateOccupancy() {
        return calculateOccupancy(LocalDate.now(), LocalDate.now());
    }

    public double calculateOccupancy(LocalDate startDate, LocalDate endDate) {
        long totalRoomDays = rooms.size() * ChronoUnit.DAYS.between(startDate, endDate.plusDays(1));
        long occupiedRoomDays = reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                .filter(r -> hasOverlap(r, startDate, endDate))
                .mapToLong(r -> calculateOverlappingDays(r, startDate, endDate))
                .sum();
        return (double) occupiedRoomDays / totalRoomDays;
    }

    public double calculateRevenue(LocalDate startDate, LocalDate endDate) {
        return reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                .filter(r -> hasOverlap(r, startDate, endDate))
                .mapToDouble(r -> calculateReservationRevenue(r, startDate, endDate))
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
                .filter(r -> hasOverlap(r, startDate, endDate))
                .collect(Collectors.groupingBy(this::getRoomType,
                        Collectors.summingDouble(r -> calculateReservationRevenue(r, startDate, endDate))));

        for (RoomType type : RoomType.values()) {
            double typeRevenue = revenueByType.getOrDefault(type, 0.0);
            report.append(String.format("%s Rooms Revenue: $%.2f\n", type, typeRevenue));
        }

        return report.toString();
    }

    public String generateCancellationReport() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(1);
        StringBuilder report = new StringBuilder("Cancellation Report:\n");
        int totalCancellations = calculateCancellations(startDate, endDate);
        report.append(String.format("Total Cancellations (Last 30 days): %d\n", totalCancellations));

        Map<RoomType, Long> cancellationsByType = reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CANCELLED)
                .filter(r -> isWithinDateRange(r, startDate, endDate))
                .collect(Collectors.groupingBy(this::getRoomType, Collectors.counting()));

        for (RoomType type : RoomType.values()) {
            long typeCancellations = cancellationsByType.getOrDefault(type, 0L);
            report.append(String.format("%s Room Cancellations: %d\n", type, typeCancellations));
        }

        return report.toString();
    }

    public String generateRoomTypePerformanceReport(LocalDate startDate, LocalDate endDate) {
        StringBuilder report = new StringBuilder("Room Type Performance Report:\n");
        report.append(String.format("Date Range: %s to %s\n", startDate, endDate));
        report.append(String.format("Total Reservations: %d\n", reservations.size()));

        for (RoomType type : RoomType.values()) {
            double occupancy = calculateOccupancyByRoomType(type, startDate, endDate);
            double revenue = calculateRevenueByRoomType(type, startDate, endDate);
            report.append(String.format("%s Rooms:\n", type));
            report.append(String.format("  Occupancy: %.2f%%\n", occupancy * 100));
            report.append(String.format("  Revenue: $%.2f\n", revenue));

            long roomCount = rooms.stream().filter(r -> r.getRoomType() == type).count();
            List<Reservation> relevantReservations = reservations.stream()
                    .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                    .filter(r -> getRoomType(r) == type)
                    .filter(r -> hasOverlap(r, startDate, endDate))
                    .toList();

            report.append(String.format("   Total %s rooms: %d\n", type, roomCount));
            report.append(String.format("   Reservations in range: %d\n", relevantReservations.size()));
            for (Reservation res : relevantReservations) {
                double resRevenue = calculateReservationRevenue(res, startDate, endDate);
                report.append(String.format("    Reservation ID: %d, Room ID: %d, Dates: %s to %s, Total Price: $%.2f, Calculated Revenue: $%.2f\n",
                        res.getReservationId(), res.getRoomId(), res.getStartDate(), res.getEndDate(), res.getTotalPrice(), resRevenue));
            }
        }
        return report.toString();
    }

    private double calculateOccupancyByRoomType(RoomType type, LocalDate startDate, LocalDate endDate) {
        long totalRoomDays = rooms.stream()
                .filter(r -> r.getRoomType() == type)
                .count() * ChronoUnit.DAYS.between(startDate, endDate.plusDays(1));
        long occupiedRoomDays = reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                .filter(r -> getRoomType(r) == type)
                .filter(r -> hasOverlap(r, startDate, endDate))
                .mapToLong(r -> calculateOverlappingDays(r, startDate, endDate))
                .sum();
        return totalRoomDays > 0 ? (double) occupiedRoomDays / totalRoomDays : 0;
    }

    private double calculateRevenueByRoomType(RoomType type, LocalDate startDate, LocalDate endDate) {
        return reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                .filter(r -> getRoomType(r) == type)
                .filter(r -> hasOverlap(r, startDate, endDate))
                .mapToDouble(r -> calculateReservationRevenue(r, startDate, endDate))
                .sum();
    }

    private boolean isCurrentlyOccupied(Reservation reservation) {
        LocalDate now = LocalDate.now();
        return !now.isBefore(reservation.getStartDate()) && !now.isAfter(reservation.getEndDate());
    }

    private boolean isWithinDateRange(Reservation reservation, LocalDate startDate, LocalDate endDate) {
        return !reservation.getEndDate().isBefore(startDate) && !reservation.getStartDate().isAfter(endDate);
    }

    private boolean hasOverlap(Reservation reservation, LocalDate startDate, LocalDate endDate) {
        return !reservation.getEndDate().isBefore(startDate) && !reservation.getStartDate().isAfter(endDate);
    }

    private long calculateOverlappingDays(Reservation reservation, LocalDate startDate, LocalDate endDate) {
        LocalDate overlapStart = reservation.getStartDate().isAfter(startDate) ? reservation.getStartDate() : startDate;
        LocalDate overlapEnd = reservation.getEndDate().isBefore(endDate) ? reservation.getEndDate() : endDate;
        return ChronoUnit.DAYS.between(overlapStart, overlapEnd.plusDays(1));
    }

    private double calculateReservationRevenue(Reservation reservation, LocalDate startDate, LocalDate endDate) {
        LocalDate overlapStart = reservation.getStartDate().isAfter(startDate) ? reservation.getStartDate() : startDate;
        LocalDate overlapEnd = reservation.getEndDate().isBefore(endDate) ? reservation.getEndDate() : endDate;

        double totalRevenue = 0.0;
        RoomType roomType = getRoomType(reservation);

        for (LocalDate date = overlapStart; !date.isAfter(overlapEnd); date = date.plusDays(1)) {
            double dailyPrice = pricingStrategy.calculatePrice(date, date.plusDays(1), roomType);
            totalRevenue += dailyPrice;
        }

        return totalRevenue;
    }

    private RoomType getRoomType(Reservation reservation) {
        return rooms.stream()
                .filter(r -> r.getRoomId() == reservation.getRoomId())
                .findFirst()
                .map(Room::getRoomType)
                .orElseThrow(() -> new IllegalStateException("Room not found for reservation"));
    }
}
