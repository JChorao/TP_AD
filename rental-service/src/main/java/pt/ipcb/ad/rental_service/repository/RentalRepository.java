package pt.ipcb.ad.rental_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ipcb.ad.rental_service.model.Rental;
import java.util.List;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    // Encontrar alugueres ativos de um utilizador
    List<Rental> findByUserId(Long userId);

    // Verificar se um carro já está alugado (para não alugar 2x)
    List<Rental> findByVehicleIdAndActiveTrue(Long vehicleId);
}