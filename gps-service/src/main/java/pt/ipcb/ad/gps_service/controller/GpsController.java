package pt.ipcb.ad.gps_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pt.ipcb.ad.gps_service.model.Location;
import pt.ipcb.ad.gps_service.repository.LocationRepository;

import java.util.List;

@RestController
@RequestMapping("/gps")
public class GpsController {

    @Autowired
    private LocationRepository repository;

    // Guardar localização de um carro/user
    @PostMapping
    public Location updateLocation(@RequestBody Location location) {
        return repository.save(location);
    }

    // Listar todas
    @GetMapping
    public List<Location> getAll() {
        return repository.findAll();
    }

    // Endpoint de Cálculo de Distância (Simulação)
    // Exemplo: GET /gps/distance?lat1=40.0&lon1=-7.0&lat2=41.0&lon2=-8.0
    @GetMapping("/distance")
    public double calculateDistance(@RequestParam double lat1, @RequestParam double lon1,
                                    @RequestParam double lat2, @RequestParam double lon2) {
        // Fórmula simples para testes (distância euclidiana em graus, não kms reais)
        // Num cenário real usarias a fórmula de Haversine
        double cateto1 = lat2 - lat1;
        double cateto2 = lon2 - lon1;
        return Math.sqrt(cateto1*cateto1 + cateto2*cateto2);
    }
}