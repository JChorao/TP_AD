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
import pt.ipcb.ad.rental_service.client.GpsClient;

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

    @Autowired
    private GpsClient gpsClient;

    @GetMapping("/viewall")
    public List<Rental> getAllRentals() {
        return repository.findAll();
    }

    @PostMapping("/start")
    public Rental startRental(@RequestBody Rental rental) {
        VehicleDto vehicle;
        try {
            UserDto user = userClient.getUserById(rental.getUserId());
            if (user == null) throw new RuntimeException("Utilizador não encontrado.");

            vehicle = vehicleClient.getVehicleById(rental.getVehicleId());
            if (vehicle == null) throw new RuntimeException("Veículo não encontrado.");

            if (!vehicle.isAvailable()) {
                throw new RuntimeException("Este veículo não está disponível para aluguer.");
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro validação: " + e.getMessage());
        }

        List<Rental> activeRentals = repository.findByVehicleIdAndActiveTrue(rental.getVehicleId());
        if (!activeRentals.isEmpty()) {
            throw new RuntimeException("Este carro já está alugado!");
        }

        if (rental.getStartTime() == null) {
            rental.setStartTime(LocalDateTime.now());
        }

        rental.setActive(true);

        // --- LÓGICA DE GEOLOCALIZAÇÃO INICIAL ---
        // Guarda as coordenadas onde o veículo iniciou a viagem
        rental.setStartLat(vehicle.getLatitude());
        rental.setStartLon(vehicle.getLongitude());

        // --- LÓGICA DE PREÇO POR HORA ---
        if (vehicle.getPricePerHour() != null) {
            rental.setPricePerHour(vehicle.getPricePerHour());
        } else {
            rental.setPricePerHour(10.0);
        }

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

        if (rental.getEndTime() == null) {
            rental.setEndTime(LocalDateTime.now());
        }

        rental.setActive(false);

        // --- SIMULAÇÃO DE GEOLOCALIZAÇÃO FINAL ---
        // Simula que o carro se deslocou para uma nova posição (ex: Castelo Branco)
        // (Num cenário real, receberias estas coordenadas do GPS do carro ou da App)
        double currentLat = 39.8230;
        double currentLon = -7.4919;
        rental.setEndLat(currentLat);
        rental.setEndLon(currentLon);

        // --- CÁLCULO DE KM VIA MICROSERVIÇO (GPS-SERVICE) ---
        // Agora chamamos o serviço externo em vez de calcular aqui
        double kmsPercorridos = 0.0;
        try {
            kmsPercorridos = gpsClient.calculateDistance(
                    rental.getStartLat(),
                    rental.getStartLon(),
                    rental.getEndLat(),
                    rental.getEndLon()
            );
        } catch (Exception e) {
            System.out.println("Erro ao contactar GPS Service: " + e.getMessage());
            // Fallback: se o GPS falhar, assume 0 ou um valor por defeito, ou lança erro
            kmsPercorridos = 0.0;
        }

        // Arredondar kms para 2 casas decimais
        kmsPercorridos = Math.round(kmsPercorridos * 100.0) / 100.0;
        rental.setDistanceKms(kmsPercorridos);

        // --- CÁLCULO DE PREÇO TEMPO ---
        Duration duration = Duration.between(rental.getStartTime(), rental.getEndTime());
        long minutes = duration.toMinutes();
        if (minutes < 0) minutes = 0;
        double hours = (minutes < 6) ? 0.1 : (minutes / 60.0); // Mínimo 6 minutos cobrados como 0.1h
        double timePrice = hours * rental.getPricePerHour();

        // --- CÁLCULO DE PREÇO DISTÂNCIA ---
        double pricePerKm = 2.0; // Taxa fixa de exemplo
        double distancePrice = rental.getDistanceKms() * pricePerKm;

        // --- PREÇO TOTAL ---
        double finalPrice = timePrice + distancePrice;
        finalPrice = Math.round(finalPrice * 100.0) / 100.0;
        rental.setTotalPrice(finalPrice);

        // --- LIBERTAR VEÍCULO ---
        try {
            vehicleClient.updateAvailability(rental.getVehicleId(), true);
        } catch (Exception e) {
            System.out.println("Aviso: Não foi possível libertar o veículo: " + e.getMessage());
        }

        // --- PROCESSAR PAGAMENTO ---
        try {
            PaymentDto paymentReq = new PaymentDto();
            paymentReq.setRentalId(rental.getId());
            paymentReq.setAmount(finalPrice);
            paymentClient.processPayment(paymentReq);
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

        return "O utilizador " + nomeCondutor + " conduziu " + rental.getDistanceKms() + "km num " + modeloCarro;
    }

}