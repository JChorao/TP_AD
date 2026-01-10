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

    private String brand;         // Marca (ex: Renault)
    private String model;         // Modelo (ex: Clio)
    private String licensePlate;  // Matrícula (ex: AA-00-BB)
    private boolean available;    // Disponível?
}