package pt.ipcb.ad.frontend_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PaymentDto {
    private Long id;
    private Long rentalId;
    private Long userId;
    private Long vehicleId;
    private Double amount;
    private LocalDateTime paymentDate;
    private String status;

    // Campos auxiliares para a tabela
    private String userName;
    private String vehicleInfo;
}