package pt.ipcb.ad.frontend_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping; // Novo
import org.springframework.web.bind.annotation.RequestBody; // Novo
import pt.ipcb.ad.frontend_service.dto.VehicleDto;
import java.util.List;

@FeignClient(name = "vehicle-service")
public interface VehicleClient {

    @GetMapping("/vehicles/viewall")
    List<VehicleDto> getAllVehicles();

    @PostMapping("/vehicles")
    VehicleDto createVehicle(@RequestBody VehicleDto vehicle);
}