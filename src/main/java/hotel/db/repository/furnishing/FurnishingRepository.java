package hotel.db.repository.furnishing;

import hotel.db.entity.Furnishing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FurnishingRepository extends JpaRepository<Furnishing, Integer> {
    List<Furnishing> findFurnishingsByIsDeletedFalse();
    Furnishing findFurnishingByFurnishingId(int id);
}
