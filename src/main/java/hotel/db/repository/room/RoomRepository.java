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

	// Hard delete method - xoa vinh vien khoi database
	@Modifying
	@Query("DELETE FROM Room r WHERE r.roomId = :roomId")
	void hardDeleteRoom(@Param("roomId") Integer roomId);

	// Xoa cac bang ghi lien quan den room
	@Modifying
	@Query(value = "DELETE FROM room_views WHERE room_id = :roomId", nativeQuery = true)
	void deleteRoomViewsByRoomId(@Param("roomId") Integer roomId);

	@Modifying
	@Query(value = "DELETE FROM room_furnishings WHERE room_id = :roomId", nativeQuery = true)
	void deleteRoomFurnishingsByRoomId(@Param("roomId") Integer roomId);

	@Modifying
	@Query(value = "DELETE FROM room_images WHERE room_id = :roomId", nativeQuery = true)
	void deleteRoomImagesByRoomId(@Param("roomId") Integer roomId);

	@Modifying
	@Query(value = "DELETE FROM order_details WHERE room_id = :roomId", nativeQuery = true)
	void deleteOrderDetailsByRoomId(@Param("roomId") Integer roomId);
}
