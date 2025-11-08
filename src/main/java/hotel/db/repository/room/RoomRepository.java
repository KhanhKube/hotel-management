package hotel.db.repository.room;

import hotel.db.entity.Room;
import hotel.db.enums.RoomStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {

    List<Room> findAllByIsDeletedFalse();

    //check xem code voucher da ton tai hay chua.
    Boolean existsByRoomNumber(String roomNumber);

    //check xem code da bi ton tai ngoai code cua chinh no hay chua (dung cho update)
    boolean existsByRoomNumberAndRoomId(String roomNumber, Long roomId);

	List<Room> findByStatus(RoomStatus status);

	List<Room> findAllByIsDeletedIsFalse();

	Room findByRoomId(Integer id);

	List<Room> findTop3ByOrderBySoldDesc();

	List<Room> findAll();

    //soft delete
    @Modifying
    @Transactional
    @Query("UPDATE Room d SET d.isDeleted = true WHERE d.roomId = :id")
    void softDeleteById(@Param("id") Integer id);

	// Hard delete method - xóa vĩnh viễn khỏi database
	@Modifying
	@Query("DELETE FROM Room r WHERE r.roomId = :roomId")
	void hardDeleteRoom(@Param("roomId") Integer roomId);

	// Xóa các bản ghi liên quan đến room
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
	
	// ===== OPTIMIZED QUERIES FOR CHECKING MANAGEMENT =====
	
	// Find rooms by status with pagination (cho after check-out)
	@Query("SELECT r FROM Room r WHERE r.status = :status AND r.isDeleted = false")
	Page<Room> findByStatusAndIsDeletedFalse(@Param("status") String status, Pageable pageable);
	
	// Count rooms by status - optimized count query
	@Query("SELECT COUNT(r) FROM Room r WHERE r.status = :status AND r.isDeleted = false")
	long countByStatusAndIsDeletedFalse(@Param("status") String status);
}
