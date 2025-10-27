package hotel.db.repository.orderdetail;

import hotel.db.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {

    // Dùng cho phần check validate để người dùng có thể book phòng.
    // Lấy end_date gần nhất của phòng (booking đang active hoặc sắp tới)
    @Query("SELECT CAST(MAX(od.endDate) AS LocalDate) FROM OrderDetail od " +
            "WHERE od.roomId = :roomId " +
            "AND od.endDate >= CURRENT_TIMESTAMP " +
            "AND od.status NOT IN ('CANCELLED', 'COMPLETED')")
    LocalDate findNextAvailableDateByRoomId(@Param("roomId") Integer roomId);
}
