package hotel.db.repository.furnishing;

import hotel.db.entity.Furnishing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FurnishingRepository extends JpaRepository<Furnishing, Integer> {
}
