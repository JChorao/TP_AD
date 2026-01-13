package pt.ipcb.ad.frontend_service.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pt.ipcb.ad.frontend_service.client.RentalClient;
import pt.ipcb.ad.frontend_service.client.UserClient;
import pt.ipcb.ad.frontend_service.client.VehicleClient;
import pt.ipcb.ad.frontend_service.dto.RentalDto;
import pt.ipcb.ad.frontend_service.dto.UserDto;
import pt.ipcb.ad.frontend_service.dto.VehicleDto;

import java.time.LocalDateTime;
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

    @GetMapping("/cars")
    public String carsPage(Model model, HttpSession session) {
        UserDto user = (UserDto) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        model.addAttribute("isLoggedIn", true);

        try {
            List<VehicleDto> cars = vehicleClient.getAllVehicles();
            // Filtrar apenas carros disponíveis no frontend para segurança visual
            List<VehicleDto> availableCars = cars.stream()
                    .filter(VehicleDto::isAvailable)
                    .collect(Collectors.toList());
            model.addAttribute("cars", availableCars);
        } catch (Exception e) {
            model.addAttribute("cars", List.of());
            model.addAttribute("error", "Serviço de veículos indisponível.");
        }

        return "cars";
    }

    @GetMapping("/cars/add")
    public String showAddCarPage(HttpSession session, Model model) {
        UserDto user = (UserDto) session.getAttribute("user");

        // 1. Verificação de Segurança: Apenas ADMIN ou GESTOR_FROTA
        if (user == null || (!user.getRoles().contains("ADMIN") && !user.getRoles().contains("GESTOR_FROTA"))) {
            return "redirect:/cars?error=Acesso Negado";
        }

        model.addAttribute("vehicle", new VehicleDto());
        return "add-vehicle";
    }

    @PostMapping("/cars/add")
    public String addCarProcess(@ModelAttribute VehicleDto vehicleDto, HttpSession session, Model model) {
        UserDto user = (UserDto) session.getAttribute("user");

        if (user == null || (!user.getRoles().contains("ADMIN") && !user.getRoles().contains("GESTOR_FROTA"))) {
            return "redirect:/login";
        }

        try {
            // CORREÇÃO: Usar setAvailable(true) em vez de setStatus
            vehicleDto.setAvailable(true);

            // Definir coordenadas padrão se não forem preenchidas (opcional)
            if (vehicleDto.getLatitude() == 0 && vehicleDto.getLongitude() == 0) {
                vehicleDto.setLatitude(39.82219); // Ex: Castelo Branco
                vehicleDto.setLongitude(-7.49087);
            }

            vehicleClient.createVehicle(vehicleDto);
            return "redirect:/cars?success=Veículo criado com sucesso";
        } catch (Exception e) {
            model.addAttribute("error", "Erro ao criar veículo: " + e.getMessage());
            model.addAttribute("vehicle", vehicleDto);
            return "add-vehicle"; // Volta para a página se der erro
        }
    }

    // --- ALUGAR CARRO (PROTEGIDO) ---
    @PostMapping("/rent-car/{vehicleId}")
    public String rentCar(@PathVariable Long vehicleId,
                          @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                          @RequestParam("endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
                          HttpSession session) {

        UserDto user = (UserDto) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        // Verificação de Roles conforme o enunciado [cite: 280, 322]
        if (user.getRoles() == null || !user.getRoles().contains("CONDUTOR")) {
            return "redirect:/cars?erro=Apenas condutores podem alugar!";
        }

        try {
            RentalDto rental = new RentalDto();
            rental.setUserId(user.getId());
            rental.setVehicleId(vehicleId);
            rental.setStartTime(startTime);
            rental.setEndTime(endTime);
            rental.setActive(true);

            // 1. Regista o aluguer no Rental-Service
            rentalClient.startRental(rental);
            // 2. Bloqueia o veículo no Vehicle-Service para que ninguém mais o veja no mapa
            // Com a interface VehicleClient corrigida acima, esta linha deixará de dar erro.
            vehicleClient.updateAvailability(vehicleId, false);

            return "redirect:/my-rentals";
        } catch (Exception e) {
            return "redirect:/cars?erro=Falha: " + e.getMessage();
        }
    }

    @GetMapping("/stop-rental/{id}")
    public String stopRental(@PathVariable Long id, HttpSession session) {
        UserDto user = (UserDto) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        try {
            // 1. Parar o aluguer (calcula preço e distância)
            RentalDto finishedRental = rentalClient.stopRental(id);

            // 2. Libertar o veículo novamente
            if (finishedRental != null && finishedRental.getVehicleId() != null) {
                vehicleClient.updateAvailability(finishedRental.getVehicleId(), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
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

        if (!sessionUser.getId().equals(id) && !sessionUser.getRoles().contains("ADMIN")) {
            return "redirect:/cars";
        }

        try {
            List<RentalDto> allRentals = rentalClient.getAllRentals();
            List<RentalDto> userRentals = allRentals.stream()
                    .filter(r -> r.getUserId() != null && r.getUserId().equals(id))
                    .collect(Collectors.toList());
            model.addAttribute("viagens", userRentals);
        } catch (Exception e) {
            model.addAttribute("error", "Erro ao carregar viagens.");
        }

        model.addAttribute("user", sessionUser);
        model.addAttribute("isLoggedIn", true);
        return "my-rentals";
    }

    @GetMapping("/users/block/{id}")
    public String blockUserProcess(@PathVariable Long id, HttpSession session) {
        UserDto currentUser = (UserDto) session.getAttribute("user");

        // Segurança: Só ADMIN pode bloquear
        if (currentUser == null || !currentUser.getRoles().contains("ADMIN")) {
            return "redirect:/users?error=Permissao Negada";
        }

        try {
            userClient.toggleBlock(id);
            return "redirect:/users?success=Estado do utilizador alterado";
        } catch (Exception e) {
            return "redirect:/users?error=Erro ao alterar estado";
        }
    }


    // --- PERFIL E UPGRADE DE CONTA ---
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        UserDto user = (UserDto) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        // Recarregar dados frescos do user
        try {
            user = userClient.getUserById(user.getId());
            session.setAttribute("user", user); // Atualizar sessão
        } catch(Exception e) {
            e.printStackTrace();
        }

        model.addAttribute("user", user);
        model.addAttribute("isLoggedIn", true);
        model.addAttribute("currentURI", "/profile");
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute UserDto userDto, HttpSession session) {
        UserDto sessionUser = (UserDto) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        try {
            UserDto updated = userClient.updateUser(sessionUser.getId(), userDto);
            session.setAttribute("user", updated);
            return "redirect:/profile?success=true";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/profile?error=" + e.getMessage();
        }
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new UserDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerProcess(@ModelAttribute UserDto userDto, Model model) {
        try {
            // Tenta registar via UserClient
            userClient.register(userDto);
            return "redirect:/login?registered=true"; // Sucesso, vai para login
        } catch (Exception e) {
            // Erro (ex: username duplicado), volta para o formulário
            model.addAttribute("error", "Erro ao registar: Verifique se os dados são únicos.");
            model.addAttribute("user", userDto); // Mantém os dados preenchidos
            return "register";
        }
    }
}