package main.java.com.hotelreservation.pricing;

import main.java.com.hotelreservation.model.enums.RoomType;
import java.time.LocalDate;

public interface PricingStrategy {
    double calculatePrice(LocalDate startDate, LocalDate endDate, RoomType roomType , double occupancyRate);
    void updateBasePrice(RoomType roomType, double newPrice);
    void setSeasonalMultiplier(LocalDate startDate, LocalDate endDate, double multiplier);
    void setEventPricing(LocalDate date, String eventName, double multiplier);
    double getOccupancyBasedDiscount(double occupancyRate);
}
