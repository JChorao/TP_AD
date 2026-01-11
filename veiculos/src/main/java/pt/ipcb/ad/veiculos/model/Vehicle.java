package pt.ipcb.ad.veiculos.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String brand;         // Marca (ex: Tesla)
    private String model;         // Modelo (ex: Model 3)
    private String licensePlate;  // Matrícula (ex: AA-00-ZE)
    private boolean available;    // Disponível?

    // --- NOVOS CAMPOS ---
    private Double latitude;
    private Double longitude;
    private Double pricePerHour; // Preço para o cálculo do aluguer
}