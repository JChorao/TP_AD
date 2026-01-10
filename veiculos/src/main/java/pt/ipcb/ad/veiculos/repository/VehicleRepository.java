package pt.ipcb.ad.veiculos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ipcb.ad.veiculos.model.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    // Aqui podes adicionar métodos extra, ex: findByBrand(String brand);
}