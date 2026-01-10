package pt.ipcb.ad.frontend_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pt.ipcb.ad.frontend_service.client.VehicleClient;
import pt.ipcb.ad.frontend_service.dto.VehicleDto;

import java.util.List;

@Controller // Nota: É @Controller e NÃO @RestController porque retorna HTML
public class WebController {

    @Autowired
    private VehicleClient vehicleClient;

    // Página Inicial (Opcional, redireciona para a lista)
    @GetMapping("/")
    public String index() {
        return "redirect:/cars";
    }

    // Página de Listagem de Carros
    @GetMapping("/cars")
    public String listCars(Model model) {
        try {
            // 1. Vai buscar a lista ao Vehicle-Service via Feign
            List<VehicleDto> carros = vehicleClient.getAllVehicles();

            // 2. Adiciona a lista ao "Model" para o Thymeleaf usar
            model.addAttribute("listaCarros", carros);

        } catch (Exception e) {
            model.addAttribute("erro", "Não foi possível carregar os veículos: " + e.getMessage());
        }

        // 3. Retorna o nome do ficheiro HTML (sem .html)
        return "cars";
    }
}