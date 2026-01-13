package pt.ipcb.ad.rental_service.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.web.bind.annotation.*;
import pt.ipcb.ad.rental_service.client.GpsClient;
import pt.ipcb.ad.rental_service.model.Rental;
import pt.ipcb.ad.rental_service.repository.RentalRepository;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/rentals")
public class RentalController {

    private final RentalRepository rentalRepository;
    private final GpsClient gpsClient;

    public RentalController(RentalRepository rentalRepository, GpsClient gpsClient) {
        this.rentalRepository = rentalRepository;
        this.gpsClient = gpsClient;
    }

    @PostMapping("/start")
    public Rental startRental(@RequestBody Rental rental) {
        rental.setStartTime(LocalDateTime.now());
        rental.setActive(true);
        return rentalRepository.save(rental);
    }

    @PostMapping("/stop/{id}")
    @CircuitBreaker(name = "gpsService", fallbackMethod = "stopRentalFallback")
    public Rental stopRental(@PathVariable Long id) {
        Rental rental = rentalRepository.findById(id).orElseThrow();
        rental.setEndTime(LocalDateTime.now());
        rental.setActive(false);

        // Simulação de chamada ao GPS
        double distance = gpsClient.getDistance(39.82, -7.49, 39.85, -7.50);
        rental.setDistance(distance);
        rental.setPrice(distance * 0.50); // Exemplo: 0.50€ por km

        return rentalRepository.save(rental);
    }

    // Método que corre se o GPS Service estiver em baixo
    public Rental stopRentalFallback(Long id, Throwable t) {
        Rental rental = rentalRepository.findById(id).orElseThrow();
        rental.setEndTime(LocalDateTime.now());
        rental.setActive(false);
        rental.setDistance(0.0);
        rental.setPrice(10.0); // Taxa fixa se o GPS falhar
        return rentalRepository.save(rental);
    }
}