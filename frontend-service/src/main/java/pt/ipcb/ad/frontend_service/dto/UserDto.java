package pt.ipcb.ad.frontend_service.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private String address;
    private Set<String> roles;
    private boolean blocked;

    private String password;

    private String newPassword;
    private String oldPassword;

    private String licenseNumber;
    private LocalDate licenseIssueDate;
    private LocalDate licenseExpiryDate;
    private Double latitude;
    private Double longitude;
}