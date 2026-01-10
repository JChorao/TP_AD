package pt.ipcb.ad.rental_service.dto;

import lombok.Getter;

@Getter
public class UserDto {
    private Long id;
    private String name;
    private String email; // Opcional, se quiseres trazer o email também

    // Construtor vazio (obrigatório para o JSON funcionar)
    public UserDto() {
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}