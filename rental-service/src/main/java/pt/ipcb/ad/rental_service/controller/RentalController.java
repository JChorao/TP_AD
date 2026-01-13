package pt.ipcb.ad.rental_service.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.web.bind.annotation.*;
import pt.ipcb.ad.rental_service.client.GpsClient;
import pt.ipcb.ad.rental_service.client.PaymentClient;
import pt.ipcb.ad.rental_service.client.VehicleClient;
import pt.ipcb.ad.rental_service.dto.PaymentDto;
import pt.ipcb.ad.rental_service.dto.VehicleDto;
import pt.ipcb.ad.rental_service.model.Rental;
import pt.ipcb.ad.rental_service.repository.RentalRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/rentals")
public class RentalController {

    private final RentalRepository rentalRepository;
    private final GpsClient gpsClient;
    private final VehicleClient vehicleClient;
    private final PaymentClient paymentClient;

    public RentalController(RentalRepository rentalRepository,
                            GpsClient gpsClient,
                            VehicleClient vehicle,
                            PaymentClient paymentClient) {
        this.rentalRepository = rentalRepository;
        this.gpsClient = gpsClient;
        this.vehicleClient = vehicle;
        this.paymentClient = paymentClient;
    }

    @PostMapping("/start")
    public Rental startRental(@RequestBody Rental rental) {
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

        // --- LINHA PARA TESTES (Apagar em produção) ---
        // rental.setStartTime(LocalDateTime.now().minusHours(1));
        // ----------------------------------------------

        try {
            VehicleDto vehicle = vehicleClient.getVehicleById(rental.getVehicleId());
            Double pricePerHour = (vehicle != null && vehicle.getPricePerHour() != null)
                    ? vehicle.getPricePerHour()
                    : 10.0;

            Duration duration = Duration.between(rental.getStartTime(), rental.getEndTime());
            long minutes = duration.toMinutes();
            double hours = minutes / 60.0;
            if (hours == 0) hours = 0.0166;

            double basePrice = hours * pricePerHour;
            double finalPrice = basePrice * 1.07; // Taxa 7%

            rental.setTotalPrice(finalPrice);
            rental.setDistance(0.0);

            // --- PROCESSAR PAGAMENTO ---
            try {
                PaymentDto payment = new PaymentDto();
                payment.setRentalId(rental.getId());
                payment.setUserId(rental.getUserId());       // Envia User
                payment.setVehicleId(rental.getVehicleId()); // Envia Carro
                payment.setAmount(finalPrice);

                paymentClient.processPayment(payment);
                System.out.println("Pagamento enviado: " + finalPrice);
            } catch (Exception e) {
                System.err.println("Erro no pagamento: " + e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            rental.setTotalPrice(5.0);
            rental.setDistance(0.0);
        }

        return rentalRepository.save(rental);
    }

    @GetMapping("/viewall")
    public List<Rental> getAllRentals() {
        return rentalRepository.findAll();
    }

    public Rental stopRentalFallback(Long id, Throwable t) {
        Rental rental = rentalRepository.findById(id).orElseThrow();
        rental.setEndTime(LocalDateTime.now());
        rental.setActive(false);
        rental.setDistance(0.0);
        rental.setTotalPrice(10.0);
        return rentalRepository.save(rental);
    }
}