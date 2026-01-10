package pt.ipcb.ad.rental_service.dto;

public class PaymentDto {
    private Long rentalId;
    private Double amount;

    // Construtores
    public PaymentDto() {}

    public PaymentDto(Long rentalId, Double amount) {
        this.rentalId = rentalId;
        this.amount = amount;
    }

    // Getters e Setters
    public Long getRentalId() { return rentalId; }
    public void setRentalId(Long rentalId) { this.rentalId = rentalId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}