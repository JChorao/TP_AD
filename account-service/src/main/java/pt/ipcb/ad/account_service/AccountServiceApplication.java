package pt.ipcb.ad.account_service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import pt.ipcb.ad.account_service.model.User;
import pt.ipcb.ad.account_service.repository.UserRepository;

import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
@EnableDiscoveryClient
public class AccountServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AccountServiceApplication.class, args);
	}

	@Bean
	CommandLineRunner run(UserRepository userRepository) {
		return args -> {
			if (userRepository.count() == 0) {
				// Cria apenas um admin básico para garantir que o serviço arranca
				User admin = new User();
				admin.setUsername("admin");
				admin.setPassword("admin123");
				admin.setAddress("Admin Address");

				Set<String> roles = new HashSet<>();
				roles.add("ADMIN");
				admin.setRoles(roles);

				userRepository.save(admin);
				System.out.println(">>> Admin user created.");
			}
		};
	}
}