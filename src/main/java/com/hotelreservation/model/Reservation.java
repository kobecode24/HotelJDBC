package main.java.com.hotelreservation.model;

import main.java.com.hotelreservation.model.enums.ReservationStatus;

import java.time.LocalDate;
import java.util.Date;

public class Reservation {
    private int reservationId;
    private Customer customer;;
    private int roomId;
    private LocalDate startDate;
    private LocalDate endDate;
    private ReservationStatus status;
    private double totalPrice;



    public Reservation(int reservationId, LocalDate startDate, LocalDate endDate, ReservationStatus status, int roomId, Customer customer) {
        this.reservationId = reservationId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.roomId = roomId;
        this.customer = customer;
    }

    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public int getCustomerId() {
        return customer.getCustomerId();
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getReservationDetails() {
        return toString();
    }

    public void modifyReservation(LocalDate newStartDate, LocalDate newEndDate, int newRoomId) {
        this.startDate = newStartDate;
        this.endDate = newEndDate;
        this.roomId = newRoomId;
    }

    public void cancelReservation() {
        this.status = ReservationStatus.CANCELLED;
    }



    @Override
    public String toString() {
        return "Reservation{" +
                "reservationId=" + reservationId +
                ", customer=" + customer +
                ", roomID=" + roomId +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status=" + status +
                '}';
    }
}
