package pt.ipcb.ad.frontend_service.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;
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

    @PostMapping("/login")
    public Object doLogin(@RequestParam String email,
                          @RequestParam String password,
                          HttpSession session,
                          Model model) {
        try {
            Map<String, String> credenciais = new HashMap<>();
            credenciais.put("email", email);
            credenciais.put("password", password);

            // 1. Tenta fazer login na API
            UserDto user = userClient.login(credenciais);

            // 2. IMPORTANTE: Guarda na sessão com o nome "user"
            // O WebController vai buscar este atributo para mostrar o Perfil
            session.setAttribute("user", user);

            // 3. Redireciona para a lista de carros mantendo o contexto (Gateway)
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
        session.invalidate(); // Limpa a sessão

        RedirectView redirect = new RedirectView();
        redirect.setUrl("/login");
        redirect.setContextRelative(true);
        return redirect;
    }
}