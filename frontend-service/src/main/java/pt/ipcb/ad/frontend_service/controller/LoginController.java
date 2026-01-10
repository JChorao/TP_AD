package pt.ipcb.ad.frontend_service.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView; // <--- IMPORTANTE
import pt.ipcb.ad.frontend_service.client.UserClient;
import pt.ipcb.ad.frontend_service.dto.UserDto;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LoginController {

    @Autowired
    private UserClient userClient;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // --- CORREÇÃO AQUI ---
    @PostMapping("/login")
    public Object doLogin(@RequestParam String email,
                          @RequestParam String password,
                          HttpSession session,
                          Model model) {
        try {
            Map<String, String> credenciais = new HashMap<>();
            credenciais.put("email", email);
            credenciais.put("password", password);

            UserDto user = userClient.login(credenciais);
            session.setAttribute("user", user);

            // EM VEZ DE: return "redirect:/cars";
            // USAMOS ISTO PARA FORÇAR O REDIRECT RELATIVO:
            RedirectView redirect = new RedirectView();
            redirect.setUrl("/cars");
            redirect.setContextRelative(true);
            return redirect;

        } catch (Exception e) {
            model.addAttribute("erro", "Credenciais inválidas.");
            return "login";
        }
    }

    @GetMapping("/logout")
    public RedirectView logout(HttpSession session) {
        session.invalidate(); // 1. Apaga a sessão (faz logout efetivo)

        // 2. Redireciona para o login mantendo a porta 8080
        RedirectView redirect = new RedirectView();
        redirect.setUrl("/login"); // Vai para a página de login
        redirect.setContextRelative(true); // Força a manter-se no Gateway (8080)
        return redirect;
    }
}