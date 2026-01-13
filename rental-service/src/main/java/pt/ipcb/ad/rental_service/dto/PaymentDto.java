package pt.ipcb.ad.rental_service.dto;

import lombok.Data;

@Data
public class PaymentDto {
    private Long rentalId;
    private Long userId;      // Adicionado
    private Long vehicleId;   // Adicionado
    private Double amount;

    public PaymentDto() {}

    public PaymentDto(Long rentalId, Long userId, Long vehicleId, Double amount) {
        this.rentalId = rentalId;
        this.userId = userId;
        this.vehicleId = vehicleId;
        this.amount = amount;
    }
}