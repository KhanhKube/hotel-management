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
//    List<Service> findByHotel_Id(Long hotelId);
//
//    Page<Service> findByHotel_Id(Long hotelId, Pageable pageable);
//
//    Page<Service> findByHotel_IdAndServiceNameContainingIgnoreCase(Long hotelId, String name, Pageable pageable);
//
//    Page<Service> findByHotel_IdAndPriceBetween(Long hotelId, Double minPrice, Double maxPrice, Pageable pageable);
//
//    @Query("SELECT s FROM Service s WHERE s.hotel.id = :hotelId " +
//           "AND (:name IS NULL OR LOWER(s.serviceName) LIKE LOWER(CONCAT('%', :name, '%'))) " +
//           "AND (:minPrice IS NULL OR s.price >= :minPrice) " +
//           "AND (:maxPrice IS NULL OR s.price <= :maxPrice)")
//    Page<Service> findServicesWithFilters(@Param("hotelId") Long hotelId,
//                                          @Param("name") String name,
//                                          @Param("minPrice") Double minPrice,
//                                          @Param("maxPrice") Double maxPrice,
//                                          Pageable pageable);
}
