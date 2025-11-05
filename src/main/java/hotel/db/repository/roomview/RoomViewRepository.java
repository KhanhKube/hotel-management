package hotel.db.repository.roomview;

import hotel.db.entity.RoomView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomViewRepository extends JpaRepository<RoomView, Integer> {

    //Lấy các Id view của id phòng.
    @Query(value = "SELECT view_id FROM room_views WHERE room_id = :roomId AND is_deleted = 0", nativeQuery = true)
    List<Integer> findRoomViewId(@Param("roomId") Integer roomId);
}
