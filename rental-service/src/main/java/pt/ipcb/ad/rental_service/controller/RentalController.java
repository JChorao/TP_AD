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
        // --- Validações de User e Vehicle ---
        try {
            UserDto user = userClient.getUserById(rental.getUserId());
            if (user == null) throw new RuntimeException("Utilizador não encontrado.");

            VehicleDto vehicle = vehicleClient.getVehicleById(rental.getVehicleId());
            if (vehicle == null) throw new RuntimeException("Veículo não encontrado.");

            // NOVO: Verifica se o veículo está marcado como disponível no serviço de veículos
            if (!vehicle.isAvailable()) {
                throw new RuntimeException("Este veículo não está disponível para aluguer.");
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro validação: " + e.getMessage());
        }

        // Validação extra: Verifica se já existe algum aluguer ativo na tabela de alugueres
        List<Rental> activeRentals = repository.findByVehicleIdAndActiveTrue(rental.getVehicleId());
        if (!activeRentals.isEmpty()) {
            throw new RuntimeException("Este carro já está alugado (registo duplicado)!");
        }

        // Se o utilizador não enviou data de início, usamos AGORA.
        if (rental.getStartTime() == null) {
            rental.setStartTime(LocalDateTime.now());
        }

        rental.setActive(true);
        rental.setPricePerHour(10.0); // Podes melhorar isto buscando o preço ao VehicleDto

        // --- ATUALIZAÇÃO DE ESTADO ---
        // Marca o veículo como INDISPONÍVEL (false) no serviço de veículos
        try {
            vehicleClient.updateAvailability(rental.getVehicleId(), false);
        } catch (Exception e) {
            System.out.println("Aviso: Não foi possível atualizar o estado do veículo: " + e.getMessage());
        }

        return repository.save(rental);
    }

    @PostMapping("/stop/{id}")
    public Rental stopRental(@PathVariable Long id) {
        Rental rental = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aluguer não encontrado"));

        if (!rental.isActive()) {
            throw new RuntimeException("Este aluguer já foi terminado.");
        }

        // --- LÓGICA DE FIM E PREÇO ---

        if (rental.getEndTime() == null) {
            rental.setEndTime(LocalDateTime.now());
        }

        rental.setActive(false);

        // Calcular duração
        Duration duration = Duration.between(rental.getStartTime(), rental.getEndTime());
        long minutes = duration.toMinutes();

        if (minutes < 0) minutes = 0;

        double hours = (minutes == 0) ? 0.1 : (minutes / 60.0);
        double finalPrice = hours * rental.getPricePerHour();

        rental.setTotalPrice(finalPrice); // Guarda o preço final

        System.out.println("Aluguer terminado. Duração: " + hours + "h. Preço: " + finalPrice);

        // --- LIBERTAR VEÍCULO ---
        // Marca o veículo como DISPONÍVEL (true) no serviço de veículos
        try {
            vehicleClient.updateAvailability(rental.getVehicleId(), true);
        } catch (Exception e) {
            System.out.println("Aviso: Não foi possível libertar o veículo: " + e.getMessage());
        }

        // Enviar para Payment
        try {
            PaymentDto paymentReq = new PaymentDto();
            paymentReq.setRentalId(rental.getId());
            paymentReq.setAmount(finalPrice);
            paymentClient.processPayment(paymentReq);
            System.out.println(">> SUCESSO: Pagamento enviado via Feign.");
        } catch (Exception e) {
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