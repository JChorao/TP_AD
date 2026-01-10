package pt.ipcb.ad.account_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    @GetMapping("/test")
    public String test() {
        return "Olá! O Account Service (Car Sharing) está a funcionar via Gateway.";
    }
}