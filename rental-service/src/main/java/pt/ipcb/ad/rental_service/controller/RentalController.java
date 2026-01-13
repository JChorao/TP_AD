package pt.ipcb.ad.rental_service.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.xml.bind.helpers.ValidationEventImpl;
import org.springframework.web.bind.annotation.*;
import pt.ipcb.ad.rental_service.client.GpsClient;
import pt.ipcb.ad.rental_service.client.VehicleClient;
import pt.ipcb.ad.rental_service.model.Rental;
import pt.ipcb.ad.rental_service.repository.RentalRepository;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/rentals")
public class RentalController {

    private final RentalRepository rentalRepository;
    private final GpsClient gpsClient;
    private final VehicleClient vehicleClient;

    public RentalController(RentalRepository rentalRepository, GpsClient gpsClient, VehicleClient vehicle) {
        this.rentalRepository = rentalRepository;
        this.gpsClient = gpsClient;
        this.vehicleClient = vehicle;
    }

    @PostMapping("/start")
    public Rental startRental(@RequestBody Rental rental) {
        // Garante que o estado inicial é ativo
        rental.setActive(true);
        if (rental.getStartTime() == null) {
            rental.setStartTime(LocalDateTime.now());
        }
        return rentalRepository.save(rental);
    }

    @PostMapping("/stop/{id}")
    @CircuitBreaker(name = "gpsService", fallbackMethod = "stopRentalFallback")
    public Rental stopRental(@PathVariable Long id) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aluguer não encontrado"));

        rental.setEndTime(LocalDateTime.now());
        rental.setActive(false);

        // Simulação de chamada ao GPS (valores fixos para exemplo)
        try {
            double distance = gpsClient.getDistance(39.82, -7.49, 39.85, -7.50);
            rental.setDistance(distance);
            rental.setTotalPrice(distance * 0.50);
        } catch (Exception e) {
            // Se o Feign falhar antes do Circuit Breaker atuar
            rental.setDistance(0.0);
            rental.setTotalPrice(10.0);
        }

        return rentalRepository.save(rental);
    }

    @GetMapping("/viewall")
    public List<Rental> getAllRentals() {
        return rentalRepository.findAll();
    }

    // Método que corre se o GPS Service estiver em baixo
    public Rental stopRentalFallback(Long id, Throwable t) {
        Rental rental = rentalRepository.findById(id).orElseThrow();
        rental.setEndTime(LocalDateTime.now());
        rental.setActive(false);
        rental.setDistance(0.0);
        rental.setTotalPrice(10.0); // Taxa fixa se o GPS falhar
        return rentalRepository.save(rental);
    }
}