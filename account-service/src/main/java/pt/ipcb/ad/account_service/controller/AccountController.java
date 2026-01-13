package pt.ipcb.ad.account_service.controller;

import org.springframework.http.HttpStatus;
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
import java.util.HashSet; // Importante para criar Sets mutáveis
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
                user.getLicenseExpiryDate(),
                user.getLatitude(),
                user.getLongitude()
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Optional<User> userOpt = userRepository.findByUsername(loginRequest.getEmail());
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(loginRequest.getEmail());
        }

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                if (user.isBlocked()) {
                    return ResponseEntity.status(403).body("Conta Bloqueada");
                }
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
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Utiliza um HashSet para garantir que a lista de roles é mutável futuramente
        user.setRoles(new HashSet<>(Collections.singletonList(Role.PASSAGEIRO.name())));
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(convertToDto(savedUser));
    }

    @GetMapping("/users")
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/users/id/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(convertToDto(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserUpdateDto updateRequest) {
        return userRepository.findById(id).map(user -> {

            // --- ATUALIZAÇÃO DE USERNAME (Com verificação de duplicado) ---
            if (updateRequest.getUsername() != null && !updateRequest.getUsername().trim().isEmpty()) {
                if (!user.getUsername().equals(updateRequest.getUsername())) {
                    if (userRepository.findByUsername(updateRequest.getUsername()).isPresent()) {
                        throw new RuntimeException("Este username já está a ser utilizado.");
                    }
                    user.setUsername(updateRequest.getUsername());
                }
            }

            if (updateRequest.getEmail() != null) user.setEmail(updateRequest.getEmail());
            if (updateRequest.getAddress() != null) user.setAddress(updateRequest.getAddress());
            if (updateRequest.getPhoneNumber() != null) user.setPhoneNumber(updateRequest.getPhoneNumber());

            // --- LÓGICA DE DATAS DA CARTA ---
            if (updateRequest.getLicenseIssueDate() != null) user.setLicenseIssueDate(updateRequest.getLicenseIssueDate());
            if (updateRequest.getLicenseExpiryDate() != null) user.setLicenseExpiryDate(updateRequest.getLicenseExpiryDate());

            // --- ALTERAÇÃO AQUI: SUBSTITUIR PASSAGEIRO POR CONDUTOR ---
            if (updateRequest.getLicenseNumber() != null) {
                user.setLicenseNumber(updateRequest.getLicenseNumber());

                // Se a carta foi inserida e não está vazia
                if (!user.getLicenseNumber().trim().isEmpty()) {

                    // 1. Criar uma cópia mutável das roles atuais para evitar UnsupportedOperationException
                    Set<String> currentRoles = new HashSet<>(user.getRoles());

                    // 2. Remover a role de PASSAGEIRO (se existir)
                    currentRoles.remove(Role.PASSAGEIRO.name());

                    // 3. Adicionar a role de CONDUTOR (se não existir)
                    currentRoles.add(Role.CONDUTOR.name());

                    // 4. Aplicar as novas roles ao utilizador
                    user.setRoles(currentRoles);
                }
            }

            // --- ATUALIZAÇÃO DE PASSWORD ---
            if (updateRequest.getNewPassword() != null && !updateRequest.getNewPassword().isEmpty()) {
                if (updateRequest.getOldPassword() == null ||
                        !passwordEncoder.matches(updateRequest.getOldPassword(), user.getPassword())) {
                    throw new RuntimeException("A password antiga está incorreta");
                }
                user.setPassword(passwordEncoder.encode(updateRequest.getNewPassword()));
            }

            // Tratamento de erros no Controller
            try {
                return ResponseEntity.ok(convertToDto(userRepository.save(user)));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }

        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}/block")
    public ResponseEntity<?> toggleBlock(@PathVariable Long id, @RequestParam boolean block) {
        return userRepository.findById(id).map(user -> {
            user.setBlocked(block);
            userRepository.save(user);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}/location")
    public ResponseEntity<?> updateLocation(@PathVariable Long id, @RequestParam Double latitude, @RequestParam Double longitude) {
        return userRepository.findById(id).map(user -> {
            user.setLatitude(latitude);
            user.setLongitude(longitude);
            User saved = userRepository.save(user);
            return ResponseEntity.ok(convertToDto(saved));
        }).orElse(ResponseEntity.notFound().build());
    }
}