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

    // Listar todos os carros
    @GetMapping("/viewall")
    public List<Vehicle> getAllVehicles() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Vehicle getVehicleById(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));
    }

    @PutMapping("/{id}/availability")
    public Vehicle updateAvailability(@PathVariable Long id, @RequestParam boolean available) {
        Vehicle vehicle = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));

        vehicle.setAvailable(available);
        return repository.save(vehicle);
    }

    @PostMapping
    public ResponseEntity<Vehicle> createVehicle(@RequestBody Vehicle vehicle) {
        // Garante que o ID é nulo para criar um novo e não atualizar
        Vehicle savedVehicle = repository.save(vehicle);
        return ResponseEntity.ok(savedVehicle);
    }
}