package hotel.db.repository.floor;

import hotel.db.entity.Floor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FloorRepository extends JpaRepository<Floor, Integer> {
	List<Floor> findAllByIsDeletedIsFalse();

	Floor findByFloorIdAndIsDeletedIsFalse(Integer id);

	Boolean existsByFloorNumberAndIsDeletedIsFalse(Integer floorNumber);

}
