package pt.ipcb.ad.gps_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.ipcb.ad.gps_service.model.Location;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    // Método útil para encontrar a localização de um veículo ou utilizador específico
    List<Location> findByReferenceIdAndType(Long referenceId, String type);
}