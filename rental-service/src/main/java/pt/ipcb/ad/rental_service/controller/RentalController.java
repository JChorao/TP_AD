package pt.ipcb.ad.rental_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import pt.ipcb.ad.rental_service.dto.PaymentDto; // <--- NOVO IMPORT
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
    private RestTemplate restTemplate;

    // 1. Listar todos os alugueres
    @GetMapping("/viewall")
    public List<Rental> getAllRentals() {
        return repository.findAll();
    }

    // 2. Criar um novo aluguer (START)
    @PostMapping("/start")
    public Rental startRental(@RequestBody Rental rental) {
        // Validação: O carro já está ocupado?
        List<Rental> activeRentals = repository.findByVehicleIdAndActiveTrue(rental.getVehicleId());

        if (!activeRentals.isEmpty()) {
            throw new RuntimeException("Este carro já está alugado!");
        }

        rental.setStartTime(LocalDateTime.now());
        rental.setActive(true);
        rental.setPricePerHour(10.0); // Valor fixo por agora

        return repository.save(rental);
    }

    // 3. Terminar um aluguer (STOP) -> Calcula Preço -> Envia para Payment-Service
    @PostMapping("/stop/{id}")
    public Rental stopRental(@PathVariable Long id) {
        Rental rental = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aluguer não encontrado"));

        if (!rental.isActive()) {
            throw new RuntimeException("Este aluguer já foi terminado.");
        }

        rental.setEndTime(LocalDateTime.now());
        rental.setActive(false);

        // --- CÁLCULO DO PREÇO ---
        Duration duration = Duration.between(rental.getStartTime(), rental.getEndTime());
        long minutes = duration.toMinutes();

        // Lógica de preço (Mínimo de 0.1 horas para testes rápidos)
        double hours = (minutes == 0) ? 0.1 : (minutes / 60.0);
        double finalPrice = hours * rental.getPricePerHour();

        // Se tiveres campo de preço na BD, descomenta:
        // rental.setFinalPrice(finalPrice);

        System.out.println("Aluguer terminado. Preço calculado: " + finalPrice);

        // --- NOVO: ENVIAR PARA O PAYMENT-SERVICE (Porta 8083) ---
        try {
            // 1. Criar o objeto de transferência
            PaymentDto paymentReq = new PaymentDto();
            paymentReq.setRentalId(rental.getId());
            paymentReq.setAmount(finalPrice);

            // 2. Definir o URL do serviço de pagamentos
            String paymentUrl = "http://localhost:8083/payments";

            // 3. Enviar o pedido POST
            restTemplate.postForObject(paymentUrl, paymentReq, String.class);

            System.out.println(">> SUCESSO: Pagamento enviado para o serviço de pagamentos.");

        } catch (Exception e) {
            System.out.println(">> ERRO: Falha ao contactar Payment-Service: " + e.getMessage());
            // Aqui podias marcar uma flag "paymentPending" na base de dados
        }
        // --------------------------------------------------------

        return repository.save(rental);
    }

    // 4. Eliminar um registo de aluguer
    @DeleteMapping("/{id}")
    public String deleteRental(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Aluguer não encontrado!");
        }
        repository.deleteById(id);
        return "Aluguer " + id + " eliminado com sucesso.";
    }

    // 5. Obter detalhes completos
    @GetMapping("/details/{id}")
    public String getRentalDetails(@PathVariable Long id) {
        Rental rental = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aluguer não encontrado"));

        // Buscar User
        String userUrl = "http://localhost:8081/users/" + rental.getUserId();
        UserDto user = null;
        try {
            user = restTemplate.getForObject(userUrl, UserDto.class);
        } catch (Exception e) {
            System.out.println("Erro user: " + e.getMessage());
        }

        // Buscar Vehicle
        String vehicleUrl = "http://localhost:8082/vehicles/" + rental.getVehicleId();
        VehicleDto vehicle = null;
        try {
            vehicle = restTemplate.getForObject(vehicleUrl, VehicleDto.class);
        } catch (Exception e) {
            System.out.println("Erro vehicle: " + e.getMessage());
        }

        String nomeCondutor = (user != null) ? user.getName() : "ID User: " + rental.getUserId();
        String modeloCarro = (vehicle != null) ? vehicle.getBrand() + " " + vehicle.getModel() : "ID Veículo: " + rental.getVehicleId();

        return "O utilizador " + nomeCondutor + " está a conduzir um " + modeloCarro;
    }
}