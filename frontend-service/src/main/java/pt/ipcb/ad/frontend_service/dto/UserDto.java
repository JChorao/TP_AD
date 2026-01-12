package pt.ipcb.ad.frontend_service.dto;

import lombok.Data;
import java.util.Set;

@Data
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;

    // Importante: Set<String> para suportar [CONDUTOR, ADMIN]
    private Set<String> roles;

    private Double rating;
    private Double latitude;
    private Double longitude;
}