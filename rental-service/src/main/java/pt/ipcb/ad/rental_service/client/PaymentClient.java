package pt.ipcb.ad.rental_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import pt.ipcb.ad.rental_service.dto.PaymentDto;

@FeignClient(name = "payment-service", url = "http://localhost:8084")
public interface PaymentClient {

    @PostMapping("/payments")
    String processPayment(@RequestBody PaymentDto payment);
}