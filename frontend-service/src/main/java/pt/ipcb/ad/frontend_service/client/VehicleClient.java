package pt.ipcb.ad.frontend_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import pt.ipcb.ad.frontend_service.dto.VehicleDto;
import java.util.List;

@FeignClient(name = "vehicle-service")
public interface VehicleClient {

    @GetMapping("/vehicles/viewall")
    List<VehicleDto> getAllVehicles();

    @PostMapping("/vehicles")
    VehicleDto createVehicle(@RequestBody VehicleDto vehicle);

    @GetMapping("/vehicles/{id}")
    VehicleDto getVehicleById(@PathVariable("id") Long id);

    @PutMapping("/vehicles/{id}/availability")
    void updateAvailability(@PathVariable("id") Long id, @RequestParam("available") boolean available);
}