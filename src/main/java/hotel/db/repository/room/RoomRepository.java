package hotel.db.repository.room;

import hotel.db.entity.Room;
import hotel.db.enums.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {
    List<Room> findByStatus(RoomStatus status);
    
    Boolean existsByRoomNumber(String roomNumber);
    
    // Hard delete method - xóa vĩnh viễn khỏi database
    @Modifying
    @Query("DELETE FROM Room r WHERE r.roomId = :roomId")
    void hardDeleteRoom(@Param("roomId") Integer roomId);
}
