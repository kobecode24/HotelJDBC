package main.java.com.hotelreservation.dao;

import main.java.com.hotelreservation.model.Reservation;
import main.java.com.hotelreservation.model.enums.ReservationStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationDAO{
    List<Reservation> findByDateRange(String startDate, String endDate);

    Optional<Reservation> findById(int Id);
    List<Reservation> findAll();
    void save(Reservation reservation);
    void update(Reservation reservation);
    void delete(int id);
    List<Reservation> findByCustomerId(int customerId);
    List<Reservation> findByDateRange(LocalDate startDate, LocalDate endDate);
    List<Reservation> findByRoomId(int roomId);
    List<Reservation> findByStatus(ReservationStatus status);
}
