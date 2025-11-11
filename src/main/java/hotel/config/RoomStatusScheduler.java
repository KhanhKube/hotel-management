package hotel.config;

import hotel.db.entity.Room;
import hotel.db.entity.RoomMaintenance;
import hotel.db.repository.room.RoomRepository;
import hotel.db.repository.roommaintenance.RoomMaintenanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RoomStatusScheduler {

    @Autowired
    private RoomMaintenanceRepository roomMaintenanceRepository;

    @Autowired
    private RoomRepository roomRepository;

    // Chạy lúc 14:01 mỗi ngày - Bắt đầu bảo trì
    @Scheduled(cron = "0 1 14 * * *")
    public void startMaintenanceStatus() {
        LocalDateTime now = LocalDateTime.now();

        // Tìm các maintenance bắt đầu hôm nay
        List<RoomMaintenance> activeMaintenances = roomMaintenanceRepository
                .findActiveMaintenances("Đã giao", now);

        for (RoomMaintenance maintenance : activeMaintenances) {
            Room room = roomRepository.findById(maintenance.getRoomId()).orElse(null);
            if (room != null && !"Đang bảo trì".equals(room.getStatus())) {
                room.setStatus("Đang bảo trì");
                room.setSystemStatus("Bảo trì");
                roomRepository.save(room);
            }
        }
    }

    // Chạy lúc 12:00 mỗi ngày - Kết thúc bảo trì
    @Scheduled(cron = "0 0 12 * * *")
    public void endMaintenanceStatus() {
        LocalDateTime now = LocalDateTime.now();

        // Tìm các maintenance đã hết hạn
        List<RoomMaintenance> expiredMaintenances = roomMaintenanceRepository
                .findExpiredMaintenances("Đã giao", now);

        for (RoomMaintenance maintenance : expiredMaintenances) {
            maintenance.setStatus("Hoàn thành");
            roomMaintenanceRepository.save(maintenance);

            Room room = roomRepository.findById(maintenance.getRoomId()).orElse(null);
            if (room != null) {
                room.setStatus("Hoạt động");
                room.setSystemStatus("Hoạt động");
                roomRepository.save(room);
            }
        }
    }

    //Tự động đổi status đang dọn dẹp sang Hoạt động mỗi 14H
    @Scheduled(cron = "0 0 14 * * *")
    public void updateCleaningStatus() {
        // Tìm tất cả phòng đang "Đang dọn dẹp"
        List<Room> cleaningRooms = roomRepository.findAll().stream()
                .filter(room -> "Đang dọn dẹp".equals(room.getStatus()))
                .collect(Collectors.toList());

        // Chuyển sang "Hoạt động"
        for (Room room : cleaningRooms) {
            room.setStatus("Hoạt động");
            roomRepository.save(room);
        }
    }
}
