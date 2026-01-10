package pt.ipcb.ad.payment_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data; // Lombok gera Getters, Setters e Construtores

import java.time.LocalDateTime;

@Entity
@Data // O Lombok faz a magia aqui. Se não usares Lombok, cria os Getters/Setters.
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long rentalId;      // ID do aluguer vindo do Rental-Service
    private Double amount;      // Valor a pagar
    private LocalDateTime paymentDate;
    private String status;      // "SUCESSO", "PENDENTE"
}