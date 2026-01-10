package pt.ipcb.ad.frontend_service.dto;

public class VehicleDto {
    private Long id;
    private String brand;
    private String model;
    private String licensePlate;
    private boolean available;

    // Construtor vazio necessário
    public VehicleDto() {}

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}