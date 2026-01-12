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
        // Envia um objeto vazio para o formulário preencher
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    @PostMapping("/login")
    public String handleLogin(@ModelAttribute LoginRequest loginRequest, HttpSession session, Model model) {
        try {
            // 1. Tenta login na API
            UserDto user = userClient.login(loginRequest);

            // 2. Guarda na Sessão
            session.setAttribute("user", user);

            // 3. Redireciona para os carros
            return "redirect:/cars";

        } catch (Exception e) {
            model.addAttribute("error", "Credenciais inválidas ou erro no servidor.");
            model.addAttribute("loginRequest", loginRequest); // Devolve o que foi escrito
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}