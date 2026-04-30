package com.agriculture.rental.controller;

import com.agriculture.rental.dto.ApiResponse;
import com.agriculture.rental.model.User;
import com.agriculture.rental.service.BookingService;
import com.agriculture.rental.service.EquipmentService;
import com.agriculture.rental.service.PaymentService;
import com.agriculture.rental.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private EquipmentService equipmentService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private PaymentService paymentService;

    /**
     * GET /api/admin/dashboard - Get dashboard statistics
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userService.getTotalUsers());
        stats.put("totalEquipment", equipmentService.getTotalEquipment());
        stats.put("availableEquipment", equipmentService.getAvailableCount());
        stats.put("totalBookings", bookingService.getTotalBookings());
        stats.put("activeBookings", bookingService.getActiveBookingsCount());
        stats.put("totalRevenue", paymentService.getTotalRevenue());
        stats.put("paidPayments", paymentService.getPaidCount());
        stats.put("pendingPayments", paymentService.getPendingCount());

        return ResponseEntity.ok(ApiResponse.success("Dashboard stats retrieved", stats));
    }

    /**
     * GET /api/admin/users - Get all users
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        // Clear passwords before sending
        users.forEach(u -> u.setPassword(null));
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    /**
     * DELETE /api/admin/users/{id} - Delete a user
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
