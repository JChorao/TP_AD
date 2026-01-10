package pt.ipcb.ad.veiculos; // Confirma o teu package

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import pt.ipcb.ad.veiculos.model.Vehicle;
import pt.ipcb.ad.veiculos.repository.VehicleRepository;

@SpringBootApplication
public class VeiculosApplication {

	public static void main(String[] args) {
		SpringApplication.run(VeiculosApplication.class, args);
	}

	// --- CÓDIGO NOVO AQUI EM BAIXO ---
	@Bean
	CommandLineRunner initDatabase(VehicleRepository repository) {
		return args -> {
			// Só insere se a base de dados estiver vazia
			if (repository.count() == 0) {
				repository.save(new Vehicle(null, "Tesla", "Model 3", "AA-00-ZE", true));
				repository.save(new Vehicle(null, "Renault", "Clio", "BB-11-CC", true));
				repository.save(new Vehicle(null, "BMW", "i3", "CC-22-DD", false)); // Um indisponível

				System.out.println(">>> Base de dados de Veículos populada com sucesso!");
			}
		};
	}
}