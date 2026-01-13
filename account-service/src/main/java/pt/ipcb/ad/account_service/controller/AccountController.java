package pt.ipcb.ad.account_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pt.ipcb.ad.account_service.dto.LoginRequest;
import pt.ipcb.ad.account_service.model.Role;
import pt.ipcb.ad.account_service.model.User;
import pt.ipcb.ad.account_service.repository.UserRepository;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Procura por email, se não encontrar tenta por username
        User user = userRepository.findByEmail(request.getEmail())
                .orElseGet(() -> userRepository.findByUsername(request.getEmail()).orElse(null));

        if (user != null && passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.ok(user);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais Inválidas");
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/users/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        return userRepository.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        return userRepository.findById(id).map(user -> {
            user.setEmail(userDetails.getEmail());
            user.setAddress(userDetails.getAddress());
            user.setPhoneNumber(userDetails.getPhoneNumber());
            // Não atualizamos a password aqui por segurança neste exemplo simples
            return ResponseEntity.ok(userRepository.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        // 1. Verificar se o username ou email já existem
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username já está em uso.");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email já está em uso.");
        }

        // 2. Encriptar a password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 3. Definir Role por defeito como PASSAGEIRO
        user.setRoles(Collections.singleton(Role.PASSAGEIRO.name()));

        // 4. Guardar
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }
}