package main.java.com.hotelreservation;

import main.java.com.hotelreservation.model.*;
import main.java.com.hotelreservation.model.enums.*;
import main.java.com.hotelreservation.repository.*;
import main.java.com.hotelreservation.service.*;
import main.java.com.hotelreservation.pricing.DynamicPricing;
import main.java.com.hotelreservation.statistics.Statistics;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Pattern;

public class HotelManagementSystem {
    private final CustomerService customerService;
    private final ReservationService reservationService;
    private final RoomService roomService;
    private final HotelService hotelService;
    private final Statistics statistics;
    private final Scanner scanner;
    private final DynamicPricing pricingStrategy;

    public HotelManagementSystem() {
        CustomerRepository customerRepository = new CustomerRepository();
        ReservationRepository reservationRepository = new ReservationRepository();
        RoomRepository roomRepository = new RoomRepository();
        HotelRepository hotelRepository = new HotelRepository();
        this.pricingStrategy = new DynamicPricing();

        this.customerService = new CustomerService(customerRepository);
        this.reservationService = new ReservationService(reservationRepository, pricingStrategy , roomRepository);
        this.roomService = new RoomService(roomRepository);
        this.hotelService = new HotelService(hotelRepository);
        this.statistics = new Statistics(reservationRepository.findAll(), roomRepository.findAll(), pricingStrategy);
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        boolean running = true;
        while (running) {
            displayMainMenu();
            int choice = getValidIntInput("Enter your choice: ", 7);

            switch (choice) {
                case 1:
                    handleCustomerOperations();
                    break;
                case 2:
                    handleReservationOperations();
                    break;
                case 3:
                    handleRoomOperations();
                    break;
                case 4:
                    handleHotelOperations();
                    break;
                case 5:
                    handleStatistics();
                    break;
                case 6:
                    handlePricingManagement();
                    break;
                case 7:
                    running = false;
                    System.out.println("Exiting the system. Goodbye!");
                    break;
            }
        }
    }

    private void displayMainMenu() {
        System.out.println("\n--- Hotel Management System ---");
        System.out.println("1. Customer Operations");
        System.out.println("2. Reservation Operations");
        System.out.println("3. Room Operations");
        System.out.println("4. Hotel Operations");
        System.out.println("5. Statistics");
        System.out.println("6. Pricing Management");
        System.out.println("7. Exit");
    }

    private void handleCustomerOperations() {
        System.out.println("\n--- Customer Operations ---");
        System.out.println("1. Add New Customer");
        System.out.println("2. Find Customer by ID");
        System.out.println("3. Update Customer");
        System.out.println("4. Delete Customer");
        int choice = getValidIntInput("Enter your choice: ", 4);

        switch (choice) {
            case 1:
                addNewCustomer();
                break;
            case 2:
                findCustomerById();
                break;
            case 3:
                updateCustomer();
                break;
            case 4:
                deleteCustomer();
                break;
        }
    }

    private void addNewCustomer() {
        String name = getValidStringInput("Enter customer name: ", "Name cannot be empty.");
        String email = getValidEmail();
        String phone = getValidPhoneNumber();

        try {
            Customer customer = new Customer(0, name, email, phone);
            Customer createdCustomer = customerService.createCustomer(customer);
            System.out.println("Customer added successfully with ID: " + createdCustomer.getCustomerId());
        } catch (RuntimeException e) {
            System.out.println("Error adding customer: " + e.getMessage());
        }
    }

    private void findCustomerById() {
        int id = getValidIntInput("Enter customer ID: ", Integer.MAX_VALUE);
        Optional<Customer> customer = customerService.getCustomerById(id);
        customer.ifPresentOrElse(
                c -> System.out.println("Customer found: " + c),
                () -> System.out.println("Customer not found.")
        );
    }

    private void updateCustomer() {
        int id = getValidIntInput("Enter customer ID to update: ", Integer.MAX_VALUE);

        Optional<Customer> optionalCustomer = customerService.getCustomerById(id);
        if (optionalCustomer.isPresent()) {
            Customer customer = optionalCustomer.get();

            String name = getOptionalStringInput("Enter new name (or press enter to keep current): ");
            if (!name.isEmpty()) customer.setName(name);

            String email = getOptionalEmail();
            if (!email.isEmpty()) customer.setEmail(email);

            String phone = getOptionalPhoneNumber();
            if (!phone.isEmpty()) customer.setPhoneNumber(phone);

            customerService.updateCustomer(customer);
            System.out.println("Customer updated successfully.");
        } else {
            System.out.println("Customer not found.");
        }
    }

    private void deleteCustomer() {
        int id = getValidIntInput("Enter customer ID to delete: ", Integer.MAX_VALUE);
        customerService.deleteCustomer(id);
        System.out.println("Customer deleted successfully.");
    }

    private void handleReservationOperations() {
        System.out.println("\n--- Reservation Operations ---");
        System.out.println("1. Make New Reservation");
        System.out.println("2. Find Reservation by ID");
        System.out.println("3. Update Reservation");
        System.out.println("4. Cancel Reservation");
        int choice = getValidIntInput("Enter your choice: ", 4);

        switch (choice) {
            case 1:
                makeNewReservation();
                break;
            case 2:
                findReservationById();
                break;
            case 3:
                updateReservation();
                break;
            case 4:
                cancelReservation();
                break;
        }
    }

    private void makeNewReservation() {
        int customerId = getValidIntInput("Enter customer ID: ", Integer.MAX_VALUE);
        int roomId = getValidIntInput("Enter room ID: ", Integer.MAX_VALUE);

        LocalDate startDate = getValidFutureDate("Enter start date (YYYY-MM-DD): ");
        LocalDate endDate = getValidEndDate(startDate);

        Optional<Customer> customer = customerService.getCustomerById(customerId);
        Optional<Room> room = roomService.getRoomById(roomId);

        if (customer.isPresent() && room.isPresent()) {
            try {
                Reservation reservation = new Reservation(0, startDate, endDate, ReservationStatus.CONFIRMED, roomId, customer.get());
                Reservation createdReservation = reservationService.createReservation(reservation);
                System.out.println("Reservation made successfully with ID: " + createdReservation.getReservationId());
                System.out.println("Total Price: $" + createdReservation.getTotalPrice());
            } catch (Exception e) {
                System.out.println("Error creating reservation: " + e.getMessage());
            }
        } else {
            System.out.println("Customer or Room not found. Please check the IDs.");
        }
    }

    private void findReservationById() {
        int id = getValidIntInput("Enter reservation ID: ", Integer.MAX_VALUE);
        Optional<Reservation> reservation = reservationService.getReservationById(id);
        reservation.ifPresentOrElse(
                r -> System.out.println("Reservation found: " + r),
                () -> System.out.println("Reservation not found.")
        );
    }

    private void updateReservation() {
        int id = getValidIntInput("Enter reservation ID to update: ", Integer.MAX_VALUE);

        Optional<Reservation> optionalReservation = reservationService.getReservationById(id);
        if (optionalReservation.isPresent()) {
            Reservation reservation = optionalReservation.get();

            LocalDate startDate = getOptionalFutureDate("Enter new start date (YYYY-MM-DD) or press enter to keep current: ");
            if (startDate != null) reservation.setStartDate(startDate);

            LocalDate endDate = getOptionalEndDate(reservation.getStartDate(), "Enter new end date (YYYY-MM-DD) or press enter to keep current: ");
            if (endDate != null) reservation.setEndDate(endDate);

            ReservationStatus status = getOptionalReservationStatus("Enter new status (CONFIRMED/CANCELLED) or press enter to keep current: ");
            if (status != null) reservation.setStatus(status);

            reservationService.updateReservation(reservation);
            System.out.println("Reservation updated successfully.");
        } else {
            System.out.println("Reservation not found.");
        }
    }

    private void cancelReservation() {
        int id = getValidIntInput("Enter reservation ID to cancel: ", Integer.MAX_VALUE);
        try {
            reservationService.cancelReservation(id);
            System.out.println("Reservation cancelled successfully.");
        } catch (RuntimeException e) {
            System.out.println("Error cancelling reservation: " + e.getMessage());
        }
    }

    private void handleRoomOperations() {
        System.out.println("\n--- Room Operations ---");
        System.out.println("1. Add New Room");
        System.out.println("2. Find Room by ID");
        System.out.println("3. Update Room");
        System.out.println("4. Delete Room");
        int choice = getValidIntInput("Enter your choice: ", 4);

        switch (choice) {
            case 1:
                addNewRoom();
                break;
            case 2:
                findRoomById();
                break;
            case 3:
                updateRoom();
                break;
            case 4:
                deleteRoom();
                break;
        }
    }

    private void addNewRoom() {
        int hotelId = getValidIntInput("Enter hotel ID: ", Integer.MAX_VALUE);
        RoomType roomType = getValidRoomType();
        boolean isAvailable = getValidBoolean("Is room available? (true/false): ");

        try {
            Room room = new Room(0, roomType, isAvailable, hotelId);
            Room createdRoom = roomService.createRoom(room);
            System.out.println("Room added successfully with ID: " + createdRoom.getRoomId());
        } catch (Exception e) {
            System.out.println("Error adding room: " + e.getMessage());
        }
    }

    private void findRoomById() {
        int id = getValidIntInput("Enter room ID: ", Integer.MAX_VALUE);
        Optional<Room> room = roomService.getRoomById(id);
        room.ifPresentOrElse(
                r -> System.out.println("Room found: " + r),
                () -> System.out.println("Room not found.")
        );
    }

    private void updateRoom() {
        int id = getValidIntInput("Enter room ID to update: ", Integer.MAX_VALUE);

        Optional<Room> optionalRoom = roomService.getRoomById(id);
        if (optionalRoom.isPresent()) {
            Room room = optionalRoom.get();

            RoomType roomType = getOptionalRoomType("Enter new room type (SINGLE/DOUBLE/SUITE) or press enter to keep current: ");
            if (roomType != null) room.setRoomType(roomType);

            Boolean isAvailable = getOptionalBoolean("Is room available? (true/false) or press enter to keep current: ");
            if (isAvailable != null) room.setAvailable(isAvailable);

            roomService.updateRoom(room);
            System.out.println("Room updated successfully.");
        } else {
            System.out.println("Room not found.");
        }
    }

    private void deleteRoom() {
        int id = getValidIntInput("Enter room ID to delete: ", Integer.MAX_VALUE);
        roomService.deleteRoom(id);
        System.out.println("Room deleted successfully.");
    }

    private void handleHotelOperations() {
        System.out.println("\n--- Hotel Operations ---");
        System.out.println("1. Add New Hotel");
        System.out.println("2. Find Hotel by ID");
        System.out.println("3. Update Hotel");
        System.out.println("4. Delete Hotel");
        int choice = getValidIntInput("Enter your choice: ", 4);

        switch (choice) {
            case 1:
                addNewHotel();
                break;
            case 2:
                findHotelById();
                break;
            case 3:
                updateHotel();
                break;
            case 4:
                deleteHotel();
                break;
        }
    }

    private void addNewHotel() {
        String name = getValidStringInput("Enter hotel name: ", "Hotel name cannot be empty.");
        String address = getValidStringInput("Enter hotel address: ", "Hotel address cannot be empty.");

        Hotel hotel = new Hotel(0, name, address);
        Hotel createdHotel = hotelService.createHotel(hotel);
        System.out.println("Hotel added successfully with ID: " + createdHotel.getHotelId());
    }

    private void findHotelById() {
        int id = getValidIntInput("Enter hotel ID: ", Integer.MAX_VALUE);
        Optional<Hotel> hotel = hotelService.getHotelById(id);
        hotel.ifPresentOrElse(
                h -> System.out.println("Hotel found: " + h),
                () -> System.out.println("Hotel not found.")
        );
    }

    private void updateHotel() {
        int id = getValidIntInput("Enter hotel ID to update: ", Integer.MAX_VALUE);

        Optional<Hotel> optionalHotel = hotelService.getHotelById(id);
        if (optionalHotel.isPresent()) {
            Hotel hotel = optionalHotel.get();

            String name = getOptionalStringInput("Enter new name (or press enter to keep current): ");
            if (!name.isEmpty()) hotel.setName(name);

            String address = getOptionalStringInput("Enter new address (or press enter to keep current): ");
            if (!address.isEmpty()) hotel.setAddress(address);

            hotelService.updateHotel(hotel);
            System.out.println("Hotel updated successfully.");
        } else {
            System.out.println("Hotel not found.");
        }
    }

    private void deleteHotel() {
        int id = getValidIntInput("Enter hotel ID to delete: ", Integer.MAX_VALUE);
        hotelService.deleteHotel(id);
        System.out.println("Hotel deleted successfully.");
    }

    private void handleStatistics() {
        System.out.println("\n--- Statistics ---");
        System.out.println("1. Occupancy Report");
        System.out.println("2. Revenue Report");
        System.out.println("3. Cancellation Report");
        System.out.println("4. Custom Date Range Report");
        System.out.println("5. Room Type Performance Report");
        System.out.println("6. Back to Main Menu");
        int choice = getValidIntInput("Enter your choice: ", 6);

        switch (choice) {
            case 1:
                System.out.println(statistics.generateOccupancyReport());
                break;
            case 2:
                generateRevenueReport();
                break;
            case 3:
                System.out.println(statistics.generateCancellationReport());
                break;
            case 4:
                generateCustomDateRangeReport();
                break;
            case 5:
                generateRoomTypePerformanceReport();
                break;
            case 6:
                return;
        }
    }

    private void generateRevenueReport() {
        System.out.println("Select time period:");
        System.out.println("1. Last Month");
        System.out.println("2. Last Quarter");
        System.out.println("3. Last Year");
        System.out.println("4. Custom Date Range");
        int choice = getValidIntInput("Enter your choice: ", 4);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate;

        switch (choice) {
            case 1:
                startDate = endDate.minusMonths(1);
                break;
            case 2:
                startDate = endDate.minusMonths(3);
                break;
            case 3:
                startDate = endDate.minusYears(1);
                break;
            case 4:
                startDate = getValidPastDate("Enter start date (YYYY-MM-DD): ");
                endDate = getValidEndDate(startDate);
                break;
            default:
                System.out.println("Invalid choice. Using last month as default.");
                startDate = endDate.minusMonths(1);
        }

        System.out.println(statistics.generateRevenueReport(startDate, endDate));
    }

    private void generateCustomDateRangeReport() {
        LocalDate startDate = getValidPastDate("Enter start date (YYYY-MM-DD): ");
        LocalDate endDate = getValidEndDate(startDate);

        System.out.println("Custom Date Range Report (" + startDate + " to " + endDate + ")");
        System.out.println("Revenue: $" + statistics.calculateRevenue(startDate, endDate));
        System.out.println("Cancellations: " + statistics.calculateCancellations(startDate, endDate));
    }

    private void generateRoomTypePerformanceReport() {
        LocalDate startDate = getValidPastDate("Enter start date (YYYY-MM-DD): ");
        LocalDate endDate = getValidEndDate(startDate);

        System.out.println("Room Type Performance Report (" + startDate + " to " + endDate + ")");
        System.out.println(statistics.generateRoomTypePerformanceReport(startDate, endDate));
    }

    private void handlePricingManagement() {
        System.out.println("\n--- Pricing Management ---");
        System.out.println("1. Update Base Price");
        System.out.println("2. Set Seasonal Pricing");
        System.out.println("3. Set Event Pricing");
        System.out.println("4. View Current Pricing");
        int choice = getValidIntInput("Enter your choice: ", 4);

        switch (choice) {
            case 1:
                updateBasePrice();
                break;
            case 2:
                setSeasonalPricing();
                break;
            case 3:
                setEventPricing();
                break;
            case 4:
                viewCurrentPricing();
                break;
        }
    }

    private void updateBasePrice() {
        RoomType roomType = getValidRoomType();
        double newPrice = getValidDoubleInput("Enter new base price: ", 0, Double.MAX_VALUE);
        pricingStrategy.updateBasePrice(roomType, newPrice);
        System.out.println("Base price updated successfully.");
    }

    private void setSeasonalPricing() {
        LocalDate startDate = getValidFutureDate("Enter start date (YYYY-MM-DD): ");
        LocalDate endDate = getValidEndDate(startDate);
        double multiplier = getValidDoubleInput("Enter price multiplier: ", 0, Double.MAX_VALUE);
        pricingStrategy.setSeasonalMultiplier(startDate, endDate, multiplier);
        System.out.println("Seasonal pricing set successfully.");
    }

    private void setEventPricing() {
        LocalDate eventDate = getValidFutureDate("Enter event date (YYYY-MM-DD): ");
        String eventName = getValidStringInput("Enter event name: ", "Event name cannot be empty.");
        double multiplier = getValidDoubleInput("Enter price multiplier: ", 0, Double.MAX_VALUE);
        pricingStrategy.setEventPricing(eventDate, eventName, multiplier);
        System.out.println("Event pricing set successfully.");
    }

    private void viewCurrentPricing() {
        System.out.println("Base Prices:");
        pricingStrategy.getCurrentBasePrices().forEach((type, price) ->
                System.out.println(type + ": $" + price));

        System.out.println("\nSeasonal Pricing:");
        pricingStrategy.getSeasonalPricingInfo().forEach(entry ->
                System.out.println(entry.getKey() + ": x" + entry.getValue()));

        System.out.println("\nEvent Pricing:");
        pricingStrategy.getEventPricingInfo().forEach(entry ->
                entry.getValue().forEach((event, multiplier) ->
                        System.out.println(entry.getKey() + " - " + event + ": x" + multiplier)));
    }

    private int getValidIntInput(String prompt, int max) {
        while (true) {
            try {
                System.out.print(prompt);
                int input = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                if (input >= 1 && input <= max) {
                    return input;
                } else {
                    System.out.println("Please enter a number between " + 1 + " and " + max + ".");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid number.");
                scanner.nextLine();
            }
        }
    }

    private double getValidDoubleInput(String prompt, double min, double max) {
        while (true) {
            try {
                System.out.print(prompt);
                double input = scanner.nextDouble();
                scanner.nextLine(); // Consume newline
                if (input >= min && input <= max) {
                    return input;
                } else {
                    System.out.println("Please enter a number between " + min + " and " + max + ".");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid number.");
                scanner.nextLine(); // Consume invalid input
            }
        }
    }

    private String getValidStringInput(String prompt, String errorMessage) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            } else {
                System.out.println(errorMessage);
            }
        }
    }

    private String getOptionalStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private String getValidEmail() {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        while (true) {
            System.out.print("Enter email address: ");
            String email = scanner.nextLine().trim();
            if (pattern.matcher(email).matches()) {
                return email;
            } else {
                System.out.println("Invalid email format. Please try again.");
            }
        }
    }

    private String getOptionalEmail() {
        String email = getOptionalStringInput("Enter new email (or press enter to keep current): ");
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            System.out.println("Invalid email format. Keeping current email.");
            return "";
        }
        return email;
    }

    private String getValidPhoneNumber() {
        String phoneRegex = "^\\+?\\d{10,14}$";
        Pattern pattern = Pattern.compile(phoneRegex);
        while (true) {
            System.out.print("Enter phone number: ");
            String phone = scanner.nextLine().trim();
            if (pattern.matcher(phone).matches()) {
                return phone;
            } else {
                System.out.println("Invalid phone number format. Please try again.");
            }
        }
    }

    private String getOptionalPhoneNumber() {
        String phone = getOptionalStringInput("Enter new phone number (or press enter to keep current): ");
        if (!phone.isEmpty() && !phone.matches("^\\+?\\d{10,14}$")) {
            System.out.println("Invalid phone number format. Keeping current phone number.");
            return "";
        }
        return phone;
    }

    private LocalDate getValidFutureDate(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String dateStr = scanner.nextLine();
                LocalDate date = LocalDate.parse(dateStr);
                if (date.isAfter(LocalDate.now())) {
                    return date;
                } else {
                    System.out.println("Please enter a future date.");
                }
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD.");
            }
        }
    }

    private LocalDate getValidPastDate(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String dateStr = scanner.nextLine();
                LocalDate date = LocalDate.parse(dateStr);
                if (date.isBefore(LocalDate.now()) || date.isEqual(LocalDate.now())) {
                    return date;
                } else {
                    System.out.println("Please enter a past or current date.");
                }
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD.");
            }
        }
    }

    private LocalDate getValidEndDate(LocalDate startDate) {
        while (true) {
            try {
                System.out.print("Enter end date (YYYY-MM-DD): ");
                String dateStr = scanner.nextLine();
                LocalDate endDate = LocalDate.parse(dateStr);
                if (endDate.isAfter(startDate)) {
                    return endDate;
                } else {
                    System.out.println("End date must be after start date.");
                }
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD.");
            }
        }
    }

    private LocalDate getOptionalFutureDate(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String dateStr = scanner.nextLine().trim();
                if (dateStr.isEmpty()) {
                    return null;
                }
                LocalDate date = LocalDate.parse(dateStr);
                if (date.isAfter(LocalDate.now())) {
                    return date;
                } else {
                    System.out.println("Please enter a future date.");
                }
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD.");
            }
        }
    }

    private LocalDate getOptionalEndDate(LocalDate startDate, String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String dateStr = scanner.nextLine().trim();
                if (dateStr.isEmpty()) {
                    return null;
                }
                LocalDate endDate = LocalDate.parse(dateStr);
                if (endDate.isAfter(startDate)) {
                    return endDate;
                } else {
                    System.out.println("End date must be after start date.");
                }
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD.");
            }
        }
    }

    private boolean getValidBoolean(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().toLowerCase().trim();
            if (input.equals("true") || input.equals("false")) {
                return Boolean.parseBoolean(input);
            } else {
                System.out.println("Invalid input. Please enter true or false.");
            }
        }
    }

    private Boolean getOptionalBoolean(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().toLowerCase().trim();
        if (input.isEmpty()) {
            return null;
        } else if (input.equals("true") || input.equals("false")) {
            return Boolean.parseBoolean(input);
        } else {
            System.out.println("Invalid input. Keeping current value.");
            return null;
        }
    }

    private RoomType getValidRoomType() {
        while (true) {
            try {
                System.out.print("Enter room type (SINGLE/DOUBLE/SUITE): ");
                return RoomType.valueOf(scanner.nextLine().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid room type. Please enter SINGLE, DOUBLE, or SUITE.");
            }
        }
    }

    private RoomType getOptionalRoomType(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().toUpperCase().trim();
        if (input.isEmpty()) {
            return null;
        }
        try {
            return RoomType.valueOf(input);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid room type. Keeping current type.");
            return null;
        }
    }

    private ReservationStatus getOptionalReservationStatus(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().toUpperCase().trim();
        if (input.isEmpty()) {
            return null;
        }
        try {
            return ReservationStatus.valueOf(input);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid reservation status. Keeping current status.");
            return null;
        }
    }

    public static void main(String[] args) {
        HotelManagementSystem system = new HotelManagementSystem();
        system.run();
    }
}
