package hotel.service.room;

import hotel.db.dto.room.ListRoomResponse;
import hotel.db.dto.room.RoomListDto;
import hotel.db.entity.Room;

import java.util.List;

public interface RoomService {

	List<Room> getAllRooms();

    List<RoomListDto> getRoomList();

	ListRoomResponse getAllRoomForSearch();

	Room getRoomById(Integer roomId);

	Room createRoom(Room room);

	Room updateRoom(Integer roomId, Room room);

	// Hard delete method - xóa vĩnh viễn khỏi database
	void hardDeleteRoom(Integer roomId);


	// Check if room number exists
	boolean existsByRoomNumber(String roomNumber);
}
