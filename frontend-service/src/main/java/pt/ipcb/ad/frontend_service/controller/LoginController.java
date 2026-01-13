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

    @GetMapping("/blocked")
    public String blockedPage() {
        return "blocked"; // Vai procurar o blocked.html
    }

    // 2. Atualizar o login para detetar o bloqueio
    @PostMapping("/login")
    public String login(@ModelAttribute LoginRequest loginRequest, HttpSession session, Model model) {
        try {
            UserDto userDto = userClient.login(loginRequest);
            session.setAttribute("user", userDto);

            // Redireciona consoante o role (Admin ou normal)
            if (userDto.getRoles().contains("ADMIN") || userDto.getRoles().contains("GESTOR_FROTA")) {
                return "redirect:/cars";
            }
            return "redirect:/cars";

        } catch (feign.FeignException.Forbidden e) {
            // ERRO 403: O utilizador está BLOQUEADO
            return "redirect:/blocked";

        } catch (Exception e) {
            // Outros erros (ex: password errada)
            model.addAttribute("error", "Login falhou: Credenciais inválidas.");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}