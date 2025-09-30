package hotel.db.repository.room;

import hotel.db.entity.Room;
import hotel.db.enums.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {
    // có thể thêm custom query nếu cần, ví dụ:
     List<Room> findByStatus(RoomStatus status);
}
