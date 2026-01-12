package pt.ipcb.ad.frontend_service.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import pt.ipcb.ad.frontend_service.client.RentalClient;
import pt.ipcb.ad.frontend_service.client.UserClient;
import pt.ipcb.ad.frontend_service.client.VehicleClient;
import pt.ipcb.ad.frontend_service.dto.RentalDto;
import pt.ipcb.ad.frontend_service.dto.UserDto;
import pt.ipcb.ad.frontend_service.dto.VehicleDto;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class WebController {

    private final VehicleClient vehicleClient;
    private final RentalClient rentalClient;
    private final UserClient userClient;

    public WebController(VehicleClient vehicleClient, RentalClient rentalClient, UserClient userClient) {
        this.vehicleClient = vehicleClient;
        this.rentalClient = rentalClient;
        this.userClient = userClient;
    }

    @GetMapping("/")
    public String rootRedirect() {
        return "redirect:/cars";
    }

    // --- LISTA DE CARROS (ACESSO PÚBLICO) ---
    @GetMapping("/cars")
    public String listCars(Model model, HttpSession session) {
        UserDto user = (UserDto) session.getAttribute("user");

        try {
            // Obtém todos os veículos do microserviço de veículos
            List<VehicleDto> cars = vehicleClient.getAllVehicles();

            model.addAttribute("listaCarros", cars);
            model.addAttribute("cars", cars);
        } catch (Exception e) {
            System.out.println("Erro ao obter carros: " + e.getMessage());
            model.addAttribute("erro", "Serviço de veículos indisponível.");
        }

        model.addAttribute("user", user);
        model.addAttribute("isLoggedIn", user != null);
        model.addAttribute("currentURI", "/cars");

        return "cars";
    }

    // --- ALUGAR CARRO (PROTEGIDO) ---
    @PostMapping("/rent-car/{vehicleId}")
    public String rentCar(@PathVariable Long vehicleId, HttpSession session) {
        UserDto user = (UserDto) session.getAttribute("user");

        // O aluguer continua a exigir login
        if (user == null) return "redirect:/login";

        if (!user.getRoles().contains("CONDUTOR")) {
            return "redirect:/cars?erro=Apenas condutores podem alugar!";
        }

        try {
            RentalDto rental = new RentalDto();
            rental.setUserId(user.getId());
            rental.setVehicleId(vehicleId);

            // Chama o microserviço de alugueres
            rentalClient.startRental(rental);

            return "redirect:/my-rentals";
        } catch (Exception e) {
            // --- ALTERAÇÃO IMPORTANTE: LOG DE ERRO ---
            // Isto vai imprimir o erro completo na consola do Docker
            e.printStackTrace();
            // Isto mostra o motivo do erro na barra de endereço do browser
            return "redirect:/cars?erro=Falha ao iniciar aluguer: " + e.getMessage();
        }
    }

    @GetMapping("/stop-rental/{id}")
    public String stopRental(@PathVariable Long id, HttpSession session) {
        UserDto user = (UserDto) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        try {
            rentalClient.stopRental(id);
        } catch (Exception e) {
            e.printStackTrace(); // Melhor logging
            System.out.println("Erro ao terminar aluguer: " + e.getMessage());
        }

        return "redirect:/my-rentals";
    }

    @GetMapping("/my-rentals")
    public String myRentals(HttpSession session, Model model) {
        UserDto user = (UserDto) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        return viewUserRentals(user.getId(), session, model);
    }

    @GetMapping("/users")
    public String listUsers(HttpSession session, Model model) {
        UserDto user = (UserDto) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        if (!user.getRoles().contains("ADMIN")) return "redirect:/cars";

        try {
            List<UserDto> users = userClient.getAllUsers();
            model.addAttribute("listaUsers", users);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("erro", "Erro ao carregar utilizadores: " + e.getMessage());
        }

        model.addAttribute("user", user);
        model.addAttribute("isLoggedIn", true);
        model.addAttribute("currentURI", "/users");

        return "users";
    }

    @GetMapping("/users/{id}/rentals")
    public String viewUserRentals(@PathVariable Long id, HttpSession session, Model model) {
        UserDto sessionUser = (UserDto) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        boolean isAdmin = sessionUser.getRoles().contains("ADMIN");
        boolean isOwner = sessionUser.getId().equals(id);

        if (!isOwner && !isAdmin) return "redirect:/cars";

        try {
            List<RentalDto> allRentals = rentalClient.getAllRentals();
            List<RentalDto> userRentals = allRentals.stream()
                    .filter(r -> r.getUserId() != null && r.getUserId().equals(id))
                    .collect(Collectors.toList());
            model.addAttribute("viagens", userRentals);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erro ao carregar viagens: " + e.getMessage());
        }

        model.addAttribute("user", sessionUser);
        model.addAttribute("isLoggedIn", true);
        model.addAttribute("currentURI", "/my-rentals");

        return "my-rentals";
    }
}