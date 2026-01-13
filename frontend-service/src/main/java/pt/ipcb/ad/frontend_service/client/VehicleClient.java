package pt.ipcb.ad.frontend_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import pt.ipcb.ad.frontend_service.dto.VehicleDto;
import java.util.List;

@FeignClient(name = "vehicle-service")
public interface VehicleClient {

    // Lista todos os veículos
    @GetMapping("/vehicles/viewall")
    List<VehicleDto> getAllVehicles();

    // Cria um novo veículo
    @PostMapping("/vehicles")
    VehicleDto createVehicle(@RequestBody VehicleDto vehicle);

    // --- MÉTODO ADICIONADO PARA O WEB CONTROLLER ---
    @PutMapping("/vehicles/{id}")
    VehicleDto updateVehicle(@PathVariable("id") Long id, @RequestBody VehicleDto vehicle);

    // Atualiza apenas a disponibilidade
    @PutMapping("/vehicles/{id}/availability")
    void updateAvailability(@PathVariable("id") Long id, @RequestParam("available") boolean available);

    // Remove um veículo
    @DeleteMapping("/vehicles/{id}")
    void deleteVehicle(@PathVariable("id") Long id);
}