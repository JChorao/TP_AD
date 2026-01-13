package pt.ipcb.ad.rental_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data // Garante que tens o @Data do Lombok para gerar os setters automaticamente
public class Rental {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long vehicleId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double distance; // CAMPO QUE ESTAVA EM FALTA
    private double price;    // CAMPO QUE ESTAVA EM FALTA
    private boolean active;
}