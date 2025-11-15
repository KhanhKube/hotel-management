package hotel.config;

import hotel.db.entity.Room;
import hotel.db.entity.RoomMaintenance;
import hotel.db.repository.room.RoomRepository;
import hotel.db.repository.roommaintenance.RoomMaintenanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomMaintenanceScheduler {

    private final RoomMaintenanceRepository roomMaintenanceRepository;
    private final RoomRepository roomRepository;

    /**
     * Chạy vào 14:00 mỗi ngày để check và update status phòng khi đến ngày dừng hoạt động
     */
    @Scheduled(cron = "0 0 14 * * *") // Chạy vào 14:00 mỗi ngày
    @Transactional
    public void updateRoomStatusForMaintenance() {
        log.info("=== Running scheduled job at 14:00: Update room status for maintenance ===");
        
        LocalDateTime now = LocalDateTime.now();
        
        // Tìm các maintenance đã đến ngày bắt đầu nhưng phòng chưa chuyển status
        List<RoomMaintenance> maintenances = roomMaintenanceRepository.findMaintenancesToActivate(now);
        
        log.info("Found {} maintenances to activate", maintenances.size());
        
        for (RoomMaintenance maintenance : maintenances) {
            try {
                Room room = roomRepository.findById(maintenance.getRoomId()).orElse(null);
                if (room == null) {
                    log.warn("Room not found for maintenance {}", maintenance.getMaintenanceId());
                    continue;
                }
                
                // Chỉ update nếu phòng đang ở trạng thái "Sắp dừng hoạt động"
                if ("Sắp dừng hoạt động".equals(room.getSystemStatus())) {
                    room.setStatus("Bảo trì khẩn");
                    room.setSystemStatus("Đã dừng hoạt động");
                    roomRepository.save(room);
                    
                    log.info("Updated room {} (Room #{}) to 'Đã dừng hoạt động' status", 
                        room.getRoomId(), room.getRoomNumber());
                }
            } catch (Exception e) {
                log.error("Error updating room status for maintenance {}", maintenance.getMaintenanceId(), e);
            }
        }
        
        log.info("=== Completed scheduled job: Updated {} rooms ===", maintenances.size());
    }
}
