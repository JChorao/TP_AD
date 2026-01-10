package pt.ipcb.ad.account_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ipcb.ad.account_service.model.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // O JpaRepository já te dá o findAll(), save(), findById(), etc.

    // Sugestão (vai ser útil para o Login depois):
    // Permite encontrar um utilizador pelo email
    Optional<User> findByEmail(String email);
}