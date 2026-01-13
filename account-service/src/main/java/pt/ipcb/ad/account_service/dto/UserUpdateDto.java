package pt.ipcb.ad.account_service.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserUpdateDto {
    private String email;
    private String phoneNumber;
    private String address;
    private String oldPassword;
    private String newPassword;
    private String licenseNumber;
    private LocalDate licenseIssueDate;
    private LocalDate licenseExpiryDate;
}