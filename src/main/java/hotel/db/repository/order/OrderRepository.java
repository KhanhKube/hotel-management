package hotel.db.repository.order;

import hotel.db.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
	List<Order> findByUserId(Integer userId);

	List<Order> findByUserIdAndStatus(Integer userId, String status);

	void deleteByUserIdAndStatus(Integer userId, String status);

	@Query(value = "SELECT o.order_id, od.order_detail_id, o.check_in, o.check_out, " +
			"r.room_number, r.room_type, o.status, o.created_at " +
			"FROM orders o " +
			"JOIN order_details od ON o.order_id = od.order_id " +
			"JOIN rooms r ON od.room_id = r.room_id " +
			"WHERE o.user_id = :userId " +
			"ORDER BY o.created_at DESC",
			nativeQuery = true)
	List<Object[]> findBookingInfoByUserId(@Param("userId") Integer userId);

	@Query(value = "SELECT o.order_id, od.order_detail_id, o.check_in, o.check_out, " +
			"r.room_number, r.room_type, o.status, o.created_at " +
			"FROM orders o " +
			"JOIN order_details od ON o.order_id = od.order_id " +
			"JOIN rooms r ON od.room_id = r.room_id " +
			"ORDER BY o.created_at DESC",
			nativeQuery = true)
	List<Object[]> findAllBookingInfo();

	// Find orders by status and created before a certain time (for cart cleanup)
	List<Order> findByStatusAndCreatedAtBefore(String status, LocalDateTime createdAt);

	// Find orders by payment order code
	List<Order> findByPaymentOrderCode(Long paymentOrderCode);
}
