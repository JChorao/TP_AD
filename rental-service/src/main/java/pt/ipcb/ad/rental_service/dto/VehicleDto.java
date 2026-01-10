package pt.ipcb.ad.rental_service.dto;

import lombok.Getter;

@Getter
public class VehicleDto {
    private Long id;
    private String brand;
    private String model;
    private String licensePlate;

    public VehicleDto() {
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }
}