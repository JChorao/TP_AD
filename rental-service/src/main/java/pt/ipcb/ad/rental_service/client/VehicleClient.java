package pt.ipcb.ad.rental_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pt.ipcb.ad.rental_service.dto.VehicleDto;

@FeignClient(name = "vehicle-service", url = "http://localhost:8082")
public interface VehicleClient {

    @GetMapping("/vehicles/{id}")
    VehicleDto getVehicleById(@PathVariable("id") Long id);
}