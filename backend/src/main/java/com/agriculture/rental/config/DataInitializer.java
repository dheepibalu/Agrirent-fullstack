package com.agriculture.rental.config;

import com.agriculture.rental.model.Equipment;
import com.agriculture.rental.model.User;
import com.agriculture.rental.repository.EquipmentRepository;
import com.agriculture.rental.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create admin user if not exists
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setFullName("System Administrator");
            admin.setUsername("admin");
            admin.setEmail("admin@agrirent.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setPhone("1234567890");
            admin.setAddress("Farm HQ, Agricultural Zone");
            admin.setRole(User.Role.ADMIN);
            admin.setActive(true);
            userRepository.save(admin);
            System.out.println("✅ Admin user created: username=admin, password=admin123");
        }

        // Create sample user if not exists
        if (!userRepository.existsByUsername("farmer1")) {
            User user = new User();
            user.setFullName("John Farmer");
            user.setUsername("farmer1");
            user.setEmail("john@farm.com");
            user.setPassword(passwordEncoder.encode("farmer123"));
            user.setPhone("9876543210");
            user.setAddress("123 Farm Road, Rural District");
            user.setRole(User.Role.USER);
            user.setActive(true);
            userRepository.save(user);
            System.out.println("✅ Sample user created: username=farmer1, password=farmer123");
        }

        // Create sample equipment if none exists
        if (equipmentRepository.count() == 0) {
            addEquipment("John Deere Tractor 5075E",
                    "75 HP utility tractor, perfect for medium-sized farms. Features 4WD and power steering.",
                    "Tractor", new BigDecimal("2500.00"), "AVAILABLE", 2, "North Farm Depot");

            addEquipment("Combine Harvester CX8",
                    "High-capacity combine harvester for wheat, corn, and soybean. 300 HP engine.",
                    "Harvester", new BigDecimal("5000.00"), "AVAILABLE", 1, "Central Equipment Hub");

            addEquipment("Rotary Tiller RT-200",
                    "Heavy-duty rotary tiller for soil preparation. 2-meter working width.",
                    "Tiller", new BigDecimal("800.00"), "AVAILABLE", 3, "South Farm Depot");

            addEquipment("Seed Drill SD-12",
                    "12-row precision seed drill for accurate seed placement. Compatible with most tractors.",
                    "Planting", new BigDecimal("1200.00"), "AVAILABLE", 2, "North Farm Depot");

            addEquipment("Irrigation Pump IP-500",
                    "High-flow irrigation pump, 500 LPM capacity. Diesel powered with 50m head.",
                    "Irrigation", new BigDecimal("600.00"), "AVAILABLE", 4, "Water Management Center");

            addEquipment("Crop Sprayer CS-1000",
                    "1000-liter boom sprayer for pesticide and fertilizer application. 18m boom width.",
                    "Sprayer", new BigDecimal("1500.00"), "AVAILABLE", 2, "Central Equipment Hub");

            addEquipment("Mini Excavator ME-30",
                    "3-ton mini excavator for drainage and land leveling. Easy to transport.",
                    "Excavator", new BigDecimal("3500.00"), "AVAILABLE", 1, "Heavy Equipment Yard");

            addEquipment("Hay Baler HB-100",
                    "Round hay baler producing 1.2m diameter bales. High-density baling system.",
                    "Harvester", new BigDecimal("1800.00"), "AVAILABLE", 2, "South Farm Depot");

            System.out.println("✅ Sample equipment data created (8 items)");
        }
    }

    private void addEquipment(String name, String description, String category,
                               BigDecimal dailyRate, String status, int quantity, String location) {
        Equipment equipment = new Equipment();
        equipment.setName(name);
        equipment.setDescription(description);
        equipment.setCategory(category);
        equipment.setDailyRate(dailyRate);
        equipment.setStatus(Equipment.EquipmentStatus.valueOf(status));
        equipment.setQuantity(quantity);
        equipment.setLocation(location);
        equipmentRepository.save(equipment);
    }
}
