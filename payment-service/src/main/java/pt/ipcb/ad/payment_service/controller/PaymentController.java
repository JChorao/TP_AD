package pt.ipcb.ad.payment_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pt.ipcb.ad.payment_service.model.Payment;
import pt.ipcb.ad.payment_service.repository.PaymentRepository;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentRepository repository;

    // Listar todos os pagamentos (Para confirmares se entrou)
    @GetMapping
    public List<Payment> getAllPayments() {
        return repository.findAll();
    }

    // Receber Pagamento (O Rental-Service vai chamar este método)
    @PostMapping
    public Payment processPayment(@RequestBody Payment payment) {
        System.out.println("Recebido pedido de pagamento para Aluguer ID: " + payment.getRentalId());

        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus("SUCESSO"); // Simulamos sucesso imediato

        return repository.save(payment);
    }
}