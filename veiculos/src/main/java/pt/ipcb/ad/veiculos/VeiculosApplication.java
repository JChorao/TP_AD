package pt.ipcb.ad.veiculos;

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

	@Bean
	CommandLineRunner initDatabase(VehicleRepository repository) {
		return args -> {
			// Só insere se a base de dados estiver vazia
			if (repository.count() == 0) {

				// --- VEÍCULO 1: TESLA (Centro C. Branco) ---
				Vehicle v1 = new Vehicle();
				v1.setBrand("Tesla");
				v1.setModel("Model 3");
				v1.setLicensePlate("AA-00-ZE");
				v1.setAvailable(true);
				v1.setLatitude(39.82219);
				v1.setLongitude(-7.49087);
				v1.setPricePerHour(15.0);
				repository.save(v1);

				// --- VEÍCULO 2: RENAULT (Politécnico/ESTCB) ---
				Vehicle v2 = new Vehicle();
				v2.setBrand("Renault");
				v2.setModel("Clio");
				v2.setLicensePlate("BB-11-CC");
				v2.setAvailable(true);
				v2.setLatitude(39.81977);
				v2.setLongitude(-7.50298);
				v2.setPricePerHour(5.0);
				repository.save(v2);

				// --- VEÍCULO 3: BMW (Indisponível - Estação) ---
				Vehicle v3 = new Vehicle();
				v3.setBrand("BMW");
				v3.setModel("i3");
				v3.setLicensePlate("CC-22-DD");
				v3.setAvailable(false); // Já está alugado ou em manutenção
				v3.setLatitude(39.82500);
				v3.setLongitude(-7.48000);
				v3.setPricePerHour(15.0);
				repository.save(v3);

				System.out.println(">>> Base de dados de Veículos populada com sucesso (com GPS e Preços)!");
			}
		};
	}
}