package hotel.db.repository.roomview;

import hotel.db.entity.RoomView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomViewRepository extends JpaRepository<RoomView, Integer> {
}
