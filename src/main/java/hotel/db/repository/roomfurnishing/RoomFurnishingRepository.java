package hotel.db.repository.roomfurnishing;

import hotel.db.entity.RoomFurnishing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomFurnishingRepository extends JpaRepository<RoomFurnishing, Integer> {
}
