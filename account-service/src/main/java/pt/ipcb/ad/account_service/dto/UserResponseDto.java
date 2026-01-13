package pt.ipcb.ad.account_service.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UserResponseDto {
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private String address;
    private Set<String> roles;
    private boolean blocked;
    private String licenseNumber;
    private LocalDate licenseIssueDate;
    private LocalDate licenseExpiryDate;
    private Double latitude;
    private Double longitude;

    // Construtor manual ou usa um Mapper
    public UserResponseDto(Long id, String username, String email, String phoneNumber, String address, Set<String> roles, boolean blocked, String licenseNumber, LocalDate licenseIssueDate, LocalDate licenseExpiryDate, Double latitude, Double longitude) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.roles = roles;
        this.blocked = blocked;
        this.licenseNumber = licenseNumber;
        this.licenseIssueDate = licenseIssueDate;
        this.licenseExpiryDate = licenseExpiryDate;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}