package hotel.db.repository.roommaintenance;

import hotel.db.entity.OrderDetail;
import hotel.db.entity.RoomMaintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RoomMaintenanceRepository extends JpaRepository<RoomMaintenance, Integer> {

    //Tìm tất cả các ngày đã được book phòng
    @Query("SELECT od FROM RoomMaintenance od " +
            "WHERE od.roomId = :roomId " +
            "AND od.status IN :statuses " +
            "AND od.endDate >= :fromDate " +
            "AND od.startDate <= :toDate " +
            "ORDER BY od.startDate ASC")
    List<RoomMaintenance> findMaintenancesByRoomAndDateRange(
            @Param("roomId") Integer roomId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("statuses") List<String> statuses
    );

    // Tìm maintenance đang active (đã bắt đầu, chưa kết thúc)
    @Query("SELECT rm FROM RoomMaintenance rm WHERE rm.status = :status " +
            "AND rm.startDate <= :now AND rm.endDate >= :now AND rm.isDeleted = false")
    List<RoomMaintenance> findActiveMaintenances(
            @Param("status") String status,
            @Param("now") LocalDateTime now
    );

    // Tìm maintenance đã hết hạn
    @Query("SELECT rm FROM RoomMaintenance rm WHERE rm.status = :status " +
            "AND rm.endDate < :now AND rm.isDeleted = false")
    List<RoomMaintenance> findExpiredMaintenances(
            @Param("status") String status,
            @Param("now") LocalDateTime now
    );
    
    // Lấy danh sách maintenance theo roomId, chưa bị xóa, sắp xếp theo ngày mới nhất
    List<RoomMaintenance> findByRoomIdAndIsDeletedFalseOrderByStartDateDesc(Integer roomId);
    
    // Tìm maintenance theo ID và chưa bị xóa
    Optional<RoomMaintenance> findByMaintenanceIdAndIsDeletedFalse(Integer maintenanceId);
    
    // Tìm các maintenance đã đến ngày bắt đầu (để scheduled job update status phòng)
    @Query("SELECT rm FROM RoomMaintenance rm WHERE rm.startDate <= :now " +
           "AND rm.endDate >= :now AND rm.isDeleted = false " +
           "AND rm.status = 'Dừng hoạt động'")
    List<RoomMaintenance> findMaintenancesToActivate(@Param("now") LocalDateTime now);
}
