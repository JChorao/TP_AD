package pt.ipcb.ad.account_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ipcb.ad.account_service.model.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Método necessário para o Login/Controller funcionar
    Optional<User> findByUsername(String username);

    // Pode manter este se quiser usar email no futuro, mas o username é o principal agora
    Optional<User> findByEmail(String email);
}