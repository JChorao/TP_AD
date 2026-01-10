package pt.ipcb.ad.gps_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pt.ipcb.ad.gps_service.repository.LocationRepository;

@Controller
@RequestMapping("/gps/view")
public class GpsViewController {

    @Autowired
    private LocationRepository repository;

    @GetMapping("/map")
    public String showMap(Model model) {
        // Enviamos a lista de localizações para o Thymeleaf
        model.addAttribute("locations", repository.findAll());
        return "gps_map"; // Nome do ficheiro HTML em src/main/resources/templates
    }
}