package com.agriculture.rental.service;

import com.agriculture.rental.model.Equipment;
import com.agriculture.rental.repository.EquipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EquipmentService {

    @Autowired
    private EquipmentRepository equipmentRepository;

    /**
     * Get all equipment
     */
    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAll();
    }

    /**
     * Get available equipment only
     */
    public List<Equipment> getAvailableEquipment() {
        return equipmentRepository.findByStatus(Equipment.EquipmentStatus.AVAILABLE);
    }

    /**
     * Get equipment by ID
     */
    public Optional<Equipment> getEquipmentById(Long id) {
        return equipmentRepository.findById(id);
    }

    /**
     * Get equipment by category
     */
    public List<Equipment> getEquipmentByCategory(String category) {
        return equipmentRepository.findByCategory(category);
    }

    /**
     * Search equipment by name
     */
    public List<Equipment> searchEquipment(String name) {
        return equipmentRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Add new equipment
     */
    public Equipment addEquipment(Equipment equipment) {
        return equipmentRepository.save(equipment);
    }

    /**
     * Update existing equipment
     */
    public Equipment updateEquipment(Long id, Equipment updatedEquipment) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment not found with id: " + id));

        equipment.setName(updatedEquipment.getName());
        equipment.setDescription(updatedEquipment.getDescription());
        equipment.setCategory(updatedEquipment.getCategory());
        equipment.setDailyRate(updatedEquipment.getDailyRate());
        equipment.setImageUrl(updatedEquipment.getImageUrl());
        equipment.setStatus(updatedEquipment.getStatus());
        equipment.setQuantity(updatedEquipment.getQuantity());
        equipment.setLocation(updatedEquipment.getLocation());

        return equipmentRepository.save(equipment);
    }

    /**
     * Delete equipment
     */
    public void deleteEquipment(Long id) {
        if (!equipmentRepository.existsById(id)) {
            throw new RuntimeException("Equipment not found with id: " + id);
        }
        equipmentRepository.deleteById(id);
    }

    /**
     * Update equipment status
     */
    public Equipment updateStatus(Long id, Equipment.EquipmentStatus status) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment not found with id: " + id));
        equipment.setStatus(status);
        return equipmentRepository.save(equipment);
    }

    /**
     * Get total equipment count
     */
    public long getTotalEquipment() {
        return equipmentRepository.count();
    }

    /**
     * Get available equipment count
     */
    public long getAvailableCount() {
        return equipmentRepository.findByStatus(Equipment.EquipmentStatus.AVAILABLE).size();
    }
}
