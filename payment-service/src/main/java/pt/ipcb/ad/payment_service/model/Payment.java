package pt.ipcb.ad.payment_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long rentalId;
    private Long userId;      // NOVO: Quem pagou
    private Long vehicleId;   // NOVO: Que carro usou

    private Double amount;
    private LocalDateTime paymentDate;
    private String status;
}