package hotel.db.repository.room;

import hotel.db.entity.Room;
import hotel.enums.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByStatus(RoomStatus status);
    boolean existsByRoomNumber(String roomNumber);
}
