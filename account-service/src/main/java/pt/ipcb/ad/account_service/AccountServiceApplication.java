package pt.ipcb.ad.account_service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import pt.ipcb.ad.account_service.model.Role;
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

				// --- 1. ADMIN + CONDUTOR ---
				User adminUser = new User();
				adminUser.setName("Administrador Silva");
				adminUser.setEmail("admin@ipcb.pt");
				adminUser.setPassword("admin123");
				adminUser.setPhoneNumber("910000001");

				Set<Role> rolesAdmin = new HashSet<>();
				rolesAdmin.add(Role.ADMIN);
				rolesAdmin.add(Role.CONDUTOR);
				adminUser.setRoles(rolesAdmin);

				adminUser.setLatitude(39.82219);
				adminUser.setLongitude(-7.49087);

				// Rating Máximo para o Admin
				adminUser.setRating(5.0);

				userRepository.save(adminUser);

				// --- 2. CONDUTOR SIMPLES ---
				User driverUser = new User();
				driverUser.setName("Carlos Condutor");
				driverUser.setEmail("condutor@ipcb.pt");
				driverUser.setPassword("condutor123");
				driverUser.setPhoneNumber("960000002");

				Set<Role> rolesDriver = new HashSet<>();
				rolesDriver.add(Role.CONDUTOR);
				driverUser.setRoles(rolesDriver);

				driverUser.setLatitude(39.81977);
				driverUser.setLongitude(-7.50298);

				// Rating Bom
				driverUser.setRating(4.8);

				userRepository.save(driverUser);

				// --- 3. PASSAGEIRO SIMPLES ---
				User passengerUser = new User();
				passengerUser.setName("Ana Passageira");
				passengerUser.setEmail("passageiro@ipcb.pt");
				passengerUser.setPassword("passageiro123");
				passengerUser.setPhoneNumber("930000003");

				Set<Role> rolesPassenger = new HashSet<>();
				rolesPassenger.add(Role.PASSAGEIRO);
				passengerUser.setRoles(rolesPassenger);

				passengerUser.setLatitude(39.82500);
				passengerUser.setLongitude(-7.48000);

				// Passageiros podem começar sem rating (null) ou com 5.0
				passengerUser.setRating(null);

				userRepository.save(passengerUser);

				System.out.println("Utilizadores criados com Rating!");
			}
		};
	}
}