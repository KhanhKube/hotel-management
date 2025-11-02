package hotel.db.repository.orderdetail;

import hotel.db.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import java.util.List;

import java.time.LocalDate;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    List<OrderDetail> findByOrderId(Integer orderId);

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
}
