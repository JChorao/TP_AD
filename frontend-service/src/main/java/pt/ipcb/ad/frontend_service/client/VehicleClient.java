package pt.ipcb.ad.frontend_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import pt.ipcb.ad.frontend_service.dto.VehicleDto; // Precisas criar este DTO simples
import java.util.List;

// "vehicle-service" é o nome que está no Eureka
@FeignClient(name = "vehicle-service")
public interface VehicleClient {

    // Este endpoint tem de existir no VehicleController
    @GetMapping("/vehicles/viewall")
    List<VehicleDto> getAllVehicles();
}