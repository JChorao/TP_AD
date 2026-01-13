package pt.ipcb.ad.account_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pt.ipcb.ad.account_service.dto.LoginRequest;
import pt.ipcb.ad.account_service.dto.UserResponseDto;
import pt.ipcb.ad.account_service.dto.UserUpdateDto;
import pt.ipcb.ad.account_service.model.Role;
import pt.ipcb.ad.account_service.model.User;
import pt.ipcb.ad.account_service.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private UserResponseDto convertToDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getAddress(),
                user.getRoles(),
                user.isBlocked(),
                user.getLicenseNumber(),
                user.getLicenseIssueDate(),
                user.getLicenseExpiryDate()
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // 1. Procurar por Username (Faltava esta parte no teu código)
        Optional<User> userOpt = userRepository.findByUsername(loginRequest.getEmail());

        // 2. Se não encontrar por Username, procura por Email
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(loginRequest.getEmail());
        }

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {

                if (user.isBlocked()) {
                    return ResponseEntity.status(403).body("Conta Bloqueada");
                }

                // CORRETO: Retorna o DTO sem password
                return ResponseEntity.ok(convertToDto(user));
            }
        }
        return ResponseEntity.status(401).body("Credenciais Inválidas");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username já está em uso.");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email já está em uso.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Collections.singleton(Role.PASSAGEIRO.name()));

        User savedUser = userRepository.save(user);

        // CORREÇÃO: Retorna DTO também no registo
        return ResponseEntity.ok(convertToDto(savedUser));
    }

    @GetMapping("/users")
    public List<UserResponseDto> getAllUsers() {
        // CORREÇÃO: Converte a lista de Users para lista de DTOs
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/users/{username}")
    public ResponseEntity<UserResponseDto> getUserByUsername(@PathVariable String username) {
        // CORREÇÃO: Retorna DTO
        return userRepository.findByUsername(username)
                .map(user -> ResponseEntity.ok(convertToDto(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserUpdateDto updateRequest) {
        return userRepository.findById(id).map(user -> {

            // 1. Dados Básicos
            if (updateRequest.getEmail() != null) user.setEmail(updateRequest.getEmail());
            if (updateRequest.getAddress() != null) user.setAddress(updateRequest.getAddress());
            if (updateRequest.getPhoneNumber() != null) user.setPhoneNumber(updateRequest.getPhoneNumber());

            // 2. Carta de Condução (NOVO)
            if (updateRequest.getLicenseNumber() != null) user.setLicenseNumber(updateRequest.getLicenseNumber());
            if (updateRequest.getLicenseIssueDate() != null) user.setLicenseIssueDate(updateRequest.getLicenseIssueDate());
            if (updateRequest.getLicenseExpiryDate() != null) user.setLicenseExpiryDate(updateRequest.getLicenseExpiryDate());

            // 3. Password (Lógica existente)
            if (updateRequest.getNewPassword() != null && !updateRequest.getNewPassword().isEmpty()) {
                if (updateRequest.getOldPassword() == null ||
                        !passwordEncoder.matches(updateRequest.getOldPassword(), user.getPassword())) {
                    return ResponseEntity.badRequest().body("A password antiga está incorreta");
                }
                user.setPassword(passwordEncoder.encode(updateRequest.getNewPassword()));
            }

            User savedUser = userRepository.save(user);
            return ResponseEntity.ok(convertToDto(savedUser));

        }).orElse(ResponseEntity.notFound().build());
    }

    // --- NOVO ENDPOINT DE BLOQUEIO (ADMINISTRADOR) ---
    @PutMapping("/users/{id}/block")
    public ResponseEntity<?> toggleBlock(@PathVariable Long id, @RequestParam boolean block) {
        return userRepository.findById(id).map(user -> {
            user.setBlocked(block);
            userRepository.save(user);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}