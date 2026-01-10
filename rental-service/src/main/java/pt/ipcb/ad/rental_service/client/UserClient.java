package pt.ipcb.ad.rental_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pt.ipcb.ad.rental_service.dto.UserDto;

// "account-service" é o nome definido no application.properties do outro serviço
@FeignClient(name = "account-service", url = "http://localhost:8081")
public interface UserClient {

    @GetMapping("/users/{id}")
    UserDto getUserById(@PathVariable("id") Long id);
}