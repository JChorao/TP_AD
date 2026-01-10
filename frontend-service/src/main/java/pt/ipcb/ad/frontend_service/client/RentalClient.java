package pt.ipcb.ad.frontend_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import pt.ipcb.ad.frontend_service.dto.RentalDto; // Vais criar este DTO abaixo
import java.util.List;

@FeignClient(name = "rental-service", url = "http://localhost:8083") // Ajusta porta se necessário (8083/8084)
public interface RentalClient {

    @PostMapping("/rentals/start")
    RentalDto startRental(@RequestBody RentalDto rental);

    @PostMapping("/rentals/stop/{id}")
    RentalDto stopRental(@PathVariable("id") Long id);

    @GetMapping("/rentals/viewall") // Para filtrar no controller depois
    List<RentalDto> getAllRentals();
}