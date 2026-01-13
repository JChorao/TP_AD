package pt.ipcb.ad.account_service.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Data
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;
    private String password;

    // --- CORREÇÃO: Adicionado o campo em falta ---
    private String email;

    // --- Dados Pessoais e Morada ---
    private String address;
    private String phoneNumber;

    // --- Gestão de Veículos e Perfis (Condutor) ---
    private String licenseNumber;
    private LocalDate licenseIssueDate;
    private LocalDate licenseExpiryDate;

    // --- Avaliação e Reputação ---
    private Double rating;

    // --- Localização ---
    private Double latitude;
    private Double longitude;

    // --- Administração ---
    private boolean blocked = false;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> roles;
}