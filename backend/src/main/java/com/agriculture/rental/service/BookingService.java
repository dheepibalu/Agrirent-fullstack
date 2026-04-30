package com.agriculture.rental.service;

import com.agriculture.rental.dto.RentRequest;
import com.agriculture.rental.model.Booking;
import com.agriculture.rental.model.Equipment;
import com.agriculture.rental.model.User;
import com.agriculture.rental.repository.BookingRepository;
import com.agriculture.rental.repository.EquipmentRepository;
import com.agriculture.rental.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Create a new booking/rental
     */
    @Transactional
    public Booking createBooking(RentRequest request) {
        // Validate dates
        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Start date cannot be in the past");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("End date must be after start date");
        }

        // Get user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        // Get equipment
        Equipment equipment = equipmentRepository.findById(request.getEquipmentId())
                .orElseThrow(() -> new RuntimeException("Equipment not found with id: " + request.getEquipmentId()));

        // Check if equipment is available
        if (equipment.getStatus() != Equipment.EquipmentStatus.AVAILABLE) {
            throw new RuntimeException("Equipment is not available for rental");
        }

        // Check for conflicting bookings
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                request.getEquipmentId(),
                request.getStartDate(),
                request.getEndDate()
        );

        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Equipment is already booked for the selected dates");
        }

        // Create booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setEquipment(equipment);
        booking.setStartDate(request.getStartDate());
        booking.setEndDate(request.getEndDate());
        booking.setNotes(request.getNotes());
        booking.setStatus(Booking.BookingStatus.CONFIRMED);

        // Save booking
        Booking savedBooking = bookingRepository.save(booking);

        // Update equipment status
        equipment.setStatus(Equipment.EquipmentStatus.RENTED);
        equipmentRepository.save(equipment);

        // Send confirmation email
        try { emailService.sendBookingConfirmation(savedBooking); } catch (Exception e) { /* silent */ }

        return savedBooking;
    }

    /**
     * Get all bookings
     */
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    /**
     * Get booking by ID
     */
    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    /**
     * Get bookings by user
     */
    public List<Booking> getBookingsByUser(Long userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Get bookings by equipment
     */
    public List<Booking> getBookingsByEquipment(Long equipmentId) {
        return bookingRepository.findByEquipmentId(equipmentId);
    }

    /**
     * Get bookings by status
     */
    public List<Booking> getBookingsByStatus(Booking.BookingStatus status) {
        return bookingRepository.findByStatus(status);
    }

    /**
     * Update booking status
     */
    @Transactional
    public Booking updateBookingStatus(Long id, Booking.BookingStatus status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));

        booking.setStatus(status);
        Booking updatedBooking = bookingRepository.save(booking);

        // If booking is completed or cancelled, make equipment available again
        if (status == Booking.BookingStatus.COMPLETED || status == Booking.BookingStatus.CANCELLED) {
            Equipment equipment = booking.getEquipment();
            equipment.setStatus(Equipment.EquipmentStatus.AVAILABLE);
            equipmentRepository.save(equipment);
        }

        return updatedBooking;
    }

    /**
     * Cancel booking
     */
    @Transactional
    public Booking cancelBooking(Long id) {
        return updateBookingStatus(id, Booking.BookingStatus.CANCELLED);
    }

    /**
     * Complete booking
     */
    @Transactional
    public Booking completeBooking(Long id) {
        return updateBookingStatus(id, Booking.BookingStatus.COMPLETED);
    }

    /**
     * Delete booking
     */
    public void deleteBooking(Long id) {
        if (!bookingRepository.existsById(id)) {
            throw new RuntimeException("Booking not found with id: " + id);
        }
        bookingRepository.deleteById(id);
    }

    /**
     * Get total bookings count
     */
    public long getTotalBookings() {
        return bookingRepository.count();
    }

    /**
     * Get active bookings count
     */
    public long getActiveBookingsCount() {
        return bookingRepository.countByStatus(Booking.BookingStatus.ACTIVE) +
               bookingRepository.countByStatus(Booking.BookingStatus.CONFIRMED);
    }
}
