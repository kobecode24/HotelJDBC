package main.java.com.hotelreservation.service;

import main.java.com.hotelreservation.model.Reservation;
import main.java.com.hotelreservation.model.Room;
import main.java.com.hotelreservation.model.enums.ReservationStatus;
import main.java.com.hotelreservation.pricing.DynamicPricing;
import main.java.com.hotelreservation.repository.ReservationRepository;
import main.java.com.hotelreservation.repository.RoomRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final DynamicPricing pricingStrategy;
    private final RoomRepository roomRepository;


    public ReservationService(ReservationRepository reservationRepository, DynamicPricing pricingStrategy, RoomRepository roomRepository) {
        this.reservationRepository = reservationRepository;
        this.pricingStrategy = pricingStrategy;
        this.roomRepository = roomRepository;
    }

    public Reservation createReservation(Reservation reservation) {
        Optional<Room> room = roomRepository.findById(reservation.getRoomId());
        if (room.isPresent()) {
            double price = pricingStrategy.calculatePrice(
                    reservation.getStartDate(),
                    reservation.getEndDate(),
                    room.get().getRoomType()
            );
            reservation.setTotalPrice(price);
            return reservationRepository.save(reservation);
        } else {
            throw new RuntimeException("Room not found with ID: " + reservation.getRoomId());
        }
    }

    public Optional<Reservation> getReservationById(int id) {
        return reservationRepository.findById(id);
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public void updateReservation(Reservation reservation) {
        reservationRepository.update(reservation);
    }

    public void deleteReservation(int id) {
        reservationRepository.delete(id);
    }

    public List<Reservation> findReservationsByCustomerId(int customerId) {
        return reservationRepository.findByCustomerId(customerId);
    }

    public List<Reservation> findReservationsByDateRange(LocalDate startDate, LocalDate endDate) {
        return reservationRepository.findByDateRange(startDate, endDate);
    }

    public List<Reservation> findReservationsByStatus(ReservationStatus status) {
        return reservationRepository.findByStatus(status);
    }

    public List<Reservation> findReservationsByRoomId(int roomId) {
        return reservationRepository.findByRoomId(roomId);
    }

    public void cancelReservation(int reservationId) {
        Optional<Reservation> optionalReservation = reservationRepository.findById(reservationId);
        if (optionalReservation.isPresent()) {
            Reservation reservation = optionalReservation.get();
            reservation.setStatus(ReservationStatus.CANCELLED);
            reservationRepository.update(reservation);
        } else {
            throw new RuntimeException("Reservation not found with ID: " + reservationId);
        }
    }
}
