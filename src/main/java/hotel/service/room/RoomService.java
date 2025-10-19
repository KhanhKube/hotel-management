package hotel.service.room;

import hotel.db.dto.room.ListRoomResponse;
import hotel.db.dto.room.RoomDetailResponseDto;
import hotel.db.dto.room.RoomHomepageResponseDto;
import hotel.db.dto.room.RoomListDto;
import hotel.db.entity.Room;

import java.util.List;

public interface RoomService {

    List<RoomListDto> getRoomList();

	List<Room> getAllRooms();

    String[] getAllStatus();

    String[] getAllRoomTypes();

    String[] getAllBedTypes();

	ListRoomResponse getAllRoomForSearch();

	Room getRoomById(Integer roomId);

	Room createRoom(Room room);

	Room updateRoom(Integer roomId, Room room);

	// Hard delete method - xóa vĩnh viễn khỏi database
	void hardDeleteRoom(Integer roomId);


	// Check if room number exists
	boolean existsByRoomNumber(String roomNumber);

	List<RoomHomepageResponseDto> getTop3Rooms();

	RoomDetailResponseDto getRoomDetailById(Integer roomId);
}
