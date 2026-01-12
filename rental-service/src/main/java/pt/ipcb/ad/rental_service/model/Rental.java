package pt.ipcb.ad.rental_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Rental {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;       // Quem alugou
    private Long vehicleId;    // Qual carro

    private LocalDateTime startTime; // Início do aluguer
    private LocalDateTime endTime;   // Fim (pode ser null se ainda estiver a andar)

    private Double pricePerHour;     // Preço congelado no momento do aluguer
    private boolean active;          // Se o aluguer está a decorrer

    private Double totalPrice; // Guarda o preço final da viagem

    private Double startLat;
    private Double startLon;
    private Double endLat;
    private Double endLon;
    private Double distanceKms;
}