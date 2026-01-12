package pt.ipcb.ad.rental_service.dto;

import lombok.Data;

@Data
public class VehicleDto {
    private Long id;
    private String brand;
    private String model;
    private String licensePlate;
    private boolean available;
    private Double pricePerHour;

    private Double latitude;
    private Double longitude;

    public VehicleDto() {
    }

    public boolean isAvailable() {
        return available;
    }
}