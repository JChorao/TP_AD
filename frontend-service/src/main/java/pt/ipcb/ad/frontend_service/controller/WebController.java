package pt.ipcb.ad.frontend_service.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pt.ipcb.ad.frontend_service.client.PaymentClient;
import pt.ipcb.ad.frontend_service.client.RentalClient;
import pt.ipcb.ad.frontend_service.client.UserClient;
import pt.ipcb.ad.frontend_service.client.VehicleClient;
import pt.ipcb.ad.frontend_service.dto.PaymentDto;
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
    private final PaymentClient paymentClient;

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
        model.addAttribute("currentURI", "/cars");

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
            model.addAttribute("erro", "Erro ao carregar utilizadores.");
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
    public String blockUserProcess(@PathVariable Long id, @RequestParam("block") boolean block, HttpSession session) {
        UserDto currentUser = (UserDto) session.getAttribute("user");
        if (currentUser == null || !currentUser.getRoles().contains("ADMIN")) {
            return "redirect:/users?error=Permissao Negada";
        }
        try {
            userClient.blockUser(id, block);
            return "redirect:/users?success=Estado atualizado";
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

        try {
            UserDto updated = userClient.updateUser(sessionUser.getId(), userDto);
            session.setAttribute("user", updated);
            return "redirect:/profile?success=true";
        } catch (Exception e) {
            return "redirect:/profile?error=Erro ao atualizar";
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
            model.addAttribute("error", "Erro ao registar.");
            model.addAttribute("user", userDto);
            return "register";
        }
    }

    @GetMapping("/statistics")
    public String statistics(HttpSession session, Model model) {
        UserDto user = (UserDto) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        // CORREÇÃO: Apenas GESTOR_FROTA pode entrar. Admin é redirecionado se não tiver esse role.
        if (user.getRoles() == null || !user.getRoles().contains("GESTOR_FROTA")) {
            return "redirect:/cars?erro=Acesso negado. Apenas o Gestor de Frota pode ver estatísticas.";
        }

        try {
            List<PaymentDto> payments = paymentClient.getAllPayments();
            List<UserDto> users = userClient.getAllUsers();
            List<VehicleDto> vehicles = vehicleClient.getAllVehicles();

            Map<Long, UserDto> userMap = users.stream().collect(Collectors.toMap(UserDto::getId, Function.identity(), (a, b) -> a));
            Map<Long, VehicleDto> vehicleMap = vehicles.stream().collect(Collectors.toMap(VehicleDto::getId, Function.identity(), (a, b) -> a));

            for (PaymentDto p : payments) {
                if (p.getUserId() != null && userMap.containsKey(p.getUserId())) p.setUserName(userMap.get(p.getUserId()).getUsername());
                if (p.getVehicleId() != null && vehicleMap.containsKey(p.getVehicleId())) {
                    VehicleDto v = vehicleMap.get(p.getVehicleId());
                    p.setVehicleInfo(v.getBrand() + " " + v.getModel());
                }
            }

            model.addAttribute("payments", payments);
            model.addAttribute("totalRevenue", payments.stream().mapToDouble(PaymentDto::getAmount).sum());
            model.addAttribute("totalTrips", payments.size());
        } catch (Exception e) {
            model.addAttribute("error", "Serviços indisponíveis.");
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

        if (!user.getRoles().contains("GESTOR_FROTA") && !user.getRoles().contains("ADMIN")) {
            return "redirect:/cars?erro=Acesso negado.";
        }

        try {
            List<VehicleDto> cars = vehicleClient.getAllVehicles();
            for (VehicleDto c : cars) {
                if (Math.abs(c.getLatitude()) < 0.000001) {
                    c.setLatitude(39.8230);
                    c.setLongitude(-7.4919);
                }
            }
            model.addAttribute("cars", cars);
        } catch (Exception e) {
            model.addAttribute("error", "Erro ao carregar frota.");
        }

        model.addAttribute("user", user);
        model.addAttribute("isLoggedIn", true);
        model.addAttribute("currentURI", "/manage-vehicles");
        model.addAttribute("newVehicle", new VehicleDto());
        return "manage-vehicles";
    }

    @PostMapping("/manage-vehicles/create")
    public String createVehicle(@ModelAttribute VehicleDto vehicleDto, HttpSession session) {
        UserDto user = (UserDto) session.getAttribute("user");
        if (user == null || !user.getRoles().contains("GESTOR_FROTA")) return "redirect:/login";

        try {
            vehicleDto.setAvailable(true);
            vehicleClient.createVehicle(vehicleDto);
            return "redirect:/manage-vehicles?success=Criado com sucesso";
        } catch (Exception e) {
            return "redirect:/manage-vehicles?error=Erro ao criar";
        }
    }

    @PostMapping("/manage-vehicles/delete/{id}")
    public String deleteVehicle(@PathVariable Long id, HttpSession session) {
        UserDto user = (UserDto) session.getAttribute("user");
        if (user == null || !user.getRoles().contains("GESTOR_FROTA")) return "redirect:/login";

        try {
            vehicleClient.deleteVehicle(id);
            return "redirect:/manage-vehicles?success=Removido";
        } catch (Exception e) {
            return "redirect:/manage-vehicles?error=Erro ao remover";
        }
    }

    @PostMapping("/manage-vehicles/update")
    public String updateVehicle(@ModelAttribute VehicleDto vehicleDto, HttpSession session) {
        UserDto user = (UserDto) session.getAttribute("user");
        if (user == null || !user.getRoles().contains("GESTOR_FROTA")) return "redirect:/login";

        try {
            vehicleClient.updateVehicle(vehicleDto.getId(), vehicleDto);
            return "redirect:/manage-vehicles?success=Atualizado com sucesso";
        } catch (Exception e) {
            return "redirect:/manage-vehicles?error=Erro ao atualizar";
        }
    }

    @PostMapping("/users/location")
    public String updateUserLocation(@RequestParam Double latitude, @RequestParam Double longitude, HttpSession session) {
        UserDto user = (UserDto) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        try {
            UserDto updated = userClient.updateLocation(user.getId(), latitude, longitude);
            session.setAttribute("user", updated);
            return "redirect:/cars?successLoc=true";
        } catch (Exception e) {
            return "redirect:/cars?errorLoc=true";
        }
    }
}