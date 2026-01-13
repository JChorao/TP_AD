package pt.ipcb.ad.frontend_service.dto;

import lombok.Data;

@Data
public class VehicleDto {
    private Long id;
    private String licensePlate;
    private String brand;
    private String model;
    private double pricePerHour; // Certifica-te que o nome é igual ao do backend
    private boolean available;
    private double latitude;
    private double longitude;
}