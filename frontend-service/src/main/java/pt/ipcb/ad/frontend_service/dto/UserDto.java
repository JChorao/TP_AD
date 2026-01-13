package pt.ipcb.ad.frontend_service.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.Set;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String password;
    private String address;
    private String phoneNumber;
    private String email;

    // Carta
    private String licenseNumber;
    private LocalDate licenseIssueDate;
    private LocalDate licenseExpiryDate;

    // Novos Campos
    private Double rating;
    private Double latitude;
    private Double longitude;
    private boolean blocked;

    private Set<String> roles;
}