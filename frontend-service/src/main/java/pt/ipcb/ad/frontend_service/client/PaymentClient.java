package pt.ipcb.ad.frontend_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import pt.ipcb.ad.frontend_service.dto.PaymentDto;
import java.util.List;

@FeignClient(name = "payment-service")
public interface PaymentClient {
    @GetMapping("/payments")
    List<PaymentDto> getAllPayments();
}