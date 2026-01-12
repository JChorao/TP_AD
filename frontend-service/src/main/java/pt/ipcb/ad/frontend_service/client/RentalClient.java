package pt.ipcb.ad.frontend_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import pt.ipcb.ad.frontend_service.dto.RentalDto;
import java.util.List;

@FeignClient(name = "rental-service", url = "http://localhost:8083")
public interface RentalClient {

    @PostMapping("/rentals/start")
    RentalDto startRental(@RequestBody RentalDto rental);

    @PostMapping("/rentals/stop/{id}")
    RentalDto stopRental(@PathVariable("id") Long id);

    // CORREÇÃO: O endpoint no backend (RentalController) é /viewall e a classe tem /rentals
    // Logo, o URL completo é /rentals/viewall
    @GetMapping("/rentals/viewall")
    List<RentalDto> getAllRentals();
}