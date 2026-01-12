package pt.ipcb.ad.frontend_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import pt.ipcb.ad.frontend_service.dto.RentalDto;
import java.util.List;

@FeignClient(name = "rental-service")
public interface RentalClient {

    @PostMapping("/rentals/start")
    RentalDto startRental(@RequestBody RentalDto rental);

    @PostMapping("/rentals/stop/{id}")
    RentalDto stopRental(@PathVariable("id") Long id);

    @GetMapping("/rentals/viewall")
    List<RentalDto> getAllRentals();
}