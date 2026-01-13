package pt.ipcb.ad.frontend_service.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import pt.ipcb.ad.frontend_service.client.UserClient;
import pt.ipcb.ad.frontend_service.dto.LoginRequest;
import pt.ipcb.ad.frontend_service.dto.UserDto;

@Controller
public class LoginController {

    private final UserClient userClient;

    public LoginController(UserClient userClient) {
        this.userClient = userClient;
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute LoginRequest loginRequest, HttpSession session, Model model) {
        try {
            UserDto user = userClient.login(loginRequest);
            if (user != null) {
                session.setAttribute("user", user);
                return "redirect:/cars";
            }
        } catch (Exception e) {
            System.err.println("Erro ao autenticar: " + e.getMessage());
        }
        model.addAttribute("error", "Credenciais inválidas ou serviço indisponível");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}