package hotel.db.repository.user;

import hotel.db.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    Boolean existsByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByPhone(String phone);
    List<User> findByRole(String role);
    Boolean existsByPhoneAndEmailNot(String phone, String email);
    Boolean existsByEmailAndPhoneNot(String email, String phone);
    Page<User> findAll(Specification<User> user, Pageable pageable);

    // Soft delete methods
    List<User> findByIsDeletedFalse();
    List<User> findByRoleAndIsDeletedFalse(String role);

}
