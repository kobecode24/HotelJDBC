package main.java.com.hotelreservation;


import main.java.com.hotelreservation.model.*;
import main.java.com.hotelreservation.model.enums.*;
import main.java.com.hotelreservation.repository.*;
import main.java.com.hotelreservation.service.*;
import main.java.com.hotelreservation.util.DatabaseConnection;
import main.java.com.hotelreservation.pricing.DynamicPricing;
import main.java.com.hotelreservation.statistics.Statistics;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

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
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

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
                default:
                    System.out.println("Invalid choice. Please try again.");
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
        System.out.print("Enter your choice: ");
    }

    private void handleCustomerOperations() {
        System.out.println("\n--- Customer Operations ---");
        System.out.println("1. Add New Customer");
        System.out.println("2. Find Customer by ID");
        System.out.println("3. Update Customer");
        System.out.println("4. Delete Customer");
        System.out.print("Enter your choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

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
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void addNewCustomer() {
        String name, email, phone;

        do {
            System.out.print("Enter customer name: ");
            name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                System.out.println("Name cannot be empty. Please try again.");
            }
        } while (name.isEmpty());

        do {
            System.out.print("Enter customer email: ");
            email = scanner.nextLine().trim();
            if (!email.contains("@")) {
                System.out.println("Please enter a valid email address.");
            }
        } while (!email.contains("@"));

        System.out.print("Enter customer phone: ");
        phone = scanner.nextLine().trim();

        try {
            Customer customer = new Customer(0, name, email, phone);
            Customer createdCustomer = customerService.createCustomer(customer);
            System.out.println("Customer added successfully with ID: " + createdCustomer.getCustomerId());
        } catch (RuntimeException e) {
            System.out.println("Error adding customer: " + e.getMessage());
        }
    }

    private void findCustomerById() {
        System.out.print("Enter customer ID: ");
        int id = scanner.nextInt();
        Optional<Customer> customer = customerService.getCustomerById(id);
        customer.ifPresentOrElse(
                c -> System.out.println("Customer found: " + c),
                () -> System.out.println("Customer not found.")
        );
    }

    private void updateCustomer() {
        System.out.print("Enter customer ID to update: ");
        int id = validateIntInput("Customer ID");
        scanner.nextLine(); // Consume newline

        Optional<Customer> optionalCustomer = customerService.getCustomerById(id);
        if (optionalCustomer.isPresent()) {
            Customer customer = optionalCustomer.get();
            System.out.print("Enter new name (or press enter to keep current): ");
            String name = scanner.nextLine().trim();
            if (!name.isEmpty()) customer.setName(name);

            String email;
            do {
                System.out.print("Enter new email (or press enter to keep current): ");
                email = scanner.nextLine().trim();
                if (!email.isEmpty() && !email.contains("@")) {
                    System.out.println("Please enter a valid email address.");
                }
            } while (!email.isEmpty() && !email.contains("@"));
            if (!email.isEmpty()) customer.setEmail(email);

            System.out.print("Enter new phone (or press enter to keep current): ");
            String phone = scanner.nextLine().trim();
            if (!phone.isEmpty()) customer.setPhoneNumber(phone);

            customerService.updateCustomer(customer);
            System.out.println("Customer updated successfully.");
        } else {
            System.out.println("Customer not found.");
        }
    }

    private void deleteCustomer() {
        System.out.print("Enter customer ID to delete: ");
        int id = scanner.nextInt();
        customerService.deleteCustomer(id);
        System.out.println("Customer deleted successfully.");
    }

    private void handleReservationOperations() {
        System.out.println("\n--- Reservation Operations ---");
        System.out.println("1. Make New Reservation");
        System.out.println("2. Find Reservation by ID");
        System.out.println("3. Update Reservation");
        System.out.println("4. Cancel Reservation");
        System.out.print("Enter your choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

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
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void makeNewReservation() {
        System.out.print("Enter customer ID: ");
        int customerId = validateIntInput("Customer ID");
        System.out.print("Enter room ID: ");
        int roomId = validateIntInput("Room ID");
        scanner.nextLine();

        LocalDate startDate = null;
        while (startDate == null) {
            System.out.print("Enter start date (YYYY-MM-DD): ");
            String startDateStr = scanner.nextLine();
            try {
                startDate = LocalDate.parse(startDateStr);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD.");
            }
        }

        LocalDate endDate = null;
        while (endDate == null || !endDate.isAfter(startDate)) {
            System.out.print("Enter end date (YYYY-MM-DD): ");
            String endDateStr = scanner.nextLine();
            try {
                endDate = LocalDate.parse(endDateStr);
                if (!endDate.isAfter(startDate)) {
                    System.out.println("End date must be after start date.");
                }
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD.");
            }
        }

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
        System.out.print("Enter reservation ID: ");
        int id = validateIntInput("Reservation ID");
        Optional<Reservation> reservation = reservationService.getReservationById(id);
        reservation.ifPresentOrElse(
                r -> System.out.println("Reservation found: " + r),
                () -> System.out.println("Reservation not found.")
        );
    }

    private void updateReservation() {
        System.out.print("Enter reservation ID to update: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        Optional<Reservation> optionalReservation = reservationService.getReservationById(id);
        if (optionalReservation.isPresent()) {
            Reservation reservation = optionalReservation.get();
            System.out.print("Enter new start date (YYYY-MM-DD) or press enter to keep current: ");
            String startDateStr = scanner.nextLine();
            if (!startDateStr.isEmpty()) reservation.setStartDate(LocalDate.parse(startDateStr));

            System.out.print("Enter new end date (YYYY-MM-DD) or press enter to keep current: ");
            String endDateStr = scanner.nextLine();
            if (!endDateStr.isEmpty()) reservation.setEndDate(LocalDate.parse(endDateStr));

            System.out.print("Enter new status (CONFIRMED/CANCELLED) or press enter to keep current: ");
            String statusStr = scanner.nextLine();
            if (!statusStr.isEmpty()) reservation.setStatus(ReservationStatus.valueOf(statusStr.toUpperCase()));

            reservationService.updateReservation(reservation);
            System.out.println("Reservation updated successfully.");
        } else {
            System.out.println("Reservation not found.");
        }
    }

    private void cancelReservation() {
        System.out.print("Enter reservation ID to cancel: ");
        int id = scanner.nextInt();
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
        System.out.print("Enter your choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

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
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void addNewRoom() {
        System.out.print("Enter hotel ID: ");
        int hotelId = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        RoomType roomType = null;
        while (roomType == null) {
            System.out.print("Enter room type (SINGLE/DOUBLE/SUITE): ");
            String roomTypeInput = scanner.nextLine().toUpperCase();
            try {
                roomType = RoomType.valueOf(roomTypeInput);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid room type. Please enter SINGLE, DOUBLE, or SUITE.");
            }
        }

        boolean isAvailable = false;
        while (true) {
            System.out.print("Is room available? (true/false): ");
            String availableInput = scanner.nextLine().toLowerCase();
            if (availableInput.equals("true") || availableInput.equals("false")) {
                isAvailable = Boolean.parseBoolean(availableInput);
                break;
            } else {
                System.out.println("Invalid input. Please enter true or false.");
            }
        }

        try {
            Room room = new Room(0, roomType, isAvailable, hotelId);
            Room createdRoom = roomService.createRoom(room);
            System.out.println("Room added successfully with ID: " + createdRoom.getRoomId());
        } catch (Exception e) {
            System.out.println("Error adding room: " + e.getMessage());
        }
    }

    private void findRoomById() {
        System.out.print("Enter room ID: ");
        int id = scanner.nextInt();
        Optional<Room> room = roomService.getRoomById(id);
        room.ifPresentOrElse(
                r -> System.out.println("Room found: " + r),
                () -> System.out.println("Room not found.")
        );
    }

    private void updateRoom() {
        System.out.print("Enter room ID to update: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        Optional<Room> optionalRoom = roomService.getRoomById(id);
        if (optionalRoom.isPresent()) {
            Room room = optionalRoom.get();
            System.out.print("Enter new room type (SINGLE/DOUBLE/SUITE) or press enter to keep current: ");
            String roomTypeStr = scanner.nextLine();
            if (!roomTypeStr.isEmpty()) room.setRoomType(RoomType.valueOf(roomTypeStr.toUpperCase()));

            System.out.print("Is room available? (true/false) or press enter to keep current: ");
            String availableStr = scanner.nextLine();
            if (!availableStr.isEmpty()) room.setAvailable(Boolean.parseBoolean(availableStr));

            roomService.updateRoom(room);
            System.out.println("Room updated successfully.");
        } else {
            System.out.println("Room not found.");
        }
    }

    private void deleteRoom() {
        System.out.print("Enter room ID to delete: ");
        int id = scanner.nextInt();
        roomService.deleteRoom(id);
        System.out.println("Room deleted successfully.");
    }

    private void handleHotelOperations() {
        System.out.println("\n--- Hotel Operations ---");
        System.out.println("1. Add New Hotel");
        System.out.println("2. Find Hotel by ID");
        System.out.println("3. Update Hotel");
        System.out.println("4. Delete Hotel");
        System.out.print("Enter your choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

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
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void addNewHotel() {
        System.out.print("Enter hotel name: ");
        String name = scanner.nextLine();
        System.out.print("Enter hotel address: ");
        String address = scanner.nextLine();

        Hotel hotel = new Hotel(0, name, address);
        Hotel createdHotel = hotelService.createHotel(hotel);
        System.out.println("Hotel added successfully with ID: " + createdHotel.getHotelId());
    }

    private void findHotelById() {
        System.out.print("Enter hotel ID: ");
        int id = scanner.nextInt();
        Optional<Hotel> hotel = hotelService.getHotelById(id);
        hotel.ifPresentOrElse(
                h -> System.out.println("Hotel found: " + h),
                () -> System.out.println("Hotel not found.")
        );
    }

    private void updateHotel() {
        System.out.print("Enter hotel ID to update: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        Optional<Hotel> optionalHotel = hotelService.getHotelById(id);
        if (optionalHotel.isPresent()) {
            Hotel hotel = optionalHotel.get();
            System.out.print("Enter new name (or press enter to keep current): ");
            String name = scanner.nextLine();
            if (!name.isEmpty()) hotel.setName(name);

            System.out.print("Enter new address (or press enter to keep current): ");
            String address = scanner.nextLine();
            if (!address.isEmpty()) hotel.setAddress(address);

            hotelService.updateHotel(hotel);
            System.out.println("Hotel updated successfully.");
        } else {
            System.out.println("Hotel not found.");
        }
    }

    private void deleteHotel() {
        System.out.print("Enter hotel ID to delete: ");
        int id = scanner.nextInt();
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
        System.out.print("Enter your choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

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
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void generateCustomDateRangeReport() {
        LocalDate startDate = getDateInput("Enter start date (YYYY-MM-DD): ");
        LocalDate endDate = getDateInput("Enter end date (YYYY-MM-DD): ");

        System.out.println("Custom Date Range Report (" + startDate + " to " + endDate + ")");
        System.out.println("Revenue: $" + statistics.calculateRevenue(startDate, endDate));
        System.out.println("Cancellations: " + statistics.calculateCancellations(startDate, endDate));
    }

    private void generateRevenueReport() {
        System.out.println("Select time period:");
        System.out.println("1. Last Month");
        System.out.println("2. Last Quarter");
        System.out.println("3. Last Year");
        System.out.println("4. Custom Date Range");
        System.out.print("Enter your choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

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
                startDate = getDateInput("Enter start date (YYYY-MM-DD): ");
                endDate = getDateInput("Enter end date (YYYY-MM-DD): ");
                break;
            default:
                System.out.println("Invalid choice. Using last month as default.");
                startDate = endDate.minusMonths(1);
        }

        System.out.println(statistics.generateRevenueReport(startDate, endDate));
    }

    /*private void generateCustomDateRangeReport() {
        LocalDate startDate = getDateInput("Enter start date (YYYY-MM-DD): ");
        LocalDate endDate = getDateInput("Enter end date (YYYY-MM-DD): ");

        System.out.println("Custom Date Range Report (" + startDate + " to " + endDate + ")");
        System.out.println("Occupancy: " + statistics.calculateOccupancy(startDate, endDate));
        System.out.println("Revenue: $" + statistics.calculateRevenue(startDate, endDate));
        System.out.println("Cancellations: " + statistics.calculateCancellations(startDate, endDate));
    }*/

    private void generateRoomTypePerformanceReport() {
        LocalDate startDate = getDateInput("Enter start date (YYYY-MM-DD): ");
        LocalDate endDate = getDateInput("Enter end date (YYYY-MM-DD): ");

        System.out.println("Room Type Performance Report (" + startDate + " to " + endDate + ")");
        System.out.println(statistics.generateRoomTypePerformanceReport(startDate, endDate));
    }

    private LocalDate getDateInput(String prompt) {
        LocalDate date = null;
        while (date == null) {
            System.out.print(prompt);
            String dateStr = scanner.nextLine();
            try {
                date = LocalDate.parse(dateStr);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD.");
            }
        }
        return date;
    }

    private void handlePricingManagement() {
        System.out.println("\n--- Pricing Management ---");
        System.out.println("1. Update Base Price");
        System.out.println("2. Set Seasonal Pricing");
        System.out.println("3. Set Event Pricing");
        System.out.println("4. View Current Pricing");
        System.out.print("Enter your choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

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
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void updateBasePrice() {
        System.out.println("Enter room type (SINGLE/DOUBLE/SUITE): ");
        RoomType roomType = RoomType.valueOf(scanner.nextLine().toUpperCase());
        System.out.println("Enter new base price: ");
        double newPrice = scanner.nextDouble();
        pricingStrategy.updateBasePrice(roomType, newPrice);
        System.out.println("Base price updated successfully.");
    }

    private void setSeasonalPricing() {
        System.out.println("Enter start date (YYYY-MM-DD): ");
        LocalDate startDate = LocalDate.parse(scanner.nextLine());
        System.out.println("Enter end date (YYYY-MM-DD): ");
        LocalDate endDate = LocalDate.parse(scanner.nextLine());
        System.out.println("Enter price multiplier: ");
        double multiplier = scanner.nextDouble();
        pricingStrategy.setSeasonalMultiplier(startDate, endDate, multiplier);
        System.out.println("Seasonal pricing set successfully.");
    }

    private void setEventPricing() {
        System.out.println("Enter event date (YYYY-MM-DD): ");
        LocalDate eventDate = LocalDate.parse(scanner.nextLine());
        System.out.println("Enter event name: ");
        String eventName = scanner.nextLine();
        System.out.println("Enter price multiplier: ");
        double multiplier = scanner.nextDouble();
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


    private int validateIntInput(String fieldName) {
        while (true) {
            try {
                return scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid number for " + fieldName + ".");
                scanner.next();
            }
        }
    }

    private LocalDate getOptionalDateInput(String prompt) {
        System.out.print(prompt);
        String dateStr = scanner.nextLine().trim();
        if (dateStr.isEmpty()) {
            return null;
        }
        return getDateInput(prompt);
    }
    public static void main(String[] args) {
        HotelManagementSystem system = new HotelManagementSystem();
        system.run();
    }
}