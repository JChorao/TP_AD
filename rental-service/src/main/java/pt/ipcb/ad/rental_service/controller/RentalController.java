package pt.ipcb.ad.rental_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pt.ipcb.ad.rental_service.client.PaymentClient;
import pt.ipcb.ad.rental_service.client.UserClient;
import pt.ipcb.ad.rental_service.client.VehicleClient;
import pt.ipcb.ad.rental_service.dto.PaymentDto;
import pt.ipcb.ad.rental_service.dto.UserDto;
import pt.ipcb.ad.rental_service.dto.VehicleDto;
import pt.ipcb.ad.rental_service.model.Rental;
import pt.ipcb.ad.rental_service.repository.RentalRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/rentals")
public class RentalController {

    @Autowired
    private RentalRepository repository;

    @Autowired
    private UserClient userClient;

    @Autowired
    private VehicleClient vehicleClient;

    @Autowired
    private PaymentClient paymentClient;

    @GetMapping("/viewall")
    public List<Rental> getAllRentals() {
        return repository.findAll();
    }

    @PostMapping("/start")
    public Rental startRental(@RequestBody Rental rental) {
        // --- 1. VALIDAÇÃO (NOVO): Verificar se o Utilizador existe ---
        try {
            UserDto user = userClient.getUserById(rental.getUserId());
            if (user == null) {
                throw new RuntimeException("Utilizador não encontrado no sistema.");
            }
        } catch (Exception e) {
            // Se o serviço de utilizadores estiver em baixo ou der 404
            throw new RuntimeException("Erro ao validar utilizador: " + e.getMessage());
        }

        // --- 2. VALIDAÇÃO (NOVO): Verificar se o Veículo existe ---
        try {
            VehicleDto vehicle = vehicleClient.getVehicleById(rental.getVehicleId());
            if (vehicle == null) {
                throw new RuntimeException("Veículo não encontrado no sistema.");
            }
            // Opcional: Se o VehicleDto tiver campo "available", podes verificar aqui também
            // if (!vehicle.isAvailable()) throw new RuntimeException("Veículo indisponível.");

        } catch (Exception e) {
            throw new RuntimeException("Erro ao validar veículo: " + e.getMessage());
        }
        // -------------------------------------------------------------

        // Validação Local: O carro já está alugado na tabela de rentals?
        List<Rental> activeRentals = repository.findByVehicleIdAndActiveTrue(rental.getVehicleId());
        if (!activeRentals.isEmpty()) {
            throw new RuntimeException("Este carro já está alugado (Aluguer em curso)!");
        }

        rental.setStartTime(LocalDateTime.now());
        rental.setActive(true);
        rental.setPricePerHour(10.0);

        return repository.save(rental);
    }

    @PostMapping("/stop/{id}")
    public Rental stopRental(@PathVariable Long id) {
        Rental rental = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aluguer não encontrado"));

        if (!rental.isActive()) {
            throw new RuntimeException("Este aluguer já foi terminado.");
        }

        rental.setEndTime(LocalDateTime.now());
        rental.setActive(false);

        // Cálculo do preço
        Duration duration = Duration.between(rental.getStartTime(), rental.getEndTime());
        long minutes = duration.toMinutes();
        double hours = (minutes == 0) ? 0.1 : (minutes / 60.0);
        double finalPrice = hours * rental.getPricePerHour();

        System.out.println("Aluguer terminado. Preço calculado: " + finalPrice);

        // Chamada ao Payment Service
        try {
            PaymentDto paymentReq = new PaymentDto();
            paymentReq.setRentalId(rental.getId());
            paymentReq.setAmount(finalPrice);

            paymentClient.processPayment(paymentReq);

            System.out.println(">> SUCESSO: Pagamento enviado via Feign.");

        } catch (Exception e) {
            // Nota: Num cenário real, deverias salvar na BD que o pagamento FALHOU (ex: status="PENDING")
            System.out.println(">> ERRO: Falha no pagamento: " + e.getMessage());
        }

        return repository.save(rental);
    }

    @DeleteMapping("/{id}")
    public String deleteRental(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Aluguer não encontrado!");
        }
        repository.deleteById(id);
        return "Aluguer " + id + " eliminado com sucesso.";
    }

    @GetMapping("/details/{id}")
    public String getRentalDetails(@PathVariable Long id) {
        Rental rental = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aluguer não encontrado"));

        String nomeCondutor = "Desconhecido";
        String modeloCarro = "Desconhecido";

        try {
            UserDto user = userClient.getUserById(rental.getUserId());
            if (user != null) nomeCondutor = user.getName();
        } catch (Exception e) {
            System.out.println("Erro ao buscar User: " + e.getMessage());
        }

        try {
            VehicleDto vehicle = vehicleClient.getVehicleById(rental.getVehicleId());
            if (vehicle != null) modeloCarro = vehicle.getBrand() + " " + vehicle.getModel();
        } catch (Exception e) {
            System.out.println("Erro ao buscar Vehicle: " + e.getMessage());
        }

        return "O utilizador " + nomeCondutor + " está a conduzir um " + modeloCarro;
    }
}