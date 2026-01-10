package pt.ipcb.ad.frontend_service.controller;

import jakarta.servlet.http.HttpSession; // Importante: Sessão
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pt.ipcb.ad.frontend_service.client.UserClient;
import pt.ipcb.ad.frontend_service.dto.UserDto;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LoginController {

    @Autowired
    private UserClient userClient;

    // Mostrar página de Login
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // Processar o Login
    @PostMapping("/login")
    public String doLogin(@RequestParam String email,
                          @RequestParam String password,
                          HttpSession session,
                          Model model) {
        try {
            // Cria o objeto JSON na mão (Map) para enviar
            Map<String, String> credenciais = new HashMap<>();
            credenciais.put("email", email);
            credenciais.put("password", password);

            // Chama o Backend
            UserDto user = userClient.login(credenciais);

            // SUCESSO: Guarda o utilizador na Sessão!
            session.setAttribute("user", user);

            return "redirect:/cars"; // Vai para a lista de carros

        } catch (Exception e) {
            model.addAttribute("erro", "Email ou password inválidos.");
            return "login";
        }
    }

    // Logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Limpa a sessão
        return "redirect:/login";
    }
}