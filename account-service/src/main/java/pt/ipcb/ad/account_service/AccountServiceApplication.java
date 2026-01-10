package pt.ipcb.ad.account_service; // Confirma o package

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import pt.ipcb.ad.account_service.model.User;
import pt.ipcb.ad.account_service.repository.UserRepository;

@SpringBootApplication
public class AccountServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AccountServiceApplication.class, args);
	}

	@Bean
	CommandLineRunner initUsers(UserRepository repository) {
		return args -> {
			if (repository.count() == 0) {
				// Criar um Administrador
				User admin = new User(null, "Administrador Chefe", "admin@ipcb.pt", "admin123", "910000000", "ADMIN");
				repository.save(admin);

				// Criar um Utilizador Normal (Cliente)
				User user = new User(null, "João Condutor", "joao@email.com", "user123", "960000000", "USER");
				repository.save(user);

				System.out.println(">>> Utilizadores de teste (ADMIN e USER) criados com sucesso!");
			}
		};
	}
}