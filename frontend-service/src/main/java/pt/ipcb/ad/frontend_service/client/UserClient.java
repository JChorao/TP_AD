package pt.ipcb.ad.frontend_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping; // Novo
import org.springframework.web.bind.annotation.RequestBody; // Novo
import pt.ipcb.ad.frontend_service.dto.UserDto;
import java.util.List;

@FeignClient(name = "account-service")
public interface UserClient {

    @GetMapping("/users")
    List<UserDto> getAllUsers();

    // --- NOVO ---
    @PostMapping("/users/login")
    UserDto login(@RequestBody Object loginRequest); // Usamos Object para não duplicar DTOs

    @GetMapping("/users/{id}")
    UserDto getUserById(@PathVariable("id") Long id);
}