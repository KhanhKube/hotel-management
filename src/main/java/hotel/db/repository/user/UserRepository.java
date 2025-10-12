package hotel.db.repository.user;

import hotel.db.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findUsersByFirstName(String name);
    Boolean existsByEmail(String email);
    Boolean existsByUsername(String username);
    List<User> findByRole(String role);
    Boolean existsByPhoneAndUsernameNot(String phone, String username);
    Boolean existsByEmailAndUsernameNot(String email, String username);
    
    // Soft delete methods
    List<User> findByIsDeletedFalse();
    List<User> findByRoleAndIsDeletedFalse(String role);

}
