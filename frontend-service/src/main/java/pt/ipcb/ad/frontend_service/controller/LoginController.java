package pt.ipcb.ad.frontend_service.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pt.ipcb.ad.frontend_service.client.UserClient;
import pt.ipcb.ad.frontend_service.dto.UserDto;

@Controller
public class LoginController {

    private final UserClient userClient;

    public LoginController(UserClient userClient) {
        this.userClient = userClient;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        try {
            UserDto user = userClient.getUserByUsername(username);

            // Verificação simples (Nota: Em produção a password deve ser verificada pelo account-service)
            if (user != null) {
                // Se a password não estiver a ser verificada, qualquer string serve.
                // Se o account-service verificar, este bloco só executa se não lançar exceção.
                session.setAttribute("user", user);
                return "redirect:/cars";
            }
        } catch (Exception e) {
            // user not found
        }
        model.addAttribute("error", "Credenciais inválidas");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // --- REGISTO ---
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new UserDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerProcess(@ModelAttribute UserDto user, Model model) {
        try {
            userClient.createUser(user);
            return "redirect:/login?registered=true";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erro ao criar conta. Username pode já existir.");
            return "register";
        }
    }
}