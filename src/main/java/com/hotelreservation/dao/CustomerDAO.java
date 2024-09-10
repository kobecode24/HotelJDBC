package main.java.com.hotelreservation.dao;

import main.java.com.hotelreservation.model.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerDAO {
    Optional<Customer> findById(int id);
    List<Customer> findAll();
    void save(Customer customer);
    void update(Customer customer);
    void delete(int id);
    Optional<Customer> findByEmail(String email);
    List<Customer> findByName(String name);
}
