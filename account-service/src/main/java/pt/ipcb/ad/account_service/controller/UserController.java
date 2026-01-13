package pt.ipcb.ad.account_service.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pt.ipcb.ad.account_service.model.User;
import pt.ipcb.ad.account_service.repository.UserRepository;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/{username}")
    public User getUser(@PathVariable String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @GetMapping("/id/{id}")
    public User getUserById(@PathVariable Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Endpoint de Registo / Criação
    @PostMapping
    public User createUser(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Regra de Negócio: Se não tiver roles, é PASSAGEIRO por defeito
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(new HashSet<>(Collections.singletonList("PASSAGEIRO")));
        }
        return userRepository.save(user);
    }

    // Endpoint de Atualização (Upgrade para Condutor)
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        return userRepository.findById(id).map(user -> {
            // Atualizar morada se fornecida
            if (updatedUser.getAddress() != null) user.setAddress(updatedUser.getAddress());

            // Atualizar Carta de Condução
            boolean licenseUpdated = false;
            if (updatedUser.getLicenseNumber() != null && !updatedUser.getLicenseNumber().isEmpty()) {
                user.setLicenseNumber(updatedUser.getLicenseNumber());
                licenseUpdated = true;
            }
            if (updatedUser.getLicenseIssueDate() != null) {
                user.setLicenseIssueDate(updatedUser.getLicenseIssueDate());
            }
            if (updatedUser.getLicenseExpiryDate() != null) {
                user.setLicenseExpiryDate(updatedUser.getLicenseExpiryDate());
            }

            // Regra de Negócio: Se tem número de carta, torna-se CONDUTOR
            if (licenseUpdated || (user.getLicenseNumber() != null && !user.getLicenseNumber().isEmpty())) {
                user.getRoles().add("CONDUTOR");
            }

            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }
}