package pt.ipcb.ad.payment_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ipcb.ad.payment_service.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // Métodos extra podem ser adicionados aqui
}