package hotel.db.repository.size;

import hotel.db.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SizeRepository extends JpaRepository<Size, Integer> {
    boolean existsBySize(Double size);
    List<Size> findBySizeAndSizeIdNot(Double size, Integer sizeId);
}