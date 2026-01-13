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

    @GetMapping("/accounts/users")
    List<UserDto> getAllUsers();

    @GetMapping("/accounts/users/username/{username}")
    UserDto getUserByUsername(@PathVariable("username") String username);

    @GetMapping("/accounts/users/{id}")
    UserDto getUserById(@PathVariable("id") Long id);

    @PostMapping("/accounts/users")
    UserDto createUser(@RequestBody UserDto user);

    @PutMapping("/accounts/users/{id}")
    UserDto updateUser(@PathVariable("id") Long id, @RequestBody UserDto user);

    @PostMapping("/accounts/register")
    UserDto register(@RequestBody UserDto user);
}