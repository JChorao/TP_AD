package pt.ipcb.ad.veiculos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ipcb.ad.veiculos.model.Vehicle;
import pt.ipcb.ad.veiculos.repository.VehicleRepository;

import java.util.List;

@RestController
@RequestMapping("/vehicles")
public class VehicleController {

    @Autowired
    private VehicleRepository repository;

    @GetMapping("/viewall")
    public List<Vehicle> getAllVehicles() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Vehicle getVehicleById(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));
    }

    @PostMapping
    public ResponseEntity<Vehicle> createVehicle(@RequestBody Vehicle vehicle) {
        // Garante que default é true se não vier preenchido
        if (!vehicle.isAvailable()) vehicle.setAvailable(true);

        Vehicle savedVehicle = repository.save(vehicle);
        return ResponseEntity.ok(savedVehicle);
    }

    // --- NOVO: ATUALIZAR VEÍCULO ---
    @PutMapping("/{id}")
    public Vehicle updateVehicle(@PathVariable Long id, @RequestBody Vehicle updated) {
        return repository.findById(id).map(v -> {
            v.setBrand(updated.getBrand());
            v.setModel(updated.getModel());
            v.setLicensePlate(updated.getLicensePlate());
            v.setPricePerHour(updated.getPricePerHour());
            v.setLatitude(updated.getLatitude());
            v.setLongitude(updated.getLongitude());
            return repository.save(v);
        }).orElseThrow(() -> new RuntimeException("Veículo não encontrado"));
    }

    // --- NOVO: APAGAR VEÍCULO ---
    @DeleteMapping("/{id}")
    public void deleteVehicle(@PathVariable Long id) {
        repository.deleteById(id);
    }

    // Mantemos este para compatibilidade com o Aluguer
    @PutMapping("/{id}/availability")
    public Vehicle updateAvailability(@PathVariable Long id, @RequestParam boolean available) {
        Vehicle vehicle = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));
        vehicle.setAvailable(available);
        return repository.save(vehicle);
    }

}