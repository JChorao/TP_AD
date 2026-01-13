package pt.ipcb.ad.rental_service.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.web.bind.annotation.*;
import pt.ipcb.ad.rental_service.client.GpsClient;
import pt.ipcb.ad.rental_service.client.VehicleClient;
import pt.ipcb.ad.rental_service.dto.VehicleDto; // <--- Import Adicionado
import pt.ipcb.ad.rental_service.model.Rental;
import pt.ipcb.ad.rental_service.repository.RentalRepository;

import java.time.Duration; // <--- Import Adicionado
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

        // --- LINHA PARA TESTAR 1 HORA (Descomente para simular) ---
        // rental.setStartTime(LocalDateTime.now().minusHours(1));
        // ----------------------------------------------------------

        try {
            // 1. Obter os dados do Veículo (para saber o preço/hora)
            VehicleDto vehicle = vehicleClient.getVehicleById(rental.getVehicleId());

            // Preço por hora (fallback para 10.0 se for nulo)
            Double pricePerHour = (vehicle != null && vehicle.getPricePerHour() != null)
                    ? vehicle.getPricePerHour()
                    : 10.0;

            // 2. Calcular a duração em Horas
            Duration duration = Duration.between(rental.getStartTime(), rental.getEndTime());
            long minutes = duration.toMinutes();

            // Converte minutos para horas (ex: 30 min = 0.5 horas)
            double hours = minutes / 60.0;

            // Proteção para testes muito rápidos (cobra pelo menos 1 minuto se for 0)
            if (hours == 0) hours = 0.1;

            // 3. Calcular Preço Base
            double basePrice = hours * pricePerHour;

            // 4. Adicionar Taxa de 7%
            double finalPrice = basePrice * 1.07;

            // Atualizar o aluguer
            rental.setTotalPrice(finalPrice);
            rental.setDistance(0.0); // Distância removida da lógica

        } catch (Exception e) {
            // Em caso de erro (ex: Vehicle Service em baixo), aplica taxa mínima
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
        rental.setTotalPrice(10.0); // Valor fixo de erro
        return rentalRepository.save(rental);
    }
}