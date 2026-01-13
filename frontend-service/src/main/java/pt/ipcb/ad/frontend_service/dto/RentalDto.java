package pt.ipcb.ad.frontend_service.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class RentalDto {
    // Getters e Setters, construtor vazio
    private Long id;
    private Long userId;
    private Long vehicleId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean active;
    private Double pricePerHour;
    private Double totalPrice;

    public void setId(Long id) { this.id = id; }

    public void setUserId(Long userId) { this.userId = userId; }

    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }

    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public void setActive(boolean active) { this.active = active; }

    public void setPricePerHour(Double pricePerHour) { this.pricePerHour = pricePerHour; }

    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
}