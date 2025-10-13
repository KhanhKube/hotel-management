package hotel.service.room;

import hotel.db.entity.Room;
import hotel.db.repository.room.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Room> getAllRooms() {
        log.info("Getting all rooms");
        return roomRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Room getRoomById(Integer roomId) {
        log.info("Getting room by ID: {}", roomId);
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng với ID: " + roomId));
    }

    @Override
    public Room createRoom(Room room) {
        log.info("Creating new room: {}", room.getRoomNumber());
        
        // Kiểm tra room number có trùng không
        if (roomRepository.existsByRoomNumber(room.getRoomNumber())) {
            throw new RuntimeException("Số phòng đã tồn tại: " + room.getRoomNumber());
        }
        
        Room savedRoom = roomRepository.save(room);
        log.info("Created room with ID: {}", savedRoom.getRoomId());
        return savedRoom;
    }

    @Override
    public Room updateRoom(Integer roomId, Room room) {
        log.info("Updating room with ID: {}", roomId);
        
        Room existingRoom = getRoomById(roomId);
        
        // Kiểm tra room number có trùng không (trừ phòng hiện tại)
        if (!existingRoom.getRoomNumber().equals(room.getRoomNumber()) && 
            roomRepository.existsByRoomNumber(room.getRoomNumber())) {
            throw new RuntimeException("Số phòng đã tồn tại: " + room.getRoomNumber());
        }
        
        existingRoom.setRoomNumber(room.getRoomNumber());
        existingRoom.setRoomType(room.getRoomType());
        existingRoom.setBedType(room.getBedType());
        existingRoom.setFloorId(room.getFloorId());
        existingRoom.setSizeId(room.getSizeId());
        existingRoom.setPrice(room.getPrice());
        existingRoom.setStatus(room.getStatus());
        existingRoom.setRoomDescription(room.getRoomDescription());
        existingRoom.setSold(room.getSold() != null ? room.getSold() : existingRoom.getSold());
        existingRoom.setView(room.getView() != null ? room.getView() : existingRoom.getView());
        
        Room updatedRoom = roomRepository.save(existingRoom);
        log.info("Updated room with ID: {}", updatedRoom.getRoomId());
        return updatedRoom;
    }

    @Override
    public void deleteRoom(Integer roomId) {
        log.info("Deleting room with ID: {}", roomId);
        
        Room room = getRoomById(roomId);
        roomRepository.delete(room);
        log.info("Deleted room with ID: {}", roomId);
    }

    @Override
    @Transactional
    public void hardDeleteRoom(Integer roomId) {
        log.info("Hard deleting room with ID: {}", roomId);
        
        // Kiểm tra phòng có tồn tại không
        Room room = getRoomById(roomId);
        
        // Xóa vĩnh viễn khỏi database
        roomRepository.hardDeleteRoom(roomId);
        log.info("Hard deleted room with ID: {}", roomId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Room> getRoomsByStatus(String status) {
        log.info("Getting rooms by status: {}", status);
        // Convert String to RoomStatus enum
        hotel.db.enums.RoomStatus roomStatus;
        try {
            roomStatus = hotel.db.enums.RoomStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái phòng không hợp lệ: " + status);
        }
        return roomRepository.findByStatus(roomStatus);
    }
}
