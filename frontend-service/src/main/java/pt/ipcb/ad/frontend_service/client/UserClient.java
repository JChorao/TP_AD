package pt.ipcb.ad.frontend_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import pt.ipcb.ad.frontend_service.dto.LoginRequest; // Importa o DTO correto
import pt.ipcb.ad.frontend_service.dto.UserDto;
import java.util.List;

@FeignClient(name = "account-service")
public interface UserClient {

    @GetMapping("/users")
    List<UserDto> getAllUsers();

    @PostMapping("/users/login")
    UserDto login(@RequestBody LoginRequest loginRequest); // Tipagem forte

    @GetMapping("/users/{id}")
    UserDto getUserById(@PathVariable("id") Long id);
}