package hotel.repository;

import hotel.db.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface DiscountRepository extends JpaRepository<Discount, Long> {
    Optional<Discount> findByCodeAndIsDeletedFalse(String code);
}
