package hotel.db.repository.hoteldetail;
import hotel.db.entity.Hotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    // Search by name (contains)
    Page<Hotel> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Filter by stars
    Page<Hotel> findByStars(Integer stars, Pageable pageable);

    // Filter by status
    Page<Hotel> findByStatus(String status, Pageable pageable);

    // Combined search and filter: name search + stars filter + status filter
    @Query("SELECT h FROM Hotel h WHERE " +
            "(:name IS NULL OR LOWER(h.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:stars IS NULL OR h.stars = :stars) AND " +
            "(:status IS NULL OR h.status = :status)")
    Page<Hotel> findByFilters(@Param("name") String name,
                              @Param("stars") Integer stars,
                              @Param("status") String status,
                              Pageable pageable);
}

