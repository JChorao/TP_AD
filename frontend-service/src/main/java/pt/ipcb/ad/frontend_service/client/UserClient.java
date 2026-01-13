package pt.ipcb.ad.frontend_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import pt.ipcb.ad.frontend_service.dto.LoginRequest;
import pt.ipcb.ad.frontend_service.dto.UserDto;
import java.util.List;

@FeignClient(name = "account-service")
public interface UserClient {

    @PostMapping("/accounts/login")
    UserDto login(@RequestBody LoginRequest loginRequest);

    @PostMapping("/accounts/register")
    UserDto register(@RequestBody UserDto user);

    @GetMapping("/accounts/users")
    List<UserDto> getAllUsers();

    @GetMapping("/accounts/users/{username}")
    UserDto getUserByUsername(@PathVariable("username") String username);

    @GetMapping("/accounts/users/id/{id}") // Confirma se no Controller tens este endpoint específico
    UserDto getUserById(@PathVariable("id") Long id);

    @PutMapping("/accounts/users/{id}")
    UserDto updateUser(@PathVariable("id") Long id, @RequestBody UserDto user);

    @PutMapping("/accounts/users/{id}/block")
    void blockUser(@PathVariable("id") Long id, @RequestParam("block") boolean block);

    @PutMapping("/accounts/users/{id}/location")
    UserDto updateLocation(@PathVariable("id") Long id, @RequestParam("latitude") Double latitude, @RequestParam("longitude") Double longitude);
}