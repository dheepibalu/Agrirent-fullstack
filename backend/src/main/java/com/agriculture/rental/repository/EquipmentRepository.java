package com.agriculture.rental.repository;

import com.agriculture.rental.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    List<Equipment> findByStatus(Equipment.EquipmentStatus status);

    List<Equipment> findByCategory(String category);

    List<Equipment> findByNameContainingIgnoreCase(String name);

    List<Equipment> findByCategoryAndStatus(String category, Equipment.EquipmentStatus status);
}
