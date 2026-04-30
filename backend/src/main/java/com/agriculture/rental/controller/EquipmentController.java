package com.agriculture.rental.controller;

import com.agriculture.rental.dto.ApiResponse;
import com.agriculture.rental.model.Equipment;
import com.agriculture.rental.service.EquipmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/equipment")
@CrossOrigin(origins = "*")
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;

    /**
     * GET /api/equipment - Get all equipment
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Equipment>>> getAllEquipment(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {

        List<Equipment> equipmentList;

        if (search != null && !search.isEmpty()) {
            equipmentList = equipmentService.searchEquipment(search);
        } else if (category != null && !category.isEmpty()) {
            equipmentList = equipmentService.getEquipmentByCategory(category);
        } else if ("available".equalsIgnoreCase(status)) {
            equipmentList = equipmentService.getAvailableEquipment();
        } else {
            equipmentList = equipmentService.getAllEquipment();
        }

        return ResponseEntity.ok(ApiResponse.success("Equipment retrieved successfully", equipmentList));
    }

    /**
     * GET /api/equipment/{id} - Get equipment by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Equipment>> getEquipmentById(@PathVariable Long id) {
        Optional<Equipment> equipment = equipmentService.getEquipmentById(id);
        return equipment.map(e -> ResponseEntity.ok(ApiResponse.success("Equipment found", e)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Equipment not found with id: " + id)));
    }

    /**
     * POST /api/equipment - Add new equipment (Admin only)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Equipment>> addEquipment(@Valid @RequestBody Equipment equipment) {
        try {
            Equipment saved = equipmentService.addEquipment(equipment);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Equipment added successfully", saved));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * PUT /api/equipment/{id} - Update equipment (Admin only)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Equipment>> updateEquipment(
            @PathVariable Long id,
            @Valid @RequestBody Equipment equipment) {
        try {
            Equipment updated = equipmentService.updateEquipment(id, equipment);
            return ResponseEntity.ok(ApiResponse.success("Equipment updated successfully", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * DELETE /api/equipment/{id} - Delete equipment (Admin only)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEquipment(@PathVariable Long id) {
        try {
            equipmentService.deleteEquipment(id);
            return ResponseEntity.ok(ApiResponse.success("Equipment deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * PATCH /api/equipment/{id}/status - Update equipment status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Equipment>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            Equipment.EquipmentStatus equipmentStatus = Equipment.EquipmentStatus.valueOf(status.toUpperCase());
            Equipment updated = equipmentService.updateStatus(id, equipmentStatus);
            return ResponseEntity.ok(ApiResponse.success("Status updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid status value: " + status));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
