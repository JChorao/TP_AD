package pt.ipcb.ad.frontend_service.dto;

import lombok.Data;

@Data
public class VehicleDto {
    private Long id;
    private String brand;
    private String model;
    private String licensePlate;
    private boolean available;

    // --- NOVOS CAMPOS ---
    private Double pricePerHour;
    private Double latitude;
    private Double longitude;
}