package pt.ipcb.ad.frontend_service.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pt.ipcb.ad.frontend_service.client.PaymentClient; // <--- Import Novo
import pt.ipcb.ad.frontend_service.client.RentalClient;
import pt.ipcb.ad.frontend_service.client.UserClient;
import pt.ipcb.ad.frontend_service.client.VehicleClient;
import pt.ipcb.ad.frontend_service.dto.PaymentDto; // <--- Import Novo
import pt.ipcb.ad.frontend_service.dto.RentalDto;
import pt.ipcb.ad.frontend_service.dto.UserDto;
import pt.ipcb.ad.frontend_service.dto.VehicleDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class WebController {

    private final VehicleClient vehicleClient;
    private final RentalClient rentalClient;
    private final UserClient userClient;
    private final PaymentClient paymentClient; // <--- Cliente Novo

    public WebController(VehicleClient vehicleClient,
                         RentalClient rentalClient,
                         UserClient userClient,
                         PaymentClient paymentClient) {
        this.vehicleClient = vehicleClient;
        this.rentalClient = rentalClient;
        this.userClient = userClient;
        this.paymentClient = paymentClient;
    }

    @GetMapping("/")
    public String rootRedirect() {
        return "redirect:/cars";
    }

    @GetMapping("/cars")
    public String carsPage(Model model, HttpSession session) {
        UserDto user = (UserDto) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        model.addAttribute("isLoggedIn", true);
        model.addAttribute("currentURI", "/cars"); // Adicionado para navbar

        try {
            List<VehicleDto> cars = vehicleClient.getAllVehicles();
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

    @PostMapping("/rent-car/{vehicleId}")
    public String rentCar(@PathVariable Long vehicleId,
                          @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                          @RequestParam("endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
                          HttpSession session) {

        UserDto user = (UserDto) session.getAttribute("user");
        if (user == null) return "redirect:/login";

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

            rentalClient.startRental(rental);
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
            RentalDto finishedRental = rentalClient.stopRental(id);
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

        model.addAttribute("currentURI", "/my-rentals");
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
    public String blockUserProcess(@PathVariable Long id,
                                   @RequestParam("block") boolean block, // <--- Adicionado parâmetro
                                   HttpSession session) {
        UserDto currentUser = (UserDto) session.getAttribute("user");

        if (currentUser == null || !currentUser.getRoles().contains("ADMIN")) {
            return "redirect:/users?error=Permissao Negada";
        }

        try {
            // Chama o método correto do UserClient (blockUser em vez de toggleBlock)
            userClient.blockUser(id, block);

            String msg = block ? "Utilizador bloqueado com sucesso" : "Utilizador desbloqueado";
            return "redirect:/users?success=" + msg;
        } catch (Exception e) {
            return "redirect:/users?error=Erro ao alterar estado";
        }
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        UserDto user = (UserDto) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        try {
            user = userClient.getUserById(user.getId());
            session.setAttribute("user", user);
        } catch(Exception e) { e.printStackTrace(); }

        model.addAttribute("user", user);
        model.addAttribute("isLoggedIn", true);
        model.addAttribute("currentURI", "/profile");
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute UserDto userDto, HttpSession session) {
        UserDto sessionUser = (UserDto) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        if (userDto.getNewPassword() != null && !userDto.getNewPassword().trim().isEmpty()) {
            if (userDto.getOldPassword() == null || userDto.getOldPassword().trim().isEmpty()) {
                return "redirect:/profile?error=Tem de colocar a password antiga para alterar a password.";
            }
        }

        try {
            UserDto updated = userClient.updateUser(sessionUser.getId(), userDto);
            session.setAttribute("user", updated);
            return "redirect:/profile?success=true";
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg.contains("A password antiga está incorreta")) msg = "A password antiga está incorreta!";
            return "redirect:/profile?error=" + msg;
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
            userClient.register(userDto);
            return "redirect:/login?registered=true";
        } catch (Exception e) {
            model.addAttribute("error", "Erro ao registar: Verifique dados.");
            model.addAttribute("user", userDto);
            return "register";
        }
    }

    // --- NOVO MÉTODO: ESTATÍSTICAS / PAGAMENTOS ---
    @GetMapping("/statistics")
    public String statistics(HttpSession session, Model model) {
        UserDto user = (UserDto) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        boolean isAdmin = user.getRoles() != null && user.getRoles().contains("ADMIN");
        boolean isManager = user.getRoles() != null && user.getRoles().contains("GESTOR_FROTA");

        if (!isAdmin && !isManager) {
            return "redirect:/cars?erro=Acesso negado.";
        }

        try {
            List<PaymentDto> payments = paymentClient.getAllPayments();
            List<UserDto> users = userClient.getAllUsers();
            List<VehicleDto> vehicles = vehicleClient.getAllVehicles();

            Map<Long, UserDto> userMap = users.stream()
                    .collect(Collectors.toMap(UserDto::getId, Function.identity(), (a, b) -> a));

            Map<Long, VehicleDto> vehicleMap = vehicles.stream()
                    .collect(Collectors.toMap(VehicleDto::getId, Function.identity(), (a, b) -> a));

            for (PaymentDto p : payments) {
                if (p.getUserId() != null && userMap.containsKey(p.getUserId())) {
                    p.setUserName(userMap.get(p.getUserId()).getUsername());
                } else {
                    p.setUserName("User #" + p.getUserId());
                }

                if (p.getVehicleId() != null && vehicleMap.containsKey(p.getVehicleId())) {
                    VehicleDto v = vehicleMap.get(p.getVehicleId());
                    p.setVehicleInfo(v.getBrand() + " " + v.getModel() + " (" + v.getLicensePlate() + ")");
                } else {
                    p.setVehicleInfo("Carro #" + p.getVehicleId());
                }
            }

            model.addAttribute("payments", payments);
            double totalRevenue = payments.stream().mapToDouble(PaymentDto::getAmount).sum();
            model.addAttribute("totalRevenue", totalRevenue);
            model.addAttribute("totalTrips", payments.size());

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erro ao carregar estatísticas. Serviços indisponíveis.");
            model.addAttribute("payments", List.of());
            model.addAttribute("totalRevenue", 0.0);
            model.addAttribute("totalTrips", 0);
        }

        model.addAttribute("user", user);
        model.addAttribute("isLoggedIn", true);
        model.addAttribute("currentURI", "/statistics");
        return "statistics";
    }

    @GetMapping("/manage-vehicles")
    public String manageVehicles(HttpSession session, Model model) {
        UserDto user = (UserDto) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        // Verifica permissões
        boolean isAdmin = user.getRoles().contains("ADMIN");
        boolean isManager = user.getRoles().contains("GESTOR_FROTA");

        if (!isAdmin && !isManager) {
            return "redirect:/cars?erro=Acesso negado.";
        }

        try {
            List<VehicleDto> cars = vehicleClient.getAllVehicles();
            model.addAttribute("cars", cars);
        } catch (Exception e) {
            model.addAttribute("error", "Erro ao carregar frota.");
        }

        model.addAttribute("user", user);
        model.addAttribute("isLoggedIn", true);
        model.addAttribute("currentURI", "/manage-vehicles");
        // Vamos usar um DTO vazio para o formulário de criação
        model.addAttribute("newVehicle", new VehicleDto());

        return "manage-vehicles";
    }

    @PostMapping("/manage-vehicles/create")
    public String createVehicle(@ModelAttribute VehicleDto vehicleDto, HttpSession session) {
        UserDto user = (UserDto) session.getAttribute("user");

        // TEM DE TER ESTA LINHA COM "GESTOR_FROTA"
        if (user == null || (!user.getRoles().contains("ADMIN") && !user.getRoles().contains("GESTOR_FROTA"))) {
            return "redirect:/login";
        }

        try {
            vehicleDto.setAvailable(true);
            vehicleClient.createVehicle(vehicleDto);
            return "redirect:/manage-vehicles?success=Veículo criado com sucesso";
        } catch (Exception e) {
            return "redirect:/manage-vehicles?error=Erro ao criar veículo";
        }
    }

    @GetMapping("/manage-vehicles/delete/{id}")
    public String deleteVehicle(@PathVariable Long id, HttpSession session) {
        UserDto user = (UserDto) session.getAttribute("user");
        if (user == null || (!user.getRoles().contains("GESTOR_FROTA"))) {
            return "redirect:/login";
        }

        try {
            vehicleClient.deleteVehicle(id);
            return "redirect:/manage-vehicles?success=Veículo removido";
        } catch (Exception e) {
            return "redirect:/manage-vehicles?error=Erro ao remover veículo";
        }
    }

    @PostMapping("/vehicles/update")
    public String updateVehicle(@ModelAttribute VehicleDto vehicleDto) {
        try {
            // O ID vem escondido no formulário (hidden input)
            vehicleClient.updateVehicle(vehicleDto.getId(), vehicleDto);
            return "redirect:/vehicles/manage?success=Veículo atualizado com sucesso";
        } catch (Exception e) {
            return "redirect:/vehicles/manage?error=Erro ao atualizar veículo";
        }
    }

    @PostMapping("/users/location")
    public String updateUserLocation(@RequestParam Double latitude, @RequestParam Double longitude, HttpSession session) {
        UserDto user = (UserDto) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        try {
            // Chama o novo endpoint do backend
            UserDto updated = userClient.updateLocation(user.getId(), latitude, longitude);

            // Atualiza o utilizador na sessão para o mapa refletir a mudança após o refresh
            session.setAttribute("user", updated);

            return "redirect:/cars?successLoc=true";
        } catch (Exception e) {
            return "redirect:/cars?errorLoc=true";
        }
    }
}