package hotel.db.repository.orderdetail;

import hotel.db.entity.OrderDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
	List<OrderDetail> findByOrderId(Integer orderId);

	// Query cho check-in list: PENDING, CART, CHECKED_IN với pagination
	@Query("SELECT od FROM OrderDetail od WHERE od.status IN ('PENDING', 'CART', 'CHECKED_IN') ORDER BY " +
			"CASE WHEN od.status IN ('PENDING', 'CART') THEN 0 ELSE 1 END, od.createdAt DESC")
	Page<OrderDetail> findCheckInList(Pageable pageable);

	// Count cho check-in
	long countByStatusIn(List<String> statuses);

	// Query cho check-out list: CHECKED_IN với pagination
	Page<OrderDetail> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

	// Count cho check-out
	long countByStatus(String status);

	// Query cho after check-out: tìm OrderDetail theo roomId và status CHECKED_OUT (giới hạn 1)
	@Query("SELECT od FROM OrderDetail od WHERE od.roomId = :roomId AND od.status = 'CHECKED_OUT' ORDER BY od.checkOut DESC")
	List<OrderDetail> findLatestCheckedOutByRoomId(@Param("roomId") Integer roomId);

	// Batch query: tìm tất cả OrderDetail CHECKED_OUT cho nhiều rooms (TỐI ƯU)
	@Query("SELECT od FROM OrderDetail od WHERE od.roomId IN :roomIds AND od.status = 'CHECKED_OUT' ORDER BY od.roomId, od.checkOut DESC")
	List<OrderDetail> findCheckedOutByRoomIds(@Param("roomIds") List<Integer> roomIds);

	// Dùng cho phần check validate để người dùng có thể book phòng.
	// Lấy end_date gần nhất của phòng (booking đang active hoặc sắp tới)
	@Query("SELECT CAST(MAX(od.endDate) AS LocalDate) FROM OrderDetail od " +
			"WHERE od.roomId = :roomId " +
			"AND od.endDate >= CURRENT_TIMESTAMP " +
			"AND od.status NOT IN ('CANCELLED', 'COMPLETED')")
	LocalDate findNextAvailableDateByRoomId(@Param("roomId") Integer roomId);

	//Check xem ngày có bị conflic ko.
	@Query("SELECT CASE WHEN EXISTS (" +
			"  SELECT 1 FROM OrderDetail od " +
			"  WHERE od.roomId = :roomId " +
			"  AND od.startDate < :endTime " +
			"  AND od.endDate > :startTime " +
			"  AND od.status NOT IN ('CANCELLED', 'COMPLETED')" +
			") THEN false ELSE true END")
	Boolean isRoomAvailableForToday(
			@Param("roomId") Integer roomId,
			@Param("startTime") LocalDateTime startTime,
			@Param("endTime") LocalDateTime endTime
	);

	//Tìm tất cả booking của phòng từ một ngày người dùng truy cập trang booking thể trở đi
	@Query("SELECT od FROM OrderDetail od " +
			"WHERE od.roomId = :roomId " +
			"AND od.endDate > :fromDate " +
			"AND od.status NOT IN ('CANCELLED', 'COMPLETED') " +
			"ORDER BY od.startDate ASC")
	List<OrderDetail> findUpcomingBookingsByRoomId(
			@Param("roomId") Integer roomId,
			@Param("fromDate") LocalDateTime fromDate
	);

	//Tìm tất cả các ngày đã được book phòng
	@Query("SELECT od FROM OrderDetail od " +
			"WHERE od.roomId = :roomId " +
			"AND od.status IN :statuses " +
			"AND od.endDate >= :fromDate " +
			"AND od.startDate <= :toDate " +
			"ORDER BY od.startDate ASC")
	List<OrderDetail> findBookingsByRoomAndDateRange(
			@Param("roomId") Integer roomId,
			@Param("fromDate") LocalDateTime fromDate,
			@Param("toDate") LocalDateTime toDate,
			@Param("statuses") List<String> statuses
	);
	
	// Query tìm bookings từ ngày disable trở đi để hủy
	@Query("SELECT od FROM OrderDetail od " +
			"WHERE od.roomId = :roomId " +
			"AND od.status IN ('CONFIRMED', 'CHECKED_IN') " +
			"AND od.startDate >= :disableDate " +
			"ORDER BY od.startDate ASC")
	List<OrderDetail> findBookingsToCancel(
			@Param("roomId") Integer roomId,
			@Param("disableDate") LocalDateTime disableDate
	);

	@Query(value = "SELECT room_id FROM order_details WHERE start_date < :endDate AND end_date > :startDate", nativeQuery = true)
	List<Integer> findRoomIdsByFilterEndateAndStatdate(
			@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate
	);

	// Find order details by room ID and status
	List<OrderDetail> findByRoomIdAndStatus(Integer roomId, String status);
}
