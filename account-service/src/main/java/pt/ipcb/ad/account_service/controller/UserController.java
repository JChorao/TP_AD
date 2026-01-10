package pt.ipcb.ad.account_service.controller; // Package corrigido

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pt.ipcb.ad.account_service.model.User;
import pt.ipcb.ad.account_service.repository.UserRepository;
import pt.ipcb.ad.account_service.dto.LoginRequest; // Importa o DTO

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository repository;

    @GetMapping
    public List<User> getAllUsers() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return repository.findById(id).orElse(null);
    }

    // NOVO ENDPOINT DE LOGIN
    @PostMapping("/login")
    public User login(@RequestBody LoginRequest request) {
        // 1. Procura pelo email
        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));

        // 2. Verifica a password (Num projeto real usarias BCrypt, aqui comparamos texto)
        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Password errada!");
        }

        return user; // Retorna o utilizador completo se tudo estiver bem
    }
}