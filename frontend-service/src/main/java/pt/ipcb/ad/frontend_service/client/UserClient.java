package pt.ipcb.ad.frontend_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import pt.ipcb.ad.frontend_service.dto.UserDto;
import java.util.List;

@FeignClient(name = "account-service")
public interface UserClient {

    @GetMapping("/users/{username}")
    UserDto getUserByUsername(@PathVariable("username") String username);

    @GetMapping("/users/id/{id}")
    UserDto getUserById(@PathVariable("id") Long id);

    @PostMapping("/users")
    UserDto createUser(@RequestBody UserDto user); // @RequestBody é importante aqui

    @PutMapping("/users/{id}")
    UserDto updateUser(@PathVariable("id") Long id, @RequestBody UserDto user);

    @GetMapping("/users")
    List<UserDto> getAllUsers();
}