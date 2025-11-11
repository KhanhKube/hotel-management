package hotel.db.repository.furnishing;

import hotel.db.entity.Furnishing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FurnishingRepository extends JpaRepository<Furnishing, Integer> {

    // Search by name
    Page<Furnishing> findByNameContainingIgnoreCaseAndIsDeletedFalse(String name, Pageable pageable);

    // Find all not deleted
    Page<Furnishing> findByIsDeletedFalse(Pageable pageable);

    // Find by ID and not deleted
    Furnishing findByFurnishingIdAndIsDeletedFalse(Integer id);

    // Check if name exists (for validation)
    boolean existsByNameIgnoreCaseAndIsDeletedFalse(String name);

    // Check if name exists except itself (for update validation)
    boolean existsByNameIgnoreCaseAndFurnishingIdNotAndIsDeletedFalse(String name, Integer furnishingId);

    // Find with filters
    @Query("SELECT f FROM Furnishing f WHERE " +
            "(:search IS NULL OR :search = '' OR LOWER(f.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(f.furnishingDescription) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:minQuantity IS NULL OR f.quantity >= :minQuantity) AND " +
            "(:maxQuantity IS NULL OR f.quantity <= :maxQuantity) AND " +
            "f.isDeleted = false")
    Page<Furnishing> findWithFilters(@Param("search") String search,
                                     @Param("minQuantity") Integer minQuantity,
                                     @Param("maxQuantity") Integer maxQuantity,
                                     Pageable pageable);

    // Get all for dropdown (not deleted)
    List<Furnishing> findAllByIsDeletedFalseOrderByNameAsc();
}
