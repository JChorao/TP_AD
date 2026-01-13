package pt.ipcb.ad.veiculos;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import pt.ipcb.ad.veiculos.model.Vehicle;
import pt.ipcb.ad.veiculos.repository.VehicleRepository;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class VeiculosApplication {

	public static void main(String[] args) {
		SpringApplication.run(VeiculosApplication.class, args);
	}

	@Bean
	public CommandLineRunner loadData(VehicleRepository repository) {
		return args -> {
			// Limpa dados antigos para garantir a posição correta
			if (repository.count() > 0) {
				repository.deleteAll();
				System.out.println(">>> Dados antigos limpos. A reposicionar veículos em estrada/estacionamento...");
			}

			List<Vehicle> veiculos = Arrays.asList(
					// 1. ESTCB - Estacionamento Principal (Acesso Av. do Empresário)
					createVehicle("Fiat", "500", "AA-22-BB", 6.50, 39.821360, -7.498835),

					// 2. Hospital Amato Lusitano - Parque de Estacionamento (Frente)
					createVehicle("Renault", "Clio", "CC-33-DD", 7.00, 39.825313, -7.491624),

					// 3. Estação CP - Estacionamento da Av. da Estação
					createVehicle("Peugeot", "208", "EE-44-FF", 7.50, 39.819385, -7.489125),

					// 4. Alegro Castelo Branco - Parque de Estacionamento Exterior
					createVehicle("Citroen", "C3", "GG-55-HH", 6.80, 39.816500, -7.520800),

					// 5. Castelo - Estacionamento na Rua do Castelo (Junto à entrada)
					createVehicle("VW", "Golf", "II-66-JJ", 12.00, 39.827725, -7.488344),

					// 6. Câmara Municipal - Estacionamento Praça do Município
					createVehicle("Toyota", "Corolla", "KK-77-LL", 13.50, 39.823589, -7.492534),

					// 7. Jardim do Paço - Estacionamento Rua Bartolomeu da Costa
					createVehicle("Mercedes", "Classe A", "MM-88-NN", 18.00, 39.825220, -7.494220),

					// 8. Piscina Praia - Parque de Estacionamento
					createVehicle("Tesla", "Model 3", "OO-99-PP", 25.00, 39.830605, -7.478635),

					// 9. Sé Catedral - Largo da Sé (Zona de paragem)
					createVehicle("Porsche", "911 Carrera", "QQ-00-RR", 65.00, 39.824150, -7.493180),

					// 10. Rotunda da Europa - Av. da Europa (Zona comercial adjacente)
					createVehicle("Ferrari", "488 Pista", "SS-11-TT", 120.00, 39.818200, -7.502000)
			);

			repository.saveAll(veiculos);
			System.out.println(">>> 10 Veículos estacionados em locais válidos (estrada/parque)!");
		};
	}

	private Vehicle createVehicle(String brand, String model, String plate, Double price, Double lat, Double lon) {
		Vehicle v = new Vehicle();
		v.setBrand(brand);
		v.setModel(model);
		v.setLicensePlate(plate);
		v.setPricePerHour(price);
		v.setLatitude(lat);
		v.setLongitude(lon);
		v.setAvailable(true);
		return v;
	}
}