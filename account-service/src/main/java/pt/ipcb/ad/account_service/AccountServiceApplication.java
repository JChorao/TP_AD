package pt.ipcb.ad.account_service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder; // Importante para o login funcionar
import pt.ipcb.ad.account_service.model.User;
import pt.ipcb.ad.account_service.repository.UserRepository;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
@EnableDiscoveryClient
public class AccountServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AccountServiceApplication.class, args);
	}

	@Bean
	CommandLineRunner run(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			// Só cria utilizadores se a base de dados estiver vazia
			if (userRepository.count() == 0) {
				System.out.println(">>> Base de dados vazia. A criar utilizadores de teste...");

				// 1. ADMIN
				User admin = new User();
				admin.setUsername("admin");
				admin.setPassword(passwordEncoder.encode("admin123")); // Password encriptada
				admin.setEmail("admin@ipcb.pt");
				admin.setAddress("Serviços Centrais IPCB, Castelo Branco");
				admin.setPhoneNumber("910000000");
				admin.setBlocked(false);
				admin.setRoles(Set.of("ADMIN", "CONDUTOR"));
				admin.setLicenseNumber("P-22222222");
				admin.setLicenseIssueDate(LocalDate.of(2015, 5, 20));
				admin.setLicenseExpiryDate(LocalDate.of(2035, 5, 20));
				userRepository.save(admin);

				// 2. GESTOR (Manager)
				User gestor = new User();
				gestor.setUsername("gestor");
				gestor.setPassword(passwordEncoder.encode("gestor123"));
				gestor.setEmail("gestor@ipcb.pt");
				gestor.setAddress("Escola Superior de Tecnologia");
				gestor.setPhoneNumber("920000000");
				gestor.setBlocked(false);
				gestor.setRoles(Set.of("GESTOR_FROTA", "CONDUTOR"));
				gestor.setLicenseNumber("P-87654321");
				gestor.setLicenseIssueDate(LocalDate.of(2015, 5, 20));
				gestor.setLicenseExpiryDate(LocalDate.of(2035, 5, 20));
				userRepository.save(gestor);

				// 3. CONDUTOR (Driver)
				User condutor = new User();
				condutor.setUsername("condutor");
				condutor.setPassword(passwordEncoder.encode("condutor123"));
				condutor.setEmail("joao.condutor@email.com");
				condutor.setAddress("Av. do Empresário, Castelo Branco");
				condutor.setPhoneNumber("930000000");

				// Dados específicos de condutor
				condutor.setLicenseNumber("P-12345678");
				condutor.setLicenseIssueDate(LocalDate.of(2015, 5, 20));
				condutor.setLicenseExpiryDate(LocalDate.of(2035, 5, 20));
				condutor.setRating(4.8);

				// Localização (Castelo Branco)
				condutor.setLatitude(39.82219);
				condutor.setLongitude(-7.49087);

				condutor.setBlocked(false);
				userRepository.save(condutor);

				// 4. PASSAGEIRO (Passenger)
				User passageiro = new User();
				passageiro.setUsername("passageiro");
				passageiro.setPassword(passwordEncoder.encode("passageiro123"));
				passageiro.setEmail("maria.passageira@email.com");
				passageiro.setAddress("Rua da Sé, Castelo Branco");
				passageiro.setPhoneNumber("960000000");

				// Dados de passageiro
				passageiro.setRating(5.0);

				// Localização
				passageiro.setLatitude(39.82350);
				passageiro.setLongitude(-7.49300);

				passageiro.setBlocked(false);
				passageiro.setRoles(Set.of("PASSAGEIRO"));
				userRepository.save(passageiro);

				System.out.println(">>> 4 Utilizadores criados com sucesso: admin, gestor, condutor, passageiro.");
			}
		};
	}
}