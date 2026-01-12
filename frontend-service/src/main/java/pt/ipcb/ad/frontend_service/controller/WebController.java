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

    // --- LISTA DE CARROS ---
    @GetMapping("/cars")
    public String listCars(Model model, HttpSession session) {
        UserDto user = (UserDto) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        try {
            List<VehicleDto> cars = vehicleClient.getAllVehicles();
            model.addAttribute("listaCarros", cars);
        } catch (Exception e) {
            model.addAttribute("erro", "Serviço de veículos indisponível.");
        }

        model.addAttribute("user", user);
        model.addAttribute("isLoggedIn", true);
        model.addAttribute("currentURI", "/cars");

        return "cars";
    }

    // --- ALUGAR CARRO ---
    @PostMapping("/rent-car/{vehicleId}")
    public String rentCar(@PathVariable Long vehicleId, HttpSession session) {
        UserDto user = (UserDto) session.getAttribute("user");

        if (user == null) return "redirect:/login";

        if (!user.getRoles().contains("CONDUTOR")) {
            return "redirect:/cars?erro=Apenas condutores podem alugar!";
        }

        try {
            RentalDto rental = new RentalDto();
            rental.setUserId(user.getId());
            rental.setVehicleId(vehicleId);

            rentalClient.startRental(rental);

            return "redirect:/my-rentals";
        } catch (Exception e) {
            return "redirect:/cars?erro=Falha ao iniciar aluguer.";
        }
    }

    // --- TERMINAR ALUGUER (NOVO MÉTODO ADICIONADO) ---
    @GetMapping("/stop-rental/{id}")
    public String stopRental(@PathVariable Long id, HttpSession session) {
        UserDto user = (UserDto) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        try {
            // Chama o backend para calcular preço e fechar a viagem
            rentalClient.stopRental(id);
        } catch (Exception e) {
            System.out.println("Erro ao terminar aluguer: " + e.getMessage());
            // Poderias redirecionar com ?erro=... se quisesses mostrar no HTML
        }

        // Redireciona de volta para a lista para veres o preço e o estado atualizado
        return "redirect:/my-rentals";
    }

    // --- MINHAS VIAGENS (User Logado) ---
    @GetMapping("/my-rentals")
    public String myRentals(HttpSession session, Model model) {
        UserDto user = (UserDto) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        return viewUserRentals(user.getId(), session, model);
    }

    // --- LISTAR UTILIZADORES (ADMIN) ---
    @GetMapping("/users")
    public String listUsers(HttpSession session, Model model) {
        UserDto user = (UserDto) session.getAttribute("user");

        if (user == null) return "redirect:/login";
        if (!user.getRoles().contains("ADMIN")) {
            return "redirect:/cars";
        }

        try {
            List<UserDto> users = userClient.getAllUsers();
            model.addAttribute("listaUsers", users);
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao carregar utilizadores: " + e.getMessage());
        }

        model.addAttribute("user", user);
        model.addAttribute("isLoggedIn", true);
        model.addAttribute("currentURI", "/users");

        return "users";
    }

    // --- VER VIAGENS DE UM USER ESPECÍFICO ---
    @GetMapping("/users/{id}/rentals")
    public String viewUserRentals(@PathVariable Long id, HttpSession session, Model model) {
        UserDto sessionUser = (UserDto) session.getAttribute("user");

        if (sessionUser == null) return "redirect:/login";

        boolean isAdmin = sessionUser.getRoles().contains("ADMIN");
        boolean isOwner = sessionUser.getId().equals(id);

        if (!isOwner && !isAdmin) {
            return "redirect:/cars";
        }

        try {
            // Nota: Idealmente o backend teria um filtro por userId, mas assim funciona
            List<RentalDto> allRentals = rentalClient.getAllRentals();

            List<RentalDto> userRentals = allRentals.stream()
                    .filter(r -> r.getUserId() != null && r.getUserId().equals(id))
                    .collect(Collectors.toList());

            model.addAttribute("viagens", userRentals);

        } catch (Exception e) {
            model.addAttribute("error", "Erro ao carregar viagens: " + e.getMessage());
        }

        model.addAttribute("user", sessionUser);
        model.addAttribute("isLoggedIn", true);
        model.addAttribute("currentURI", "/my-rentals");

        return "my-rentals";
    }
}