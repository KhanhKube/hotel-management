package hotel.db.repository.service;

import hotel.db.entity.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Integer> {

    // Find all services that are not deleted
    List<Service> findByIsDeletedFalse();

    // Find services with pagination
    Page<Service> findByIsDeletedFalse(Pageable pageable);

    // Search services by name
    Page<Service> findByServiceNameContainingIgnoreCaseAndIsDeletedFalse(String name, Pageable pageable);

    // Filter services by price range
    Page<Service> findByPriceBetweenAndIsDeletedFalse(Double minPrice, Double maxPrice, Pageable pageable);

    // Complex search with filters
    @Query("SELECT s FROM Service s WHERE s.isDeleted = false " +
            "AND (:name IS NULL OR LOWER(s.serviceName) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:minPrice IS NULL OR s.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR s.price <= :maxPrice)")
    Page<Service> findServicesWithFilters(@Param("name") String name,
                                          @Param("minPrice") Double minPrice,
                                          @Param("maxPrice") Double maxPrice,
                                          Pageable pageable);

    // Public service methods
    List<Service> findByIsFeaturedTrueAndIsDeletedFalse();
    List<Service> findByStatusAndIsDeletedFalse(String status);
}
