package hotel.db.repository.size;

import hotel.db.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SizeRepository extends JpaRepository<Size, Integer> {
	List<Size> findAllByIsDeletedIsFalse();

	Size findBySizeIdAndIsDeletedIsFalse(Integer sizeId);

	Boolean existsBySizeAndIsDeletedIsFalse(BigDecimal sizeId);


	@Query(
			value = "SELECT * FROM sizes s " +
					"WHERE s.size BETWEEN :min AND :max " +
					"AND s.is_deleted = FALSE",
			nativeQuery = true
	)
	List<Size> findBySizeRangeAndIsDeletedIsFalse(@Param("min") BigDecimal min,
	                                              @Param("max") BigDecimal max);

}
