package hotel.service.room;

import hotel.db.entity.Room;

import java.util.List;

public interface RoomService {
    
    List<Room> getAllRooms();
    
    Room getRoomById(Integer roomId);
    
    Room createRoom(Room room);
    
    Room updateRoom(Integer roomId, Room room);
    
    void deleteRoom(Integer roomId);
    
    // Hard delete method - xóa vĩnh viễn khỏi database
    void hardDeleteRoom(Integer roomId);
    
    List<Room> getRoomsByStatus(String status);
}
