package main.java.com.hotelreservation.service;

import main.java.com.hotelreservation.model.Reservation;
import main.java.com.hotelreservation.model.enums.ReservationStatus;
import main.java.com.hotelreservation.repository.ReservationRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ReservationService {
    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public Reservation createReservation(Reservation reservation) {
        reservationRepository.save(reservation);
        return reservation;
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
}
