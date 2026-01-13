package pt.ipcb.ad.veiculos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pt.ipcb.ad.veiculos.model.Vehicle;
import pt.ipcb.ad.veiculos.repository.VehicleRepository;

import java.util.List;

@RestController
@RequestMapping("/vehicles")
public class VehicleController {

    @Autowired
    private VehicleRepository repository;

    // Teste simples
    @GetMapping("/test")
    public String test() {
        return "O Microserviço de Veículos (Porta 8082) está a funcionar!";
    }

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

    // Adicionar um carro novo (via POST)
    @PostMapping
    public Vehicle addVehicle(@RequestBody Vehicle vehicle) {
        return repository.save(vehicle);
    }

    @PutMapping("/{id}/availability")
    public Vehicle updateAvailability(@PathVariable Long id, @RequestParam boolean available) {
        Vehicle vehicle = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));

        vehicle.setAvailable(available);
        return repository.save(vehicle);
    }
}