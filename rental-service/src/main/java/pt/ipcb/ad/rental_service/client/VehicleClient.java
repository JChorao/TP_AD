package pt.ipcb.ad.rental_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping; // <--- Importante
import org.springframework.web.bind.annotation.RequestParam; // <--- Importante
import pt.ipcb.ad.rental_service.dto.VehicleDto;

@FeignClient(name = "vehicle-service")
public interface VehicleClient {

    @GetMapping("/vehicles/{id}")
    VehicleDto getVehicleById(@PathVariable("id") Long id);

    // --- NOVO MÉTODO ---
    @PutMapping("/vehicles/{id}/availability")
    void updateAvailability(@PathVariable("id") Long id, @RequestParam("available") boolean available);
}