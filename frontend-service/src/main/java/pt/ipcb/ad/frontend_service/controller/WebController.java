package pt.ipcb.ad.frontend_service.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import pt.ipcb.ad.frontend_service.client.RentalClient;
import pt.ipcb.ad.frontend_service.client.UserClient;
import pt.ipcb.ad.frontend_service.client.VehicleClient;
import pt.ipcb.ad.frontend_service.dto.RentalDto;
import pt.ipcb.ad.frontend_service.dto.UserDto;
import pt.ipcb.ad.frontend_service.dto.VehicleDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@Controller
public class WebController {

    @Autowired
    private VehicleClient vehicleClient;

    @Autowired
    private UserClient userClient;

    @Autowired
    private RentalClient rentalClient;

    // --- NOVO: Lógica Global para a Navbar ---
    @ModelAttribute
    public void addAttributes(HttpSession session, Model model, HttpServletRequest request) {
        // 1. Lógica do Login (Mantém-se)
        try {
            Object sessionUser = session.getAttribute("user");
            if (sessionUser != null && sessionUser instanceof UserDto) {
                model.addAttribute("user", (UserDto) sessionUser);
                model.addAttribute("isLoggedIn", true);
            } else {
                model.addAttribute("isLoggedIn", false);
            }
        } catch (Exception e) {
            model.addAttribute("isLoggedIn", false);
        }

        // 2. NOVA CORREÇÃO: Passar o URI atual para o HTML manualmente
        model.addAttribute("currentURI", request.getRequestURI());
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/cars";
    }

    // --- PÁGINA: LISTA DE CARROS ---
    @GetMapping("/cars")
    public String listCars(Model model) {
        try {
            List<VehicleDto> carros = vehicleClient.getAllVehicles();
            model.addAttribute("listaCarros", carros);
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao carregar veículos: " + e.getMessage());
        }
        return "cars";
    }

    // --- PÁGINA: LISTA DE UTILIZADORES ---
    @GetMapping("/users")
    public String listUsers(HttpSession session, Model model) {
        // Opcional: Proteger para só ADMIN ver (podes adicionar depois)
        try {
            List<UserDto> users = userClient.getAllUsers();
            model.addAttribute("listaUsers", users);
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao carregar utilizadores: " + e.getMessage());
        }
        return "users";
    }

    // --- AÇÃO: ALUGAR CARRO ---
    @GetMapping("/rent-car/{vehicleId}")
    public String rentCarAction(@PathVariable Long vehicleId,
                                HttpSession session,
                                Model model) {

        // 1. Verifica se está logado
        UserDto user = (UserDto) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login"; // Manda para o login se não estiver
        }

        try {
            RentalDto rental = new RentalDto();
            rental.setVehicleId(vehicleId);
            rental.setUserId(user.getId()); // USA O ID REAL DO USER LOGADO!

            rentalClient.startRental(rental);

            return "redirect:/my-rentals";

        } catch (Exception e) {
            return "redirect:/cars?erro=" + "Erro: " + e.getMessage();
        }
    }

    // --- PÁGINA: MINHAS VIAGENS ---
    @GetMapping("/my-rentals")
    public String myRentals(HttpSession session, Model model) {

        // 1. Segurança: Se não houver user, manda para login
        UserDto user = (UserDto) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            List<RentalDto> allRentals = rentalClient.getAllRentals();

            // 2. Filtra apenas as viagens do utilizador logado (user.getId())
            List<RentalDto> myRentals = allRentals.stream()
                    .filter(r -> r.getUserId().equals(user.getId())) // <--- CORRIGIDO AQUI
                    .toList();

            model.addAttribute("viagens", myRentals);

        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao carregar viagens: " + e.getMessage());
        }
        return "my-rentals";
    }

    // --- AÇÃO: TERMINAR VIAGEM ---
    @GetMapping("/stop-rental/{id}")
    public String stopRentalAction(@PathVariable Long id) {
        try {
            rentalClient.stopRental(id);
        } catch (Exception e) {
            System.out.println("Erro ao parar: " + e.getMessage());
        }
        return "redirect:/my-rentals";
    }
}