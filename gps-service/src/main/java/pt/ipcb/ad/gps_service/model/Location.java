package pt.ipcb.ad.gps_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double latitude;
    private double longitude;

    // Pode estar associada a um veículo ou a um utilizador
    private Long referenceId; // Ex: ID do Veículo ou ID do User

    private String type;      // "VEHICLE" ou "USER"
}